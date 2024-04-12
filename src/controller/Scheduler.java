package controller;

import com.oocourse.elevator3.DoubleCarResetRequest;
import com.oocourse.elevator3.NormalResetRequest;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.ResetRequest;
import com.oocourse.elevator3.TimableOutput;
import servicer.Elevator;
import servicer.ElevatorStatus;
import servicer.Flag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Scheduler extends Thread {
    private final RequestQueue<Request> totalQueue;
    private final Random random = new Random();
    private RequestCount count;
    private final ArrayList<ArrayList<PersonRequest>> passengerQueues;
    private final ArrayList<RequestQueue<PersonRequest>> processingQueues;
    private final ArrayList<ElevatorStatus> runningStates;
    private final ArrayList<RequestQueue<PersonRequest>> exitHalfwayQueues;
    private final HashMap<Integer, Integer> transferFloor = new HashMap<>();
    // 换乘楼层, 电梯id - 楼层
    private final HashMap<Integer, RequestQueue<PersonRequest>> doubleCarQueues = new HashMap<>();
    // 电梯ID - 电梯ID-B 的已分配请求队列

    public Scheduler(RequestQueue<Request> totalQueue,
                     ArrayList<RequestQueue<PersonRequest>> processingQueues,
                     ArrayList<ElevatorStatus> runningStates,
                     ArrayList<RequestQueue<PersonRequest>> exitHalfwayQueues,
                     ArrayList<ArrayList<PersonRequest>> passengerQueues,
                     RequestCount count) {
        this.totalQueue = totalQueue;
        this.processingQueues = processingQueues;
        this.runningStates = runningStates;
        this.exitHalfwayQueues = exitHalfwayQueues;
        this.passengerQueues = passengerQueues;
        this.count = count;

        for (int i = 1; i < 7; i++) {
            transferFloor.put(i, 0);
        }
    }

    @Override
    public void run() {
        while (true) {
            // reAddPerson();

            if (!isElevatorRunning()) {
                totalQueue.setRunningEnd(true);
            }

            if (totalQueue.isEnd() && count.getCnt() == 0 && !isElevatorResetting()) {
                for (int i = 0; i < processingQueues.size(); i++) {
                    processingQueues.get(i).setEnd(true);
                    runningStates.get(i).setOver();
                }
                for (RequestQueue<PersonRequest> doubleCarQueue : doubleCarQueues.values()) {
                    doubleCarQueue.setEnd(true);
                }
                // System.out.println(getName() + " over");
                return;
            }

            Request request = totalQueue.getOneTotalRequestAndRemove();

            if (request == null) {
                continue;
            }

            if (request instanceof ResetRequest) {
                if (request instanceof NormalResetRequest) {
                    processNormalReset((NormalResetRequest) request);
                } else {
                    processDoubleCarReset((DoubleCarResetRequest) request);
                }
            }

            else if (request instanceof PersonRequest) {
                if (runningStates.size() - getResetNum() < 2) {
                    try {
                        sleep(1200);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                int elevatorId = random.nextInt(5) + 1;

                synchronized (runningStates.get(elevatorId - 1)) {
                    if (runningStates.get(elevatorId - 1).getReset() != 0) {
                        totalQueue.addRequest(request);
                        continue;
                    }
                }

                dispatchPersonRequest(elevatorId, (PersonRequest) request);

            }
        }
    }

    public void dispatchPersonRequest(int elevatorId, PersonRequest request) {
        if (transferFloor.get(elevatorId) == 0) {
            processingQueues.get(elevatorId - 1).addRequest(request);
            TimableOutput.println("RECEIVE-" + request.getPersonId()
                    + "-" + elevatorId);
        } else {
            int transFloor = transferFloor.get(elevatorId);
            int fromFloor = request.getFromFloor();
            if (fromFloor < transFloor) {
                processingQueues.get(elevatorId - 1).addRequest(request);
                TimableOutput.println("RECEIVE-" + request.getPersonId()
                        + "-" + elevatorId + "-A");
            } else if (fromFloor > transFloor) {
                doubleCarQueues.get(elevatorId).addRequest(request);
                TimableOutput.println("RECEIVE-" + request.getPersonId()
                        + "-" + elevatorId + "-B");
            } else {
                int toFloor = request.getToFloor();
                if (toFloor < transFloor) {
                    processingQueues.get(elevatorId - 1).addRequest(request);
                    TimableOutput.println("RECEIVE-" + request.getPersonId()
                            + "-" + elevatorId + "-A");
                } else {
                    doubleCarQueues.get(elevatorId).addRequest(request);
                    TimableOutput.println("RECEIVE-" + request.getPersonId()
                            + "-" + elevatorId + "-B");
                }
            }
        }
    }

    public void processNormalReset(NormalResetRequest request) {
        ElevatorStatus elevatorStatus = runningStates.
                get(request.getElevatorId() - 1);
        elevatorStatus.setResetInfo(request);
        elevatorStatus.setReset(1);
        processingQueues.get(request.getElevatorId() - 1).wake();
    }

    public void processDoubleCarReset(DoubleCarResetRequest request) {
        int elevatorId = request.getElevatorId();
        Flag occupied = new Flag();
        ElevatorStatus elevatorStatus = runningStates.get(elevatorId - 1);

        elevatorStatus.setResetInfo(request);
        elevatorStatus.setReset(2);
        elevatorStatus.setOccupied(occupied);
        processingQueues.get(request.getElevatorId() - 1).wake();


        transferFloor.put(elevatorId, request.getTransferFloor());
        createDoubleCarElevator(elevatorId, occupied, request);
    }

    public void reAddPerson() {
        for (int i = 0; i < exitHalfwayQueues.size(); i++) {
            RequestQueue<PersonRequest> exitHalfwayPassengers = exitHalfwayQueues.get(i);
            synchronized (exitHalfwayPassengers) {
                if (exitHalfwayPassengers.isEmpty()) {
                    continue;
                }
                while (!exitHalfwayPassengers.isEmpty()) {
                    PersonRequest personRequest = exitHalfwayPassengers.getOneRequestAndRemove();
                    totalQueue.addRequest(personRequest);
                }
            }
        }
    }

    private void createDoubleCarElevator(int elevatorId, Flag occupied,
                                         DoubleCarResetRequest request) {
        RequestQueue<PersonRequest> newQueue = new RequestQueue<>(); // 待处理队伍
        doubleCarQueues.put(elevatorId, newQueue);

        RequestQueue<PersonRequest> newExitHalfway = new RequestQueue<>();
        exitHalfwayQueues.add(newExitHalfway);

        ElevatorStatus elevatorStatus = new ElevatorStatus((int)(request.getSpeed() * 1000), true,
                request.getCapacity(), 0);
        elevatorStatus.setOccupied(occupied);
        elevatorStatus.setTransferFloor(request.getTransferFloor());
        ArrayList<PersonRequest> passengers = new ArrayList<>();
        passengerQueues.add(passengers);

        Elevator elevator = new Elevator(newQueue, elevatorId,
                newExitHalfway, passengers, totalQueue, count);
        elevator.setName(elevatorId + "-B");
        elevator.setDoubleCarStatus(elevatorStatus);

        elevator.start();
    }

    public boolean isElevatorResetting() {
        for (ElevatorStatus status : runningStates) {
            if (status.getReset() != 0) {
                return true;
            }
        }
        return false;
    }

    public int getResetNum() {
        int ret = 0;
        for (ElevatorStatus status : runningStates) {
            if (status.getReset() != 0) {
                ret++;
            }
        }
        return ret;
    }

    public boolean isElevatorRunning() {
        for (RequestQueue<PersonRequest> requestQueue : processingQueues) {
            if (!requestQueue.isEmptyNoNotify()) {
                return true;
            }
        }

        for (RequestQueue<PersonRequest> requestQueue : doubleCarQueues.values()) {
            if (!requestQueue.isEmptyNoNotify()) {
                return true;
            }
        }
        for (ArrayList<PersonRequest> passengers : passengerQueues) {
            if (!passengers.isEmpty()) {
                return true;
            }
        }
        return false;
    }

}

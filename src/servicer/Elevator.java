package servicer;

import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import controller.RequestCount;
import controller.RequestQueue;
import strategy.Advice;
import strategy.LookStrategy;
import com.oocourse.elevator3.TimableOutput;
import strategy.Strategy;

import java.util.ArrayList;
import java.util.Iterator;

public class Elevator extends Thread {
    private static final int ELEVATOR_TYPE_D = 3;
    private static final int ELEVATOR_TYPE_S = 2;
    private final RequestQueue<Request> totalQueue;
    private int elevatorType;
    private Strategy strategy; // 采取的策略
    private ElevatorStatus status;
    private RequestCount count;
    private Flag occupied;
    private final ArrayList<PersonRequest> passengers; // 乘客
    private final RequestQueue<PersonRequest> exitHalfwayPassengers; //
    private final RequestQueue<PersonRequest> requests; // 待处理
    private final int elevatorId; // 当前电梯ID
    private Integer curFloor; // 当前所在楼层
    // private boolean moveDirection; // 移动方向，0 -> down ; 1 -> up

    public Elevator(RequestQueue<PersonRequest> requests, int elevatorId,
                    RequestQueue<PersonRequest> exitHalfwayPassengers,
                    ArrayList<PersonRequest> passengers, Flag occupied,
                    RequestQueue<Request> totalQueue, RequestCount count) {
        this.elevatorId = elevatorId;
        this.passengers = passengers;
        this.requests = requests;
        this.exitHalfwayPassengers = exitHalfwayPassengers;
        this.totalQueue = totalQueue;
        this.count = count;
        this.occupied = occupied;

        this.curFloor = 1;
    }

    @Override
    public void run() {
        while (true) {
            if (elevatorType == ELEVATOR_TYPE_S) {
                checkReset();
            }

            Advice advice;
            synchronized (requests) {
                advice = strategy.getAdvice(curFloor, status.getMoveDirection());
            }
            if (advice == Advice.OVER) {
                totalQueue.wake();
                return;
            } else if (advice == Advice.MOVE) {
                move();
            } else if (advice == Advice.UTURN) {
                status.reverseMoveDirection();
            } else if (advice == Advice.WAIT) {
                if (elevatorType == ELEVATOR_TYPE_D && curFloor == status.getTransferFloor()) {
                    move();
                    continue;
                }
                requests.waitRequest();

            } else if (advice == Advice.OPEN) {
                openAndClose();
            } else if (advice == Advice.TRANSFER) {
                transfer();
            }
        }
    }

    public void transfer() {
        TimableOutput.println("OPEN-" + curFloor + "-" + getName());

        status.wake();

        if (!passengers.isEmpty()) {
            cleanPassengers();
        }
        status.reverseMoveDirection();
        requestEnterByFromFloor();

        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        status.wake();

        TimableOutput.println("CLOSE-" + curFloor + "-" + getName());

        move();
        totalQueue.wake();
    }

    public void checkReset() {
        int resetType = status.getReset();
        if (resetType != 0) {
            if (!passengers.isEmpty()) {
                TimableOutput.println("OPEN-" + curFloor + "-" + getName());
                cleanPassengers();

                try {
                    sleep(400);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                status.wake();

                TimableOutput.println("CLOSE-" + curFloor + "-" + getName());
            }
            TimableOutput.println("RESET_BEGIN-" + elevatorId);
            cleanRequests();
            try {
                sleep(1200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            status.resetStatus();
            if (resetType == 2) { // DCReset, -> A
                elevatorType = ELEVATOR_TYPE_D;
                curFloor = status.getTransferFloor() - 1;
                status.setMoveDirection(false);
                setName(elevatorId + "-A");
            }

            TimableOutput.println("RESET_END-" + elevatorId);
        }
    }

    public void cleanPassengers() {
        int sum = passengers.size();
        int halfWay = 0;
        for (PersonRequest personRequest : passengers) {
            TimableOutput.println("OUT-" +
                    personRequest.getPersonId() + "-" + curFloor + "-" + getName());
        }

        Iterator<PersonRequest> iterator = passengers.iterator();
        while (iterator.hasNext()) {
            PersonRequest personRequest = iterator.next();

            if (personRequest.getToFloor() != curFloor) {
                halfWay++;
                PersonRequest newPerson = new PersonRequest(curFloor,
                        personRequest.getToFloor(), personRequest.getPersonId());
                totalQueue.addRequest(newPerson);
            }
            iterator.remove();
            status.finishOneRequest();
        }

        synchronized (count) {
            count.finish(sum - halfWay);
        }

    }

    public void setNormalStatus() {
        this.status = new ElevatorStatus(400, true, 6, 0);
        this.strategy = new LookStrategy(this.requests, this.passengers,
                this.status, this.exitHalfwayPassengers);

        this.elevatorType = ELEVATOR_TYPE_S;

    }

    public void setDoubleCarStatus(ElevatorStatus status) {
        this.status = status;
        this.strategy = new LookStrategy(this.requests, this.passengers,
                this.status, this.exitHalfwayPassengers);
        this.elevatorType = ELEVATOR_TYPE_D;

        curFloor = status.getTransferFloor() + 1;
    }

    public void cleanRequests() {
        // 已有请求
        while (!requests.isEmpty()) {
            PersonRequest personRequest = requests.getOnePersonAndRemoveNoWait();
            totalQueue.addRequest(personRequest);
        }
    }

    public void move() {
        try {
            this.sleep(status.getMoveOneFloorTime());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (status.getMoveDirection()) {
            curFloor++;
        } else {
            curFloor--;
        }
        if (elevatorType == ELEVATOR_TYPE_D && curFloor == status.getTransferFloor()) {
            synchronized (occupied) {
                occupied.setOccupied();
            }
        }
        TimableOutput.println("ARRIVE-" + curFloor + "-" + getName());
        if (elevatorType == ELEVATOR_TYPE_D &&
                Math.abs(curFloor - status.getTransferFloor()) == 1) {
            synchronized (occupied) {
                occupied.setRelease();
            }
        }
    }

    public void openAndClose() {
        TimableOutput.println("OPEN-" + curFloor + "-" + getName());

        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        finishRequestsByToFloorAndRemove(curFloor); // 出

        requestEnterByFromFloor(); // 进

        TimableOutput.println("CLOSE-" + curFloor + "-" + getName());
    }

    public void requestEnterByFromFloor() {
        synchronized (requests) {
            if (passengers.size() < status.getFullLoadLimit()) {
                PersonRequest personRequest = requests.getOneRequestByFromFloorAndRemove(curFloor,
                        status.getMoveDirection());
                while (personRequest != null && passengers.size() < status.getFullLoadLimit()) {
                    TimableOutput.println("IN-" + personRequest.getPersonId()
                            + "-" + curFloor + "-" + getName());
                    passengers.add(personRequest);
                    status.addOnePerson();
                    if (passengers.size() < status.getFullLoadLimit()) {
                        personRequest = requests.getOneRequestByFromFloorAndRemove(curFloor,
                                status.getMoveDirection());
                    }
                }
            }
        }
    }

    public void finishRequestsByToFloorAndRemove(int curFloor) {
        Iterator<PersonRequest> iterator = passengers.iterator();
        while (iterator.hasNext()) {
            PersonRequest personRequest = iterator.next();
            if (personRequest.getToFloor() == curFloor) {
                iterator.remove();
                status.finishOneRequest();
                TimableOutput.println("OUT-" +
                        personRequest.getPersonId() + "-" + curFloor + "-" + getName());
                synchronized (count) {
                    count.finish();
                }
            }
        }
        totalQueue.wake();
    }

    public ElevatorStatus getStatus() {
        synchronized (status) {
            status.notifyAll();
            return this.status;
        }
    }

    @Override
    public String toString() {
        String eleID = "电梯 ID 为" + currentThread().getName() + "\n";
        String passengerNum = "电梯里还有" + passengers.size() + "名乘客\n";
        String waitNum = "等待人数为 " + requests.getRequestQueue().size() + "\n";
        String moveDir = "移动方向为" + status.getMoveDirection() + "\n";
        String sta = "status: " + status;
        return eleID + passengerNum + waitNum + moveDir + sta;
    }
}

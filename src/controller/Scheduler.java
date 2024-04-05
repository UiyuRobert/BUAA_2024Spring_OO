package controller;

import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ResetRequest;
import com.oocourse.elevator2.TimableOutput;
import servicer.ElevatorStatus;
import java.util.ArrayList;

public class Scheduler extends Thread {
    private final RequestQueue<Request> totalQueue;
    private final ArrayList<PersonRequestQueue> processingQueues;
    private final ArrayList<ElevatorStatus> runningStates;
    private final ArrayList<PersonRequestQueue> exitHalfwayQueues;

    public Scheduler(RequestQueue<Request> totalQueue,
                     ArrayList<PersonRequestQueue> processingQueues,
                     ArrayList<ElevatorStatus> runningStates,
                     ArrayList<PersonRequestQueue> exitHalfwayQueues) {
        this.totalQueue = totalQueue;
        this.processingQueues = processingQueues;
        this.runningStates = runningStates;
        this.exitHalfwayQueues = exitHalfwayQueues;
    }

    @Override
    public void run() {
        while (true) {
            reAddPerson();

            if (totalQueue.isEmpty() && totalQueue.isEnd() && !isElevatorResetting()) {
                for (int i = 0; i < processingQueues.size(); i++) {
                    processingQueues.get(i).setEnd(true);
                    exitHalfwayQueues.get(i).setEnd(true);
                }
                return;
            }

            Request request = totalQueue.getOneRequestAndRemove();
            if (request == null) {
                continue;
            }
            if (request instanceof ResetRequest) {
                ElevatorStatus elevatorStatus = runningStates.
                        get(((ResetRequest) request).getElevatorId() - 1);
                elevatorStatus.setResetInfo((ResetRequest) request);
                elevatorStatus.setReset(true);
                processingQueues.get(((ResetRequest) request).getElevatorId() - 1).wake();
            } else if (request instanceof PersonRequest) {
                double priority = 0.0;
                int elevatorId = -1;
                for (int i = 0; i < processingQueues.size(); i++) {
                    ElevatorStatus status = runningStates.get(i);
                    int waitNum = processingQueues.get(i).getSize();
                    double newPriority = calculatePriority(status, waitNum);
                    if (newPriority > priority) {
                        priority = newPriority;
                        elevatorId = i;
                    }
                }
                processingQueues.get(elevatorId).addRequest((PersonRequest) request);
                TimableOutput.println("RECEIVE-" + ((PersonRequest) request).getPersonId()
                        + "-" + (elevatorId + 1));
            }
        }
    }

    public void reAddPerson() {
        for (int i = 0; i < exitHalfwayQueues.size(); i++) {
            PersonRequestQueue exitHalfwayPassengers = exitHalfwayQueues.get(i);
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

    public double calculatePriority(ElevatorStatus status, int waitNum) {
        double spaceCount = 0.3 * (1 -
                ((double) status.getCurrentLoadCount() / status.getFullLoadLimit()));
        double waitCount = Math.exp(-1.0 * waitNum / status.getFullLoadLimit());
        double speedCount = 2.1 - 3.5 * status.getMoveOneFloorTime() / 1000;
        if (status.isReset()) {
            return 0.7 * (speedCount + spaceCount + waitCount);
        }
        return speedCount + spaceCount + waitCount;
    }

    public boolean isElevatorResetting() {
        for (ElevatorStatus status : runningStates) {
            if (status.isReset()) {
                return true;
            }
        }
        return false;
    }
}

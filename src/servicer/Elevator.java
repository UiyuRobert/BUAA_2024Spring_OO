package servicer;

import com.oocourse.elevator2.PersonRequest;
import controller.PersonRequestQueue;
import strategy.Advice;
import strategy.LookStrategy;
import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;
import java.util.Iterator;

public class Elevator extends Thread {
    private LookStrategy strategy; // 采取的策略
    private final ElevatorStatus status;
    private ArrayList<PersonRequest> passengers; // 乘客
    private final PersonRequestQueue exitHalfwayPassengers; //
    private final PersonRequestQueue requests; // 待处理
    private final int elevatorId; // 当前电梯ID
    private int curFloor; // 当前所在楼层
    // private boolean moveDirection; // 移动方向，0 -> down ; 1 -> up

    public Elevator(PersonRequestQueue requests, int elevatorId,
                    PersonRequestQueue exitHalfwayPassengers) {
        this.elevatorId = elevatorId;
        this.passengers = new ArrayList<>();
        this.requests = requests;
        this.exitHalfwayPassengers = exitHalfwayPassengers;
        this.status = new ElevatorStatus(400, true,
                6, 0);
        this.strategy = new LookStrategy(this.requests, this.passengers, this.status);

        this.curFloor = 1;
    }

    @Override
    public void run() {
        while (true) {
            Advice advice;
            synchronized (requests) {
                advice = strategy.getAdvice(curFloor, status.getMoveDirection());
                if (checkReset()) {
                    continue;
                }
            }
            if (advice == Advice.OVER) {
                return;
            } else if (advice == Advice.MOVE) {
                move();
            } else if (advice == Advice.UTURN) {
                status.reverseMoveDirection();
            } else if (advice == Advice.WAIT) {
                requests.waitRequest();
            } else if (advice == Advice.OPEN) {
                openAndClose();
            }
        }
    }

    public boolean checkReset() {
        if (status.isReset()) {
            if (!passengers.isEmpty()) {
                cleanPassengers();
            }
            TimableOutput.println("RESET_BEGIN-" + elevatorId);
            cleanRequests();
            try {
                sleep(1200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            status.resetStatus();
            TimableOutput.println("RESET_END-" + elevatorId);
            return true;
        }
        return false;
    }

    public void cleanPassengers() {
        TimableOutput.println("OPEN-" + curFloor + "-" + elevatorId);

        Iterator<PersonRequest> iterator = passengers.iterator();
        while (iterator.hasNext()) {
            PersonRequest personRequest = iterator.next();

            if (personRequest.getToFloor() != curFloor) {
                PersonRequest newPerson = new PersonRequest(curFloor,
                        personRequest.getToFloor(), personRequest.getPersonId());
                exitHalfwayPassengers.addRequestButNotNotify(newPerson);
            }
            iterator.remove();
            status.finishOneRequest();
            TimableOutput.println("OUT-" +
                    personRequest.getPersonId() + "-" + curFloor + "-" + elevatorId);
        }

        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        exitHalfwayPassengers.wake();

        TimableOutput.println("CLOSE-" + curFloor + "-" + elevatorId);
    }

    public void cleanRequests() {
        // 已有请求
        while (!requests.isEmpty()) {
            PersonRequest personRequest = requests.getOnePersonAndRemoveNoWait();
            exitHalfwayPassengers.addRequest(personRequest);
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
        TimableOutput.println("ARRIVE-" + curFloor + "-" + elevatorId);
    }

    public void openAndClose() {
        TimableOutput.println("OPEN-" + curFloor + "-" + elevatorId);
        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        finishRequestsByToFloorAndRemove(curFloor);
        synchronized (requests) {
            if (passengers.size() < status.getFullLoadLimit()) {
                PersonRequest personRequest = requests.getOneRequestByFromFloorAndRemove(curFloor,
                        status.getMoveDirection());
                while (personRequest != null && passengers.size() < status.getFullLoadLimit()) {
                    TimableOutput.println("IN-" + personRequest.getPersonId()
                            + "-" + curFloor + "-" + elevatorId);
                    passengers.add(personRequest);
                    status.addOnePerson();
                    if (passengers.size() < status.getFullLoadLimit()) {
                        personRequest = requests.getOneRequestByFromFloorAndRemove(curFloor,
                                status.getMoveDirection());
                    }
                }
            }
        }
        TimableOutput.println("CLOSE-" + curFloor + "-" + elevatorId);
    }

    public void finishRequestsByToFloorAndRemove(int curFloor) {
        Iterator<PersonRequest> iterator = passengers.iterator();
        while (iterator.hasNext()) {
            PersonRequest personRequest = iterator.next();
            if (personRequest.getToFloor() == curFloor) {
                iterator.remove();
                status.finishOneRequest();
                TimableOutput.println("OUT-" +
                        personRequest.getPersonId() + "-" + curFloor + "-" + elevatorId);
            }
        }
    }

    public synchronized ElevatorStatus getStatus() {
        return this.status;
    }

    @Override
    public String toString() {
        String eleID = "电梯 ID 为" + currentThread().getName() + "\n";
        String passengerNum = "电梯里还有" + passengers.size() + "名乘客\n";
        String waitNum = "等待人数为 " + requests.getRequestQueue().size() + "\n";
        String moveDir = "移动方向为" + status.getMoveDirection() + "\n";
        String sta = "status: " + status.toString();
        return eleID + passengerNum + waitNum + moveDir + sta;
    }
}

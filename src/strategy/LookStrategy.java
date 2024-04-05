package strategy;

import com.oocourse.elevator2.PersonRequest;
import controller.PersonRequestQueue;
import servicer.ElevatorStatus;

import java.util.ArrayList;

public class LookStrategy {
    private PersonRequestQueue processingQueue; // 处理中队列
    private ArrayList<PersonRequest> passengerQueue; // 乘客队列
    private ElevatorStatus elevatorStatus;

    public LookStrategy(PersonRequestQueue processingQueue,
                        ArrayList<PersonRequest> passengerQueue,
                        ElevatorStatus elevatorStatus) {
        this.processingQueue = processingQueue;
        this.passengerQueue = passengerQueue;
        this.elevatorStatus = elevatorStatus;
    }

    public Advice getAdvice(int curFloor, boolean moveDirection) {
        if (canOpenForOut(curFloor) || canOpenForIn(curFloor, moveDirection)) {
            return Advice.OPEN;
        }
        if (!passengerQueue.isEmpty()) {
            return Advice.MOVE;
        } else {
            if (processingQueue.isEmpty()) {
                if (processingQueue.isEnd()) {
                    return Advice.OVER;
                } else {
                    return Advice.WAIT;
                }
            }
            if (hasReqInOriginDirection(curFloor, moveDirection)) {
                return Advice.MOVE;
            } else {
                return Advice.UTURN;
            }
        }
    }

    public boolean canOpenForOut(int curFloor) {
        for (PersonRequest personRequest : passengerQueue) {
            if (personRequest.getToFloor() == curFloor) {
                return true;
            }
        }
        return false;
    }

    public boolean canOpenForIn(int curFloor, boolean moveDirection) {
        if (passengerQueue.size() == elevatorStatus.getFullLoadLimit()) { // 人满了
            return false;
        } else {
            synchronized (processingQueue) {
                for (PersonRequest personRequest : processingQueue.getRequestQueue()) {
                    if (personRequest.getFromFloor() == curFloor &&
                            getMoveDirection(personRequest) == moveDirection) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public boolean hasReqInOriginDirection(int curFloor, boolean moveDirection) {
        synchronized (processingQueue) {
            for (PersonRequest personRequest : processingQueue.getRequestQueue()) {
                if (personRequest.getFromFloor() > curFloor && moveDirection) {
                    return true;
                } else if (personRequest.getFromFloor() < curFloor && !moveDirection) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean getMoveDirection(com.oocourse.elevator2.PersonRequest personRequest) {
        return (personRequest.getFromFloor() < personRequest.getToFloor());
    }

}

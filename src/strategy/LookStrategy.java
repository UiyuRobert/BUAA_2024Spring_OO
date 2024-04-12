package strategy;

import com.oocourse.elevator3.PersonRequest;
import controller.RequestQueue;
import servicer.ElevatorStatus;

import java.util.ArrayList;

public class LookStrategy implements Strategy {
    private final RequestQueue<PersonRequest> processingQueue; // 处理中队列
    private final ArrayList<PersonRequest> passengerQueue; // 乘客队列
    private final ElevatorStatus elevatorStatus;
    private final RequestQueue<PersonRequest> exitHalfway;

    public LookStrategy(RequestQueue<PersonRequest> processingQueue,
                        ArrayList<PersonRequest> passengerQueue,
                        ElevatorStatus elevatorStatus,
                        RequestQueue<PersonRequest> exitHalfway) {
        this.processingQueue = processingQueue;
        this.passengerQueue = passengerQueue;
        this.elevatorStatus = elevatorStatus;
        this.exitHalfway = exitHalfway;
    }

    @Override
    public Advice getAdvice(int curFloor, boolean moveDirection) {
        if (curFloor == elevatorStatus.getTransferFloor()) {
            return Advice.TRANSFER;
        }
        if (canOpenForOut(curFloor) || canOpenForIn(curFloor, moveDirection)) {
            return Advice.OPEN;
        }
        if (!passengerQueue.isEmpty()) {
            return Advice.MOVE;
        } else {
            if (processingQueue.isEmpty()) {
                if (processingQueue.isEnd()) {
                    // System.out.println(Thread.currentThread().getName() + "over");
                    return Advice.OVER;
                } else {
                    // System.out.println(Thread.currentThread().getName() + "wait");
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

    public boolean getMoveDirection(com.oocourse.elevator3.PersonRequest personRequest) {
        return (personRequest.getFromFloor() < personRequest.getToFloor());
    }

}

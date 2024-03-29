package strategy;

import controller.Request;
import controller.RequestQueue;
import java.util.ArrayList;

public class LookStrategy {
    private RequestQueue processingQueue; // 处理中队列
    private ArrayList<Request> passengerQueue; // 乘客队列

    public LookStrategy(RequestQueue processingQueue, ArrayList<Request> passengerQueue) {
        this.processingQueue = processingQueue;
        this.passengerQueue = passengerQueue;
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
        for (Request request : passengerQueue) {
            if (request.getToFloor() == curFloor) {
                return true;
            }
        }
        return false;
    }

    public boolean canOpenForIn(int curFloor, boolean moveDirection) {
        if (passengerQueue.size() == 6) { // 人满了
            return false;
        } else {
            synchronized (passengerQueue) {
                for (Request request : processingQueue.getRequestQueue()) {
                    if (request.getFromFloor() == curFloor &&
                            request.getMoveDirection() == moveDirection) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public boolean hasReqInOriginDirection(int curFloor, boolean moveDirection) {
        synchronized (processingQueue) {
            for (Request request : processingQueue.getRequestQueue()) {
                if (request.getFromFloor() > curFloor && moveDirection) {
                    return true;
                } else if (request.getFromFloor() < curFloor && !moveDirection) {
                    return true;
                }
            }
            return false;
        }
    }

}

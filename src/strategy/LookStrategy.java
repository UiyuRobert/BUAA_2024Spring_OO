package strategy;

import controller.Request;
import controller.RequestQueue;

public class LookStrategy {
    private RequestQueue processingQueue; // 处理中队列
    private RequestQueue passengerQueue; // 乘客队列

    public LookStrategy(RequestQueue processingQueue, RequestQueue passengerQueue) {
        this.processingQueue = processingQueue;
        this.passengerQueue = passengerQueue;
    }

    public Advice getAdvice(int curFloor, boolean moveDirection) {
        if (canOpenForOut(curFloor)) {
            return Advice.OPENFOROUT;
        } else if (canOpenForIn(curFloor, moveDirection)) {
            return Advice.OPENFORIN;
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
        for (Request request : passengerQueue.getRequestQueue()) {
            if (request.getToFloor() == curFloor) {
                return true;
            }
        }
        return false;
    }

    public boolean canOpenForIn(int curFloor, boolean moveDirection) {
        if (passengerQueue.getRequestQueue().size() == 6) { // 人满了
            return false;
        } else {
            for (Request request : processingQueue.getRequestQueue()) {
                if (request.getFromFloor() == curFloor &&
                    request.getMoveDirection() == moveDirection) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean hasReqInOriginDirection(int curFloor, boolean moveDirection) {
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

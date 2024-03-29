package servicer;

import controller.Request;
import controller.RequestQueue;
import strategy.Advice;
import strategy.LookStrategy;
import com.oocourse.elevator1.TimableOutput;

public class Elevator extends Thread {
    private LookStrategy strategy; // 采取的策略
    private boolean moveDirection; // 移动方向，0 -> down ; 1 -> up
    private RequestQueue passengers; // 乘客
    private RequestQueue requests; // 待处理
    private int elevatorId; // 当前电梯ID
    private int curFloor; // 当前所在楼层

    public Elevator(RequestQueue requests, RequestQueue passengers, int elevatorId) {
        this.elevatorId = elevatorId;
        this.passengers = passengers;
        this.requests = requests;
        this.strategy = new LookStrategy(this.requests, this.passengers);
        this.moveDirection = true;
        this.curFloor = 1;
    }

    @Override
    public void run() {
        while (true) {
            Advice advice = strategy.getAdvice(curFloor, moveDirection);
            if (advice == Advice.OVER) {
                break;
            } else if (advice == Advice.MOVE) {
                move();
            } else if (advice == Advice.UTURN) {
                moveDirection = !moveDirection;
            } else if (advice == Advice.WAIT) {
                requests.waitRequest();
            } else if (advice == Advice.OPENFOROUT || advice == Advice.OPENFORIN) {
                openAndClose();
            }
        }
    }

    public void move() {
        try {
            this.sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (moveDirection) {
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
        Request request = passengers.getOneRequestByToFloorAndRemove(curFloor);
        while (request != null) {
            TimableOutput.println("OUT-" +
                    request.getPersonId() + "-" + curFloor + "-" + elevatorId);
            request = passengers.getOneRequestByToFloorAndRemove(curFloor);
        }
        request = requests.getOneRequestByFromFloorAndRemove(curFloor, moveDirection);
        while (request != null && passengers.getRequestQueue().size() <= 6) {
            TimableOutput.println("IN-" + request.getPersonId()
                    + "-" + curFloor + "-" + elevatorId);
            passengers.addRequest(request);
            request = requests.getOneRequestByFromFloorAndRemove(curFloor, moveDirection);
        }
        TimableOutput.println("CLOSE-" + curFloor + "-" + elevatorId);
    }

    @Override
    public String toString() {
        return "电梯里还有" + passengers.getRequestQueue().size() + "名乘客, " +
                "待处理队伍是否为空 : " + requests.isEnd();

    }
}

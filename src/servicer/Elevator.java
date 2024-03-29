package servicer;

import controller.Request;
import controller.RequestQueue;
import strategy.Advice;
import strategy.LookStrategy;
import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;
import java.util.Iterator;

public class Elevator extends Thread {
    private LookStrategy strategy; // 采取的策略
    private boolean moveDirection; // 移动方向，0 -> down ; 1 -> up
    private ArrayList<Request> passengers; // 乘客
    private RequestQueue requests; // 待处理
    private int elevatorId; // 当前电梯ID
    private int curFloor; // 当前所在楼层

    public Elevator(RequestQueue requests, int elevatorId) {
        this.elevatorId = elevatorId;
        this.passengers = new ArrayList<>();
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
                return;
            } else if (advice == Advice.MOVE) {
                move();
            } else if (advice == Advice.UTURN) {
                moveDirection = !moveDirection;
            } else if (advice == Advice.WAIT) {
                requests.waitRequest();
            } else if (advice == Advice.OPEN) {
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
        Request request = getOneRequestByToFloorAndRemove(curFloor);
        while (request != null) {
            TimableOutput.println("OUT-" +
                    request.getPersonId() + "-" + curFloor + "-" + elevatorId);
            request = getOneRequestByToFloorAndRemove(curFloor);
        }

        request = requests.getOneRequestByFromFloorAndRemove(curFloor, moveDirection);
        while (request != null && passengers.size() < 6) {
            TimableOutput.println("IN-" + request.getPersonId()
                    + "-" + curFloor + "-" + elevatorId);
            passengers.add(request);
            request = requests.getOneRequestByFromFloorAndRemove(curFloor, moveDirection);
        }
        TimableOutput.println("CLOSE-" + curFloor + "-" + elevatorId);
    }

    public Request getOneRequestByToFloorAndRemove(int curFloor) {
        Iterator<Request> iterator = passengers.iterator();
        while (iterator.hasNext()) {
            Request request = iterator.next();
            if (request.getToFloor() == curFloor) {
                iterator.remove();
                return request;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "电梯里还有" + passengers.size() + "名乘客, " +
                "待处理队伍是否为空 : " + requests.isEnd();

    }
}

package servicer;

import com.oocourse.elevator2.ResetRequest;

public class ElevatorStatus {
    private long moveOneFloorTime; // 移动一层需要的时间 ms
    private boolean moveDirection; // 移动方向
    private int fullLoadLimit; // 满载人数
    private int currentLoadCount; // 当前乘客数
    private boolean reset;
    private ResetRequest resetInfo;

    public ElevatorStatus(int moveOneFloorTime, boolean moveDirection,
                          int fullLoadLimit, int currentLoadCount) {
        this.moveOneFloorTime = moveOneFloorTime;
        this.moveDirection = moveDirection;
        this.fullLoadLimit = fullLoadLimit;
        this.currentLoadCount = currentLoadCount;
        this.resetInfo = null;
        reset = false;
    }

    public long getMoveOneFloorTime() {
        return moveOneFloorTime;
    }

    public boolean getMoveDirection() {
        return moveDirection;
    }

    public void reverseMoveDirection() {
        moveDirection = !moveDirection;
    }

    public int getFullLoadLimit() {
        return fullLoadLimit;
    }

    public int getCurrentLoadCount() {
        return currentLoadCount;
    }

    public void addOnePerson() {
        currentLoadCount++;
    }

    public void finishOneRequest() {
        currentLoadCount--;
    }

    public synchronized boolean isReset() {
        notifyAll();
        return reset;
    }

    public void setResetInfo(ResetRequest resetInfo) {
        this.resetInfo = resetInfo;
    }

    public synchronized void resetStatus() {
        this.fullLoadLimit = resetInfo.getCapacity();
        this.moveOneFloorTime = (long) (resetInfo.getSpeed() * 1000);
        reset = false; // 重置完成
        notifyAll();
    }

    public synchronized void setReset(boolean reset) {
        this.reset = reset;
    }

    @Override
    public String toString() {
        return " speed : " + moveOneFloorTime + "; maxLoad : " + fullLoadLimit
                + "; nowLoad : " + currentLoadCount + "; RESET : " + reset;
    }
}

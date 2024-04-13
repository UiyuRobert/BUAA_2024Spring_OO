package servicer;

import com.oocourse.elevator3.DoubleCarResetRequest;
import com.oocourse.elevator3.NormalResetRequest;
import com.oocourse.elevator3.ResetRequest;

public class ElevatorStatus {
    private static final int NORMALRESET = 1;
    private static final int DCRESET = 2;
    private static final int NOTRESET = 0;
    private long moveOneFloorTime; // 移动一层需要的时间 ms
    private boolean moveDirection; // 移动方向
    private int fullLoadLimit; // 满载人数
    private int currentLoadCount; // 当前乘客数
    private int reset;
    private boolean over;
    private ResetRequest resetInfo;
    // DoubleCarElevator
    private int transferFloor;

    public ElevatorStatus(int moveOneFloorTime, boolean moveDirection,
                          int fullLoadLimit, int currentLoadCount) {
        this.moveOneFloorTime = moveOneFloorTime;
        this.moveDirection = moveDirection;
        this.fullLoadLimit = fullLoadLimit;
        this.currentLoadCount = currentLoadCount;
        this.resetInfo = null;
        transferFloor = 0;
        over = false;
        reset = ElevatorStatus.NOTRESET;
    }

    public long getMoveOneFloorTime() {
        return moveOneFloorTime;
    }

    public void setMoveDirection(boolean moveDirection) {
        this.moveDirection = moveDirection;
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

    public void addOnePerson() {
        currentLoadCount++;
    }

    public void finishOneRequest() {
        currentLoadCount--;
    }

    public void setTransferFloor(int transferFloor) {
        this.transferFloor = transferFloor;
    }

    public synchronized int getTransferFloor() {
        notifyAll();
        return transferFloor;
    }

    public synchronized int getReset() {
        notifyAll();
        return reset;
    }

    public synchronized void wake() {
        notifyAll();
    }

    public void setResetInfo(ResetRequest resetInfo) {
        this.resetInfo = resetInfo;
    }

    public synchronized void resetStatus() {
        if (resetInfo instanceof NormalResetRequest) {
            this.fullLoadLimit = ((NormalResetRequest) resetInfo).getCapacity();
            this.moveOneFloorTime = (long) (((NormalResetRequest) resetInfo).getSpeed() * 1000);
        } else {
            this.fullLoadLimit = ((DoubleCarResetRequest) resetInfo).getCapacity();
            this.moveOneFloorTime = (long) (((DoubleCarResetRequest) resetInfo).getSpeed() * 1000);
            this.transferFloor = ((DoubleCarResetRequest) resetInfo).getTransferFloor();
        }

        reset = ElevatorStatus.NOTRESET; // 重置完成
        notifyAll();
    }

    public synchronized void setReset(int reset) {
        this.reset = reset;
        notifyAll();
    }

    public synchronized void setOver() {
        over = true;
        notifyAll();
    }

    @Override
    public String toString() {
        return " speed : " + moveOneFloorTime + "; maxLoad : " + fullLoadLimit
                + "; nowLoad : " + currentLoadCount + "; RESET : " + reset;
    }
}

package servicer;

public class Flag {
    enum State { OCCUPIED, UNOCCUPIED }

    private State state;

    public Flag() {
        this.state = State.UNOCCUPIED;
    }

    public synchronized void setOccupied() {
        waitRelease();
        // TimableOutput.println("enter transferFloor");
        state = State.OCCUPIED;
        notifyAll();
    }

    public synchronized void setRelease() {
        this.state = State.UNOCCUPIED;
        notifyAll();
    }

    private synchronized void waitRelease() {
        // TimableOutput.println("wait transferFloor");
        notifyAll();
        while (state == State.OCCUPIED) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

package controller;

public class RequestCount {
    private int cnt;

    public RequestCount() {
        this.cnt = 0;
    }

    public synchronized void addCnt() {
        ++cnt;
        notifyAll();
    }

    public synchronized void finish() {
        --cnt;
        notifyAll();
    }

    public synchronized void finish(int num) {
        cnt = cnt - num;
        notifyAll();
    }

    public synchronized int getCnt() {
        return cnt;
    }
}

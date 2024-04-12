package controller;

import com.oocourse.elevator3.PersonRequest;

import java.util.ArrayList;
import java.util.Iterator;

public class RequestQueue<T> {
    private boolean endTag = false; // 结束标志
    private boolean runningEnd = false;
    private final ArrayList<T> requestQueue;

    public RequestQueue() {
        this.endTag = false;
        this.requestQueue = new ArrayList<>();
    }

    public synchronized ArrayList<T> getRequestQueue() {
        notifyAll();
        return requestQueue;
    }

    public synchronized void addRequest(T request) { // 新增一个请求
        requestQueue.add(request);
        notifyAll();
    }

    public synchronized void wake() {
        notifyAll();
    }

    public synchronized T getOneRequestAndRemove() {
        if (requestQueue.isEmpty() && !endTag) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (requestQueue.isEmpty()) {
            return null;
        }
        T request = requestQueue.get(0);
        requestQueue.remove(0);
        notifyAll();
        return request;
    }

    public synchronized T getOneTotalRequestAndRemove() {
        if (requestQueue.isEmpty() && !runningEnd) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (requestQueue.isEmpty()) {
            return null;
        }
        T request = requestQueue.get(0);
        requestQueue.remove(0);
        notifyAll();
        return request;
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return requestQueue.isEmpty();
    }

    public synchronized boolean isEmptyNoNotify() {
        return requestQueue.isEmpty();
    }

    public synchronized boolean isEnd() {
        notifyAll();
        return endTag;
    }

    public synchronized void setEnd(boolean endTag) {
        this.endTag = endTag;
        notifyAll();
    }

    public void setRunningEnd(boolean runningEnd) {
        this.runningEnd = runningEnd & endTag;
    }

    public boolean isRunningEnd() {
        return runningEnd;
    }

    public synchronized T getOnePersonAndRemoveNoWait() {
        if (requestQueue.isEmpty()) {
            return null;
        }
        T request = requestQueue.get(0);
        requestQueue.remove(0);
        notifyAll();
        return request;
    }

    public synchronized void waitRequest() {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized PersonRequest getOneRequestByFromFloorAndRemove(int curFloor,
                                                                        boolean moveDirection) {
        if (requestQueue.isEmpty()) {
            return null;
        }
        Iterator<T> iterator = requestQueue.iterator();
        while (iterator.hasNext()) {
            PersonRequest personRequest = (PersonRequest) iterator.next();
            if (personRequest.getFromFloor() == curFloor &&
                    getMoveDirection(personRequest) == moveDirection) {
                iterator.remove();
                notifyAll();
                return personRequest;
            }
        }
        return null;
    }

    public boolean getMoveDirection(PersonRequest personRequest) {
        return (personRequest.getFromFloor() < personRequest.getToFloor());
    }
}

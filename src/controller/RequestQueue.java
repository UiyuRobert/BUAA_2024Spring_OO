package controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.LockSupport;

public class RequestQueue { // 注意线程安全
    private boolean endTag = false; // 结束标志
    private ArrayList<Request> requestQueue;

    public RequestQueue() {
        endTag = false;
        this.requestQueue = new ArrayList<>();
    }

    public synchronized ArrayList<Request> getRequestQueue() {
        return requestQueue;
    }

    public synchronized Request getOneRequestAndRemove() {
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
        Request request = requestQueue.get(0);
        requestQueue.remove(0);
        notifyAll();
        return request;
    }

    public synchronized Request getOneRequestByFromFloorAndRemove(int curFloor,
                                                                  boolean moveDirection) {
        if (requestQueue.isEmpty()) {
            return null;
        }
        Iterator<Request> iterator = requestQueue.iterator();
        while (iterator.hasNext()) {
            Request request = iterator.next();
            if (request.getFromFloor() == curFloor && request.getMoveDirection() == moveDirection) {
                iterator.remove();
                notifyAll();
                return request;
            }
        }
        return null;
    }

    public synchronized void addRequest(Request request) { // 新增一个请求
        requestQueue.add(request);
        notifyAll();
    }

    public synchronized void addRequestAndNotify(Request request, Thread thread) {
        requestQueue.add(request);
        LockSupport.unpark(thread);
    }

    public synchronized void setEnd(boolean endTag) {
        this.endTag = endTag;
        notifyAll();
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return requestQueue.isEmpty();
    }

    public synchronized boolean isEnd() {
        notifyAll();
        return endTag;
    }

    public synchronized void waitRequest() {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

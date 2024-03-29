package controller;

import java.util.ArrayList;
import java.util.Iterator;

public class RequestQueue { // 注意线程安全
    private boolean endTag; // 结束标志
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
                e.printStackTrace();
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
        Iterator<Request> iterator = requestQueue.iterator();
        while (iterator.hasNext()) {
            Request request = iterator.next();
            if (request.getFromFloor() == curFloor && request.getMoveDirection() == moveDirection) {
                iterator.remove();
                return request;
            }
        }
        return null;
    }

    public synchronized Request getOneRequestByToFloorAndRemove(int curFloor) {
        Iterator<Request> iterator = requestQueue.iterator();
        while (iterator.hasNext()) {
            Request request = iterator.next();
            if (request.getToFloor() == curFloor) {
                iterator.remove();
                return request;
            }
        }
        return null;
    }

    public synchronized void addRequest(Request request) { // 新增一个请求
        requestQueue.add(request);
        notifyAll();
    }

    public synchronized void setEnd(boolean endTag) {
        this.endTag = endTag;
    }

    public synchronized boolean isEmpty() {
        return requestQueue.isEmpty();
    }

    public synchronized boolean isEnd() {
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

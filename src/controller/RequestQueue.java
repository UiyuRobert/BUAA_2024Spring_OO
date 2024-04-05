package controller;

import java.util.ArrayList;

public class RequestQueue<T> {
    private boolean endTag = false; // 结束标志
    private ArrayList<T> requestQueue;

    public RequestQueue() {
        this.endTag = false;
        this.requestQueue = new ArrayList<>();
    }

    public boolean getEndTag() {
        return endTag;
    }

    public synchronized ArrayList<T> getRequestQueue() {
        return requestQueue;
    }

    public int getSize() {
        return requestQueue.size();
    }

    public synchronized void addRequest(T request) { // 新增一个请求
        requestQueue.add(request);
        notifyAll();
    }

    public synchronized void addRequestButNotNotify(T request) {
        requestQueue.add(request);
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

    public synchronized boolean isEmpty() {
        notifyAll();
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
}

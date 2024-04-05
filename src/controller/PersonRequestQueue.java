package controller;

import com.oocourse.elevator2.PersonRequest;
import java.util.ArrayList;
import java.util.Iterator;

public class PersonRequestQueue extends RequestQueue<PersonRequest> { // 注意线程安全

    public PersonRequestQueue() {
    }

    public synchronized ArrayList<PersonRequest> getRequestQueue() {
        return super.getRequestQueue();
    }

    public synchronized PersonRequest getOneRequestByFromFloorAndRemove(int curFloor,
                                                                          boolean moveDirection) {
        if (super.getRequestQueue().isEmpty()) {
            return null;
        }
        Iterator<PersonRequest> iterator = super.getRequestQueue().iterator();
        while (iterator.hasNext()) {
            PersonRequest personRequest = iterator.next();
            if (personRequest.getFromFloor() == curFloor &&
                    getMoveDirection(personRequest) == moveDirection) {
                iterator.remove();
                notifyAll();
                return personRequest;
            }
        }
        return null;
    }

    public synchronized void waitRequest() {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void wake() {
        notifyAll();
    }

    public boolean getMoveDirection(PersonRequest personRequest) {
        return (personRequest.getFromFloor() < personRequest.getToFloor());
    }

    public synchronized PersonRequest getOnePersonAndRemoveNoWait() {
        if (super.getRequestQueue().isEmpty()) {
            return null;
        }
        PersonRequest request = super.getRequestQueue().get(0);
        super.getRequestQueue().remove(0);
        return request;
    }
}

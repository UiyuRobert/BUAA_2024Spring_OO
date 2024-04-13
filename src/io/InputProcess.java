package io;

import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.ElevatorInput;
import controller.RequestCount;
import controller.RequestQueue;

import java.io.IOException;

public class InputProcess extends Thread {
    private final RequestQueue<Request> requestQueue;
    private RequestCount count;

    public InputProcess(RequestQueue<Request> requestQueue, RequestCount count) {
        this.requestQueue = requestQueue;
        this.count = count;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                try {
                    elevatorInput.close();
                    requestQueue.setEnd(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            } else {
                if (request instanceof PersonRequest) {
                    synchronized (count) {
                        count.addCnt();
                    }
                }
                requestQueue.addRequest(request);
            }
        }
    }

}

package io;

import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ElevatorInput;
import controller.RequestQueue;

import java.io.IOException;

public class InputProcess extends Thread {
    private final RequestQueue<Request> requestQueue;

    public InputProcess(RequestQueue<Request> requestQueue) {
        this.requestQueue = requestQueue;
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
                requestQueue.addRequest(request);
            }
        }
    }
}

package io;

import controller.Request;
import controller.RequestQueue;
import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;

import java.io.IOException;

public class InputProcess extends Thread {
    private RequestQueue waitQueue;
    private Thread scheduler;

    public InputProcess(RequestQueue waitQueue, Thread scheduler) {
        this.waitQueue = waitQueue;
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            PersonRequest personRequest = elevatorInput.nextPersonRequest();
            if (personRequest == null) {
                try {
                    elevatorInput.close();
                    waitQueue.setEnd(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            } else {
                int requestId = personRequest.getPersonId();
                int fromFloor = personRequest.getFromFloor();
                int toFloor = personRequest.getToFloor();
                int elevatorId = personRequest.getElevatorId();
                Request request = new Request(requestId,fromFloor,toFloor,elevatorId);
                waitQueue.addRequest(request);
            }
        }
    }
}

package controller;

import java.util.ArrayList;

public class Scheduler extends Thread {
    private final RequestQueue waitQueue;
    private final ArrayList<RequestQueue> processingQueues;
    // private final ArrayList<Thread> threads;

    public Scheduler(RequestQueue waitQueue, ArrayList<RequestQueue> processingQueues) {
        this.waitQueue = waitQueue;
        this.processingQueues = processingQueues;
        // this.threads = threads;
    }

    @Override
    public void run() {
        while (true) {
            if (waitQueue.isEmpty() && waitQueue.isEnd()) {
                for (int i = 0; i < processingQueues.size(); i++) {
                    processingQueues.get(i).setEnd(true);
                }
                return;
            }
            Request request = waitQueue.getOneRequestAndRemove();
            if (request == null) {
                continue;
            }
            int elevatorId = request.getElevatorId();
            // System.out.println("\t\t\t\t\t ID 为 " +request.getPersonId()
            // + " 的人进入 ID 为 " + elevatorId + " 的电梯");
            if (elevatorId == 1) {
                processingQueues.get(0).addRequest(request);
            } else if (elevatorId == 2) {
                processingQueues.get(1).addRequest(request);
            } else if (elevatorId == 3) {
                processingQueues.get(2).addRequest(request);
            } else if (elevatorId == 4) {
                processingQueues.get(3).addRequest(request);
            } else if (elevatorId == 5) {
                processingQueues.get(4).addRequest(request);
            } else {
                processingQueues.get(5).addRequest(request);
            }
        }
    }
}

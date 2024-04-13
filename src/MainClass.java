import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import controller.RequestCount;
import controller.RequestQueue;
import controller.Scheduler;
import io.InputProcess;
import servicer.Elevator;
import com.oocourse.elevator3.TimableOutput;
import servicer.ElevatorStatus;
import servicer.Flag;

import java.util.ArrayList;
import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp(); // 初始化

        RequestQueue<Request> totalQueue = new RequestQueue<>(); // 总请求表

        ArrayList<RequestQueue<PersonRequest>> queues = new ArrayList<>(); // 六个电梯各自的候乘表
        // 退出电梯换乘 RESET
        ArrayList<RequestQueue<PersonRequest>> exitHalfwayQueues = new ArrayList<>();
        ArrayList<ElevatorStatus> runningStates = new ArrayList<>(); // 六部电梯当前状态

        ArrayList<ArrayList<PersonRequest>> passengerQueues = new ArrayList<>();

        RequestCount count = new RequestCount();

        HashMap<Integer, Flag> occupies = new HashMap<>();

        for (int i = 1; i < 7; i++) {
            RequestQueue<PersonRequest> processQueue = new RequestQueue<>(); // 各个电梯的待处理队伍
            queues.add(processQueue);

            ArrayList<PersonRequest> passengers = new ArrayList<>();

            RequestQueue<PersonRequest> exitHalfwayPassengers = new RequestQueue<>();
            Flag occupied = new Flag();
            Elevator elevator = new Elevator(processQueue, i,
                    exitHalfwayPassengers, passengers, occupied, totalQueue, count);
            elevator.setName("" + i);
            elevator.setNormalStatus();

            runningStates.add(elevator.getStatus());
            occupies.put(i, occupied);
            passengerQueues.add(passengers);
            exitHalfwayQueues.add(exitHalfwayPassengers);
            elevator.start();
        }

        Scheduler scheduler = new Scheduler(totalQueue, queues, runningStates,
                exitHalfwayQueues, passengerQueues, count, occupies);
        scheduler.setName("scheduler");
        scheduler.start();

        Thread input = new InputProcess(totalQueue, count);
        input.start();

    }
}

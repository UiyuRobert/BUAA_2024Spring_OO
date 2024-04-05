import com.oocourse.elevator2.Request;
import controller.PersonRequestQueue;
import controller.RequestQueue;
import controller.Scheduler;
import io.InputProcess;
import servicer.Elevator;
import com.oocourse.elevator2.TimableOutput;
import servicer.ElevatorStatus;
import java.util.ArrayList;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp(); // 初始化

        RequestQueue<Request> totalQueue = new RequestQueue<>(); // 总请求表

        ArrayList<PersonRequestQueue> queues = new ArrayList<>(); // 六个电梯各自的候乘表
        ArrayList<PersonRequestQueue> exitHalfwayQueues = new ArrayList<>(); // 退出电梯换乘 RESET
        ArrayList<ElevatorStatus> runningStates = new ArrayList<>(); // 六部电梯当前状态

        for (int i = 1; i < 7; i++) {
            PersonRequestQueue processQueue = new PersonRequestQueue(); // 各个电梯的待处理队伍
            queues.add(processQueue);

            PersonRequestQueue exitHalfwayPassengers = new PersonRequestQueue();
            Elevator elevator = new Elevator(processQueue, i, exitHalfwayPassengers);
            elevator.setName("elevator " + i);

            runningStates.add(elevator.getStatus());
            exitHalfwayQueues.add(exitHalfwayPassengers);
            elevator.start();
        }

        Scheduler scheduler = new Scheduler(totalQueue, queues, runningStates, exitHalfwayQueues);
        scheduler.setName("scheduler");
        scheduler.start();

        Thread input = new InputProcess(totalQueue);
        input.start();

    }
}

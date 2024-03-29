import controller.RequestQueue;
import controller.Scheduler;
import io.InputProcess;
import servicer.Elevator;
import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp(); // 初始化
        RequestQueue totalQueue = new RequestQueue(); // 总候乘表
        ArrayList<RequestQueue> queues = new ArrayList<>(); // 六个电梯各自的候乘表
        ArrayList<Thread> threads = new ArrayList<>(); // 六部电梯
        for (int i = 1; i < 7; i++) {
            RequestQueue processQueue = new RequestQueue(); // 各个电梯的待处理队伍
            queues.add(processQueue);

            Thread elevator = new Elevator(processQueue, i);
            threads.add(elevator);
            elevator.start();
        }

        Scheduler scheduler = new Scheduler(totalQueue, queues, threads);
        scheduler.start();

        Thread input = new InputProcess(totalQueue, scheduler);
        input.start();
    }
}

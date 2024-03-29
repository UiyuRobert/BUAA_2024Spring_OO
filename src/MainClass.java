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
        Runnable inputProcess = new InputProcess(totalQueue); // 输入处理
        for (int i = 1; i < 7; i++) {
            RequestQueue processQueue = new RequestQueue();
            queues.add(processQueue);
            RequestQueue passengers = new RequestQueue();
            Thread elevator = new Elevator(processQueue, passengers, i);
            elevator.start();
        }
        Scheduler scheduler = new Scheduler(totalQueue, queues);
        scheduler.start();
        Thread input = new Thread(inputProcess);
        input.start();
    }
}

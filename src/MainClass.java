import controller.RequestQueue;
import controller.Scheduler;
import io.InputProcess;
import servicer.Elevator;
import com.oocourse.elevator1.TimableOutput;
import java.util.ArrayList;
import static java.lang.Thread.sleep;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp(); // 初始化
        RequestQueue totalQueue = new RequestQueue(); // 总候乘表
        ArrayList<RequestQueue> queues = new ArrayList<>(); // 六个电梯各自的候乘表
        for (int i = 1; i < 7; i++) {
            RequestQueue processQueue = new RequestQueue(); // 各个电梯的待处理队伍
            queues.add(processQueue);

            Elevator elevator = new Elevator(processQueue, i);
            elevator.start();
            try {
                sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Scheduler scheduler = new Scheduler(totalQueue, queues);
        scheduler.start();

        Thread input = new InputProcess(totalQueue, scheduler);
        input.start();
    }
}

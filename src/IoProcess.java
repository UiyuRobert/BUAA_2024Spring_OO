import expr.Adjust;
import expr.Definer;

import java.io.IOException;
import java.util.Scanner;

public class IoProcess {
    private Scanner scanner;
    private String inputInfo;

    public IoProcess(Scanner scanner) {
        this.scanner = scanner;
    }

    public void processFunction() throws IOException, ClassNotFoundException { // 处理自定义函数
        int funcNum = scanner.nextInt();
        scanner.nextLine(); // \n
        for (int i = 0; i < funcNum; ++i) {
            String funcInput = Adjust.simplifyString(scanner.nextLine());
            Definer.addFunc(funcInput, (i == 0));
        }
    }
}

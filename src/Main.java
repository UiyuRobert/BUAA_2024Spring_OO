import expr.Adjust;
import expr.Expr;
import expr.Lexer;
import expr.Parser;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Scanner scanner = new Scanner(System.in);
        IoProcess ioProcess = new IoProcess(scanner);
        ioProcess.processFunction();
        String inputInfo = scanner.nextLine();
        inputInfo = Adjust.simplifyString(inputInfo);
        Lexer lexer = new Lexer(inputInfo);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        System.out.println(expr.toString());
        String result = Adjust.simplifyString(expr.toPoly().toString());
        System.out.println(result);
    }
}
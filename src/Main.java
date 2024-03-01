import expr.Expr;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String inputInfo = scanner.nextLine();
        inputInfo = Adjust.simplifyString(inputInfo);
        Lexer lexer = new Lexer(inputInfo);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        String result = Adjust.simplifyString(expr.toPoly().toString());
        result = Adjust.simplifyPoly(result);
        System.out.println(result);
    }
}
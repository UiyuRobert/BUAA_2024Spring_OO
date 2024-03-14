package expr;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class FuncFactor implements Factor, Serializable {
    private String newFunc; // 将函数实参带入形参位置后的结果(字符串形式)
    private Expr expr; // 将newFunc解析成表达式后的结果

    public FuncFactor(String name, ArrayList<Factor> actualParas)
            throws IOException, ClassNotFoundException {
        this.newFunc = Definer.callFunc(name, actualParas);
        this.expr = setExpr();
    }

    public Expr setExpr() throws IOException, ClassNotFoundException {
        String str = Adjust.simplifyString(newFunc);
        Lexer lexer = new Lexer(str);
        Parser parser = new Parser(lexer);
        return parser.parseExpr();
    }

    @Override
    public void setExp(String exp) {

    }

    @Override
    public Poly toPoly() throws IOException, ClassNotFoundException {
        return expr.toPoly();
    }

    @Override
    public Factor derive() throws IOException, ClassNotFoundException {
        return expr.derive();
    }

    @Override
    public String toString() {
        return "(" + newFunc + ")";
    }
}

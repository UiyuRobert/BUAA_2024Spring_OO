package expr;

import java.io.IOException;
import java.math.BigInteger;

public class ExpFactor implements Factor {
    private Factor factor; // e ^ () 括号内的内容
    private String exp = "1"; // 指数函数整体的 指数

    public ExpFactor(Factor factor, String exp) {
        this.factor = factor;
        this.exp = exp;
    }

    @Override
    public void setExp(String exp) {
        this.exp = exp;
    }

    @Override
    public Poly toPoly() throws IOException, ClassNotFoundException {
        Poly poly = new Poly();
        if (new BigInteger(exp).equals(BigInteger.ZERO)) { // 如果指数部分为 0
            return poly.monoToPoly(BigInteger.ONE, BigInteger.ZERO); // 1*x^0
        } else {
            Poly newPoly = new Poly();
            // 将整体指数放入括号内
            newPoly = newPoly.monoToPoly(new BigInteger(exp),BigInteger.ZERO);
            newPoly = newPoly.mulPoly(factor.toPoly());
            return poly.monoToPoly(new Mono(newPoly));
        }
    }

    @Override
    public String toString() {
        if (factor == null || exp.equals("0")) {
            return "1";
        } else if (exp.equals("1")) {
            return "exp(" + factor.toString() + ")";
        }
        return "exp(" + factor.toString() + ")^" + exp;
    }
}

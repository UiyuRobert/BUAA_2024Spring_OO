package expr;

import java.math.BigInteger;

public class Num implements Factor {
    private String exp = "0";
    private final String num;

    public Num(String num) {
        this.num = num;
    }

    public String getNum() {
        return this.num;
    }

    @Override
    public void setExp(String exp) {
        this.exp = exp;
    }

    @Override
    public Poly toPoly() {
        Poly poly = new Poly();
        return poly.monoToPoly(new BigInteger(num), 0);
    }

    @Override
    public String toString() {
        return num;
    }
}

package expr;

import java.math.BigInteger;

public class Power implements Factor {
    private String exp;
    private String base;

    public Power(String base, String exp) {
        this.base = base;
        this.exp = exp;
    }

    @Override
    public void setExp(String exp) {
        this.exp = exp;
    }

    @Override
    public Poly toPoly() {
        Poly poly = new Poly();
        return poly.monoToPoly(new BigInteger("1"), Integer.parseInt(exp));
    }

    @Override
    public String toString() {
        if (exp.equals("0")) {
            return "1";
        } else if (exp.equals("1")) {
            return base;
        }
        return base + "^" + exp;
    }
}

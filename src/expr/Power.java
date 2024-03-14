package expr;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;

public class Power implements Factor, Serializable {
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
        return poly.monoToPoly(new BigInteger("1"), new BigInteger(exp));
    }

    @Override
    public Factor derive() throws IOException, ClassNotFoundException {
        Term term = new Term(1);
        if (exp.equals("1")) {
            term.addFactor(new Num("1"));
            term.merge();
            return term;
        } else if (exp.equals("0")) {
            term.addFactor(new Num("0"));
            term.merge();
            return term;
        } else {
            BigInteger expBI = new BigInteger(exp).add(new BigInteger("-1")); // 指数减 1
            Power power = new Power(new String(base), new String(String.valueOf(expBI)));
            Num num = new Num(new String(exp));
            term.addFactor(num);
            term.addFactor(power);
            term.merge();
            return term;
        }
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

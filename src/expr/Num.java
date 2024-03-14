package expr;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;

public class Num implements Factor, Serializable {
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
        return poly.monoToPoly(new BigInteger(num), BigInteger.ZERO);
    }

    @Override
    public Factor derive() throws IOException, ClassNotFoundException {
        Term term = new Term(1);
        term.addFactor(new Num("0"));
        term.merge();
        return term;
    }

    @Override
    public String toString() {
        return num;
    }
}

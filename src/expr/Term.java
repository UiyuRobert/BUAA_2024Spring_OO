package expr;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;

public class Term implements Factor {
    private String exp = "0";
    private BigInteger coe = new BigInteger("1"); // Term 的符号, -1 -> -   |   +1 -> +
    private final HashSet<Factor> factors;

    public Term(int sign) {
        if (sign == -1) {
            this.coe = this.coe.negate();
        }
        factors = new HashSet<>();
    }

    public void addFactor(Factor factor) {
        this.factors.add(factor);
    }

    public void negate() {
        coe = coe.negate();
    }

    public void merge() { // 合并有符号常数并更新 Term 的符号
        Iterator<Factor> iterator = factors.iterator();
        while (iterator.hasNext()) {
            Factor factor = iterator.next();
            if (factor instanceof Num) {
                BigInteger num = new BigInteger(((Num) factor).getNum());
                this.coe = this.coe.multiply(num);
                iterator.remove();
            }
        }
    }

    @Override
    public Poly toPoly() throws IOException, ClassNotFoundException {
        Poly poly = new Poly();
        poly = poly.monoToPoly(coe, BigInteger.ZERO);
        for (Factor factor : factors) {
            poly = poly.mulPoly(factor.toPoly());
        }
        return poly;
    }

    @Override
    public void setExp(String exp) {
        this.exp = exp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!factors.isEmpty() && coe.equals(BigInteger.ONE)) {
            boolean flag = false;
            for (Factor factor : factors) {
                if (!flag) {
                    sb.append("(").append(factor.toString()).append(")");
                    flag = true;
                } else {
                    sb.append("*(").append(factor.toString()).append(")");
                }
            }
        } else if (!factors.isEmpty() && coe.equals(new BigInteger("-1"))) {
            boolean flag = false;
            for (Factor factor : factors) {
                if (!flag) {
                    sb.append("-(").append(factor.toString()).append(")");
                    flag = true;
                } else {
                    sb.append("*(").append(factor.toString()).append(")");
                }
            }
        }
        else {
            sb.append(coe);
            for (Factor factor : factors) {
                sb.append("*(").append(factor.toString()).append(")");
            }
        }
        return sb.toString();
    }
}

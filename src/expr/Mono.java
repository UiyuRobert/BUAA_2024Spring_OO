package expr;

import java.math.BigInteger;

public class Mono { // 单项式类
    private BigInteger coe; // 底数
    private int exp; // 指数

    public Mono(BigInteger coe, int exp) {
        this.coe = coe;
        this.exp = exp;
    }

    public BigInteger getCoe() {
        return coe;
    }

    public int getExp() {
        return exp;
    }

    @Override
    public String toString() {
        return coe + "*x^" + exp;
    }
}

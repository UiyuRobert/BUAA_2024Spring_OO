package expr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;

public class ExpFactor implements Factor, Serializable {
    private Factor factor; // e ^ () 括号内的内容
    private String exp = "1"; // 指数函数整体的 指数

    public ExpFactor(Factor factor, String exp) {
        this.factor = factor;
        this.exp = exp;
    }

    public ExpFactor deepCop(ExpFactor des) throws IOException, ClassNotFoundException { // 深克隆
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(des);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        ExpFactor expFactor = (ExpFactor) ois.readObject();//从流中把数据读出来
        bos.close();
        bis.close();
        return expFactor;
    }

    @Override
    public Factor derive() throws IOException, ClassNotFoundException {
        if (exp.equals("0")) {
            return new Term(BigInteger.ZERO);
        } else {
            Term term = new Term(new BigInteger(exp));
            Factor second = deepCop(this); // exp()
            Factor first = factor.derive(); // 括号内求导
            term.addFactor(first);
            term.addFactor(second);
            term.merge();
            return term;
        }
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
            if (factor instanceof Expr) {
                return "exp((" + factor.toString() + "))";
            }
            return "exp(" + factor.toString() + ")";
        }
        if (factor instanceof Expr) {
            return "exp((" + factor.toString() + "))";
        }
        return "exp(" + factor.toString() + ")^" + exp;
    }
}

package expr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Objects;

public class Mono implements Serializable { // 单项式类
    private BigInteger coe; // 底数
    private BigInteger exp; // 指数
    private Poly expExp; // exp 括号内的因子
    // private HashMap<Character, Integer> varAndExp; // 底数和指数对

    public Mono(BigInteger coe, BigInteger exp) { // 只有 ax^n 部分
        expExp = new Poly();
        // varAndExp = new HashMap<>();
        this.coe = coe;
        this.exp = exp;
        // varAndExp.put(var, exp);
    }

    public Mono(Poly poly) { // 只有 exp ()^n 部分
        this.expExp = poly;
        this.exp = BigInteger.ZERO;
        this.coe = BigInteger.ONE;
    }

    public Mono(BigInteger coe, BigInteger exp, Poly poly) {
        this.expExp = poly;
        this.exp = exp;
        this.coe = coe;
    }

    public BigInteger getCoe() {
        return coe;
    }

    public BigInteger getExp() {
        return exp;
    }

    public Poly getExpExp() {
        return this.expExp;
    }

    public Mono add(Mono mono) {
        return new Mono(this.coe.add(mono.getCoe()),this.exp,this.expExp);
    }

    public Mono mul(Mono mono) throws IOException, ClassNotFoundException {
        return new Mono(this.coe.multiply(mono.getCoe()),
                this.exp.add(mono.getExp()), this.expExp.addPoly(mono.getExpExp()));
    }

    public boolean canAdd(Mono mono) { // 两个 Mono 能否相加
        if (Objects.equals(this.exp, mono.getExp())) { // x^n 部分指数相同
            if (expExp.isPolyNull() && mono.getExpExp().isPolyNull()) { // exp() 为空
                return true;
            }
            return expExp.equals(mono.getExpExp()); // 判断 exp() 的内容是否<相等>
        }
        return false;
    }

    public Mono deepCop(Mono des) throws IOException, ClassNotFoundException { // 深克隆
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(des);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        Mono mono = (Mono) ois.readObject();//从流中把数据读出来
        bos.close();
        bis.close();
        return mono;
    }

    @Override
    public String toString() {
        if (Objects.equals(coe, BigInteger.ZERO)) {
            return "0";
        } else if (expExp.isPolyNull()) { // 如果没有exp()部分
            return simplifyFirstHalf();
        } else {
            ArrayList<Mono> tmpMonoList = expExp.getMonoList();
            if (tmpMonoList.size() == 1) { // exp(一项)
                String str = tmpMonoList.get(0).toString();
                if (str.equals("exp(0)") || str.equals("exp(+0)") || str.equals("exp(-0)")) {
                    return simplifyFirstHalf();
                }
            }
            String firstHalf = simplifyFirstHalf();
            if (firstHalf.equals("1")) {
                return "exp((" + expExp.toString() + "))";
            } else if (firstHalf.equals("-1")) {
                return "-exp((" + expExp.toString() + "))";
            }
            return firstHalf + "*" + "exp((" + expExp.toString() + "))";
        }
    }

    public String simplifyFirstHalf() {
        if (exp.equals(BigInteger.ZERO)) { // 如果 a*x^n 的 n = 0
            return coe.toString();
        } else if (exp.equals(BigInteger.ONE)) { // 指数为 1
            if (coe.equals(BigInteger.ONE)) { // 系数为 1
                return "x";
            } else if (Objects.equals(coe, BigInteger.valueOf(-1))) {
                return "-x";
            }
            return coe + "*x";
        } else if (Objects.equals(coe, BigInteger.valueOf(1))) {
            return "x^" + exp; // 系数为1，只输出指数
        } else if (Objects.equals(coe, BigInteger.valueOf(-1))) {
            return "-x^" + exp; // 系数为 -1，只输出指数
        }
        return coe + "*x^" + exp;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Mono) {
            Mono mono = (Mono) object;
            if (Objects.equals(this.coe, mono.getCoe()) &&
                    Objects.equals(this.exp, mono.getExp())) { // x^n 指数相同
                if (expExp.isPolyNull()) { // exp() 为空
                    return true;
                }
                return expExp.equals(mono.getExpExp()); // exp() 的内容是否相等
            }
            return false;
        }
        return false;
    }
}

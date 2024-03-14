package expr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;

public class Expr implements Factor, Serializable {
    private String exp = "1";
    private final ArrayList<Term> terms;

    public Expr() {
        this.terms = new ArrayList<>();
    }

    public void addTerm(Term term) {
        this.terms.add(term);
    }

    public static Expr mergeExpr(Expr expr1, Expr expr2) { // 合并两个表达式
        if (expr1 == null) {
            return expr2;
        }
        if (expr2 == null) {
            return expr1;
        }
        Expr expr = new Expr();
        expr1.terms.forEach(expr::addTerm);
        expr2.terms.forEach(expr::addTerm);
        return expr;
    }

    public boolean isEmpty() {
        return terms.isEmpty();
    }

    @Override
    public void setExp(String exp) {
        this.exp = exp;
    }

    @Override
    public Poly toPoly() throws IOException, ClassNotFoundException {
        Poly poly = new Poly();
        for (Term term : terms) {
            poly = poly.addPoly(term.toPoly());
        }
        if (this.exp.equals("1")) {
            return poly;
        } else {
            return poly.powPoly(Integer.parseInt(exp));
        }
    }

    @Override
    public Factor derive() throws IOException, ClassNotFoundException {
        if (terms.isEmpty()) {
            return new Term(BigInteger.ZERO);
        }
        if (exp.equals("1")) {
            Expr expr = new Expr();
            for (int i = 0; i < terms.size(); i++) {
                Expr termDerived = (Expr) terms.get(i).derive();
                expr = Expr.mergeExpr(expr,termDerived);
            }
            Term term = new Term(1);
            term.addFactor(expr);
            term.merge();
            return term;
        } else if (exp.equals("0")) {
            return new Term(BigInteger.ZERO);
        } else {
            Term term = new Term(new BigInteger(exp));
            Expr factor = deepCop(this);
            BigInteger expDerived = new BigInteger(exp).add(new BigInteger("-1"));
            factor.setExp(String.valueOf(expDerived));
            Expr expr = new Expr();
            for (int i = 0; i < terms.size(); i++) {
                Expr termDerived = (Expr) terms.get(i).derive();
                expr = Expr.mergeExpr(expr,termDerived);
            }
            term.addFactor(expr);
            term.addFactor(factor);
            term.merge();
            return term;
        }
    }

    public Expr deepCop(Expr des) throws IOException, ClassNotFoundException { // 深克隆
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(des);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        Expr expr = (Expr) ois.readObject();//从流中把数据读出来
        bos.close();
        bis.close();
        return expr;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean flag = false;
        for (Term term : terms) {
            if (!flag) {
                sb.append(term.toString());
                flag = true;
            } else {
                sb.append("+").append(term.toString());
            }
        }
        if (exp.equals("1")) {
            return sb.toString().replaceAll("(\\+\\+)|(--)", "+").replaceAll("(-\\+)|(\\+-)", "-");
        } else if (exp.equals("0")) {
            return "1";
        } else {
            sb.insert(0,"(");
            sb.append(")^").append(exp);
            return sb.toString().replaceAll("(\\+\\+)|(--)", "+").replaceAll("(-\\+)|(\\+-)", "-");
        }
    }
}

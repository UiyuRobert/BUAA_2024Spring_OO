package expr;

import java.io.IOException;
import java.util.HashSet;

public class Expr implements Factor {
    private String exp = "1";
    private final HashSet<Term> terms;

    public Expr() {
        this.terms = new HashSet<>();
    }

    public void addTerm(Term term) {
        this.terms.add(term);
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

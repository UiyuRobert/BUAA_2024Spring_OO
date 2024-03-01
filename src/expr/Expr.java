package expr;

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
    public Poly toPoly() {
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
}

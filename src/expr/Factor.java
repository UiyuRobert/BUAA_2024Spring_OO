package expr;

public interface Factor {
    void setExp(String exp);

    Poly toPoly();
}

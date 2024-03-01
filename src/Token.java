public class Token {

    public Token(Type type, String content) {
        this.type = type;
        this.content = content;
    }

    public enum Type { // 按运算优先级从低到高
        NULL, // 初始状态
        ADD, SUB, // expr.Expr -> Term
        MUL, // Term -> expr.Factor
        NUM, VAR, EXP, LP, RP; // expr.Factor
    }

    private final Type type; // 该 Token 的类型
    private final String content; // 该 Token 的内容

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

}

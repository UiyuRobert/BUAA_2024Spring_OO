package expr;

import java.math.BigInteger;

public class Lexer {
    private final String input;
    private int pos = 0;
    private Token curToken = new Token(Token.Type.NULL,"");

    public Lexer(String input) {
        this.input = input;
        this.nextToken();
    }

    public String getNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            ++pos;
        }
        return String.valueOf(new BigInteger(sb.toString()));
    }

    public void nextToken() {
        if (pos == input.length()) {
            return;
        }
        char c = input.charAt(pos);
        if (Character.isDigit(c)) {
            curToken = new Token(Token.Type.NUM, getNumber());
        } else if (c == 'e') {
            pos += 3; // 跳过 exp
            curToken = new Token(Token.Type.EXPFUN, "exp");
        } else if ("fgh".indexOf(c) != -1) { // 识别到了函数
            ++pos; // 跳过函数名
            curToken = new Token(Token.Type.FUN, String.valueOf(c));
        } else {
            ++pos;
            switch (c) {
                case '+' :
                    curToken = new Token(Token.Type.ADD,"+");
                    break;
                case '-' :
                    curToken = new Token(Token.Type.SUB,"-");
                    break;
                case '*' :
                    curToken = new Token(Token.Type.MUL,"*");
                    break;
                case '(' :
                    curToken = new Token(Token.Type.LP,"(");
                    break;
                case ')' :
                    curToken = new Token(Token.Type.RP,")");
                    break;
                case '^' :
                    curToken = new Token(Token.Type.EXP,"^");
                    break;
                default:
                    curToken = new Token(Token.Type.VAR,String.valueOf(c));
            }
        }
    }

    public Token peek() {
        return curToken;
    }

    public boolean hasNext() {
        return pos < input.length();
    }
}

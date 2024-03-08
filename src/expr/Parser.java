package expr;

import java.util.ArrayList;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        Expr expr = new Expr();
        int sign = 1;
        if (lexer.hasNext() && lexer.peek().getType() == Token.Type.SUB) { // 第一项前有 -
            sign = -1;
            lexer.nextToken();
        } else if (lexer.hasNext() && lexer.peek().getType() == Token.Type.ADD) { // 第一项前有 +
            lexer.nextToken();
        }
        expr.addTerm(parseTerm(sign));
        while (lexer.hasNext() && (lexer.peek().getType() == Token.Type.ADD ||
                lexer.peek().getType() == Token.Type.SUB)) {
            if (lexer.peek().getType() == Token.Type.SUB) {
                lexer.nextToken(); // 跳过 -
                expr.addTerm(parseTerm(-1));
            } else {
                lexer.nextToken(); // 跳过 +
                expr.addTerm(parseTerm(1));
            }
        }
        return expr;
    }

    public Term parseTerm(int sign) {
        Term term = new Term(sign);
        if (lexer.hasNext() && lexer.peek().getType() == Token.Type.SUB) { // 第一因子前有 -
            lexer.nextToken();
            term.negate(); // 取反
        } else if (lexer.hasNext() && lexer.peek().getType() == Token.Type.ADD) { // 第一因子前有 +
            lexer.nextToken();
        }
        term.addFactor(parseFactor());
        while (lexer.hasNext() && lexer.peek().getType() == Token.Type.MUL) {
            lexer.nextToken(); // 跳过 *
            term.addFactor(parseFactor());
        }
        term.merge();
        return term;
    }

    public Factor parseFactor() {
        if (lexer.peek().getType() == Token.Type.FUN) { // 自定义函数因子 解析
            String funcName = lexer.peek().getContent();
            lexer.nextToken(); // 跳过函数名
            lexer.nextToken(); // 跳过 (
            Factor funcFactor = parseFuncFactor(funcName);
            lexer.nextToken(); // 跳过 )
            return funcFactor;
        } else if (lexer.peek().getType() == Token.Type.EXPFUN) { // exp 函数因子
            lexer.nextToken(); // 跳过 exp
            lexer.nextToken(); // 跳过左括号
            Factor inside = parseFactor(); // exp() 内部解析
            lexer.nextToken(); // 跳过右括号
            return new ExpFactor(inside, parseIndex());
        } else if (lexer.peek().getType() == Token.Type.LP) { // 表达式因子
            lexer.nextToken(); // 跳过左括号
            Factor expr = parseExpr();
            lexer.nextToken(); // 跳过右括号
            expr.setExp(parseIndex()); // 如果有 ^
            return expr;
        } else if (lexer.peek().getType() == Token.Type.VAR) { // 幂函数因子
            String base = lexer.peek().getContent();
            lexer.nextToken();
            return new Power(base,parseIndex());
        } else { // 常数因子
            if (lexer.peek().getType() == Token.Type.ADD || // 有符号
                    lexer.peek().getType() == Token.Type.SUB) {
                String numStr = lexer.peek().getContent(); // 先取符号
                lexer.nextToken(); // 取数字
                numStr = numStr + lexer.peek().getContent();
                lexer.nextToken(); // 跳出该数字，进入下一项
                return new Num(numStr);
            }
            else { // 无符号
                String num = lexer.peek().getContent();
                lexer.nextToken(); // 跳过当前这个 num expr.Token
                return new Num(num);
            }
        }
    }

    public String parseIndex() { // 从 ^ 开始解析
        if (lexer.hasNext() && lexer.peek().getType() == Token.Type.EXP) {
            lexer.nextToken(); // 跳过 ^
            if (lexer.peek().getType() == Token.Type.ADD) {
                lexer.nextToken(); // 跳过可能存在的 +
            }
            // 当前位于 num
            String num = lexer.peek().getContent();
            lexer.nextToken(); // 进入下个 expr.Token，跳出 +exp
            return num;
        } else {
            return "1";
        }
    }

    public Factor parseFuncFactor(String funcName) {
        ArrayList<Factor> actualParas = new ArrayList<>(); // 实参集合
        actualParas.add(parseFactor());
        while (lexer.peek().getType() != Token.Type.RP) { // 不是 ),也就不是结尾
            lexer.nextToken(); // 跳过 ,
            actualParas.add(parseFactor());
        }
        return new FuncFactor(funcName,actualParas);
    }
}

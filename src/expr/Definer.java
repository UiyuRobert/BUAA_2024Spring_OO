package expr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Definer { // 自定义函数的解析
    private static HashMap<String, String> funcMap = new HashMap<>(); // f/g/h -> 定义式
    private static HashMap<String, ArrayList<String>> paraMap = new HashMap<>(); // f/g/h->x/y/z

    public static void addFunc(String input, boolean isFirst)
            throws IOException, ClassNotFoundException { // 新增一个自定义函数
        String[] parts = input.trim().split("=");
        Pattern pattern = Pattern.compile("\\b([fghxyz])\\b");
        Matcher matcher = pattern.matcher(parts[0]);
        boolean flag = true;
        String funcName = ""; // 函数名
        String funcBody = parts[1]; // 函数体
        ArrayList<String> paraList = new ArrayList<>();
        while (matcher.find()) {
            if (flag) {
                funcName = matcher.group();
                flag = false;
            } else {
                String para = matcher.group();
                if (para.equals("x")) {
                    paraList.add("a");
                } else if (para.equals("y")) {
                    paraList.add("b");
                } else if (para.equals("z")) {
                    paraList.add("c");
                }
            }
        }
        paraMap.put(funcName, paraList);
        if (!isFirst) {
            funcBody = parseFuncBody(funcBody);
        }
        StringBuffer result = convertPara(pattern, funcBody);
        funcMap.put(funcName, result.toString());
    }

    public static String parseFuncBody(String funcBodyBefore)
            throws IOException, ClassNotFoundException {
        String str = Adjust.simplifyString(funcBodyBefore);
        Lexer lexer = new Lexer(str);
        Parser parser = new Parser(lexer);
        return parser.parseExpr().toString();
    }

    private static StringBuffer convertPara(Pattern pattern, String funcBody) {
        // 将表达式中x/y/z 换成 a/b/c
        Matcher funcMatcher = pattern.matcher(funcBody);
        StringBuffer result = new StringBuffer();
        while (funcMatcher.find()) {
            String replacement = null;
            String para = funcMatcher.group();
            if (para.equals("x")) {
                replacement = "a";
            } else if (para.equals("y")) {
                replacement = "b";
            } else if (para.equals("z")) {
                replacement = "c";
            }
            funcMatcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        funcMatcher.appendTail(result);
        return result;
    }

    public static String callFunc(String name, ArrayList<Factor> actualParas) { // 形参替换实参
        String funcExpr = funcMap.get(name); // 函数表达式
        ArrayList<String> paraList = paraMap.get(name); // 名为 name 函数的参数列表
        // HashMap<String, Factor> paraToArguMap = new HashMap<>(); // 形参到实参的映射
        for (int i = 0; i < actualParas.size(); ++i) {
            funcExpr = replacePara(funcExpr, paraList.get(i), actualParas.get(i).toString());
        }
        return funcExpr;
    }

    public static String replacePara(String strReplaced, String para, String replacementExpr) {
        // strReplaced -> 要被替换的字符串 | para -> 要被替换的内容 | replacementExpr -> 要替换为什么
        String regex = "\\b" + para + "\\b"; // 匹配非字母的 x
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(strReplaced);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String replacement = "(" + replacementExpr + ")";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}

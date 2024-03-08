package expr;

import expr.Factor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Definer { // 自定义函数的解析
    private static HashMap<String, String> funcMap = new HashMap<>(); // f/g/h -> 定义式
    private static HashMap<String, ArrayList<String>> paraMap = new HashMap<>(); // f/g/h->x/y/z

    public static void addFunc(String input) { // 新增一个自定义函数
        String[] parts = input.trim().split("=");
        Pattern pattern = Pattern.compile("\\b([fghxyz]+)\\b");
        Matcher matcher = pattern.matcher(parts[0]);
        boolean flag = true;
        String funcName = ""; // 函数名
        ArrayList<String> paraList = new ArrayList<>();
        while (matcher.find()) {
            if (flag) {
                funcName = matcher.group();
                flag = false;
            } else {
                paraList.add(matcher.group());
            }
        }
        funcMap.put(funcName, parts[1]);
        paraMap.put(funcName, paraList);
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

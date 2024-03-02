import java.math.BigInteger;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adjust {
    public static final String simplifyString(String input) {
        String inputInfo = input.replaceAll("(\\+\\+)|(--)", "+");
        inputInfo = inputInfo.replaceAll("(-\\+)|(\\+-)", "-");
        inputInfo = inputInfo.replaceAll("\\^\\+", "^");
        inputInfo = inputInfo.replaceAll("\\*\\+", "*");
        inputInfo = inputInfo.replaceAll("(\\+\\+)|(--)", "+");
        inputInfo = inputInfo.replaceAll("(-\\+)|(\\+-)", "-");
        inputInfo = inputInfo.replaceAll("\\s","");
        return inputInfo;
    }

    private static final BigInteger parseCoefficient(String coefficient) {
        // 如果系数为空，表示为1
        if (coefficient.isEmpty()) {
            return new BigInteger("1");
        }
        return new BigInteger(coefficient);
    }

    private static final String simplifyTerm(BigInteger coefficient, int exponent) {
        // 处理系数和指数，构建化简后的单项式
        if (Objects.equals(coefficient, BigInteger.valueOf(0))) {
            return ""; // 如果系数为0，则单项式为0
        } else if (exponent == 0) {
            return String.valueOf(coefficient); // 如果指数为0，只有系数
        } else if (exponent == 1) {
            return coefficient + "*x";// 如果指数为 1 ，省略 ^1
        } else if (Objects.equals(coefficient, BigInteger.valueOf(1))) {
            return "x^" + exponent; // 系数为1，只输出指数
        } else if (Objects.equals(coefficient, BigInteger.valueOf(-1))) {
            return "-x^" + exponent; // 系数为 -1，只输出指数
        } else {
            return coefficient + "*x^" + exponent; // 一般情况
        }
    }

    public static final String simplifyPoly(String input) {
        Pattern pattern = Pattern.compile("([+-]?\\d*)\\*x\\^(\\d+)");
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            BigInteger coefficient = parseCoefficient(matcher.group(1));
            int exponent = Integer.parseInt(matcher.group(2));
            String simplifiedTerm = simplifyTerm(coefficient, exponent);
            if (result.length() > 0) {
                result.append(matcher.group(1).startsWith("-") ?
                        simplifiedTerm : ("+" + simplifiedTerm));
            } else {
                result.append(simplifiedTerm);
            }
        }
        String ret = result.toString();
        if (ret.isEmpty()) {
            return "0";
        } else if (ret.startsWith("+")) {
            return ret.substring(1);
        }
        return result.toString();
    }
}

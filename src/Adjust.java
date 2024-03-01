
public class Adjust {
    public static final String simplifyString(String input) {
        String inputInfo = input.replaceAll("(\\+\\+)|(--)", "+");
        inputInfo = inputInfo.replaceAll("(-\\+)|(\\+-)", "-");
        inputInfo = inputInfo.replaceAll("\\^\\+", "^");
        inputInfo = inputInfo.replaceAll("\\*\\+", "*");
        inputInfo = inputInfo.replaceAll("\\s","");
        return inputInfo;
    }

    public static final String simplifyPoly(String inputInfo) {
        String result = inputInfo.replaceAll("((\\+)|(-))0\\*x\\^\\d+","");
        result = result.replaceAll("1\\*x\\^","x^");
        result = result.replaceAll("-1\\*x\\^","-x^");
        result = result.replaceAll("\\*x\\^0","");
        result = result.replaceAll("x\\^0","1");
        result = result.replaceAll("x\\^1","x");
        if (result.charAt(0) == '+') {
            result = result.substring(1);
        }
        return result;
    }
}

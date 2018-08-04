package scott.wemessage.commons.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static List<String> getStringListFromString(String string){
        ArrayList<String> matchList = new ArrayList<String>();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(string);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                matchList.add(regexMatcher.group(2));
            } else {
                matchList.add(regexMatcher.group());
            }
        }
        return matchList;
    }

    public static String join(List<String> stringList, String separator, int trimAmount){
        StringBuilder sb = new StringBuilder();
        for (String s : stringList) {
            sb.append(s);
            sb.append(separator);
        }
        String builtString = sb.toString();

        if (builtString.length() == 0) return builtString;

        if(trimAmount > 0) {
            return builtString.substring(0, builtString.length() - trimAmount);
        }else {
            return builtString;
        }
    }

    public static String trimORC(String string){
        char[] chars = string.toCharArray();
        String finalString = "";

        for (char c : chars){
            String hex = String.format("%04x", (int) c);

            if (!hex.equals("fffc")){
                finalString += c;
            }
        }
        return finalString;
    }

    public static String toFixedString(String string, int length) {
        return String.format("%1$" + length + "s", string);
    }

    public static String uppercaseFirst(String string){
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static boolean isEmpty(String string){
        return (string == null) || string.equals("");
    }

    public static boolean containsIgnoreCase(String one, String two){
        return one.toLowerCase().contains(two.toLowerCase());
    }
}
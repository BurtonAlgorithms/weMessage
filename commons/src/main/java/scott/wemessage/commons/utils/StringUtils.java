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

        if(trimAmount > 0) {
            return builtString.substring(0, builtString.length() - trimAmount);
        }else {
            return builtString;
        }
    }

    public static String uppercaseFirst(String string){
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static boolean isEmpty(String string){
        return (string == null) || string.equals("");
    }
}
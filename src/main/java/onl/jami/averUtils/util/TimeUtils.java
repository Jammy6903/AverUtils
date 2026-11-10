package onl.jami.averUtils.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {

    private static final Pattern TOKEN = Pattern.compile("(\\d+)([dhmsMwy])");

    public static Long parseTime(String input) {
        String s = input.replace(" ", "");
        Matcher m = TOKEN.matcher(s);

        long total = 0L;
        int matched = 0;
        while (m.find()) {
            long n = Long.parseLong(m.group(1));
            total += switch (m.group(2)) {
                case "y" -> n * 31_536_000; // 1y = 365d
                case "M" -> n * 2_592_000; // 1M = 30d
                case "w" -> n * 604_800;
                case "d" -> n * 86_400;
                case "h" -> n * 3_600;
                case "m" -> n * 60;
                case "s" -> n;
                default -> throw new IllegalArgumentException();
            };
            matched += m.group().length();
        }
        if (matched != s.length()) {
            return null;
        }
        return total;
    }
}

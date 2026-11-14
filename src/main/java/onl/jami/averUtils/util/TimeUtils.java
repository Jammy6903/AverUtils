package onl.jami.averUtils.util;

import onl.jami.averUtils.AverUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public static String formatDateTime(long millis) {
        String tz = AverUtils.getCfg().getString("timezone");
        if (tz == null) {
            tz = "UTC";
        }
        return DATE_TIME.format(Instant.ofEpochMilli(millis).atZone(ZoneId.of(tz)));
    }

    public static String formatRemainingTimeUntil(Long time) {
        if (time == null) { return "permanent"; }
        long seconds = (time - System.currentTimeMillis()) / 1000L;
        if (seconds < 0) { return "expired"; }

        long days = seconds / 86_400;
        long hours = (seconds % 86_400) / 3_600;
        long minutes = (seconds % 3_600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (secs > 0 || sb.isEmpty()) sb.append(secs).append("s");

        return sb.toString().trim();
    }
}

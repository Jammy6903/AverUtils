package onl.jami.averUtils.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern QUOTED = Pattern.compile("([\"'])(.*?)\\1|(\\S+)");

    public static List<String> parseQuoted(String[] args, int from) {
        String joined = String.join(" ", Arrays.copyOfRange(args, from, args.length));
        Matcher m = QUOTED.matcher(joined);
        List<String> out = new ArrayList<>();
        while (m.find()) {
            out.add(m.group(1) != null ? m.group(2) : m.group(3));
        }
        return out;
    }
}

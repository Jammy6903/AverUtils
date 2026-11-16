package onl.jami.averUtils.util;

import onl.jami.averUtils.exceptions.FlagParseException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern QUOTED = Pattern.compile("([\"'])(.*?)\\1|(\\S+)");

    /**
     * Parse quoted sections in provided arguments
     * @param args provided args containing quoted sections
     * @param from starting argument to parse quotes from
     * @return list of quoted sections
     */
    public static List<String> parseQuoted(String[] args, int from) {
        String joined = String.join(" ", Arrays.copyOfRange(args, from, args.length));
        Matcher m = QUOTED.matcher(joined);
        List<String> out = new ArrayList<>();
        while (m.find()) {
            out.add(m.group(1) != null ? m.group(2) : m.group(3));
        }
        return out;
    }

    public record ParsedFlags(Map<String, List<String>> flags, Map<String, Boolean> switches) {
        public List<String> allFlags(String key) {
            return flags.getOrDefault(key, List.of());
        }

        public Optional<String> single(String key) throws FlagParseException {
            List<String> values = allFlags(key);
            if (values.size() > 1) {
                throw new FlagParseException("Flag may only be used once: " + key);
            }
            return values.stream().findFirst();
        }
    }

    /**
     * Parse flags in the format flag1: argument one flag2: argument 2
     * Parse provided switches as booleans, if arguments contains a switch, it is true
     * @param args provided args containing flags
     * @param from starting argument to parse flags from
     * @param keys set of keys to parse arguments for
     * @return record of flags and switches
     * @throws FlagParseException if duplicate flags, unexpected flags or empty argument
     */
    public static ParsedFlags parseFlags(String[] args, int from, Set<String> keys, Set<String> switches) throws FlagParseException {
        Map<String, List<StringBuilder>> fieldMap = new LinkedHashMap<>();
        Map<String, Boolean> switchMap = new LinkedHashMap<>();
        for (String sw : switches) { switchMap.put(sw, false); }
        StringBuilder current = null;
        for (int i = from; i < args.length; i++) {
            String token = args[i];
            if (switchMap.containsKey(token)) {
                switchMap.put(token, true);
                continue;
            }
            if (token.startsWith("-") && token.length() == 2) {
                for (String sw : switchMap.keySet()) {
                    if (sw.equalsIgnoreCase(token)) {
                        throw new FlagParseException("Unknown switch " + token + " (did you mean " + sw + "?)");
                    }
                }
            }
            int colon = token.indexOf(':');
            String key = colon > 0 ? token.substring(0, colon).toLowerCase(Locale.ROOT) : null;
            if (key != null && keys.contains(key)) {
                current = new StringBuilder(token.substring(colon + 1));
                fieldMap.computeIfAbsent(key, k -> new ArrayList<>()).add(current);
            } else {
                if (current == null) {
                    throw new FlagParseException("Unexpected argument: " + token);
                }
                if (!current.isEmpty()) { current.append(" "); }
                current.append(token);
            }
        }

        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<StringBuilder>> entry : fieldMap.entrySet()) {
            List<String> values = new ArrayList<>();
            for (StringBuilder sb : entry.getValue()) {
                if (sb.isEmpty()) {
                    throw new FlagParseException("Empty argument found in provided args: " + entry.getKey());
                }
                values.add(sb.toString());
            }
            result.put(entry.getKey(), values);
        }

        return new ParsedFlags(result, switchMap);
    }
}

package onl.jami.averUtils.staff.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.schedules.Scheduled;
import onl.jami.averUtils.social.messaging.Message;
import onl.jami.averUtils.storage.database.ScheduleRepo;
import onl.jami.averUtils.storage.database.ScheduleType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Schedule implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length < 3) { return false; }
        ScheduleType type;
        try {
            type = ScheduleType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            Messages.send(sender, "commands.schedule.failure.invalid-type");
            return true;
        }
        Long interval;
        String message;
        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 5) { return false; }
                interval = parseTime(args[3]);
                if (interval == null || interval == 0) { return false; }
                message = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                create(sender, args[2], type, interval, message);
                break;
            case "edit":
                if (args.length < 5) { return false; }
                interval = parseTime(args[3]);
                if (interval == null || interval == 0) { return false; }
                message = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                edit(sender, args[2], type, interval, message);
                break;
            case "delete":
                delete(sender, args[2], type);
                break;
            case "set":
                set(sender, args[2], type, args);
                break;
            default:
                return false;
        }
        Scheduled.update();
        return true;
    }

    public static void create(CommandSender sender, String name, ScheduleType type, Long interval, String message) {
        try {
            ScheduleRepo.create(name, type, message, interval);
            Messages.send(sender, "commands.schedule.create.success",
                    Placeholder.component("type", Component.text(type.name())),
                    Placeholder.component("name", Component.text(name)),
                    Placeholder.component("message", Component.text(message))
            );
        } catch (SQLException e) {
            Messages.send(sender, "commands.schedule.create.failure.already-exists");
            AverUtils.logWarning(Messages.getString("errors.database.schedules"), e);
        }
    }

    public static void edit(CommandSender sender, String name, ScheduleType type, Long interval, String message) {
        try {
            if (!ScheduleRepo.edit(name, type, message, interval)) {
                Messages.send(sender, "commands.schedule.edit.failure.doesnt-exist");
                return;
            }
            Messages.send(sender, "commands.schedule.edit.success",
                    Placeholder.component("type", Component.text(type.name())),
                    Placeholder.component("name", Component.text(name)),
                    Placeholder.component("message", Component.text(message))
            );
        } catch (SQLException e) {
            Messages.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.database.schedules"), e);
        }
    }

    public static void delete(CommandSender sender, String name, ScheduleType type) {
        try {
            if (!ScheduleRepo.delete(name, type)) {
                Messages.send(sender, "commands.schedule.delete.failure.doesnt-exist");
                return;
            }
            Messages.send(sender, "commands.schedule.delete.success",
                    Placeholder.component("type", Component.text(type.name())),
                    Placeholder.component("name", Component.text(name))
            );
        } catch (SQLException e) {
            Messages.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.database.schedules"), e);
        }
    }

    public static void set(CommandSender sender, String name, ScheduleType type, String[] active) {
        try {
            boolean activeBool = false;
            if (active.length < 4) {
                ScheduleRepo.ScheduleData scheduleData = ScheduleRepo.getByName(name, type);
                if (scheduleData != null && !scheduleData.isActive()) {
                    activeBool = true;
                }
            } else {
                activeBool = Boolean.parseBoolean(active[3]);
            }
            if (!ScheduleRepo.setActive(name, type, activeBool)) {
                Messages.send(sender, "commands.schedule.set.failure.doesnt-exist");
                return;
            }
            Messages.send(sender, "commands.schedule.set.success",
                    Placeholder.component("type", Component.text(type.name())),
                    Placeholder.component("name", Component.text(name)),
                    Placeholder.component("state", Component.text(activeBool))
            );
        } catch (SQLException e) {
            Messages.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.database.schedules"), e);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return List.of("create", "edit", "delete", "set");
        }
        if (args.length == 2) {
            return List.of("command", "message");
        }
        if (args.length == 4) {
            if (List.of("create", "edit").contains(args[0].toLowerCase())) {
                return List.of("30m", "1h", "2h", "4h", "1d", "1d6h");
            }
            if (args[0].equalsIgnoreCase("set")) {
                return List.of("true", "false");
            }
        }
        return List.of();
    }

    private static final Pattern TOKEN = Pattern.compile("(\\d+)([dhmsMwy])");

    public static Long parseTime(String input) {
        String s = input.replace(" ", "");
        Matcher m = TOKEN.matcher(s);

        Long total = 0L;
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

package onl.jami.averUtils.staff.moderation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.PunishmentRepo;
import onl.jami.averUtils.storage.database.PunishmentType;
import onl.jami.averUtils.util.StringUtils;
import onl.jami.averUtils.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class Punish implements TabExecutor {

    private static final List<String> DURATION_PUNISHMENTS = List.of("TIMEOUT", "TEMP_BAN");
    private static final List<String> NON_CREATE = List.of("REMOVE", "EDIT_DETAILS");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player p && args.length < 2) {
            // show ui
            return true;
        }
        if (args.length < 2) {
            return false;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(args[0]);
        Player target = null;
        if (offlinePlayer == null) {
            Messages.send(sender, "commands.punish.failure.player-not-joined");
            return true;
        }
        if (offlinePlayer.isOnline()) {
            target = offlinePlayer.getPlayer();
        }

        String duration = null;
        Long durationEnd = null;
        Long parsedTime;

        List<String> arguments;
        PunishmentType punishmentType;

        switch(args[1].toUpperCase(Locale.ROOT)) {
            case "WARN":
                durationEnd = warnKickDuration();
                if (durationEnd == null) { Messages.send(sender, "errors.config"); }
                punishmentType = PunishmentType.WARN;
                arguments = parseReasonDetails(args, 2);
                if (arguments.size() > 2) { return false; }
                if (target != null) {
                    target.sendMessage(getComponent("moderation.punishments.strings.warn.on-warn",
                            Placeholder.component("reason", Component.text(arguments.getFirst()))
                    ));
                }
                break;
            case "KICK":
                if (target == null) {
                    Messages.send(sender, "commands.punish.kick.failure.");
                    return true;
                }
                durationEnd = warnKickDuration();
                if (durationEnd == null) { Messages.send(sender, "errors.config"); }
                punishmentType = PunishmentType.KICK;
                arguments = parseReasonDetails(args, 2);
                if (arguments.size() > 2) { return false; }
                if (target != null) {
                    target.kick(getComponent("moderation.punishments.strings.kick",
                            Placeholder.component("reason", Component.text(arguments.getFirst()))
                    ));
                }
                break;
            case "TIMEOUT":
                if (args.length < 3) { return false;}

                duration = args[2];
                parsedTime = TimeUtils.parseTime(args[2]);
                if (parsedTime == null) { return false; }
                durationEnd = System.currentTimeMillis() + parsedTime * 1000L;

                punishmentType = PunishmentType.TIMEOUT;
                arguments = parseReasonDetails(args, 3);
                if (arguments.size() > 2) { return false; }
                if (target != null) {
                    target.sendMessage(getComponent("moderation.punishments.strings.timeout.on-timeout",
                            Placeholder.component("reason", Component.text(arguments.getFirst())),
                            Placeholder.component("duration", Component.text(duration))
                    ));
                }
                break;
            case "TEMP_BAN":
                if (args.length < 3) { return false;}

                duration = args[2];
                parsedTime = TimeUtils.parseTime(args[2]);
                if (parsedTime == null) { return false; }
                durationEnd = System.currentTimeMillis() + parsedTime * 1000L;

                punishmentType = PunishmentType.TEMP_BAN;
                arguments = parseReasonDetails(args, 3);
                if (arguments.size() > 2) { return false; }
                if (target != null) {
                    target.kick(getComponent("moderation.punishments.strings.temp-ban",
                            Placeholder.component("reason", Component.text(arguments.getFirst())),
                            Placeholder.component("duration", Component.text(duration)),
                            Placeholder.component("remaining", Component.text(TimeUtils.formatRemainingTimeUntil(durationEnd))),
                            Placeholder.component("id", Component.text("pending"))
                    ));
                }
                break;
            case "BAN":
                punishmentType = PunishmentType.BAN;
                arguments = parseReasonDetails(args, 2);
                if (arguments.size() > 2) { return false; }
                if (target != null) {
                    target.kick(getComponent("moderation.punishments.strings.ban",
                            Placeholder.component("reason", Component.text(arguments.getFirst())),
                            Placeholder.component("id", Component.text("pending"))
                    ));
                }
                break;
            case "REMOVE":
                try {
                    if (PunishmentRepo.setExpired(Integer.parseInt(args[2]), true)) {
                        Messages.send(sender, "commands.punish.remove.success",
                                Placeholder.component("id", Component.text(args[2])),
                                Placeholder.component("player", Component.text(args[0])) // TODO: validate this
                        );
                    } else {
                        Messages.send(sender, "commands.punish.remove.failure.doesnt-exist");
                    }
                } catch (SQLException e) {
                    Messages.send(sender, "commands.generic.failure.unknown");
                    AverUtils.logWarning(Messages.getString("errors.database.punishments"), e);
                }
                return true;
            case "EDIT_DETAILS":
                return true;
            default:
                return false;
        }

        UUID punisherUuid = null;
        if (sender instanceof Player p) {
            punisherUuid = p.getUniqueId();
        }

        try {
            PunishmentRepo.create(offlinePlayer.getUniqueId(), punisherUuid, punishmentType, duration, durationEnd, arguments.getFirst(), arguments.getLast());
            Messages.send(sender, "commands.punish." + args[1].toLowerCase(Locale.ROOT) + ".success",
                    Placeholder.component("reason", Component.text(arguments.getFirst())),
                    Placeholder.component("duration", Component.text(TimeUtils.formatRemainingTimeUntil(durationEnd))),
                    Placeholder.component("details", Component.text(arguments.getLast()))
            );
        } catch (Exception e) {
            Messages.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.database.punishments"), e);
        }

        return true;
    }

    public static Component getComponent(String key, TagResolver... tagResolvers) {
        String raw = AverUtils.getCfg().getString(key);
        if (raw == null) { raw = key; }
        return MiniMessage.miniMessage().deserialize(raw, tagResolvers);
    }

    private static List<String> parseReasonDetails(String[] args, int start) {
        List<String> arguments = StringUtils.parseQuoted(args, start);
        if (arguments.isEmpty()) {
            arguments.add(AverUtils.getCfg().getString("moderation.punishments.no-reason"));
        }
        if (arguments.size() < 2) {
            arguments.add(AverUtils.getCfg().getString("moderation.punishments.no-details"));
        }
        return arguments;
    }

    private static Long warnKickDuration() {
        String duration = AverUtils.getCfg().getString("moderation.punishments.warn-kick-duration");
        Long secs = duration == null ? null : TimeUtils.parseTime(duration);
        return secs == null ? null : System.currentTimeMillis() + secs * 1000L;
    }

    private static void remove(CommandSender sender, OfflinePlayer offlinePlayer, int punishmentId) {

    }

    private static void editDetails(CommandSender sender, OfflinePlayer offlinePlayer, int punishmentId, String details) {

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return null;
        }
        if (args.length == 2) {
            return List.of("WARN", "KICK", "TIMEOUT", "TEMP_BAN", "BAN", "REMOVE", "EDIT_DETAILS");
        }
        if (args.length == 3) {
            if (List.of("REMOVE", "EDIT_DETAILS").contains(args[1].toUpperCase(Locale.ROOT))) {
                List<String> ids = new ArrayList<>();
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(args[0]);
                try {
                  List<PunishmentRepo.PunishmentData> punishments = PunishmentRepo.getByUUID(offlinePlayer.getUniqueId());
                  for (PunishmentRepo.PunishmentData p : punishments) {
                      ids.add(String.valueOf(p.id()));
                  }
                  return ids;
                } catch (SQLException e) {
                    AverUtils.logWarning(Messages.getString("errors.database.punishments"), e);
                }
                return List.of("[punishment id]");
            }
            if (DURATION_PUNISHMENTS.contains(args[1].toUpperCase(Locale.ROOT))) {
                return List.of("[duration: format 8h30m (y,M,w,d,h,m");
            }
            return List.of("'<reason>'");
        }
        if (args.length == 4) {
            if (args[1].equalsIgnoreCase("REMOVE")) {
                return List.of("'[details]'");
            }
            if (DURATION_PUNISHMENTS.contains(args[1].toUpperCase(Locale.ROOT))) {
                return List.of("'<reason>'");
            }
            return List.of("'[details]'");
        }
        return List.of();
    }
}

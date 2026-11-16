package onl.jami.averUtils.commands.moderation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.exceptions.DurationParseException;
import onl.jami.averUtils.exceptions.FlagParseException;
import onl.jami.averUtils.exceptions.PlayerNotOnlineException;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.managers.PunishmentManager;
import onl.jami.averUtils.menus.punish.PunishMenu;
import onl.jami.averUtils.menus.punish.playerdetails.PlayerDetailsMenu;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.database.PunishmentType;
import onl.jami.averUtils.util.StringUtils;
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

    private static final Set<String> KEYS = Set.of("player", "duration", "reason", "details");
    private static final Set<String> SWITCHES = Set.of("-s");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player p && args.length < 2) {
            try {
                PunishMenu menu = new PunishMenu();
                if (args.length == 0) {
                    p.openInventory(menu.getInventory());
                }
                if (args.length == 1) {
                    try {
                        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[0]);
                        if (target == null) {
                            Locale.send(sender, "commands.punish.failure.player-not-joined");
                            return true;
                        }
                        PlayerRepo.PlayerData playerData = PlayerRepo.getPlayer(target.getUniqueId());
                        if (playerData == null) {
                            Locale.send(sender, "commands.punish.failure.player-not-joined");
                            return true;
                        }
                        PlayerDetailsMenu playerDetailsMenu = new PlayerDetailsMenu(menu, playerData);
                        p.openInventory(playerDetailsMenu.getInventory());
                    } catch (SQLException e) {
                        Locale.send(sender, "commands.generic.failure.unknown");
                        AverUtils.logWarning("errors.database.players", e);
                        return true;
                    }
                }
            } catch (SQLException e) {
                Locale.send(sender, "commands.generic.failure.unknown");
                AverUtils.logWarning(Locale.getString("errors.database.players"), e);
            }
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        PunishmentManager.Builder punishment = new PunishmentManager.Builder();

        String subCommand = args[0].toUpperCase(java.util.Locale.ROOT);

        if (List.of("REMOVE", "EDIT").contains(subCommand)) {
            // handle punishment editing
            return true;
        }

        if (!List.of("BAN", "KICK", "TIMEOUT", "WARN").contains(subCommand)) {
            return false;
        }

        try {
            punishment.setType(PunishmentType.valueOf(subCommand));
            StringUtils.ParsedFlags arguments = StringUtils.parseFlags(args, 1, KEYS, SWITCHES);
            for (String p : arguments.allFlags("player")) {
                OfflinePlayer t = Bukkit.getOfflinePlayerIfCached(p);
                if (t == null) {
                    Locale.send(sender, "commands.punish.failure.player-not-joined");
                } else {
                    if (sender instanceof Player staff && t.equals(staff)) {
                        Locale.send(sender, "commands.punish.failure.self");
                        return true;
                    }
                    punishment.addTarget(t);
                }
            }
            if (sender instanceof Player p) {
                punishment.setStaff(p.getUniqueId());
            }
            punishment.setDuration(arguments.single("duration").orElse(null));
            punishment.setReason(arguments.single("reason").orElse(null));
            punishment.setDetails(arguments.single("details").orElse(null));
            if (arguments.switches().get("-s")) {
                punishment.setSilent(true);
            }
            PunishmentManager.issue(punishment.build());
        } catch (DurationParseException | NumberFormatException e) {
            Locale.send(sender, "commands.punish.failure.invalid-duration");
        } catch (IllegalArgumentException e) {
            Locale.send(sender, "commands.punish.failure.invalid-arguments");
        } catch (FlagParseException e) {
            Locale.send(sender, "commands.punish.failure.invalid-flag",
                    Placeholder.component("error", Component.text(e.getMessage())));
        } catch (PlayerNotOnlineException e) {
            Locale.send(sender, "commands.punish.failure.player-not-online");
        } catch (SQLException e) {
            Locale.send(sender, "commands.generic.failure.unknown");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return List.of("warn", "kick", "timeout", "ban", "remove", "edit");
        }
        String previous = args[args.length - 2].toLowerCase(java.util.Locale.ROOT);
        String current = args[args.length - 1].toLowerCase(java.util.Locale.ROOT);
        if (current.startsWith("player:")) {
            return filter(onlineNames("player:"), current);
        }
        if (previous.equals("player:")) {
            return filter(onlineNames(""), current);
        }
        Set<String> used = new HashSet<>();
        for (int i = 1; i < args.length - 1; ++i) {
            String token = args[i].toLowerCase(java.util.Locale.ROOT);
            int colon = token.indexOf(':');
            if (colon > 0) { used.add(token.substring(0, colon)); }
            else if (SWITCHES.contains(token)) { used.add(token); }
        }
        List<String> options = new ArrayList<>();
        for (String key : KEYS) {
            if (key.equals("player") || !used.contains(key)) { options.add(key + ":"); }
        }
        for (String sw : SWITCHES) {
            if (!used.contains(sw)) { options.add(sw); }
        }
        return filter(options, current);
    }

    private static List<String> onlineNames(String prefix) {
        return Bukkit.getOnlinePlayers().stream().map(p -> prefix + p.getName()).toList();
    }

    private static List<String> filter(List<String> options, String current) {
        return options.stream().filter(s -> s.toLowerCase(java.util.Locale.ROOT).startsWith(current)).sorted().toList();
    }
}

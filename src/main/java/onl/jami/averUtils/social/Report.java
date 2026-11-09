package onl.jami.averUtils.social;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.social.messaging.Message;
import onl.jami.averUtils.storage.database.ReportRepo;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Report implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        if (args.length < 2) { return false; }

        UUID target = null;
        if (!args[0].equalsIgnoreCase("unknown")) {
            target = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
        }

        if (!AverUtils.getCfg().getStringList("reporting.reasons").contains(args[1])) {
            Messages.send(sender, "commands.report.failure.invalid-reason");
            return true;
        }

        String details = "No details";
        if (args.length > 2) {
            details = String.join(" ", Arrays.copyOfRange(args, 2, args.length));;
        }

        try {
            ReportRepo.create(target, p.getUniqueId(), args[1], details);
            Messages.send(sender, "commands.report.success",
                    Placeholder.component("player", Component.text(args[0])),
                    Placeholder.component("reason", Component.text(args[1])),
                    Placeholder.component("details", Component.text(details))
            );
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("averutils.moderation.reports")) {
                    Messages.send(player, "moderation.new-report");
                }
            }
        } catch (SQLException e) {
            Messages.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.database.reports"), e);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).toList());
            players.add("Unknown");
            return players;
        }
        if (args.length == 2) {
            return AverUtils.getCfg().getStringList("reporting.reasons");
        }
        return List.of();
    }
}

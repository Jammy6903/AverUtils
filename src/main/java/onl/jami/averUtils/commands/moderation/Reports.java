package onl.jami.averUtils.commands.moderation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.menus.report.ReportMenu;
import onl.jami.averUtils.storage.database.ReportRepo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public class Reports implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Locale.send(sender, "commands.generic.failure.unknown");
            return true;
        }

        try {
            ReportMenu menu = new ReportMenu();
            p.openInventory(menu.getInventory());
        } catch (SQLException e) {
            Locale.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Locale.getString("errors.database.reports"), e);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }

    public static void sendReportsOnJoin(Player player) {
        if (player.hasPermission("averutils.moderation.reports")) {
            try {
                List<ReportRepo.ReportData> reports = ReportRepo.getByActive(true);
                if (!reports.isEmpty()) {
                    Locale.send(player, "moderation.reports-pending",
                            Placeholder.component("count", Component.text(reports.size()))
                    );
                }
            } catch (SQLException exception) {
                AverUtils.logWarning(Locale.getString("errors.database.reports"), exception);
            }
        }
    }
}

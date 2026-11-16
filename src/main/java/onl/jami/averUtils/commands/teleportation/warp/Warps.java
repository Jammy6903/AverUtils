package onl.jami.averUtils.commands.teleportation.warp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.storage.database.WarpRepo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public class Warps implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        try {
            List<WarpRepo.WarpData> warps = WarpRepo.listWarps();
            if (warps.isEmpty()) {
                Locale.send(sender, "commands.warps.failure.doesnt-exist");
                return true;
            }
            StringBuilder warpsString = new StringBuilder();
            for (WarpRepo.WarpData warp : warps) {
                warpsString.append(warp.name()).append(", ");
            }
            warpsString.delete(warpsString.length() - 2, warpsString.length());
            Locale.send(sender, "commands.warps.success",
                    Placeholder.component("warps", Component.text(warpsString.toString()))
            );
        } catch (SQLException exception) {
            Locale.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Locale.getString("errors.database.warps"), exception);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

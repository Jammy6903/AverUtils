package onl.jami.averUtils.teleportation.warp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.database.WarpRepo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class Warps implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        try {
            List<WarpRepo.WarpData> warps = WarpRepo.listWarps();
            StringBuilder warpsString = new StringBuilder();
            for (WarpRepo.WarpData warp : warps) {
                warpsString.append(warp.name()).append(", ");
            }
            warpsString.delete(warpsString.length() - 2, warpsString.length());
            sender.sendMessage(Component.text("Warps: \n" + warpsString,  NamedTextColor.YELLOW));
        } catch (SQLException exception) {
            sender.sendMessage(Component.text("An unknown error has occurred", NamedTextColor.RED));
            AverUtils.logWarning("Failed to fetch warps from database", exception);
        }
        return true;
    }
}

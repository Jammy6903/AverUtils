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

public class DelWarp implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) { return false; }
        try {
            if (WarpRepo.deleteWarp(args[0])) {
                sender.sendMessage(Component.text("Warp has been deleted.", NamedTextColor.GREEN));
            }
        } catch (SQLException exception) {
            sender.sendMessage(Component.text("An unknown error has occurred", NamedTextColor.RED));
            AverUtils.logWarning("Failed to remove warp from database", exception);
        }
        return true;
    }
}

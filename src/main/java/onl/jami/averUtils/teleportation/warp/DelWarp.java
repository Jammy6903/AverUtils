package onl.jami.averUtils.teleportation.warp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.storage.database.WarpRepo;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.logging.Level;

public class DelWarp implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) { return false; }
        try {
            if (WarpRepo.deleteWarp(args[0]) == true) {
                sender.sendMessage(Component.text("Warp has been deleted.", NamedTextColor.GREEN));
            }
        } catch (SQLException e) {
            sender.sendMessage(Component.text("An unknown error has occurred", NamedTextColor.RED));
            Bukkit.getLogger().log(Level.WARNING, "Warp could not be deleted from database", e);
        }
        return true;
    }
}

package onl.jami.averUtils.teleportation.warp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.database.WarpRepo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class SetWarp implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can execute this command!", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) { return false; }
        try {
            if (!WarpRepo.createWarp(args[0], player.getLocation().getWorld().getName(), player.getX(), player.getY(), player.getZ(), player.getPitch(), player.getYaw())) {
                sender.sendMessage(Component.text("Failed to create warp! Does it already exist?", NamedTextColor.RED));
                return true;
            }
            sender.sendMessage(Component.text("Warp has been created: " + args[0], NamedTextColor.GREEN));
        } catch (SQLException exception) {
            sender.sendMessage(Component.text("An unknown error has occured", NamedTextColor.RED));
            AverUtils.logWarning("Failed to create warp in database", exception);
        }
        return true;
    }
}

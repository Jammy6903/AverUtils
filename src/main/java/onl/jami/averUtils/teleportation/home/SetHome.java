package onl.jami.averUtils.teleportation.home;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.database.HomeRepo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.logging.Level;

public class SetHome implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command!",  NamedTextColor.RED));
            return true;
        }
        String name = "home";
        if (args.length > 0) {
            name = args[0];
        }
        Player player = (Player) sender;
        Location location = player.getLocation();
        try {
            if (HomeRepo.createHome(player.getUniqueId(), name, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getPitch(), location.getYaw())) {
                sender.sendMessage(Component.text("Home has been created!",  NamedTextColor.GREEN));
                return true;
            }
            sender.sendMessage(Component.text("Home could not be created!",  NamedTextColor.RED));
            return true;
        } catch (SQLException exception) {
            sender.sendMessage(Component.text("Home could not be created!",  NamedTextColor.RED));
            AverUtils.logWarning("Failed to create home for player in database", exception);
        }
        return true;
    }
}

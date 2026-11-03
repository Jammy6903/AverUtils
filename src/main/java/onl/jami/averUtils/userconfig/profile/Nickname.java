package onl.jami.averUtils.userconfig.profile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.cache.PlayerCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class Nickname implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can execute this command!", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            return false;
        }
        if (args[0].length() > 16 || args[0].length() < 3) {
            sender.sendMessage(Component.text("Nickname must be 3-16 characters!", NamedTextColor.RED));
            return true;
        }

        try {
            PlayerRepo.updateNickname(((Player) sender).getUniqueId(), args[0]);
            PlayerCache.updatePlayer(((Player) sender).getUniqueId());
            sender.sendMessage(Component.text("Nickname has been updated!", NamedTextColor.GREEN));
        } catch (SQLException exception) {
            sender.sendMessage(Component.text("Failed to update nickname!", NamedTextColor.RED));
            AverUtils.logWarning("Failed to fetch player or role link from database", exception);
        }
        return true;
    }
}

package onl.jami.averUtils.teleportation;

import onl.jami.averUtils.locale.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Spawn implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            Messages.send(sender, "commands.generic.failure.player-only");
            return true;
        }
        ((Player) sender).teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
        Messages.send(sender, "commands.spawn.success");
        return true;
    }
}

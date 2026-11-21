package onl.jami.averUtils.commands.teleportation;

import onl.jami.averUtils.locale.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Spawn implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Locale.send(sender, "commands.generic.failure.players-only");
            return true;
        }
        Location spawn = Bukkit.getWorlds().getFirst().getSpawnLocation().add(0.5, 0, 0.5);
        p.teleport(spawn);
        Locale.send(sender, "commands.spawn.success");
        return true;
    }
}

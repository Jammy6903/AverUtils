package onl.jami.averUtils.teleportation;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Spawn implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        ((Player) sender).teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
        sender.sendMessage("Teleported you to overworld spawn");
        return true;
    }
}

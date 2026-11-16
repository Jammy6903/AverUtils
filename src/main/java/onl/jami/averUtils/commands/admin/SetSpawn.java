package onl.jami.averUtils.commands.admin;

import onl.jami.averUtils.locale.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SetSpawn implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Locale.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        if (p.getWorld() != Bukkit.getWorlds().getFirst()) {
            Locale.send(sender, "commands.setspawn.failure.invalid-world");
            return true;
        }

        Bukkit.getWorlds().getFirst().setSpawnLocation(p.getLocation());
        Locale.send(sender, "commands.setspawn.success");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

package onl.jami.averUtils.userconfig.profile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.cache.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class NameChoice implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can execute this command!", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            return false;
        }
        if (Arrays.stream(PlayerRepo.PlayerData.NameChoice.values()).noneMatch(nc -> nc.name().equals(args[0]))) {
            return false;
        }

        try {
            PlayerRepo.updateNameChoice(((Player) sender).getUniqueId(), args[0]);
            PlayerCache.updatePlayer(((Player) sender).getUniqueId());
            sender.sendMessage(Component.text("Name choice has been updated!", NamedTextColor.GREEN));

        } catch (SQLException e) {
            sender.sendMessage(Component.text("Failed to update name choice!", NamedTextColor.RED));
            Bukkit.getLogger().log(Level.WARNING, "Failed to update a players name choice " + e);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (PlayerRepo.PlayerData.NameChoice choice : PlayerRepo.PlayerData.NameChoice.values()) {
                completions.add(choice.toString());
            }
            return completions;
        }
        return List.of();
    }
}

package onl.jami.averUtils.chat;

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
import java.util.List;
import java.util.logging.Level;

public class Pronouns implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can execute this command!", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            return false;
        }
        if (args[0].length() > 10) {
            sender.sendMessage(Component.text("Pronouns must be 1-10 characters!", NamedTextColor.RED));
            return true;
        }
        try {
            PlayerRepo.updatePronouns(((Player) sender).getUniqueId(), args[0].toLowerCase());
            sender.sendMessage("Successfully updated your pronouns: " + args[0].toLowerCase());
            PlayerCache.updatePlayer(((Player) sender).getUniqueId());
        } catch (SQLException e) {
            sender.sendMessage(Component.text("An error occurred while trying to update your pronouns", NamedTextColor.RED));
            Bukkit.getLogger().log(Level.WARNING, "Could not update players pronouns", e);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of("he/him", "she/her", "they/them", "ask");
    }
}

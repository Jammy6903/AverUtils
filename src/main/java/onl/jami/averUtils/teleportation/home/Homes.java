package onl.jami.averUtils.teleportation.home;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.storage.database.HomeRepo;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class Homes implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command",  NamedTextColor.RED));
        }

        try {
            List<HomeRepo.HomeData> homes = HomeRepo.listHomes(((Player) sender).getUniqueId());
            StringBuilder homesString = new StringBuilder();
            for (HomeRepo.HomeData home : homes) {
                homesString.append(home.name() + ", ");
            }
            homesString.delete(homesString.length() - 2, homesString.length());
            sender.sendMessage(Component.text("Your homes: \n" + homesString,  NamedTextColor.YELLOW));
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to fetch homes for player " + e);
        }
        return true;
    }
}

package onl.jami.averUtils.teleportation.home;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.storage.database.HomeRepo;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DelHome implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command",  NamedTextColor.RED));
        }
        if (args.length == 0) {
            return false;
        }
        try {
            HomeRepo.deleteHome(((Player) sender).getUniqueId(), args[0]);
            sender.sendMessage(Component.text("Home: " + args[0] + " has been deleted!",  NamedTextColor.GREEN));
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Could not delete home: " + args[0], e);
            sender.sendMessage(Component.text("Could not delete home: " + args[0], NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            try {
                List<HomeRepo.HomeData> homes = HomeRepo.listHomes(((Player) sender).getUniqueId());
                List<String> results = new ArrayList<>();
                for (HomeRepo.HomeData home : homes) {
                    results.add(home.name());
                }
                return results;
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to fetch homes for player " + e);
            }
        }
        return null;
    }
}

package onl.jami.averUtils.teleportation.home;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.storage.database.HomeRepo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

public class Home implements TabExecutor {

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
        try {
            HomeRepo.HomeData homeData = HomeRepo.getHome(((Player) sender).getUniqueId(), name);
            if (homeData == null) {
                sender.sendMessage(Component.text("Home: " + name + " does not exist.", NamedTextColor.RED));
                return true;
            }
            Location location = new Location(Bukkit.getWorld(homeData.world()), homeData.x(), homeData.y(), homeData.z(),  homeData.yaw(), homeData.pitch());
            ((Player) sender).teleport(location);
            sender.sendMessage(Component.text("Successfully teleported to " + name, NamedTextColor.GREEN));
        } catch (SQLException e) {
            sender.sendMessage(Component.text("Failed to teleport to " + name, NamedTextColor.RED));
            Bukkit.getLogger().warning("Failed to player teleport to their home" + e);
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
                Bukkit.getLogger().log(Level.WARNING, "Failed to fetch homes" + e);
            }
        }
        return null;
    }
}

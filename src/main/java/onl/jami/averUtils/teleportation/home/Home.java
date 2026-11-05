package onl.jami.averUtils.teleportation.home;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
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

public class Home implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }
        String name = "home";
        if (args.length > 0) {
            name = args[0];
        }
        try {
            HomeRepo.HomeData homeData = HomeRepo.getHome(((Player) sender).getUniqueId(), name);
            if (homeData == null) {
                Messages.send(sender, "commands.home.failure.doesnt-exist",
                        Placeholder.component("home", Component.text(args[0]))
                );
                return true;
            }
            Location location = new Location(Bukkit.getWorld(homeData.world()), homeData.x(), homeData.y(), homeData.z(),  homeData.yaw(), homeData.pitch());
            ((Player) sender).teleport(location);
            Messages.send(sender, "commands.home.success",
                    Placeholder.component("home", Component.text(args[0]))
            );
        } catch (SQLException exception) {
            Messages.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.database.homes"), exception);
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
            } catch (SQLException exception) {
                AverUtils.logWarning(Messages.getString("errors.database.homes"), exception);
            }
        }
        return null;
    }
}

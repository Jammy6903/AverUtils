package onl.jami.averUtils.commands.teleportation.home;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.storage.database.HomeRepo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public class Homes implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            Locale.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        try {
            List<HomeRepo.HomeData> homes = HomeRepo.listHomes(((Player) sender).getUniqueId());
            if (homes.isEmpty()) {
                Locale.send(sender, "commands.homes.failure.doesnt-exist");
            }
            StringBuilder homesString = new StringBuilder();
            for (HomeRepo.HomeData home : homes) {
                homesString.append(home.name()).append(", ");
            }
            homesString.delete(homesString.length() - 2, homesString.length());
            Locale.send(sender, "commands.homes.success",
                    Placeholder.component("homes", Component.text(homesString.toString()))
            );
        } catch (SQLException exception) {
            AverUtils.logWarning(Locale.getString("errors.database.homes"), exception);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

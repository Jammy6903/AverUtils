package onl.jami.averUtils.teleportation.home;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.HomeRepo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class Homes implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        try {
            List<HomeRepo.HomeData> homes = HomeRepo.listHomes(((Player) sender).getUniqueId());
            if (homes.isEmpty()) {
                Messages.send(sender, "commands.homes.failure.doesnt-exist");
            }
            StringBuilder homesString = new StringBuilder();
            for (HomeRepo.HomeData home : homes) {
                homesString.append(home.name()).append(", ");
            }
            homesString.delete(homesString.length() - 2, homesString.length());
            Messages.send(sender, "commands.homes.success",
                    Placeholder.component("homes", Component.text(homesString.toString()))
            );
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.homes"), exception);
        }
        return true;
    }
}

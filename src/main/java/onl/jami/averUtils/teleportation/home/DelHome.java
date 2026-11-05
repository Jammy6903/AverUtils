package onl.jami.averUtils.teleportation.home;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.HomeRepo;
import onl.jami.averUtils.storage.database.WarpRepo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DelHome implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }
        if (args.length == 0) {
            return false;
        }
        try {
            if (HomeRepo.deleteHome(((Player) sender).getUniqueId(), args[0])) {
                Messages.send(sender, "commands.delwhome.success",
                        Placeholder.component("home", Component.text(args[0]))
                );
                return true;
            }
            Messages.send(sender, "commands.delhome.failure.doesnt-exist",
                    Placeholder.component("home", Component.text(args[0]))
            );
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.homes"), exception);
            Messages.send(sender, "commands.generic.failure.unknown");

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

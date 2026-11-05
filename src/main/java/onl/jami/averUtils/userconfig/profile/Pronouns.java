package onl.jami.averUtils.userconfig.profile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.cache.PlayerCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public class Pronouns implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            Messages.send(sender, "commands.generic.failure.player-only");
            return true;
        }
        if (args.length == 0) {
            return false;
        }
        if (args[0].length() > 10) {
            Messages.send(sender, "commands.pronouns.failure.length");
            return true;
        }
        try {
            PlayerRepo.updatePronouns(((Player) sender).getUniqueId(), args[0].toLowerCase());
            PlayerCache.updatePlayer(((Player) sender).getUniqueId());
            Messages.send(sender, "commands.pronouns.success",
                    Placeholder.component("pronouns", Component.text(args[0]))
            );
        } catch (SQLException exception) {
            Messages.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return Messages.getList("player.profile.pronouns");
    }
}

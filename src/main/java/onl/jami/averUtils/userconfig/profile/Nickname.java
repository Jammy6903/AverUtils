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

public class Nickname implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            Messages.send(sender, "commands.generic.failure.player-only");
            return true;
        }
        if (args.length == 0) {
            return false;
        }
        if (args[0].length() > 16 || args[0].length() < 3) {
            Messages.send(sender, "commands.nickname.failure.length");
            return true;
        }

        try {
            PlayerRepo.updateNickname(((Player) sender).getUniqueId(), args[0]);
            PlayerRepo.updateNameChoice(((Player) sender).getUniqueId(), PlayerRepo.PlayerData.NameChoice.NICKNAME);
            PlayerCache.updatePlayer(((Player) sender).getUniqueId());
            Messages.send(sender, "commands.nickname.success",
                    Placeholder.component("name", Component.text(args[0]))
            );
        } catch (SQLException exception) {
            Messages.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

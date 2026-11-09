package onl.jami.averUtils.userconfig.profile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.database.PlayerRepo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public class ToggleTpa implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        PlayerRepo.PlayerData playerData = PlayerCache.getPlayer(p);
        if (playerData == null) { return true; }
        boolean tp = !playerData.tpEnabled();
        try {
            PlayerRepo.updateTp(p.getUniqueId(), tp);
            Messages.send(p, "commands.toggletpa.success",
                    Placeholder.component("state", Component.text(tp))
            );
            PlayerCache.updatePlayer(p.getUniqueId());
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

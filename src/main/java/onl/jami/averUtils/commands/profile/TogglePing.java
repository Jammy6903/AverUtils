package onl.jami.averUtils.commands.profile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Locale;
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

public class TogglePing implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Locale.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        PlayerRepo.PlayerData playerData = PlayerCache.getPlayer(p);
        if (playerData == null) { return true; }
        boolean ping = !playerData.pingEnabled();
        try {
            PlayerRepo.updatePing(p.getUniqueId(), ping);
            Locale.send(p, "commands.toggleping.success",
                    Placeholder.component("state", Component.text(ping))
            );
            PlayerCache.updatePlayer(p.getUniqueId());
        } catch (SQLException exception) {
            Locale.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Locale.getString("errors.database.players"), exception);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

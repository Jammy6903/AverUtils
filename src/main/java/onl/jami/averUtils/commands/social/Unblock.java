package onl.jami.averUtils.commands.social;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.storage.cache.BlockCache;
import onl.jami.averUtils.storage.database.BlockRepo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Unblock implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Locale.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        if (args.length == 0) { return false; }

        OfflinePlayer blocked = Bukkit.getOfflinePlayerIfCached(args[0]);

        if (blocked == null || blocked.getUniqueId().equals(p.getUniqueId())) {
            Locale.send(p, "commands.unblock.failure.self");
            return true;
        }

        try {
            if (!BlockRepo.deleteBlock(p.getUniqueId(), blocked.getUniqueId())) {
                Locale.send(sender, "commands.unblock.failure.doesnt-exist",
                        Placeholder.component("player", Component.text(args[0]))
                );
                return true;
            }
            Locale.send(sender, "commands.unblock.success",
                    Placeholder.component("player", Component.text(args[0]))
            );
            BlockCache.loadBlocksForPlayer(p.getUniqueId());
        } catch (Exception exception) {
            Locale.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Locale.getString("errors.database.blocks"), exception);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return null;
        }
        return List.of();
    }
}

package onl.jami.averUtils.social.blocks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
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
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        if (args.length == 0) { return false; }

        OfflinePlayer blocked = Bukkit.getOfflinePlayer(args[0]);

        if (blocked.getUniqueId().equals(p.getUniqueId())) {
            Messages.send(p, "commands.unblock.failure.self");
            return true;
        }

        try {
            if (!BlockRepo.deleteBlock(p.getUniqueId(), blocked.getUniqueId())) {
                Messages.send(sender, "commands.unblock.failure.doesnt-exist",
                        Placeholder.component("player", Component.text(args[0]))
                );
                return true;
            }
            Messages.send(sender, "commands.unblock.success",
                    Placeholder.component("player", Component.text(args[0]))
            );
            BlockCache.loadBlocksForPlayer(p.getUniqueId());
        } catch (Exception exception) {
            Messages.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.database.blocks"), exception);
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

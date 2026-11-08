package onl.jami.averUtils.social.blocks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.BlockCategory;
import onl.jami.averUtils.storage.database.BlockRepo;
import onl.jami.averUtils.storage.database.PlayerRepo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class Block implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        if (args.length == 0) { return false; }

        EnumSet<BlockCategory> categories = EnumSet.allOf(BlockCategory.class);

        if (args.length > 1) {
            categories = EnumSet.noneOf(BlockCategory.class);
            for (int i = 1; i < args.length - 1; i++) {
                try {
                    categories.add(BlockCategory.valueOf(args[i]));
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        }

        try {
            OfflinePlayer blocked = Bukkit.getOfflinePlayer(args[0]);
            if (PlayerRepo.getPlayer(blocked.getUniqueId()) == null) {
                Messages.send(sender, "commands.block.failure.doesnt-exist",
                        Placeholder.component("player", Component.text(args[0]))
                );
                return true;
            }
            BlockRepo.createBlock(p.getUniqueId(), blocked.getUniqueId(), categories);
            Messages.send(sender, "commands.block.success",
                    Placeholder.component("player", Component.text(args[0]))
            );
        } catch (SQLException exception) {
            Messages.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) { return null; }
        if (args.length > 1) {
            List<String> so = new ArrayList<>(Arrays.stream(BlockCategory.values()).map(Enum::name).toList());
            for (int i = 1; i < args.length - 1; i++) {
                so.remove(args[i].toUpperCase());
            }
            return so;
        }
        return List.of();
    }
}

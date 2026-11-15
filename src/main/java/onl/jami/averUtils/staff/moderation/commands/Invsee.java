package onl.jami.averUtils.staff.moderation.commands;

import onl.jami.averUtils.locale.Messages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Invsee implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Messages.send(sender, "commands.generic.failure.unknown");
            return true;
        }

        if (args.length < 1) {
            return false;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[0]);
        if (target == null || !target.isOnline()) {
            Messages.send(sender, "commands.invsee.failure.not-online");
            return true;
        }

        Player targetPlayer = target.getPlayer();
        if (targetPlayer == null) { return true; }

        Inventory inv = args.length > 1 && args[1].equalsIgnoreCase("ENDER_CHEST") ? targetPlayer.getEnderChest() : targetPlayer.getInventory();

        p.openInventory(inv);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return null;
        }
        if (args.length == 2) {
            return List.of("ENDER_CHEST", "INVENTORY");
        }
        return List.of();
    }
}

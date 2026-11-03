package onl.jami.averUtils.teleportation.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Tpa implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can execute this command.", NamedTextColor.RED));
            return true;
        }
        if  (args.length == 0) {
            return false;
        }
        Player recipient = Bukkit.getPlayerExact(args[0]);
        if (recipient == null) {
            sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
        } else {
            TpRequestCache.addRequest(recipient.getUniqueId(), new TpRequestCache.TpRequest(((Player) sender).getUniqueId(), TpRequestCache.TpRequest.Direction.TO_RECIPIENT));
            sender.sendMessage("Requesting to teleport to " + recipient.getName());
            recipient.sendMessage(sender.getName() + " is requesting to teleport to you, /tpaccept to accept, /tpdeny to deny");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return null;
        }
        return Collections.emptyList();
    }
}

package onl.jami.averUtils.teleportation.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TpAccept implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        TpRequestCache.TpRequest latestRequest = TpRequestCache.getLatestRequest(((Player) sender).getUniqueId());
        if (latestRequest == null) {
            sender.sendMessage(Component.text("There is no request for you!", NamedTextColor.RED));
            return true;
        }
        Player requester = Bukkit.getPlayer(latestRequest.requester());
        if (!(requester == null)) {
            if (latestRequest.direction().equals(TpRequestCache.TpRequest.Direction.TO_RECIPIENT)) {
                requester.teleport(((Player) sender).getLocation());
                requester.sendMessage(Component.text("Successfully teleported you to " + sender.getName(), NamedTextColor.GREEN));
                sender.sendMessage(Component.text("Sucessfully teleported " + requester.getName() + " to you.", NamedTextColor.GREEN));
            } else {
                ((Player) sender).teleport(requester.getLocation());
                requester.sendMessage(Component.text("Sucessfully teleported " + sender.getName() + " to you.", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("Successfully teleported you to " + requester.getName(), NamedTextColor.GREEN));
            }
            TpRequestCache.removeLatestRequest(((Player) sender).getUniqueId());
            return true;
        }
        sender.sendMessage(Component.text("The requester is currently offline.", NamedTextColor.RED));
        return true;
    }
}

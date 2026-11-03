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

public class TpDeny implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        TpRequestCache.TpRequest latestRequest = TpRequestCache.getLatestRequest(((Player) sender).getUniqueId());
        if (latestRequest == null) {
            sender.sendMessage(Component.text("There is no request for you!", NamedTextColor.RED));
            return true;
        }
        Player requester = Bukkit.getPlayer(latestRequest.requester());
        sender.sendMessage(Component.text("Request denied.", NamedTextColor.YELLOW));
        TpRequestCache.removeLatestRequest(((Player) sender).getUniqueId());
        if (!(requester == null)) {
            requester.sendMessage(sender.getName() + " denied your tp request");
        }
        return true;
    }
}

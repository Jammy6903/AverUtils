package onl.jami.averUtils.teleportation.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.locale.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpDeny implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        TpRequestCache.TpRequest latestRequest = TpRequestCache.getLatestRequest(((Player) sender).getUniqueId());
        if (latestRequest == null) {
            Messages.send(sender, "commands.tpdeny.failure.no-request");
            return true;
        }
        Player requester = Bukkit.getPlayer(latestRequest.requester());
        if (requester == null) {
            Messages.send(sender, "commands.tpdenny.success.not-online");
            return true;
        }
        Messages.send(sender, "commands.tpdeny.success.receiving",
                Placeholder.component("player", Component.text(requester.getName()))
        );
        Messages.send(requester, "commands.tpdeny.success.sending",
                Placeholder.component("player", Component.text(sender.getName()))
        );
        TpRequestCache.removeLatestRequest(((Player) sender).getUniqueId());
        return true;
    }
}

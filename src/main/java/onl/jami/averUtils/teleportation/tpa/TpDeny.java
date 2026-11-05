package onl.jami.averUtils.teleportation.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.locale.Messages;
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
            Messages.send(sender, "commands.tpdeny.failure.no-request");
            return true;
        }
        Player requester = Bukkit.getPlayer(latestRequest.requester());
        Messages.send(sender, "commands.tpdeny.receiving",
                Placeholder.component("player", Component.text(requester.getDisplayName()))
        );
        Messages.send(requester, "commands.tpdeny.sending",
                Placeholder.component("player", Component.text(sender.getName()))
        );
        TpRequestCache.removeLatestRequest(((Player) sender).getUniqueId());
        return true;
    }
}

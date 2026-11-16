package onl.jami.averUtils.commands.teleportation.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.locale.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpAccept implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Locale.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        TpRequestCache.TpRequest latestRequest = TpRequestCache.getLatestRequest(p.getUniqueId());
        if (latestRequest == null) {
            Locale.send(sender, "commands.tpaccept.failure.no-request");
            return true;
        }
        Player requester = Bukkit.getPlayer(latestRequest.requester());
        if (requester == null) {
            Locale.send(sender, "commands.tpaccept.failure.player-not-found");
            return true;
        }

        if (latestRequest.direction().equals(TpRequestCache.TpRequest.Direction.TO_RECIPIENT)) {
            requester.teleport(p.getLocation());
            Locale.send(requester, "commands.tpaccept.success.tpa.sending",
                    Placeholder.component("player", Component.text(requester.getName()))
            );
            Locale.send(sender, "commands.tpaccept.success.tpa.receiving",
                    Placeholder.component("player", Component.text(sender.getName()))
            );
        } else {
            p.teleport(requester.getLocation());
            Locale.send(requester, "commands.tpaccept.success.tpahere.sending",
                    Placeholder.component("player", Component.text(requester.getName()))
            );
            Locale.send(sender, "commands.tpaccept.success.tpahere.receiving",
                    Placeholder.component("player", Component.text(sender.getName()))
            );
        }
        TpRequestCache.removeLatestRequest(p.getUniqueId());

        return true;
    }
}

package onl.jami.averUtils.teleportation.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.locale.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class TpaHere implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            Messages.send(sender, "commands.generic.failure.player-only");
            return true;
        }
        if  (args.length == 0) { return false; }
        Player recipient = Bukkit.getPlayerExact(args[0]);
        if (recipient == null) {
            Messages.send(sender, "commands.tpahere.failure.player-not-found");
        }
        TpRequestCache.addRequest(recipient.getUniqueId(), new TpRequestCache.TpRequest(((Player) sender).getUniqueId(), TpRequestCache.TpRequest.Direction.TO_REQUESTER));
        Messages.send(sender, "commands.tpahere.success.sending",
                Placeholder.component("player", Component.text(recipient.getName()))
        );
        Messages.send(recipient, "commands.tpahere.success.receiving",
                Placeholder.component("player", Component.text(sender.getName()))
        );
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

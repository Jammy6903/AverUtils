package onl.jami.averUtils.teleportation.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.cache.BlockCache;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.database.BlockCategory;
import onl.jami.averUtils.storage.database.BlockRepo;
import onl.jami.averUtils.storage.database.PlayerRepo;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class Tpa implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Messages.send(sender, "commands.generic.failure.player-only");
            return true;
        }
        if  (args.length == 0) {
            return false;
        }
        Player recipient = Bukkit.getPlayerExact(args[0]);
        if (recipient == null) {
            Messages.send(sender, "commands.tpa.failure.player-not-found");
            return true;
        }

        PlayerRepo.PlayerData recipientData = PlayerCache.getPlayer(recipient);
        if (recipientData != null && !recipientData.tpEnabled()) {
            Messages.send(sender, "commands.tpa.failure.disabled");
            return true;
        }

        BlockRepo.BlockData block = BlockCache.getBlockForPlayers(recipient.getUniqueId(), p.getUniqueId());
        if (block != null && block.categories().contains(BlockCategory.TPA)) {
            Messages.send(sender, "commands.tpa.failure.blocked");
            return true;
        }

        TpRequestCache.addRequest(recipient.getUniqueId(), new TpRequestCache.TpRequest(((Player) sender).getUniqueId(), TpRequestCache.TpRequest.Direction.TO_RECIPIENT));
        Messages.send(sender, "commands.tpa.success.sending",
                Placeholder.component("player", Component.text(recipient.getName()))
        );
        Messages.send(recipient, "commands.tpa.success.receiving",
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

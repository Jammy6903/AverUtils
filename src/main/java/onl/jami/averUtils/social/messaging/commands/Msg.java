package onl.jami.averUtils.social.messaging.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.social.messaging.Message;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.staff.moderation.commands.Punish;
import onl.jami.averUtils.storage.cache.BlockCache;
import onl.jami.averUtils.storage.cache.PunishmentCache;
import onl.jami.averUtils.storage.database.BlockCategory;
import onl.jami.averUtils.storage.database.BlockRepo;
import onl.jami.averUtils.storage.database.PunishmentRepo;
import onl.jami.averUtils.storage.database.PunishmentType;
import onl.jami.averUtils.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Msg implements TabExecutor {

    private static final Map<UUID, UUID> LAST_MESSAGED_BY = new ConcurrentHashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length < 2) {
            return false;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            Messages.send(sender, "commands.msg.failure.doesnt-exist");
            return true;
        }
        if (sender instanceof Player p) {
            for (PunishmentRepo.PunishmentData punishment : PunishmentCache.getCachedPunishmentsWithActiveCheck(p.getUniqueId())) {
                if (punishment.type().equals(PunishmentType.TIMEOUT)) {
                    sender.sendMessage(Punish.getComponent("moderation.punishments.strings.timeout.on-action",
                            Placeholder.component("reason", Component.text(punishment.reason())),
                            Placeholder.component("remaining", Component.text(TimeUtils.formatRemainingTimeUntil(punishment.durationEnd())))
                    ));
                    return true;
                }
            }

            BlockRepo.BlockData block = BlockCache.getBlockForPlayers(target.getUniqueId(), p.getUniqueId());
            if (block != null && block.categories().contains(BlockCategory.MESSAGES) && !p.hasPermission("averutils.bypass-block")) {
                Messages.send(sender, "commands.msg.failure.blocked");
                return true;
            }
        }

        Message.sendMessage(sender, target, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));

        if (sender instanceof Player) {
            LAST_MESSAGED_BY.put(target.getUniqueId(), ((Player) sender).getUniqueId());
            LAST_MESSAGED_BY.put(((Player) sender).getUniqueId(), target.getUniqueId());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) { return null; }
        return Collections.emptyList();
    }

    public static UUID getLastMessagedBy(UUID uuid) {
        return LAST_MESSAGED_BY.get(uuid);
    }
}

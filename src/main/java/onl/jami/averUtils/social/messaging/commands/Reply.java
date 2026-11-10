package onl.jami.averUtils.social.messaging.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.social.messaging.Message;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.staff.moderation.Punish;
import onl.jami.averUtils.storage.cache.PunishmentCache;
import onl.jami.averUtils.storage.database.PunishmentRepo;
import onl.jami.averUtils.storage.database.PunishmentType;
import onl.jami.averUtils.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Reply implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }
        for (PunishmentRepo.PunishmentData punishment : PunishmentCache.getCachedPunishmentsWithActiveCheck(p.getUniqueId())) {
            if (punishment.type().equals(PunishmentType.TIMEOUT)) {
                sender.sendMessage(Punish.getComponent("moderation.punishments.strings.timeout.on-action",
                        Placeholder.component("reason", Component.text(punishment.reason())),
                        Placeholder.component("remaining", Component.text(TimeUtils.formatRemainingTimeUntil(punishment.durationEnd())))
                ));
                return true;
            }
        }

        UUID targetId = Msg.getLastMessagedBy(p.getUniqueId());
        if (targetId == null) {
            Messages.send(sender, "commands.reply.failure.not-online");
            return true;
        }
        Player target = Bukkit.getPlayer(targetId);
        if (target == null) {
            Messages.send(sender, "commands.reply.failure.no-messages");
            return true;
        }

        Message m = new Message();
        m.formatMessage(target, sender, String.join(" ", Arrays.copyOfRange(args, 0, args.length)));
        m.sendToRecipients();
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

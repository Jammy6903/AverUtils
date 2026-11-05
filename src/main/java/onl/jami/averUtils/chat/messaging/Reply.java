package onl.jami.averUtils.chat.messaging;

import onl.jami.averUtils.locale.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public class Reply implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }
        UUID targetId = Msg.getLastMessagedBy(((Player) sender).getUniqueId());
        if (targetId == null) {
            Messages.send(sender, "commands.reply.failure.not-online");
            return true;
        }
        Player target = Bukkit.getPlayer(targetId);
        if (target == null) {
            Messages.send(sender, "commands.reply.failure.no-messages");
        }

        Message m = new Message();
        m.formatMessage(target, sender, String.join(" ", Arrays.copyOfRange(args, 0, args.length)));
        m.sendToRecipients();

        return true;
    }
}

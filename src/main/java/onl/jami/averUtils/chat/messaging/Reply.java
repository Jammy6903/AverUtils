package onl.jami.averUtils.chat.messaging;

import net.kyori.adventure.text.format.NamedTextColor;
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
            sender.sendMessage(NamedTextColor.RED + "Only players can execute this command.");
            return true;
        }
        UUID targetId = Msg.getLastMessagedBy(((Player) sender).getUniqueId());
        if (targetId != null) {
            Player target = Bukkit.getPlayer(targetId);
            if (!(target == null)) {
                String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
                Message m = new Message();
                m.formatMessage(target, sender, message, 1);
                m.formatMessage(sender, target, message, 0);
                m.sendToRecipients();
            } else {
                sender.sendMessage(NamedTextColor.RED + "Player is not online!");
            }
        } else {
            sender.sendMessage(NamedTextColor.RED + "You haven't received a message recently.");
        }
        return true;
    }
}

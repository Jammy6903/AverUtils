package onl.jami.averUtils.chat.messaging;

import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.locale.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Msg implements TabExecutor {

    private static final Map<UUID, UUID> lastMessagedBy = new ConcurrentHashMap<>();

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

        Message m = new Message();
        m.formatMessage(target, sender, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        m.sendToRecipients();

        if (sender instanceof Player) {
            lastMessagedBy.put(target.getUniqueId(), ((Player) sender).getUniqueId());
            lastMessagedBy.put(((Player) sender).getUniqueId(), target.getUniqueId());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) { return null; }
        return Collections.emptyList();
    }

    public static UUID getLastMessagedBy(UUID uuid) {
        return lastMessagedBy.get(uuid);
    }
}

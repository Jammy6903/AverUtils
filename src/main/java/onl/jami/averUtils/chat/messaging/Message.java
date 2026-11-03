package onl.jami.averUtils.chat.messaging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.chat.PlayerName;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Message {

    private final Map<CommandSender, Component> messages = new HashMap<>();

    public void formatMessage(CommandSender recipient, CommandSender other, String message, Integer direction) {
        Component prefix = Component.text("Member", NamedTextColor.BLUE);

        String dir = " ⬅ ";

        if (direction == 1) {
            dir = " ➡ ";
        }

        Component finalMessage = Component.text("[✉] ", NamedTextColor.YELLOW)
                .append(Component.text("[", NamedTextColor.GRAY))
                .append(prefix)
                .append(Component.text("] ", NamedTextColor.GRAY))
                .append(PlayerName.getComponent((Player) other))
                .append(Component.text(dir, NamedTextColor.GRAY))
                .append(Component.text(message, NamedTextColor.WHITE));

        messages.put(recipient, finalMessage);
    }

    public void sendToRecipients() {
        for (Map.Entry<CommandSender, Component> entry : messages.entrySet()) {
            entry.getKey().sendMessage(entry.getValue());
        }
    }

}

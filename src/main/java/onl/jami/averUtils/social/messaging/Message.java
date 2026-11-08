package onl.jami.averUtils.social.messaging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.social.chat.Formatter;
import onl.jami.averUtils.locale.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Message {

    private final Map<CommandSender, Component> messages = new HashMap<>();

    public void formatMessage(CommandSender recipient, CommandSender other, String message) {
        messages.put(recipient, buildForRecipient(other, message, true));
        messages.put(other, buildForRecipient(recipient, message, false));
    }

    public Component buildForRecipient(CommandSender recipient, String message, boolean forSender) {
        String messageFormatString = Messages.getString("chat.message.format.sending");
        if (forSender) {
            messageFormatString = Messages.getString("chat.message.format.receiving");
        }
        String prefixFormatString = Messages.getString("chat.message.format.prefix");
        String suffixFormatString = Messages.getString("chat.message.format.suffix");
        String pronounsFormatString = Messages.getString("chat.message.format.pronouns");

        Component prefix = Component.empty();
        Component suffix = Component.empty();
        Component pronouns = Component.empty();
        Component name = Component.text("CONSOLE");

        if (recipient instanceof Player player) {
            Formatter.MessageData messageData = Formatter.getMessageData(player, prefixFormatString, suffixFormatString, pronounsFormatString);
            prefix = messageData.prefix();
            suffix = messageData.suffix();
            pronouns = messageData.pronouns();
            name = messageData.name();
        }

        return MiniMessage.miniMessage().deserialize(messageFormatString,
                Placeholder.component("prefix", prefix),
                Placeholder.component("suffix", suffix),
                Placeholder.component("name", name),
                Placeholder.component("pronouns", pronouns),
                Placeholder.component("message", Component.text(message)));
    }

    public void sendToRecipients() {
        for (Map.Entry<CommandSender, Component> entry : messages.entrySet()) {
            entry.getKey().sendMessage(entry.getValue());
        }
    }

}

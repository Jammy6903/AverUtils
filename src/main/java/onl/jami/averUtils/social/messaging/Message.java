package onl.jami.averUtils.social.messaging;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.social.chat.Formatter;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.staff.admin.SocialSpy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Message {

    private static final Sound PING = Sound.sound(
            Key.key("entity.experience_orb.pickup"),
            Sound.Source.PLAYER,
            1.0f, 1.5f
    );

    public static void sendMessage(CommandSender sender, CommandSender recipient, String message) {
        String senderFormatString = Messages.getString("chat.message.format.sender");
        String recipientFormatString = Messages.getString("chat.message.format.recipient");

        sender.sendMessage(buildMessage(sender, recipient, senderFormatString, message));
        recipient.sendMessage(buildMessage(sender, sender, recipientFormatString, message));

        if (recipient instanceof Player p) {
            p.playSound(PING);
        }

        SocialSpy.sendToSpies(sender, recipient, message);
    }

    private static Component buildMessage(CommandSender sender, CommandSender other, String formatString, String message) {
        String prefixFormatString = Messages.getString("chat.format.prefix");
        String suffixFormatString = Messages.getString("chat.format.suffix");
        String pronounsFormatString = Messages.getString("chat.format.pronouns");

        Component prefix = Component.empty();
        Component suffix = Component.empty();
        Component pronouns = Component.empty();
        Component name = Component.text("CONSOLE");

        Component messageComponent = Component.text(message);

        if (other instanceof Player p) {
            Formatter.MessageData messageData = Formatter.getMessageData(p, prefixFormatString, suffixFormatString, pronounsFormatString);
            prefix = messageData.prefix();
            suffix = messageData.suffix();
            pronouns = messageData.pronouns();
            name = messageData.name();
        }

        if (sender instanceof Player p) {
            messageComponent = Formatter.parseChat(p, messageComponent);
        }

        return MiniMessage.miniMessage().deserialize(formatString,
                Placeholder.component("prefix", prefix),
                Placeholder.component("suffix", suffix),
                Placeholder.component("name", name),
                Placeholder.component("pronouns", pronouns),
                Placeholder.component("message", messageComponent));
    }

}

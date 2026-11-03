package onl.jami.averUtils.discord.chatlink;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.chat.Formatter;
import onl.jami.averUtils.storage.database.ChannelLinkRepo;
import org.bukkit.Bukkit;

import java.sql.SQLException;

public class DiscordToMinecraft extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage() || !event.isFromGuild()) {
            return;
        }
        try {
            if (!ChannelLinkRepo.isLinked(event.getGuild().getIdLong(), event.getChannel().getIdLong())) { return; }
            String message = event.getMessage().getContentRaw();
            if (!event.getMessage().getStickers().isEmpty()) {
                message = "[sticker]";
            }
            if (!event.getMessage().getAttachments().isEmpty()) {
                message = message + " [attachments]";
            }
            Component component = Formatter.formatDiscordChat(event.getAuthor().getIdLong(), message);
            if (component == null) { return; }
            Bukkit.broadcast(component);
        } catch (SQLException exception) {
            AverUtils.logWarning("Failed to fetch channel link from database", exception);
        }
    }
}

package onl.jami.averUtils.discord.chatlink;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.chat.ChatFormatter;
import onl.jami.averUtils.chat.PlayerName;
import onl.jami.averUtils.storage.database.ChannelLinkRepo;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.logging.Level;

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
            Component component = ChatFormatter.formatDiscordChat(event.getAuthor().getIdLong(), message);
            if (message == null) { return; }
            Bukkit.broadcast(component);
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to check if channel linked for " + event.getChannel().getName(), e);
        }
    }

    private static Component formatDiscordChat(Long guildId, Long userId, String message) {
        return Component.text("[", NamedTextColor.GRAY)
                .append(Component.text("D", NamedTextColor.BLUE))
                .append(Component.text("] ", NamedTextColor.GRAY))
                .append(PlayerName.getComponent(userId).color(NamedTextColor.WHITE))
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(message, NamedTextColor.WHITE));
    }
}

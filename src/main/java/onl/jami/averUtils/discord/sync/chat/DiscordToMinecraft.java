package onl.jami.averUtils.discord.sync.chat;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.social.chat.Formatter;
import onl.jami.averUtils.storage.cache.ChannelLinkCache;
import onl.jami.averUtils.storage.database.ChannelLinkCategory;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NonNull;

public class DiscordToMinecraft extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event) {
        if (!AverUtils.getCfg().getBoolean("discord.enabled") || event.getAuthor().isBot() || event.isWebhookMessage() || !event.isFromGuild()) {
            return;
        }
        if (!ChannelLinkCache.isLinked(event.getGuild().getIdLong(), event.getChannel().getIdLong(), ChannelLinkCategory.CHAT)) { return; }
        String message = event.getMessage().getContentDisplay();
        if (!event.getMessage().getStickers().isEmpty()) {
            message = "[sticker]";
        }
        if (!event.getMessage().getAttachments().isEmpty()) {
            message = message + " [attachments]";
        }

        Component component = Formatter.formatDiscordChat(event, message, event.getMessage().getReferencedMessage());
        if (component == null) { return; }
        Bukkit.broadcast(component);
    }
}

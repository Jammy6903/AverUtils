package onl.jami.averUtils.discord.chatlink;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.database.ChannelLinkCategory;
import onl.jami.averUtils.storage.database.ChannelLinkRepo;
import onl.jami.averUtils.discord.Bot;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class MinecraftToDiscord {
    private static final HttpClient http = HttpClient.newHttpClient();

    public static void sendChatMessage(AsyncChatEvent event) {
        if (!AverUtils.getCfg().getBoolean("discord.enabled")) {
            return;
        }
        try {
            List<ChannelLinkRepo.ChannelRef> channelRef = ChannelLinkRepo.getChannelsFor(ChannelLinkCategory.CHAT);
            for  (ChannelLinkRepo.ChannelRef channel : channelRef) {
                JsonObject body = new JsonObject();
                body.addProperty("content", PlainTextComponentSerializer.plainText().serialize(event.message()));
                body.addProperty("username", event.getPlayer().getName());
                body.addProperty("avatar_url", "https://mc-heads.net/avatar/" + event.getPlayer().getUniqueId());
                JsonObject mentions = new JsonObject();
                mentions.add("parse", new JsonArray());
                body.add("allowed_mentions", mentions);

                HttpRequest request = HttpRequest.newBuilder(URI.create(channel.webhookUrl()))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                        .build();

                http.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                        .thenAccept(response -> {
                            if (response.statusCode() == 300) {
                                AverUtils.getPlugin().getLogger().log(Level.WARNING, "Failed to post to webhook url");
                            }
                        });
            }
        } catch (SQLException exception) {
            AverUtils.logWarning("Failed to fetch channel links from database", exception);}
    }

    public static void sendJoinMessage(PlayerJoinEvent event) {
        if (!AverUtils.getCfg().getBoolean("discord.enabled")) {
            return;
        }
        try {
            List<ChannelLinkRepo.ChannelRef> channelRef = ChannelLinkRepo.getChannelsFor(ChannelLinkCategory.JOIN);
            for (ChannelLinkRepo.ChannelRef channel : channelRef) {
                TextChannel c = Bot.getJDA().getGuildById(channel.guildId()).getChannelById(TextChannel.class, channel.channelId());
                if  (c == null) {
                    return;
                }
                c.sendMessage(event.getPlayer().getName() + " joined the game").queue();
            }
        } catch (SQLException exception) {
            AverUtils.logWarning("Failed to fetch channel links from database", exception);
        }
    }

    public static void sendPlayerLeaveMessage(PlayerQuitEvent event) {
        if (!AverUtils.getCfg().getBoolean("discord.enabled")) {
            return;
        }
        try {
            List<ChannelLinkRepo.ChannelRef> channelRef = ChannelLinkRepo.getChannelsFor(ChannelLinkCategory.QUIT);
            for (ChannelLinkRepo.ChannelRef channel : channelRef) {
                TextChannel c = Bot.getJDA().getGuildById(channel.guildId()).getChannelById(TextChannel.class, channel.channelId());
                if  (c == null) {
                    return;
                }
                c.sendMessage(event.getPlayer().getName() + " left the game").queue();
            }
        } catch (SQLException exception) {
            AverUtils.logWarning("Failed to fetch channel links from database", exception);
        }
    }

    public static void sendPlayerDeathMessage(PlayerDeathEvent event) {
        if (!AverUtils.getCfg().getBoolean("discord.enabled")) {
            return;
        }
        try {
            List<ChannelLinkRepo.ChannelRef> channelRef = ChannelLinkRepo.getChannelsFor(ChannelLinkCategory.DEATH);
            for (ChannelLinkRepo.ChannelRef channel : channelRef) {
                TextChannel c = Bot.getJDA().getGuildById(channel.guildId()).getChannelById(TextChannel.class, channel.channelId());
                if  (c == null) {
                    return;
                }
                c.sendMessage(PlainTextComponentSerializer.plainText().serialize(event.deathMessage())).queue();
            }
        } catch (SQLException exception) {
            AverUtils.logWarning("Failed to fetch channel links from database", exception);
        }
    }

    public static void sendPlayerAdvancementMessage(PlayerAdvancementDoneEvent event) {
        if (!AverUtils.getCfg().getBoolean("discord.enabled")) {
            return;
        }
        Component message = event.message();
        if (message != null) {
            try {
                List<ChannelLinkRepo.ChannelRef> channelRef = ChannelLinkRepo.getChannelsFor(ChannelLinkCategory.ADVANCEMENT);
                for (ChannelLinkRepo.ChannelRef channel : channelRef) {
                    TextChannel c = Bot.getJDA().getGuildById(channel.guildId()).getChannelById(TextChannel.class, channel.channelId());
                    if (c == null) {
                        return;
                    }
                    c.sendMessage(PlainTextComponentSerializer.plainText().serialize(message)).queue();
                }
            } catch (SQLException exception) {
                AverUtils.logWarning("Failed to fetch channel links from database", exception);
            }
        }
    }
}

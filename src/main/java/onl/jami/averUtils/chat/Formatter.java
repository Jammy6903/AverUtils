package onl.jami.averUtils.chat;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.cacheddata.CachedMetaData;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.userconfig.profile.PlayerName;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class Formatter {
    public record MessageData(Component name, Component prefix, Component suffix, Component pronouns) {}

    public static MessageData getMessageData(Player player, String prefixFormatString, String suffixFormatString, String pronounsFormatString) {
        CachedMetaData md = AverUtils.getLuckPermsApi().getPlayerAdapter(Player.class).getUser(player).getCachedData().getMetaData();
        String lpPrefixString = md.getPrefix();
        String lpSuffixString = md.getSuffix();
        if (lpPrefixString == null) {
            lpPrefixString = "default";
        }
        if (lpSuffixString == null) {
            lpSuffixString = "";
        }
        Component lpPrefix = MiniMessage.miniMessage().deserialize(lpPrefixString);
        Component lpSuffix = MiniMessage.miniMessage().deserialize(lpSuffixString);
        String playerPronouns = PlayerCache.getPlayer(player.getUniqueId()).pronouns();

        Component prefix = MiniMessage.miniMessage().deserialize(prefixFormatString,
                Placeholder.component("prefix", lpPrefix));
        Component suffix = MiniMessage.miniMessage().deserialize(suffixFormatString,
                Placeholder.component("suffix", lpSuffix));
        Component pronouns = Component.empty();
        if (!(playerPronouns == null)) {
            if (!playerPronouns.isEmpty()) {
                pronouns = MiniMessage.miniMessage().deserialize(pronounsFormatString,
                        Placeholder.component("pronouns", Component.text(playerPronouns)));
            }
        }
        return new MessageData(PlayerName.getComponent(player), prefix, suffix, pronouns);
    }

    public static Component formatChat(Player player, Component message) {
        String chatFormatString = Messages.getString("format.chat.message");
        String prefixFormatString = Messages.getString("format.chat.prefix");
        String suffixFormatString = Messages.getString("format.chat.suffix");
        String pronounsFormatString = Messages.getString("format.chat.pronouns");

        MessageData messageData = getMessageData(player, prefixFormatString, suffixFormatString, pronounsFormatString);

        return MiniMessage.miniMessage().deserialize(chatFormatString,
                Placeholder.component("prefix", messageData.prefix),
                Placeholder.component("suffix", messageData.suffix),
                Placeholder.component("name", PlayerName.getComponent(player)),
                Placeholder.component("pronouns", messageData.pronouns),
                Placeholder.component("message", message)
        );
    }

    public static Component formatDiscordChat(MessageReceivedEvent event, String message) {
        String discordFormatString = Messages.getString("chat.format.discord");
        String playerName = event.getAuthor().getName();
        try {
            PlayerRepo.PlayerData playerData = PlayerRepo.getPlayerByDiscordId(event.getAuthor().getIdLong());
            if (playerData == null) {
                return discordNonPlayer(discordFormatString, playerName, message);
            }

            Player p = Bukkit.getOfflinePlayer(playerData.uuid()).getPlayer();
            if (p == null) {
                return discordNonPlayer(discordFormatString, playerName, message);
            }

            MessageData messageData = getMessageData(p, discordFormatString, playerName, message);
            return MiniMessage.miniMessage().deserialize(discordFormatString,
                    Placeholder.component("prefix", messageData.prefix),
                    Placeholder.component("suffix", messageData.suffix),
                    Placeholder.component("name", Component.text(playerName)),
                    Placeholder.component("pronouns", messageData.pronouns),
                    Placeholder.component("message", Component.text(message))
            );
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
        }
        return null;
    }

    private static Component discordNonPlayer(String formatString, String name, String message) {
        return MiniMessage.miniMessage().deserialize(formatString,
                Placeholder.component("prefix", Component.empty()),
                Placeholder.component("suffix", Component.empty()),
                Placeholder.component("name", Component.text(name)),
                Placeholder.component("pronouns", Component.empty()),
                Placeholder.component("message", Component.text(message)));
    }
}

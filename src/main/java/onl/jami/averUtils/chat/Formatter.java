package onl.jami.averUtils.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.cacheddata.CachedMetaData;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.discord.Bot;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.userconfig.profile.PlayerName;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class Formatter {
    public static Component formatChat(Player player, Component message, Component discordPreceder) {
        String chatFormatString = AverUtils.getCfg().getString("chat.format");
        String prefixFormatString = AverUtils.getCfg().getString("chat.prefix-format");
        String suffixFormatString = AverUtils.getCfg().getString("chat.suffix-format");
        String pronounsFormatString = AverUtils.getCfg().getString("chat.pronouns-format");

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
        Component pronouns;
        if (playerPronouns == null || playerPronouns.isEmpty()) {
            pronouns = Component.empty();
        } else {
            pronouns = MiniMessage.miniMessage().deserialize(pronounsFormatString,
                    Placeholder.component("pronouns", Component.text(playerPronouns)));
        }

        return MiniMessage.miniMessage().deserialize(chatFormatString,
                Placeholder.component("discord", discordPreceder),
                Placeholder.component("prefix", prefix),
                Placeholder.component("suffix", suffix),
                Placeholder.component("name", PlayerName.getComponent(player)),
                Placeholder.component("pronouns", pronouns),
                Placeholder.component("message", message));
    }

    public static Component formatDiscordChat(Long userId, String message) {
        String chatFormatString = AverUtils.getCfg().getString("chat.format");
        String discordFormatString = AverUtils.getCfg().getString("chat.discord-format");
        String playerName = Bot.getJDA().getUserById(userId).getGlobalName();
        if (playerName == null) { return null; }
        Component discordPreceder = MiniMessage.miniMessage().deserialize(discordFormatString)
                .hoverEvent(HoverEvent.showText(Component.text("This message originated from Discord")));
        try {
            PlayerRepo.PlayerData playerData = PlayerRepo.getPlayerByDiscordId(userId);
            if (playerData == null) { return discordNonPlayer(chatFormatString, discordPreceder, playerName, message); }
            Player p = Bukkit.getOfflinePlayer(playerData.uuid()).getPlayer();
            if (p == null) { return discordNonPlayer(chatFormatString, discordPreceder, playerName, message); }
            return (formatChat(p, Component.text(message), discordPreceder));
        } catch (SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to fetch player " + exception);
        }
        return null;
    }

    private static Component discordNonPlayer(String formatString, Component preceder, String name, String message) {
        return MiniMessage.miniMessage().deserialize(formatString,
                Placeholder.component("discord", preceder),
                Placeholder.component("prefix", Component.empty()),
                Placeholder.component("suffix", Component.empty()),
                Placeholder.component("name", Component.text(name)),
                Placeholder.component("pronouns", Component.empty()),
                Placeholder.component("message", Component.text(message)));
    }
}

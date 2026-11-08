package onl.jami.averUtils.userconfig.profile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.cacheddata.CachedMetaData;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.cache.PlayerCache;
import org.bukkit.entity.Player;

public class PlayerName {

    public static Component getComponent(Player player, boolean styled) {
        PlayerRepo.PlayerData playerData = PlayerCache.getPlayer(player);
        if (playerData == null) {
            return Component.text(player.getName());
        }

        String playerName = switch (playerData.nameChoice()) {
            case null -> player.getName();
            case PlayerRepo.PlayerData.NameChoice.PLAYER_NAME -> player.getName();
            case PlayerRepo.PlayerData.NameChoice.DISCORD_GLOBAL -> playerData.discordGlobalName() != null ? playerData.discordGlobalName() : player.getName();
            case PlayerRepo.PlayerData.NameChoice.DISCORD_NICKNAME -> playerData.discordUsername() != null ? playerData.discordUsername() : player.getName();
            case PlayerRepo.PlayerData.NameChoice.NICKNAME -> playerData.nickname() != null ? playerData.nickname() : player.getName();
        };

        String nameStyle = playerData.nameStyle();
        String styleString = playerName;
        if (nameStyle != null && !nameStyle.isEmpty() && player.hasPermission("averutils.namestyle")) {
            for (String style : playerData.nameStyle().split(" ")) {
                NameStyle.Wrap tags = NameStyle.parse(player, style);
                if (tags != null) {
                    styleString = tags.open() + styleString + tags.close();
                }
            }
        }
        Component styledName = MiniMessage.miniMessage().deserialize(styleString);

        String nick = playerData.nickname() == null ? "n/a" : playerData.nickname();
        String discordUser = playerData.discordUsername() == null ? "n/a" : playerData.discordUsername();
        String discordGlobal = playerData.discordGlobalName() == null ? "n/a" : playerData.discordGlobalName();
        String discordNick = playerData.discordNickname() == null ? "n/a" : playerData.discordNickname();

        String hoverFormat = Messages.getAll("player.profile.name-hover");

        Component hover = MiniMessage.miniMessage().deserialize(hoverFormat,
                Placeholder.component("name", styledName),
                Placeholder.component("ign", Component.text(player.getName())),
                Placeholder.component("nick", Component.text(nick)),
                Placeholder.component("discord-user", Component.text(discordUser)),
                Placeholder.component("discord-global", Component.text(discordGlobal)),
                Placeholder.component("discord-nick", Component.text(discordNick))
        );

        if (!styled) {
            return Component.text(playerName)
                    .hoverEvent(HoverEvent.showText(hover));
        }

        return styledName
                .hoverEvent(HoverEvent.showText(hover))
                .clickEvent(ClickEvent.suggestCommand("/msg " + player.getName() + " "));
    }

    public static void updatePlayerList(Player player) {
        PlayerRepo.PlayerData playerData = PlayerCache.getPlayer(player);
        if (playerData == null) { return; }

        String formatString = Messages.getString("player.list.format.player");
        CachedMetaData md = AverUtils.getLuckPermsApi().getPlayerAdapter(Player.class).getUser(player).getCachedData().getMetaData();
        String lpPrefixString = md.getPrefix();
        String lpSuffixString = md.getSuffix();
        if (lpPrefixString == null) {
            lpPrefixString = "default";
        }
        if (lpSuffixString == null) {
            lpSuffixString = "";
        }
        String playerPronouns = playerData.pronouns();
        Component pronouns = Component.empty();
        if (!(playerPronouns == null)) {
            if (!playerPronouns.isEmpty()) {
                pronouns = Component.text(playerPronouns);
            }
        }
        Component component = MiniMessage.miniMessage().deserialize(formatString,
                Placeholder.component("prefix", MiniMessage.miniMessage().deserialize(lpPrefixString)),
                Placeholder.component("suffix", MiniMessage.miniMessage().deserialize(lpSuffixString)),
                Placeholder.component("name", getComponent(player, false)),
                Placeholder.component("pronouns", pronouns)
        );
        player.playerListName(component);
    }
}

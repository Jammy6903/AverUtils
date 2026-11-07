package onl.jami.averUtils.userconfig.profile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.cacheddata.CachedMetaData;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.cache.PlayerCache;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class PlayerName {
    public static Component getComponent(Player player) {
        PlayerRepo.PlayerData playerData = PlayerCache.getPlayer(player);
        if (playerData == null) { return null; }

        String playerName = switch (playerData.nameChoice()) {
            case null -> player.getName();
            case PlayerRepo.PlayerData.NameChoice.PLAYER_NAME -> player.getName();
            case PlayerRepo.PlayerData.NameChoice.DISCORD_GLOBAL -> playerData.discordGlobalName() != null ? playerData.discordGlobalName() : player.getName();
            case PlayerRepo.PlayerData.NameChoice.DISCORD_NICKNAME -> playerData.discordUsername() != null ? playerData.discordUsername() :  player.getName();
            case PlayerRepo.PlayerData.NameChoice.NICKNAME -> playerData.nickname() != null ? playerData.nickname() : player.getName();
        };

        String discordUsername = playerData.discordUsername();
        String discordGlobalName = playerData.discordGlobalName();
        String discordNickname = playerData.nickname();

        Component nameHoverComponent = Component.text(playerName + "'s Profile", NamedTextColor.AQUA).decorate(TextDecoration.UNDERLINED);
        nameHoverComponent = nameHoverComponent.append(Component.text("\nPlayer Name: " + player.getName()));
        if (discordUsername != null) { nameHoverComponent = nameHoverComponent.append(Component.text("\nDiscord Username: " + playerData.discordUsername())); }
        if (discordGlobalName != null) { nameHoverComponent = nameHoverComponent.append(Component.text("\nDiscord Global Name: " + playerData.discordGlobalName())); }
        if (discordNickname != null) { nameHoverComponent = nameHoverComponent.append(Component.text("\nDiscord nickname: " + playerData.discordNickname())); }
        nameHoverComponent = nameHoverComponent.append(Component.text("\n\nClick to private message!"));

        return Component.text(playerName).color(NamedTextColor.WHITE)
                .hoverEvent(HoverEvent.showText(nameHoverComponent))
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
                Placeholder.component("name", getComponent(player)),
                Placeholder.component("pronouns", pronouns)
        );
        player.playerListName(component);
    }
}

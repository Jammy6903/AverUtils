package onl.jami.averUtils.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.social.chat.Formatter;
import onl.jami.averUtils.discord.sync.chat.MinecraftToDiscord;
import onl.jami.averUtils.staff.moderation.Punish;
import onl.jami.averUtils.storage.cache.BlockCache;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.cache.PunishmentCache;
import onl.jami.averUtils.storage.database.*;
import onl.jami.averUtils.util.TimeUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener implements Listener {

    private static final Pattern MENTION = Pattern.compile("(?<!\\w)@(\\w{1,16})");
    private static final Sound PING = Sound.sound(
            Key.key("entity.experience_orb.pickup"),
            Sound.Source.PLAYER,
            1.0f, 1.5f
    );

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        for (PunishmentRepo.PunishmentData p : PunishmentCache.getCachedPunishmentsWithActiveCheck(e.getPlayer().getUniqueId())) {
            if (p.type().equals(PunishmentType.TIMEOUT)) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(Punish.getComponent("moderation.punishments.strings.timeout.on-action",
                        Placeholder.component("reason", Component.text(p.reason())),
                        Placeholder.component("remaining", Component.text(TimeUtils.formatRemainingTimeUntil(p.durationEnd())))
                ));
                return;
            }
        }

        if (AverUtils.getCfg().getBoolean("discord.enabled") && AverUtils.getCfg().getBoolean("discord.require-verification")) {
            PlayerRepo.PlayerData playerData = PlayerCache.getPlayer(e.getPlayer());
            if (playerData == null || !playerData.isVerified()) {
                e.setCancelled(true);
                return;
            }
        }

        // send message to linked discord channels
        MinecraftToDiscord.sendChatMessage(e);

        // format chat message
        Component formattedMessage = Formatter.formatChat(e.getPlayer(), e.message());
        e.message(formattedMessage);
        if (!e.getPlayer().hasPermission("averutils.bypass-block")) {
            e.viewers().removeIf(audience -> {
                if (audience instanceof Player p) {
                    BlockRepo.BlockData block = BlockCache.getBlockForPlayers(p.getUniqueId(), e.getPlayer().getUniqueId());
                    return block != null && block.categories().contains(BlockCategory.CHAT);
                }
                return false;
            });
        }

        Map<String, Player> mentioned;

        Player sender = e.getPlayer();
        if (sender.hasPermission("averutils.ping")) {
            mentioned = findMentions(PlainTextComponentSerializer.plainText().serialize(e.message()), e.viewers());
            for (Player p : mentioned.values()) {
                if (p.equals(e.getPlayer())) { continue; }
                p.playSound(PING);
            }
            // TODO: make this show players namechoice rather than ign
            e.message(e.message().replaceText(config -> config.match(MENTION)
                    .replacement((result, builder) -> {
                        String name = result.group(1).toLowerCase(Locale.ROOT);
                        if (!mentioned.containsKey(name)) { return null; }
                        return builder.color(NamedTextColor.YELLOW);
                    })));
        }

        e.renderer(((_, _, message, _) -> message));
    }

    private static Map<String, Player> findMentions(String text, Collection<? extends Audience> viewers) {
        Map<String, Player> byName = new HashMap<>();
        for (Audience audience : viewers) {
            if (audience instanceof Player p) {
                byName.put(p.getName().toLowerCase(Locale.ROOT), p);
            }
        }

        Map<String, Player> mentioned = new HashMap<>();
        Matcher matcher = MENTION.matcher(text);
        while (matcher.find()) {
            String key = matcher.group(1).toLowerCase(Locale.ROOT);
            Player p = byName.get(key);
            if (p == null) { continue; }
            PlayerRepo.PlayerData playerData = PlayerCache.getPlayer(p);
            if (playerData != null && playerData.pingEnabled()) { mentioned.put(key, p); }
        }

        return mentioned;
    }
}

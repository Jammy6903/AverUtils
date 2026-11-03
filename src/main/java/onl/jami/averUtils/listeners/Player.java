package onl.jami.averUtils.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.admin.PlayerListDecoration;
import onl.jami.averUtils.discord.chatlink.MinecraftToDiscord;
import onl.jami.averUtils.discord.sync.RoleSync;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.database.PlayerListDecorationRepo;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.discord.verification.VerifyLock;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Player implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        org.bukkit.entity.Player player = e.getPlayer();
        try {
            UUID uuid = player.getUniqueId();

            PlayerRepo.createPlayer(uuid);
            VerifyLock.verifyOnLogin(e.getPlayer());
            PlayerCache.addPlayer(uuid, PlayerRepo.getPlayer(uuid));

            // cache this, also simplify it
            List<PlayerListDecorationRepo.PlayerListDecorationData> headers = PlayerListDecorationRepo.getActive(PlayerListDecorationRepo.PlayerListDecorationData.Location.HEADER);
            List<PlayerListDecorationRepo.PlayerListDecorationData> footers = PlayerListDecorationRepo.getActive(PlayerListDecorationRepo.PlayerListDecorationData.Location.FOOTER);
            if (!headers.isEmpty()) {
                if (headers.size() == 1) {
                    player.sendPlayerListHeader(MiniMessage.miniMessage().deserialize(headers.getFirst().message()));
                } else {
                    int random = ThreadLocalRandom.current().nextInt(0, headers.size());
                    player.sendPlayerListHeader(MiniMessage.miniMessage().deserialize(headers.get(random).message()));
                }
            }
            if (!footers.isEmpty()) {
                if (footers.size() == 1) {
                    player.sendPlayerListFooter(MiniMessage.miniMessage().deserialize(footers.getFirst().message()));
                } else {
                    int random = ThreadLocalRandom.current().nextInt(0, headers.size());
                    player.sendPlayerListFooter(MiniMessage.miniMessage().deserialize(footers.get(random).message()));
                }
            }

            RoleSync.sync(player);
            MinecraftToDiscord.sendJoinMessage(e);
        } catch (SQLException exception) {
            AverUtils.logWarning("Failed to access database on player join", exception);
            player.kick(Component.text("An unknown error has occurred."));
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        MinecraftToDiscord.sendPlayerLeaveMessage(e);
        PlayerCache.removePlayer(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        MinecraftToDiscord.sendPlayerDeathMessage(e);
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent e) {
        MinecraftToDiscord.sendPlayerAdvancementMessage(e);
    }
}

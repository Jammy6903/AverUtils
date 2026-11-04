package onl.jami.averUtils.listeners;

import net.kyori.adventure.text.Component;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.discord.chatlink.MinecraftToDiscord;
import onl.jami.averUtils.discord.sync.RoleSync;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.discord.verification.VerifyLock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.UUID;

public class Player implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        try {
            UUID uuid = e.getPlayer().getUniqueId();

            // create player if not exists
            PlayerRepo.createPlayer(uuid);

            // verify player if required, sync roles with discord and send join message to any linked channels
            VerifyLock.verifyOnLogin(e.getPlayer());
            RoleSync.sync(e.getPlayer());
            MinecraftToDiscord.sendJoinMessage(e);

            // cache player
            PlayerCache.addPlayer(uuid, PlayerRepo.getPlayer(uuid));
        } catch (SQLException exception) {
            AverUtils.logWarning("Failed to create player in database", exception);
            e.getPlayer().kick(Component.text("An unknown error has occurred."));
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        // send leave message to any linked discord channels
        MinecraftToDiscord.sendPlayerLeaveMessage(e);

        // remove player from cache
        PlayerCache.removePlayer(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        // send death message to any linked discord channels
        MinecraftToDiscord.sendPlayerDeathMessage(e);
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent e) {
        // send advancement message to any linked discord channels
        MinecraftToDiscord.sendPlayerAdvancementMessage(e);
    }
}

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
            PlayerRepo.createPlayer(uuid);
            VerifyLock.verifyOnLogin(e.getPlayer());
            PlayerCache.addPlayer(uuid, PlayerRepo.getPlayer(uuid));
            RoleSync.sync(e.getPlayer());
            MinecraftToDiscord.sendJoinMessage(e);
        } catch (SQLException exception) {
            AverUtils.logWarning("Failed to create player in database", exception);
            e.getPlayer().kick(Component.text("An unknown error has occurred."));
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

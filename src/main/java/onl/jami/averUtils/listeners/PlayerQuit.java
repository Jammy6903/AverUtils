package onl.jami.averUtils.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.discord.chatlink.MinecraftToDiscord;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.managers.PingManager;
import onl.jami.averUtils.managers.PlayerName;
import onl.jami.averUtils.managers.VanishManager;
import onl.jami.averUtils.storage.cache.BlockCache;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.cache.PunishmentCache;
import onl.jami.averUtils.storage.database.PlayerRepo;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class PlayerQuit implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        PlayerRepo.PlayerData playerData = PlayerCache.loadPlayer(e.getPlayer());

        sendQuitMessages(playerData, e);

        // send leave message to any linked discord channels
        MinecraftToDiscord.sendPlayerLeaveMessage(e.quitMessage());

        PingManager.updatePingCompletions(player, false);

        // log location on leave, for tpo
        try {
            Location l = e.getPlayer().getLocation();
            PlayerRepo.updateLastLocation(player.getUniqueId(), player.getWorld().getUID(), l.getX(), l.getY(), l.getZ());
        } catch (SQLException ex) {
            AverUtils.logWarning(Locale.getString("errors.database.players"), ex);
        }

        VanishManager.onLeave(player);

        // remove player from cache
        PlayerCache.removePlayer(e.getPlayer().getUniqueId());
        BlockCache.removeBlocksForPlayer(e.getPlayer().getUniqueId());
        PunishmentCache.removePlayer(e.getPlayer().getUniqueId());
    }

    private static void sendQuitMessages(PlayerRepo.PlayerData playerData, PlayerQuitEvent e) {
        e.quitMessage(null);
        if (playerData != null && playerData.isVerified() && !VanishManager.hasVanishedState(e.getPlayer())) {
            Component joinMessage = Locale.getRandom("player.quit.messages",
                    Placeholder.component("name", PlayerName.getComponent(e.getPlayer(), false)));

            e.quitMessage(Locale.getComponent("player.quit.format.message",
                    Placeholder.component("message", joinMessage)));
        }
    }

}

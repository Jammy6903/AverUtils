package onl.jami.averUtils.listeners;

import net.kyori.adventure.text.Component;
import onl.jami.averUtils.chat.PlayerName;
import onl.jami.averUtils.discord.sync.RoleSync;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.discord.verification.VerifyLock;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        try {
            UUID uuid = e.getPlayer().getUniqueId();

            PlayerRepo.createPlayer(uuid);
            VerifyLock.verifyOnLogin(e.getPlayer());
            PlayerCache.addPlayer(uuid, PlayerRepo.getPlayer(uuid));

            PlayerName.updatePlayerList(e.getPlayer());

            RoleSync.sync(e.getPlayer());
        } catch (SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Could not create player " + e.getPlayer().getName(), exception);
            e.getPlayer().kick(Component.text("An unknown error has occurred."));
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        PlayerCache.removePlayer(e.getPlayer().getUniqueId());
    }
}

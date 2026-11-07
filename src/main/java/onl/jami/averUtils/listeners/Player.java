package onl.jami.averUtils.listeners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.discord.chatlink.MinecraftToDiscord;
import onl.jami.averUtils.discord.sync.RoleSync;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.cache.PlayerListDecorationCache;
import onl.jami.averUtils.storage.database.PlayerListDecorationRepo;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.discord.verification.VerifyLock;
import onl.jami.averUtils.userconfig.profile.PlayerName;
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
            // create player if not exists
            PlayerRepo.createPlayer(player.getUniqueId());

        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
            player.kick(MiniMessage.miniMessage().deserialize(Messages.getString(Messages.getString("errors.generic"))));
            return;
        }

        // cache player
        PlayerCache.updatePlayer(player.getUniqueId());

        // verify player if required, sync roles with discord and send join message to any linked channels
        VerifyLock.verifyOnLogin(player);
        RoleSync.sync(player);
        MinecraftToDiscord.sendJoinMessage(e);

        PlayerName.updatePlayerList(player);

        player.sendPlayerListHeader(MiniMessage.miniMessage().deserialize(PlayerListDecorationCache.getRandom(PlayerListDecorationRepo.PlayerListDecorationData.Location.HEADER).message()));
        player.sendPlayerListFooter(MiniMessage.miniMessage().deserialize(PlayerListDecorationCache.getRandom(PlayerListDecorationRepo.PlayerListDecorationData.Location.FOOTER).message()));
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

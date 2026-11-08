package onl.jami.averUtils.listeners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.discord.sync.chat.MinecraftToDiscord;
import onl.jami.averUtils.discord.sync.roles.RoleSync;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.cache.BlockCache;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.cache.PlayerListDecorationCache;
import onl.jami.averUtils.storage.database.BlockCategory;
import onl.jami.averUtils.storage.database.BlockRepo;
import onl.jami.averUtils.storage.database.PlayerListDecorationRepo;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.discord.verification.VerifyLock;
import onl.jami.averUtils.userconfig.PlayerName;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        org.bukkit.entity.Player player = e.getPlayer();
        try {
            // create player if not exists
            PlayerRepo.createPlayer(player.getUniqueId());

        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
            player.kick(MiniMessage.miniMessage().deserialize(Messages.getString("errors.generic")));
            return;
        }

        // cache player
        PlayerCache.loadPlayer(player);
        BlockCache.loadBlocksForPlayer(player.getUniqueId());

        // verify player if required, sync roles with discord and send join message to any linked channels
        VerifyLock.verifyOnLogin(player);
        RoleSync.sync(player);
        MinecraftToDiscord.sendJoinMessage(e);

        PlayerName.updatePlayerList(player);

        String playerListHeader = PlayerListDecorationCache.getRandom(PlayerListDecorationRepo.PlayerListDecorationData.Location.HEADER);
        if (playerListHeader != null) {
            player.sendPlayerListHeader(MiniMessage.miniMessage().deserialize(playerListHeader));
        }
        String playerListFooter = PlayerListDecorationCache.getRandom(PlayerListDecorationRepo.PlayerListDecorationData.Location.FOOTER);
        if (playerListFooter != null) {
            player.sendPlayerListFooter(MiniMessage.miniMessage().deserialize(playerListFooter));
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        // send leave message to any linked discord channels
        MinecraftToDiscord.sendPlayerLeaveMessage(e);

        // remove player from cache
        PlayerCache.removePlayer(e.getPlayer().getUniqueId());
        BlockCache.removeBlocksForPlayer(e.getPlayer().getUniqueId());
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

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) { return; }
        Player attacker = resolveAttacker(e.getDamager());
        if (attacker == null || attacker.equals(victim)) { return; }

        PlayerRepo.PlayerData victimData = PlayerCache.getPlayer(victim);
        PlayerRepo.PlayerData attackerData = PlayerCache.getPlayer(attacker);
        BlockRepo.BlockData block = BlockCache.getBlockForPlayers(victim.getUniqueId(), attacker.getUniqueId());
        if (victimData == null ||
                attackerData == null ||
                (
                        victimData.pvpEnabled() &&
                        attackerData.pvpEnabled() &&
                        !(block != null && block.categories().contains(BlockCategory.PVP)) ||
                        attacker.hasPermission("averutils.bypass-block")
                )
        ) { return; }
        e.setCancelled(true);
        Messages.send(attacker, "player.profile.pvp",
                Placeholder.component("victim", PlayerName.getComponent(victim, false))
        );
    }

    private static Player resolveAttacker(Entity damager) {
        if (damager instanceof Player p) { return p; }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player p) { return p; }
        if (damager instanceof TNTPrimed tnt && tnt.getSource() instanceof Player p) { return p; }
        return null;
    }
}

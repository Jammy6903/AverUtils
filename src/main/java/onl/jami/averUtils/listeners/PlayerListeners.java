package onl.jami.averUtils.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.discord.sync.chat.MinecraftToDiscord;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.social.messaging.Message;
import onl.jami.averUtils.storage.cache.BlockCache;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.cache.PlayerListDecorationCache;
import onl.jami.averUtils.storage.database.BlockCategory;
import onl.jami.averUtils.storage.database.BlockRepo;
import onl.jami.averUtils.storage.database.PlayerListDecorationRepo;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.discord.verification.VerifyLock;
import onl.jami.averUtils.userconfig.PlayerName;
import org.bukkit.Bukkit;
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
import java.util.List;

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
        PlayerRepo.PlayerData playerData = PlayerCache.loadPlayer(player);
        BlockCache.loadBlocksForPlayer(player.getUniqueId());

        e.joinMessage(null);

        if (playerData != null && playerData.isVerified()) {
            Component joinMessage = Messages.getRandom("player.join.messages",
                    Placeholder.component("name", PlayerName.getComponent(e.getPlayer(), false)));

            e.joinMessage(Messages.getComponent("player.join.format.message",
                    Placeholder.component("message", joinMessage)));
        }

        // verify player if required, sync roles with discord and send join message to any linked channels
        VerifyLock.verifyOnLogin(player);
        MinecraftToDiscord.sendJoinMessage(e.joinMessage());

        PlayerName.updatePlayerList(player);

        String playerListHeader = PlayerListDecorationCache.getRandom(PlayerListDecorationRepo.PlayerListDecorationData.Location.HEADER);
        if (playerListHeader != null) {
            player.sendPlayerListHeader(MiniMessage.miniMessage().deserialize(playerListHeader));
        }
        String playerListFooter = PlayerListDecorationCache.getRandom(PlayerListDecorationRepo.PlayerListDecorationData.Location.FOOTER);
        if (playerListFooter != null) {
            player.sendPlayerListFooter(MiniMessage.miniMessage().deserialize(playerListFooter));
        }

        String tag = "@" + player.getName();
        List<String> everyone = Bukkit.getOnlinePlayers().stream()
                .map(p -> "@" + p.getName()).toList();

        if (player.hasPermission("averutils.ping")) {
            player.setCustomChatCompletions(everyone);
        }

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player) && other.hasPermission("averutils.ping")) {
                other.addCustomChatCompletions(List.of(tag));
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        e.quitMessage(null);

        PlayerRepo.PlayerData playerData = PlayerCache.loadPlayer(e.getPlayer());

        if (playerData != null && playerData.isVerified()) {
            Component joinMessage = Messages.getRandom("player.quit.messages",
                    Placeholder.component("name", PlayerName.getComponent(e.getPlayer(), false)));

            e.quitMessage(Messages.getComponent("player.quit.format.message",
                    Placeholder.component("message", joinMessage)));
        }

        // send leave message to any linked discord channels
        MinecraftToDiscord.sendPlayerLeaveMessage(e.quitMessage());

        Player player = e.getPlayer();
        String tag = "@" + player.getName();
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player) && other.hasPermission("averutils.ping")) {
                other.removeCustomChatCompletions(List.of(tag));
            }
        }

        // remove player from cache
        PlayerCache.removePlayer(e.getPlayer().getUniqueId());
        BlockCache.removeBlocksForPlayer(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        /*
        Component joinMessage = Messages.getRandom("player.death.messages",
                Placeholder.component("name", PlayerName.getComponent(e.getPlayer(), false)));

        e.deathMessage(Messages.getComponent("player.death.format.message",
                Placeholder.component("message", joinMessage)));
         */

        // send death message to any linked discord channels
        MinecraftToDiscord.sendPlayerDeathMessage(e.deathMessage());
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

package onl.jami.averUtils.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.discord.sync.chat.MinecraftToDiscord;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.staff.VanishManager;
import onl.jami.averUtils.staff.admin.PlayerListDecoration;
import onl.jami.averUtils.staff.moderation.commands.Punish;
import onl.jami.averUtils.staff.moderation.commands.Reports;
import onl.jami.averUtils.staff.moderation.commands.Vanish;
import onl.jami.averUtils.storage.cache.BlockCache;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.cache.PlayerListDecorationCache;
import onl.jami.averUtils.storage.cache.PunishmentCache;
import onl.jami.averUtils.storage.database.*;
import onl.jami.averUtils.discord.verification.VerifyLock;
import onl.jami.averUtils.userconfig.PlayerName;
import onl.jami.averUtils.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;

import java.sql.SQLException;
import java.util.List;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        try {
            long now = System.currentTimeMillis();
            PunishmentRepo.PunishmentData ban = null;

            for (PunishmentRepo.PunishmentData p : PunishmentRepo.getByUUID(event.getUniqueId())) {
                boolean isBan = p.type() == PunishmentType.BAN || p.type() == PunishmentType.TEMP_BAN;
                boolean isActive = !p.isExpired() && (p.durationEnd() == null || p.durationEnd() > now);
                if (isBan && isActive) {
                    ban = p;
                } else if (isBan && p.durationEnd() < now && !p.isExpired()) {
                    // set punishment to expired if time has elapsed
                    PunishmentRepo.setExpired(p.id(), true);
                }
            }

            if (ban != null) {
                String key = ban.type() == PunishmentType.BAN ? "moderation.punishments.strings.ban" : "moderation.punishments.strings.temp-ban";
                Component duration = Component.empty();
                if (ban.duration() != null) {
                    duration = Component.text(ban.duration());
                }
                Component remaining = Component.empty();
                if (ban.durationEnd() != null) {
                    remaining = Component.text(TimeUtils.formatRemainingTimeUntil(ban.durationEnd()));
                }
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        Punish.getComponent(key,
                                Placeholder.component("reason", Component.text(ban.reason())),
                                Placeholder.component("duration", duration),
                                Placeholder.component("remaining", remaining),
                                Placeholder.component("id", Component.text(ban.id()))));
            }
        } catch (SQLException e) {
            AverUtils.logWarning(Messages.getString("errors.database.punishments"), e);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messages.getComponent("errors.generic"));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
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

        sendJoinMessages(playerData, e);

        // verify player if required and send join message to any linked channels
        VerifyLock.verifyOnLogin(player);
        MinecraftToDiscord.sendJoinMessage(e.joinMessage());

        // update ping completions for players
        updatePingCompletions(player, true);

        // update player list
        PlayerName.updatePlayerList(player);
        PlayerListDecoration.loadActiveDecorations(player);

        // join notifications
        Punish.sendWarnOnJoin(player);
        Reports.sendReportsOnJoin(player);;

        // moderation checks
        VanishManager.onJoin(player);
    }

    public static void sendJoinMessages(PlayerRepo.PlayerData playerData, PlayerJoinEvent e) {
        e.joinMessage(null);
        if (playerData != null && playerData.isVerified() && !Vanish.isVanished(e.getPlayer())) {
            Component joinMessage = Messages.getRandom("player.join.messages",
                    Placeholder.component("name", PlayerName.getComponent(e.getPlayer(), false)));

            e.joinMessage(Messages.getComponent("player.join.format.message",
                    Placeholder.component("message", joinMessage)));
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        PlayerRepo.PlayerData playerData = PlayerCache.loadPlayer(e.getPlayer());

        sendQuitMessages(playerData, e);

        // send leave message to any linked discord channels
        MinecraftToDiscord.sendPlayerLeaveMessage(e.quitMessage());

        updatePingCompletions(player, false);

        // log location on leave, for tpo
        try {
            Location l = e.getPlayer().getLocation();
            PlayerRepo.updateLastLocation(player.getUniqueId(), player.getWorld().getUID(), l.getX(), l.getY(), l.getZ());
        } catch (SQLException ex) {
            AverUtils.logWarning(Messages.getString("errors.database.players"), ex);
        }

        VanishManager.onLeave(player);

        // remove player from cache
        PlayerCache.removePlayer(e.getPlayer().getUniqueId());
        BlockCache.removeBlocksForPlayer(e.getPlayer().getUniqueId());
        PunishmentCache.removePlayer(e.getPlayer().getUniqueId());
    }

    private static void sendQuitMessages(PlayerRepo.PlayerData playerData, PlayerQuitEvent e) {
        e.quitMessage(null);
        if (playerData != null && playerData.isVerified() && !Vanish.isVanished(e.getPlayer())) {
            Component joinMessage = Messages.getRandom("player.quit.messages",
                    Placeholder.component("name", PlayerName.getComponent(e.getPlayer(), false)));

            e.quitMessage(Messages.getComponent("player.quit.format.message",
                    Placeholder.component("message", joinMessage)));
        }
    }

    private static void updatePingCompletions(Player player, boolean joining) {
        String tag = "@" + player.getName();
        if (joining) {
            List<String> everyone = Bukkit.getOnlinePlayers().stream()
                    .map(p -> "@" + p.getName()).toList();

            if (player.hasPermission("averutils.ping")) {
                player.setCustomChatCompletions(everyone);
            }
            if (!Vanish.isVanished(player)) {
                for (Player other : Bukkit.getOnlinePlayers()) {
                    if (!other.equals(player) && other.hasPermission("averutils.ping")) {
                        other.addCustomChatCompletions(List.of(tag));
                    }
                }
            }
        } else {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(player) && other.hasPermission("averutils.ping")) {
                    other.removeCustomChatCompletions(List.of(tag));
                }
            }
        }
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

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        // prevent open animation and noise for vanished players
        if (VanishManager.isVanished(player)) {
            if (e.getHand() != EquipmentSlot.HAND) { return; }
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
            Block block = e.getClickedBlock();
            if (block == null) { return; }
            if (block.getState() instanceof Container container) {
                e.setCancelled(true);
                player.openInventory(container.getInventory());
            }
        }
    }
}

package onl.jami.averUtils.listeners;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.managers.PlayerName;
import onl.jami.averUtils.storage.cache.BlockCache;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.database.BlockCategory;
import onl.jami.averUtils.storage.database.BlockRepo;
import onl.jami.averUtils.storage.database.PlayerRepo;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamage implements Listener {

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
        Locale.send(attacker, "player.profile.pvp",
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

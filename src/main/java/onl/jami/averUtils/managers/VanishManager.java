package onl.jami.averUtils.managers;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.AverUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VanishManager {
    private static final Set<UUID> VANISHED = ConcurrentHashMap.newKeySet();
    private static final NamespacedKey VANISHED_STATE = new NamespacedKey(AverUtils.getPlugin(), "vanished");

    private static final BossBar INDICATOR = BossBar.bossBar(Component.text("YOU ARE VANISHED", NamedTextColor.RED), 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.NOTCHED_20);
    private static final PotionEffect NIGHT_VISION = new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 0, false, false, false);

    public static boolean hasVanishedState(Player p) {
        Boolean state = p.getPersistentDataContainer().get(VANISHED_STATE, PersistentDataType.BOOLEAN);
        return state != null && state;
    }

    public static void setVanishedState(Player p, boolean state) {
        p.getPersistentDataContainer().set(VANISHED_STATE, PersistentDataType.BOOLEAN, state);
    }

    public static boolean isVanished(Player player) {
        return VANISHED.contains(player.getUniqueId());
    }

    public static boolean isVanished(UUID uuid) {
        return VANISHED.contains(uuid);
    }

    public static int vanishedCount() {
        return VANISHED.size();
    }

    public static boolean canSee(Player viewer, Player vanisher) {
        if (!isVanished(vanisher)) { return true; }
        return viewer.hasPermission("averutils.moderation.vanish") || isVanished(viewer);
    }

    public static void refresh(Player viewer, Player vanisher) {
        if (viewer.equals(vanisher)) { return; }
        if (canSee(viewer, vanisher)) {
            viewer.showPlayer(AverUtils.getPlugin(), vanisher);
            viewer.listPlayer(vanisher);
            // viewer.playerListName( current player list + [v] );
        } else {
            if (isVanished(vanisher)) {
                viewer.hidePlayer(AverUtils.getPlugin(), vanisher);
                viewer.removeCustomChatCompletions(List.of("@" + vanisher.getName()));
                viewer.unlistPlayer(vanisher);
            }
        }
    }

    public static void enable(Player p) {
        VANISHED.add(p.getUniqueId());
        p.setCollidable(false);
        p.setSilent(true);
        p.setCanPickupItems(false);
        p.setAffectsSpawning(false);
        p.setSleepingIgnored(true);
        p.setInvulnerable(true);
        p.setGlowing(true); // for staff to distinguish vanished players
        p.addPotionEffect(NIGHT_VISION);
        p.showBossBar(INDICATOR);
        syncVisibility(p);
        PlayerName.updatePlayerList(p);
    }

    public static void disable(Player p) {
        VANISHED.remove(p.getUniqueId());
        p.setCollidable(true);
        p.setSilent(false);
        p.setCanPickupItems(true);
        p.setAffectsSpawning(true);
        p.setSleepingIgnored(false);
        p.setInvulnerable(false);
        p.setGlowing(false); // for staff to distinguish vanished players
        p.removePotionEffect(PotionEffectType.NIGHT_VISION);
        p.hideBossBar(INDICATOR);
        syncVisibility(p);
        PlayerName.updatePlayerList(p);
    }

    public static void syncVisibility(Player player) {
        for (Player other :  Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) { continue; }
            refresh(other, player);
            refresh(player, other);
        }
    }

    public static void onJoin(Player player) {
        if (VanishManager.hasVanishedState(player)) {
            enable(player);
        } else {
            syncVisibility(player);
        }
    }

    public static void onLeave(Player player) {
        VANISHED.remove(player.getUniqueId());
    }

    public static void shutdown() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            for (Player vanisher : Bukkit.getOnlinePlayers()) {
                if (viewer.equals(vanisher)) { continue; }
                viewer.showPlayer(AverUtils.getPlugin(), vanisher);
                viewer.listPlayer(vanisher);
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isVanished(p)) {
                p.setCollidable(true);
                p.setSilent(false);
                p.setCanPickupItems(true);
                p.setAffectsSpawning(true);
                p.setSleepingIgnored(false);
                p.setInvulnerable(false);
                p.setGlowing(false); // for staff to distinguish vanished players
                p.removePotionEffect(PotionEffectType.NIGHT_VISION);
                p.hideBossBar(INDICATOR);
            }
        }
        VANISHED.clear();
    }
}

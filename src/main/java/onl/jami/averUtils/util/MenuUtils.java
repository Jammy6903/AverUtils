package onl.jami.averUtils.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.database.HomeRepo;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.database.PunishmentRepo;
import onl.jami.averUtils.storage.database.ReportRepo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class MenuUtils {

    public static final NamespacedKey REPORT_ID = new NamespacedKey(AverUtils.getPlugin(), "report_id");
    public static final NamespacedKey PLAYER_ID = new NamespacedKey(AverUtils.getPlugin(), "player_id");
    public static final NamespacedKey PUNISHMENT_ID = new NamespacedKey(AverUtils.getPlugin(), "punishment_id");

    public static final NamespacedKey HOME_WORLD = new NamespacedKey(AverUtils.getPlugin(), "home_world");
    public static final NamespacedKey HOME_X = new NamespacedKey(AverUtils.getPlugin(), "home_x");
    public static final NamespacedKey HOME_Y = new NamespacedKey(AverUtils.getPlugin(), "home_y");
    public static final NamespacedKey HOME_Z = new NamespacedKey(AverUtils.getPlugin(), "home_Z");
    public static final NamespacedKey HOME_PITCH = new NamespacedKey(AverUtils.getPlugin(), "home_pitch");
    public static final NamespacedKey HOME_YAW = new NamespacedKey(AverUtils.getPlugin(), "home_yaw");



    public static final Sound ERROR_SOUND = Sound.sound(Key.key("entity.villager.no"), Sound.Source.PLAYER, 1.0f, 1.0f);

    public static ItemStack button(Material m, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack playerItem(PlayerRepo.PlayerData playerData, List<Component> additionalLore) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerData.uuid());
        String name = player.getName();
        if (name == null) {
            name = "Unknown";
        }
        String firstPlayed = TimeUtils.formatDateTime(player.getFirstPlayed());
        long lastSeen = player.getLastSeen();
        String lastPlayed = TimeUtils.formatDateTime(lastSeen);
        if (player.isOnline()) {
            lastPlayed = "now";
        }

        List<Component> lore = new ArrayList<>(List.of(Component.text("UUID: " + playerData.uuid()),
                Component.text("Discord User: " + playerData.discordUsername() + " (" + playerData.discordId() + ")"),
                Component.empty(),
                Component.text("First Joined: " + firstPlayed),
                Component.text("Last seen: " + lastPlayed)));

        lore.addAll(additionalLore);

        ItemStack item = button(Material.PLAYER_HEAD, Component.text(name), lore);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(PLAYER_ID, PersistentDataType.STRING, playerData.uuid().toString());
        SkullMeta skullMeta = (SkullMeta) meta;
        skullMeta.setOwningPlayer(player);
        item.setItemMeta(skullMeta);
        return item;
    }

    public static ItemStack punishmentItem(PunishmentRepo.PunishmentData punishmentData) {
        Material m = switch (punishmentData.type()) {
            case WARN -> Material.PAPER;
            case KICK -> Material.LEATHER_BOOTS;
            case TIMEOUT -> Material.CLOCK;
            case TEMP_BAN -> Material.MACE;
            case BAN -> Material.MACE;
        };

        String playerName = Bukkit.getOfflinePlayer(punishmentData.playerUuid()).getName();
        String punisherName = Bukkit.getOfflinePlayer(punishmentData.punisherUuid()).getName();
        String duration = punishmentData.duration();
        if (duration == null) { duration = "permanent"; }
        Long durationEnd = punishmentData.durationEnd();
        if (durationEnd == null) { durationEnd = 0L; }
        String state = punishmentData.isExpired() ? "expired" : "active";

        ItemStack item = button(m,
                Component.text("Punishment #" + punishmentData.id() + ": " + playerName),
                List.of(Component.text("Punished by: " + punisherName),
                        Component.text("Reason: " + punishmentData.reason()),
                        Component.text("Details: " + punishmentData.details()),
                        Component.text("Duration: " + duration),
                        Component.text("Ends: " + TimeUtils.formatDateTime(durationEnd)),
                        Component.text("State: " + state)));
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(PUNISHMENT_ID, PersistentDataType.INTEGER, punishmentData.id());
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack reportItem(ReportRepo.ReportData reportData) {
        String reportedName = "Unknown";
        if (reportData.reportedUuid() != null) {
            reportedName = Bukkit.getOfflinePlayer(reportData.reportedUuid()).getName();
        }
        OfflinePlayer reporter = Bukkit.getOfflinePlayer(reportData.reporterUuid());
        ItemStack item = button(Material.PAPER,
                Component.text("Report #" + reportData.id()),
                List.of(Component.text("Player: " + reportedName),
                        Component.text("Reported by: " + reporter.getName()),
                        Component.text("Reason: " + reportData.reason()),
                        Component.text("Details: " + reportData.details())));
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(REPORT_ID, PersistentDataType.INTEGER, reportData.id());
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack homeItem(HomeRepo.HomeData homeData) {
        Material m = switch (homeData.world()) {
            case "world" -> Material.GRASS_BLOCK;
            case "world_nether" -> Material.NETHERRACK;
            case "world_the_end" -> Material.END_STONE;
            default -> Material.BRICK;
        };
        ItemStack item = button(m, Component.text(homeData.name()),
                List.of(Component.text("World: " + homeData.world()),
                        Component.text("x/y/z: " + homeData.x() + "/" + homeData.y() + "/" + homeData.z()),
                        Component.text("pitch/yaw: " + homeData.pitch() + "/" + homeData.yaw())));
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(HOME_WORLD, PersistentDataType.STRING, homeData.world());
        meta.getPersistentDataContainer().set(HOME_X, PersistentDataType.DOUBLE, homeData.x());
        meta.getPersistentDataContainer().set(HOME_Y, PersistentDataType.DOUBLE, homeData.y());
        meta.getPersistentDataContainer().set(HOME_Z, PersistentDataType.DOUBLE, homeData.z());
        meta.getPersistentDataContainer().set(HOME_PITCH, PersistentDataType.FLOAT, homeData.pitch());
        meta.getPersistentDataContainer().set(HOME_YAW, PersistentDataType.FLOAT, homeData.yaw());
        item.setItemMeta(meta);
        return item;
    }

    public static void drawFooter(Inventory inventory, int start, int end, Material material) {
        for (int i = start; i < end + 1; i++) {
            ItemStack footer = new ItemStack(material, 1);
            ItemMeta meta = footer.getItemMeta();
            meta.setHideTooltip(true);
            footer.setItemMeta(meta);
            inventory.setItem(i, footer);
        }
    }
}

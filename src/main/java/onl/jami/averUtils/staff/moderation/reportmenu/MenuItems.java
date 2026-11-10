package onl.jami.averUtils.staff.moderation.reportmenu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.database.ReportRepo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class MenuItems {

    public static final NamespacedKey REPORT_ID = new NamespacedKey(AverUtils.getPlugin(), "report_id");

    public static ItemStack button(Material m, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
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
}

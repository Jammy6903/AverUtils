package onl.jami.averUtils.staff.moderation.menus.reportmenu;

import net.kyori.adventure.text.Component;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.ReportRepo;
import onl.jami.averUtils.util.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class ReportDetailMenu implements InventoryHolder {

    public static final int DETAILS = 4, TP_REPORTER = 11, TP_REPORTED = 13, CLOSE_OPEN = 15, BACK = 22;
    private final Inventory inventory;
    private final ReportRepo.ReportData reportData;

    public ReportDetailMenu(ReportRepo.ReportData reportData) throws SQLException {
        this.reportData = reportData;

        String reportedName = "Unknown";
        if (reportData.reportedUuid() != null) {
            reportedName = Bukkit.getOfflinePlayer(reportData.reportedUuid()).getName();
        }
        OfflinePlayer reporter = Bukkit.getOfflinePlayer(reportData.reporterUuid());

        inventory = Bukkit.createInventory(this, 27,
                Messages.getComponent("moderation.reports-ui.title")
                        .append(Component.text(" > Report #" + reportData.id())));

        inventory.setItem(DETAILS, MenuUtils.button(Material.PAPER,
                Component.text("Report #" + reportData.id()),
                List.of(Component.text("Player: " + reportedName),
                        Component.text("Reported by: " + reporter.getName()),
                        Component.text("Reason: " + reportData.reason()),
                        Component.text("Details: " + reportData.details()))));

        inventory.setItem(TP_REPORTER, MenuUtils.button(Material.ENDER_PEARL,
                Component.text("Teleport to reporter"), List.of()));
        if (reportData.reportedUuid() != null) {
            inventory.setItem(TP_REPORTED, MenuUtils.button(Material.ENDER_EYE,
                    Component.text("Teleport to reported"), List.of()));
        }
        if (reportData.active()) {
            inventory.setItem(CLOSE_OPEN, MenuUtils.button(Material.GREEN_WOOL,
                    Component.text("Close report"), List.of()));
        }
        if (!reportData.active()) {
            inventory.setItem(CLOSE_OPEN, MenuUtils.button(Material.RED_WOOL,
                    Component.text("Open report"), List.of()));
        }
        inventory.setItem(BACK, MenuUtils.button(Material.ARROW,
                Component.text("Back"), List.of()));
    }

    public ReportRepo.ReportData getReportData() {
        return reportData;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

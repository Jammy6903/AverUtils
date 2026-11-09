package onl.jami.averUtils.staff.moderation.reportmenu;

import net.kyori.adventure.text.Component;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.ReportRepo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class ReportDetailMenu implements InventoryHolder {

    public static final int DETAILS = 4, TP_REPORTER = 11, TP_REPORTED = 13, CLOSE = 15, BACK = 22;
    private final Inventory inventory;
    private final ReportRepo.ReportData reportData;

    public ReportDetailMenu(ReportRepo.ReportData reportData) throws SQLException {
        this.reportData = reportData;
        inventory = Bukkit.createInventory(this, 27,
                Messages.getComponent("moderation.reports-ui.title")
                        .append(Component.text(" > Report #" + reportData.id())));

        inventory.setItem(DETAILS, MenuItems.button(Material.PAPER,
                Component.text("Report #" + reportData.id()),
                List.of(Component.text("Reason: " + reportData.reason()),
                        Component.text("Details: " + reportData.details()))));

        inventory.setItem(TP_REPORTER, MenuItems.button(Material.ENDER_PEARL,
                Component.text("Teleport to reporter"), List.of()));
        if (reportData.reportedUuid() != null) {
            inventory.setItem(TP_REPORTED, MenuItems.button(Material.ENDER_EYE,
                    Component.text("Teleport to reported"), List.of()));
        }
        inventory.setItem(CLOSE, MenuItems.button(Material.RED_WOOL,
                Component.text("Close report"), List.of()));
        inventory.setItem(BACK, MenuItems.button(Material.ARROW,
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

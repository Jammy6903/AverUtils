package onl.jami.averUtils.staff.moderation.menus.reportmenu;

import net.kyori.adventure.text.Component;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.ReportRepo;
import onl.jami.averUtils.util.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class ClosedReportMenu implements InventoryHolder {

    public static final int OPEN_BUTTON_SLOT = 49;
    private final Inventory inventory;

    public ClosedReportMenu() throws SQLException {
        inventory = Bukkit.createInventory(this, 54, Messages.getComponent("moderation.reports-ui.title").append(Messages.getComponent("moderation.reports-ui.closed-title")));

        List<ReportRepo.ReportData> reports = ReportRepo.getByActive(false);
        for (int i = 0; i < Math.min(reports.size(), 45); i++) {
            inventory.setItem(i, MenuUtils.reportItem(reports.get(i)));
        }
        inventory.setItem(OPEN_BUTTON_SLOT, MenuUtils.button(Material.ENDER_CHEST, Component.text("Open reports"), List.of()));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

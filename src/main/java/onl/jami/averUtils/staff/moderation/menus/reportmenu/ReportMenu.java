package onl.jami.averUtils.staff.moderation.menus.reportmenu;

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

public class ReportMenu implements InventoryHolder {

    public static final int CLOSED_BUTTON_SLOT = 49;
    private final Inventory inventory;

    public ReportMenu() throws SQLException {
        inventory = Bukkit.createInventory(this, 54, Messages.getComponent("moderation.reports-ui.title"));

        List<ReportRepo.ReportData> reports = ReportRepo.getByActive(true);
        for (int i = 0; i < Math.min(reports.size(), 45); i++) {
            inventory.setItem(i, MenuItems.reportItem(reports.get(i)));
        }
        inventory.setItem(CLOSED_BUTTON_SLOT, MenuItems.button(Material.CHEST, Component.text("Closed reports"), List.of()));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

}

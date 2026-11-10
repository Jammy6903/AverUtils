package onl.jami.averUtils.staff.moderation.reportmenu;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.ReportRepo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.sql.SQLException;
import java.util.UUID;

public class ReportMenuListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(event.getWhoClicked() instanceof Player p)) { return; }

        boolean isList = holder instanceof ReportMenu || holder instanceof ClosedReportMenu;
        if (!isList && !(holder instanceof ReportDetailMenu)) { return; }

        event.setCancelled(true);

        if (event.getClickedInventory() != event.getInventory()) { return; }
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) { return; }

        try {
            if (isList) { handleList(p, event.getSlot(), clicked, holder); }
            else { handleDetail(p, event.getSlot(), (ReportDetailMenu) holder); }
        } catch (SQLException e) {
            Messages.send(p, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.database.reports"), e);
        }
    }

    private void handleList(Player p, int slot, ItemStack clicked, InventoryHolder holder) throws SQLException {
        if (holder instanceof ReportMenu && slot == ReportMenu.CLOSED_BUTTON_SLOT) {
            p.openInventory(new ClosedReportMenu().getInventory());
            return;
        }
        if (holder instanceof ClosedReportMenu && slot == ClosedReportMenu.OPEN_BUTTON_SLOT) {
            p.openInventory(new ReportMenu().getInventory());
            return;
        }
        Integer id = clicked.getItemMeta().getPersistentDataContainer().get(MenuItems.REPORT_ID, PersistentDataType.INTEGER);
        if (id == null) { return; }
        ReportRepo.ReportData report = ReportRepo.getById(id);
        if (report == null) { return; }
        p.openInventory(new ReportDetailMenu(report).getInventory());
    }

    private void handleDetail(Player p, int slot, ReportDetailMenu menu) throws SQLException {
        ReportRepo.ReportData report = menu.getReportData();
        switch (slot) {
            case ReportDetailMenu.TP_REPORTER -> teleportTo(p, report.reporterUuid());
            case ReportDetailMenu.TP_REPORTED -> { if (report.reportedUuid() != null) { teleportTo(p, report.reportedUuid()); } }
            case ReportDetailMenu.CLOSE_OPEN -> {
                if (report.active()) {
                    ReportRepo.updateActive(report.id(), false);
                }
                if (!report.active()) {
                    ReportRepo.updateActive(report.id(), true);
                }
                p.openInventory(new ReportMenu().getInventory());
            }
            case ReportDetailMenu.BACK -> p.openInventory(new ReportMenu().getInventory());
        }
    }

    private void teleportTo(Player staff, UUID target) {
        Player t = Bukkit.getPlayer(target);
        if (t == null) {
            Messages.send(staff, "moderation.reports-ui.teleport.failure.player-offline");
            return;
        }
        staff.closeInventory();
        staff.teleport(t);
        Messages.send(staff, "moderation.reports-ui.teleport.success");
    }

}

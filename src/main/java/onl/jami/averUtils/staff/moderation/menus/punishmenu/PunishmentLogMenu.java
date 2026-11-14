package onl.jami.averUtils.staff.moderation.menus.punishmenu;

import net.kyori.adventure.text.Component;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.PunishmentRepo;
import onl.jami.averUtils.util.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PunishmentLogMenu implements InventoryHolder {

    private static final int FooterStart = 45, FooterEnd = 53;

    public static final int RETURN_BUTTON_SLOT = 49;
    public static final int PREVIOUS_BUTTON_SLOT = 45;
    public static final int NEXT_BUTTON_SLOT = 53;
    public static final int PAGE_SIZE = 45;

    private final Inventory inventory;
    private final int page;

    private final UUID TARGET;

    private final InventoryHolder PARENT;

    public PunishmentLogMenu(InventoryHolder parent) throws SQLException {
        this(parent, null, 0);
    }

    public PunishmentLogMenu(InventoryHolder parent, UUID target) throws SQLException {
        this(parent, target, 0);
    }

    public PunishmentLogMenu(InventoryHolder parent, UUID target, int requestedPage) throws SQLException {
        PARENT = parent;
        TARGET = target;

        List<PunishmentRepo.PunishmentData> punishments;
        int totalEntries;
        int totalPages;
        if (target != null) {
            totalEntries = PunishmentRepo.count(target);
            totalPages = totalEntries / PAGE_SIZE + 1;
            this.page = Math.clamp(requestedPage, 0, totalPages - 1);
            punishments = PunishmentRepo.getByUUID(target, page, PAGE_SIZE);
        } else {
            totalEntries = PunishmentRepo.count();
            totalPages = totalEntries / PAGE_SIZE + 1;
            this.page = Math.clamp(requestedPage, 0, totalPages - 1);
            punishments = PunishmentRepo.getAll(page, PAGE_SIZE);
        }

        inventory = Bukkit.createInventory(this, 54, Messages.getComponent("moderation.punish-ui.punishment-log-title"));

        for (int i = 0; i < punishments.size(); i++) {
            inventory.setItem(i, MenuUtils.punishmentItem(punishments.get(i)));
        }

        MenuUtils.drawFooter(inventory, FooterStart, FooterEnd, Material.BLUE_STAINED_GLASS_PANE);

        if (totalPages > 1) {
            if (page > 0) {
                inventory.setItem(PREVIOUS_BUTTON_SLOT, MenuUtils.button(Material.ARROW, Component.text("<- Previous Page"), List.of()));
            }
            if (page < totalPages - 1) {
                inventory.setItem(NEXT_BUTTON_SLOT, MenuUtils.button(Material.ARROW, Component.text("Next Page ->"), List.of()));
            }
        }

        inventory.setItem(RETURN_BUTTON_SLOT, MenuUtils.button(Material.BARRIER, Component.text("Back"), List.of()));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public int getPage() {
        return page;
    }

    public UUID getTarget() {
        return TARGET;
    }

    public InventoryHolder getParent() {
        return PARENT;
    }
}

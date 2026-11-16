package onl.jami.averUtils.menus.punish;

import net.kyori.adventure.text.Component;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.util.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;

public class PunishMenu implements InventoryHolder {

    private static final int FooterStart = 45, FooterEnd = 53;
    public static final int PAGE_SIZE = 45;
    public static final int PREVIOUS_BUTTON_SLOT = 45;
    public static final int PUNISHMENT_LOG_BUTTON_SLOT = 48;
    public static final int REPORT_LOG_BUTTON_SLOT = 50;
    public static final int NEXT_BUTTON_SLOT = 53;

    private final Inventory inventory;
    private final int page;

    public PunishMenu() throws SQLException {
        this(0);
    }

    public PunishMenu(int requestedPage) throws SQLException {
        int totalEntries = PlayerRepo.count();
        int totalPages = Math.max(1, Math.ceilDiv(totalEntries, PAGE_SIZE));

        this.page = Math.clamp(requestedPage, 0, totalPages - 1);
        List<PlayerRepo.PlayerData> players = new ArrayList<>(PlayerRepo.getPage(page, PAGE_SIZE));

        inventory = Bukkit.createInventory(this, 54, Locale.getComponent("moderation.punish-ui.title"));

        for (int i = 0; i < players.size(); i++) {
            inventory.setItem(i, MenuUtils.playerItem(players.get(i), List.of(Component.empty(),
                                                            Component.text("Left-click to see details"),
                                                            Component.text("Right-click to punish"))
            ));
        }

        MenuUtils.drawFooter(inventory, FooterStart, FooterEnd, Material.YELLOW_STAINED_GLASS_PANE);

        if (totalPages > 1) {
            if (page > 0) {
                inventory.setItem(PREVIOUS_BUTTON_SLOT, MenuUtils.button(Material.ARROW, Component.text("<- Previous Page"), List.of()));
            }
            if (page < totalPages - 1) {
                inventory.setItem(NEXT_BUTTON_SLOT, MenuUtils.button(Material.ARROW, Component.text("Next Page ->"), List.of()));
            }
        }

        inventory.setItem(PUNISHMENT_LOG_BUTTON_SLOT, MenuUtils.button(Material.WRITABLE_BOOK, Component.text("Punishment Log"), List.of()));
        inventory.setItem(REPORT_LOG_BUTTON_SLOT, MenuUtils.button(Material.DARK_OAK_HANGING_SIGN, Component.text("Report"), List.of()));
    }

    public int getPage() {
        return page;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

}

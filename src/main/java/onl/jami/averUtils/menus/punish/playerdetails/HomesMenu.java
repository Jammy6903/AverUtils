package onl.jami.averUtils.menus.punish.playerdetails;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.storage.database.HomeRepo;
import onl.jami.averUtils.util.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class HomesMenu implements InventoryHolder {

    private final InventoryHolder PARENT;

    public static final int BACK_BUTTON_SLOT = 49;
    private static final int FooterStart = 45, FooterEnd = 53;

    private final Inventory inventory;

    public HomesMenu(InventoryHolder parent, UUID target) throws SQLException {
        this.PARENT = parent;

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);

        inventory = Bukkit.createInventory(this, 54, Locale.getComponent("moderation.punish-ui.homes-title", Placeholder.component("player", Component.text(targetPlayer.getName()))));

        List<HomeRepo.HomeData> homes = HomeRepo.listHomes(targetPlayer.getUniqueId());
        for (int i = 0; i < Math.min(homes.size(), 54 - 9); i++) {
            HomeRepo.HomeData home = homes.get(i);
            inventory.setItem(i, MenuUtils.homeItem(home));
        }

        MenuUtils.drawFooter(inventory, FooterStart, FooterEnd, Material.BLUE_STAINED_GLASS_PANE);

        inventory.setItem(BACK_BUTTON_SLOT, MenuUtils.button(Material.BARRIER, Component.text("Back"), List.of()));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public InventoryHolder getParent() {
        return PARENT;
    }

    public int getBackButtonSlot() {
        return BACK_BUTTON_SLOT;
    }
}

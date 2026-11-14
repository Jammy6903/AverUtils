package onl.jami.averUtils.staff.moderation.menus.punishmenu.newpunishment;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.util.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class NewPunishmentMenu implements InventoryHolder {
    private final Inventory inventory;

    private final static int BACK_BUTTON_FOOTER_SLOT = 2;
    private final int BACK_BUTTON_SLOT;
    private final static int CUSTOM_BUTTON_FOOTER_SLOT = 6;
    private final int CUSTOM_PUNISH_BUTTON_SLOT;

    private final List<PunishmentSuggestion.ReasonEntry> reasons = PunishmentSuggestion.getReasons();

    private final InventoryHolder PARENT;

    private final UUID TARGET;

    public NewPunishmentMenu(InventoryHolder parent, UUID target) throws SQLException {
        this.TARGET = target;
        this.PARENT = parent;

        int size = Math.min(Math.ceilDiv(reasons.size() + 9, 9) * 9, 54);
        BACK_BUTTON_SLOT = size - 9 + BACK_BUTTON_FOOTER_SLOT;
        CUSTOM_PUNISH_BUTTON_SLOT = size - 9 + CUSTOM_BUTTON_FOOTER_SLOT;

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);

        inventory = Bukkit.createInventory(this, size, Messages.getComponent("moderation.punish-ui.new-punishment-title", Placeholder.component("player", Component.text(targetPlayer.getName()))));

        for (int i = 0; i < Math.min(reasons.size(), 54 - 9); i++) {
            PunishmentSuggestion.ReasonEntry reason = reasons.get(i);
            ItemStack item = MenuUtils.button(reason.suggestion().material(), Component.text(reason.suggestion().displayName()), List.of(Component.text(reason.reason())));
            inventory.setItem(i, item);
        }

        MenuUtils.drawFooter(inventory, size - 9, size - 1, Material.YELLOW_STAINED_GLASS_PANE);

        inventory.setItem(BACK_BUTTON_SLOT, MenuUtils.button(Material.BARRIER, Component.text("BACK"), List.of()));
        inventory.setItem(CUSTOM_PUNISH_BUTTON_SLOT, MenuUtils.button(Material.OAK_HANGING_SIGN, Component.text("Custom Punishment"), List.of()));
    }

    public PunishmentSuggestion.ReasonEntry getReason(int slot) {
        return slot >= 0 && slot < reasons.size() ? reasons.get(slot) : null;
    }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }

    public UUID getTarget() { return TARGET; }

    public InventoryHolder getParent() { return PARENT; }

    public int getBackButtonSlot() { return BACK_BUTTON_SLOT; }

    public int getCustomPunishButtonSlot() { return CUSTOM_PUNISH_BUTTON_SLOT; }

}

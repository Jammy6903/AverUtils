package onl.jami.averUtils.staff.moderation.menus.punishmenu.newpunishment;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.PunishmentType;
import onl.jami.averUtils.util.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PunishmentSuggestMenu implements InventoryHolder {
    private static final Material BAN = Material.MACE;
    private static final Material TEMP_BAN = Material.MACE;
    private static final Material TIMEOUT = Material.CLOCK;
    private static final Material KICK = Material.LEATHER_BOOTS;
    private static final Material WARN = Material.PAPER;

    private final Inventory inventory;

    private final static int BACK_BUTTON_FOOTER_SLOT = 2;
    public int backButtonSlot;
    private final static int CUSTOM_BUTTON_FOOTER_SLOT = 6;
    public int customButtonSlot;

    private final List<PunishmentSuggestion.Option> options;
    public PunishmentSuggestion.ReasonEntry reason;

    private final InventoryHolder PARENT;

    public UUID target;

    public PunishmentSuggestMenu(InventoryHolder parent, UUID target, PunishmentSuggestion.ReasonEntry reason) {
        this.target = target;
        this.options = reason.suggestion().options();
        this.reason = reason;
        this.PARENT = parent;

        int size = Math.min(Math.ceilDiv(options.size() + 9, 9) * 9, 54);
        backButtonSlot = size - 9 + BACK_BUTTON_FOOTER_SLOT;
        customButtonSlot = size - 9 + CUSTOM_BUTTON_FOOTER_SLOT;

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);

        inventory = Bukkit.createInventory(this, size, Messages.getComponent("moderation.punish-ui.new-punishment-title", Placeholder.component("player", Component.text(targetPlayer.getName()))));

        for (int i = 0; i < Math.min(options.size(), 54 - 9); i++) {
            PunishmentSuggestion.Option option = options.get(i);
            Material m = switch (option.type()) {
                case BAN -> BAN;
                case TEMP_BAN -> TEMP_BAN;
                case TIMEOUT -> TIMEOUT;
                case KICK -> KICK;
                case WARN -> WARN;
            };
            Component name = switch (option.type()) {
                case BAN -> Component.text("Permanent Ban");
                case TEMP_BAN -> Component.text("Temporary Ban: " + option.duration());
                case TIMEOUT -> Component.text("Timeout: " + option.duration());
                case KICK -> Component.text("Kick");
                case WARN -> Component.text("Warn");
            };
            ItemStack item = MenuUtils.button(m, name, List.of(Component.text(reason.reason())));
            if (option.type().equals(PunishmentType.BAN)) {
                ItemMeta meta = item.getItemMeta();
                meta.setEnchantmentGlintOverride(true);
                item.setItemMeta(meta);
            }
            inventory.setItem(i, item);
        }

        MenuUtils.drawFooter(inventory, size - 9, size - 1, Material.YELLOW_STAINED_GLASS_PANE);

        inventory.setItem(backButtonSlot, MenuUtils.button(Material.BARRIER, Component.text("BACK"), List.of()));
        inventory.setItem(customButtonSlot, MenuUtils.button(Material.OAK_HANGING_SIGN, Component.text("Custom Punishment"), List.of()));
    }

    public PunishmentSuggestion.Option getOption(int slot) {
        return slot >= 0 && slot < options.size() ? options.get(slot) : null;
    }

    public PunishmentSuggestion.ReasonEntry getReason() {
        return reason;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public UUID getTarget() {
        return target;
    }

    public InventoryHolder getParent() {
        return PARENT;
    }
}

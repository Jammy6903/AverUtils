package onl.jami.averUtils.menus.punish.playerdetails;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.util.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class PlayerDetailsMenu implements InventoryHolder {
    private final Inventory inventory;

    public static final int PLAYER_INFO_SLOT = 13;

    public static final int TELEPORT_BUTTON_SLOT = 10;
    public static final int HOMES_BUTTON_SLOT = 11;
    public static final int INVSEE_BUTTON_SLOT = 15;
    public static final int ENDER_CHEST_BUTTON_SLOT = 16;
    public static final int CORE_PROTECT_LOG_BUTTON_SLOT = 28;
    public static final int REPORTS_LOG_BUTTON_SLOT = 29;
    public static final int CHAT_LOG_BUTTON_SLOT = 31;
    public static final int PUNISHMENT_LOG_BUTTON_SLOT = 33;
    public static final int NEW_PUNISHMENT_BUTTON_SLOT = 34;

    public static final int BACK_BUTTON_SLOT = 49;
    private static final int FooterStart = 45, FooterEnd = 53;

    private final InventoryHolder PARENT;

    private final UUID target;

    public PlayerDetailsMenu(InventoryHolder parent, PlayerRepo.PlayerData playerData) {
        target = playerData.uuid();
        PARENT = parent;

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);

        inventory = Bukkit.createInventory(this, 54, Locale.getComponent("moderation.punish-ui.player-info-title", Placeholder.component("player", Component.text(targetPlayer.getName()))));

        inventory.setItem(PLAYER_INFO_SLOT, MenuUtils.playerItem(playerData, List.of()));

        inventory.setItem(TELEPORT_BUTTON_SLOT, MenuUtils.button(Material.ENDER_PEARL, Component.text("Teleport"), List.of()));
        inventory.setItem(HOMES_BUTTON_SLOT, MenuUtils.button(Material.RECOVERY_COMPASS, Component.text("Homes"), List.of()));

        inventory.setItem(INVSEE_BUTTON_SLOT, MenuUtils.button(Material.CHEST, Component.text("Inspect Inventory"), List.of()));
        inventory.setItem(ENDER_CHEST_BUTTON_SLOT, MenuUtils.button(Material.ENDER_CHEST, Component.text("Inspect Ender Chest"), List.of()));

        inventory.setItem(CORE_PROTECT_LOG_BUTTON_SLOT, MenuUtils.button(Material.BOOK, Component.text("Core Protect Log"), List.of()));
        inventory.setItem(REPORTS_LOG_BUTTON_SLOT, MenuUtils.button(Material.DARK_OAK_HANGING_SIGN, Component.text("Report Log"), List.of()));

        inventory.setItem(CHAT_LOG_BUTTON_SLOT, MenuUtils.button(Material.ENCHANTED_BOOK, Component.text("Chat Log"), List.of()));

        inventory.setItem(PUNISHMENT_LOG_BUTTON_SLOT, MenuUtils.button(Material.WRITABLE_BOOK, Component.text("Punishment Log"), List.of()));
        inventory.setItem(NEW_PUNISHMENT_BUTTON_SLOT, MenuUtils.button(Material.MACE, Component.text("New Punishment"), List.of()));

        MenuUtils.drawFooter(inventory, FooterStart, FooterEnd, Material.YELLOW_STAINED_GLASS_PANE);

        inventory.setItem(BACK_BUTTON_SLOT, MenuUtils.button(Material.BARRIER, Component.text("Back"), List.of()));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public UUID getTarget() { return target; }

    public InventoryHolder getParent() { return PARENT; }

}

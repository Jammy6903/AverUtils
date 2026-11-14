package onl.jami.averUtils.staff.moderation.menus.punishmenu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.staff.moderation.menus.punishmenu.newpunishment.NewPunishmentMenu;
import onl.jami.averUtils.staff.moderation.menus.punishmenu.newpunishment.PunishmentSuggestMenu;
import onl.jami.averUtils.staff.moderation.menus.punishmenu.newpunishment.PunishmentSuggestion;
import onl.jami.averUtils.staff.moderation.menus.punishmenu.playerdetails.PlayerDetailsMenu;
import onl.jami.averUtils.staff.moderation.menus.reportmenu.ReportMenu;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.util.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.sql.SQLException;
import java.util.UUID;

public class PunishMenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(event.getWhoClicked() instanceof Player p)) { return; }

        boolean isPlayerList = holder instanceof PunishMenu;
        boolean isPunishmentList = holder instanceof PunishmentLogMenu;
        boolean isPlayerDetails = holder instanceof PlayerDetailsMenu;
        boolean isNewPunishment = holder instanceof NewPunishmentMenu;
        boolean isPunishmentSuggest = holder instanceof PunishmentSuggestMenu;
        if (!(isPlayerList || isPunishmentList || isPlayerDetails || isNewPunishment || isPunishmentSuggest)) { return; }

        event.setCancelled(true);

        if (event.getClickedInventory() != event.getInventory()) { return; }
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) { return; }
        try {
            if (isPlayerList) {
                handlePlayerListInteraction(event, holder, clicked, p);
            }
            if (isPlayerDetails) {
                handlePlayerDetailsInteraction(event, holder, p);
            }
            if (isPunishmentList) {
                handlePunishmentListInteraction(event, holder, p);
            }
            if (isNewPunishment) {
                handleNewPunishmentInteraction(event, holder, p);
            }
            if (isPunishmentSuggest) {
                handlePunishmentSuggestionInteraction(event, holder, p);
            }
        } catch (SQLException e) {
            p.playSound(MenuUtils.ERROR_SOUND);
            Messages.send(p, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.generic"), e);
        }
    }

    private void handlePlayerListInteraction(InventoryClickEvent event, InventoryHolder holder, ItemStack clicked, Player player) throws SQLException {
        int slot = event.getSlot();
        if (!(holder instanceof PunishMenu menu)) { return; }
        if (slot == PunishMenu.PREVIOUS_BUTTON_SLOT || slot == PunishMenu.NEXT_BUTTON_SLOT) {
            int page = menu.getPage() - 1;
            if (slot == PunishMenu.NEXT_BUTTON_SLOT) {
                page = menu.getPage() + 1;
            }
            PunishMenu otherPage = new PunishMenu(page);
            player.openInventory(otherPage.getInventory());
            return;
        }

        if (slot == PunishMenu.PUNISHMENT_LOG_BUTTON_SLOT) {
            PunishmentLogMenu logMenu = new PunishmentLogMenu(menu);
            player.openInventory(logMenu.getInventory());
            return;
        }
        if (slot == PunishMenu.REPORT_LOG_BUTTON_SLOT) {
            ReportMenu reportMenu = new ReportMenu();
            player.openInventory(reportMenu.getInventory());
            return;
        }

        String playerUuid = clicked.getItemMeta().getPersistentDataContainer().get(MenuUtils.PLAYER_ID, PersistentDataType.STRING);
        if (playerUuid == null) { return; }
        PlayerRepo.PlayerData playerData = PlayerRepo.getPlayer(UUID.fromString(playerUuid));
        if (playerData == null) { return; }
        if (event.getClick().isLeftClick()) {
            player.openInventory(new PlayerDetailsMenu(menu, playerData).getInventory());
        }
        if (event.getClick().isRightClick()) {
            player.openInventory(new NewPunishmentMenu(menu, playerData.uuid()).getInventory());
        }
    }

    private void handlePlayerDetailsInteraction(InventoryClickEvent event, InventoryHolder holder, Player player) throws SQLException {
        int slot = event.getSlot();
        if (!(holder instanceof PlayerDetailsMenu menu)) { return; }
        switch (slot) {
            case PlayerDetailsMenu.BACK_BUTTON_SLOT:
                player.openInventory(menu.getParent().getInventory());
                break;
            case PlayerDetailsMenu.TELEPORT_BUTTON_SLOT:
                teleportTo(player, menu.getTarget());
                break;
            case PlayerDetailsMenu.INVSEE_BUTTON_SLOT:
                break;
            case PlayerDetailsMenu.CORE_PROTECT_LOG_BUTTON_SLOT:
                break;
            case PlayerDetailsMenu.REPORTS_LOG_BUTTON_SLOT:
                break;
            case PlayerDetailsMenu.PUNISHMENT_LOG_BUTTON_SLOT:
                PunishmentLogMenu logMenu = new PunishmentLogMenu(menu, menu.getTarget());
                player.openInventory(logMenu.getInventory());
                break;
            case PlayerDetailsMenu.NEW_PUNISHMENT_BUTTON_SLOT:
                NewPunishmentMenu newPunishmentMenu = new NewPunishmentMenu(menu, menu.getTarget());
                player.openInventory(newPunishmentMenu.getInventory());
                break;
        }
    }

    private void handlePunishmentListInteraction(InventoryClickEvent event, InventoryHolder holder, Player player) throws SQLException {
        int slot = event.getSlot();
        if (!(holder instanceof PunishmentLogMenu menu)) { return; }
        if (slot == PunishmentLogMenu.RETURN_BUTTON_SLOT) {
            player.openInventory(menu.getParent().getInventory());
            return;
        }
        if (slot == PunishmentLogMenu.PREVIOUS_BUTTON_SLOT || slot == PunishmentLogMenu.NEXT_BUTTON_SLOT) {
            int page = menu.getPage() - 1;
            if (slot == PunishmentLogMenu.NEXT_BUTTON_SLOT) {
                page = menu.getPage() + 1;
            }
            if (menu.getTarget() != null) {
                PunishmentLogMenu otherPage = new PunishmentLogMenu(menu.getParent(), menu.getTarget(), page);
                player.openInventory(otherPage.getInventory());
            }
        }
    }

    private void handleNewPunishmentInteraction(InventoryClickEvent event, InventoryHolder holder, Player player) throws SQLException {
        int slot = event.getSlot();
        if (!(holder instanceof NewPunishmentMenu menu)) { return; }
        OfflinePlayer target = Bukkit.getOfflinePlayer(menu.getTarget());
        if (slot == menu.backButtonSlot) {
            player.openInventory(menu.getParent().getInventory());
            return;
        }
        if (slot == menu.customButtonSlot) {
            player.closeInventory();
            player.sendMessage(Component.text("Click to punish", Style.style(NamedTextColor.RED, TextDecoration.UNDERLINED)).clickEvent(ClickEvent.suggestCommand("/punish " + target.getName() + " ")));
            return;
        }
        PunishmentSuggestion.ReasonEntry reason = menu.getReason(slot);
        if (reason == null) { return; }
        PunishmentSuggestMenu psm = new PunishmentSuggestMenu(menu, menu.getTarget(), reason);
        player.openInventory(psm.getInventory());
    }

    private void handlePunishmentSuggestionInteraction(InventoryClickEvent event, InventoryHolder holder, Player player) throws SQLException {
        int slot = event.getSlot();
        if (!(holder instanceof PunishmentSuggestMenu menu)) { return; }
        OfflinePlayer target = Bukkit.getOfflinePlayer(menu.getTarget());
        if (slot == menu.backButtonSlot) {
            player.openInventory(menu.getParent().getInventory());
            return;
        }
        if (slot == menu.customButtonSlot) {
            player.closeInventory();
            player.sendMessage(Component.text("Click to punish", Style.style(NamedTextColor.RED, TextDecoration.UNDERLINED)).clickEvent(ClickEvent.suggestCommand("/punish " + target.getName() + " [PUNISHMENT] [DURATION] \"" + menu.reason.reason() + "\" \"[DETAILS]\"")));
            return;
        }
        PunishmentSuggestion.Option option = menu.getOption(slot);
        if (option == null) { return; }
        PunishmentSuggestion.ReasonEntry reason = menu.getReason();
        switch (option.type()) {
            case BAN -> player.performCommand("punish " + target.getName() + " BAN \"" + reason.reason() + "\"");
            case TEMP_BAN -> player.performCommand("punish " + target.getName() + " TEMP_BAN " + option.duration() + " \"" + reason.reason() + "\"");
            case TIMEOUT -> player.performCommand("punish " + target.getName() + " TIMEOUT " + option.duration() + " \"" + reason.reason() + "\"");
            case KICK -> player.performCommand("punish " + target.getName() + " KICK \"" + reason.reason() + "\"");
            case WARN -> player.performCommand("punish " + target.getName() + " WARN \"" + reason.reason() + "\"");
        }
        player.closeInventory();
    }

    private void teleportTo(Player staff, UUID target) {
        OfflinePlayer t = Bukkit.getOfflinePlayer(target);
        staff.performCommand("tpo " + t.getName());
        staff.closeInventory();
    }
}

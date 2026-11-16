package onl.jami.averUtils.listeners;

import onl.jami.averUtils.managers.VanishManager;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteract implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        // prevent open animation and noise for vanished players
        if (VanishManager.isVanished(player)) {
            if (e.getHand() != EquipmentSlot.HAND) { return; }
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
            Block block = e.getClickedBlock();
            if (block == null) { return; }
            if (block.getState() instanceof Container container) {
                e.setCancelled(true);
                player.openInventory(container.getInventory());
            }
        }
    }

}

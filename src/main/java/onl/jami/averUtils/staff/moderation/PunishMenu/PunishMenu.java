package onl.jami.averUtils.staff.moderation.PunishMenu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import onl.jami.averUtils.locale.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PunishMenu implements InventoryHolder {
    private final Inventory inventory;

    public PunishMenu() {
        this.inventory = Bukkit.createInventory(this, 54, Messages.getComponent("moderation.punishment-ui.title"));

    }

    @Override
    public @NotNull Inventory getInventory() {
        List<ItemStack> heads = getOnlineHeads();
        if (heads.size() > 45) {}
        return inventory;
    }

    public static List<ItemStack> getOnlineHeads() {
        List<ItemStack> playerHeads = new ArrayList<>(Bukkit.getOnlinePlayers().size());
        for (Player p : Bukkit.getOnlinePlayers()) {
            playerHeads.add(headOf(p));
        }
        return playerHeads;
    }

    public static ItemStack headOf(Player p) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setPlayerProfile(p.getPlayerProfile());
        meta.displayName(Component.text(p.getName())
                .decoration(TextDecoration.ITALIC, false));
        head.setItemMeta(meta);
        return head;
    }
}

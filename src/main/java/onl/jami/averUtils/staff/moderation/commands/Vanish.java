package onl.jami.averUtils.staff.moderation.commands;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.staff.VanishManager;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class Vanish implements TabExecutor {

    private static final NamespacedKey VANISHED = new NamespacedKey(AverUtils.getPlugin(), "vanished");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Messages.send(sender, "commands.generic.failure.unknown");
            return true;
        }

        boolean vanishState;
        Boolean current = p.getPersistentDataContainer().get(VANISHED, PersistentDataType.BOOLEAN);
        vanishState = Objects.requireNonNullElse(current, false);

        String key;

        // NOTE: split for readability, otherwise it's a mess to decipher
        if (vanishState) {
            VanishManager.disable(p);
            key = "visible";
            p.getPersistentDataContainer().set(VANISHED, PersistentDataType.BOOLEAN, false);
        } else {
            VanishManager.enable(p);
            key = "invisible";
            p.getPersistentDataContainer().set(VANISHED, PersistentDataType.BOOLEAN, true);
        }

        Messages.send(sender, "commands.vanish.success." + key);

        return true;
    }

    public static boolean isVanished(Player p) {
        Boolean state = p.getPersistentDataContainer().get(VANISHED, PersistentDataType.BOOLEAN);
        return state != null && state;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

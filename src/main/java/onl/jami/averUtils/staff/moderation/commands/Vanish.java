package onl.jami.averUtils.staff.moderation.commands;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
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

    private static final BossBar indicator = BossBar.bossBar(Component.text("YOU ARE VANISHED", NamedTextColor.RED), 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.NOTCHED_20);
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
        p.setInvisible(!vanishState);

        String key = "visible";

        if (!vanishState) {
            key = "invisible";
            p.showBossBar(indicator);
            p.getPersistentDataContainer().set(VANISHED, PersistentDataType.BOOLEAN, true);
        } else {
            p.hideBossBar(indicator);
            p.getPersistentDataContainer().set(VANISHED, PersistentDataType.BOOLEAN, false);
        }

        Messages.send(sender, "commands.vanish.success." + key);

        return true;
    }

    public static void showIndicator(Player p) {
        Boolean state = p.getPersistentDataContainer().get(VANISHED, PersistentDataType.BOOLEAN);
        if (state != null && state) {
            p.showBossBar(indicator);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

package onl.jami.averUtils.staff.moderation.commands;

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

public class Fly implements TabExecutor {

    private static final NamespacedKey FLYING = new NamespacedKey(AverUtils.getPlugin(), "flying");


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Messages.send(sender, "commands.generic.failure.unknown");
            return true;
        }

        boolean flyingState;
        Boolean current = p.getPersistentDataContainer().get(FLYING, PersistentDataType.BOOLEAN);
        flyingState = Objects.requireNonNullElse(current, false);
        p.setAllowFlight(!flyingState);

        String key = "enabled";
        if (flyingState) {
            key = "disabled";
            p.getPersistentDataContainer().set(FLYING, PersistentDataType.BOOLEAN, false);
        } else {
            p.getPersistentDataContainer().set(FLYING, PersistentDataType.BOOLEAN, true);
        }

        Messages.send(sender, "commands.fly.success." + key);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

package onl.jami.averUtils.commands.moderation;

import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.managers.VanishManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Vanish implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Locale.send(sender, "commands.generic.failure.unknown");
            return true;
        }

        boolean vanishState = VanishManager.hasVanishedState(p);
        String key;

        // NOTE: split for readability, otherwise it's a mess to decipher
        if (vanishState) {
            VanishManager.setVanishedState(p, false);
            VanishManager.disable(p);
            key = "visible";
        } else {
            VanishManager.setVanishedState(p, true);
            VanishManager.enable(p);
            key = "invisible";
        }

        Locale.send(sender, "commands.vanish.success." + key);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

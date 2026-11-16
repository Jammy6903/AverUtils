package onl.jami.averUtils.commands.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.storage.database.WarpRepo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public class SetWarp implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            Locale.send(sender, "commands.generic.failure.players-only");
            return true;
        }
        if (args.length == 0) { return false; }
        try {
            if (!WarpRepo.createWarp(args[0], player.getLocation().getWorld().getName(), player.getX(), player.getY(), player.getZ(), player.getPitch(), player.getYaw())) {
                Locale.send(sender, "commands.setwarp.failure.already-exists");
                return true;
            }
            Locale.send(sender, "commands.setwarp.success",
                    Placeholder.component("warp", Component.text(args[0]))
            );
        } catch (SQLException exception) {
            Locale.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Locale.getString("errors.database.warps"), exception);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

package onl.jami.averUtils.teleportation.warp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.WarpRepo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class SetWarp implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            Messages.send(sender, "commands.generic.failure.player-only");
            return true;
        }
        if (args.length == 0) { return false; }
        try {
            if (!WarpRepo.createWarp(args[0], player.getLocation().getWorld().getName(), player.getX(), player.getY(), player.getZ(), player.getPitch(), player.getYaw())) {
                Messages.send(sender, "commands.setwarp.failure.already-exists");
                return true;
            }
            Messages.send(sender, "commands.setwarp.success",
                    Placeholder.component("warp", Component.text(args[0]))
            );
        } catch (SQLException exception) {
            Messages.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.database.warps"), exception);
        }
        return true;
    }
}

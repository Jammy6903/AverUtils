package onl.jami.averUtils.teleportation.warp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.WarpRepo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Warp implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }
        if (args.length == 0) { return false; }
        try {
            WarpRepo.WarpData warp = WarpRepo.getWarp(args[0]);
            if (warp == null) {
                Messages.send(sender, "commands.warp.failure.doesnt-exist",
                        Placeholder.component("warp", Component.text(args[0]))
                );
                return true;
            }
            ((Player) sender).teleport(new Location(Bukkit.getWorld(warp.world()), warp.x(), warp.y(), warp.z(), warp.yaw(), warp.pitch()));
            Messages.send(sender, "commands.warp.success",
                    Placeholder.component("warp", Component.text(args[0]))
            );
        } catch (SQLException exception) {
            Messages.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.database.warps"), exception);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();
        try {
            for (WarpRepo.WarpData warp : WarpRepo.listWarps()) {
                completions.add(warp.name());
            }
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.warps"), exception);
        }
        return completions;
    }
}

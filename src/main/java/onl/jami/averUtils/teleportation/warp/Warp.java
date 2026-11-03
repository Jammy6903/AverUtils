package onl.jami.averUtils.teleportation.warp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.AverUtils;
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
import java.util.logging.Level;

public class Warp implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) { return false; }
        try {
            WarpRepo.WarpData warp = WarpRepo.getWarp(args[0]);
            if (warp == null) {
                sender.sendMessage(Component.text("That warp doesn't exist!", NamedTextColor.RED));
                return true;
            }
            ((Player) sender).teleport(new Location(Bukkit.getWorld(warp.world()), warp.x(), warp.y(), warp.z(), warp.yaw(), warp.pitch()));
            sender.sendMessage(Component.text("Teleported to warp location!", NamedTextColor.GREEN));
        } catch (SQLException exception) {
            sender.sendMessage(Component.text("An unknown error has occurred", NamedTextColor.RED));
            AverUtils.logWarning("Failed to fetch warp from database", exception);
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
            AverUtils.logWarning("Failed to fetch warps from database", exception);
        }
        return completions;
    }
}

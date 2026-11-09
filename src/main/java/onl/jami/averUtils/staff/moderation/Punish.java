package onl.jami.averUtils.staff.moderation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Punish implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return List.of("TIMEOUT", "KICK", "TEMP_BAN", "BAN", "FETCH");
        }
        if (args.length == 2) {
            return null;
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("TIMEOUT") || args[0].equalsIgnoreCase("TEMP_BAN")) {
                return List.of("1m", "10m", "30m", "1h", "6h", "1d", "3d", "1w", "1M", "6M", "1Y");
            }
        }
        return List.of();
    }
}

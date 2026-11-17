package onl.jami.averUtils.commands.admin;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.storage.cache.ServerIconCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Reload implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        AverUtils.loadConfig(AverUtils.getPlugin());
        ServerIconCache.load();
        Locale.send(sender, "commands.reload.success");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

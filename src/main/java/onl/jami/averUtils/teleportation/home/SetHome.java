package onl.jami.averUtils.teleportation.home;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.HomeRepo;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class SetHome implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        int maxHomes = AverUtils.getCfg().getInt("homes.default-limit");

        boolean unlimited = player.hasPermission("averutils.homes.unlimited");

        if (!unlimited) {
            for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
                if (!perm.getValue()) {
                    continue;
                }
                String permission = perm.getPermission();
                if (!permission.startsWith("averutils.homes.")) {
                    continue;
                }
                try {
                    int value = Integer.parseInt(permission.substring("averutils.homes.".length()));
                    if (value > maxHomes) {
                        maxHomes = value;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        String name = "home";
        if (args.length > 0) {
            name = args[0].toLowerCase(Locale.ROOT);
        }
        Location location = player.getLocation();
        try {
            List<HomeRepo.HomeData> homes = HomeRepo.listHomes(player.getUniqueId());

            boolean upsert = false;
            for (HomeRepo.HomeData home : homes) {
                if (home.name().equalsIgnoreCase(name)) {
                    upsert = true;
                }
            }

            if (homes.size() >= maxHomes && !unlimited && !upsert) {
                Messages.send(sender, "commands.sethome.failure.not-enough-homes",
                        Placeholder.component("limit", Component.text(maxHomes)));
                return true;
            }

            HomeRepo.createHome(player.getUniqueId(), name, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getPitch(), location.getYaw());
            Messages.send(sender, "commands.sethome.success",
                    Placeholder.component("home", Component.text(name))
            );
            return true;
        } catch (SQLException exception) {
            Messages.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.database.homes"), exception);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

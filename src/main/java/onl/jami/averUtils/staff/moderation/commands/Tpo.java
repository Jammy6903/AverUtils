package onl.jami.averUtils.staff.moderation.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.PlayerRepo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public class Tpo implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }
        if (args.length < 1) {
            return false;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[0]);
        if (target == null) {
            Messages.send(sender, "commands.tpo.failure.doesnt-exist");
            return true;
        }

        if (target.isOnline()) {
            Player targetPlayer = target.getPlayer();
            if (targetPlayer != null) {
                p.teleport(target.getPlayer().getLocation());
                Messages.send(sender, "commands.tpo.success.online");
                return true;
            }
        }

        try {
            PlayerRepo.PlayerData playerData = PlayerRepo.getPlayer(target.getUniqueId());
            if (playerData == null
                    || playerData.last_world() == null
                    || playerData.last_x() == null
                    || playerData.last_y() == null
                    || playerData.last_z() == null) {
                Messages.send(sender, "commands.tpo.failure.no-location",
                        Placeholder.component("player", Component.text(args[0])));
                return true;
            }
            World world = Bukkit.getWorld(playerData.last_world());
            if (world == null) {
                Messages.send(sender, "commands.tpo.failure.world-missing");
            }
            p.teleport(new Location(world, playerData.last_x(), playerData.last_y(), playerData.last_z()));
            Messages.send(sender, "commands.tpo.success.offline",
                    Placeholder.component("player", Component.text(args[0])));
        } catch (SQLException e) {
            AverUtils.logWarning(Messages.getString("errors.database.players"), e);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return null;
        }
        return List.of();
    }
}

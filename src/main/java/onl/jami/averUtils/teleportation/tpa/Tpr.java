package onl.jami.averUtils.teleportation.tpa;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Tpr implements TabExecutor {

    public static final Set<Material> UNSAFE = Set.of(Material.LAVA, Material.WATER, Material.FIRE,
            Material.SOUL_FIRE, Material.MAGMA_BLOCK, Material.CAMPFIRE, Material.SOUL_CAMPFIRE,
            Material.CACTUS, Material.SWEET_BERRY_BUSH, Material.POWDER_SNOW, Material.AIR);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        int distance = AverUtils.getCfg().getInt("teleportation.tpr-distance");

        double randomX = ThreadLocalRandom.current().nextDouble(-distance, distance);
        double randomZ = ThreadLocalRandom.current().nextDouble(-distance, distance);

        Location location;

        World world = player.getWorld();
        if (world.getEnvironment().equals(World.Environment.NETHER)) {
            Messages.send(sender, "commands.tpr.failure.nether");
            return true;
        }
        location = new Location(world, randomX, 64.0, randomZ).toHighestLocation(HeightMap.MOTION_BLOCKING_NO_LEAVES);
        int attempts = 0;
        WorldBorder border = world.getWorldBorder();
        while (!isSafe(location) || !border.isInside(location)) {
            if (attempts > 10) {
                Messages.send(sender, "commands.tpr.failure.attempts");
                return true;
            }
            location.add(-10, 0, -10).toHighestLocation(HeightMap.MOTION_BLOCKING_NO_LEAVES);
            attempts++;
        }

        player.teleport(location.add(0.5, 1, 0.5));
        Messages.send(sender, "commands.tpr.success");

        return true;
    }

    private boolean isSafe(Location groundLocation) {
        return !UNSAFE.contains(groundLocation.getBlock().getType());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

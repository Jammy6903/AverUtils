package onl.jami.averUtils.teleportation.tpa;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
            Material.CACTUS, Material.SWEET_BERRY_BUSH, Material.POWDER_SNOW);

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
        location = new Location(world, randomX, 64.0, randomZ).toHighestLocation().add(0, 1, 0);
        int attempts = 0;
        while (isSafe(location)) {
            if (attempts > 10) {
                Messages.send(sender, "commands.tpr.failure.attempts");
            }
            location.add(10, 0, 10);
            Messages.send(sender, "commands.tpr.success");
            attempts++;
        }

        player.teleport(location);

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

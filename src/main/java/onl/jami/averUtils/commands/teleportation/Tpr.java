package onl.jami.averUtils.commands.teleportation;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Locale;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Tpr implements TabExecutor {

    public static final Set<Material> UNSAFE = Set.of(Material.FIRE, Material.SOUL_FIRE, Material.MAGMA_BLOCK,
            Material.CAMPFIRE, Material.SOUL_CAMPFIRE, Material.CACTUS, Material.SWEET_BERRY_BUSH,
            Material.WITHER_ROSE);

    private static final Set<UUID> SEARCHING = ConcurrentHashMap.newKeySet();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            Locale.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        World world = player.getWorld();
        if (world.getEnvironment().equals(World.Environment.NETHER)) {
            Locale.send(sender, "commands.tpr.failure.nether");
            return true;
        }

        if (!SEARCHING.add(player.getUniqueId())) {
            Locale.send(sender, "commands.tpr.failure.already-searching");
            return true;
        }

        int distance = Math.max(1, AverUtils.getCfg().getInt("teleportation.tpr-distance", 5000));
        int attempts = Math.max(1, AverUtils.getCfg().getInt("teleportation.tpr-attempts", 10));

        WorldBorder border = world.getWorldBorder();
        int radius = (int) Math.min(distance, border.getSize() / 2 - 16);
        if (radius < 1) {
            SEARCHING.remove(player.getUniqueId());
            Locale.send(sender, "commands.tpr.failure.attempts");
        }

        search(player, world, border.getCenter(), radius, attempts);
        return true;
    }

    private static void search(Player player, World world, Location center, int radius, int attemptsLeft) {
        if (attemptsLeft <= 0 || !player.isOnline()) {
            SEARCHING.remove(player.getUniqueId());
            if (player.isOnline()) {
                Locale.send(player, "commands.tpr.failure.attempts");
            }
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int x = center.getBlockX() + random.nextInt(-radius, radius + 1);
        int y = center.getBlockY() + random.nextInt(-radius, radius + 1);

        world.getChunkAtAsync(x >> 4, y >> 4).thenAccept(chunk -> {
            Block ground = world.getHighestBlockAt(x, y, HeightMap.MOTION_BLOCKING_NO_LEAVES);
            if (!isSafe(ground)) {
                search(player, world, center, radius, attemptsLeft - 1);
                return;
            }

            Location destination = ground.getLocation().add(0.5, 1, 0.5);
            destination.setDirection(player.getLocation().getDirection());
            player.teleportAsync(destination).thenAccept(success -> {
                SEARCHING.remove(player.getUniqueId());
                Locale.send(player, success ? "commands.tpr.success" : "commands.tpr.failure.attempts");
            }).exceptionally(ex -> {
                SEARCHING.remove(player.getUniqueId());
                AverUtils.logWarning("Random teleport failed", ex instanceof Exception e ? e : new RuntimeException(ex));
                return null;
            });
        });
    }

    private static boolean isSafe(Block ground) {
        if (ground.getY() <= ground.getWorld().getMinHeight()) { return false; }
        if (!ground.getType().isSolid() || UNSAFE.contains(ground.getType())) { return false; }
        Block feet = ground.getRelative(0, 1, 0);
        Block head = ground.getRelative(0, 2, 0);
        return feet.isPassable() && !feet.isLiquid() && head.isPassable() && !head.isLiquid();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

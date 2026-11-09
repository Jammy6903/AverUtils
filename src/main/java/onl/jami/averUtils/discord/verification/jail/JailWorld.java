package onl.jami.averUtils.discord.verification.jail;

import onl.jami.averUtils.AverUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.EndGateway;
import org.bukkit.entity.Player;

public class JailWorld {

    private static final int MIN_X = 6, MAX_X = 10;
    private static final int MIN_Y = 99, MAX_Y = 103;
    private static final int MIN_Z = 6, MAX_Z = 10;

    public static World getOrCreateJailWorld() {
        World world = Bukkit.getWorld("averutils_jail");
        if (world != null) { return world; }
        return new WorldCreator("averutils_jail")
                .generator(new VoidGenerator())
                .environment(World.Environment.NORMAL)
                .generateStructures(false)
                .createWorld();
    }

    public static void buildInnerBox(World world) {
        for (int x = MIN_X; x <= MAX_X; x++) {
            for (int y = MIN_Y; y <= MAX_Y; y++) {
                for (int z = MIN_Z; z <= MAX_Z; z++) {
                    boolean onShell =  x == MIN_X || x == MAX_X
                                    || y == MIN_Y || y == MAX_Y
                                    || z == MIN_Z || z == MAX_Z;
                    if (onShell) {
                        world.getBlockAt(x,y,z).setType(Material.BARRIER);
                    }
                }
            }
        }
    }

    public static void buildOuterBox(World world) {
        for (int x = MIN_X - 1; x <= MAX_X + 1; x++) {
            for (int y = MIN_Y - 1; y <= MAX_Y + 1; y++) {
                for (int z = MIN_Z - 1; z <= MAX_Z + 1; z++) {
                    boolean onShell =  x == MIN_X - 1 || x == MAX_X + 1
                                    || y == MIN_Y - 1 || y == MAX_Y + 1
                                    || z == MIN_Z - 1 || z == MAX_Z + 1;
                    if (onShell) {
                        world.getBlockAt(x,y,z).setType(Material.END_GATEWAY);
                        Block b = world.getBlockAt(x, y, z);
                        if (b.getState() instanceof EndGateway gateway) {
                            gateway.setAge(999999999999999999L);
                            gateway.setExitLocation(new Location(world, 8.5, 100, 8.5));
                            gateway.setExactTeleport(true);
                            gateway.update();
                        }
                    }
                }
            }
        }
    }

    public static void configureWorld(World world) {
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setGameRule(GameRules.SPAWN_MOBS, false);
        world.setGameRule(GameRules.ADVANCE_TIME, false);
        world.setGameRule(GameRules.ADVANCE_WEATHER, false);
        world.setTime(6000);
        world.setStorm(false);
        world.setThundering(false);
        world.setSpawnLocation(8, 100, 8);
    }

    public static void release(Player player) {
        Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> player.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation()));
    }

}

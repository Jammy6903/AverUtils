package onl.jami.averUtils.discord.verification.jail;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.jspecify.annotations.NonNull;

import java.util.Random;

public class VoidGenerator extends ChunkGenerator {

    @Override
    public Location getFixedSpawnLocation(@NonNull World world, @NonNull Random random) {
        return new Location(world, 8.5, 100, 8.5);
    }
}

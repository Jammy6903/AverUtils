package onl.jami.averUtils.storage.cache;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.BlockRepo;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BlockCache {

    private static final Map<UUID, List<BlockRepo.BlockData>> blocks = new ConcurrentHashMap<>();

    public static void loadBlocksForPlayer(UUID uuid) {
        try {
            blocks.put(uuid, BlockRepo.getBlocksForPlayer(uuid));
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.blocks"), exception);
        }
    }

    public static BlockRepo.BlockData getBlockForPlayers(UUID uuid, UUID blocked) {
        List<BlockRepo.BlockData> blockData = blocks.get(uuid);
        if (blockData == null) { return null; }
        return blockData.stream()
                .filter(b -> b.blockedUuid().equals(blocked))
                .findFirst()
                .orElse(null);
    }

    public static void removeBlocksForPlayer(UUID uuid) {
        blocks.remove(uuid);
    }
}

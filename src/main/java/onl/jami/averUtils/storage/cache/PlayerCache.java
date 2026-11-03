package onl.jami.averUtils.storage.cache;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.database.PlayerRepo;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCache {

    private static final Map<UUID, PlayerRepo.PlayerData> players = new ConcurrentHashMap<>();

    public static PlayerRepo.PlayerData getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public static void addPlayer(UUID uuid, PlayerRepo.PlayerData data) {
        players.put(uuid, data);
    }

    public static void updatePlayer(UUID uuid) {
        try {
            players.put(uuid, PlayerRepo.getPlayer(uuid));
        } catch (SQLException exception) {
            AverUtils.logWarning("Failed to fetch player from database", exception);
        }
    }

    public static void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

}

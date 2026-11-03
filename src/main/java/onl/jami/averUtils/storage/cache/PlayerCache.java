package onl.jami.averUtils.storage.cache;

import onl.jami.averUtils.storage.database.PlayerRepo;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

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
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to update player in cache " + uuid, e);
        }
    }

    public static void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

}

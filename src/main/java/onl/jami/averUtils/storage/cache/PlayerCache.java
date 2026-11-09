package onl.jami.averUtils.storage.cache;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.PlayerRepo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCache {

    private static final Map<UUID, PlayerRepo.PlayerData> players = new ConcurrentHashMap<>();

    public static PlayerRepo.PlayerData getPlayer(Player player) {
        PlayerRepo.PlayerData playerData = players.get(player.getUniqueId());
        if (playerData == null) {
            playerData = updatePlayer(player.getUniqueId());
        }
        if (playerData == null) {
            Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> player.kick(Messages.getComponent("player.moderation.kick.unknown")));
            return null;
        }
        return playerData;
    }

    public static PlayerRepo.PlayerData updatePlayer(UUID uuid) {
        try {
            if (!players.containsKey(uuid)) {
                return null;
            }
            PlayerRepo.PlayerData playerData = PlayerRepo.getPlayer(uuid);
            players.put(uuid, playerData);
            return playerData;
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
            return null;
        }
    }

    public static void loadPlayer(Player player) {
        try {
            PlayerRepo.PlayerData playerData = PlayerRepo.getPlayer(player.getUniqueId());
            players.put(player.getUniqueId(), playerData);
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
            player.kick(Messages.getComponent("player.moderation.kick.unknown"));
        }
    }

    public static void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

}

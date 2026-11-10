package onl.jami.averUtils.storage.cache;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.PunishmentRepo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PunishmentCache {
    private static final Map<UUID, List<PunishmentRepo.PunishmentData>> players = new ConcurrentHashMap<>();

    public static List<PunishmentRepo.PunishmentData> loadActivePunishmentsForPlayer(UUID uuid) {
        long now = System.currentTimeMillis();
        try {
            List<PunishmentRepo.PunishmentData> punishments = PunishmentRepo.getByUUID(uuid);
            if (!punishments.isEmpty()) {
                List<PunishmentRepo.PunishmentData> activePunishments = new ArrayList<>();
                for (PunishmentRepo.PunishmentData p : punishments) {
                    if (!p.isExpired() && (p.durationEnd() == null || p.durationEnd() > now)) {
                        activePunishments.add(p);
                    }
                }
                players.put(uuid, activePunishments);
                return activePunishments;
            }
        } catch (SQLException e) {
            AverUtils.logWarning(Messages.getString("errors.database.punishments"), e);
        }
        return List.of();
    }

    public static List<PunishmentRepo.PunishmentData> getCachedPunishmentsWithActiveCheck(UUID uuid) {
        long now = System.currentTimeMillis();
        List<PunishmentRepo.PunishmentData> active = new ArrayList<>();
        for (PunishmentRepo.PunishmentData p : players.getOrDefault(uuid, List.of())) {
            if (!p.isExpired() && (p.durationEnd() == null || p.durationEnd() > now)) {
                active.add(p);
            }
        }
        return active;
    }

    public static void removePlayer(UUID uuid) {
        players.remove(uuid);
    }
}

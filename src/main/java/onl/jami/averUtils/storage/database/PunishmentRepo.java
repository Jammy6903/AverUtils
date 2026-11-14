package onl.jami.averUtils.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PunishmentRepo {
    public record PunishmentData(int id, UUID playerUuid, UUID punisherUuid, PunishmentType type, String duration, Long durationEnd, String reason, String details, boolean isExpired) {}

    public static void create(UUID playerUuid, UUID punisherUuid, PunishmentType type, String duration, Long durationEnd, String reason, String details) throws SQLException {
        String sql = "INSERT INTO punishments (player_uuid, punisher_uuid, type, duration, duration_end, reason, details) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            if (punisherUuid == null) {
                ps.setString(2, null);
            } else {
                ps.setString(2, punisherUuid.toString());
            }
            ps.setString(3, type.toString());
            ps.setString(4, duration);
            if (durationEnd == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setLong(5, durationEnd);
            }
            ps.setString(6, reason);
            ps.setString(7, details);
            ps.executeUpdate();
        }
    }

    public static List<PunishmentData> getByUUID(UUID playerUuid) throws SQLException {
        String sql = "SELECT * FROM punishments WHERE player_uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<PunishmentData> punishments = new ArrayList<>();
                while (rs.next()) {
                    punishments.add(new PunishmentData(
                            rs.getInt("punishment_id"),
                            playerUuid,
                            uuidOrNull(rs.getString("punisher_uuid")),
                            PunishmentType.valueOf(rs.getString("type")),
                            rs.getString("duration"),
                            longOrNull(rs, "duration_end"),
                            rs.getString("reason"),
                            rs.getString("details"),
                            rs.getBoolean("is_expired")
                    ));
                }
                return punishments;
            }
        }
    }

    public static List<PunishmentData> getByUUID(UUID playerUuid, int page, int pageSize) throws SQLException {
        String sql = "SELECT * FROM punishments WHERE player_uuid = ? ORDER BY is_expired ASC, punishment_id DESC LIMIT ? OFFSET ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, pageSize);
            ps.setInt(3, page * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<PunishmentData> punishments = new ArrayList<>();
                while (rs.next()) {
                    punishments.add(new PunishmentData(
                            rs.getInt("punishment_id"),
                            UUID.fromString(rs.getString("player_uuid")),
                            uuidOrNull(rs.getString("punisher_uuid")),
                            PunishmentType.valueOf(rs.getString("type")),
                            rs.getString("duration"),
                            longOrNull(rs, "duration_end"),
                            rs.getString("reason"),
                            rs.getString("details"),
                            rs.getBoolean("is_expired")
                    ));
                }
                return punishments;
            }
        }
    }

    public static List<PunishmentData> getAll(int page, int pageSize) throws SQLException {
        String sql = "SELECT * FROM punishments ORDER BY is_expired ASC, punishment_id DESC LIMIT ? OFFSET ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, page * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<PunishmentData> punishments = new ArrayList<>();
                while (rs.next()) {
                    punishments.add(new PunishmentData(
                            rs.getInt("punishment_id"),
                            UUID.fromString(rs.getString("player_uuid")),
                            uuidOrNull(rs.getString("punisher_uuid")),
                            PunishmentType.valueOf(rs.getString("type")),
                            rs.getString("duration"),
                            longOrNull(rs, "duration_end"),
                            rs.getString("reason"),
                            rs.getString("details"),
                            rs.getBoolean("is_expired")
                    ));
                }
                return punishments;
            }
        }
    }

    public static int count(UUID playerUuid) throws SQLException {
        String sql = "SELECT COUNT(*) FROM punishments WHERE player_uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return 0;
                return rs.getInt(1);
            }
        }
    }

    public static int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM punishments";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return 0;
                return rs.getInt(1);
            }
        }
    }

    public static boolean setExpired(int id, boolean expired) throws SQLException {
        String sql = "UPDATE punishments SET is_expired = ? WHERE punishment_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, expired);
            ps.setInt(2, id);
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean setDetails(int id, String details) throws SQLException {
        String sql = "UPDATE punishments SET details = ? WHERE punishment_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, details);
            ps.setInt(2, id);
            return ps.executeUpdate() != 0;
        }
    }

    private static UUID uuidOrNull(String uuid) {
        return uuid == null ? null : UUID.fromString(uuid);
    }

    private static Long longOrNull(ResultSet rs, String column) throws SQLException {
        long l = rs.getLong(column);
        return rs.wasNull() ? null : l;
    }
}

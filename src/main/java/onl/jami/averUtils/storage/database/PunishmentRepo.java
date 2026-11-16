package onl.jami.averUtils.storage.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PunishmentRepo {
    public record PunishmentData(int id, UUID playerUuid, UUID punisherUuid, PunishmentType type, String duration, Long durationEnd, String reason, String details, UUID lastAmendedBy, boolean isExpired) {}

    public static int create(UUID playerUuid, UUID punisherUuid, PunishmentType type, String duration, Long durationEnd, String reason, String details) throws SQLException {
        String sql = "INSERT INTO punishments (player_uuid, punisher_uuid, type, duration, duration_end, reason, details) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { return keys.getInt(1); }
                throw new SQLException("Insert succeeded but no id was returned");
            }
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
                            uuidOrNull(rs.getString("last_amended_by")),
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
                            uuidOrNull(rs.getString("last_amended_by")),
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
                            uuidOrNull(rs.getString("last_amended_by")),
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

    public static boolean setDuration(int id, String duration, long durationEnd, UUID amender) throws SQLException {
        String sql = "UPDATE punishments SET duration = ?, duration_end = ?, last_amended_by = ? WHERE id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, duration);
            ps.setLong(2, durationEnd);
            ps.setString(3, amender.toString());
            ps.setInt(4, id);
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean setExpired(int id, boolean expired, UUID amender) throws SQLException {
        String sql = "UPDATE punishments SET is_expired = ?, last_amended_by = ? WHERE punishment_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, expired);
            ps.setString(2, amender.toString());
            ps.setInt(3, id);
            return ps.executeUpdate() != 0;
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

    public static boolean setDetails(int id, String details, UUID amender) throws SQLException {
        String sql = "UPDATE punishments SET details = ?, last_amended_by = ? WHERE punishment_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, details);
            ps.setString(2, amender.toString());
            ps.setInt(3, id);
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

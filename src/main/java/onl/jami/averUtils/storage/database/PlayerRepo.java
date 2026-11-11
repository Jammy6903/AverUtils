package onl.jami.averUtils.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerRepo {
    public record PlayerData(UUID uuid, boolean isVerified, Long discordId, String discordUsername, String discordGlobalName, String discordNickname, String nickname, String pronouns, NameChoice nameChoice, String nameStyle, boolean pvpEnabled, boolean tpEnabled, boolean pingEnabled, UUID last_world, Double last_x, Double last_y, Double last_z) {
        public enum NameChoice {
            PLAYER_NAME,
            NICKNAME,
            DISCORD_GLOBAL,
            DISCORD_NICKNAME
        }
    }

    public static void createPlayer(UUID uuid) throws SQLException {
        String sql = "INSERT OR IGNORE INTO players (uuid) VALUES (?)";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }

    public static PlayerData getPlayer(UUID uuid) throws SQLException {
        String sql = "SELECT * FROM players WHERE uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                long id = rs.getLong("discord_id");
                Long discordId = rs.wasNull() ? null : id;
                Double lastX = doubleOrNull(rs, "last_x");
                Double lastY = doubleOrNull(rs, "last_y");
                Double lastZ = doubleOrNull(rs, "last_z");
                return new PlayerData(
                        uuid,
                        rs.getBoolean("is_verified"), discordId,
                        rs.getString("discord_username"),
                        rs.getString("discord_globalname"),
                        rs.getString("discord_nickname"),
                        rs.getString("nickname"),
                        rs.getString("pronouns"),
                        PlayerData.NameChoice.valueOf(rs.getString("name_choice")),
                        rs.getString("name_style"),
                        rs.getBoolean("pvp_enabled"),
                        rs.getBoolean("tp_enabled"),
                        rs.getBoolean("ping_enabled"),
                        uuidOrNull(rs.getString("last_world_uuid")),
                        lastX,
                        lastY,
                        lastZ
                );
            }
        }
    }

    public static PlayerData getPlayerByDiscordId(Long id) throws SQLException {
        String sql = "SELECT * FROM players WHERE discord_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new PlayerData(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getBoolean("is_verified"), id,
                        rs.getString("discord_username"),
                        rs.getString("discord_globalname"),
                        rs.getString("discord_nickname"),
                        rs.getString("nickname"),
                        rs.getString("pronouns"),
                        PlayerData.NameChoice.valueOf(rs.getString("name_choice")),
                        rs.getString("name_style"),
                        rs.getBoolean("pvp_enabled"),
                        rs.getBoolean("tp_enabled"),
                        rs.getBoolean("ping_enabled"),
                        uuidOrNull(rs.getString("last_world_uuid")),
                        rs.getObject("last_x", Double.class),
                        rs.getObject("last_y", Double.class),
                        rs.getObject("last_z", Double.class)
                );
            }
        }
    }

    public static void verifyPlayer(UUID uuid, Long discordId, String discordName, String discordNameGlobal, String discordNick) throws SQLException {
        String sql = """
                        UPDATE players
                        SET is_verified = 1,
                            discord_id = ?,
                            discord_username = ?,
                            discord_globalname = ?,
                            discord_nickname = ?
                        WHERE uuid = ?
                     """;
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setLong(1, discordId);
            ps.setString(2, discordName);
            ps.setString(3, discordNameGlobal);
            ps.setString(4, discordNick);
            ps.setString(5, uuid.toString());
            ps.executeUpdate();
        }
    }

    public static void updateDiscordUserName(UUID uuid, String discordName) throws SQLException {
        String sql = "UPDATE players SET discord_username = ? WHERE uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, discordName);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public static void updateDiscordGobalName(UUID uuid, String discordGlobalName) throws SQLException {
        String sql = "UPDATE players SET discord_globalname = ? WHERE uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, discordGlobalName);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public static void updateDiscordNickname(UUID uuid, String discordNickname) throws SQLException {
        String sql = "UPDATE players SET discord_nickname = ? WHERE uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, discordNickname);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public static void updateNickname(UUID uuid, String nickname) throws SQLException {
        String sql = "UPDATE players SET nickname = ? WHERE uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, nickname);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public static void updateNameChoice(UUID uuid, PlayerData.NameChoice choice) throws SQLException {
        String sql = "UPDATE players SET name_choice = ? WHERE uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, choice.toString());
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public static void updatePronouns(UUID uuid, String pronouns) throws SQLException {
        String sql = "UPDATE players SET pronouns = ? WHERE uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, pronouns);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public static void updateNameStyle(UUID uuid, String nameStyle) throws SQLException {
        String sql = "UPDATE players SET name_style = ? WHERE uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, nameStyle);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public static void updatePvp(UUID uuid, boolean pvpEnabled) throws SQLException {
        String sql = "UPDATE players SET pvp_enabled = ? WHERE uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, pvpEnabled);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public static void updateTp(UUID uuid, boolean tpEnabled) throws SQLException {
        String sql = "UPDATE players SET tp_enabled = ? WHERE uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, tpEnabled);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public static void updatePing(UUID uuid, boolean pingEnabled) throws SQLException {
        String sql = "UPDATE players SET ping_enabled = ? WHERE uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, pingEnabled);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public static void unVerify(UUID uuid) throws SQLException {
        String sql = "UPDATE players SET is_verified = false, discord_globalname = null, discord_id = null, discord_nickname = null, discord_username = null, name_choice = 'PLAYER_NAME' WHERE uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }

    public static void updateLastLocation(UUID player, UUID world, Double x, Double y, Double z) throws SQLException {
        String sql = "UPDATE players SET last_world_uuid = ?, last_x = ?, last_y = ?, last_z = ? WHERE uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, world.toString());
            ps.setDouble(2, x);
            ps.setDouble(3, y);
            ps.setDouble(4, z);
            ps.setString(5, player.toString());
            ps.executeUpdate();
        }
    }

    private static UUID uuidOrNull(String uuid) {
        return uuid == null ? null : UUID.fromString(uuid);
    }

    private static Double doubleOrNull(ResultSet rs, String column) throws SQLException {
        double d = rs.getDouble(column);
        return rs.wasNull() ? null : d;
    }
}

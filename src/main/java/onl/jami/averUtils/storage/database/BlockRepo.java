package onl.jami.averUtils.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class BlockRepo {
    public record BlockData(UUID playerUuid, UUID blockedUuid, EnumSet<BlockCategory> categories) {}

    public static List<BlockData> getBlocksForPlayer(UUID uuid) throws SQLException {
        String sql = "SELECT * FROM blocks WHERE player_uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<BlockData> blocks = new ArrayList<>();
                while (rs.next()) {
                    blocks.add(new BlockData(
                            uuid,
                            UUID.fromString(rs.getString("blocked_uuid")),
                            BlockCategory.fromMask(rs.getInt("categories"))
                    ));
                }
                return blocks;
            }
        }
    }

    public static void createBlock(UUID playerUuid, UUID blockedUuid, EnumSet<BlockCategory> categories) throws SQLException {
        String sql = "INSERT OR REPLACE INTO blocks VALUES (?, ?, ?)";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, blockedUuid.toString());
            ps.setInt(3, BlockCategory.toMask(categories));
            ps.executeUpdate();
        }
    }

    public static boolean deleteBlock(UUID playerUuid, UUID blockedUuid) throws SQLException {
        String sql = "DELETE FROM blocks WHERE player_uuid = ? AND blocked_uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, blockedUuid.toString());
            return ps.executeUpdate() != 0;
        }
    }
}

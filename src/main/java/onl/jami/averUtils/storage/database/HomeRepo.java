package onl.jami.averUtils.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomeRepo {
    public record HomeData(UUID playerUUID, String name, String world, double x, double y, double z, float pitch, float yaw) {}

    public static boolean createHome(UUID uuid, String name, String world, double x, double y, double z, float pitch, float yaw) throws SQLException {
        String sql = "INSERT OR IGNORE INTO homes (player_uuid, name, world, x, y, z, pitch, yaw) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.setString(3, world);
            ps.setDouble(4, x);
            ps.setDouble(5, y);
            ps.setDouble(6, z);
            ps.setFloat(7, pitch);
            ps.setFloat(8, yaw);
            int created = ps.executeUpdate();
            return created != 0;
        }
    }

    public static HomeRepo.HomeData getHome(UUID uuid, String name) throws SQLException {
        String sql = "SELECT * FROM homes WHERE player_uuid = ? AND name = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new HomeRepo.HomeData(
                        uuid, name,
                        rs.getString("world"),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z"),
                        rs.getFloat("pitch"),
                        rs.getFloat("yaw")
                );
            }
        }
    }

    public static List<HomeData> listHomes(UUID uuid) throws SQLException {
        String sql = "SELECT * FROM homes WHERE player_uuid = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<HomeData> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new HomeData(
                            uuid,
                            rs.getString("name"),
                            rs.getString("world"),
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getFloat("pitch"),
                            rs.getFloat("yaw")
                    ));
                }
                return list;
            }
        }
    }

    public static boolean deleteHome(UUID uuid, String name) throws SQLException {
        String sql = "DELETE FROM homes WHERE player_uuid = ? AND name = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            int deleted = ps.executeUpdate();
            return deleted != 0;
        }
    }
}

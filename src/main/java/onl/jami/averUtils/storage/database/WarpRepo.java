package onl.jami.averUtils.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WarpRepo {
    public record WarpData(String name, String world, double x, double y, double z, float pitch, float yaw) {}

    public static boolean createWarp(String name, String world, double x, double y, double z, float pitch, float yaw) throws SQLException {
        String sql = "INSERT OR IGNORE INTO warps (name, world, x, y, z, pitch, yaw) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, world);
            ps.setDouble(3, x);
            ps.setDouble(4, y);
            ps.setDouble(5, z);
            ps.setFloat(6, pitch);
            ps.setFloat(7, yaw);
            int created = ps.executeUpdate();
            return created != 0;
        }
    }

    public static WarpRepo.WarpData getWarp(String name) throws SQLException {
        String sql = "SELECT * FROM warps WHERE name = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new WarpRepo.WarpData(
                        name,
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

    public static List<WarpRepo.WarpData> listWarps() throws SQLException {
        String sql = "SELECT * FROM warps";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                List<WarpRepo.WarpData> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new WarpRepo.WarpData(
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

    public static boolean  deleteWarp(String name) throws SQLException {
        String sql = "DELETE FROM warps WHERE name = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            int deleted = ps.executeUpdate();
            return deleted != 0;
        }
    }
}

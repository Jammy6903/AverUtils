package onl.jami.averUtils.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MotdRepo {
    public record MotdData(String name, String message, boolean isActive) {}

    public static boolean create(String name, String message) throws SQLException {
        String sql = "INSERT INTO motds (name, message, is_active) VALUES (?, ?, ?)";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, message);
            ps.setBoolean(3, false);
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean edit(String name, String message) throws SQLException {
        String sql = "UPDATE motds SET message = ? WHERE name = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, message);
            ps.setString(2, name);
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean setActive(String name, boolean active) throws SQLException {
        String sql = "UPDATE motds SET is_active = ? WHERE name = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setString(2, name);
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean delete(String name) throws SQLException {
        String sql = "DELETE FROM motds WHERE name = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            return ps.executeUpdate() != 0;
        }
    }

    public static MotdData get(String name) throws SQLException {
        String sql = "SELECT * FROM motds WHERE name = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new MotdData(rs.getString("name"), rs.getString("message"), rs.getBoolean("is_active"));
            }
        }
    }

    public static List<MotdData> list() throws SQLException {
        String sql = "SELECT * FROM motds";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                List<MotdData> motds = new ArrayList<>();
                while (rs.next()) {
                    motds.add(new MotdData(rs.getString("name"), rs.getString("message"), rs.getBoolean("is_active")));
                }
                return motds;
            }
        }
    }

    public static List<MotdData> getActive() throws SQLException {
        String sql = "SELECT * FROM motds WHERE is_active = true";
        List<MotdData> motds = new ArrayList<>();
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    motds.add(new MotdData(rs.getString("name"), rs.getString("message"), rs.getBoolean("is_active")));
                }
                return motds;
            }
        }
    }
}

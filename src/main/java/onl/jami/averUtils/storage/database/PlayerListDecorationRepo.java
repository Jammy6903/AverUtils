package onl.jami.averUtils.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerListDecorationRepo {
    public record PlayerListDecorationData(String name, Location location, String message, boolean isActive) {
        public enum Location { HEADER, FOOTER }
    }

    public static boolean create(String name, PlayerListDecorationData.Location location, String message) throws SQLException {
        String sql = "INSERT INTO player_list_decorations (name, location, message, is_active) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, location.toString());
            ps.setString(3, message);
            ps.setBoolean(4, false);
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean edit(String name, PlayerListDecorationData.Location location, String message) throws SQLException {
        String sql = "UPDATE player_list_decorations SET message = ? WHERE name = ? AND location = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, message);
            ps.setString(2, name);
            ps.setString(3, location.toString());
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean setActive(String name, PlayerListDecorationData.Location location, boolean active) throws SQLException {
        String sql = "UPDATE player_list_decorations SET is_active = ? WHERE name = ? AND location = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setString(2, name);
            ps.setString(3, location.toString());
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean delete(String name, PlayerListDecorationData.Location location) throws SQLException {
        String sql = "DELETE FROM player_list_decorations WHERE name = ? AND location = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, location.toString());
            return ps.executeUpdate() != 0;
        }
    }

    public static PlayerListDecorationData get(String name, PlayerListDecorationData.Location location) throws SQLException {
        String sql = "SELECT * FROM player_list_decorations WHERE name = ? AND location = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, location.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new PlayerListDecorationData(rs.getString("name"), PlayerListDecorationData.Location.valueOf(rs.getString("location")), rs.getString("message"), rs.getBoolean("is_active"));
            }
        }
    }

    public static List<PlayerListDecorationData> list(PlayerListDecorationData.Location location) throws SQLException {
        String sql = "SELECT * FROM player_list_decorations WHERE location = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, location.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<PlayerListDecorationData> player_list_decorations = new ArrayList<>();
                while (rs.next()) {
                    player_list_decorations.add(new PlayerListDecorationData(rs.getString("name"), PlayerListDecorationData.Location.valueOf(rs.getString("location")), rs.getString("message"), rs.getBoolean("is_active")));
                }
                return player_list_decorations;
            }
        }
    }

    public static List<PlayerListDecorationData> getActive(PlayerListDecorationData.Location location) throws SQLException {
        String sql = "SELECT * FROM player_list_decorations WHERE is_active = true AND location = ?";
        List<PlayerListDecorationData> player_list_decorations = new ArrayList<>();
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, location.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    player_list_decorations.add(new PlayerListDecorationData(rs.getString("name"), PlayerListDecorationData.Location.valueOf(rs.getString("location")), rs.getString("message"), rs.getBoolean("is_active")));
                }
                return player_list_decorations;
            }
        }
    }
}

package onl.jami.averUtils.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ScheduleRepo {
    public record ScheduleData(String name, ScheduleType type, String message, long intervalSeconds, boolean isActive) {}

    public static boolean create(String name, ScheduleType type, String message, long intervalSeconds) throws SQLException {
        String sql = "INSERT INTO schedules (name, type, message, interval_seconds) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, type.name());
            ps.setString(3, message);
            ps.setLong(4, intervalSeconds);
                return ps.executeUpdate() != 0;
        }
    }

    public static boolean edit(String name, ScheduleType type, String message, long intervalSeconds) throws SQLException {
        String sql = "UPDATE schedules SET message = ?, interval_seconds = ? WHERE name = ? AND type = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, message);
            ps.setLong(2, intervalSeconds);
            ps.setString(3, name);
            ps.setString(4, type.name());
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean delete(String name, ScheduleType type) throws SQLException {
        String sql = "DELETE FROM schedules WHERE name = ? AND type = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, type.name());
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean setActive(String name, ScheduleType type, boolean active) throws SQLException {
        String sql = "UPDATE schedules set is_active = ? WHERE name = ? AND type = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setString(2, name);
            ps.setString(3, type.name());
            return ps.executeUpdate() != 0;
        }
    }

    public static ScheduleData getByName(String name, ScheduleType type) throws SQLException {
        String sql = "SELECT * FROM schedules WHERE name = ? AND type = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) { return null; }
                return new ScheduleData(
                        name,
                        ScheduleType.valueOf(rs.getString("type")),
                        rs.getString("message"),
                        rs.getLong("interval_seconds"),
                        rs.getBoolean("is_active")
                );
            }
        }
    }

    public static List<ScheduleData> getActive() throws SQLException {
        String sql = "SELECT * FROM schedules WHERE is_active = true";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                List<ScheduleData> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new ScheduleData(
                            rs.getString("name"),
                            ScheduleType.valueOf(rs.getString("type")),
                            rs.getString("message"),
                            rs.getLong("interval_seconds"),
                            rs.getBoolean("is_active")
                    ));
                }
                return list;
            }
        }
    }
}

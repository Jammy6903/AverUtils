package onl.jami.averUtils.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReportRepo {
    public record ReportData(int id, UUID reportedUuid, UUID reporterUuid, String reason, String details, boolean active) {}

    public static void create(UUID reportedUuid, UUID reporterUuid, String reason, String details) throws SQLException {
        String sql = "INSERT INTO reports (reported_uuid, reporter_uuid, reason, details) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, reportedUuid == null ? null : reportedUuid.toString());
            ps.setString(2, reporterUuid.toString());
            ps.setString(3, reason);
            ps.setString(4, details);
            ps.executeUpdate();
        }
    }

    public static ReportData getById(int id) throws SQLException {
        String sql = "SELECT * FROM reports WHERE report_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ReportData(
                            id,
                            uuidOrNull(rs.getString("reported_uuid")),
                            UUID.fromString(rs.getString("reporter_uuid")),
                            rs.getString("reason"),
                            rs.getString("details"),
                            rs.getBoolean("is_active")
                    );
                }
                return null;
            }
        }
    }

    public static List<ReportData> getByActive(boolean active) throws SQLException {
        String sql = "SELECT * FROM reports WHERE is_active = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ResultSet rs = ps.executeQuery();
            List<ReportData> reports = new ArrayList<>();
            while (rs.next()) {
                reports.add(new ReportData(
                        rs.getInt("report_id"),
                        uuidOrNull(rs.getString("reported_uuid")),
                        UUID.fromString(rs.getString("reporter_uuid")),
                        rs.getString("reason"),
                        rs.getString("details"),
                        rs.getBoolean("is_active")
                ));
            }
            return reports;
        }
    }

    public static void updateActive(int id, boolean active) throws SQLException {
        String sql = "UPDATE reports SET is_active = ? WHERE report_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private static UUID uuidOrNull(String uuid) {
        return uuid == null ? null : UUID.fromString(uuid);
    }
}

package onl.jami.averUtils.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Deprecated // TEMPORARILY DEPRECATED
public class RoleLinkRepo {
    public record RoleData(long roleId, String lpGroup) {}

    public static boolean addRole(long roleId, String lpGroup) throws SQLException {
        String sql = "INSERT OR REPLACE INTO role_links VALUES (?, ?)";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setLong(1, roleId);
            ps.setString(2, lpGroup);
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean deleteRole(long roleId) throws SQLException {
        String sql = "DELETE FROM role_links WHERE role_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setLong(1, roleId);
            return ps.executeUpdate() != 0;
        }
    }

   public static RoleData getRole(long roleId) throws SQLException {
        String sql = "SELECT * FROM role_links WHERE role_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setLong(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new RoleData(
                        roleId,
                        rs.getString("lp_group")
                );
            }
        }
   }

   public static List<RoleData> getRoles() throws SQLException {
        String sql = "SELECT * FROM role_links";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                List<RoleData> roles = new ArrayList<>();
                while (rs.next()) {
                    RoleData roleData = new RoleData(
                            rs.getLong("role_id"),
                            rs.getString("lp_group")
                    );
                    roles.add(roleData);
                }
                return roles;
            }
        }
   }
}

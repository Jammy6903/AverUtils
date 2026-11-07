package onl.jami.averUtils.storage.cache;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.RoleLinkRepo;

import java.sql.SQLException;
import java.util.List;

public class RoleLinkCache {
    private static volatile List<RoleLinkRepo.RoleData> roles = List.of();

    public static List<RoleLinkRepo.RoleData> getRoles() {
        return roles;
    }

    public static void updateRoles() {
        try {
            roles = List.copyOf(RoleLinkRepo.getRoles());
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.role_links"), exception);
        }
    }
}

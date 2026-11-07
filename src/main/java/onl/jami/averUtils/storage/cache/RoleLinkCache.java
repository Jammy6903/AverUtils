package onl.jami.averUtils.storage.cache;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.RoleLinkRepo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoleLinkCache {
    private static final Map<Long, RoleLinkRepo.RoleData> roles = new ConcurrentHashMap<>();

    public static List<RoleLinkRepo.RoleData> getRoles() {
        return new ArrayList<>(roles.values());
    }

    public static void updateRoles() {
        try {
            List<RoleLinkRepo.RoleData> roleData = RoleLinkRepo.getRoles();
            for (RoleLinkRepo.RoleData role : roleData) {
                roles.put(role.roleId(), role);
            }
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.role_links"), exception);
        }
    }
}

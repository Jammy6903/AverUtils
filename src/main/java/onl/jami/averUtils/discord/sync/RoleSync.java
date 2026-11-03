package onl.jami.averUtils.discord.sync;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.database.RoleLinkRepo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class RoleSync {

    public static void sync(Player p) {
        Bukkit.getScheduler().runTaskAsynchronously(AverUtils.getPlugin(), () -> {
            clearGroups(p.getUniqueId());
            try {
                PlayerRepo.PlayerData playerData = PlayerRepo.getPlayer(p.getUniqueId());
                if (playerData == null || !playerData.isVerified()) {
                    return;
                }

                List<RoleLinkRepo.RoleData> rolesData = RoleLinkRepo.getRoles();
                Member m = AverUtils.getGuild().getMemberById(playerData.discordId());
                if (m == null) {
                    m = AverUtils.getGuild().retrieveMemberById(playerData.discordId()).complete();
                }
                for (RoleLinkRepo.RoleData roleData : rolesData) {
                    for (Role role : m.getRoles()) {
                        if (role.getIdLong() == roleData.roleId()) {
                            AverUtils.getLuckPermsApi().getUserManager().modifyUser(playerData.uuid(), user -> {
                                user.data().add(InheritanceNode.builder(roleData.lpGroup()).build());
                            });
                        }
                    }
                }
            } catch (SQLException exception) {
                AverUtils.logWarning("Failed to fetch player or role link from database", exception);
            } catch (ErrorResponseException ex) {

            }
        });
    }

    public static void syncOnline() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            sync(p);
        }
    }

    public static void clearGroups(UUID uuid) {
        UserManager um = AverUtils.getLuckPermsApi().getUserManager();
        User user = um.loadUser(uuid).join();
        user.data().clear(NodeType.INHERITANCE.predicate());
        user.data().add(InheritanceNode.builder("default").build());
        um.saveUser(user).join();
    }
}

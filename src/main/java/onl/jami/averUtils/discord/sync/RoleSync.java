package onl.jami.averUtils.discord.sync;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.cache.RoleLinkCache;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.database.RoleLinkRepo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class RoleSync {

    public static void sync(Player p) {
        if (!AverUtils.getCfg().getBoolean("discord.enabled")) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(AverUtils.getPlugin(), () -> {
            clearGroups(p.getUniqueId());

            PlayerRepo.PlayerData playerData = PlayerCache.getPlayer(p);
            if (playerData == null) { return; }

            List<RoleLinkRepo.RoleData> rolesData = RoleLinkCache.getRoles();
            if (rolesData.isEmpty()) { return; }

            Member m = AverUtils.getGuild().getMemberById(playerData.discordId());
            if (m == null) {
                m = AverUtils.getGuild().retrieveMemberById(playerData.discordId()).complete();
            }

            for (RoleLinkRepo.RoleData roleData : rolesData) {
                for (Role role : m.getRoles()) {
                    if (role.getIdLong() == roleData.roleId()) {
                        AverUtils.getLuckPermsApi().getUserManager().modifyUser(playerData.uuid(), user ->
                            user.data().add(InheritanceNode.builder(roleData.lpGroup()).build())
                        );
                    }
                }
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

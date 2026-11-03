package onl.jami.averUtils.discord.sync;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateGlobalNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.database.RoleLinkRepo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class SyncOnChange extends ListenerAdapter {
    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent e) {
        try {
            PlayerRepo.PlayerData playerData = PlayerRepo.getPlayerByDiscordId(e.getUser().getIdLong());
            if (playerData != null) {
                Player player = Bukkit.getPlayer(playerData.uuid());
                if (player == null) { return;}
                RoleSync.sync(player);
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Could not get roles from database", ex);
        }
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent e) {
        try {
            PlayerRepo.PlayerData playerData = PlayerRepo.getPlayerByDiscordId(e.getUser().getIdLong());
            if (playerData != null) {
                Player player = Bukkit.getPlayer(playerData.uuid());
                if (player == null) { return;}
                RoleSync.sync(player);
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Could not get roles from database", ex);
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent e) {
        try {
            PlayerRepo.PlayerData player = PlayerRepo.getPlayerByDiscordId(e.getUser().getIdLong());
            if (player == null) { return; }
            AverUtils.getLuckPermsApi().getUserManager().modifyUser(player.uuid(), user -> {
                user.data().clear(NodeType.INHERITANCE.predicate(
                        node -> !node.getGroupName().equalsIgnoreCase("default")
                ));
            });
            PlayerRepo.deletePlayer(player.uuid());
            Player p = Bukkit.getPlayer(player.uuid());
            if (p != null) {p.kick(Component.text("You have left the Discord server!"));}
        } catch (SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to search for user ", exception);
        }
    }

    @Override
    public void onGuildMemberUpdateTimeOut(GuildMemberUpdateTimeOutEvent e) {

    }

    @Override
    public void onUserUpdateGlobalName(UserUpdateGlobalNameEvent e) {
        try {
            PlayerRepo.PlayerData player = PlayerRepo.getPlayerByDiscordId(e.getUser().getIdLong());
            if (player == null) { return; }
            PlayerRepo.updateDiscordGobalName(player.uuid(), e.getNewGlobalName());
        } catch (SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to search for user ", exception);
        }
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent e) {
        try {
            PlayerRepo.PlayerData player = PlayerRepo.getPlayerByDiscordId(e.getUser().getIdLong());
            if (player == null) { return; }
            PlayerRepo.updateDiscordUserName(player.uuid(), e.getNewName());
        } catch (SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to search for user ", exception);
        }
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent e) {
        try {
            PlayerRepo.PlayerData player = PlayerRepo.getPlayerByDiscordId(e.getUser().getIdLong());
            if (player == null) { return; }
            PlayerRepo.updateDiscordNickname(player.uuid(), e.getNewNickname());
        } catch (SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to search for user ", exception);
        }
    }
}

package onl.jami.averUtils.discord.sync;

import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateGlobalNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.NodeType;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.discord.sync.roles.RoleSync;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.database.PlayerRepo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.UUID;

public class SyncOnChange extends ListenerAdapter {
    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent e) {
        if (!AverUtils.getGuild().getId().equals(e.getGuild().getId())) { return; }
        try {
            PlayerRepo.PlayerData playerData = PlayerRepo.getPlayerByDiscordId(e.getUser().getIdLong());
            if (playerData != null) {
                Player player = Bukkit.getPlayer(playerData.uuid());
                if (player == null) { return;}
                Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> RoleSync.sync(player));
            }
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
        }
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent e) {
        if (!AverUtils.getGuild().getId().equals(e.getGuild().getId())) { return; }
        try {
            PlayerRepo.PlayerData playerData = PlayerRepo.getPlayerByDiscordId(e.getUser().getIdLong());
            if (playerData != null) {
                Player player = Bukkit.getPlayer(playerData.uuid());
                if (player == null) { return;}
                Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> RoleSync.sync(player));
            }
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent e) {
        if (!AverUtils.getGuild().getId().equals(e.getGuild().getId())) { return; }
        try {
            PlayerRepo.PlayerData player = PlayerRepo.getPlayerByDiscordId(e.getUser().getIdLong());
            if (player == null) { return; }
            UUID uuid = player.uuid();

            Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.kick(MiniMessage.miniMessage().deserialize(Messages.getString("player.moderation.verification.left-discord")));

                }
            });

            LuckPerms lp = AverUtils.getLuckPermsApi();
            if (lp != null) {
                lp.getUserManager().modifyUser(player.uuid(), user ->
                        user.data().clear(NodeType.INHERITANCE.predicate(
                                node -> !node.getGroupName().equalsIgnoreCase("default")
                        ))
                );
            }
            PlayerRepo.unVerify(player.uuid());
            PlayerCache.removePlayer(player.uuid());
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
        }
    }

    @Override
    public void onGuildMemberUpdateTimeOut(@NonNull GuildMemberUpdateTimeOutEvent e) {
        if (!AverUtils.getGuild().getId().equals(e.getGuild().getId())) { return; }
    }

    @Override
    public void onUserUpdateGlobalName(UserUpdateGlobalNameEvent e) {
        try {
            PlayerRepo.PlayerData player = PlayerRepo.getPlayerByDiscordId(e.getUser().getIdLong());
            if (player == null) { return; }
            PlayerRepo.updateDiscordGobalName(player.uuid(), e.getNewGlobalName());
            PlayerCache.updatePlayer(player.uuid());
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
        }
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent e) {
        try {
            PlayerRepo.PlayerData player = PlayerRepo.getPlayerByDiscordId(e.getUser().getIdLong());
            if (player == null) { return; }
            PlayerRepo.updateDiscordUserName(player.uuid(), e.getNewName());
            PlayerCache.updatePlayer(player.uuid());
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
        }
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent e) {
        try {
            PlayerRepo.PlayerData player = PlayerRepo.getPlayerByDiscordId(e.getUser().getIdLong());
            if (player == null) { return; }
            PlayerRepo.updateDiscordNickname(player.uuid(), e.getNewNickname());
            PlayerCache.updatePlayer(player.uuid());
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
        }
    }
}

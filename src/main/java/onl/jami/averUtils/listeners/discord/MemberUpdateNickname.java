package onl.jami.averUtils.listeners.discord;

import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.database.PlayerRepo;

import java.sql.SQLException;

public class MemberUpdateNickname extends ListenerAdapter {

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent e) {
        try {
            PlayerRepo.PlayerData player = PlayerRepo.getPlayerByDiscordId(e.getUser().getIdLong());
            if (player == null) {
                return;
            }
            PlayerRepo.updateDiscordNickname(player.uuid(), e.getNewNickname());
            PlayerCache.updatePlayer(player.uuid());
        } catch (SQLException exception) {
            AverUtils.logWarning(Locale.getString("errors.database.players"), exception);
        }
    }
}

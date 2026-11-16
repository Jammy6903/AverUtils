package onl.jami.averUtils.listeners.discord;

import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import onl.jami.averUtils.AverUtils;
import org.jspecify.annotations.NonNull;

public class MemberUpdateTimeout extends ListenerAdapter {

    @Override
    public void onGuildMemberUpdateTimeOut(@NonNull GuildMemberUpdateTimeOutEvent e) {
        if (!AverUtils.getGuild().getId().equals(e.getGuild().getId())) { return; }
    }

}

package onl.jami.averUtils.listeners.discord;

import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.database.PlayerRepo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class MemberRemove extends ListenerAdapter {

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
                    p.kick(MiniMessage.miniMessage().deserialize(Locale.getString("player.moderation.verification.left-discord")));

                }
            });

            PlayerRepo.unVerify(player.uuid());
            PlayerCache.removePlayer(player.uuid());
        } catch (SQLException exception) {
            AverUtils.logWarning(Locale.getString("errors.database.players"), exception);
        }
    }

}

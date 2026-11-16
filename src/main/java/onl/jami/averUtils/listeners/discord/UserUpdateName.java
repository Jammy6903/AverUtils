package onl.jami.averUtils.listeners.discord;

import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.database.PlayerRepo;

import java.sql.SQLException;

public class UserUpdateName extends ListenerAdapter {

    @Override
    public void onUserUpdateName(UserUpdateNameEvent e) {
        try {
            PlayerRepo.PlayerData player = PlayerRepo.getPlayerByDiscordId(e.getUser().getIdLong());
            if (player == null) { return; }
            PlayerRepo.updateDiscordUserName(player.uuid(), e.getNewName());
            PlayerCache.updatePlayer(player.uuid());
        } catch (SQLException exception) {
            AverUtils.logWarning(Locale.getString("errors.database.players"), exception);
        }
    }

}

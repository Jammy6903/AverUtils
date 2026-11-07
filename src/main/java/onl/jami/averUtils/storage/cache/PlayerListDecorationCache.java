package onl.jami.averUtils.storage.cache;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.PlayerListDecorationRepo;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerListDecorationCache {
    private static volatile List<PlayerListDecorationRepo.PlayerListDecorationData> headers = List.of();
    private static volatile List<PlayerListDecorationRepo.PlayerListDecorationData> footers = List.of();

    public static String getRandom(PlayerListDecorationRepo.PlayerListDecorationData.Location location) {
        List<PlayerListDecorationRepo.PlayerListDecorationData> current = headers;
        if (location == PlayerListDecorationRepo.PlayerListDecorationData.Location.FOOTER) {
            current = footers;
        }
        if (current.isEmpty()) { return null; }
        return current.get(ThreadLocalRandom.current().nextInt(0, current.size())).message();
    }

    public static void updateDecorations() {
        try {
            headers = List.copyOf(PlayerListDecorationRepo.getActive(PlayerListDecorationRepo.PlayerListDecorationData.Location.HEADER));
            footers = List.copyOf(PlayerListDecorationRepo.getActive(PlayerListDecorationRepo.PlayerListDecorationData.Location.FOOTER));
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.player_list_decorations"), exception);
        }
    }
}

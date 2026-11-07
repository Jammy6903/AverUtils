package onl.jami.averUtils.storage.cache;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.PlayerListDecorationRepo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerListDecorationCache {
    private static final List<PlayerListDecorationRepo.PlayerListDecorationData> headers = new ArrayList<>();
    private static final List<PlayerListDecorationRepo.PlayerListDecorationData> footers = new ArrayList<>();

    public static PlayerListDecorationRepo.PlayerListDecorationData getRandom(PlayerListDecorationRepo.PlayerListDecorationData.Location location) {
        if (location == PlayerListDecorationRepo.PlayerListDecorationData.Location.HEADER) {
            if (!headers.isEmpty()) {
                if (headers.size() == 1) {
                    return headers.getFirst();
                } else {
                    int random = ThreadLocalRandom.current().nextInt(0, headers.size());
                    return headers.get(random);
                }
            }
        }
        if (!footers.isEmpty()) {
            if (footers.size() == 1) {
                return footers.getFirst();
            } else {
                int random = ThreadLocalRandom.current().nextInt(0, headers.size());
                return footers.get(random);
            }
        }
        return null;
    }

    public static void updateDecorations() {
        try {
            headers.clear();
            footers.clear();
            headers.addAll(PlayerListDecorationRepo.getActive(PlayerListDecorationRepo.PlayerListDecorationData.Location.HEADER));
            footers.addAll(PlayerListDecorationRepo.getActive(PlayerListDecorationRepo.PlayerListDecorationData.Location.FOOTER));
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.player_list_decorations"), exception);
        }
    }
}

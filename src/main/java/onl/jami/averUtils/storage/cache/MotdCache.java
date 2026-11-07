package onl.jami.averUtils.storage.cache;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.MotdRepo;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MotdCache {
    private static volatile List<MotdRepo.MotdData> motds = List.of();

    public static MotdRepo.MotdData getRandom() {
        List<MotdRepo.MotdData> current = motds;
        if (current.isEmpty()) { return null; }
        return current.get(ThreadLocalRandom.current().nextInt(current.size()));
    }

    public static void updateMotds() {
        try {
            motds = List.copyOf(MotdRepo.getActive());
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.motds"), exception);
        }
    }
}

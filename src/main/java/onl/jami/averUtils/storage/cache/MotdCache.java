package onl.jami.averUtils.storage.cache;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.MotdRepo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MotdCache {
    private static final List<MotdRepo.MotdData> motds = new ArrayList<>();

    public static MotdRepo.MotdData getRandom() {
        if (motds.isEmpty()) { return null; }
        if (motds.size() == 1) {
            return motds.getFirst();
        }
        int random = ThreadLocalRandom.current().nextInt(0, motds.size());
        return motds.get(random);
    }

    public static void updateMotds() {
        try {
            motds.clear();
            motds.addAll(MotdRepo.getActive());
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.motds"), exception);
        }
    }
}

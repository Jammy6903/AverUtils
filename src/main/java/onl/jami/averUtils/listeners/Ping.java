package onl.jami.averUtils.listeners;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.database.MotdRepo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Ping implements Listener {

    @EventHandler
    public void onPing(PaperServerListPingEvent e){
        try {
            // cache this
            List<MotdRepo.MotdData> motds = MotdRepo.getActive();
            if (motds.isEmpty()) { return; }
            if (motds.size() == 1) {
                e.motd(MiniMessage.miniMessage().deserialize(motds.getFirst().message()));
                return;
            }
            int random = ThreadLocalRandom.current().nextInt(0, motds.size());
            e.motd(MiniMessage.miniMessage().deserialize(motds.get(random).message()));
        } catch (SQLException exception) {
            AverUtils.logWarning("Failed to fetch motds from database", exception);
        }
    }
}

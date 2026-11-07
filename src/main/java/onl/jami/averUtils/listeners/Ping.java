package onl.jami.averUtils.listeners;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import onl.jami.averUtils.storage.cache.MotdCache;
import onl.jami.averUtils.storage.database.MotdRepo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Ping implements Listener {

    @EventHandler
    public void onPing(PaperServerListPingEvent e){
        MotdRepo.MotdData motd = MotdCache.getRandom();
        if (motd != null) {
            e.motd(MiniMessage.miniMessage().deserialize(motd.message()));
        }
    }
}

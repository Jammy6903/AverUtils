package onl.jami.averUtils.listeners;

import onl.jami.averUtils.managers.PunishmentManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PlayerPreLogin implements Listener {

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        PunishmentManager.onJoin(event);
    }

}

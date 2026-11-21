package onl.jami.averUtils.listeners;

import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerSpawn implements Listener {

    private static Location centredSpawn() {
        return Bukkit.getWorlds().getFirst().getSpawnLocation().add(0.5, 0, 0.5);
    }

    @EventHandler
    public void onFirstJoin(AsyncPlayerSpawnLocationEvent event) {
        if (event.isNewPlayer()) {
            event.setSpawnLocation(centredSpawn());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn() && !event.isAnchorSpawn()) {
            event.setRespawnLocation(centredSpawn());
        }
    }
}

package onl.jami.averUtils.listeners;

import onl.jami.averUtils.discord.chatlink.MinecraftToDiscord;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeath implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        /*
        Component joinMessage = Messages.getRandom("player.death.messages",
                Placeholder.component("name", PlayerName.getComponent(e.getPlayer(), false)));

        e.deathMessage(Messages.getComponent("player.death.format.message",
                Placeholder.component("message", joinMessage)));
         */

        // send death message to any linked discord channels
        MinecraftToDiscord.sendPlayerDeathMessage(e.deathMessage());
    }

}

package onl.jami.averUtils.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        Component formattedMessage = ChatFormatter.formatChat(e.getPlayer(), e.message(), Component.empty());
        e.message(formattedMessage);
        e.renderer(((source, sourceDisplayName, message, viewer) -> message));
    }
}

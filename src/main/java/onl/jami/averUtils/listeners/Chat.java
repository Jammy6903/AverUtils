package onl.jami.averUtils.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import onl.jami.averUtils.chat.Formatter;
import onl.jami.averUtils.discord.chatlink.MinecraftToDiscord;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Chat implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        MinecraftToDiscord.sendChatMessage(e);
        Component formattedMessage = Formatter.formatChat(e.getPlayer(), e.message(), Component.empty());
        e.message(formattedMessage);
        e.renderer(((source, sourceDisplayName, message, viewer) -> message));
    }
}

package onl.jami.averUtils.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.social.chat.Formatter;
import onl.jami.averUtils.discord.sync.chat.MinecraftToDiscord;
import onl.jami.averUtils.storage.cache.BlockCache;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.database.BlockCategory;
import onl.jami.averUtils.storage.database.BlockRepo;
import onl.jami.averUtils.storage.database.PlayerRepo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        if (AverUtils.getCfg().getBoolean("discord.enabled") && AverUtils.getCfg().getBoolean("discord.require-verification")) {
            PlayerRepo.PlayerData playerData = PlayerCache.getPlayer(e.getPlayer());
            if (playerData == null || !playerData.isVerified()) {
                e.setCancelled(true);
                return;
            }
        }

        // send message to linked discord channels
        MinecraftToDiscord.sendChatMessage(e);

        // format chat message
        Component formattedMessage = Formatter.formatChat(e.getPlayer(), e.message());
        e.message(formattedMessage);
        if (!e.getPlayer().hasPermission("averutils.bypass-block")) {
            e.viewers().removeIf(audience -> {
                if (audience instanceof Player p) {
                    BlockRepo.BlockData block = BlockCache.getBlockForPlayers(p.getUniqueId(), e.getPlayer().getUniqueId());
                    return block != null && block.categories().contains(BlockCategory.CHAT);
                }
                return false;
            });
        }
        e.renderer(((_, _, message, _) -> message));
    }
}

package onl.jami.averUtils.listeners;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.database.PlayerRepo;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;

public class CommandListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String label = e.getMessage().substring(1).split(" ")[0].toLowerCase(Locale.ROOT);
        Command cmd = Bukkit.getCommandMap().getCommand(label);
        if (cmd instanceof PluginCommand pc &&
                pc.getPlugin() == AverUtils.getPlugin() &&
                cmd.getName().equals("verify")
        ) { return; }
        PlayerRepo.PlayerData playerData = PlayerCache.getPlayer(e.getPlayer());
        if (AverUtils.getCfg().getBoolean("discord.require-verification") && (playerData == null || !playerData.isVerified())) {
            e.setCancelled(true);
        }
    }
}

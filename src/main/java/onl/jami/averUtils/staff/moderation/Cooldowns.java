package onl.jami.averUtils.staff.moderation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Cooldowns implements Listener {

    private record CooldownKey(UUID uuid, Category category) {
        enum Category {
            CHAT,
            COMMANDS,
            COMBAT
        }
    }

    private static long chatCooldown;
    private static Map<String, Long> commandCooldowns;
    private static long combatCooldown;
    private static List<String> combatCommands;

    private static void loadCooldowns() {
        Configuration config = AverUtils.getCfg();

        chatCooldown = 0L;
        String chatString = config.getString("cooldowns.chat.standard");
        if (chatString != null) {
            Long chat = TimeUtils.parseTime(chatString);
            if (chat != null) {
                chatCooldown = chat;
            }
        }

        combatCooldown = 0L;
        String combatString = config.getString("cooldowns.combat.cooldown");
        List<String> commandList = config.getStringList("cooldowns.combat.commands");
        if (combatString != null) {
            Long combat = TimeUtils.parseTime(combatString);
            if (combat != null) {
                combatCooldown = combat;
                combatCommands = commandList;
            }
        }

        ConfigurationSection commandSection = config.getConfigurationSection("cooldowns.commands");
        if (commandSection != null) {
            Set<String> commands = commandSection.getKeys(false);
            for (String command : commands) {
                Long commandCooldown = 0L;
                String commandString = commandSection.getString(command);
                if (commandString != null) {
                    Long time = TimeUtils.parseTime(commandString);
                    if (time != null) {
                        commandCooldown = time;
                    }
                }
                commandCooldowns.put(command, commandCooldown);
            }
        }
    }

    private static final Cache<CooldownKey, Long> COOLDOWNS = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("averutils.ignore-cooldowns")) { return; }

        long now = System.currentTimeMillis();

        CooldownKey key = new CooldownKey(player.getUniqueId(), CooldownKey.Category.CHAT);
        Long cooldownEnd = COOLDOWNS.getIfPresent(key);
        if (cooldownEnd == null) {
            COOLDOWNS.put(key, now + chatCooldown);
            return;
        }

        if (cooldownEnd > now) {
            Messages.send(player, "cooldowns.chat.standard",
                    Placeholder.component("remaining", Component.text(TimeUtils.formatRemainingTimeUntil(cooldownEnd))));
            event.setCancelled(true);
            return;
        }

        COOLDOWNS.put(key, now + chatCooldown);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String token = event.getMessage().replace("/", "").split(" ")[0];
        Command command = Bukkit.getCommandMap().getCommand(token);
        if (command == null) { return; }
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) { return; }
        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof Projectile projectile
                    && projectile.getShooter() instanceof Player p) {
            attacker = p;
        } else { return; }

        long now = System.currentTimeMillis();

        COOLDOWNS.put(new CooldownKey(attacker.getUniqueId(), CooldownKey.Category.COMBAT), now + combatCooldown);
        Messages.send(attacker, "cooldowns.combat.on-combat");

        if (AverUtils.getCfg().getBoolean("cooldowns.combat.cooldown-both-attacker-and-victim")) {
            COOLDOWNS.put(new CooldownKey(victim.getUniqueId(), CooldownKey.Category.COMBAT), now + combatCooldown);
            Messages.send(victim, "cooldowns.combat.on-combat");
        }
    }
}

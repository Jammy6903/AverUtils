package onl.jami.averUtils.managers;

import net.kyori.adventure.text.Component;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.database.ChannelLinkCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager {
    private static volatile Map<String, ChatChannel> channels = Map.of();
    private static volatile String defaultId = "global";
    private static final Map<UUID, String> ACTIVE = new ConcurrentHashMap<>();

    record ChatChannel(
            String id,
            String format,
            String speakPerm,
            String listenPerm,
            double range,
            Scope scope,
            ChannelLinkCategory discordCategory,
            String shortcut
    ) {
        static ChatChannel fromConfig(String id, ConfigurationSection s) {
            if (s == null) { return null; }
            String format = s.getString("format");
            if (format == null || format.isBlank()) {
                AverUtils.logWarning("Chat channel '" + id + "' has no format, skipping");
                return null;
            }

            Scope scope;
            try {
                scope = Scope.valueOf(s.getString("scope", "ALL").toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                AverUtils.logWarning("Chat channel '" + id + "' has an invalid scope, skipping");
                return null;
            }
            double range = s.getDouble("range", -1);
            if (scope.equals(scope.RANGE) && range <= 0) {
                AverUtils.logWarning("Chat channel '" + id + "' uses RANGE but has no positive range, skipping");
                return null;
            }

            String discordCategory = s.getString("discord-category", "CHAT");
            ChannelLinkCategory discord = discordCategory.isBlank()? null : ChannelLinkCategory.valueOf(discordCategory.toUpperCase(Locale.ROOT));

            return new ChatChannel(
                    id,
                    format,
                    s.getString("speak-permission", ""),
                    s.getString("listen-permission", ""),
                    range,
                    scope,
                    discord,
                    s.getString("shortcut", "")
            );
        }
    }

    public enum Scope {
        ALL,
        WORLD,
        RANGE
    }

    public static void load() {
        Map<String, ChatChannel> built = new LinkedHashMap<>();
        ConfigurationSection root = AverUtils.getCfg().getConfigurationSection("chat.channels");
        if (root != null) {
            for (String id : root.getKeys(false)) {
                ChatChannel channel = ChatChannel.fromConfig(id, root.getConfigurationSection(id));
                if (channel != null) {
                    built.put(id.toLowerCase(Locale.ROOT), channel);
                }
            }
        }
        if (built.isEmpty()) {
            built.put("global", new ChatChannel(
                                        "global",
                                        "chat.format.message",
                                        "",
                                        "",
                                        -1,
                                        Scope.ALL,
                                        ChannelLinkCategory.CHAT,
                                        "!"
                                    )
                            );
        }
        channels = Map.copyOf(built);
        defaultId = AverUtils.getPlugin().getConfig().getString("chat.default-channel");
        if (defaultId == null || defaultId.isEmpty()) { defaultId = "global"; }
    }

    private static boolean canSpeak() {
        return true;
    }

    private static ChatChannel resolveChannel() {
        return null;
    }

    private static List<Player> recipients() {
        return null;
    }

    private static Component format() {
        return null;
    }

    private static void render() {}
}

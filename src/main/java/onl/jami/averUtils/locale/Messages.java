package onl.jami.averUtils.locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import onl.jami.averUtils.AverUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Messages {

    private static YamlConfiguration locale;
    private static YamlConfiguration fallbackLocale;

    public static void load(AverUtils plugin) {
        File file = new File(plugin.getDataFolder(), "locale/en_US.yml");
        if (!file.exists()) {
            plugin.saveResource("locale/en_US.yml", false);
        }

        String name = plugin.getConfig().getString("locale");
        File chosen = new File(plugin.getDataFolder(), "locale/" + name + ".yml");
        locale = YamlConfiguration.loadConfiguration(chosen);

        try (InputStreamReader reader = new InputStreamReader(plugin.getResource("locale/en_US.yml"), StandardCharsets.UTF_8)) {
            fallbackLocale =  YamlConfiguration.loadConfiguration(reader);
        } catch (IOException exception) {
            AverUtils.logWarning("Could not load the bundles locale", exception);
            fallbackLocale = new YamlConfiguration();
        }
    }

    public static String getString(String key) {
        return locale.getString(key);
    }

    public static Component getComponent(String key, TagResolver... tagResolvers) {
        String raw = locale.getString(key);
        if (raw == null) { raw = fallbackLocale.getString(key); }
        if (raw == null) { raw = key; }
        return MiniMessage.miniMessage().deserialize(raw, tagResolvers);
    }

    public static void send(CommandSender sender, String key, TagResolver... tagResolvers) {
        sender.sendMessage(getComponent(key, tagResolvers));
    }

    public static Component getRandom(String key, TagResolver... tagResolvers) {
        List<String> raw = locale.getStringList(key);
        int random = ThreadLocalRandom.current().nextInt(0, raw.size());
        return MiniMessage.miniMessage().deserialize(raw.get(random), tagResolvers);
    }

    public static String getAll(String key) {
        List<String> raw = locale.getStringList(key);
        StringBuilder sb = new StringBuilder();
        for (String line : raw) {
            sb.append(line).append("<newline>");
        }
        return sb.toString();
    }

    public static List<String> getList(String key) {
        return locale.getStringList(key);
    }
}

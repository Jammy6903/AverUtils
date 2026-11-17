package onl.jami.averUtils.storage.cache;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.managers.ServerIconManager;
import org.bukkit.Bukkit;
import org.bukkit.util.CachedServerIcon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class ServerIconCache {
    private static volatile List<CachedServerIcon> icons = List.of();

    public static CachedServerIcon getRandom() {
        List<CachedServerIcon> current = icons;
        if (current.isEmpty()) { return null; }
        return current.get(ThreadLocalRandom.current().nextInt(current.size()));
    }

    public static void load() {
        try {
            ServerIconManager.init();
        } catch (IOException e) {
            AverUtils.logWarning("Could not create icon folders", e);
            return;
        }

        File[] files = ServerIconManager.folder().toFile().listFiles((_, name) -> name.toLowerCase(Locale.ROOT).endsWith(".png"));

        List<CachedServerIcon> loaded = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                try {
                    loaded.add(Bukkit.loadServerIcon(file));
                } catch (Exception e) {
                    AverUtils.logWarning("Skipping icon " + file.getName(), e);
                }
            }
        }
        icons = List.copyOf(loaded);
    }
}

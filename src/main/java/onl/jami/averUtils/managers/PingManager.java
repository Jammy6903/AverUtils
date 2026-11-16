package onl.jami.averUtils.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class PingManager {

    public static void updatePingCompletions(Player player, boolean joining) {
        String tag = "@" + player.getName();
        if (joining) {
            List<String> everyone = Bukkit.getOnlinePlayers().stream()
                    .map(p -> "@" + p.getName()).toList();

            if (player.hasPermission("averutils.ping")) {
                player.setCustomChatCompletions(everyone);
            }
            if (!VanishManager.hasVanishedState(player)) {
                for (Player other : Bukkit.getOnlinePlayers()) {
                    if (!other.equals(player) && other.hasPermission("averutils.ping")) {
                        other.addCustomChatCompletions(List.of(tag));
                    }
                }
            }
        } else {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(player) && other.hasPermission("averutils.ping")) {
                    other.removeCustomChatCompletions(List.of(tag));
                }
            }
        }
    }

    private static void canPing(Player pinger, Player target) {

    }

}

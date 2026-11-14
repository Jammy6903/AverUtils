package onl.jami.averUtils.staff.moderation.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.userconfig.PlayerName;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SocialSpy implements TabExecutor {

    public static final NamespacedKey SPY = new NamespacedKey(AverUtils.getPlugin(), "social_spy");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        boolean spyState;
        Boolean current = p.getPersistentDataContainer().get(SPY, PersistentDataType.BOOLEAN);
        spyState = Objects.requireNonNullElse(current, false);
        p.getPersistentDataContainer().set(SPY, PersistentDataType.BOOLEAN, !spyState);

        String key = "enabled";
        if (spyState) {
            key = "disabled";
        }

        Messages.send(sender, "commands.socialspy.success." + key);
        return true;
    }

    public static void sendToSpies(CommandSender sender, CommandSender recipient, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Boolean spyState = player.getPersistentDataContainer().get(SPY, PersistentDataType.BOOLEAN);
            if (spyState != null && spyState) {
                if (player.hasPermission("averutils.moderation.socialspy")) {
                    Component senderName = Component.text("CONSOLE");
                    Component recipientName = Component.text("CONSOLE");

                    if (sender instanceof Player p) {
                        if (player.equals(p)) { continue; }
                        senderName = PlayerName.getComponent(p, false);
                    }

                    if (recipient instanceof Player p) {
                        if (player.equals(p)) { continue; }
                        recipientName = PlayerName.getComponent(p, false);
                    }

                    Messages.send(player, "moderation.socialspy.format",
                            Placeholder.component("sender", senderName),
                            Placeholder.component("recipient", recipientName),
                            Placeholder.component("message", Component.text(message))
                    );
                }
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

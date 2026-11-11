package onl.jami.averUtils.staff.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.userconfig.PlayerName;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SocialSpy implements TabExecutor {

    public static final List<Player> SPIES = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        if (SPIES.contains(p)) {
            SPIES.remove(p);
            Messages.send(sender, "commands.socialspy.disabled");
            return true;
        }

        SPIES.add(p);
        Messages.send(sender, "commands.socialspy.enabled");
        return true;
    }

    public static void sendToSpies(CommandSender sender, CommandSender recipient, String message) {
        for (Player spy : SPIES) {
            if (spy.hasPermission("averutils.moderation.socialspy")) {
                Component senderName = Component.text("CONSOLE");
                Component recipientName = Component.text("CONSOLE");

                if (sender instanceof Player p) {
                    if (spy == p) { return; }
                    senderName = PlayerName.getComponent(p, false);
                }

                if (recipient instanceof Player p) {
                    if (spy == p) { return; }
                    recipientName = PlayerName.getComponent(p, false);
                }

                Messages.send(spy, "moderation.socialspy.format",
                        Placeholder.component("sender", senderName),
                        Placeholder.component("recipient", recipientName),
                        Placeholder.component("message", Component.text(message))
                );
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}

package onl.jami.averUtils.staff.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.locale.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DoAs implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length < 2) {
            return false;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            Messages.send(sender, "commands.doas.failure.player-not-found");
            return true;
        }
        String c = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        player.performCommand(c);
        Messages.send(sender, "commands.doas.success",
                Placeholder.component("player", Component.text(player.getName())),
                Placeholder.component("command", Component.text(c))
        );
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
            return completions;
        }
        return List.of();
    }
}

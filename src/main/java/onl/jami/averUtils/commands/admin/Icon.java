package onl.jami.averUtils.commands.admin;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.managers.ServerIconManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.util.List;

public class Icon implements TabExecutor {

    private static final List<String> SUBCOMMANDS = List.of("list", "enable", "disable", "add");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) { return false; }

        switch (args[0].toLowerCase(java.util.Locale.ROOT)) {
            case "list" -> Locale.send(sender, "commands.icon.success.list",
                    Placeholder.unparsed("enabled", String.join(", ", ServerIconManager.list(true))),
                    Placeholder.unparsed("disabled", String.join(", ", ServerIconManager.list(false)))
            );
            case "enable", "disable" -> {
                if (args.length < 2) { return false; }
                boolean enable = args[0].equalsIgnoreCase("enable");
                try {
                    ServerIconManager.setEnabled(args[1], enable);
                    Locale.send(sender, enable ? "commands.icon.success.enabled" : "commands.icon.success.disabled",
                            Placeholder.unparsed("name", args[1]));
                } catch (NoSuchFileException e) {
                    Locale.send(sender, "commands.icon.failure.not-found",
                            Placeholder.unparsed("name", args[1]));
                } catch (FileAlreadyExistsException e) {
                    Locale.send(sender, "commands.icon.failure.exists",
                            Placeholder.unparsed("name", args[1]));
                } catch (IllegalArgumentException e) {
                    Locale.send(sender, "commands.icon.failure.invalid",
                            Placeholder.unparsed("error", e.getMessage()));
                } catch (IOException e) {
                    AverUtils.logWarning("Could not move icon", e);
                    Locale.send(sender, "commands.generic.failure.unknown");
                }
            }
            case "add" -> {
                if (args.length < 2) { return false; }
                String link = args[1];
                String name = args.length > 2 ? args[2] : null;
                Locale.send(sender, "commands.icon.downloading");
                Bukkit.getScheduler().runTaskAsynchronously(AverUtils.getPlugin(), () -> {
                    try {
                        String saved = ServerIconManager.add(link, name);
                        Locale.send(sender, "commands.icon.success.added",
                                Placeholder.unparsed("name", saved));
                    } catch (FileAlreadyExistsException e) {
                        Locale.send(sender, "commands.icon.failure.exists", Placeholder.unparsed("name", e.getMessage()));
                    } catch (IllegalArgumentException | IOException e) {
                        Locale.send(sender, "commands.icon.failure.invalid", Placeholder.unparsed("error", String.valueOf(e.getMessage())));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            default -> { return false; }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        String current = args[args.length - 1].toLowerCase(java.util.Locale.ROOT);
        List<String> options = List.of();
        if (args.length == 1) {
            options = SUBCOMMANDS;
        } else if (args.length == 2) {
            switch (args[0].toLowerCase(java.util.Locale.ROOT)) {
                case "enable" -> options = ServerIconManager.list(false);
                case "disable" -> options = ServerIconManager.list(true);
            }
        }
        return options.stream().filter(s -> s.toLowerCase(java.util.Locale.ROOT).startsWith(current)).toList();
    }
}

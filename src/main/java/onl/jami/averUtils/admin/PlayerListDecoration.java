package onl.jami.averUtils.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.cache.PlayerListDecorationCache;
import onl.jami.averUtils.storage.database.PlayerListDecorationRepo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerListDecoration implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            if (sender instanceof Player p) {
                // Show gui
            }
            return true;
        }
        PlayerListDecorationRepo.PlayerListDecorationData.Location location = PlayerListDecorationRepo.PlayerListDecorationData.Location.valueOf(args[1]);
        switch (args[0].toLowerCase()) {
            case "create":
                create(sender, args, location);
                break;
            case "edit":
                edit(sender, args, location);
                break;
            case "delete":
                delete(sender, args, location);
                break;
            case "set":
                set(sender, args, location);
                break;
            default:
                return false;
        }
        PlayerListDecorationCache.updateDecorations();
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) { return List.of("create", "edit", "delete", "set"); }
        if (args.length == 2) { return List.of("HEADER", "FOOTER"); }
        String sub = args[0].toLowerCase();
        if (args.length == 3 && (sub.equals("edit") || sub.equals("delete") || sub.equals("set"))) {
            List<String> completions = new ArrayList<>();
            try {
                List<PlayerListDecorationRepo.PlayerListDecorationData> decorations = PlayerListDecorationRepo.list(PlayerListDecorationRepo.PlayerListDecorationData.Location.valueOf(args[1].toUpperCase()));
                for (PlayerListDecorationRepo.PlayerListDecorationData decoration : decorations) {
                    completions.add(decoration.name());
                }
                return completions;
            } catch (SQLException exception) {
                AverUtils.logWarning(Messages.getString("errors.database.player_list_decorations"), exception);
            }
        }
        return List.of();
    }

    private static void create(CommandSender sender, String[] args, PlayerListDecorationRepo.PlayerListDecorationData.Location location) {
        if (args.length < 4) {
            Messages.send(sender, "commands.playerlistdecoration.create.failure.wrong-args");
            return;
        }
        try {
            String message = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            if (!PlayerListDecorationRepo.create(args[2], location, message)) {
                Messages.send(sender, "commands.playerlistdecoration.create.failure.already-exists",
                        Placeholder.component("location", Component.text(args[1])),
                        Placeholder.component("name", Component.text(args[2]))
                );
                return;
            }
            Messages.send(sender, "commands.playerlistdecoration.create.success",
                    Placeholder.component("location", Component.text(args[1])),
                    Placeholder.component("name", Component.text(args[2])),
                    Placeholder.component("decoration", MiniMessage.miniMessage().deserialize(message))
            );
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.player_list_decorations"), exception);
            Messages.send(sender, "commands.generic.failure.unknown");
        }
    }

    private static void edit(CommandSender sender, String[] args, PlayerListDecorationRepo.PlayerListDecorationData.Location location) {
        if (args.length < 4) {
            Messages.send(sender, "commands.playerlistdecoration.edit.failure.wrong-args");
            return;
        }
        try {
            String message = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            if (!PlayerListDecorationRepo.edit(args[2], location, message)) {
                Messages.send(sender, "commands.playerlistdecoration.edit.failure.doesnt-exist",
                        Placeholder.component("location", Component.text(args[1])),
                        Placeholder.component("name", Component.text(args[2]))
                );
                return;
            }
            Messages.send(sender, "commands.playerlistdecoration.edit.success",
                    Placeholder.component("location", Component.text(args[1])),
                    Placeholder.component("name", Component.text(args[2])),
                    Placeholder.component("decoration", MiniMessage.miniMessage().deserialize(message))
            );        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.player_list_decorations"), exception);
            Messages.send(sender, "commands.generic.failure.unknown");
        }
    }

    private static void delete(CommandSender sender, String[] args, PlayerListDecorationRepo.PlayerListDecorationData.Location location) {
        if (args.length < 3) {
            Messages.send(sender, "commands.playerlistdecoration.delete.failure.wrong-args");
            return;
        }
        try {
            if (!PlayerListDecorationRepo.delete(args[2], location)) {
                Messages.send(sender, "commands.playerlistdecoration.delete.failure.already-exists",
                        Placeholder.component("location", Component.text(args[1])),
                        Placeholder.component("name", Component.text(args[2]))
                );
                return;
            }
            Messages.send(sender, "commands.playerlistdecoration.delete.success",
                    Placeholder.component("location", Component.text(args[1])),
                    Placeholder.component("name", Component.text(args[2]))
            );
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.player_list_decorations"), exception);
            Messages.send(sender, "commands.generic.failure.unknown");
        }
    }

    private static void set(CommandSender sender, String[] args, PlayerListDecorationRepo.PlayerListDecorationData.Location location) {
        if (args.length < 3) {
            Messages.send(sender, "commands.playerlistdecoration.set.failure.wrong-args");
            return;
        }
        try {
            if (args.length >= 4) {
                if (!PlayerListDecorationRepo.setActive(args[2], location, Boolean.parseBoolean(args[3]))){
                    Messages.send(sender, "commands.playerlistdecoration.set.success",
                            Placeholder.component("location", Component.text(args[1])),
                            Placeholder.component("name", Component.text(args[2])),
                            Placeholder.component("state", Component.text(String.valueOf(args[3])))

                    );
                    return;
                }
                Messages.send(sender, "commands.playerlistdecoration.set.success",
                        Placeholder.component("location", Component.text(args[1])),
                        Placeholder.component("name", Component.text(args[2])),
                        Placeholder.component("state", Component.text(String.valueOf(args[3])))
                );
                return;
            }
            PlayerListDecorationRepo.PlayerListDecorationData decoration = PlayerListDecorationRepo.get(args[2], location);
            if (decoration == null) {
                Messages.send(sender, "commands.playerlistdecoration.delete.failure.doesnt-exist",
                        Placeholder.component("location", Component.text(args[1])),
                        Placeholder.component("name", Component.text(args[2]))
                );
                return;
            }
            boolean active = !decoration.isActive();
            PlayerListDecorationRepo.setActive(args[2], location, active);
            Messages.send(sender, "commands.playerlistdecoration.set.success",
                    Placeholder.component("location", Component.text(args[1])),
                    Placeholder.component("name", Component.text(args[2])),
                    Placeholder.component("state", Component.text(String.valueOf(active)))
            );
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.player_list_decorations"), exception);
            Messages.send(sender, "commands.generic.failure.unknown");
        }
    }
}

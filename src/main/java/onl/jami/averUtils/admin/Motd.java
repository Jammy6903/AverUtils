package onl.jami.averUtils.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.listeners.Player;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.cache.MotdCache;
import onl.jami.averUtils.storage.database.MotdRepo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Motd implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            if (sender instanceof Player p) {
                // Show gui
            }
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create":
                create(sender, args);
                break;
            case "edit":
                edit(sender, args);
                break;
            case "delete":
                delete(sender, args);
                break;
            case "set":
                set(sender, args);
                break;
            default:
                return false;
        }
        MotdCache.updateMotds();
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) { return List.of("create", "edit", "delete", "set"); }
        String sub = args[0].toLowerCase();
        if (args.length == 2 && (sub.equals("edit") || sub.equals("delete") || sub.equals("set"))) {
            List<String> completions = new ArrayList<>();
            try {
                List<MotdRepo.MotdData> motds = MotdRepo.list();
                for (MotdRepo.MotdData motd : motds) {
                    completions.add(motd.name());
                }
                return completions;
            } catch (SQLException exception) {
                AverUtils.logWarning(Messages.getString("errors.database.motds"), exception);
            }
        }
        return List.of();
    }

    private static void create(CommandSender sender, String[] args) {
        if (args.length < 3) {
            Messages.send(sender, "commands.motd.create.failure.wrong-args");
            return;
        }
        try {
            String motd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            if (!MotdRepo.create(args[1], motd)) {
                Messages.send(sender, "commands.motd.create.failure.already-exists",
                        Placeholder.component("name", Component.text(args[1]))
                );
                return;
            }
            Messages.send(sender, "commands.motd.create.success",
                    Placeholder.component("name", Component.text(args[1])),
                    Placeholder.component("motd", MiniMessage.miniMessage().deserialize(motd))
            );
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.motds"), exception);
            Messages.send(sender, "commands.generic.failure.unknown");
        }
    }

    private static void edit(CommandSender sender, String[] args) {
        if (args.length < 3) {
            Messages.send(sender, "commands.motd.edit.failure.wrong-args");
            return;
        }
        try {
            String motd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            if (!MotdRepo.edit(args[1], motd)) {
                Messages.send(sender, "commands.motd.edit.failure.doesnt-exist",
                        Placeholder.component("name", Component.text(args[1]))
                );
                return;
            }
            Messages.send(sender, "commands.motd.edit.success",
                    Placeholder.component("name", Component.text(args[1])),
                    Placeholder.component("motd", MiniMessage.miniMessage().deserialize(motd))
            );
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.motds"), exception);
            Messages.send(sender, "commands.generic.failure.unknown");
        }
    }

    private static void delete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Messages.send(sender, "commands.motd.delete.failure.wrong-args");
            return;
        }
        try {
            if (!MotdRepo.delete(args[1])) {
                Messages.send(sender, "commands.motd.delete.failure.doesnt-exist",
                        Placeholder.component("name", Component.text(args[1]))
                );
                return;
            }
            Messages.send(sender, "commands.motd.delete.success",
                    Placeholder.component("name", Component.text(args[1]))
            );
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.motds"), exception);
            Messages.send(sender, "commands.generic.failure.unknown");
        }
    }

    private static void set(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Messages.send(sender, "commands.motd.set.failure.wrong-args");
            return;
        }
        try {
            if (args.length >= 3) {
                if (!MotdRepo.setActive(args[1], Boolean.parseBoolean(args[2]))){
                    Messages.send(sender, "commands.motd.set.success",
                            Placeholder.component("name", Component.text(args[1])),
                            Placeholder.component("state", Component.text(String.valueOf(args[3])))

                    );
                    return;
                }
                Messages.send(sender, "commands.motd.set.success",
                        Placeholder.component("name", Component.text(args[1])),
                        Placeholder.component("state", Component.text(args[2].toLowerCase()))
                );
                return;
            }
            MotdRepo.MotdData motd = MotdRepo.get(args[1]);
            if (motd == null) {
                Messages.send(sender, "commands.motd.set.failure.doesnt-exist",
                        Placeholder.component("name", Component.text(args[1]))
                );
                return;
            }
            boolean active = !motd.isActive();
            MotdRepo.setActive(args[1], active);
            Messages.send(sender, "commands.motd.set.success",
                    Placeholder.component("name", Component.text(args[1])),
                    Placeholder.component("state", Component.text(String.valueOf(active)))
            );
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.motds"), exception);
            Messages.send(sender, "commands.generic.failure.unknown");
        }
    }
}

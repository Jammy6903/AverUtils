package onl.jami.averUtils.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.listeners.Player;
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
        }
        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /motd create [name] [message]"));
                    return true;
                }
                try {
                    String motd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    if (!MotdRepo.create(args[1], motd)) {
                        sender.sendMessage(Component.text("Failed to create motd, does it already exist?", NamedTextColor.RED));
                        return true;
                    }
                    sender.sendMessage(Component.text("Successfully created motd", NamedTextColor.GREEN));
                } catch (SQLException exception) {
                    AverUtils.logWarning("Failed to create motd in database", exception);
                    sender.sendMessage(Component.text("An unknown error has occurred", NamedTextColor.RED));
                }
                break;
            case "edit":
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /motd edit [name] [message]"));
                    return true;
                }
                try {
                    String motd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    if (!MotdRepo.edit(args[1], motd)) {
                        sender.sendMessage(Component.text("Failed to edit motd, does it exist?", NamedTextColor.RED));
                        return true;
                    }
                    sender.sendMessage(Component.text("Successfully edited motd", NamedTextColor.GREEN));
                } catch (SQLException exception) {
                    AverUtils.logWarning("Failed to modify motd in database", exception);
                    sender.sendMessage(Component.text("An unknown error has occurred", NamedTextColor.RED));
                }
                break;
            case "delete":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /motd delete [name]"));
                    return true;
                }
                try {
                    if (!MotdRepo.delete(args[1])) {
                        sender.sendMessage(Component.text("Failed to delete motd, does it exist?", NamedTextColor.RED));
                        return true;
                    }
                    sender.sendMessage(Component.text("Successfully deleted motd", NamedTextColor.GREEN));
                } catch (SQLException exception) {
                    AverUtils.logWarning("Failed to remove motd from database", exception);
                    sender.sendMessage(Component.text("An unknown error has occurred", NamedTextColor.RED));
                }
                break;
            case "set":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /motd set [name] [true/false]"));
                    return true;
                }
                try {
                    if (args.length >= 3) {
                        if (!MotdRepo.setActive(args[1], Boolean.parseBoolean(args[2]))){
                            sender.sendMessage(Component.text("Failed to set motd, does it exist?", NamedTextColor.RED));
                            return true;
                        }
                        sender.sendMessage(Component.text("Successfully set motd", NamedTextColor.GREEN));
                        return true;
                    }
                    MotdRepo.MotdData motd = MotdRepo.get(args[1]);
                    if (motd == null) {
                        sender.sendMessage(Component.text("Failed to set motd, does it exist?", NamedTextColor.RED));
                        return true;
                    }
                    if (motd.isActive()){
                        MotdRepo.setActive(args[1], false);
                        sender.sendMessage(Component.text("Successfully unset motd", NamedTextColor.GREEN));
                        return true;
                    }
                    MotdRepo.setActive(args[1], true);
                    sender.sendMessage(Component.text("Successfully set motd", NamedTextColor.GREEN));
                } catch (SQLException exception) {
                    AverUtils.logWarning("Failed to modify motd in database", exception);
                    sender.sendMessage(Component.text("An unknown error has occurred", NamedTextColor.RED));
                }
                break;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) { return List.of("create", "edit", "delete", "set"); }
        String sub = args[0].toLowerCase();
        boolean isPresent = sub.equals("edit") || sub.equals("delete") || sub.equals("set");
        if (args.length == 2 && isPresent) {
            List<String> completions = new ArrayList<>();
            try {
                List<MotdRepo.MotdData> motds = MotdRepo.list();
                for (MotdRepo.MotdData motd : motds) {
                    completions.add(motd.name());
                }
                return completions;
            } catch (SQLException e) {
                AverUtils.logWarning("Failed to fetch motds from database", e);
            }
        }
        return List.of();
    }
}

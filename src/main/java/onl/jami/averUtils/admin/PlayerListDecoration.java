package onl.jami.averUtils.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.listeners.Player;
import onl.jami.averUtils.storage.database.PlayerListDecorationRepo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
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
        }
        PlayerListDecorationRepo.PlayerListDecorationData.Location location = PlayerListDecorationRepo.PlayerListDecorationData.Location.valueOf(args[1]);
        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 4) {
                    sender.sendMessage(Component.text("Usage: /playerlistdecoration create [HEADER/FOOTER] [name] [message]"));
                    return true;
                }
                try {
                    String message = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                    if (!PlayerListDecorationRepo.create(args[2], location, message)) {
                        sender.sendMessage(Component.text("Failed to create player list decoration, does it already exist?", NamedTextColor.RED));
                        return true;
                    }
                    sender.sendMessage(Component.text("Successfully created player list decoration", NamedTextColor.GREEN));
                } catch (SQLException exception) {
                    AverUtils.logWarning("Failed to create player list decoration in database", exception);
                    sender.sendMessage(Component.text("An unknown error has occurred", NamedTextColor.RED));
                }
                break;
            case "edit":
                if (args.length < 4) {
                    sender.sendMessage(Component.text("Usage: /playerlistdecoration edit [HEADER/FOOTER] [name] [message]"));
                    return true;
                }
                try {
                    String message = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                    if (!PlayerListDecorationRepo.edit(args[2], location, message)) {
                        sender.sendMessage(Component.text("Failed to edit player list decoration, does it exist?", NamedTextColor.RED));
                        return true;
                    }
                    sender.sendMessage(Component.text("Successfully edited player list decoration", NamedTextColor.GREEN));
                } catch (SQLException exception) {
                    AverUtils.logWarning("Failed to modify player list decoration in database", exception);
                    sender.sendMessage(Component.text("An unknown error has occurred", NamedTextColor.RED));
                }
                break;
            case "delete":
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /playerlistdecoration delete [HEADER/FOOTER] [name]"));
                    return true;
                }
                try {
                    if (!PlayerListDecorationRepo.delete(args[2], location)) {
                        sender.sendMessage(Component.text("Failed to delete player list decoration, does it exist?", NamedTextColor.RED));
                        return true;
                    }
                    sender.sendMessage(Component.text("Successfully deleted player list decoration", NamedTextColor.GREEN));
                } catch (SQLException exception) {
                    AverUtils.logWarning("Failed to remove player list decoration from database", exception);
                    sender.sendMessage(Component.text("An unknown error has occurred", NamedTextColor.RED));
                }
                break;
            case "set":
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /playerlistdecoration set [HEADER/FOOTER] [name] [true/false]"));
                    return true;
                }
                try {
                    if (args.length >= 4) {
                        if (!PlayerListDecorationRepo.setActive(args[2], location, Boolean.parseBoolean(args[2]))){
                            sender.sendMessage(Component.text("Failed to set player list decoration, does it exist?", NamedTextColor.RED));
                            return true;
                        }
                        sender.sendMessage(Component.text("Successfully set player list decoration", NamedTextColor.GREEN));
                        return true;
                    }
                    PlayerListDecorationRepo.PlayerListDecorationData decoration = PlayerListDecorationRepo.get(args[2], location);
                    if (decoration == null) {
                        sender.sendMessage(Component.text("Failed to set player list decoration, does it exist?", NamedTextColor.RED));
                        return true;
                    }
                    if (decoration.isActive()){
                        PlayerListDecorationRepo.setActive(args[2], location, false);
                        sender.sendMessage(Component.text("Successfully unset player list decoration", NamedTextColor.GREEN));
                        return true;
                    }
                    PlayerListDecorationRepo.setActive(args[2], location, true);
                    sender.sendMessage(Component.text("Successfully set player list decoration", NamedTextColor.GREEN));
                } catch (SQLException exception) {
                    AverUtils.logWarning("Failed to modify player list decoration in database", exception);
                    sender.sendMessage(Component.text("An unknown error has occurred", NamedTextColor.RED));
                }
                break;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) { return List.of("create", "edit", "delete", "set"); }
        if (args.length == 2) {
            return List.of("HEADER", "FOOTER");
        }
        String sub = args[0].toLowerCase();
        boolean isPresent = sub.equals("edit") || sub.equals("delete") || sub.equals("set");
        if (args.length == 3 && isPresent) {
            List<String> completions = new ArrayList<>();
            try {
                List<PlayerListDecorationRepo.PlayerListDecorationData> decorations = PlayerListDecorationRepo.list(PlayerListDecorationRepo.PlayerListDecorationData.Location.valueOf(args[1].toUpperCase()));
                for (PlayerListDecorationRepo.PlayerListDecorationData decoration : decorations) {
                    completions.add(decoration.name());
                }
                return completions;
            } catch (SQLException e) {
                AverUtils.logWarning("Failed to fetch player list decorations from database", e);
            }
        }
        return List.of();
    }
}

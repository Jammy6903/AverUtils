package onl.jami.averUtils.userconfig.profile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.database.PlayerRepo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;

public class NameStyle implements TabExecutor {
    private static final List<String> STYLE_OPTIONS = List.of("BOLD", "UNDERLINED", "ITALIC");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }

        if (args.length == 0) {
            return false;
        }
        try {
            UUID uuid = ((Player) sender).getUniqueId();
            if (args[0].equalsIgnoreCase("reset")) {
                PlayerRepo.updateNameStyle(uuid, "");
                PlayerCache.updatePlayer(uuid);
            }
            StringBuilder result = new StringBuilder();
            StringBuilder preview = new StringBuilder("<name>");
            for (String style : args) {
                Wrap tags = parse(sender, style);
                if (tags == null) { return false; }
                preview.insert(0, tags.open());
                preview.append(tags.close());
                result.append(style).append(" ");
            }
            Component playerName = PlayerName.getComponent(((Player) sender).getPlayer(), false);
            if (playerName == null) {
                playerName = Component.text("YourName");
            }
            Component namePreview = MiniMessage.miniMessage().deserialize(preview.toString(),
                    Placeholder.component("name", playerName)
            );
            Messages.send(sender, "commands.namestyle.success",
                    Placeholder.component("name", namePreview)
            );
            PlayerRepo.updateNameStyle(uuid, result.toString());
            PlayerCache.updatePlayer(uuid);
        } catch (SQLException exception) {
            Messages.send(sender, "commands.generic.failure.unknown");
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            Set<String> names = NamedTextColor.NAMES.keys();
            List<String> completions = new ArrayList<>(List.of("RESET"));
            if (sender.hasPermission("averutils.namestyle.color")) {
                completions.addAll(names);
                completions.add("#0011FF");
            }
            if (sender.hasPermission("averutils.namestyle.gradient")) {
                completions.addAll(List.of("red:purple","#0011FF:#FF1100", "rainbow", "pride"));
            }
            return completions;
        }
        if (sender.hasPermission("averutils.namestyle.decoration")) {
            List<String> so = new ArrayList<>(STYLE_OPTIONS);
            for (int i = 1; i < args.length -1; i++) {
                so.remove(args[i].toUpperCase());
            }
            return so;
        }
        return List.of();
    }

    public record Wrap(String open, String close) {}
    private static final Set<TextDecoration> ALLOWED_DECORATIONS = EnumSet.of(TextDecoration.BOLD, TextDecoration.ITALIC, TextDecoration.UNDERLINED);

    public static Wrap parse(CommandSender sender, String style) {
        String lower = style.toLowerCase();

        TextDecoration decoration = TextDecoration.NAMES.value(lower);
        if (decoration != null && ALLOWED_DECORATIONS.contains(decoration)) {
            if (!sender.hasPermission("averutils.namestyle.decoration")) { return null; }
            String tag = TextDecoration.NAMES.key(decoration);
            return new Wrap("<" + tag + ">", "</" + tag + ">");
        }

        if (lower.equals("rainbow")) {
            if (!sender.hasPermission("averutils.namestyle.gradient")) { return null; }
            return new Wrap("<rainbow>", "</rainbow>");
        }

        if (lower.equals("pride")) {
            if (!sender.hasPermission("averutils.namestyle.gradient")) { return null; }
            return new Wrap("<pride>", "</pride>");
        }

        if (lower.contains(":")) {
            if (!sender.hasPermission("averutils.namestyle.gradient")) { return null; }
            String[] parts = lower.split(":", -1);
            if (parts.length != 2) { return null; }
            TextColor from = parseColor(parts[0]);
            TextColor to = parseColor(parts[1]);
            if (from == null || to == null) { return null; }
            return new Wrap("<gradient:" + from.asHexString() + ":" + to.asHexString() + ">", "</gradient>");
        }

        TextColor color = parseColor(lower);
        if (color != null) {
            if (!sender.hasPermission("averutils.namestyle.color")) { return null; }
            String hex = color.asHexString();
            return new Wrap("<" + hex + ">", "</" + hex + ">");
        }

        return null;
    }

    private static TextColor parseColor(String s) {
        return s.startsWith("#") ? TextColor.fromHexString(s) : NamedTextColor.NAMES.value(s);
    }
}

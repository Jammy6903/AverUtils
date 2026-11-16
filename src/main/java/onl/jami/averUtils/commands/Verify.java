package onl.jami.averUtils.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.discord.verification.VerificationCodes;
import onl.jami.averUtils.worlds.jail.JailWorld;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.cache.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class Verify implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            Locale.send(sender, "commands.generic.failure.players-only");
            return true;
        }
        if (!AverUtils.getCfg().getBoolean("discord.enabled")) {
            Locale.send(sender, "commands.verify.failure.not-available");
            return true;
        }
        if (args.length < 1) return false;
        Long id = VerificationCodes.checkCode(args[0]);
        if (id == null) {
            Locale.send(sender, "commands.verify.failure.not-valid");
            return true;
        }
        Guild g = AverUtils.getGuild();
        g.retrieveMemberById(id).queue(
                m -> {
                    try {
                        PlayerRepo.verifyPlayer(p.getUniqueId(), id, m.getUser().getName(), m.getUser().getGlobalName(), m.getNickname());
                        PlayerCache.updatePlayer(p.getUniqueId());
                        Locale.send(sender, "commands.verify.success",
                                Placeholder.component("discord", Component.text(m.getEffectiveName()))
                        );
                        JailWorld.release(p);
                    } catch (SQLException exception) {
                        AverUtils.logWarning(Locale.getString("errors.database.players"), exception);
                        Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> Locale.send(sender, "commands.generic.failure.unknown"));
                    }
                },
                _ -> Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> Locale.send(sender, "commands.verify.failure.not-in-discord"))
        );

        return true;
    }
}

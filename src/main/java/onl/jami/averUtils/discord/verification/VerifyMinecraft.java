package onl.jami.averUtils.discord.verification;

import net.dv8tion.jda.api.entities.Guild;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.discord.sync.RoleSync;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.cache.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class VerifyMinecraft implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            Messages.send(sender, "commands.generic.failure.players-only");
            return true;
        }
        if (!AverUtils.getCfg().getBoolean("discord.enabled")) {
            Messages.send(sender, "commands.verify.failure.not-available");
            return true;
        }
        if (args.length < 1) return false;
        Long id = VerificationCodes.checkCode(args[0]);
        if (id == null) {
            Messages.send(sender, "commands.verify.failure.not-valid");
            return true;
        }
        Guild g = AverUtils.getGuild();
        g.retrieveMemberById(id).queue(
                m -> {
                    try {
                        PlayerRepo.verifyPlayer(((Player)sender).getUniqueId(), id, m.getUser().getName(), m.getUser().getGlobalName(), m.getNickname());
                        PlayerCache.updatePlayer(((Player)sender).getUniqueId());
                        Messages.send(sender, "commands.verify.success",
                                Placeholder.component("discord", Component.text(m.getUser().getName()))
                        );
                        RoleSync.sync((Player)sender);
                    } catch (SQLException exception) {
                        AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
                        Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> Messages.send(sender, "commands.generic.failure.unknown"));
                    }
                },
                _ -> Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> Messages.send(sender, "commands.verify.failure.not-in-discord"))
        );

        return true;
    }
}

package onl.jami.averUtils.discord.verification;

import net.dv8tion.jda.api.entities.Guild;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.discord.sync.RoleSync;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.cache.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.logging.Level;

public class VerifyMinecraft implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can run this command!",  NamedTextColor.RED));
        }
        if (args.length < 1) return false;
        Long id = VerificationCodes.checkCode(args[0]);
        if (id == null) {
            sender.sendMessage(Component.text("Invalid verification code.",  NamedTextColor.RED));
            return true;
        }
        Guild g = AverUtils.getGuild();
        if (g == null) {
            sender.sendMessage(Component.text("Verification is unavailable right now."));
            return true;
        }
        g.retrieveMemberById(id).queue(
                m -> {
                    try {
                        PlayerRepo.verifyPlayer(((Player)sender).getUniqueId(), id, m.getUser().getName(), m.getUser().getGlobalName(), m.getNickname());
                        PlayerCache.addPlayer(((Player)sender).getUniqueId(), PlayerRepo.getPlayer(((Player)sender).getUniqueId()));
                        sender.sendMessage(Component.text("You have been verified!", NamedTextColor.GREEN));
                        RoleSync.sync((Player)sender);
                    } catch (SQLException ex) {
                        Bukkit.getLogger().log(Level.WARNING, "Failed to verify player!", ex);
                        Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> sender.sendMessage(Component.text("An unknown error has occured.")));
                    }
                },
                failure -> Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> sender.sendMessage(Component.text("You are not in the Discord.")))
        );

        return true;
    }
}

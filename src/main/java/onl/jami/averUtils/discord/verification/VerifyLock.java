package onl.jami.averUtils.discord.verification;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.dv8tion.jda.api.entities.Guild;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.MiniMessage;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.discord.sync.RoleSync;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.cache.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class VerifyLock {
    public static void verifyOnLogin(Player p) {
        try {
            if (!AverUtils.getCfg().getBoolean("discord.enabled")) {
                return;
            }
            if (PlayerRepo.getPlayer(p.getUniqueId()).isVerified()) {
                return;
            }
            VerifyLock.showVerifyDialog(p);
            Bukkit.getScheduler().runTaskLater(AverUtils.getPlugin(), () -> {
                try {
                    if (!PlayerRepo.getPlayer(p.getUniqueId()).isVerified()) {
                        p.kick(MiniMessage.miniMessage().deserialize(Messages.getString("player.moderation.verification.not-verified")));
                    }
                } catch (SQLException exception) {
                    AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
                    p.kick(MiniMessage.miniMessage().deserialize(Messages.getString("player.moderation.kick.unknown")));
                }
            }, 20L * 300);
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
            p.kick(MiniMessage.miniMessage().deserialize(Messages.getString("player.moderation.kick.unknown")));
        }
    }

    private static void showVerifyDialog(Player p) {
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(MiniMessage.miniMessage().deserialize(Messages.getString("player.moderation.verification.dialog.title"))).canCloseWithEscape(false)
                        .inputs(List.of(
                                DialogInput.text("code", MiniMessage.miniMessage().deserialize(Messages.getString("player.moderation.verification.dialog.code")))
                                        .build()
                        ))
                        .build()
                )
                .type(DialogType.confirmation(
                        ActionButton.create(
                                MiniMessage.miniMessage().deserialize(Messages.getString("player.moderation.verification.dialog.confirm")),
                                MiniMessage.miniMessage().deserialize(Messages.getString("player.moderation.verification.dialog.confirm-hover")),
                                100,
                                DialogAction.customClick((view, _) -> {
                                    Long id = VerificationCodes.checkCode(view.getText("code"));
                                    if (id == null) {
                                        p.kick(MiniMessage.miniMessage().deserialize(Messages.getString("player.moderation.verification.wrong-code")));
                                        return;
                                    }
                                    Guild g = AverUtils.getGuild();
                                    g.retrieveMemberById(id).queue(
                                            m -> {
                                                try {
                                                    PlayerRepo.verifyPlayer(p.getUniqueId(), id, m.getUser().getName(), m.getUser().getGlobalName(), m.getNickname());
                                                    PlayerCache.addPlayer(p.getUniqueId(), PlayerRepo.getPlayer(p.getUniqueId()));
                                                    Messages.send(p, Messages.getString("commands.verify.success"));
                                                    RoleSync.sync(p);
                                                } catch (SQLException exception) {
                                                    AverUtils.logWarning(Messages.getString("errors.database.players"), exception);
                                                    Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> p.kick(Component.text(Messages.getString("player.moderation.kick.unknown"))));
                                                }
                                            },
                                            _ -> Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> p.kick(Component.text(Messages.getString("player.moderation.kick.unknown"))))
                                    );
                                }, ClickCallback.Options.builder().build())
                        ),
                        ActionButton.create(
                                MiniMessage.miniMessage().deserialize(Messages.getString("player.moderation.verification.dialog.cancel")),
                                MiniMessage.miniMessage().deserialize(Messages.getString("player.moderation.verification.dialog.cancel-hover")),
                                100,
                                DialogAction.customClick((_, audience) -> {
                                    if (audience instanceof Player) {
                                        p.kick(MiniMessage.miniMessage().deserialize(Messages.getString("player.moderation.verification.not-verified")));
                                    }
                                }, ClickCallback.Options.builder().build())
                        )
                ))
        );
        p.showDialog(dialog);
    }
}

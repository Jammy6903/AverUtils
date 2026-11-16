package onl.jami.averUtils.menus.verify;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.discord.verification.VerificationCodes;
import onl.jami.averUtils.worlds.jail.JailWorld;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.storage.database.PlayerRepo;
import onl.jami.averUtils.storage.cache.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

public class VerifyLock {
    public static void verifyOnLogin(Player p) {
        if (!AverUtils.getCfg().getBoolean("discord.enabled") || !AverUtils.getCfg().getBoolean("discord.require-verification")) {
            return;
        }
        PlayerRepo.PlayerData playerData = PlayerCache.getPlayer(p);
        if (playerData == null) {
            return;
        }
        if (playerData.isVerified()) {
            checkDiscordStatus(p, playerData.discordId());
            return;
        }
        p.teleport(AverUtils.getJailWorld().getSpawnLocation());
        p.setInvulnerable(true);
        VerifyLock.showVerifyDialog(p);
        Bukkit.getScheduler().runTaskLater(AverUtils.getPlugin(), () -> {
            PlayerRepo.PlayerData pd = PlayerCache.getPlayer(p);
            if (pd == null || !pd.isVerified()) {
                p.kick(MiniMessage.miniMessage().deserialize(Locale.getString("player.moderation.verification.not-verified")));
            }
        }, 20L * 300);
    }

    private static void checkDiscordStatus(Player p, long id) {
        Guild g = AverUtils.getGuild();
        if (g.getMemberById(id) != null) { return; }
        g.retrieveMemberById(id).queue(_ -> { }, failure -> {
            if (failure instanceof ErrorResponseException exception && exception.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER) {
                Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> p.kick(Locale.getComponent("player.moderation.verification.left-discord")));
                return;
            }
            AverUtils.logWarning("Failed to check Discord membership for " + p.getName(), (Exception) failure);
        });

    }

    private static void showVerifyDialog(Player p) {
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Locale.getComponent("player.moderation.verification.dialog.title"))
                        .canCloseWithEscape(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .pause(false)
                        .body(List.of(
                                DialogBody.plainMessage(Locale.getComponent("player.moderation.verification.dialog.body",
                                        Placeholder.parsed("invite-link", AverUtils.getCfg().getString("discord.invite-link")))
                                )
                        ))
                        .inputs(List.of(
                                DialogInput.text("code", MiniMessage.miniMessage().deserialize(Locale.getString("player.moderation.verification.dialog.code")))
                                        .build()
                        ))
                        .build()
                )
                .type(DialogType.confirmation(
                        ActionButton.create(
                                MiniMessage.miniMessage().deserialize(Locale.getString("player.moderation.verification.dialog.confirm")),
                                MiniMessage.miniMessage().deserialize(Locale.getString("player.moderation.verification.dialog.confirm-hover")),
                                100,
                                DialogAction.customClick((view, audience) -> {
                                    Long id = VerificationCodes.checkCode(view.getText("code"));
                                    if (id == null) {
                                        p.kick(MiniMessage.miniMessage().deserialize(Locale.getString("player.moderation.verification.wrong-code")));
                                        return;
                                    }
                                    Guild g = AverUtils.getGuild();
                                    g.retrieveMemberById(id).queue(
                                            m -> {
                                                try {
                                                    PlayerRepo.verifyPlayer(p.getUniqueId(), id, m.getUser().getName(), m.getUser().getGlobalName(), m.getNickname());
                                                    Locale.send(p, Locale.getString("commands.verify.success"),
                                                            Placeholder.component("discord", Component.text(m.getEffectiveName()))
                                                    );
                                                    JailWorld.release(p);
                                                    audience.closeDialog();
                                                    p.setInvulnerable(false);
                                                    PlayerCache.updatePlayer(p.getUniqueId());
                                                } catch (SQLException exception) {
                                                    AverUtils.logWarning(Locale.getString("errors.database.players"), exception);
                                                    Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> p.kick(Component.text(Locale.getString("player.moderation.kick.unknown"))));
                                                }
                                            },
                                            _ -> Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> p.kick(Component.text(Locale.getString("player.moderation.kick.unknown"))))
                                    );
                                }, ClickCallback.Options.builder().build())
                        ),
                        ActionButton.create(
                                MiniMessage.miniMessage().deserialize(Locale.getString("player.moderation.verification.dialog.cancel")),
                                MiniMessage.miniMessage().deserialize(Locale.getString("player.moderation.verification.dialog.cancel-hover")),
                                100,
                                DialogAction.customClick((_, audience) -> {
                                    if (audience instanceof Player) {
                                        p.kick(MiniMessage.miniMessage().deserialize(Locale.getString("player.moderation.verification.not-verified")));
                                    }
                                }, ClickCallback.Options.builder().build())
                        )
                ))
        );
        p.showDialog(dialog);
    }
}

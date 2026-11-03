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
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.discord.sync.RoleSync;
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
            if (!AverUtils.getCfg().getBoolean("discord.require-verification")) {
                return;
            }
            if (PlayerRepo.getPlayer(p.getUniqueId()).isVerified()) {
                return;
            }
            VerifyLock.showVerifyDialog(p);
            Bukkit.getScheduler().runTaskLater(AverUtils.getPlugin(), () -> {
                try {
                    if (!PlayerRepo.getPlayer(p.getUniqueId()).isVerified()) {
                        p.kick(Component.text("Verification timed out."));
                    }
                } catch (SQLException exception) {
                    AverUtils.logWarning("Failed to update player in database", exception);
                    p.kick(Component.text("An unknown error has occurred."));
                }
            }, 20L * 300);
        } catch (SQLException exception) {
            AverUtils.logWarning("Failed to update player in database", exception);
            p.kick(Component.text("An unknown error has occurred."));
        }
    }

    private static void showVerifyDialog(Player p) {
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(text("Please verify your Discord using /verify")).canCloseWithEscape(false)
                        .inputs(List.of(
                                DialogInput.text("code", text("Verification code"))
                                        .build()
                        ))
                        .build()
                )
                .type(DialogType.confirmation(
                        ActionButton.create(
                                text("Confirm", TextColor.color(0xAEFFC1)),
                                text("Click to confirm your input."),
                                100,
                                DialogAction.customClick((view, _) -> {
                                    Long id = VerificationCodes.checkCode(view.getText("code"));
                                    if (id == null) {
                                        p.kick(Component.text("You must enter a valid verification code"));
                                        return;
                                    }
                                    Guild g = AverUtils.getGuild();
                                    if (g == null) {
                                        p.kick(Component.text("Verification is unavailable right now."));
                                        return;
                                    }
                                    g.retrieveMemberById(id).queue(
                                            m -> {
                                                try {
                                                    PlayerRepo.verifyPlayer(p.getUniqueId(), id, m.getUser().getName(), m.getUser().getGlobalName(), m.getNickname());
                                                    PlayerCache.addPlayer(p.getUniqueId(), PlayerRepo.getPlayer(p.getUniqueId()));
                                                    p.sendMessage(Component.text("You have been verified!", NamedTextColor.GREEN));
                                                    RoleSync.sync(p);
                                                } catch (SQLException exception) {
                                                    AverUtils.logWarning("Failed to update player in database", exception);
                                                    Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> p.kick(Component.text("An unknown error has occured.")));
                                                }
                                            },
                                            _ -> Bukkit.getScheduler().runTask(AverUtils.getPlugin(), () -> p.kick(Component.text("You are not in the Discord.")))
                                    );
                                }, ClickCallback.Options.builder().build())
                        ),
                        ActionButton.create(
                                text("Cancel", TextColor.color(0xFFA0B1)),
                                text("Cancel verification, you will be kicked."),
                                100,
                                DialogAction.customClick((_, audience) -> {
                                    if (audience instanceof Player) {
                                        p.kick(Component.text("You must verify your account in the Discord server"));
                                    }
                                }, ClickCallback.Options.builder().build())
                        )
                ))
        );
        p.showDialog(dialog);
    }
}

package onl.jami.averUtils.dialogs;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.storage.cache.MotdCache;
import onl.jami.averUtils.storage.database.MotdRepo;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

public class MotdDialog {
    private static final String MESSAGE_KEY = "message";

    public static void open(Player player, String name, String initialMessage, boolean creating) {
        TextDialogInput messageInput = DialogInput.text(MESSAGE_KEY, Component.text("MOTD (MiniMessage)"))
                .width(400)
                .maxLength(2000)
                .initial(initialMessage == null ? "" : initialMessage)
                .multiline(TextDialogInput.MultilineOptions.create(4, 120))
                .build();

        DialogBase base = DialogBase.builder(Component.text((creating ? "New MOTD: " : "Edit MOTD: ") + name))
                .canCloseWithEscape(true)
                .body(List.of(DialogBody.plainMessage(Component.text("Supports MiniMessage."))))
                .inputs(List.of(messageInput))
                .build();

        ActionButton save = ActionButton.builder(Component.text("Save", NamedTextColor.GREEN))
                .action(DialogAction.customClick((view, audience) -> save(player, name, view.getText(MESSAGE_KEY), creating),
                        ClickCallback.Options.builder().uses(1).build())).build();

        ActionButton cancel = ActionButton.builder(Component.text("Cancel", NamedTextColor.RED)).build();

        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(base)
                .type(DialogType.confirmation(save, cancel)));

        player.showDialog(dialog);
    }

    public static void save(Player player, String name, String message, boolean creating) {
        if (message == null || message.isEmpty()) {
            Locale.send(player, "commands.motd.failure.empty");
            return;
        }
        try {
            boolean ok = creating ? MotdRepo.create(name, message) : MotdRepo.edit(name, message);
            if (!ok) {
                Locale.send(player, creating ?
                        "commands.motd.create.failure.already-exists" : "commands.motd.edit.failure.doesnt-exist",
                        Placeholder.unparsed("name", name));
                return;
            }
            MotdCache.updateMotds();
            Locale.send(player, creating ?
                    "commands.motd.create.success" : "commands.motd.edit.success",
                    Placeholder.unparsed("name", name),
                    Placeholder.parsed("motd", message));
        } catch (SQLException e) {
            AverUtils.logWarning(Locale.getString("errors.database.motds"), e);
            Locale.send(player, "commands.generic.failure.unknown");
        }
    };
}

package onl.jami.averUtils.discord.rolelink;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.luckperms.api.model.group.Group;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.discord.sync.roles.RoleSync;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.cache.RoleLinkCache;
import onl.jami.averUtils.storage.database.RoleLinkRepo;

import java.sql.SQLException;

@Deprecated // TEMPORARILY DEPRECATED
public class EditRoles extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!(event.getName().equals("role"))) { return; }
        if (event.getSubcommandName() == null) {
            event.reply("You need to pick a subcommand").setEphemeral(true).queue();
            return;
        }
        try {
            switch (event.getSubcommandName()) {
                case "add":
                    Role roleToAdd = event.getOption("role").getAsRole();
                    Group group = AverUtils.getLuckPermsApi().getGroupManager().getGroup(event.getOption("group").getAsString());
                    if (group == null) {
                        event.reply("The group you are trying to add or edit does not exist").setEphemeral(true).queue();
                        return;
                    }
                    RoleLinkRepo.addRole(roleToAdd.getIdLong(), group.getName());
                    event.reply("Successfully added role").setEphemeral(true).queue();
                    break;
                case "remove":
                    Role roleToRemove = event.getOption("role").getAsRole();
                    if (!RoleLinkRepo.deleteRole(roleToRemove.getIdLong())) {
                        event.reply("Couldn't delete role, does it exist?").setEphemeral(true).queue();
                    }
                    event.reply("Successfully removed role").setEphemeral(true).queue();
                    break;
                case "list":
                    return;
            }
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.role_links"), exception);
            event.reply(Messages.getString("discord.commands.generic.failure.unknown")).setEphemeral(true).queue();
            return;
        }
        RoleLinkCache.updateRoles();
        RoleSync.syncOnline();
    }
}

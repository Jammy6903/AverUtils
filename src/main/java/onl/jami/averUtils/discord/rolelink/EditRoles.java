package onl.jami.averUtils.discord.rolelink;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.luckperms.api.model.group.Group;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.discord.sync.RoleSync;
import onl.jami.averUtils.storage.database.RoleLinkRepo;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.logging.Level;

public class EditRoles extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!(event.getName().equals("role"))) { return; }
        if (event.getSubcommandName() == null) {
            event.reply("You need to pick a subcommand").setEphemeral(true).queue();
            return;
        }

        switch (event.getSubcommandName()) {
            case "add":
                Role roleToAdd = event.getOption("role").getAsRole();
                Group group = AverUtils.getLuckPermsApi().getGroupManager().getGroup(event.getOption("group").getAsString());
                if (group == null) {
                    event.reply("The group you are trying to add or edit does not exist").setEphemeral(true).queue();
                    return;
                }
                try {
                    RoleLinkRepo.addRole(roleToAdd.getIdLong(), group.getName());
                    RoleSync.syncOnline();
                    event.reply("Successfully added role").setEphemeral(true).queue();
                } catch (SQLException exception) {
                    AverUtils.logWarning("Failed to create role link in database", exception);
                    event.reply("An unknown error has occured").setEphemeral(true).queue();
                    return;
                }
                break;
            case "remove":
                Role roleToRemove = event.getOption("role").getAsRole();
                try {
                    RoleLinkRepo.deleteRole(roleToRemove.getIdLong());
                    RoleSync.syncOnline();
                    event.reply("Successfully removed role").setEphemeral(true).queue();
                } catch (SQLException exception) {
                    AverUtils.logWarning("Failed to remove role link from database", exception);
                    event.reply("An unknown error has occured").setEphemeral(true).queue();
                    return;
                }
                break;
            case "list":
                break;
        }
    }
}

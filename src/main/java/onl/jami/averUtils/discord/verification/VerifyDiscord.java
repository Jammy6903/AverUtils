package onl.jami.averUtils.discord.verification;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class VerifyDiscord extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("verify")) return;

        String code = VerificationCodes.generateVerificationCode(event.getUser().getIdLong());
        event.reply("Your code is: `" + code + "`").setEphemeral(true).queue();
    }
}

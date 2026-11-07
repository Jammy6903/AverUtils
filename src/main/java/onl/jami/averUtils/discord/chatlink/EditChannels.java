package onl.jami.averUtils.discord.chatlink;

import java.sql.SQLException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.cache.ChannelLinkCache;
import onl.jami.averUtils.storage.database.ChannelLinkCategory;
import onl.jami.averUtils.storage.database.ChannelLinkRepo;
import onl.jami.averUtils.discord.Bot;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.Set;

public class EditChannels extends ListenerAdapter {

    private static final Cache<String, Long> interactionCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!(event.getName().equals("channel"))) { return; }
        if (event.getSubcommandName() == null) {
            event.reply("You need to pick a subcommand").setEphemeral(true).queue();
            return;
        }

        Channel channel = event.getOption("channel").getAsChannel();
        switch(event.getSubcommandName()) {
            case "add":
                if (channel.getType() != ChannelType.TEXT) {
                    event.reply("Channel must be a text channel").setEphemeral(true).queue();
                    return;
                }
                if (!event.getGuild().getSelfMember().hasPermission((GuildChannel) channel, Permission.MANAGE_WEBHOOKS)) {
                    event.reply("I need the Manage Webhooks permission in " + channel.getAsMention()).setEphemeral(true).queue();
                    return;
                }
                addEmbed(event);
                break;
            case "remove":
                try {
                    TextChannel textChannel = ((GuildChannelUnion) channel).asTextChannel();
                    textChannel.retrieveWebhooks().queue(webhooks -> {
                        for (Webhook webhook : webhooks) {
                            if (webhook.getName().equals("averutils-chatlink") && webhook.getOwnerAsUser() != null && webhook.getOwnerAsUser().getIdLong() == Bot.getJDA().getSelfUser().getIdLong()) {
                                webhook.delete().queue();
                            }
                        }
                    });
                    if (!ChannelLinkRepo.deleteChannel(event.getGuild().getIdLong(), textChannel.getIdLong())) {
                        event.reply("That channel link doesn't exist").setEphemeral(true).queue();
                    }
                    event.reply("Channel link has been removed").setEphemeral(true).queue();
                } catch (SQLException exception) {
                    AverUtils.logWarning(Messages.getString("discord.commands.generic.failure.unknown"), exception);
                    event.reply(Messages.getString("errors.database.channel_links")).setEphemeral(true).queue();
                }
                break;
            case "list":
                break;
        }
        ChannelLinkCache.updateChannels();
    }

    private static void addEmbed(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Select Categories");
        embed.setDescription("Pick the categories to link between Minecraft and " + event.getOption("channel").getAsMentionable().getAsMention());
        embed.setFooter("You have 5 minutes to select categories.");
        embed.setColor(Color.GREEN);

        StringSelectMenu.Builder categoriesBuilder = StringSelectMenu.create("categories-" +event.getInteraction().getIdLong());
        interactionCache.put(("categories-" + event.getInteraction().getIdLong()), event.getOption("channel").getAsChannel().getIdLong());
        for (ChannelLinkCategory channelLinkCategory : ChannelLinkCategory.values()) {
            categoriesBuilder.addOption(channelLinkCategory.name(), channelLinkCategory.name().toLowerCase());
        }
        categoriesBuilder.setMaxValues(8);
        StringSelectMenu categories = categoriesBuilder.build();
        ActionRow row = ActionRow.of(categories);

        event.replyEmbeds(embed.build()).addComponents(row).setEphemeral(true).queue();
    }

    @Override
    public void onStringSelectInteraction(@NonNull StringSelectInteractionEvent event) {
        String eventName = event.getInteraction().getComponentId();
        if (!(eventName.startsWith("categories"))) { return; }
        Long channelId = interactionCache.getIfPresent(eventName);
        TextChannel channel = event.getGuild().getChannelById(TextChannel.class, channelId);
        channel.retrieveWebhooks().queue(webhooks -> {
            for (Webhook webhook : webhooks) {
                if (webhook.getName().equals("averutils-chatlink") && webhook.getOwnerAsUser() != null && webhook.getOwnerAsUser().getIdLong() == Bot.getJDA().getSelfUser().getIdLong()) {
                    webhook.delete().queue();
                }
            }
        });
        channel.createWebhook("averutils-chatlink").queue(webhook -> {
            List<String> categories = event.getInteraction().getValues();
            Set<ChannelLinkCategory> categoriesSet = new HashSet<>();
            for (String category : categories) {
                categoriesSet.add(ChannelLinkCategory.valueOf(category.toUpperCase()));
            }
            try {
                if (!ChannelLinkRepo.setCategories(event.getGuild().getIdLong(), channelId, categoriesSet, webhook.getUrl())) {
                    event.reply(Messages.getString("discord.commands.generic.failure.unknown")).setEphemeral(true).queue();
                    return;
                }
                event.reply("Channel link has been saved.").setEphemeral(true).queue();
            } catch (SQLException exception) {
                AverUtils.logWarning(Messages.getString("errors.database.channel_links"), exception);
                event.reply(Messages.getString("discord.commands.generic.failure.unknown")).setEphemeral(true).queue();
            }
        });
    }
}

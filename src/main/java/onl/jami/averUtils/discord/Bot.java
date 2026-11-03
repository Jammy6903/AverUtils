package onl.jami.averUtils.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import onl.jami.averUtils.discord.chatlink.DiscordToMinecraft;
import onl.jami.averUtils.discord.chatlink.EditChannels;
import onl.jami.averUtils.discord.rolelink.EditRoles;
import onl.jami.averUtils.discord.sync.SyncOnChange;
import onl.jami.averUtils.discord.verification.VerifyDiscord;

public class Bot {
    public static JDA jda;

    public static JDA jdaInit(String token) {
        jda = JDABuilder.createLight(token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_WEBHOOKS, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new DiscordToMinecraft(), new SyncOnChange(), new VerifyDiscord(), new EditChannels(), new EditRoles())
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();
        jda.updateCommands().addCommands(verifyCommand(), channelCommand(), roleCommand()).queue();
        return jda;
    }

    public static void close() {
        if (jda == null) { return; }
        jda.shutdown();
        jda = null;
    }

    public static SlashCommandData verifyCommand() {
        return Commands.slash("verify", "Get a code to verify your Minecraft account.").setContexts(InteractionContextType.GUILD);
    }

    public static SlashCommandData channelCommand() {
        SubcommandData add = new SubcommandData("add", "Add or edit a channel link")
                .addOption(OptionType.CHANNEL, "channel", "The channel to add or edit a link", true);
        SubcommandData remove = new SubcommandData("remove", "Remove a channel link")
                .addOption(OptionType.CHANNEL, "channel", "The channel to remove a link", true);
        SubcommandData list = new SubcommandData("list", "List linked channels");
        return Commands.slash("channel", "Commands to edit channel links").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)).setContexts(InteractionContextType.GUILD).addSubcommands(add, remove, list);
    }

    public static SlashCommandData roleCommand() {
        SubcommandData add = new SubcommandData("add", "Add or edit a role link")
                .addOption(OptionType.ROLE, "role", "The role to add or edit a link", true)
                .addOption(OptionType.STRING, "group", "The luck perms group to link", true);
        SubcommandData remove = new SubcommandData("remove", "Remove a role link")
                .addOption(OptionType.ROLE, "role", "The role to remove a link", true);
        SubcommandData list = new SubcommandData("list", "List linked roles");
        return Commands.slash("role", "Commands to edit role links").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)).setContexts(InteractionContextType.GUILD).addSubcommands(add, remove, list);
    }

    public static JDA getJDA() {
        return jda;
    }
}
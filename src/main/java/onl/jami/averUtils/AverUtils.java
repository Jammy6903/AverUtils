package onl.jami.averUtils;

import net.dv8tion.jda.api.entities.Guild;
import net.luckperms.api.LuckPerms;
import onl.jami.averUtils.admin.DoAs;
import onl.jami.averUtils.admin.Motd;
import onl.jami.averUtils.admin.PlayerListDecoration;
import onl.jami.averUtils.listeners.Chat;
import onl.jami.averUtils.listeners.Ping;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.locale.Reload;
import onl.jami.averUtils.storage.cache.ChannelLinkCache;
import onl.jami.averUtils.storage.cache.MotdCache;
import onl.jami.averUtils.storage.cache.PlayerListDecorationCache;
import onl.jami.averUtils.storage.cache.RoleLinkCache;
import onl.jami.averUtils.userconfig.profile.NameChoice;
import onl.jami.averUtils.userconfig.profile.NameStyle;
import onl.jami.averUtils.userconfig.profile.Nickname;
import onl.jami.averUtils.userconfig.profile.Pronouns;
import onl.jami.averUtils.chat.messaging.Msg;
import onl.jami.averUtils.chat.messaging.Reply;
import onl.jami.averUtils.storage.database.Driver;
import onl.jami.averUtils.discord.Bot;
import onl.jami.averUtils.discord.verification.VerifyMinecraft;
import onl.jami.averUtils.listeners.Player;
import onl.jami.averUtils.teleportation.Spawn;
import onl.jami.averUtils.teleportation.home.*;
import onl.jami.averUtils.teleportation.tpa.TpAccept;
import onl.jami.averUtils.teleportation.tpa.TpDeny;
import onl.jami.averUtils.teleportation.tpa.Tpa;
import onl.jami.averUtils.teleportation.tpa.TpaHere;
import onl.jami.averUtils.teleportation.warp.DelWarp;
import onl.jami.averUtils.teleportation.warp.SetWarp;
import onl.jami.averUtils.teleportation.warp.Warp;
import onl.jami.averUtils.teleportation.warp.Warps;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public final class AverUtils extends JavaPlugin {

    private static Configuration cfg;
    private static Plugin instance;
    private static LuckPerms luckPermsApi;

    @Override
    public void onEnable() {
        instance = this;

        // save and load default config and locale
        loadConfig(this);

        // initialize database/tables, migrate if necessary
        try {
            Driver.initDb();
            Driver.initTables();
            Driver.migrate();

            MotdCache.updateMotds();
            RoleLinkCache.updateRoles();
            ChannelLinkCache.updateChannels();
            PlayerListDecorationCache.updateDecorations();
        } catch (ClassNotFoundException | SQLException e) {
            this.getLogger().log(Level.SEVERE, "Could not initialize or migrate the database!", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // check for discord config, start bot, disable discord on incorrect config
        if (cfg.getBoolean("discord.enabled")) {
            String token = cfg.getString("discord.token");
            long id = cfg.getLong("discord.verification-guild-id");
            if (token != null && !token.isEmpty() && id != 0 ) {
                if (!Bot.jdaInit(cfg.getString("discord.token"))) {
                    cfg.set("discord.enabled", false);
                }
            } else {
                cfg.set("discord.enabled", false);
                this.getLogger().log(Level.WARNING, "Discord token or guild id is not set in config.yml");
            }
        }

        // get LuckPerms API if present
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPermsApi = provider.getProvider();
        }

        getServer().getPluginManager().registerEvents(new Chat(), this);
        getServer().getPluginManager().registerEvents(new Player(), this);
        getServer().getPluginManager().registerEvents(new Ping(), this);
        registerCommand("msg", new Msg());
        registerCommand("reply", new Reply());
        registerCommand("tpa", new Tpa());
        registerCommand("tpahere", new TpaHere());
        registerCommand("tpaccept", new TpAccept());
        registerCommand("tpdeny", new TpDeny());
        registerCommand("spawn", new Spawn());
        registerCommand("setHome", new SetHome());
        registerCommand("home", new Home());
        registerCommand("homes", new Homes());
        registerCommand("delHome", new DelHome());
        registerCommand("verify", new VerifyMinecraft());
        registerCommand("doas", new DoAs());
        registerCommand("pronouns", new Pronouns());
        registerCommand("nickname", new Nickname());
        registerCommand("namechoice", new NameChoice());
        registerCommand("warp", new Warp());
        registerCommand("warps", new Warps());
        registerCommand("delWarp", new DelWarp());
        registerCommand("setWarp", new SetWarp());
        registerCommand("motd", new Motd());
        registerCommand("playerlistdecoration", new PlayerListDecoration());
        registerCommand("namestyle", new NameStyle());
        registerCommand("reload", new Reload());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bot.close();
        try {
            Driver.close();
        } catch (SQLException e) {
            this.getLogger().log(Level.WARNING, "Failed to close database connection", e);
        }
    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().log(Level.WARNING, "Command not found: " + name);
            return;
        }
        command.setExecutor(executor);
        if (executor instanceof TabCompleter completer) {
            command.setTabCompleter(completer);
        }
    }

    public static void loadConfig(Plugin plugin) {
        plugin.saveDefaultConfig();

        // load config and locale
        cfg = plugin.getConfig();
        Messages.load((AverUtils) plugin);
    }

    public static void logWarning(String message, Exception e) { AverUtils.getPlugin().getLogger().log(Level.WARNING, message, e); }

    public static Plugin getPlugin() {return instance;}

    public static Configuration getCfg() {return cfg;}

    public static Guild getGuild() { return Bot.getJDA().getGuildById(getCfg().getLong("discord.verification-guild-id")); }

    public static LuckPerms getLuckPermsApi() {
        return luckPermsApi;
    }
}

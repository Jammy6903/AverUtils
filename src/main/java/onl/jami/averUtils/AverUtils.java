package onl.jami.averUtils;

import net.dv8tion.jda.api.entities.Guild;
import net.luckperms.api.LuckPerms;
import onl.jami.averUtils.admin.DoAs;
import onl.jami.averUtils.admin.Motd;
import onl.jami.averUtils.admin.PlayerListDecoration;
import onl.jami.averUtils.listeners.Chat;
import onl.jami.averUtils.listeners.Ping;
import onl.jami.averUtils.userconfig.profile.NameChoice;
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
        this.saveResource("config.yml", false);

        cfg = this.getConfig();

        try {
            Driver.initDb();
            Driver.initTables();
            Driver.migrate();
        } catch (ClassNotFoundException | SQLException e) {
            this.getLogger().log(Level.SEVERE, "Could not initialize or migrate the database!", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (cfg.getBoolean("discord.enabled")) {
            Bot.jdaInit(cfg.getString("discord.token"));
        }

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPermsApi = provider.getProvider();
        }

        getServer().getPluginManager().registerEvents(new Chat(), this);
        getServer().getPluginManager().registerEvents(new Player(), this);
        getServer().getPluginManager().registerEvents(new Ping(), this);
        Msg msg = new Msg();
        getCommand("msg").setExecutor(msg);
        getCommand("msg").setTabCompleter(msg);
        Reply reply = new Reply();
        getCommand("reply").setExecutor(reply);
        Tpa tpa = new Tpa();
        getCommand("tpa").setExecutor(tpa);
        getCommand("tpa").setTabCompleter(tpa);
        TpaHere tpaHere = new TpaHere();
        getCommand("tpahere").setExecutor(tpaHere);
        getCommand("tpahere").setTabCompleter(tpaHere);
        TpAccept tpAccept = new TpAccept();
        getCommand("tpaccept").setExecutor(tpAccept);
        TpDeny tpDeny = new TpDeny();
        getCommand("tpdeny").setExecutor(tpDeny);
        Spawn spawn = new Spawn();
        getCommand("spawn").setExecutor(spawn);
        SetHome setHome =  new SetHome();
        getCommand("sethome").setExecutor(setHome);
        Home homeCommand =  new Home();
        getCommand("home").setExecutor(homeCommand);
        getCommand("home").setTabCompleter(homeCommand);
        Homes homes = new Homes();
        getCommand("homes").setExecutor(homes);
        DelHome delHome = new DelHome();
        getCommand("delhome").setExecutor(delHome);
        getCommand("delhome").setTabCompleter(delHome);
        VerifyMinecraft verifyMinecraft = new VerifyMinecraft();
        getCommand("verify").setExecutor(verifyMinecraft);
        DoAs doAs = new DoAs();
        getCommand("doas").setExecutor(doAs);
        getCommand("doas").setTabCompleter(doAs);
        Pronouns pronouns = new Pronouns();
        getCommand("pronouns").setExecutor(pronouns);
        getCommand("pronouns").setTabCompleter(pronouns);
        Nickname nickname = new Nickname();
        getCommand("nickname").setExecutor(nickname);
        NameChoice nameChoice = new NameChoice();
        getCommand("name").setExecutor(nameChoice);
        getCommand("name").setTabCompleter(nameChoice);
        Warp warp = new Warp();
        getCommand("warp").setExecutor(warp);
        getCommand("warp").setTabCompleter(warp);
        Warps warps = new Warps();
        getCommand("warps").setExecutor(warps);
        DelWarp delWarp = new DelWarp();
        getCommand("delwarp").setExecutor(delWarp);
        SetWarp setWarp = new SetWarp();
        getCommand("setwarp").setExecutor(setWarp);
        Motd motd = new Motd();
        getCommand("motd").setExecutor(motd);
        getCommand("motd").setTabCompleter(motd);
        PlayerListDecoration playerListDecoration = new PlayerListDecoration();
        getCommand("playerlistdecoration").setExecutor(playerListDecoration);
        getCommand("playerlistdecoration").setTabCompleter(playerListDecoration);
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

    public static void logWarning(String message, Exception e) {
        AverUtils.getPlugin().getLogger().log(Level.WARNING, message, e);
    }

    public static Plugin getPlugin() {return instance;}

    public static Configuration getCfg() {return cfg;}

    public static Guild getGuild() { return Bot.getJDA().getGuildById(getCfg().getLong("discord.verification-guild-id")); }

    public static LuckPerms getLuckPermsApi() {
        return luckPermsApi;
    }
}

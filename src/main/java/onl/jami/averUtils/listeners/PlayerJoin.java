package onl.jami.averUtils.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.commands.admin.PlayerListDecoration;
import onl.jami.averUtils.commands.moderation.Fly;
import onl.jami.averUtils.commands.moderation.Reports;
import onl.jami.averUtils.discord.chatlink.MinecraftToDiscord;
import onl.jami.averUtils.menus.verify.VerifyLock;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.managers.PingManager;
import onl.jami.averUtils.managers.PlayerName;
import onl.jami.averUtils.managers.PunishmentManager;
import onl.jami.averUtils.managers.VanishManager;
import onl.jami.averUtils.storage.cache.BlockCache;
import onl.jami.averUtils.storage.cache.PlayerCache;
import onl.jami.averUtils.storage.database.PlayerRepo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        try {
            // create player if not exists
            PlayerRepo.createPlayer(player.getUniqueId());
        } catch (SQLException exception) {
            AverUtils.logWarning(Locale.getString("errors.database.players"), exception);
            player.kick(MiniMessage.miniMessage().deserialize(Locale.getString("errors.generic")));
            return;
        }

        // cache player
        PlayerRepo.PlayerData playerData = PlayerCache.loadPlayer(player);
        BlockCache.loadBlocksForPlayer(player.getUniqueId());

        sendJoinMessages(playerData, e);

        // verify player if required and send join message to any linked channels
        VerifyLock.verifyOnLogin(player);
        MinecraftToDiscord.sendJoinMessage(e.joinMessage());

        // update ping completions for players
        PingManager.updatePingCompletions(player, true);

        // update player list
        PlayerName.updatePlayerList(player);
        PlayerListDecoration.loadActiveDecorations(player);

        // join notifications
        PunishmentManager.sendWarnOnJoin(player);
        Reports.sendReportsOnJoin(player);;

        // moderation checks
        VanishManager.onJoin(player);
        Fly.onJoin(player);
    }

    public static void sendJoinMessages(PlayerRepo.PlayerData playerData, PlayerJoinEvent e) {
        e.joinMessage(null);
        if (playerData != null && playerData.isVerified() && !VanishManager.hasVanishedState(e.getPlayer())) {
            Component joinMessage = Locale.getRandom("player.join.messages",
                    Placeholder.component("name", PlayerName.getComponent(e.getPlayer(), false)));

            e.joinMessage(Locale.getComponent("player.join.format.message",
                    Placeholder.component("message", joinMessage)));
        }
    }
}

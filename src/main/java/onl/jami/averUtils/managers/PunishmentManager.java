package onl.jami.averUtils.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.exceptions.PlayerNotOnlineException;
import onl.jami.averUtils.locale.Locale;
import onl.jami.averUtils.storage.cache.PunishmentCache;
import onl.jami.averUtils.storage.database.PunishmentRepo;
import onl.jami.averUtils.storage.database.PunishmentType;
import onl.jami.averUtils.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.sql.SQLException;
import java.util.*;

public class PunishmentManager {

    public static final UUID CONSOLE_UUID = new UUID(0L, 0L);

    public record Punishment(OfflinePlayer target, UUID staff, PunishmentType type, String duration, Long durationEnd, String reason, String details, boolean silent) {}

    public static class Builder {

        private final LinkedHashSet<OfflinePlayer> targets = new LinkedHashSet<>();
        private UUID staffUUID = null;
        private PunishmentType type;
        private String duration;
        private Long durationEnd;
        private String reason = "Reason not provided";
        private String details = "Details not provided";
        private boolean silent = false;

        public Builder() {}

        public Builder addTarget(OfflinePlayer p) {
            this.targets.add(p);
            return this;
        }

        public Builder setStaff(UUID staffUUID) {
            this.staffUUID = staffUUID;
            return this;
        }

        public Builder setType(PunishmentType type) {
            this.type = type;
            return this;
        }

        public Builder setDuration(String duration) {
            this.duration = duration;
            return this;
        }

        public Builder setReason(String reason) {
            if (reason != null) {
                this.reason = reason;
            }
            return this;
        }

        public Builder setDetails(String details) {
            if (details != null) {
            this.details = details;
            }
            return this;
        }

        public Builder setSilent(boolean silent) {
            this.silent = silent;
            return this;
        }

        public List<Punishment> build() throws IllegalArgumentException {
            if (targets.isEmpty()) {
                throw new IllegalArgumentException("No targets were provided");
            }

            UUID finalStaffUUID = staffUUID != null ? staffUUID : CONSOLE_UUID;
            PunishmentType finalType = type;
            String finalDuration = duration;
            Long finalDurationEnd = null;

            if (List.of(PunishmentType.KICK, PunishmentType.WARN).contains(this.type) && finalDuration == null) {
                finalDuration = defaultWarnKickDuration();
            }

            if (type.equals(PunishmentType.BAN) && finalDuration != null) {
                finalType = PunishmentType.TEMP_BAN;
            }

            if (finalDuration != null) {
                long now = System.currentTimeMillis();
                finalDurationEnd = now + (TimeUtils.parseTime(finalDuration) * 1000L);
            }
            List<Punishment> out = new ArrayList<>();
            for (OfflinePlayer p : targets) {
                out.add(new Punishment(p, finalStaffUUID, finalType, finalDuration, finalDurationEnd, reason, details, silent));
            }
            return out;
        }

        private static String defaultWarnKickDuration() {
            String duration = AverUtils.getCfg().getString("moderation.punishments.warn-kick-duration");
            if (duration == null || duration.isEmpty()) { return "7d"; }
            return duration;
        }

    }

    public static void issue(List<Punishment> ps) throws SQLException, PlayerNotOnlineException {
        for (Punishment p : ps) {
            if ((!p.target.isOnline() || p.target.getPlayer() == null) && p.type.equals(PunishmentType.KICK)) {
                throw new PlayerNotOnlineException("Player" + p.target.getName() + " not online");
            }
        }

        for (Punishment p : ps) {
            int id = PunishmentRepo.create(p.target.getUniqueId(), p.staff, p.type, p.duration, p.durationEnd, p.reason, p.details);

            if (p.target.isOnline()) {
                Player target = p.target.getPlayer();
                if (target != null) {
                    switch (p.type) {
                        case BAN, TEMP_BAN, KICK -> kick(id, target, p);
                        case WARN, TIMEOUT -> sendMessage(target, p);
                    }
                }
            }
            announceToPunishers(p.target.getName(), p);
            if (!p.silent) {
                announceToPlayers(p.target.getName(), p);
            }
            PunishmentCache.loadActivePunishmentsForPlayer(p.target.getUniqueId());
        }
    }

    public static void kick(int id, Player player, Punishment punishment) {
        String duration = "permanent";
        String durationEnd = "never";
        if (punishment.duration() != null) {
            duration = punishment.duration();
            durationEnd = TimeUtils.formatRemainingTimeUntil(punishment.durationEnd());
        }

        Component kickMessage = getComponent("moderation.punishments.strings." + punishment.type().toString().toLowerCase(java.util.Locale.ROOT),
                Placeholder.component("reason", Component.text(punishment.reason())),
                Placeholder.component("duration", Component.text(duration)),
                Placeholder.component("remaining", Component.text(durationEnd)),
                Placeholder.component("id", Component.text(id)));

        player.kick(kickMessage);
    }

    public static void sendMessage(Player player, Punishment punishment) {
        String duration = "permanent";
        if (punishment.duration() != null) {
            duration = punishment.duration();
        }
        String key = punishment.type().toString().toLowerCase(java.util.Locale.ROOT);
        player.sendMessage(getComponent("moderation.punishments.strings." + key + ".on-" + key,
                Placeholder.component("reason", Component.text(punishment.reason())),
                Placeholder.component("duration", Component.text(duration))
        ));
    }

    public static void announceToPunishers(String name, Punishment p) {
        String punishment = switch (p.type()) {
            case WARN -> "warned";
            case KICK -> "kicked";
            case TIMEOUT ->  "timed out";
            case BAN, TEMP_BAN -> "banned";
        };
        String staffName = "CONSOLE";
        if (!p.staff.equals(CONSOLE_UUID)) {
            String n = Bukkit.getOfflinePlayer(p.staff()).getName();
            if (n != null) {
                staffName = n;
            }
        }

        String duration = "permanent";
        if (p.duration() != null) {
            duration = p.duration();
        }

        Component message = getComponent("moderation.punishments.strings.announce.moderators",
                Placeholder.component("player", Component.text(name)),
                Placeholder.component("punishment", Component.text(punishment)),
                Placeholder.component("moderator", Component.text(staffName)),
                Placeholder.component("reason", Component.text(p.reason)),
                Placeholder.component("details", Component.text(p.details)),
                Placeholder.component("duration", Component.text(duration))
        );
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("averutils.moderation.punish")) {
                staff.sendMessage(message);
            }
        }
    }

    public static void announceToPlayers(String name, Punishment p) {
        String messageRaw = AverUtils.getCfg().getString("moderation.punishments.strings.announce.public." + p.type().name().toLowerCase());
        if (messageRaw == null) { return; }
        if (messageRaw.isEmpty()) { return; }
        if (name == null) { return; }
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize(messageRaw,
                Placeholder.component("player", Component.text(name))));
        // TODO: add more placeholders
    }

    public static Component getComponent(String key, TagResolver... tagResolvers) {
        String raw = AverUtils.getCfg().getString(key);
        if (raw == null) { raw = key; }
        return MiniMessage.miniMessage().deserialize(raw, tagResolvers);
    }

    public static void sendWarnOnJoin(Player player) {
        for (PunishmentRepo.PunishmentData p : PunishmentCache.loadActivePunishmentsForPlayer(player.getUniqueId())) {
            if (p.type().equals(PunishmentType.WARN) || p.type().equals(PunishmentType.KICK)) {
                player.sendMessage(getComponent("moderation.punishments.strings.warn.on-join",
                        Placeholder.component("reason", Component.text(p.reason()))
                ));
            }
        }
    }

    public static PunishmentRepo.PunishmentData checkPunishments(UUID uuid, Set<PunishmentType> type) throws SQLException{
        long now = System.currentTimeMillis();
        List<PunishmentRepo.PunishmentData> punishments = PunishmentRepo.getByUUID(uuid);
        for (PunishmentRepo.PunishmentData p : punishments) {
            if (type.contains(p.type())) {
                if (p.isExpired()) { continue; }
                if (p.durationEnd() != null && p.durationEnd() < now) {
                    try {
                        PunishmentRepo.setExpired(p.id(), true);
                    } catch (SQLException ex) {
                        AverUtils.logWarning(Locale.getString("errors.database.punishments"), ex);
                    }
                    continue;
                }
                return p;
            }
        }
        return null;
    }

    public static void onJoin(AsyncPlayerPreLoginEvent event) {
        try {
            PunishmentRepo.PunishmentData ban = PunishmentManager.checkPunishments(event.getUniqueId(), Set.of(PunishmentType.BAN, PunishmentType.TEMP_BAN));
            if (ban != null) {
                String key = ban.type() == PunishmentType.BAN ? "moderation.punishments.strings.ban" : "moderation.punishments.strings.temp_ban";
                Component duration = Component.empty();
                if (ban.duration() != null) {
                    duration = Component.text(ban.duration());
                }
                Component remaining = Component.empty();
                if (ban.durationEnd() != null) {
                    remaining = Component.text(TimeUtils.formatRemainingTimeUntil(ban.durationEnd()));
                }
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        PunishmentManager.getComponent(key,
                                Placeholder.component("reason", Component.text(ban.reason())),
                                Placeholder.component("duration", duration),
                                Placeholder.component("remaining", remaining),
                                Placeholder.component("id", Component.text(ban.id()))));
            }
        } catch (SQLException e) {
            AverUtils.logWarning(Locale.getString("errors.database.punishments"), e);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, Locale.getComponent("errors.generic.unknown"));
        }
    }

}

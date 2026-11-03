package onl.jami.averUtils.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ChannelLinkRepo {
    public static EnumSet<ChannelLinkCategory> getCategories(Long guildId, Long channelId) throws SQLException {
        String sql = "SELECT categories FROM channel_links WHERE guild_id = ? AND channel_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setLong(1, guildId);
            ps.setLong(2, channelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return ChannelLinkCategory.fromMask(rs.getInt("categories"));
            }
        }
    }

    public record ChannelRef(Long guildId, Long channelId, String webhookUrl) {}

    public static List<ChannelRef> getChannelsFor(ChannelLinkCategory c) throws SQLException {
        String sql = "SELECT guild_id, channel_id, webhook_url FROM channel_links WHERE (categories & ?) != 0";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setInt(1, c.bit);
            try (ResultSet rs = ps.executeQuery()) {
                List<ChannelRef> channels = new ArrayList<>();
                while (rs.next()) {
                    channels.add(new ChannelRef(rs.getLong("guild_id"), rs.getLong("channel_id"), rs.getString("webhook_url")));
                }
                return channels;
            }
        }
    }

    public static boolean isLinked(Long guildId, Long channelId) throws SQLException {
        String sql = "SELECT 1 FROM channel_links WHERE guild_id = ? AND channel_id = ? LIMIT 1";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setLong(1, guildId);
            ps.setLong(2, channelId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static boolean setCategories(Long guild_id, Long channel_id, Set<ChannelLinkCategory> categories, String webhookUrl) throws SQLException {
        String sql = "INSERT INTO channel_links (guild_id, channel_id, categories, webhook_url) VALUES (?, ?, ?, ?) ON CONFLICT(guild_id, channel_id) DO UPDATE SET categories = excluded.categories, webhook_url = excluded.webhook_url";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setLong(1, guild_id);
            ps.setLong(2, channel_id);
            ps.setInt(3, ChannelLinkCategory.toMask(categories));
            ps.setString(4, webhookUrl);
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean enableCategory(Long guildId, Long channelId, ChannelLinkCategory c) throws SQLException {
        String sql = "UPDATE channel_links SET categories = categories | ? WHERE guild_id = ? AND channel_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setInt(1, c.bit);
            ps.setLong(2, guildId);
            ps.setLong(3, channelId);
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean disableCategory(Long guildId, Long channelId, ChannelLinkCategory c) throws SQLException {
        String sql = "UPDATE channel_links SET categories = categories & ~? WHERE guild_id = ? AND channel_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setInt(1, c.bit);
            ps.setLong(2, guildId);
            ps.setLong(3, channelId);
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean toggleCategory(Long guildId, Long channelId, ChannelLinkCategory c) throws SQLException {
        String sql = "UPDATE channel_links SET categories = (categories | ?) - (categories & ?) WHERE guild_id = ? AND channel_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setInt(1, c.bit);
            ps.setInt(2, c.bit);
            ps.setLong(3, guildId);
            ps.setLong(4, channelId);
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean deleteChannel(Long guildId, Long channelId) throws SQLException {
        String sql = "DELETE FROM channel_links WHERE guild_id = ? AND channel_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setLong(1, guildId);
            ps.setLong(2, channelId);
            return ps.executeUpdate() != 0;
        }
    }
}

package onl.jami.averUtils.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ChannelLinkRepo {
    public static EnumSet<ChannelLinkCategory> getCategories(long guildId, long channelId) throws SQLException {
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

    public record ChannelData(long guildId, long channelId, String webhookUrl) {}

    public static List<ChannelData> getChannelsFor(ChannelLinkCategory c) throws SQLException {
        String sql = "SELECT guild_id, channel_id, webhook_url FROM channel_links WHERE (categories & ?) != 0";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setInt(1, c.bit);
            try (ResultSet rs = ps.executeQuery()) {
                List<ChannelData> channels = new ArrayList<>();
                while (rs.next()) {
                    channels.add(new ChannelData(rs.getLong("guild_id"), rs.getLong("channel_id"), rs.getString("webhook_url")));
                }
                return channels;
            }
        }
    }

    public static boolean setCategories(long guild_id, long channel_id, Set<ChannelLinkCategory> categories, String webhookUrl) throws SQLException {
        String sql = "INSERT INTO channel_links (guild_id, channel_id, categories, webhook_url) VALUES (?, ?, ?, ?) ON CONFLICT(guild_id, channel_id) DO UPDATE SET categories = excluded.categories, webhook_url = excluded.webhook_url";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setLong(1, guild_id);
            ps.setLong(2, channel_id);
            ps.setInt(3, ChannelLinkCategory.toMask(categories));
            ps.setString(4, webhookUrl);
            return ps.executeUpdate() != 0;
        }
    }

    public static boolean deleteChannel(long guildId, long channelId) throws SQLException {
        String sql = "DELETE FROM channel_links WHERE guild_id = ? AND channel_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setLong(1, guildId);
            ps.setLong(2, channelId);
            return ps.executeUpdate() != 0;
        }
    }
}

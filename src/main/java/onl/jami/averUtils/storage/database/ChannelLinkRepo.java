package onl.jami.averUtils.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ChannelLinkRepo {
    public record ChannelData(long guildId, long channelId, EnumSet<ChannelLinkCategory> categories, String webhookUrl) {}

    public static List<ChannelData> getChannelsFor(ChannelLinkCategory c) throws SQLException {
        String sql = "SELECT guild_id, channel_id, categories, webhook_url FROM channel_links WHERE (categories & ?) != 0";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setInt(1, c.bit);
            try (ResultSet rs = ps.executeQuery()) {
                List<ChannelData> channels = new ArrayList<>();
                while (rs.next()) {
                    channels.add(new ChannelData(rs.getLong("guild_id"), rs.getLong("channel_id"), ChannelLinkCategory.fromMask(rs.getInt("categories")), rs.getString("webhook_url")));
                }
                return channels;
            }
        }
    }

    public static void setCategories(long guild_id, long channel_id, Set<ChannelLinkCategory> categories, String webhookUrl) throws SQLException {
        String sql = "INSERT INTO channel_links (guild_id, channel_id, categories, webhook_url) VALUES (?, ?, ?, ?) ON CONFLICT(guild_id, channel_id) DO UPDATE SET categories = excluded.categories, webhook_url = excluded.webhook_url";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setLong(1, guild_id);
            ps.setLong(2, channel_id);
            ps.setInt(3, ChannelLinkCategory.toMask(categories));
            ps.setString(4, webhookUrl);
            ps.executeUpdate();
        }
    }

    public static void setWebhook(long guild_id, long channel_id, String webhookUrl) throws SQLException {
        String sql = "UPDATE channel_links SET webhook_url = ? WHERE guild_id = ? AND channel_id = ?";
        try (PreparedStatement ps = Driver.getConnection().prepareStatement(sql)) {
            ps.setString(1, webhookUrl);
            ps.setLong(2, guild_id);
            ps.setLong(3, channel_id);
            ps.executeUpdate();
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

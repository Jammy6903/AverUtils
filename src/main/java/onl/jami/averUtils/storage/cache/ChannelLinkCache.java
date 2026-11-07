package onl.jami.averUtils.storage.cache;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.ChannelLinkCategory;
import onl.jami.averUtils.storage.database.ChannelLinkRepo;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelLinkCache {
    private static final Map<ChannelLinkCategory, List<ChannelLinkRepo.ChannelData>> channels = new ConcurrentHashMap<>();

    public static List<ChannelLinkRepo.ChannelData> getChannels(ChannelLinkCategory category) {
        return channels.get(category);
    }

    public static boolean isLinked(long guildId, long channelId, ChannelLinkCategory category) {
        return channels.getOrDefault(category, List.of()).stream().anyMatch(c -> c.guildId() == guildId && c.channelId() == channelId);
    }

    public static void updateChannels() {
        try {
            for (ChannelLinkCategory category : ChannelLinkCategory.values()) {
                channels.put(category, ChannelLinkRepo.getChannelsFor(category));
            }
        } catch (SQLException exception) {
            AverUtils.logWarning(Messages.getString("errors.database.channel_links"), exception);
        }
    }

}

package onl.jami.averUtils.storage.database;

import java.util.EnumSet;
import java.util.Set;

public enum ChannelLinkCategory {
    CHAT(1 << 0),
    JOIN(2 << 0),
    QUIT(4 << 0),
    DEATH(8 << 0),
    ADVANCEMENT(16 << 0),
    STARTUP(32 << 0),
    SHUTDOWN(64 << 0),
    CONSOLE(128 << 0);

    public final int bit;
    ChannelLinkCategory(int bit) {this.bit = bit;}

    public static int toMask(Set<ChannelLinkCategory> set) {
        int mask = 0;
        for (ChannelLinkCategory c : set) mask |= c.bit;
        return mask;
    }

    public static EnumSet<ChannelLinkCategory> fromMask(int mask) {
        EnumSet<ChannelLinkCategory> set = EnumSet.noneOf(ChannelLinkCategory.class);
        for (ChannelLinkCategory c : ChannelLinkCategory.values()) {
            if ((mask & c.bit) != 0) set.add(c);
        }
        return set;
    }
}

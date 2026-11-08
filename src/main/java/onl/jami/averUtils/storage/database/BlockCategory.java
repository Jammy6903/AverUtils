package onl.jami.averUtils.storage.database;

import java.util.EnumSet;
import java.util.Set;

public enum BlockCategory {
    CHAT(1 << 0),
    MESSAGES(2 << 0),
    TPA(4 << 0),
    PVP(8 << 0);

    public final int bit;
    BlockCategory(int bit) {this.bit = bit;}

    public static int toMask(Set<BlockCategory> set) {
        int mask = 0;
        for (BlockCategory c : set) mask |= c.bit;
        return mask;
    }

    public static EnumSet<BlockCategory> fromMask(int mask) {
        EnumSet<BlockCategory> set = EnumSet.noneOf(BlockCategory.class);
        for (BlockCategory c : BlockCategory.values()) {
            if ((mask & c.bit) != 0) set.add(c);
        }
        return set;
    }
}

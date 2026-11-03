package onl.jami.averUtils.teleportation.tpa;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TpRequestCache {

    public record TpRequest(UUID requester, Direction direction) {
        public enum Direction {TO_RECIPIENT, TO_REQUESTER}
    }

    private static final Cache<UUID, TpRequest> tpRequests = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();;

    public static TpRequest getLatestRequest(UUID uuid) {
        return tpRequests.getIfPresent(uuid);
    }

    public static void removeLatestRequest(UUID uuid) {
        tpRequests.invalidate(uuid);
    }

    public static void addRequest(UUID recipient, TpRequest request) {
        tpRequests.put(recipient, request);
    }

}

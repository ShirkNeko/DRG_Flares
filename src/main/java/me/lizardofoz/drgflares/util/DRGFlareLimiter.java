package me.lizardofoz.drgflares.util;

import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.entity.FlareEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Here's how this whole thing works:<br/>
 * During a tick, each flare reports itself to the DRGFlareLimiter, and the oldest flare gets remembered.<br/>
 * Upon a new tick, if there are more than N flares, the oldest one (which we've remembered during the report process) gets deleted.<br/>
 * This means each tick we can delete 1 flare above the limit.
 */
public class DRGFlareLimiter
{
    private static final Map<Player, TrackerInstance> playerMap = new HashMap<>();

    private DRGFlareLimiter() { }

    public static void initOrReset()
    {
        playerMap.clear();
        playerMap.put(null, new TrackerInstance());
    }

    public static void onPlayerJoin(Player player)
    {
        playerMap.put(player, new TrackerInstance());
    }

    public static void onPlayerLeave(Player player)
    {
        playerMap.remove(player);
    }

    public static void tick()
    {
        if (ServerSettings.CURRENT.flareEntityLimitPerPlayer.value <= 0)
            return;
        for (TrackerInstance tracker : playerMap.values())
        {
            if (tracker.flareEntityCount > ServerSettings.CURRENT.flareEntityLimitPerPlayer.value && tracker.oldestFlare != null)
                tracker.oldestFlare.discard();
            tracker.reset();
        }
    }

    public static void reportFlare(FlareEntity entity)
    {
        if (ServerSettings.CURRENT.flareEntityLimitPerPlayer.value <= 0)
            return;

        TrackerInstance aspect = null;
        Entity owner = entity.getOwner();
        if (owner instanceof Player)
            aspect = playerMap.get(owner);
        if (aspect == null)
            aspect = playerMap.get(null);

        aspect.flareEntityCount++;
        if (entity.lifespan > aspect.oldestFlareLifetime)
        {
            aspect.oldestFlareLifetime = entity.lifespan;
            aspect.oldestFlare = entity;
        }
    }

    private static class TrackerInstance
    {
        private int flareEntityCount;
        private int oldestFlareLifetime;
        private FlareEntity oldestFlare;

        private void reset()
        {
            flareEntityCount = 0;
            oldestFlareLifetime = -1;
            oldestFlare = null;
        }
    }
}

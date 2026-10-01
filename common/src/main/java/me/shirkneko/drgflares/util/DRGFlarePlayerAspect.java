package me.shirkneko.drgflares.util;

import me.shirkneko.drgflares.registry.DRGFlareRegistry;
import me.shirkneko.drgflares.config.ServerSettings;
import me.shirkneko.drgflares.entity.FlareEntity;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class DRGFlarePlayerAspect
{
    private static final Map<Player, DRGFlarePlayerAspect> playerMap = new HashMap<>();

    public static final DRGFlarePlayerAspect clientLocal = new DRGFlarePlayerAspect();

    public static void initOrReset()
    {
        playerMap.clear();
        clientLocal.resetInst();
    }

    public static void onPlayerJoin(Player player)
    {
        playerMap.put(player, new DRGFlarePlayerAspect());
    }

    public static void onPlayerLeave(Player player)
    {
        playerMap.remove(player);
    }

    public static DRGFlarePlayerAspect get(Player player)
    {
        return playerMap.get(player);
    }

    public static void tickAll()
    {
        for (DRGFlarePlayerAspect value : playerMap.values())
            value.tick();
    }

    private int flaresLeft;
    private int flareRegenStatus;

    private DRGFlarePlayerAspect()
    {
        resetInst();
    }

    private void resetInst()
    {
        flaresLeft = ServerSettings.CURRENT.regeneratingFlaresMaxCharges.value;
        flareRegenStatus = 0;
    }

    public int getFlaresLeft()
    {
        return flaresLeft;
    }

    public int getFlareRegenStatus()
    {
        return flareRegenStatus;
    }

    public void tick()
    {
        if (flaresLeft < ServerSettings.CURRENT.regeneratingFlaresMaxCharges.value)
        {
            if (flareRegenStatus < ServerSettings.CURRENT.regeneratingFlaresRechargeTime.value * 20)
                flareRegenStatus++;
            else
            {
                flareRegenStatus = 0;
                flaresLeft++;
            }
        }
        else
            flaresLeft = ServerSettings.CURRENT.regeneratingFlaresMaxCharges.value;
    }

    public void reduceFlareCount(Player player)
    {
        if (!DRGFlaresUtil.hasUnlimitedRegeneratingFlares(player) && ServerSettings.CURRENT.regeneratingFlaresEnabled.value)
            flaresLeft = Math.max(0, flaresLeft - 1);
    }

    public boolean checkFlareToss(Player player)
    {
        return flaresLeft > 0 || DRGFlaresUtil.hasUnlimitedRegeneratingFlares(player);
    }

    public void tryThrowRegeneratingFlare(Player player, FlareColor color)
    {
        if (!checkFlareToss(player) || DRGFlaresUtil.isRegenFlareOnCooldown(player) || player.isSpectator())
            return;
        FlareEntity.throwFlare(player, color);
        Map<FlareColor, Item> itemTypes = DRGFlareRegistry.getFlareItemTypes();
        player.getCooldowns().addCooldown(itemTypes.get(FlareColor.RED), 5);
        player.awardStat(Stats.ITEM_USED.get(itemTypes.get(color)));
        reduceFlareCount(player);
    }
}

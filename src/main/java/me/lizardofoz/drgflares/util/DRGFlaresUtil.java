package me.lizardofoz.drgflares.util;

import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.DRGFlares;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.item.FlareItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DRGFlaresUtil
{
    private DRGFlaresUtil() { }

    public static void removeFlareRecipes(RecipeManager recipeManager)
    {
        try
        {
            List<RecipeHolder<?>> filtered = new ArrayList<>();
            for (RecipeHolder<?> holder : recipeManager.getRecipes())
            {
                if (!DRGFlares.MOD_ID.equals(holder.id().getNamespace()))
                    filtered.add(holder);
            }
            recipeManager.replaceRecipes(filtered);
        }
        catch (Throwable e)
        {
            DRGFlares.LOGGER.error("Failed to remove flare recipes", e);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isOnRemoteServer()
    {
        try
        {
            return !Minecraft.getInstance().getConnection().getConnection().isMemoryConnection();
        }
        catch (Throwable ignored)
        {
            return false;
        }
    }

    public static int getVoidDamageLevel(Level world)
    {
        return world.getMinBuildHeight() - 64;
    }

    public static boolean hasUnlimitedRegeneratingFlares(Player player)
    {
        return (player.getAbilities().instabuild && ServerSettings.CURRENT.creativeUnlimitedRegeneratingFlares.value) || ServerSettings.CURRENT.unlimitedSurvivalFlares();
    }

    public static boolean isRegenFlareOnCooldown(Player player)
    {
        return player.getCooldowns().isOnCooldown(DRGFlareRegistry.getFlareItem(FlareColor.RED));
    }

    public static FlareColor getFlareColorFromItem(ItemStack stack)
    {
        for (Map.Entry<FlareColor, Item> entry : DRGFlareRegistry.getFlareItemTypes().entrySet())
        {
            if (stack.getItem().equals(entry.getValue()))
                return entry.getKey();
        }
        return FlareColor.RED;
    }

    public static boolean tryFlare(Player player, List<ItemStack> inventorySection)
    {
        for (ItemStack itemStack : inventorySection)
        {
            Item item = itemStack.getItem();
            if (item instanceof FlareItem)
            {
                if (player.getCooldowns().isOnCooldown(item))
                    return true;
                FlareEntity.throwFlare(player, DRGFlaresUtil.getFlareColorFromItem(itemStack));
                if (!player.getAbilities().instabuild)
                    itemStack.shrink(1);
                player.getCooldowns().addCooldown(item, 5);
                player.awardStat(Stats.ITEM_USED.get(item));
                return true;
            }
        }
        return false;
    }

    //We had to move these 2 methods outside, so that a Dedicated Server won't try to load Client-Only classes
    @OnlyIn(Dist.CLIENT)
    public static void playSoundFromEntityOnClient(Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch)
    {
        Minecraft.getInstance().getSoundManager().play(new EntityBoundSoundInstance(sound, category, volume, pitch, entity, new Random().nextLong()));
    }

    @OnlyIn(Dist.CLIENT)
    public static void addEntityOnClient(Level world, Entity entity)
    {
        ((ClientLevel) world).addEntity(entity);
    }
}

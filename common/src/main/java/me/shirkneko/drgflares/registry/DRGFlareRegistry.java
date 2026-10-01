package me.shirkneko.drgflares.registry;

import me.shirkneko.drgflares.block.FlareLightBlockEntity;
import me.shirkneko.drgflares.entity.FlareEntity;
import me.shirkneko.drgflares.util.FlareColor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Map;

/**
 * Loader-agnostic registry abstraction. The shared ("common") code accesses
 * registered game content through these static accessors; each mod loader
 * provides a concrete implementation (e.g. NeoForgeDRGFlareRegistry) and
 * registers it via {@link #setInstance(DRGFlareRegistry)} at startup.
 */
public abstract class DRGFlareRegistry
{
    private static DRGFlareRegistry instance;

    public static void setInstance(DRGFlareRegistry instance)
    {
        DRGFlareRegistry.instance = instance;
    }

    public static EntityType<FlareEntity> getFlareEntityType()
    {
        return instance.flareEntityType();
    }

    public static Map<FlareColor, Item> getFlareItemTypes()
    {
        return instance.flareItemTypes();
    }

    public static Item getFlareItem(FlareColor color)
    {
        return instance.flareItem(color);
    }

    public static Block getLightSourceBlockType()
    {
        return instance.lightSourceBlockType();
    }

    public static BlockEntityType<FlareLightBlockEntity> getLightSourceBlockEntityType()
    {
        return instance.lightSourceBlockEntityType();
    }

    public static SoundEvent getFlareThrowSound()
    {
        return instance.flareThrowSound();
    }

    public static SoundEvent getFlareBounceSound()
    {
        return instance.flareBounceSound();
    }

    public static SoundEvent getFlareBounceFarSound()
    {
        return instance.flareBounceFarSound();
    }

    protected abstract EntityType<FlareEntity> flareEntityType();

    protected abstract Map<FlareColor, Item> flareItemTypes();

    protected abstract Item flareItem(FlareColor color);

    protected abstract Block lightSourceBlockType();

    protected abstract BlockEntityType<FlareLightBlockEntity> lightSourceBlockEntityType();

    protected abstract SoundEvent flareThrowSound();

    protected abstract SoundEvent flareBounceSound();

    protected abstract SoundEvent flareBounceFarSound();
}

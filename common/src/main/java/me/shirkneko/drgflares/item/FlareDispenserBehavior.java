package me.shirkneko.drgflares.item;

import me.shirkneko.drgflares.registry.DRGFlareRegistry;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;

public final class FlareDispenserBehavior
{
    private FlareDispenserBehavior()
    {
    }

    public static void initialize()
    {
        for (Item item : DRGFlareRegistry.getFlareItemTypes().values())
            DispenserBlock.registerBehavior(item, new ProjectileDispenseBehavior(item));
    }
}

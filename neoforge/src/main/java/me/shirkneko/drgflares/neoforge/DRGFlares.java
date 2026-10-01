package me.shirkneko.drgflares.neoforge;

import me.shirkneko.drgflares.DRGFlaresCommon;
import me.shirkneko.drgflares.item.FlareDispenserBehavior;
import me.shirkneko.drgflares.neoforge.client.ClientSetup;
import me.shirkneko.drgflares.neoforge.packet.PacketStuff;
import me.shirkneko.drgflares.registry.DRGFlareRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(DRGFlaresCommon.MOD_ID)
public final class DRGFlares
{
    public DRGFlares(IEventBus modBus, Dist dist)
    {
        DRGFlareRegistry.setInstance(new NeoForgeDRGFlareRegistry());
        NeoForgeDRGFlareRegistry.register(modBus);

        modBus.addListener(PacketStuff::registerPayloads);
        modBus.addListener(DRGFlares::onCommonSetup);

        NeoForge.EVENT_BUS.register(new ServerEventHandler());

        if (dist.isClient())
        {
            ClientSetup.init(modBus);
            NeoForge.EVENT_BUS.register(ClientEventHandler.INSTANCE);
        }
    }

    private static void onCommonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(FlareDispenserBehavior::initialize);
    }
}

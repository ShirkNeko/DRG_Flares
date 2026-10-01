package me.lizardofoz.drgflares;

import me.lizardofoz.drgflares.client.ClientSetup;
import me.lizardofoz.drgflares.item.FlareDispenserBehavior;
import me.lizardofoz.drgflares.packet.PacketStuff;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DRGFlares.MOD_ID)
public final class DRGFlares
{
    public static final String MOD_ID = "drg_flares";
    public static final Logger LOGGER = LogManager.getLogger("DRGFlares");

    public DRGFlares(IEventBus modBus, Dist dist)
    {
        DRGFlareRegistry.register(modBus);
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

package me.shirkneko.drgflares.neoforge.client;

import me.shirkneko.drgflares.DRGFlaresCommon;
import me.shirkneko.drgflares.config.PlayerSettings;
import me.shirkneko.drgflares.neoforge.NeoForgeDRGFlareRegistry;
import me.shirkneko.drgflares.registry.DRGFlareRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.fml.ModLoadingContext;

@OnlyIn(Dist.CLIENT)
public final class ClientSetup
{
    private ClientSetup()
    {
    }

    public static void init(IEventBus modBus)
    {
        modBus.addListener(ClientSetup::registerKeyMappings);
        modBus.addListener(ClientSetup::registerRenderers);
        modBus.addListener(ClientSetup::registerGuiLayers);

        if (NeoForgeDRGFlareRegistry.isClothConfigLoaded())
            ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (client, parent) -> SettingsScreen.create(parent));
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event)
    {
        event.register(PlayerSettings.INSTANCE.throwFlareKey);
        if (NeoForgeDRGFlareRegistry.isClothConfigLoaded())
            event.register(PlayerSettings.INSTANCE.flareModSettingsKey);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(DRGFlareRegistry.getFlareEntityType(), FlareEntityRenderer::new);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event)
    {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(DRGFlaresCommon.MOD_ID, "flare_hud"), FlareHUDRenderer::render);
    }
}

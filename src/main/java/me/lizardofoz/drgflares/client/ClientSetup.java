package me.lizardofoz.drgflares.client;

import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.DRGFlares;
import me.lizardofoz.drgflares.config.PlayerSettings;
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

        if (DRGFlareRegistry.isClothConfigLoaded())
            ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (client, parent) -> SettingsScreen.create(parent));
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event)
    {
        event.register(PlayerSettings.INSTANCE.throwFlareKey);
        if (DRGFlareRegistry.isClothConfigLoaded())
            event.register(PlayerSettings.INSTANCE.flareModSettingsKey);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(DRGFlareRegistry.getFlareEntityType(), FlareEntityRenderer::new);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event)
    {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(DRGFlares.MOD_ID, "flare_hud"), FlareHUDRenderer::render);
    }
}

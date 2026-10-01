package me.shirkneko.drgflares.neoforge;

import me.shirkneko.drgflares.DRGFlaresCommon;
import me.shirkneko.drgflares.block.FlareLightBlock;
import me.shirkneko.drgflares.config.ServerSettings;
import me.shirkneko.drgflares.neoforge.packet.PacketStuff;
import me.shirkneko.drgflares.util.DRGFlareLimiter;
import me.shirkneko.drgflares.util.DRGFlarePlayerAspect;
import me.shirkneko.drgflares.util.DRGFlaresUtil;
import me.shirkneko.drgflares.util.ServerSyncMode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class ServerEventHandler
{
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event)
    {
        MinecraftServer server = event.getServer();
        DRGFlaresCommon.serverSyncMode = ServerSyncMode.SYNC_WITH_SERVER;
        DRGFlareLimiter.initOrReset();
        DRGFlarePlayerAspect.initOrReset();
        ServerSettings.CURRENT.loadFromJson(ServerSettings.LOCAL.asJson());
        FlareLightBlock.refreshBlockStates();
        if (!ServerSettings.CURRENT.flareRecipesInSurvival.value)
            DRGFlaresUtil.removeFlareRecipes(server.getRecipeManager());
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event)
    {
        DRGFlareLimiter.tick();
        DRGFlarePlayerAspect.tickAll();
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        DRGFlareLimiter.onPlayerJoin(player);
        DRGFlarePlayerAspect.onPlayerJoin(player);
        PacketStuff.sendSettingsSync(player);
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event)
    {
        DRGFlareLimiter.onPlayerLeave(event.getEntity());
        DRGFlarePlayerAspect.onPlayerLeave(event.getEntity());
    }
}

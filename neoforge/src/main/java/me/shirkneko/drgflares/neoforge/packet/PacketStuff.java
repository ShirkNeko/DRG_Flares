package me.shirkneko.drgflares.neoforge.packet;

import me.shirkneko.drgflares.config.ServerSettings;
import me.shirkneko.drgflares.util.DRGFlarePlayerAspect;
import me.shirkneko.drgflares.util.FlareColor;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PacketStuff
{
    private PacketStuff() { }

    public static void registerPayloads(RegisterPayloadHandlersEvent event)
    {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ThrowFlarePayload.TYPE, ThrowFlarePayload.STREAM_CODEC, ThrowFlarePayload::handle);
        registrar.playToClient(SyncServerSettingsPayload.TYPE, SyncServerSettingsPayload.STREAM_CODEC, SyncServerSettingsPayload::handle);
    }

    public static void sendSettingsSync(ServerPlayer player)
    {
        PacketDistributor.sendToPlayer(player, new SyncServerSettingsPayload(ServerSettings.LOCAL.asJson()));
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendFlareThrow(FlareColor color)
    {
        PacketDistributor.sendToServer(new ThrowFlarePayload(color));
        DRGFlarePlayerAspect.clientLocal.reduceFlareCount(Minecraft.getInstance().player);
    }
}

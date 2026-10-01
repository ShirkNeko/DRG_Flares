package me.lizardofoz.drgflares.packet;

import io.netty.buffer.ByteBuf;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.DRGFlarePlayerAspect;
import me.lizardofoz.drgflares.util.DRGFlaresUtil;
import me.lizardofoz.drgflares.util.FlareColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ThrowFlarePayload(byte colorId) implements CustomPacketPayload
{
    public static final Type<ThrowFlarePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("drg_flares", "throw_flare"));

    public static final StreamCodec<ByteBuf, ThrowFlarePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE,
            ThrowFlarePayload::colorId,
            ThrowFlarePayload::new
    );

    public ThrowFlarePayload(FlareColor color)
    {
        this((byte) color.id);
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(ThrowFlarePayload payload, IPayloadContext context)
    {
        Player player = context.player();
        if (player == null)
            return;
        FlareColor color = FlareColor.byId(payload.colorId());

        if (ServerSettings.CURRENT.regeneratingFlaresEnabled.value)
        {
            DRGFlarePlayerAspect playerAspect = DRGFlarePlayerAspect.get(player);
            if (playerAspect != null)
                playerAspect.tryThrowRegeneratingFlare(player, color);
        }
        else
        {
            //Here we exploit the fact that when any tryFlare returns true, the subsequent tryFlare-s never get called
            if (DRGFlaresUtil.tryFlare(player, player.getInventory().offhand) || DRGFlaresUtil.tryFlare(player, player.getInventory().items))
                return;
            //Inventorio integration has been removed (no 1.21.1 build exists for Inventorio)
        }
    }
}

package me.lizardofoz.drgflares.packet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.ServerSyncMode;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncServerSettingsPayload(String settingsJson) implements CustomPacketPayload
{
    public static final Type<SyncServerSettingsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("drg_flares", "sync_settings"));

    public static final StreamCodec<ByteBuf, SyncServerSettingsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SyncServerSettingsPayload::settingsJson,
            SyncServerSettingsPayload::new
    );

    public SyncServerSettingsPayload(JsonObject settings)
    {
        this(settings.toString());
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(SyncServerSettingsPayload payload, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            JsonObject settings = new Gson().fromJson(payload.settingsJson(), JsonObject.class);
            DRGFlareRegistry.serverSyncMode = ServerSyncMode.SYNC_WITH_SERVER;
            ServerSettings.CURRENT.loadFromJson(settings);
            FlareLightBlock.refreshBlockStates();
        });
    }
}

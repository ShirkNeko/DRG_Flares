package me.shirkneko.drgflares.neoforge;

import me.shirkneko.drgflares.DRGFlaresCommon;
import me.shirkneko.drgflares.block.FlareLightBlock;
import me.shirkneko.drgflares.config.PlayerSettings;
import me.shirkneko.drgflares.config.ServerSettings;
import me.shirkneko.drgflares.neoforge.client.SettingsScreen;
import me.shirkneko.drgflares.neoforge.packet.PacketStuff;
import me.shirkneko.drgflares.util.DRGFlareLimiter;
import me.shirkneko.drgflares.util.DRGFlarePlayerAspect;
import me.shirkneko.drgflares.util.DRGFlaresUtil;
import me.shirkneko.drgflares.util.FlareColor;
import me.shirkneko.drgflares.util.ServerSyncMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@OnlyIn(Dist.CLIENT)
public final class ClientEventHandler
{
    public static final ClientEventHandler INSTANCE = new ClientEventHandler();

    private ClientEventHandler()
    {
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event)
    {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.isPaused())
            return;
        DRGFlareLimiter.tick();
        DRGFlarePlayerAspect.clientLocal.tick();

        if (NeoForgeDRGFlareRegistry.isClothConfigLoaded() && PlayerSettings.INSTANCE.flareModSettingsKey.consumeClick())
            client.setScreen(SettingsScreen.create(client.screen));

        if (PlayerSettings.INSTANCE.throwFlareKey.consumeClick() && !DRGFlaresUtil.isRegenFlareOnCooldown(player))
        {
            if (DRGFlarePlayerAspect.clientLocal.checkFlareToss(player))
            {
                FlareColor flareColor = FlareColor.RandomColorPicker.unwrapRandom(PlayerSettings.INSTANCE.flareColor.value, true);
                if (DRGFlaresCommon.serverSyncMode != ServerSyncMode.SYNC_WITH_SERVER && ServerSettings.CURRENT.regeneratingFlaresEnabled.value)
                    DRGFlarePlayerAspect.clientLocal.tryThrowRegeneratingFlare(Minecraft.getInstance().player, flareColor);
                else
                    PacketStuff.sendFlareThrow(flareColor);
            }
            else if (ServerSettings.CURRENT.regeneratingFlaresEnabled.value)
                player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, PlayerSettings.INSTANCE.flareSoundVolume.value / 1234f, 1.7f);
        }
    }

    @SubscribeEvent
    public void onClientConnect(ClientPlayerNetworkEvent.LoggingIn event)
    {
        if (DRGFlaresCommon.serverSyncMode == ServerSyncMode.UNDEFINED)
        {
            DRGFlaresCommon.serverSyncMode = ServerSyncMode.CLIENT_ONLY;
            FlareLightBlock.refreshBlockStates();
            ServerSettings.CURRENT.loadFromJson(ServerSettings.LOCAL.asJson());
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event)
    {
        DRGFlaresCommon.serverSyncMode = ServerSyncMode.UNDEFINED;
    }
}

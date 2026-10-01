package me.lizardofoz.drgflares.client;

import com.mojang.blaze3d.systems.RenderSystem;
import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.config.PlayerSettings;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.DRGFlarePlayerAspect;
import me.lizardofoz.drgflares.util.DRGFlaresUtil;
import me.lizardofoz.drgflares.util.FlareColor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FlareHUDRenderer
{
    private static final ResourceLocation HUD_TEXTURE = ResourceLocation.fromNamespaceAndPath("drg_flares", "textures/gui/hud.png");
    private static final Minecraft client = Minecraft.getInstance();

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        if (!ServerSettings.CURRENT.regeneratingFlaresEnabled.value || client.player == null || client.player.isSpectator())
            return;

        int widgetX = (int) (client.getWindow().getGuiScaledWidth() * PlayerSettings.INSTANCE.flareUISlotX.value);
        int widgetY = (int) (client.getWindow().getGuiScaledHeight() * PlayerSettings.INSTANCE.flareUISlotY.value) - 19;
        Component keyHintLabel = PlayerSettings.INSTANCE.throwFlareKey.getTranslatedKeyMessage();
        boolean shouldRenderKeybindHint = PlayerSettings.INSTANCE.flareButtonHint.value && keyHintLabel.getString().length() == 1;
        FlareColor flareColor = FlareColor.RandomColorPicker.unwrapRandom(PlayerSettings.INSTANCE.flareColor.value, false);
        ItemStack flareDisplayStack = new ItemStack(DRGFlareRegistry.getFlareItem(flareColor));
        Font font = client.font;

        //Frame
        RenderSystem.enableBlend();
        guiGraphics.blit(HUD_TEXTURE, widgetX - 3, widgetY - 3, -200, 0, 0, 22, 22, 32, 32);
        if (shouldRenderKeybindHint)
            guiGraphics.blit(HUD_TEXTURE, widgetX + 12, widgetY - 6, -200, 22, 0, 10, 10, 32, 32);

        guiGraphics.renderItem(flareDisplayStack, widgetX, widgetY);

        if (!DRGFlaresUtil.hasUnlimitedRegeneratingFlares(client.player))
        {
            //Progress Bar
            int count = DRGFlarePlayerAspect.clientLocal.getFlaresLeft();
            int currentRegenStatus = DRGFlarePlayerAspect.clientLocal.getFlareRegenStatus();
            int regenBarMaxValue = ServerSettings.CURRENT.regeneratingFlaresRechargeTime.value * 20;
            if (count < ServerSettings.CURRENT.regeneratingFlaresMaxCharges.value)
            {
                float h = Math.max(0.0F, currentRegenStatus / (float) regenBarMaxValue);
                int i = Math.round(currentRegenStatus * 12.0F / regenBarMaxValue);
                int j = Mth.hsvToRgb(h / 3, 1, 1);
                guiGraphics.fill(widgetX + 1, widgetY + 2, widgetX + 3, widgetY + 15, 0xFF000000);
                guiGraphics.fill(widgetX + 1, widgetY + 14 - i, widgetX + 2, widgetY + 14, 0xFF000000 | (j & 0xFFFFFF));
            }

            //Amount Text
            String countText = String.valueOf(count);
            guiGraphics.drawString(font, countText, widgetX + 19 - 2 - font.width(countText), widgetY + 6 + 3, 16777215, true);
        }

        //Keybind Hint Text
        if (shouldRenderKeybindHint)
        {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(0.7f, 0.7f, 0.7f);
            guiGraphics.drawString(font, keyHintLabel, (int) ((widgetX + 15) / 0.7f), (int) ((widgetY - 4) / 0.7f), 16777215, true);
            guiGraphics.pose().popPose();
        }
    }
}

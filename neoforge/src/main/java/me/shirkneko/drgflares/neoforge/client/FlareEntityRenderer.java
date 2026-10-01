package me.shirkneko.drgflares.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.shirkneko.drgflares.entity.FlareEntity;
import me.shirkneko.drgflares.util.FlareColor;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class FlareEntityRenderer extends EntityRenderer<FlareEntity>
{
    private final Map<FlareColor, ResourceLocation> TEXTURES = new HashMap<>();
    private final int MAX_LIGHT = LightTexture.pack(15, 15);

    private final ModelPart rodModel;
    private final ModelPart metalModel;

    public FlareEntityRenderer(EntityRendererProvider.Context context)
    {
        super(context);

        //The reason to have 2 models is that the rod itself remains glowing in the dark
        MeshDefinition rodMesh = new MeshDefinition();
        rodMesh.getRoot().addOrReplaceChild("rod", CubeListBuilder.create().texOffs(0, 12).addBox(-1, -9, -1, 2, 13, 2), PartPose.ZERO);
        rodModel = LayerDefinition.create(rodMesh, 32, 32).bakeRoot();

        MeshDefinition metalMesh = new MeshDefinition();
        PartDefinition metalRoot = metalMesh.getRoot();
        metalRoot.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 6).addBox(-2, -8, -2, 4, 2, 4), PartPose.ZERO);
        metalRoot.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 0).addBox(-2, 1, -2, 4, 2, 4), PartPose.ZERO);
        metalModel = metalRoot.bake(32, 32);

        for (FlareColor color : FlareColor.colors)
            TEXTURES.put(color, ResourceLocation.fromNamespaceAndPath("drg_flares", "textures/block/drg_flare_" + color + ".png"));
    }

    @Override
    public void render(FlareEntity entity, float yaw, float subTickTime, PoseStack matrices, MultiBufferSource vertexConsumers, int light)
    {
        super.render(entity, yaw, subTickTime, matrices, vertexConsumers, light);
        Vec3 velocity = entity.getDeltaMovement().scale(10);

        entity.frame(subTickTime);

        matrices.pushPose();
        matrices.translate(0, 0.1f, 0);
        //Here's a trick - we want each flare to end up with a different rotation when laying on the floor.
        matrices.mulPose(Axis.YP.rotationDegrees(entity.getId() * 119));
        matrices.mulPose(Axis.XP.rotationDegrees(entity.rotation));
        matrices.mulPose(Axis.XP.rotationDegrees(Mth.sin((float) (velocity.x + 90) / 15) * 360));
        matrices.mulPose(Axis.YP.rotationDegrees(Mth.cos((float) (velocity.y + velocity.x * 200) / 15) * 360));
        matrices.scale(0.6f, 0.6f, 0.6f);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.entityCutout(TEXTURES.get(entity.getColor())));
        rodModel.render(matrices, vertexConsumer, MAX_LIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        metalModel.render(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        matrices.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(FlareEntity entity)
    {
        return TEXTURES.get(entity.getColor());
    }
}

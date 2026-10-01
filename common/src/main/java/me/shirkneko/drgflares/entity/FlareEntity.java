package me.shirkneko.drgflares.entity;

import me.shirkneko.drgflares.registry.DRGFlareRegistry;
import me.shirkneko.drgflares.DRGFlaresCommon;
import me.shirkneko.drgflares.block.FlareLightBlock;
import me.shirkneko.drgflares.block.FlareLightBlockEntity;
import me.shirkneko.drgflares.config.PlayerSettings;
import me.shirkneko.drgflares.config.ServerSettings;
import me.shirkneko.drgflares.util.DRGFlareLimiter;
import me.shirkneko.drgflares.util.DRGFlaresUtil;
import me.shirkneko.drgflares.util.FlareColor;
import me.shirkneko.drgflares.util.ServerSyncMode;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class FlareEntity extends ThrowableProjectile
{
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(FlareEntity.class, EntityDataSerializers.INT);

    //Persistent Entity Data
    public int lifespan = -1;

    //Temporary/Service Entity Data
    public int bounceCount = 1;
    public float rotation = 0;
    private int idleTicks = 0;
    private float prevPartialTick = 0;

    private BlockPos lightBlockPos = null;
    private BlockPos lastHitBlockPos = null;
    private BlockState lastHitBlockState = null;

    public FlareEntity(EntityType<? extends FlareEntity> entityType, Level level)
    {
        super(entityType, level);
        this.entityData.set(DATA_COLOR, FlareColor.RED.id);
    }

    public FlareEntity(Level level, FlareColor color)
    {
        super(DRGFlareRegistry.getFlareEntityType(), level);
        setColor(FlareColor.RandomColorPicker.unwrapRandom(color, false));
    }

    private FlareEntity(LivingEntity owner, FlareColor color)
    {
        super(DRGFlareRegistry.getFlareEntityType(), owner, owner.level());
        setColor(FlareColor.RandomColorPicker.unwrapRandom(color, false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        builder.define(DATA_COLOR, FlareColor.RED.id);
    }

    public FlareColor getColor()
    {
        return FlareColor.byId(this.entityData.get(DATA_COLOR));
    }

    public void setColor(FlareColor color)
    {
        this.entityData.set(DATA_COLOR, color.id);
    }

    //Note: we call this only on the server's side
    //UPD: if this is called on the client's side, we know it has to be in the client-side-only mode
    public static FlareEntity throwFlare(LivingEntity owner, FlareColor color)
    {
        float throwAngle = ServerSettings.CURRENT.flareThrowAngle.value;
        float pitchModifier = Math.min(throwAngle, Math.max(0, owner.getXRot() + (95 - throwAngle)));

        FlareEntity flareEntity = new FlareEntity(owner, color);
        flareEntity.setPos(flareEntity.getX(), flareEntity.getY() - 0.5, flareEntity.getZ());
        flareEntity.shootFromRotation(owner, owner.getXRot() - pitchModifier, owner.getYRot(), 0, 0.75f * ServerSettings.CURRENT.flareThrowSpeed.value, 1);
        if (!owner.level().isClientSide)
            owner.level().addFreshEntity(flareEntity);
        else
        {
            //EntityId magic to avoid clashing with entityId-s of real entities
            int randomNegativeId = flareEntity.getId() - 100000;
            while (owner.level().getEntity(randomNegativeId) != null)
                randomNegativeId = owner.level().random.nextInt(1000000) - 2000000;
            flareEntity.setId(randomNegativeId);
            DRGFlaresUtil.addEntityOnClient(owner.level(), flareEntity);
        }
        return flareEntity;
    }

    public static FlareEntity make(EntityType<FlareEntity> entityType, Level level)
    {
        return new FlareEntity(entityType, level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag)
    {
        super.addAdditionalSaveData(tag);
        tag.putInt("lifespan", lifespan);
        tag.putShort("color", (short) getColor().id);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag)
    {
        super.readAdditionalSaveData(tag);
        lifespan = tag.getInt("lifespan");
        setColor(FlareColor.byId(tag.getShort("color")));
    }

    @Override
    public boolean isAttackable()
    {
        return false;
    }

    @Override
    protected double getDefaultGravity()
    {
        return 0.024 * ServerSettings.CURRENT.flareGravity.value;
    }

    public void frame(float partialTick)
    {
        if (Minecraft.getInstance().isPaused())
            return;
        while (partialTick <= prevPartialTick)
            prevPartialTick -= 1;
        float delta = partialTick - prevPartialTick;
        prevPartialTick = partialTick;

        if (getDeltaMovement().lengthSqr() < 0.1 && bounceCount > 2)
            rotation = 0;
        else
            rotation += 10.0f * delta / bounceCount;
    }

    public boolean isLit()
    {
        return lifespan < (ServerSettings.CURRENT.secondsUntilDimmingOut.value + ServerSettings.CURRENT.andThenSecondsUntilFizzlingOut.value) * 20;
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult)
    {
        super.onHitBlock(blockHitResult);
        BlockPos hitBlockPos = blockHitResult.getBlockPos();

        bounceCount++;
        //Only play the impact sound on the first contact with a block; the later
        //bounces and the final settle shouldn't keep making noise after landing
        if (lastHitBlockPos == null && level().isClientSide && !isInWater())
        {
            float pitch = 1.1f + level().random.nextFloat() * 0.3f;
            float volume = PlayerSettings.INSTANCE.flareSoundVolume.value / 100.0f;
            float farVolume = volume < 0.25f ? volume * 3 : volume < 0.5f ? volume * 2 : volume;
            DRGFlaresUtil.playSoundFromEntityOnClient(this, DRGFlareRegistry.getFlareBounceSound(), SoundSource.MASTER, volume, pitch);
            DRGFlaresUtil.playSoundFromEntityOnClient(this, DRGFlareRegistry.getFlareBounceFarSound(), SoundSource.MASTER, farVolume * 3, pitch);
        }

        //If we don't disable gravity, a flare will just bob up and down when laying on the ground
        if (hitBlockPos.equals(lastHitBlockPos) && getDeltaMovement().lengthSqr() < 0.01)
        {
            setDeltaMovement(Vec3.ZERO);
            setNoGravity(true);
            return;
        }
        else
        {
            lastHitBlockPos = hitBlockPos;
            lastHitBlockState = level().getBlockState(hitBlockPos);
        }

        double speedDivider = ServerSettings.CURRENT.flareSpeedBounceDivider.value;
        Vec3 velocity = getDeltaMovement();
        if (blockHitResult.getDirection() == Direction.EAST || blockHitResult.getDirection() == Direction.WEST)
            setDeltaMovement(-velocity.x / speedDivider, velocity.y / speedDivider, velocity.z / speedDivider);
        else if (blockHitResult.getDirection() == Direction.UP || blockHitResult.getDirection() == Direction.DOWN)
            setDeltaMovement(velocity.x / speedDivider, -velocity.y / speedDivider, velocity.z / speedDivider);
        else
            setDeltaMovement(velocity.x / speedDivider, velocity.y / speedDivider, -velocity.z / speedDivider);
    }

    @Override
    public void tick()
    {
        int ticksUntilDespawn = ServerSettings.CURRENT.secondsUntilDimmingOut.value
                + ServerSettings.CURRENT.andThenSecondsUntilFizzlingOut.value
                + ServerSettings.CURRENT.andThenSecondsUntilDespawn.value;

        if (++lifespan == 0 && level().isClientSide)
            DRGFlaresUtil.playSoundFromEntityOnClient(this, DRGFlareRegistry.getFlareThrowSound(), SoundSource.MASTER, PlayerSettings.INSTANCE.flareSoundVolume.value / 100.0f, 1);

        if (lifespan > ticksUntilDespawn * 20 || isInLava() || getY() <= DRGFlaresUtil.getVoidDamageLevel(level()))
            discard();

        if (!level().isClientSide || DRGFlaresCommon.serverSyncMode == ServerSyncMode.CLIENT_ONLY)
            DRGFlareLimiter.reportFlare(this);
        else
            frame(0);

        int idleOpt = ServerSettings.CURRENT.secondsUntilIdlingFlareGetsOptimized.value * 20;
        if (getDeltaMovement().lengthSqr() < 0.01 && bounceCount > 2)
            idleTicks++;
        else
            idleTicks = 0;
        if (idleOpt <= 0 || idleTicks < idleOpt)
            super.tick();

        if (isInWater())
        {
            idleTicks = 0;
            bounceCount = 10;              //Stop the flare from spinning in water
            addDeltaMovement(new Vec3(0, 0.04, 0));       //Make it float in water
        }

        if (idleTicks == 20)
            lightBlockPos = null;

        //If the block below the flare has changed or it's in water, then re-enable gravity
        boolean isInsideWaterBlock = level().isWaterAt(blockPosition());
        if (isNoGravity() && (isInsideWaterBlock || !getBlockStateOn().equals(lastHitBlockState)))
        {
            setNoGravity(false);
            idleTicks = 0;
        }

        //If the Flare has fizzled out, we skip the bottom part which is responsible for lighting things up
        if (isLit() && (level().isClientSide || ServerSettings.CURRENT.serverSideLightSources.value))
            spawnLightSource(isInsideWaterBlock);
    }

    private void spawnLightSource(boolean isInWaterBlock)
    {
        try
        {
            if (lightBlockPos == null)
            {
                lightBlockPos = findFreeSpace(level(), blockPosition(), ServerSettings.CURRENT.lightSourceSearchDistance.value);
                if (lightBlockPos == null)
                    return;

                BlockEntity blockEntity = level().getBlockEntity(lightBlockPos);
                if (blockEntity instanceof FlareLightBlockEntity)
                    ((FlareLightBlockEntity) blockEntity).refresh(isInWaterBlock ? 20 : 0);
                else if (lifespan < ServerSettings.CURRENT.secondsUntilDimmingOut.value * 20)
                    level().setBlockAndUpdate(lightBlockPos, FlareLightBlock.getFullBrightnessBlockState());
                else
                    level().setBlockAndUpdate(lightBlockPos, FlareLightBlock.getDimmedOutBlockState());
            }
            else if (checkDistance(lightBlockPos, blockPosition(), ServerSettings.CURRENT.lightSourceRefreshDistance.value))
            {
                BlockEntity blockEntity = level().getBlockEntity(lightBlockPos);
                if (blockEntity instanceof FlareLightBlockEntity)
                {
                    int lightLevel = FlareLightBlock.getLightLevel(level(), lightBlockPos);
                    if (lifespan < ServerSettings.CURRENT.secondsUntilDimmingOut.value * 20)
                    {
                        if (lightLevel != ServerSettings.CURRENT.fullBrightnessLightLevel.value)
                            level().setBlockAndUpdate(lightBlockPos, FlareLightBlock.getFullBrightnessBlockState());
                    }
                    else if (lightLevel != ServerSettings.CURRENT.dimmedLightLevel.value)
                        level().setBlockAndUpdate(lightBlockPos, FlareLightBlock.getDimmedOutBlockState());
                    ((FlareLightBlockEntity) blockEntity).refresh(isInWaterBlock ? 20 : 0);
                }
                else
                    lightBlockPos = null;
            }
            else
                lightBlockPos = null;
        }
        catch (Throwable e)
        {
            DRGFlaresCommon.LOGGER.error("Failed to process a light source block for " + this + " -> " + lightBlockPos, e);
        }
    }

    private boolean checkDistance(BlockPos blockPosA, BlockPos blockPosB, int distance)
    {
        return Math.abs(blockPosA.getX() - blockPosB.getX()) <= distance
                && Math.abs(blockPosA.getY() - blockPosB.getY()) <= distance
                && Math.abs(blockPosA.getZ() - blockPosB.getZ()) <= distance;
    }

    private BlockPos findFreeSpace(Level world, BlockPos blockPos, int maxDistance)
    {
        if (blockPos == null)
            return null;

        int[] offsets = new int[maxDistance * 2 + 1];
        offsets[0] = 0;
        for (int i = 2; i <= maxDistance * 2; i += 2)
        {
            offsets[i - 1] = i / 2;
            offsets[i] = -i / 2;
        }
        for (int x : offsets)
            for (int y : offsets)
                for (int z : offsets)
                {
                    BlockPos offsetPos = blockPos.offset(x, y, z);
                    BlockState state = world.getBlockState(offsetPos);
                    if (state.isAir() || state.getBlock() == DRGFlareRegistry.getLightSourceBlockType())
                        return offsetPos;
                }

        return null;
    }
}

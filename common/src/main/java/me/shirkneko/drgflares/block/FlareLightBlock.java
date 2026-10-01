package me.shirkneko.drgflares.block;

import com.mojang.serialization.MapCodec;
import me.shirkneko.drgflares.registry.DRGFlareRegistry;
import me.shirkneko.drgflares.config.ServerSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class FlareLightBlock extends BaseEntityBlock
{
    public static final IntegerProperty LIGHT_LEVEL = BlockStateProperties.LEVEL;

    private static BlockState fullBrightnessBlockState;
    private static BlockState dimmedOutBlockState;

    public static void refreshBlockStates()
    {
        BlockState defaultState = DRGFlareRegistry.getLightSourceBlockType().defaultBlockState();
        fullBrightnessBlockState = defaultState.setValue(LIGHT_LEVEL, ServerSettings.CURRENT.fullBrightnessLightLevel.value);
        dimmedOutBlockState = defaultState.setValue(LIGHT_LEVEL, ServerSettings.CURRENT.dimmedLightLevel.value);
    }

    public static BlockState getFullBrightnessBlockState()
    {
        return fullBrightnessBlockState;
    }

    public static BlockState getDimmedOutBlockState()
    {
        return dimmedOutBlockState;
    }

    public static int getLightLevel(Level world, BlockPos blockPos)
    {
        try
        {
            return world.getBlockState(blockPos).getValue(FlareLightBlock.LIGHT_LEVEL);
        }
        catch (Throwable e)
        {
            return 0;
        }
    }

    public FlareLightBlock(BlockBehaviour.Properties settings)
    {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return simpleCodec(FlareLightBlock::new);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new FlareLightBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(LIGHT_LEVEL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state)
    {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.INVISIBLE;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos)
    {
        return 1;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return world.isClientSide || ServerSettings.CURRENT.serverSideLightSources.value
                ? createTickerHelper(type, DRGFlareRegistry.getLightSourceBlockEntityType(), FlareLightBlockEntity::staticTick)
                : null;
    }
}

package me.lizardofoz.drgflares;

import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.block.FlareLightBlockEntity;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.item.FlareItem;
import me.lizardofoz.drgflares.packet.PacketStuff;
import me.lizardofoz.drgflares.util.FlareColor;
import me.lizardofoz.drgflares.util.ServerSyncMode;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class DRGFlareRegistry
{
    public static ServerSyncMode serverSyncMode = ServerSyncMode.UNDEFINED;

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DRGFlares.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(DRGFlares.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DRGFlares.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, DRGFlares.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, DRGFlares.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DRGFlares.MOD_ID);

    public static final ResourceLocation FLARE_THROW_ID = rl("flare_throw");
    public static final ResourceLocation FLARE_BOUNCE_ID = rl("flare_bounce");
    public static final ResourceLocation FLARE_BOUNCE_FAR_ID = rl("flare_bounce_far");

    public static final Supplier<SoundEvent> FLARE_THROW = SOUND_EVENTS.register("flare_throw", () -> SoundEvent.createVariableRangeEvent(FLARE_THROW_ID));
    public static final Supplier<SoundEvent> FLARE_BOUNCE = SOUND_EVENTS.register("flare_bounce", () -> SoundEvent.createVariableRangeEvent(FLARE_BOUNCE_ID));
    public static final Supplier<SoundEvent> FLARE_BOUNCE_FAR = SOUND_EVENTS.register("flare_bounce_far", () -> SoundEvent.createFixedRangeEvent(FLARE_BOUNCE_FAR_ID, 64.0f));

    public static final Supplier<Block> LIGHT_SOURCE_BLOCK = BLOCKS.register("flare_light_block", () -> new FlareLightBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .replaceable()
                    .noCollission()
                    .sound(SoundType.WOOD)
                    .strength(3600000.8F)
                    .noLootTable()
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(FlareLightBlock.LIGHT_LEVEL))));

    public static final Supplier<BlockEntityType<FlareLightBlockEntity>> LIGHT_SOURCE_BLOCK_ENTITY = BLOCK_ENTITIES.register("flare_light_block_entity", () ->
            BlockEntityType.Builder.of(FlareLightBlockEntity::new, LIGHT_SOURCE_BLOCK.get()).build(null));

    public static final Supplier<EntityType<FlareEntity>> FLARE_ENTITY = ENTITY_TYPES.register("drg_flare", () ->
            EntityType.Builder.<FlareEntity>of(FlareEntity::make, MobCategory.MISC)
                    .sized(0.4f, 0.2f)
                    .clientTrackingRange(64)
                    .canSpawnFarFromPlayer()
                    .updateInterval(1)
                    .build("drg_flares:drg_flare"));

    public static final Map<FlareColor, Supplier<Item>> FLARE_ITEMS = registerFlareItems();

    public static final Supplier<CreativeModeTab> CREATIVE_TAB = CREATIVE_TABS.register("drg_flares", () -> CreativeModeTab.builder()
            .title(Component.translatable("drg_flares.creative_group"))
            .icon(() -> new ItemStack(FLARE_ITEMS.get(FlareColor.MAGENTA).get()))
            .displayItems((params, output) -> {
                for (FlareColor color : FlareColor.colors)
                    output.accept(FLARE_ITEMS.get(color).get());
            })
            .build());

    private static Map<FlareColor, Supplier<Item>> registerFlareItems()
    {
        Map<FlareColor, Supplier<Item>> map = new EnumMap<>(FlareColor.class);
        for (FlareColor color : FlareColor.values())
            map.put(color, ITEMS.register("drg_flare_" + color.name, () -> new FlareItem(new Item.Properties())));
        return map;
    }

    private DRGFlareRegistry()
    {
    }

    public static void register(IEventBus modBus)
    {
        ITEMS.register(modBus);
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ENTITY_TYPES.register(modBus);
        SOUND_EVENTS.register(modBus);
        CREATIVE_TABS.register(modBus);
    }

    public static EntityType<FlareEntity> getFlareEntityType()
    {
        return FLARE_ENTITY.get();
    }

    public static Map<FlareColor, Item> getFlareItemTypes()
    {
        Map<FlareColor, Item> map = new EnumMap<>(FlareColor.class);
        for (Map.Entry<FlareColor, Supplier<Item>> entry : FLARE_ITEMS.entrySet())
            map.put(entry.getKey(), entry.getValue().get());
        return map;
    }

    public static Item getFlareItem(FlareColor color)
    {
        return FLARE_ITEMS.get(color).get();
    }

    public static Block getLightSourceBlockType()
    {
        return LIGHT_SOURCE_BLOCK.get();
    }

    public static BlockEntityType<FlareLightBlockEntity> getLightSourceBlockEntityType()
    {
        return LIGHT_SOURCE_BLOCK_ENTITY.get();
    }

    public static boolean isClothConfigLoaded()
    {
        return ModList.get().isLoaded("cloth_config");
    }

    public static boolean isInventorioLoaded()
    {
        //Inventorio does not have a 1.21.1 build; integration has been removed.
        return false;
    }

    public static void broadcastSettingsChange()
    {
        try
        {
            for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
                PacketStuff.sendSettingsSync(player);
        }
        catch (Throwable ignored)
        {
        }
    }

    private static ResourceLocation rl(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(DRGFlares.MOD_ID, path);
    }
}

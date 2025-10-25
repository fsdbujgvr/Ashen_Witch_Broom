package com.magicbroom.examplemod.core;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.magicbroom.examplemod.item.MajoHatItem;
import com.magicbroom.examplemod.item.MajoClothItem;
import com.magicbroom.examplemod.item.MajoBroomItem;
import com.magicbroom.examplemod.entity.MajoBroomEntity;
import com.magicbroom.examplemod.command.BroomCommand;
import com.magicbroom.examplemod.core.Config;
import com.magicbroom.examplemod.chunk.ChunkLoadingManager;
import com.magicbroom.examplemod.chunk.BroomChunkTicketManager;
import com.magicbroom.examplemod.util.LoggerWrapper;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AshenWitchBroom.MODID)
public class AshenWitchBroom {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "ashenwitchbroom";
    // 原始Logger实例
    public static final Logger LOGGER = LogUtils.getLogger();
    // 包装后的Logger实例，用于控制日志输出
    public static final LoggerWrapper WRAPPED_LOGGER = new LoggerWrapper(LOGGER);
    // Create a Deferred Register to hold Blocks which will all be registered under the "ashenwitchbroom" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "ashenwitchbroom" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "ashenwitchbroom" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    // Create a Deferred Register to hold ArmorMaterials which will all be registered under the "ashenwitchbroom" namespace
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, MODID);
    // Create a Deferred Register to hold EntityTypes which will all be registered under the "ashenwitchbroom" namespace
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);



    // Creates the majo hat armor material
    public static final Holder<ArmorMaterial> MAJO_HAT_MATERIAL = ARMOR_MATERIALS.register("majo_hat", () -> new ArmorMaterial(
            // Defense values for each armor piece (boots, leggings, chestplate, helmet)
            java.util.Map.of(
                    ArmorItem.Type.HELMET, 2,
                    ArmorItem.Type.CHESTPLATE, 0,
                    ArmorItem.Type.LEGGINGS, 0,
                    ArmorItem.Type.BOOTS, 0
            ),
            // Enchantability
            15,
            // Equip sound
            SoundEvents.ARMOR_EQUIP_LEATHER,
            // Repair ingredient
            () -> Ingredient.of(ItemTags.WOOL),
            // Texture layers
            java.util.List.of(
                    new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MODID, "majo_hat"), "", true),
                    new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MODID, "majo_hat"), "_overlay", false)
            ),
            // Toughness
            0.0f,
            // Knockback resistance
            0.0f
    ));

    // Creates the majo hat item
    public static final DeferredItem<MajoHatItem> MAJO_HAT = ITEMS.register("majo_hat", 
            () -> new MajoHatItem(MAJO_HAT_MATERIAL, ArmorItem.Type.HELMET, 
                new Item.Properties().stacksTo(1)
                    .component(DataComponents.DYED_COLOR, new DyedItemColor(0xdda3c7, false))));

    // Creates the majo cloth armor material
    public static final Holder<ArmorMaterial> MAJO_CLOTH_MATERIAL = ARMOR_MATERIALS.register("majo_cloth", () -> new ArmorMaterial(
            // Defense values for each armor piece (boots, leggings, chestplate, helmet)
            java.util.Map.of(
                    ArmorItem.Type.HELMET, 0,
                    ArmorItem.Type.CHESTPLATE, 6,
                    ArmorItem.Type.LEGGINGS, 0,
                    ArmorItem.Type.BOOTS, 0
            ),
            // Enchantability
            15,
            // Equip sound
            SoundEvents.ARMOR_EQUIP_LEATHER,
            // Repair ingredient
            () -> Ingredient.of(ItemTags.WOOL),
            // Texture layers (single layer, no dyeing)
            java.util.List.of(
                    new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MODID, "majo_cloth"), "", false)
            ),
            // Toughness
            0.0f,
            // Knockback resistance
            0.0f
    ));

    // Creates the majo cloth item
    public static final DeferredItem<MajoClothItem> MAJO_CLOTH = ITEMS.register("majo_cloth", 
            () -> new MajoClothItem(MAJO_CLOTH_MATERIAL, ArmorItem.Type.CHESTPLATE, 
                new Item.Properties().stacksTo(1)));

    // Creates the majo broom item
    public static final DeferredItem<MajoBroomItem> MAJO_BROOM_ITEM = ITEMS.register("majo_broom", 
            () -> new MajoBroomItem(new Item.Properties().stacksTo(1)));

    // Creates the majo broom entity type
    public static final DeferredHolder<EntityType<?>, EntityType<MajoBroomEntity>> MAJO_BROOM_ENTITY = ENTITY_TYPES.register("majo_broom", 
            () -> EntityType.Builder.<MajoBroomEntity>of(MajoBroomEntity::new, MobCategory.MISC)
                .sized(1.0F, 0.5F)
                .clientTrackingRange(192)
                .updateInterval(1)
                .build("majo_broom"));

    // Creates a creative tab with the id "ashenwitchbroom:example_tab" for the mod items, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ashenwitchbroom")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> MAJO_BROOM_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(MAJO_HAT.get()); // Add the majo hat to the tab
                output.accept(MAJO_CLOTH.get()); // Add the majo cloth to the tab
                output.accept(MAJO_BROOM_ITEM.get()); // Add the majo broom to the tab
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public AshenWitchBroom(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so armor materials get registered
        ARMOR_MATERIALS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so entity types get registered
        ENTITY_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (AshenWitchBroom) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.debug("通用设置初始化完成");
    }

    // Add items to creative tabs
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Currently no items to add to vanilla creative tabs
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.debug("服务器启动中");
        
        // Initialize chunk persistence system
        BroomChunkTicketManager.getInstance().setServer(event.getServer());
        LOGGER.debug("区块持久化系统已初始化");
        
        // Register broom commands
        BroomCommand.register(event.getServer().getCommands().getDispatcher());
        LOGGER.debug("扫帚指令已注册");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        // Save chunk data and shutdown gracefully
        LOGGER.debug("服务器停止中，保存区块数据");
        ChunkLoadingManager.getInstance().clearAllLoadedChunks();
        // BroomChunkTicketManager 会在 shutdown() 中自动保存数据，不再清空
        BroomChunkTicketManager.getInstance().shutdown();
        LOGGER.debug("区块数据已保存，管理器已关闭");
    }
}

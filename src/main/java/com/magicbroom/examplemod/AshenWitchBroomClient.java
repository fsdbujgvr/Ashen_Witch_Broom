package com.magicbroom.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = AshenWitchBroom.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = AshenWitchBroom.MODID, value = Dist.CLIENT)
public class AshenWitchBroomClient {
    public AshenWitchBroomClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        
        // 初始化客户端事件处理器
        init();
    }
    
    public static void init() {
        // 注册客户端事件处理器
        NeoForge.EVENT_BUS.register(ClientHandler.class);
        // KeybindingRegistry 使用 @EventBusSubscriber 注解自动注册到 mod bus
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        AshenWitchBroom.LOGGER.info("HELLO FROM CLIENT SETUP");
        AshenWitchBroom.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
    
    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MajoHatModel.LAYER_LOCATION, MajoHatModel::createBodyLayer);
        event.registerLayerDefinition(MajoClothModel.LAYER_LOCATION, MajoClothModel::createBodyLayer);
        event.registerLayerDefinition(MajoBroomModel.LAYER_LOCATION, MajoBroomModel::createBodyLayer);
    }
    
    @SubscribeEvent
    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AshenWitchBroom.MAJO_BROOM_ENTITY.get(), MajoBroomEntityRenderer::new);
    }
    
    // 按键注册已移至 KeybindingRegistry.java 中处理，避免重复注册
    // @SubscribeEvent
    // public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
    //     KeyBoardInput.registerKeyMappings(event);
    // }
    
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // 为 MajoHatItem 注册颜色处理器，使物品在物品栏中显示染色效果
        event.register((stack, tintIndex) -> {
            // tintIndex 0 对应 layer0（可染色基础层），tintIndex 1 对应 layer1（覆盖层不染色）
            return tintIndex == 0 ? ((MajoHatItem) stack.getItem()).getColor(stack) : -1;
        }, AshenWitchBroom.MAJO_HAT.get());
    }
    
    // 注意：玩家输入处理已移至ClientHandler.java，避免重复处理
    
    /**
     * 发送召唤扫帚的网络包到服务端
     */
    public static void sendSummonBroomPacket(double x, double y, double z) {
        PacketDistributor.sendToServer(new SummonBroomPack(x, y, z));
    }
    
    /**
     * 发送骑乘控制的网络包到服务端
     */
    public static void sendRidePacket(int entityId, boolean ride) {
        PacketDistributor.sendToServer(new RidePack(entityId, ride));
    }

}

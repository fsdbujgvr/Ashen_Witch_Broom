package com.magicbroom.examplemod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * 按键绑定注册器
 * 负责注册所有自定义按键映射
 */
@EventBusSubscriber(modid = AshenWitchBroom.MODID, value = Dist.CLIENT)
public class KeybindingRegistry {
    
    /**
     * 注册按键映射事件
     */
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        // 注册所有按键映射
        for (var keyMapping : KeyBoardInput.getKeyMappings()) {
            event.register(keyMapping);
        }
        
        AshenWitchBroom.LOGGER.info("Registered {} key mappings for Ashen Witch Broom", KeyBoardInput.getKeyMappings().length);
    }
}
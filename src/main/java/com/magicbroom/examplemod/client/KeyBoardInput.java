package com.magicbroom.examplemod.client;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import com.magicbroom.examplemod.core.AshenWitchBroom;

/**
 * 键盘输入处理类
 * 处理扫帚飞行控制的按键映射和输入事件
 */
@EventBusSubscriber(modid = AshenWitchBroom.MODID, value = Dist.CLIENT)
public class KeyBoardInput {
    
    // 按键映射定义
    public static final KeyMapping UP_KEY = new KeyMapping("key.up",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SPACE,  // 空格键上升
            "key.category.majobroom");

    public static final KeyMapping DOWN_KEY = new KeyMapping("key.down",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,  // 左Alt键下降
            "key.category.majobroom");

    public static final KeyMapping SUMMON_KEY = new KeyMapping("key.summon",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,  // R键召唤/收回扫帚
            "key.category.majobroom");

    public static final KeyMapping SPEED_BOOST_KEY = new KeyMapping("key.speed_boost",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,  // 左Ctrl键加速
            "key.category.majobroom");
    
    // 按键状态管理
    public static boolean up = false;
    public static boolean down = false;
    public static boolean speedBoost = false;
    
    /**
     * 注册按键映射
     */
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
          event.register(UP_KEY);
          event.register(DOWN_KEY);
          event.register(SUMMON_KEY);
          event.register(SPEED_BOOST_KEY);
      }
    
    /**
     * 处理键盘输入事件
     */
    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.Key event) {
        // 处理上升键
        if (UP_KEY.getKey().getValue() == event.getKey()) {
            if (event.getAction() == GLFW.GLFW_PRESS){
                up = true;
            }else if (event.getAction() == GLFW.GLFW_RELEASE){
                up = false;
            }
        }
        
        // 处理下降键
        if (DOWN_KEY.getKey().getValue() == event.getKey()) {
            if (event.getAction() == GLFW.GLFW_PRESS){
                down = true;
            }else if (event.getAction() == GLFW.GLFW_RELEASE){
                down = false;
            }
        }
        
        // 处理召唤键
        if (SUMMON_KEY.getKey().getValue() == event.getKey()) {
            if (event.getAction() == GLFW.GLFW_PRESS){
                // 发送召唤扫帚网络包
                var minecraft = net.minecraft.client.Minecraft.getInstance();
                if (minecraft.player != null) {
                    var pos = minecraft.player.position();
                    AshenWitchBroomClient.sendSummonBroomPacket(pos.x, pos.y, pos.z);
                }
            }
        }
        
        // 处理加速键
        if (SPEED_BOOST_KEY.getKey().getValue() == event.getKey()) {
            if (event.getAction() == GLFW.GLFW_PRESS){
                speedBoost = true;
            }else if (event.getAction() == GLFW.GLFW_RELEASE){
                speedBoost = false;
            }
        }
    }
    
    /**
     * 获取所有按键映射数组，用于注册
     */
    public static KeyMapping[] getKeyMappings() {
        return new KeyMapping[]{
            UP_KEY,
            DOWN_KEY,
            SUMMON_KEY,
            SPEED_BOOST_KEY
        };
    }
}
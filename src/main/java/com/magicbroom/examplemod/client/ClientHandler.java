package com.magicbroom.examplemod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;

import com.magicbroom.examplemod.core.AshenWitchBroom;
import com.magicbroom.examplemod.core.Config;
import com.magicbroom.examplemod.entity.MajoBroomEntity;

/**
 * 客户端事件处理器
 * 处理玩家tick事件和扫帚控制逻辑
 */
@EventBusSubscriber(modid = AshenWitchBroom.MODID, value = Dist.CLIENT)
public class ClientHandler {
    
    /**
     * 处理玩家tick事件
     * 在客户端每tick检查玩家是否在骑乘扫帚，并更新输入状态
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Minecraft mc = Minecraft.getInstance();
        
        // 确保是客户端玩家
        if (player != mc.player || mc.level == null) {
            return;
        }
        
        // 检查玩家是否在骑乘扫帚
        if (player.getVehicle() instanceof MajoBroomEntity broomEntity) {
            // 获取WASD输入状态
            float forward = 0.0F;
            float strafe = 0.0F;
            boolean jumping = false;
            boolean shiftKeyDown = false;
            boolean sprinting = false;
            boolean ctrlPressed = false;
            
            // W键前进
            if (mc.options.keyUp.isDown()) {
                forward += 1.0F;
            }
            
            // S键后退
            if (mc.options.keyDown.isDown()) {
                forward -= 1.0F;
            }
            
            // A键左转
            if (mc.options.keyLeft.isDown()) {
                strafe += 1.0F;
            }
            
            // D键右转
            if (mc.options.keyRight.isDown()) {
                strafe -= 1.0F;
            }
            
            // 空格键上升
            if (KeyBoardInput.up || mc.options.keyJump.isDown()) {
                jumping = true;
            }
            
            // Ctrl键下降
            if (KeyBoardInput.down) {
                shiftKeyDown = true;
            }
            
            // 使用自定义加速键进行速度加成切换
            if (KeyBoardInput.speedBoost) {
                ctrlPressed = true;
            }
            
            // 处理按键冲突 - 如果下降键和加速键都是左Ctrl
            if (KeyBoardInput.DOWN_KEY.getKey().getValue() == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL && 
                KeyBoardInput.SPEED_BOOST_KEY.getKey().getValue() == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL) {
                if (KeyBoardInput.speedBoost) {
                    shiftKeyDown = true;
                    sprinting = true;
                }
            } else {
                // 加速键处理
                if (KeyBoardInput.speedBoost) {
                    sprinting = true;
                }
            }
            
            // 更新扫帚实体的输入数据
            broomEntity.updatePlayerInput(forward, strafe, jumping, shiftKeyDown, sprinting, KeyBoardInput.up, KeyBoardInput.down, ctrlPressed);
        }
    }
    
    /**
     * 处理FOV调整事件
     * 当玩家骑乘扫帚并按下加速键时，提高视野范围
     */
    @SubscribeEvent
    public static void onComputeFovModifier(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        
        // 检查玩家是否在骑乘扫帚
        if (player.getVehicle() instanceof MajoBroomEntity broomEntity) {
            // 检查扫帚是否处于加速状态
            if (broomEntity.isSpeedBoostActive()) {
                // 提高视野范围15%
                // FOV修饰符是乘法的，所以我们直接乘以1.15来增加15%
                event.setNewFovModifier(event.getFovModifier() * 1.15f);
            }
        }
    }
    
    // 存储玩家骑乘前的视角状态
    private static CameraType previousCameraType = null;
    
    /**
     * 处理实体骑乘事件
     * 当玩家骑乘扫帚时，根据advancedMode参数自动切换到第三人称视角
     */
    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        // 确保是客户端且是扫帚实体
        if (!event.getLevel().isClientSide || !(event.getEntityBeingMounted() instanceof MajoBroomEntity broomEntity)) {
            return;
        }
        
        // 确保是本地玩家
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !event.getEntityMounting().equals(mc.player)) {
            return;
        }
        
        if (event.isMounting()) {
            // 骑乘时：检查advancedMode参数
            if (Config.ADVANCED_MODE.get()) {
                // 保存当前视角
                previousCameraType = mc.options.getCameraType();
                // 切换到第三人称视角
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
        } else {
            // 下马时：恢复之前的视角
            if (Config.ADVANCED_MODE.get() && previousCameraType != null) {
                mc.options.setCameraType(previousCameraType);
                previousCameraType = null;
            }
        }
    }
}
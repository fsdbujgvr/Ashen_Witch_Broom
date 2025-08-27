package com.magicbroom.examplemod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.magicbroom.examplemod.core.AshenWitchBroom;
import com.magicbroom.examplemod.data.BroomDataManager;
import com.magicbroom.examplemod.entity.MajoBroomEntity;
import com.magicbroom.examplemod.service.BroomSummonService;

/**
 * 魔女扫帚物品类
 * 右键方块召唤扫帚实体，潜行左键收回扫帚
 */
public class MajoBroomItem extends Item {
    
    public MajoBroomItem(Properties properties) {
        super(properties);
    }
    
    /**
     * 右键方块时放置扫帚实体并存储数据
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        
        if (player != null && !level.isClientSide) {
            // 在方块上方1格高度放出扫帚
            BlockPos broomPos = pos.above();
            Vec3 spawnPos = new Vec3(broomPos.getX() + 0.5, broomPos.getY(), broomPos.getZ() + 0.5);
            
            // 生成唯一的扫帚名称
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                String broomName = BroomDataManager.generateUniqueBroomName(serverLevel, player.getUUID());
                
                // 创建扫帚实体
                MajoBroomEntity broomEntity = new MajoBroomEntity(AshenWitchBroom.MAJO_BROOM_ENTITY.get(), level);
                broomEntity.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                broomEntity.setYRot(player.getYRot());
                
                // 设置扫帚信息
                broomEntity.setBroomInfo(broomName, player.getUUID());
                
                level.addFreshEntity(broomEntity);
                
                // 存储扫帚数据，包含实体UUID
                BroomDataManager.addBroom(serverLevel, player.getUUID(), broomName, level.dimension(), broomPos, broomEntity.getUUID());
                
                // 向玩家发送消息
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "message.ashenwitchbroom.broom_placed", broomName));
                
                AshenWitchBroom.LOGGER.info("Player {} placed broom '{}' at {} in {}", 
                    player.getName().getString(), broomName, broomPos, level.dimension().location());
            }
            
            // 消耗物品
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }
    
    /**
     * 右键空气时的处理（暂时不实现特殊功能）
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}
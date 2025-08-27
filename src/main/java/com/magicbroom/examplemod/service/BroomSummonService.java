package com.magicbroom.examplemod.service;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.magicbroom.examplemod.core.AshenWitchBroom;
import com.magicbroom.examplemod.core.Config;
import com.magicbroom.examplemod.entity.MajoBroomEntity;
import com.magicbroom.examplemod.data.BroomDataManager;
import com.magicbroom.examplemod.data.BroomData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 扫帚召唤服务类
 * 负责处理扫帚召唤的核心逻辑
 */
public class BroomSummonService {
    
    // 为每个玩家维护一个互斥锁，防止疯狂按R键导致的并发问题
    private static final ConcurrentHashMap<UUID, ReentrantLock> playerLocks = new ConcurrentHashMap<>();
    
    /**
     * 执行扫帚召唤
     */
    public static void summonBroom(ServerPlayer player, double x, double y, double z) {
        UUID playerUUID = player.getUUID();
        ReentrantLock playerLock = playerLocks.computeIfAbsent(playerUUID, k -> new ReentrantLock());
        
        // 尝试获取锁，如果无法立即获取则直接返回（防止疯狂按键）
        if (!playerLock.tryLock()) {
            AshenWitchBroom.LOGGER.debug("Player {} summon request ignored - previous operation still in progress", playerUUID);
            return;
        }
        
        try {
             // 如果玩家已经在骑乘扫帚，则不处理
             if (player.isPassenger() && player.getVehicle() instanceof MajoBroomEntity) {
                 player.sendSystemMessage(Component.translatable("message.ashenwitchbroom.already_riding"));
                 return;
             }
         
             ServerLevel level = player.serverLevel();
             BlockPos playerPos = player.blockPosition();
             ResourceKey<Level> playerDimension = level.dimension();
        
        // 获取玩家的扫帚数据
        List<BroomData> playerBrooms = BroomDataManager.loadPlayerBrooms(level, playerUUID);
        
        // 按照配置的召唤顺序查找扫帚
        CompletableFuture<String> summonFuture = findBestBroomToSummon(level, player, playerBrooms, playerPos, playerDimension);
        
            summonFuture.thenAccept(broomName -> {
                level.getServer().execute(() -> {
                    try {
                        if (broomName != null) {
                            // 找到了扫帚，执行召唤
                            executeBroomSummon(player, broomName, playerPos, playerDimension);
                        } else {
                            // 没有找到扫帚
                            player.sendSystemMessage(Component.translatable("message.ashenwitchbroom.no_broom_available"));
                            
                            // 显示详细信息
                            if (Config.ENABLE_DETAILED_INFO.get()) {
                                showBroomSummary(player, playerBrooms);
                            }
                        }
                    } finally {
                        // 确保在所有操作完成后释放锁
                        playerLock.unlock();
                    }
                });
            }).exceptionally(throwable -> {
                // 异常情况下也要释放锁
                level.getServer().execute(() -> playerLock.unlock());
                AshenWitchBroom.LOGGER.error("Error during broom summon for player {}: {}", playerUUID, throwable.getMessage());
                return null;
            });
        } catch (Exception e) {
            // 如果在try块中发生异常，也要释放锁
            playerLock.unlock();
            AshenWitchBroom.LOGGER.error("Error in summonBroom for player {}: {}", playerUUID, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 按照召唤顺序查找最佳扫帚
     */
    private static CompletableFuture<String> findBestBroomToSummon(ServerLevel level, ServerPlayer player, 
            List<BroomData> playerBrooms, BlockPos playerPos, ResourceKey<Level> playerDimension) {
        
        List<BroomData> candidateBrooms = new ArrayList<>();
        
        // 1. 当前范围内的扫帚
        if (Config.ENABLE_NEARBY_SUMMON.get()) {
            int searchRange = Config.NEARBY_SEARCH_RANGE.get();
            List<BroomData> nearbyBrooms = filterBroomsByDistance(playerBrooms, playerPos, playerDimension, searchRange);
            candidateBrooms.addAll(nearbyBrooms);
        }
        
        // 2. 本世界的扫帚
        if (Config.ENABLE_WORLD_SUMMON.get() && candidateBrooms.isEmpty()) {
            List<BroomData> worldBrooms = filterBroomsByDimension(playerBrooms, playerDimension);
            candidateBrooms.addAll(worldBrooms);
        }
        
        // 3. 全维度的扫帚
        if (Config.ENABLE_CROSS_DIMENSION_SUMMON.get() && candidateBrooms.isEmpty()) {
            candidateBrooms.addAll(playerBrooms);
        }
        
        // 4. 如果没有存储的扫帚且启用背包召唤，从背包召唤
        if (candidateBrooms.isEmpty() && Config.ENABLE_INVENTORY_SUMMON.get()) {
            ItemStack broomItem = findBroomInInventory(player);
            if (broomItem != null) {
                return CompletableFuture.completedFuture("FROM_INVENTORY");
            }
        }
        
        if (candidateBrooms.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        
        // 排序：按距离从近到远
        candidateBrooms.sort(Comparator.comparingDouble(broom -> broom.getDistanceSquared(playerPos, playerDimension)));
        
        // 依次验证扫帚是否存在
        return verifyBroomsSequentially(player, candidateBrooms, 0);
    }
    
    /**
     * 按距离过滤扫帚
     */
    private static List<BroomData> filterBroomsByDistance(List<BroomData> brooms, BlockPos playerPos, 
            ResourceKey<Level> playerDimension, int maxDistance) {
        List<BroomData> filtered = new ArrayList<>();
        double maxDistanceSquared = maxDistance * maxDistance;
        
        for (BroomData broom : brooms) {
            if (broom.getDimension().equals(playerDimension)) {
                double distanceSquared = broom.getDistanceSquared(playerPos, playerDimension);
                if (distanceSquared <= maxDistanceSquared) {
                    filtered.add(broom);
                }
            }
        }
        
        return filtered;
    }
    
    /**
     * 按维度过滤扫帚
     */
    private static List<BroomData> filterBroomsByDimension(List<BroomData> brooms, ResourceKey<Level> dimension) {
        List<BroomData> filtered = new ArrayList<>();
        
        for (BroomData broom : brooms) {
            if (broom.getDimension().equals(dimension)) {
                filtered.add(broom);
            }
        }
        
        return filtered;
    }
    
    /**
     * 依次验证扫帚是否存在
     */
    private static CompletableFuture<String> verifyBroomsSequentially(ServerPlayer player, List<BroomData> brooms, int index) {
        if (index >= brooms.size()) {
            return CompletableFuture.completedFuture(null);
        }
        
        BroomData currentBroom = brooms.get(index);
        return BroomDataManager.verifyBroomExists(player, currentBroom.getBroomName())
            .thenCompose(exists -> {
                if (exists) {
                    return CompletableFuture.completedFuture(currentBroom.getBroomName());
                } else {
                    // 扫帚不存在，从记录中删除
                    BroomDataManager.removeBroom(player.serverLevel(), player.getUUID(), currentBroom.getBroomName());
                    // 继续验证下一个
                    return verifyBroomsSequentially(player, brooms, index + 1);
                }
            });
    }
    
    /**
     * 执行扫帚召唤
     */
    private static void executeBroomSummon(ServerPlayer player, String broomName, BlockPos playerPos, ResourceKey<Level> playerDimension) {
        ServerLevel level = player.serverLevel();
        
        if ("FROM_INVENTORY".equals(broomName)) {
            // 从背包召唤
            summonFromInventory(player, playerPos);
            return;
        }
        
        // 从存储的扫帚召唤
        BroomData broomData = BroomDataManager.findBroom(level, player.getUUID(), broomName);
        if (broomData != null) {
            // 删除原位置的扫帚实体
            removeOldBroomEntity(player, broomData);
            
            // 检查是否为跨维度召唤
            boolean isCrossDimension = !broomData.getDimension().equals(playerDimension);
            
            if (isCrossDimension) {
                // 跨维度召唤：删除原数据记录，创建新记录
                BroomDataManager.removeBroom(level, player.getUUID(), broomName);
                
                // 在玩家附近创建新扫帚
                MajoBroomEntity newBroom = new MajoBroomEntity(AshenWitchBroom.MAJO_BROOM_ENTITY.get(), level);
                newBroom.setPos(playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5);
                newBroom.setYRot(player.getYRot());
                
                // 设置扫帚信息
                newBroom.setBroomInfo(broomName, player.getUUID());
                
                level.addFreshEntity(newBroom);
                
                // 添加新的数据记录，包含实体UUID
                BroomDataManager.addBroom(level, player.getUUID(), broomName, playerDimension, playerPos, newBroom.getUUID());
                
                AshenWitchBroom.LOGGER.info("Cross-dimension summon: broom '{}' moved from {} to {} for player {}", 
                    broomName, broomData.getDimension().location(), playerDimension.location(), player.getUUID());
            } else {
                // 同维度召唤：直接更新位置
                MajoBroomEntity newBroom = new MajoBroomEntity(AshenWitchBroom.MAJO_BROOM_ENTITY.get(), level);
                newBroom.setPos(playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5);
                newBroom.setYRot(player.getYRot());
                
                // 设置扫帚信息
                newBroom.setBroomInfo(broomName, player.getUUID());
                
                level.addFreshEntity(newBroom);
                
                // 更新扫帚数据位置，包含实体UUID
                BroomDataManager.updateBroom(level, player.getUUID(), broomName, playerDimension, playerPos, newBroom.getUUID());
            }
            
            player.sendSystemMessage(Component.translatable("message.ashenwitchbroom.broom_summoned", broomName));
            
            // 显示详细信息
            if (Config.ENABLE_DETAILED_INFO.get()) {
                List<BroomData> allBrooms = BroomDataManager.loadPlayerBrooms(level, player.getUUID());
                showBroomSummary(player, allBrooms);
            }
        }
    }
    
    /**
     * 删除原位置的扫帚实体（仅使用UUID精确匹配）
     * 坐标仅用于加载区块，删除完全基于UUID
     */
    private static void removeOldBroomEntity(ServerPlayer player, BroomData broomData) {
        ServerLevel targetLevel = player.getServer().getLevel(broomData.getDimension());
        if (targetLevel == null) return;
        
        UUID targetEntityUUID = broomData.getEntityUUID();
        if (targetEntityUUID == null) {
            AshenWitchBroom.LOGGER.warn("Cannot remove broom '{}' - no entityUUID available", broomData.getBroomName());
            return;
        }
        
        // 使用坐标加载相关区块（确保实体已加载）
        BlockPos broomPos = broomData.getPosition();
        ChunkPos centerChunk = new ChunkPos(broomPos);
        
        // 强制加载周围9个区块 (3x3区域)
        List<ChunkPos> chunksToLoad = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                chunksToLoad.add(new ChunkPos(centerChunk.x + x, centerChunk.z + z));
            }
        }
        
        // 同步加载区块
        for (ChunkPos chunkPos : chunksToLoad) {
            targetLevel.getChunk(chunkPos.x, chunkPos.z);
        }
        
        // 仅使用UUID精确匹配
        Entity targetEntity = targetLevel.getEntity(targetEntityUUID);
        if (targetEntity instanceof MajoBroomEntity broomEntity) {
            // 验证扫帚名称和拥有者匹配
            if (broomData.getBroomName().equals(broomEntity.getBroomName()) && 
                player.getUUID().equals(broomEntity.getOwnerUUID())) {
                targetEntity.discard();
                AshenWitchBroom.LOGGER.info("Removed broom entity '{}' with UUID {} for player {}", 
                    broomData.getBroomName(), targetEntityUUID, player.getUUID());
            } else {
                AshenWitchBroom.LOGGER.warn("Broom entity UUID {} found but name/owner mismatch. Expected: {}/{}, Found: {}/{}", 
                    targetEntityUUID, broomData.getBroomName(), player.getUUID(), 
                    broomEntity.getBroomName(), broomEntity.getOwnerUUID());
            }
        } else {
            AshenWitchBroom.LOGGER.debug("Broom entity with UUID {} not found after chunk loading", targetEntityUUID);
        }
    }
    
    /**
     * 从背包召唤扫帚
     */
    private static void summonFromInventory(ServerPlayer player, BlockPos playerPos) {
        ItemStack broomItem = findBroomInInventory(player);
        if (broomItem != null) {
            ServerLevel level = player.serverLevel();
            
            // 生成唯一名称
            String broomName = BroomDataManager.generateUniqueBroomName(level, player.getUUID());
            
            // 创建扫帚实体
            MajoBroomEntity broomEntity = new MajoBroomEntity(AshenWitchBroom.MAJO_BROOM_ENTITY.get(), level);
            broomEntity.setPos(playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5);
            broomEntity.setYRot(player.getYRot());
            
            // 设置扫帚信息
            broomEntity.setBroomInfo(broomName, player.getUUID());
            
            level.addFreshEntity(broomEntity);
            
            // 消耗物品（创造模式除外）
            if (!player.getAbilities().instabuild) {
                broomItem.shrink(1);
            }
            
            // 存储扫帚数据，包含实体UUID
            BroomDataManager.addBroom(level, player.getUUID(), broomName, level.dimension(), playerPos, broomEntity.getUUID());
            
            player.sendSystemMessage(Component.translatable("message.ashenwitchbroom.broom_summoned_from_inventory", broomName));
            
            // 显示详细信息
            if (Config.ENABLE_DETAILED_INFO.get()) {
                List<BroomData> allBrooms = BroomDataManager.loadPlayerBrooms(level, player.getUUID());
                showBroomSummary(player, allBrooms);
            }
        }
    }
    
    /**
     * 在玩家背包中查找扫帚物品
     */
    private static ItemStack findBroomInInventory(ServerPlayer player) {
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() == AshenWitchBroom.MAJO_BROOM_ITEM.get()) {
                return itemStack;
            }
        }
        return null;
    }
    
    /**
     * 显示扫帚统计信息
     */
    private static void showBroomSummary(ServerPlayer player, List<BroomData> brooms) {
        int storedBrooms = brooms.size(); // 存储在数据文件中的扫帚数量
        int inventoryBrooms = 0;
        
        // 统计背包中的扫帚
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() == AshenWitchBroom.MAJO_BROOM_ITEM.get()) {
                inventoryBrooms += itemStack.getCount();
            }
        }
        
        // 全部扫帚 = 存储的扫帚 + 背包中的扫帚
        int totalBrooms = storedBrooms + inventoryBrooms;
        
        ResourceKey<Level> currentDimension = player.level().dimension();
        long currentDimensionBrooms = brooms.stream()
            .filter(broom -> broom.getDimension().equals(currentDimension))
            .count();
        
        // 全维度扫帚数量（所有维度的扫帚总数）
        long allDimensionBrooms = brooms.size(); // 这就是全维度的扫帚数量
        
        int nearbyRange = Config.NEARBY_SEARCH_RANGE.get();
        BlockPos playerPos = player.blockPosition();
        long nearbyBrooms = brooms.stream()
            .filter(broom -> broom.getDimension().equals(currentDimension))
            .filter(broom -> broom.getDistanceSquared(playerPos, currentDimension) <= nearbyRange * nearbyRange)
            .count();
        
        // 显示统计信息
        player.sendSystemMessage(Component.translatable("message.ashenwitchbroom.broom_summary_total", totalBrooms));
        player.sendSystemMessage(Component.translatable("message.ashenwitchbroom.broom_summary_inventory", inventoryBrooms));
        player.sendSystemMessage(Component.translatable("message.ashenwitchbroom.broom_summary_nearby", nearbyRange, nearbyBrooms));
        player.sendSystemMessage(Component.translatable("message.ashenwitchbroom.broom_summary_current_world", currentDimensionBrooms));
        player.sendSystemMessage(Component.translatable("message.ashenwitchbroom.broom_summary_all_dimensions", allDimensionBrooms));
        
        // 显示配置状态
        String nearbyStatus = Config.ENABLE_NEARBY_SUMMON.get() ? "✔" : "×";
        String worldStatus = Config.ENABLE_WORLD_SUMMON.get() ? "✔" : "×";
        String crossDimensionStatus = Config.ENABLE_CROSS_DIMENSION_SUMMON.get() ? "✔" : "×";
        String inventoryStatus = Config.ENABLE_INVENTORY_SUMMON.get() ? "✔" : "×";
        
        player.sendSystemMessage(Component.translatable("message.ashenwitchbroom.summon_order_status", 
            nearbyStatus, worldStatus, crossDimensionStatus, inventoryStatus));
    }
}

package com.magicbroom.examplemod.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.io.File;
import java.io.IOException;

import com.magicbroom.examplemod.core.AshenWitchBroom;
import com.magicbroom.examplemod.entity.MajoBroomEntity;
import com.magicbroom.examplemod.chunk.ChunkLoadingManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 扫帚数据管理器
 * 负责扫帚数据的文件存储、读取和验证
 */
public class BroomDataManager {
    private static final String DATA_FOLDER = "ashenwitchbroom";
    private static final String FILE_EXTENSION = ".dat";
    
    // 为每个玩家维护一个读写锁，确保线程安全
    private static final ConcurrentHashMap<UUID, ReentrantReadWriteLock> playerLocks = new ConcurrentHashMap<>();
    
    /**
     * 获取玩家的读写锁
     */
    private static ReentrantReadWriteLock getPlayerLock(UUID playerUUID) {
        return playerLocks.computeIfAbsent(playerUUID, k -> new ReentrantReadWriteLock());
    }
    
    /**
     * 获取玩家的扫帚数据文件路径
     */
    private static File getPlayerBroomFile(ServerLevel level, UUID playerUUID) {
        File worldDir = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile();
        File dataDir = new File(worldDir, DATA_FOLDER);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        return new File(dataDir, playerUUID.toString() + FILE_EXTENSION);
    }
    
    /**
     * 读取玩家的扫帚数据列表
     */
    public static List<BroomData> loadPlayerBrooms(ServerLevel level, UUID playerUUID) {
        ReentrantReadWriteLock lock = getPlayerLock(playerUUID);
        lock.readLock().lock();
        try {
            List<BroomData> brooms = new ArrayList<>();
            File broomFile = getPlayerBroomFile(level, playerUUID);
            
            if (!broomFile.exists()) {
                return brooms;
            }
            
            try {
                CompoundTag rootTag = NbtIo.readCompressed(broomFile.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                ListTag broomsList = rootTag.getList("brooms", 10); // 10 = CompoundTag type
                
                for (int i = 0; i < broomsList.size(); i++) {
                    CompoundTag broomTag = broomsList.getCompound(i);
                    BroomData broomData = BroomData.fromNBT(broomTag);
                    brooms.add(broomData);
                }
            } catch (IOException e) {
                AshenWitchBroom.WRAPPED_LOGGER.error("加载玩家 {} 的扫帚数据失败：{}", playerUUID, e.getMessage());
            }
            
            return brooms;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 保存玩家的扫帚数据列表
     */
    public static void savePlayerBrooms(ServerLevel level, UUID playerUUID, List<BroomData> brooms) {
        ReentrantReadWriteLock lock = getPlayerLock(playerUUID);
        lock.writeLock().lock();
        try {
            File broomFile = getPlayerBroomFile(level, playerUUID);
            
            try {
                CompoundTag rootTag = new CompoundTag();
                ListTag broomsList = new ListTag();
                
                for (BroomData broomData : brooms) {
                    broomsList.add(broomData.toNBT());
                }
                
                rootTag.put("brooms", broomsList);
                NbtIo.writeCompressed(rootTag, broomFile.toPath());
            } catch (IOException e) {
                AshenWitchBroom.WRAPPED_LOGGER.error("保存玩家 {} 的扫帚数据失败：{}", playerUUID, e.getMessage());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 添加扫帚数据（必须包含entityUUID）
     */
    public static void addBroom(ServerLevel level, UUID playerUUID, String broomName, ResourceKey<Level> dimension, BlockPos position, UUID entityUUID) {
        if (entityUUID == null) {
            throw new IllegalArgumentException("entityUUID cannot be null when adding broom data");
        }
        List<BroomData> brooms = loadPlayerBrooms(level, playerUUID);
        brooms.add(new BroomData(broomName, dimension, position, entityUUID));
        savePlayerBrooms(level, playerUUID, brooms);
        
        AshenWitchBroom.WRAPPED_LOGGER.debug("为玩家 {} 添加扫帚 '{}' 在位置 {} 维度 {} (实体UUID: {})",
                playerUUID, broomName, position, dimension.location(), entityUUID);
    }
    
    /**
     * 删除扫帚数据
     */
    public static boolean removeBroom(ServerLevel level, UUID playerUUID, String broomName) {
        List<BroomData> brooms = loadPlayerBrooms(level, playerUUID);
        boolean removed = brooms.removeIf(broom -> broom.getBroomName().equals(broomName));
        
        if (removed) {
            savePlayerBrooms(level, playerUUID, brooms);
            AshenWitchBroom.WRAPPED_LOGGER.debug("为玩家 {} 移除扫帚 '{}'", playerUUID, broomName);
        }
        
        return removed;
    }
    
    /**
     * 更新扫帚数据（必须包含entityUUID）
     */
    public static boolean updateBroom(ServerLevel level, UUID playerUUID, String broomName, ResourceKey<Level> newDimension, BlockPos newPosition, UUID newEntityUUID) {
        if (newEntityUUID == null) {
            throw new IllegalArgumentException("newEntityUUID cannot be null when updating broom data");
        }
        List<BroomData> brooms = loadPlayerBrooms(level, playerUUID);
        
        for (BroomData broom : brooms) {
            if (broom.getBroomName().equals(broomName)) {
                broom.setDimension(newDimension);
                broom.setPosition(newPosition);
                if (newEntityUUID != null) {
                    broom.setEntityUUID(newEntityUUID);
                }
                savePlayerBrooms(level, playerUUID, brooms);
                
                // AshenWitchBroom.WRAPPED_LOGGER.debug("为玩家 {} 更新扫帚 '{}' 到位置 {} 维度 {} (实体UUID: {})", 
                //     playerUUID, broomName, newPosition, newDimension.location(), newEntityUUID);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 查找指定名称的扫帚数据
     */
    public static BroomData findBroom(ServerLevel level, UUID playerUUID, String broomName) {
        List<BroomData> brooms = loadPlayerBrooms(level, playerUUID);
        
        for (BroomData broom : brooms) {
            if (broom.getBroomName().equals(broomName)) {
                return broom;
            }
        }
        
        return null;
    }
    
    /**
     * 查找指定entityUUID的扫帚数据
     */
    public static BroomData findBroomByEntityUUID(ServerLevel level, UUID playerUUID, UUID entityUUID) {
        List<BroomData> brooms = loadPlayerBrooms(level, playerUUID);
        
        for (BroomData broom : brooms) {
            if (entityUUID.equals(broom.getEntityUUID())) {
                return broom;
            }
        }
        
        return null;
    }
    
    /**
     * 验证扫帚的entityUUID是否匹配（UUID不匹配时直接删除过期数据）
     * @param level 服务器世界
     * @param playerUUID 玩家UUID
     * @param broomName 扫帚名称
     * @param expectedEntityUUID 期望的实体UUID
     * @return 是否匹配
     */
    public static boolean validateBroomEntityUUID(ServerLevel level, UUID playerUUID, String broomName, UUID expectedEntityUUID) {
        BroomData broom = findBroom(level, playerUUID, broomName);
        if (broom == null) {
             AshenWitchBroom.WRAPPED_LOGGER.debug("玩家 {} 的扫帚 '{}' 未找到", playerUUID, broomName);
             return false;
         }
         
         UUID storedUUID = broom.getEntityUUID();
         if (storedUUID == null) {
             AshenWitchBroom.WRAPPED_LOGGER.warn("扫帚 '{}' 没有实体UUID，为玩家 {} 移除无效数据", broomName, playerUUID);
             removeBroom(level, playerUUID, broomName);
             return false;
         }
         
         if (!storedUUID.equals(expectedEntityUUID)) {
             AshenWitchBroom.WRAPPED_LOGGER.warn("玩家 {} 的扫帚 '{}' 实体UUID不匹配。期望：{}，存储：{}。移除过期数据。",
                 playerUUID, broomName, expectedEntityUUID, storedUUID);
             removeBroom(level, playerUUID, broomName);
             return false;
         }
        
        return true;
    }
    
    /**
     * 验证扫帚是否真实存在（仅使用UUID精确匹配）
     * 坐标仅用于加载区块，验证完全基于UUID
     * 在写入期间禁止调用此函数，确保数据一致性
     */
    public static CompletableFuture<Boolean> verifyBroomExists(ServerPlayer player, String broomName) {
        UUID playerUUID = player.getUUID();
        ReentrantReadWriteLock lock = getPlayerLock(playerUUID);
        
        // 检查是否有写操作正在进行，如果有则直接返回false，避免数据不一致
         if (lock.isWriteLocked()) {
             AshenWitchBroom.WRAPPED_LOGGER.debug("跳过玩家 {} 的扫帚验证（写入操作进行中）", playerUUID);
             return CompletableFuture.completedFuture(false);
         }
         
         BroomData broomData = findBroom((ServerLevel) player.level(), playerUUID, broomName);
         if (broomData == null) {
             AshenWitchBroom.WRAPPED_LOGGER.debug("玩家 {} 的扫帚 '{}' 在数据中未找到", playerUUID, broomName);
             return CompletableFuture.completedFuture(false);
         }
         
         // 必须有entityUUID才能验证
         UUID targetEntityUUID = broomData.getEntityUUID();
         if (targetEntityUUID == null) {
             AshenWitchBroom.WRAPPED_LOGGER.warn("扫帚 '{}' 没有实体UUID，为玩家 {} 移除无效数据", broomName, playerUUID);
             // 删除无效的旧数据
             removeBroom((ServerLevel) player.level(), playerUUID, broomName);
             return CompletableFuture.completedFuture(false);
         }
         
         // 获取目标维度的服务端世界
         ServerLevel targetLevel = player.getServer().getLevel(broomData.getDimension());
         if (targetLevel == null) {
             AshenWitchBroom.WRAPPED_LOGGER.warn("扫帚 '{}' 的目标维度 {} 未找到", broomName, broomData.getDimension().location());
             return CompletableFuture.completedFuture(false);
         }
        
        // 使用异步区块加载等待机制
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        
        // 获取扫帚位置和区块坐标
        BlockPos broomPos = broomData.getPosition();
        ChunkPos broomChunk = new ChunkPos(broomPos);
        
        // 记录坐标转换日志
                AshenWitchBroom.WRAPPED_LOGGER.debug("BroomDataManager - 扫帚检测坐标转换: 原始坐标 BlockPos({}, {}, {}) -> 区块坐标 ChunkPos({}, {})",
                    broomPos.getX(), broomPos.getY(), broomPos.getZ(), broomChunk.x, broomChunk.z);
        
        // 异步加载区块并等待加载完成
        ChunkLoadingManager.getInstance().addWeakLoadedChunkAsync(targetLevel, broomChunk.x, broomChunk.z, 
            (chunkLoaded) -> {
                try {
                    if (chunkLoaded) {
                        AshenWitchBroom.WRAPPED_LOGGER.debug("BroomDataManager - 区块 ({}, {}) 加载完成，开始验证扫帚 '{}' 在维度 {}", 
                            broomChunk.x, broomChunk.z, broomName, targetLevel.dimension().location());
                        
                        // 区块加载完成后，验证实体存在性
                        Entity targetEntity = targetLevel.getEntity(targetEntityUUID);
                        if (targetEntity instanceof MajoBroomEntity broomEntity) {
                            // 验证扫帚名称和拥有者匹配
                            if (broomData.getBroomName().equals(broomEntity.getBroomName()) && 
                                playerUUID.equals(broomEntity.getOwnerUUID())) {
                                AshenWitchBroom.WRAPPED_LOGGER.debug("玩家 {} 的扫帚 '{}' 已通过UUID {} 验证",
                            playerUUID, broomName, targetEntityUUID);
                                result.complete(true);
                            } else {
                                AshenWitchBroom.WRAPPED_LOGGER.warn("找到扫帚实体UUID {} 但名称/拥有者不匹配。期望：{}/{}，实际：{}/{}", 
                                    targetEntityUUID, broomData.getBroomName(), playerUUID, 
                                    broomEntity.getBroomName(), broomEntity.getOwnerUUID());
                                result.complete(false);
                            }
                        } else {
                            AshenWitchBroom.WRAPPED_LOGGER.debug("区块加载后未找到UUID为 {} 的扫帚实体，移除过期数据", targetEntityUUID);
                            // 实体不存在，删除过期数据
                            removeBroom(targetLevel, playerUUID, broomName);
                            result.complete(false);
                        }
                    } else {
                        AshenWitchBroom.WRAPPED_LOGGER.error("BroomDataManager - 区块 ({}, {}) 加载失败或超时，无法验证扫帚 '{}'", 
                            broomChunk.x, broomChunk.z, broomName);
                        result.complete(false);
                    }
                } finally {
                    // 验证完成后立即清理加载的区块
                    ChunkLoadingManager.getInstance().removeWeakLoadedChunk(targetLevel, broomChunk.x, broomChunk.z);
                    AshenWitchBroom.WRAPPED_LOGGER.debug("BroomDataManager - 已清理验证用的弱加载区块 ({}, {}) 在维度 {}", 
                        broomChunk.x, broomChunk.z, targetLevel.dimension().location());
                }
            }, 15); // 15秒超时
        
        return result;
    }
    
    /**
     * 生成唯一的扫帚名称
     */
    public static String generateUniqueBroomName(ServerLevel level, UUID playerUUID) {
        List<BroomData> existingBrooms = loadPlayerBrooms(level, playerUUID);
        int counter = 1;
        String baseName = Component.translatable("broom.ashenwitchbroom.default_name").getString();
        String broomName = baseName + counter;
        
        // 确保名称唯一
        boolean nameExists = true;
        while (nameExists) {
            final String currentName = broomName;
            nameExists = existingBrooms.stream().anyMatch(broom -> broom.getBroomName().equals(currentName));
            if (nameExists) {
                counter++;
                broomName = baseName + counter;
            }
        }
        
        return broomName;
    }
    
    /**
     * 基于现有名称生成唯一的扫帚名称（重载版本）
     */
    public static String generateUniqueBroomName(ServerLevel level, UUID playerUUID, String baseName) {
        List<BroomData> existingBrooms = loadPlayerBrooms(level, playerUUID);
        
        // 首先检查原名称是否已存在
        boolean nameExists = existingBrooms.stream().anyMatch(broom -> broom.getBroomName().equals(baseName));
        if (!nameExists) {
            return baseName; // 原名称不存在冲突，直接返回
        }
        
        // 如果存在冲突，添加数字后缀
        int counter = 2;
        String broomName = baseName + "_" + counter;
        
        // 确保名称唯一
        nameExists = true;
        while (nameExists) {
            final String currentName = broomName;
            nameExists = existingBrooms.stream().anyMatch(broom -> broom.getBroomName().equals(currentName));
            if (nameExists) {
                counter++;
                broomName = baseName + "_" + counter;
            }
        }
        
        return broomName;
    }
}

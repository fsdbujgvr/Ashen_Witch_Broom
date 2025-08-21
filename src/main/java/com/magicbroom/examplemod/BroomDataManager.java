package com.magicbroom.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.io.File;
import java.io.IOException;
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
                AshenWitchBroom.LOGGER.error("Failed to load broom data for player {}: {}", playerUUID, e.getMessage());
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
                AshenWitchBroom.LOGGER.error("Failed to save broom data for player {}: {}", playerUUID, e.getMessage());
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
        
        AshenWitchBroom.LOGGER.info("Added broom '{}' for player {} at {} in {} with entityUUID {}", 
            broomName, playerUUID, position, dimension.location(), entityUUID);
    }
    
    /**
     * 删除扫帚数据
     */
    public static boolean removeBroom(ServerLevel level, UUID playerUUID, String broomName) {
        List<BroomData> brooms = loadPlayerBrooms(level, playerUUID);
        boolean removed = brooms.removeIf(broom -> broom.getBroomName().equals(broomName));
        
        if (removed) {
            savePlayerBrooms(level, playerUUID, brooms);
            AshenWitchBroom.LOGGER.info("Removed broom '{}' for player {}", broomName, playerUUID);
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
                
                AshenWitchBroom.LOGGER.info("Updated broom '{}' for player {} to {} in {} with entityUUID {}", 
                    broomName, playerUUID, newPosition, newDimension.location(), newEntityUUID);
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
            AshenWitchBroom.LOGGER.debug("Broom '{}' not found for player {}", broomName, playerUUID);
            return false;
        }
        
        UUID storedUUID = broom.getEntityUUID();
        if (storedUUID == null) {
            AshenWitchBroom.LOGGER.warn("Broom '{}' has no entityUUID, removing invalid data for player {}", broomName, playerUUID);
            removeBroom(level, playerUUID, broomName);
            return false;
        }
        
        if (!storedUUID.equals(expectedEntityUUID)) {
            AshenWitchBroom.LOGGER.warn("Broom '{}' entity UUID mismatch for player {}. Expected: {}, Stored: {}. Removing stale data.", 
                broomName, playerUUID, expectedEntityUUID, storedUUID);
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
            AshenWitchBroom.LOGGER.debug("Skipping broom verification for player {} during write operation", playerUUID);
            return CompletableFuture.completedFuture(false);
        }
        
        BroomData broomData = findBroom((ServerLevel) player.level(), playerUUID, broomName);
        if (broomData == null) {
            AshenWitchBroom.LOGGER.debug("Broom '{}' not found in data for player {}", broomName, playerUUID);
            return CompletableFuture.completedFuture(false);
        }
        
        // 必须有entityUUID才能验证
        UUID targetEntityUUID = broomData.getEntityUUID();
        if (targetEntityUUID == null) {
            AshenWitchBroom.LOGGER.warn("Broom '{}' has no entityUUID, removing invalid data for player {}", broomName, playerUUID);
            // 删除无效的旧数据
            removeBroom((ServerLevel) player.level(), playerUUID, broomName);
            return CompletableFuture.completedFuture(false);
        }
        
        // 获取目标维度的服务端世界
        ServerLevel targetLevel = player.getServer().getLevel(broomData.getDimension());
        if (targetLevel == null) {
            AshenWitchBroom.LOGGER.warn("Target dimension {} not found for broom '{}'", broomData.getDimension().location(), broomName);
            return CompletableFuture.completedFuture(false);
        }
        
        return CompletableFuture.supplyAsync(() -> {
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
            
            // 仅使用UUID验证实体存在性
            Entity targetEntity = targetLevel.getEntity(targetEntityUUID);
            if (targetEntity instanceof MajoBroomEntity broomEntity) {
                // 验证扫帚名称和拥有者匹配
                if (broomData.getBroomName().equals(broomEntity.getBroomName()) && 
                    playerUUID.equals(broomEntity.getOwnerUUID())) {
                    AshenWitchBroom.LOGGER.debug("Broom '{}' verified by UUID {} for player {}", 
                        broomName, targetEntityUUID, playerUUID);
                    return true;
                } else {
                    AshenWitchBroom.LOGGER.warn("Broom entity UUID {} found but name/owner mismatch. Expected: {}/{}, Found: {}/{}", 
                        targetEntityUUID, broomData.getBroomName(), playerUUID, 
                        broomEntity.getBroomName(), broomEntity.getOwnerUUID());
                    return false;
                }
            } else {
                AshenWitchBroom.LOGGER.debug("Broom entity with UUID {} not found after chunk loading, removing stale data", targetEntityUUID);
                // 实体不存在，删除过期数据
                removeBroom(targetLevel, playerUUID, broomName);
                return false;
            }
        });
    }
    
    /**
     * 生成唯一的扫帚名称
     */
    public static String generateUniqueBroomName(ServerLevel level, UUID playerUUID) {
        List<BroomData> existingBrooms = loadPlayerBrooms(level, playerUUID);
        int counter = 1;
        String baseName = "扫帚";
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
}

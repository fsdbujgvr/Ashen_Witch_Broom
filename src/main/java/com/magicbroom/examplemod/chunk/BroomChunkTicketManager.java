package com.magicbroom.examplemod.chunk;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import com.magicbroom.examplemod.core.AshenWitchBroom;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.nbt.NbtAccounter;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 扫帚区块票据管理器
 * 负责管理扫帚相关的区块强制加载功能
 * 支持强加载（ticking）和弱加载（lazy）两种模式
 * 支持批量操作和持久化功能
 */
public class BroomChunkTicketManager {
    
    // 弱加载票据定义 - 用于扫帚区块加载
    private static final TicketType<ChunkPos> BROOM_WEAK_LOADING_TICKET = 
        TicketType.create("ashenwitchbroom_command_weak", (a, b) -> Long.compare(a.toLong(), b.toLong()), 0);
    
    // 强加载票据定义 - 用于扫帚区块强加载
    private static final TicketType<ChunkPos> BROOM_STRONG_LOADING_TICKET = 
        TicketType.create("ashenwitchbroom_command_strong", (a, b) -> Long.compare(a.toLong(), b.toLong()), 0);
    
    // 存储加载的区块 <维度ID, <区块位置, 加载类型>>
    // 加载类型：true = 强加载(ticking), false = 弱加载(lazy)
    private static final Map<String, Map<ChunkPos, Boolean>> loadedChunks = 
        new ConcurrentHashMap<>();
    
    // 异步执行器，用于区块状态检测
    private static final ScheduledExecutorService chunkCheckExecutor = 
        Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "BroomChunkTicketManager-AsyncChecker");
            t.setDaemon(true);
            return t;
        });
    
    // 单例模式
    private static BroomChunkTicketManager instance;
    
    // 持久化相关常量
    private static final String CHUNKS_DATA_FOLDER = "ashenwitchbroom";
    private static final String CHUNKS_SUBFOLDER = "chunks";
    private static final String WEAK_CHUNKS_DATA_FILE = "weak_loaded_chunks.dat";
    private static final String STRONG_CHUNKS_DATA_FILE = "strong_loaded_chunks.dat";
    
    // 服务器实例引用，用于获取存档路径
    private MinecraftServer server;
    
    public static BroomChunkTicketManager getInstance() {
        if (instance == null) {
            instance = new BroomChunkTicketManager();
        }
        return instance;
    }
    
    /**
     * 设置服务器实例（在服务器启动时调用）
     * @param server 服务器实例
     */
    public void setServer(MinecraftServer server) {
        this.server = server;
        // 服务器启动时加载持久化数据
        loadChunkDataFromFile();
    }
    
    /**
     * 区块添加结果枚举
     */
    public enum ChunkAddResult {
        SUCCESS,                    // 成功添加
        ALREADY_EXISTS_SAME_TYPE,   // 已存在相同类型
        ALREADY_EXISTS_DIFFERENT_TYPE, // 已存在不同类型
        FAILED                      // 添加失败
    }
    
    /**
     * 批量操作结果类
     */
    public static class BatchOperationResult {
        private final int successCount;
        private final int failureCount;
        private final List<ChunkPos> successChunks;
        private final List<ChunkPos> failureChunks;
        private final List<ChunkPos> alreadyExistsSameType;
        private final List<ChunkPos> alreadyExistsDifferentType;
        
        public BatchOperationResult(int successCount, int failureCount, 
                                  List<ChunkPos> successChunks, List<ChunkPos> failureChunks) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.successChunks = successChunks;
            this.failureChunks = failureChunks;
            this.alreadyExistsSameType = new ArrayList<>();
            this.alreadyExistsDifferentType = new ArrayList<>();
        }
        
        public BatchOperationResult(int successCount, int failureCount, 
                                  List<ChunkPos> successChunks, List<ChunkPos> failureChunks,
                                  List<ChunkPos> alreadyExistsSameType, List<ChunkPos> alreadyExistsDifferentType) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.successChunks = successChunks;
            this.failureChunks = failureChunks;
            this.alreadyExistsSameType = alreadyExistsSameType;
            this.alreadyExistsDifferentType = alreadyExistsDifferentType;
        }
        
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public List<ChunkPos> getSuccessChunks() { return successChunks; }
        public List<ChunkPos> getFailureChunks() { return failureChunks; }
        public List<ChunkPos> getAlreadyExistsSameType() { return alreadyExistsSameType; }
        public List<ChunkPos> getAlreadyExistsDifferentType() { return alreadyExistsDifferentType; }
        public int getTotalCount() { return successCount + failureCount; }
    }
    
    /**
     * 添加弱加载区块（lazy模式）
     * @param level 服务端世界
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 添加结果
     */
    public ChunkAddResult addLazyLoadedChunk(ServerLevel level, int chunkX, int chunkZ) {
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        String dimensionKey = level.dimension().location().toString();
        
        // 检查区块是否已经存在
        Boolean existingType = getChunkLoadType(level, chunkX, chunkZ);
        if (existingType != null) {
            if (!existingType) {
                // 已存在相同类型（弱加载）
                AshenWitchBroom.WRAPPED_LOGGER.debug("区块 ({}, {}) 在维度 {} 已经是弱加载状态",
                    chunkX, chunkZ, dimensionKey);
                return ChunkAddResult.ALREADY_EXISTS_SAME_TYPE;
            } else {
                // 已存在不同类型（强加载）
                AshenWitchBroom.WRAPPED_LOGGER.debug("区块 ({}, {}) 在维度 {} 已经是强加载状态，无法添加为弱加载",
                    chunkX, chunkZ, dimensionKey);
                return ChunkAddResult.ALREADY_EXISTS_DIFFERENT_TYPE;
            }
        }
        
        try {
            // 使用弱加载票据系统，半径0确保只加载单个区块
            level.getChunkSource().addRegionTicket(BROOM_WEAK_LOADING_TICKET, chunkPos, 0, chunkPos);
            
            loadedChunks.computeIfAbsent(dimensionKey, k -> new ConcurrentHashMap<>())
                       .put(chunkPos, false); // false = 弱加载
            
            // 自动保存数据
            saveChunkDataToFile();
            
            AshenWitchBroom.WRAPPED_LOGGER.debug("成功添加弱加载区块：({}, {}) 在维度 {}",
                chunkX, chunkZ, dimensionKey);
            return ChunkAddResult.SUCCESS;
        } catch (Exception e) {
            AshenWitchBroom.WRAPPED_LOGGER.error("添加弱加载区块失败：({}, {}) 在维度 {}：{}",
                    chunkX, chunkZ, dimensionKey, e.getMessage());
            return ChunkAddResult.FAILED;
        }
    }
    
    /**
     * 添加强加载区块（ticking模式）
     * @param level 服务端世界
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 添加结果
     */
    public ChunkAddResult addTickingLoadedChunk(ServerLevel level, int chunkX, int chunkZ) {
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        String dimensionKey = level.dimension().location().toString();
        
        // 检查区块是否已经存在
        Boolean existingType = getChunkLoadType(level, chunkX, chunkZ);
        if (existingType != null) {
            if (existingType) {
                // 已存在相同类型（强加载）
                AshenWitchBroom.WRAPPED_LOGGER.debug("区块 ({}, {}) 在维度 {} 已经是强加载状态",
                    chunkX, chunkZ, dimensionKey);
                return ChunkAddResult.ALREADY_EXISTS_SAME_TYPE;
            } else {
                // 已存在不同类型（弱加载）
                AshenWitchBroom.WRAPPED_LOGGER.debug("区块 ({}, {}) 在维度 {} 已经是弱加载状态，无法添加为强加载",
                    chunkX, chunkZ, dimensionKey);
                return ChunkAddResult.ALREADY_EXISTS_DIFFERENT_TYPE;
            }
        }
        
        try {
            // 使用区块票据系统进行强加载，半径为2
            level.getChunkSource().addRegionTicket(BROOM_STRONG_LOADING_TICKET, chunkPos, 2, chunkPos);
            
            loadedChunks.computeIfAbsent(dimensionKey, k -> new ConcurrentHashMap<>())
                       .put(chunkPos, true); // true = 强加载
            
            // 自动保存数据
            saveChunkDataToFile();
            
            AshenWitchBroom.WRAPPED_LOGGER.debug("成功添加强加载区块：({}, {}) 在维度 {}，半径为2",
                chunkX, chunkZ, dimensionKey);
            return ChunkAddResult.SUCCESS;
        } catch (Exception e) {
            AshenWitchBroom.WRAPPED_LOGGER.error("添加强加载区块失败：({}, {}) 在维度 {}：{}",
                    chunkX, chunkZ, dimensionKey, e.getMessage());
            return ChunkAddResult.FAILED;
        }
    }
    
    /**
     * 移除加载的区块
     * @param level 服务端世界
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @param loadType 加载类型：true = 强加载, false = 弱加载, null = 自动检测
     * @return 是否成功移除
     */
    public boolean removeLoadedChunk(ServerLevel level, int chunkX, int chunkZ, Boolean loadType) {
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        String dimensionKey = level.dimension().location().toString();
        
        Map<ChunkPos, Boolean> dimensionChunks = loadedChunks.get(dimensionKey);
        if (dimensionChunks == null || !dimensionChunks.containsKey(chunkPos)) {
            return false;
        }
        
        // 如果没有指定加载类型，从存储中获取
        boolean isStrongLoaded = loadType != null ? loadType : dimensionChunks.get(chunkPos);
        
        try {
            if (isStrongLoaded) {
                // 使用区块票据系统移除强加载
                level.getChunkSource().removeRegionTicket(BROOM_STRONG_LOADING_TICKET, chunkPos, 2, chunkPos);
            } else {
                level.getChunkSource().removeRegionTicket(BROOM_WEAK_LOADING_TICKET, chunkPos, 0, chunkPos);
            }
            
            dimensionChunks.remove(chunkPos);
            if (dimensionChunks.isEmpty()) {
                loadedChunks.remove(dimensionKey);
            }
            
            // 自动保存数据
            saveChunkDataToFile();
            
            AshenWitchBroom.WRAPPED_LOGGER.debug("成功移除{}区块：({}, {}) 在维度 {}",
                isStrongLoaded ? "强加载" : "弱加载", chunkX, chunkZ, dimensionKey);
            return true;
        } catch (Exception e) {
            AshenWitchBroom.WRAPPED_LOGGER.error("移除{}区块失败：({}, {}) 在维度 {}：{}",
                isStrongLoaded ? "强加载" : "弱加载", chunkX, chunkZ, dimensionKey, e.getMessage());
            return false;
        }
    }
    
    /**
     * 批量添加弱加载区块
     * @param level 服务端世界
     * @param chunks 区块位置列表
     * @return 批量操作结果
     */
    public BatchOperationResult addLazyLoadedChunksBatch(ServerLevel level, List<ChunkPos> chunks) {
        List<ChunkPos> successChunks = new ArrayList<>();
        List<ChunkPos> failureChunks = new ArrayList<>();
        List<ChunkPos> alreadyExistsSameType = new ArrayList<>();
        List<ChunkPos> alreadyExistsDifferentType = new ArrayList<>();
        
        for (ChunkPos chunkPos : chunks) {
            ChunkAddResult result = addLazyLoadedChunk(level, chunkPos.x, chunkPos.z);
            
            switch (result) {
                case SUCCESS:
                    successChunks.add(chunkPos);
                    break;
                case ALREADY_EXISTS_SAME_TYPE:
                    alreadyExistsSameType.add(chunkPos);
                    break;
                case ALREADY_EXISTS_DIFFERENT_TYPE:
                    alreadyExistsDifferentType.add(chunkPos);
                    break;
                case FAILED:
                    failureChunks.add(chunkPos);
                    break;
            }
        }
        
        return new BatchOperationResult(successChunks.size(), failureChunks.size(), 
                                      successChunks, failureChunks,
                                      alreadyExistsSameType, alreadyExistsDifferentType);
    }
    
    /**
     * 批量添加强加载区块
     * @param level 服务端世界
     * @param chunks 区块位置列表
     * @return 批量操作结果
     */
    public BatchOperationResult addTickingLoadedChunksBatch(ServerLevel level, List<ChunkPos> chunks) {
        List<ChunkPos> successChunks = new ArrayList<>();
        List<ChunkPos> failureChunks = new ArrayList<>();
        List<ChunkPos> alreadyExistsSameType = new ArrayList<>();
        List<ChunkPos> alreadyExistsDifferentType = new ArrayList<>();
        
        for (ChunkPos chunkPos : chunks) {
            ChunkAddResult result = addTickingLoadedChunk(level, chunkPos.x, chunkPos.z);
            
            switch (result) {
                case SUCCESS:
                    successChunks.add(chunkPos);
                    break;
                case ALREADY_EXISTS_SAME_TYPE:
                    alreadyExistsSameType.add(chunkPos);
                    break;
                case ALREADY_EXISTS_DIFFERENT_TYPE:
                    alreadyExistsDifferentType.add(chunkPos);
                    break;
                case FAILED:
                    failureChunks.add(chunkPos);
                    break;
            }
        }
        
        return new BatchOperationResult(successChunks.size(), failureChunks.size(), 
                                      successChunks, failureChunks,
                                      alreadyExistsSameType, alreadyExistsDifferentType);
    }
    
    /**
     * 批量移除加载的区块
     * @param level 服务端世界
     * @param chunks 区块位置列表
     * @param loadType 加载类型：true = 强加载, false = 弱加载, null = 自动检测
     * @return 批量操作结果
     */
    public BatchOperationResult removeLoadedChunksBatch(ServerLevel level, List<ChunkPos> chunks, Boolean loadType) {
        List<ChunkPos> successChunks = new ArrayList<>();
        List<ChunkPos> failureChunks = new ArrayList<>();
        String dimensionKey = level.dimension().location().toString();
        
        Map<ChunkPos, Boolean> dimensionChunks = loadedChunks.get(dimensionKey);
        if (dimensionChunks == null) {
            // 如果该维度没有加载的区块，所有操作都失败
            return new BatchOperationResult(0, chunks.size(), successChunks, new ArrayList<>(chunks));
        }
        
        for (ChunkPos chunkPos : chunks) {
            if (!dimensionChunks.containsKey(chunkPos)) {
                failureChunks.add(chunkPos);
                continue;
            }
            
            // 如果没有指定加载类型，从存储中获取
            boolean isStrongLoaded = loadType != null ? loadType : dimensionChunks.get(chunkPos);
            
            try {
                if (isStrongLoaded) {
                    // 使用区块票据系统移除强加载
                    level.getChunkSource().removeRegionTicket(BROOM_STRONG_LOADING_TICKET, chunkPos, 2, chunkPos);
                } else {
                    level.getChunkSource().removeRegionTicket(BROOM_WEAK_LOADING_TICKET, chunkPos, 0, chunkPos);
                }
                
                dimensionChunks.remove(chunkPos);
                successChunks.add(chunkPos);
                
                AshenWitchBroom.WRAPPED_LOGGER.debug("成功移除{}区块：({}, {}) 在维度 {}",
                    isStrongLoaded ? "强加载" : "弱加载", chunkPos.x, chunkPos.z, dimensionKey);
            } catch (Exception e) {
                failureChunks.add(chunkPos);
                AshenWitchBroom.WRAPPED_LOGGER.error("移除{}区块失败：({}, {}) 在维度 {}：{}",
                    isStrongLoaded ? "强加载" : "弱加载", chunkPos.x, chunkPos.z, dimensionKey, e.getMessage());
            }
        }
        
        // 清理空的维度映射
        if (dimensionChunks.isEmpty()) {
            loadedChunks.remove(dimensionKey);
        }
        
        // 批量操作完成后统一保存
        if (!successChunks.isEmpty()) {
            saveChunkDataToFile();
        }
        
        return new BatchOperationResult(successChunks.size(), failureChunks.size(), 
                                      successChunks, failureChunks);
    }
    
    /**
     * 解析坐标范围，支持单个坐标和范围坐标
     * @param coordStr 坐标字符串，格式：x,z 或 x1,z1 x2,z2
     * @return 区块位置列表
     */
    public static List<ChunkPos> parseCoordinates(String coordStr) {
        List<ChunkPos> chunks = new ArrayList<>();
        
        try {
            String[] parts = coordStr.trim().split("\\s+");
            
            if (parts.length == 1) {
                // 单个坐标：x,z
                String[] coords = parts[0].split(",");
                if (coords.length == 2) {
                    int x = Integer.parseInt(coords[0].trim());
                    int z = Integer.parseInt(coords[1].trim());
                    chunks.add(new ChunkPos(x, z));
                }
            } else if (parts.length == 2) {
                // 范围坐标：x1,z1 x2,z2
                String[] coords1 = parts[0].split(",");
                String[] coords2 = parts[1].split(",");
                
                if (coords1.length == 2 && coords2.length == 2) {
                    int x1 = Integer.parseInt(coords1[0].trim());
                    int z1 = Integer.parseInt(coords1[1].trim());
                    int x2 = Integer.parseInt(coords2[0].trim());
                    int z2 = Integer.parseInt(coords2[1].trim());
                    
                    // 确保坐标顺序正确
                    int minX = Math.min(x1, x2);
                    int maxX = Math.max(x1, x2);
                    int minZ = Math.min(z1, z2);
                    int maxZ = Math.max(z1, z2);
                    
                    // 生成范围内的所有区块
                    for (int x = minX; x <= maxX; x++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            chunks.add(new ChunkPos(x, z));
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            AshenWitchBroom.WRAPPED_LOGGER.error("解析坐标失败：{}", coordStr, e);
        }
        
        return chunks;
    }
    
    /**
     * 解析坐标参数，支持 x y 或 x y w h 格式
     * @param args 参数数组，长度必须为2或4
     * @return 区块位置列表
     * @throws IllegalArgumentException 如果参数数量不正确
     */
    public static List<ChunkPos> parseCoordinateArgs(int[] args) {
        List<ChunkPos> chunks = new ArrayList<>();
        
        if (args.length == 2) {
            // 单个区块：x y
            int x = args[0];
            int z = args[1];
            chunks.add(new ChunkPos(x, z));
        } else if (args.length == 4) {
            // 范围区块：x y w h
            int x = args[0];
            int z = args[1];
            int w = args[2];
            int h = args[3];
            
            // 生成从 (x, z) 开始，宽度为w，高度为h的区块范围
            for (int dx = 0; dx < w; dx++) {
                for (int dz = 0; dz < h; dz++) {
                    chunks.add(new ChunkPos(x + dx, z + dz));
                }
            }
        } else {
            throw new IllegalArgumentException("参数数量必须为2个（x y）或4个（x y w h），实际为：" + args.length);
        }
        
        return chunks;
    }
    
    /**
     * 检查区块是否被此管理器加载
     * @param level 服务端世界
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 加载类型：true = 强加载, false = 弱加载, null = 未加载
     */
    public Boolean getChunkLoadType(ServerLevel level, int chunkX, int chunkZ) {
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        String dimensionKey = level.dimension().location().toString();
        
        Map<ChunkPos, Boolean> dimensionChunks = loadedChunks.get(dimensionKey);
        if (dimensionChunks != null && dimensionChunks.containsKey(chunkPos)) {
            return dimensionChunks.get(chunkPos);
        }
        return null;
    }
    
    /**
     * 检查区块是否被此管理器加载
     * @param level 服务端世界
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 是否已加载
     */
    public boolean isChunkLoadedByManager(ServerLevel level, int chunkX, int chunkZ) {
        return getChunkLoadType(level, chunkX, chunkZ) != null;
    }
    
    /**
     * 获取指定维度中所有已加载的区块
     * @param level 服务端世界
     * @return 已加载的区块位置和类型映射
     */
    public Map<ChunkPos, Boolean> getLoadedChunksInDimension(ServerLevel level) {
        String dimensionKey = level.dimension().location().toString();
        return loadedChunks.getOrDefault(dimensionKey, new ConcurrentHashMap<>());
    }
    
    /**
     * 获取指定维度中指定类型的已加载区块
     * @param level 服务端世界
     * @param loadType 加载类型：true = 强加载, false = 弱加载
     * @return 指定类型的区块位置集合
     */
    public Set<ChunkPos> getLoadedChunksByType(ServerLevel level, boolean loadType) {
        Map<ChunkPos, Boolean> allChunks = getLoadedChunksInDimension(level);
        Set<ChunkPos> result = new HashSet<>();
        
        for (Map.Entry<ChunkPos, Boolean> entry : allChunks.entrySet()) {
            if (entry.getValue() == loadType) {
                result.add(entry.getKey());
            }
        }
        
        return result;
    }
    
    /**
     * 检查区块是否已完全加载并可以安全访问实体
     * @param level 服务端世界
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 区块是否已完全加载且可以安全访问实体
     */
    public boolean hasChunk(ServerLevel level, int chunkX, int chunkZ) {
        try {
            // 检查区块是否处于实体tick范围内
            ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
            boolean isEntityTicking = level.areEntitiesLoaded(chunkPos.toLong());
            if (!isEntityTicking) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            AshenWitchBroom.WRAPPED_LOGGER.debug("区块 ({}, {}) 在维度 {} 检查失败: {}", 
                chunkX, chunkZ, level.dimension().location().toString(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 异步等待区块加载完成，然后执行回调
     * @param level 服务端世界
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @param onChunkLoaded 区块加载完成后的回调函数
     * @param timeoutSeconds 超时时间（秒）
     * @return CompletableFuture，可用于取消或等待完成
     */
    public CompletableFuture<Boolean> waitForChunkLoadedAsync(ServerLevel level, int chunkX, int chunkZ, 
                                                             Consumer<Boolean> onChunkLoaded, int timeoutSeconds) {
        return CompletableFuture.supplyAsync(() -> {
            String dimensionKey = level.dimension().location().toString();
            
            long startTime = System.currentTimeMillis();
            long timeoutMs = timeoutSeconds * 1000L;
            int checkCount = 0;
            
            try {
                while (System.currentTimeMillis() - startTime < timeoutMs) {
                    checkCount++;
                    
                    if (hasChunk(level, chunkX, chunkZ)) {
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        AshenWitchBroom.WRAPPED_LOGGER.debug("区块加载完成: ({}, {}) 在维度 {}, 耗时: {}ms, 检测次数: {}", 
                            chunkX, chunkZ, dimensionKey, elapsedTime, checkCount);
                        return true;
                    }
                    
                    // 统一检测间隔为50ms
                    Thread.sleep(50);
                }
                
                AshenWitchBroom.WRAPPED_LOGGER.warn("区块加载超时: ({}, {}) 在维度 {}, 超时时间: {}秒, 总检测次数: {}", 
                    chunkX, chunkZ, dimensionKey, timeoutSeconds, checkCount);
                return false;
                
            } catch (InterruptedException e) {
                AshenWitchBroom.WRAPPED_LOGGER.warn("区块加载等待被中断: ({}, {}) 在维度 {}, 检测次数: {}", 
                    chunkX, chunkZ, dimensionKey, checkCount);
                Thread.currentThread().interrupt();
                return false;
            }
        }, chunkCheckExecutor).whenComplete((result, throwable) -> {
            // 将回调调度到主线程执行
            level.getServer().execute(() -> {
                if (throwable != null) {
                    AshenWitchBroom.WRAPPED_LOGGER.error("异步区块加载检测出错: ({}, {}) 在维度 {}: {}", 
                        chunkX, chunkZ, level.dimension().location().toString(), throwable.getMessage());
                    onChunkLoaded.accept(false);
                } else {
                    onChunkLoaded.accept(result);
                }
            });
        });
    }
    
    /**
     * 清理所有已加载的区块（服务器关闭时调用）
     */
    public void clearAllLoadedChunks() {
        loadedChunks.clear();
        AshenWitchBroom.WRAPPED_LOGGER.debug("已清除BroomChunkTicketManager中的所有加载区块");
    }
    
    /**
     * 关闭异步执行器（服务器关闭时调用）
     */
    public void shutdown() {
        // 在关闭前保存区块数据
        AshenWitchBroom.WRAPPED_LOGGER.info("正在保存区块数据...");
        saveChunkDataToFile();
        
        chunkCheckExecutor.shutdown();
        try {
            if (!chunkCheckExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                chunkCheckExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            chunkCheckExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        AshenWitchBroom.WRAPPED_LOGGER.debug("BroomChunkTicketManager执行器关闭完成");
    }
    
    /**
     * 获取所有已加载的区块数据
     * @return 所有维度的区块数据
     */
    public Map<String, Map<ChunkPos, Boolean>> getAllLoadedChunks() {
        return new ConcurrentHashMap<>(loadedChunks);
    }
    
    /**
     * 获取统计信息
     * @param level 服务端世界
     * @return 统计信息数组：[总数, 强加载数, 弱加载数]
     */
    public int[] getStatistics(ServerLevel level) {
        Map<ChunkPos, Boolean> chunks = getLoadedChunksInDimension(level);
        int total = chunks.size();
        int strongCount = 0;
        int weakCount = 0;
        
        for (Boolean isStrong : chunks.values()) {
            if (isStrong) {
                strongCount++;
            } else {
                weakCount++;
            }
        }
        
        return new int[]{total, strongCount, weakCount};
    }
    
    /**
     * 获取弱加载区块数据存储路径
     * @return 弱加载区块数据文件路径
     */
    private Path getWeakChunkDataPath() {
        return getChunkDataPath(WEAK_CHUNKS_DATA_FILE);
    }
    
    /**
     * 获取强加载区块数据存储路径
     * @return 强加载区块数据文件路径
     */
    private Path getStrongChunkDataPath() {
        return getChunkDataPath(STRONG_CHUNKS_DATA_FILE);
    }
    
    /**
     * 获取区块数据存储路径
     * @param fileName 数据文件名
     * @return 区块数据文件路径
     */
    private Path getChunkDataPath(String fileName) {
        if (server == null) {
            AshenWitchBroom.WRAPPED_LOGGER.warn("服务器实例为空，无法获取存档路径");
            return null;
        }
        
        Path worldPath = server.getWorldPath(LevelResource.ROOT);
        Path dataFolder = worldPath.resolve(CHUNKS_DATA_FOLDER);
        Path chunksFolder = dataFolder.resolve(CHUNKS_SUBFOLDER);
        
        try {
            // 确保目录存在
            Files.createDirectories(chunksFolder);
        } catch (IOException e) {
            AshenWitchBroom.WRAPPED_LOGGER.error("创建区块数据目录失败: {}", e.getMessage());
            return null;
        }
        
        return chunksFolder.resolve(fileName);
    }
    
    /**
     * 保存区块数据到文件
     */
    public void saveChunkDataToFile() {
        saveWeakChunkDataToFile();
        saveStrongChunkDataToFile();
    }
    
    /**
     * 保存弱加载区块数据到文件
     */
    private void saveWeakChunkDataToFile() {
        Path dataPath = getWeakChunkDataPath();
        if (dataPath == null) {
            return;
        }
        
        saveChunkDataToFile(dataPath, false, "弱加载");
    }
    
    /**
     * 保存强加载区块数据到文件
     */
    private void saveStrongChunkDataToFile() {
        Path dataPath = getStrongChunkDataPath();
        if (dataPath == null) {
            return;
        }
        
        saveChunkDataToFile(dataPath, true, "强加载");
    }
    
    /**
     * 保存指定类型的区块数据到文件
     * @param dataPath 文件路径
     * @param isStrong 是否为强加载类型
     * @param typeName 类型名称（用于日志）
     */
    private void saveChunkDataToFile(Path dataPath, boolean isStrong, String typeName) {
        try {
            CompoundTag rootTag = new CompoundTag();
            CompoundTag dimensionsTag = new CompoundTag();
            
            // 遍历所有维度的区块数据
            for (Map.Entry<String, Map<ChunkPos, Boolean>> dimensionEntry : loadedChunks.entrySet()) {
                String dimensionId = dimensionEntry.getKey();
                Map<ChunkPos, Boolean> chunks = dimensionEntry.getValue();
                
                ListTag chunksList = new ListTag();
                
                // 只保存指定类型的区块
                for (Map.Entry<ChunkPos, Boolean> chunkEntry : chunks.entrySet()) {
                    ChunkPos pos = chunkEntry.getKey();
                    Boolean chunkIsStrong = chunkEntry.getValue();
                    
                    // 只保存匹配类型的区块
                    if (chunkIsStrong.equals(isStrong)) {
                        CompoundTag chunkTag = new CompoundTag();
                        chunkTag.putInt("x", pos.x);
                        chunkTag.putInt("z", pos.z);
                        
                        chunksList.add(chunkTag);
                    }
                }
                
                // 只有当该维度有该类型的区块时才添加
                if (!chunksList.isEmpty()) {
                    dimensionsTag.put(dimensionId, chunksList);
                }
            }
            
            rootTag.put("dimensions", dimensionsTag);
            rootTag.putString("version", "1.0");
            rootTag.putString("type", isStrong ? "strong" : "weak");
            rootTag.putLong("saveTime", System.currentTimeMillis());
            
            // 写入文件
            NbtIo.writeCompressed(rootTag, dataPath);
            
            AshenWitchBroom.WRAPPED_LOGGER.debug("{}区块数据已保存到: {}", typeName, dataPath);
            
        } catch (IOException e) {
            AshenWitchBroom.WRAPPED_LOGGER.error("保存{}区块数据失败: {}", typeName, e.getMessage());
        }
    }
    
    /**
     * 从文件加载区块数据
     */
    public void loadChunkDataFromFile() {
        // 清空现有数据
        loadedChunks.clear();
        
        // 分别加载弱加载和强加载数据
        int weakCount = loadWeakChunkDataFromFile();
        int strongCount = loadStrongChunkDataFromFile();
        
        int totalCount = weakCount + strongCount;
        AshenWitchBroom.WRAPPED_LOGGER.info("从文件加载了 {} 个区块数据（弱加载: {}, 强加载: {}）", totalCount, weakCount, strongCount);
        
        // 重新应用区块票据
        if (totalCount > 0) {
            reapplyChunkTickets();
        }
    }
    
    /**
     * 从文件加载弱加载区块数据
     * @return 加载的区块数量
     */
    private int loadWeakChunkDataFromFile() {
        Path dataPath = getWeakChunkDataPath();
        return loadChunkDataFromFile(dataPath, false, "弱加载");
    }
    
    /**
     * 从文件加载强加载区块数据
     * @return 加载的区块数量
     */
    private int loadStrongChunkDataFromFile() {
        Path dataPath = getStrongChunkDataPath();
        return loadChunkDataFromFile(dataPath, true, "强加载");
    }
    
    /**
     * 从指定文件加载区块数据
     * @param dataPath 文件路径
     * @param isStrong 是否为强加载类型
     * @param typeName 类型名称（用于日志）
     * @return 加载的区块数量
     */
    private int loadChunkDataFromFile(Path dataPath, boolean isStrong, String typeName) {
        if (dataPath == null || !Files.exists(dataPath)) {
            AshenWitchBroom.WRAPPED_LOGGER.debug("{}区块数据文件不存在，跳过加载: {}", typeName, dataPath);
            return 0;
        }
        
        try {
            CompoundTag rootTag = NbtIo.readCompressed(Files.newInputStream(dataPath), NbtAccounter.unlimitedHeap());
            
            if (!rootTag.contains("dimensions")) {
                AshenWitchBroom.WRAPPED_LOGGER.warn("{}区块数据文件格式无效", typeName);
                return 0;
            }
            
            // 验证文件类型
            String fileType = rootTag.getString("type");
            String expectedType = isStrong ? "strong" : "weak";
            if (!expectedType.equals(fileType)) {
            AshenWitchBroom.WRAPPED_LOGGER.warn("{}区块数据文件类型不匹配，期望: {}, 实际: {}", typeName, expectedType, fileType);
            }
            
            CompoundTag dimensionsTag = rootTag.getCompound("dimensions");
            int loadedChunkCount = 0;
            
            // 遍历所有维度
            for (String dimensionId : dimensionsTag.getAllKeys()) {
                ListTag chunksList = dimensionsTag.getList(dimensionId, Tag.TAG_COMPOUND);
                
                // 获取或创建该维度的区块映射
                Map<ChunkPos, Boolean> dimensionChunks = loadedChunks.computeIfAbsent(dimensionId, k -> new ConcurrentHashMap<>());
                
                // 遍历该维度的所有区块
                for (int i = 0; i < chunksList.size(); i++) {
                    CompoundTag chunkTag = chunksList.getCompound(i);
                    
                    int x = chunkTag.getInt("x");
                    int z = chunkTag.getInt("z");
                    
                    ChunkPos pos = new ChunkPos(x, z);
                    dimensionChunks.put(pos, isStrong);
                    loadedChunkCount++;
                }
            }
            
            AshenWitchBroom.WRAPPED_LOGGER.debug("从{}文件加载了 {} 个区块数据", typeName, loadedChunkCount);
            return loadedChunkCount;
            
        } catch (IOException e) {
            AshenWitchBroom.WRAPPED_LOGGER.error("加载{}区块数据失败: {}", typeName, e.getMessage());
            return 0;
        }
    }
    
    /**
     * 重新应用区块票据（在加载数据后调用）
     */
    private void reapplyChunkTickets() {
        if (server == null) {
            AshenWitchBroom.WRAPPED_LOGGER.warn("服务器实例为空，无法重新应用区块票据");
            return;
        }
        
        int reappliedCount = 0;
        
        for (Map.Entry<String, Map<ChunkPos, Boolean>> dimensionEntry : loadedChunks.entrySet()) {
            String dimensionId = dimensionEntry.getKey();
            Map<ChunkPos, Boolean> chunks = dimensionEntry.getValue();
            
            // 尝试获取对应的服务端世界
            ServerLevel level = null;
            for (ServerLevel serverLevel : server.getAllLevels()) {
                if (serverLevel.dimension().location().toString().equals(dimensionId)) {
                    level = serverLevel;
                    break;
                }
            }
            
            if (level == null) {
                AshenWitchBroom.WRAPPED_LOGGER.warn("找不到维度 {}，跳过区块票据应用", dimensionId);
                continue;
            }
            
            // 为每个区块重新应用票据
            for (Map.Entry<ChunkPos, Boolean> chunkEntry : chunks.entrySet()) {
                ChunkPos pos = chunkEntry.getKey();
                Boolean isStrong = chunkEntry.getValue();
                
                try {
                    if (isStrong) {
                        // 使用区块票据系统重新应用强加载，半径为2
                        level.getChunkSource().addRegionTicket(BROOM_STRONG_LOADING_TICKET, pos, 2, pos);
                    } else {
                        level.getChunkSource().addRegionTicket(BROOM_WEAK_LOADING_TICKET, pos, 0, pos);
                    }
                    reappliedCount++;
                } catch (Exception e) {
                    AshenWitchBroom.WRAPPED_LOGGER.error("重新应用区块票据失败: ({}, {}) 在维度 {}: {}", 
                        pos.x, pos.z, dimensionId, e.getMessage());
                }
            }
        }
        
        AshenWitchBroom.WRAPPED_LOGGER.info("重新应用了 {} 个区块票据", reappliedCount);
    }
    
    // ==================== 区块分组相关方法 ====================
    
    /**
     * 区块区域表示类
     */
    public static class ChunkRegion {
        private final int minX, minZ, maxX, maxZ;
        private final List<ChunkPos> chunks;
        private final boolean isRectangular;
        
        public ChunkRegion(List<ChunkPos> chunks) {
            this.chunks = new ArrayList<>(chunks);
            
            // 计算边界
            int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            
            for (ChunkPos chunk : chunks) {
                minX = Math.min(minX, chunk.x);
                minZ = Math.min(minZ, chunk.z);
                maxX = Math.max(maxX, chunk.x);
                maxZ = Math.max(maxZ, chunk.z);
            }
            
            this.minX = minX;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxZ = maxZ;
            
            // 检查是否为矩形区域
            int expectedChunks = (maxX - minX + 1) * (maxZ - minZ + 1);
            this.isRectangular = chunks.size() == expectedChunks;
        }
        
        public int getMinX() { return minX; }
        public int getMinZ() { return minZ; }
        public int getMaxX() { return maxX; }
        public int getMaxZ() { return maxZ; }
        public List<ChunkPos> getChunks() { return new ArrayList<>(chunks); }
        public boolean isRectangular() { return isRectangular; }
        public int getChunkCount() { return chunks.size(); }
        
        /**
         * 获取区域的显示字符串
         */
        public String getDisplayString() {
            if (chunks.size() == 1) {
                ChunkPos chunk = chunks.get(0);
                return String.format("(%d, %d)", chunk.x, chunk.z);
            } else if (isRectangular && chunks.size() > 1) {
                return String.format("(%d, %d) to (%d, %d) [%dx%d]", 
                    minX, minZ, maxX, maxZ, 
                    maxX - minX + 1, maxZ - minZ + 1);
            } else {
                return String.format("Region with %d chunks: %s", 
                    chunks.size(), 
                    chunks.stream()
                        .map(c -> String.format("(%d,%d)", c.x, c.z))
                        .reduce((a, b) -> a + ", " + b)
                        .orElse(""));
            }
        }
    }
    
    /**
     * 将区块集合分组为相邻的区域
     * @param chunks 区块集合
     * @return 分组后的区域列表
     */
    public static List<ChunkRegion> groupAdjacentChunks(Set<ChunkPos> chunks) {
        if (chunks.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ChunkRegion> regions = new ArrayList<>();
        Set<ChunkPos> unprocessed = new HashSet<>(chunks);
        
        while (!unprocessed.isEmpty()) {
            // 选择一个未处理的区块作为起点
            ChunkPos start = unprocessed.iterator().next();
            List<ChunkPos> connectedChunks = findConnectedChunks(start, unprocessed);
            
            // 从未处理集合中移除已连接的区块
            unprocessed.removeAll(connectedChunks);
            
            // 创建区域
            regions.add(new ChunkRegion(connectedChunks));
        }
        
        return regions;
    }
    
    /**
     * 使用深度优先搜索找到与起始区块相邻的所有区块
     * @param start 起始区块
     * @param available 可用的区块集合
     * @return 连通的区块列表
     */
    private static List<ChunkPos> findConnectedChunks(ChunkPos start, Set<ChunkPos> available) {
        List<ChunkPos> connected = new ArrayList<>();
        Set<ChunkPos> visited = new HashSet<>();
        
        // 深度优先搜索
        dfsConnectedChunks(start, available, visited, connected);
        
        return connected;
    }
    
    /**
     * 深度优先搜索相邻区块
     */
    private static void dfsConnectedChunks(ChunkPos current, Set<ChunkPos> available, 
                                         Set<ChunkPos> visited, List<ChunkPos> connected) {
        if (!available.contains(current) || visited.contains(current)) {
            return;
        }
        
        visited.add(current);
        connected.add(current);
        
        // 检查四个相邻方向
        ChunkPos[] neighbors = {
            new ChunkPos(current.x + 1, current.z),     // 东
            new ChunkPos(current.x - 1, current.z),     // 西
            new ChunkPos(current.x, current.z + 1),     // 南
            new ChunkPos(current.x, current.z - 1)      // 北
        };
        
        for (ChunkPos neighbor : neighbors) {
            dfsConnectedChunks(neighbor, available, visited, connected);
        }
    }
    
    /**
     * 获取分组后的区块区域（按类型）
     * @param level 服务端世界
     * @param loadType 加载类型（true=强加载，false=弱加载）
     * @return 分组后的区域列表
     */
    public List<ChunkRegion> getGroupedChunksByType(ServerLevel level, boolean loadType) {
        Set<ChunkPos> chunks = getLoadedChunksByType(level, loadType);
        return groupAdjacentChunks(chunks);
    }
}
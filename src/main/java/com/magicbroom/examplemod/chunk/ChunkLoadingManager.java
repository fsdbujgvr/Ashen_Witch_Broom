package com.magicbroom.examplemod.chunk;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import com.magicbroom.examplemod.core.AshenWitchBroom;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 区块加载管理器
 * 负责管理扫帚相关的区块强制加载功能
 * 使用弱加载票据系统，只加载单个区块而不进行tick更新
 */
public class ChunkLoadingManager {
    
    // 弱加载票据定义 - 用于扫帚区块加载
    private static final TicketType<ChunkPos> BROOM_WEAK_LOADING_TICKET = 
        TicketType.create("ashenwitchbroom_broom_weak", (a, b) -> Long.compare(a.toLong(), b.toLong()), 0);
    
    // 存储加载的区块 <维度ID, <区块位置, 是否已加载>>
    private static final Map<String, Map<ChunkPos, Boolean>> loadedChunks = 
        new ConcurrentHashMap<>();
    
    // 异步执行器，用于区块状态检测
    private static final ScheduledExecutorService chunkCheckExecutor = 
        Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "ChunkLoadingManager-AsyncChecker");
            t.setDaemon(true);
            return t;
        });
    
    // 单例模式
    private static ChunkLoadingManager instance;
    
    public static ChunkLoadingManager getInstance() {
        if (instance == null) {
            instance = new ChunkLoadingManager();
        }
        return instance;
    }
    
    /**
     * 添加弱加载区块（用于扫帚位置）
     * @param level 服务端世界
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 是否成功添加
     */
    public boolean addWeakLoadedChunk(ServerLevel level, int chunkX, int chunkZ) {
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        String dimensionKey = level.dimension().location().toString();
        
        try {
            // 使用自定义票据系统，半径0确保只加载单个区块
            level.getChunkSource().addRegionTicket(BROOM_WEAK_LOADING_TICKET, chunkPos, 0, chunkPos);
            
            loadedChunks.computeIfAbsent(dimensionKey, k -> new ConcurrentHashMap<>())
                       .put(chunkPos, true); // true = 已加载
            
            AshenWitchBroom.WRAPPED_LOGGER.debug("为扫帚 {} 在维度 {} 的区块 ({}, {}) 添加加载票据",
                chunkX, chunkZ, dimensionKey);
            return true;
        } catch (Exception e) {
            AshenWitchBroom.WRAPPED_LOGGER.error("添加弱加载区块失败：({}, {}) 在维度 {}：{}",
                    chunkX, chunkZ, dimensionKey, e.getMessage());
            return false;
        }
    }
    
    /**
     * 移除弱加载区块
     * @param level 服务端世界
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 是否成功移除
     */
    public boolean removeWeakLoadedChunk(ServerLevel level, int chunkX, int chunkZ) {
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        String dimensionKey = level.dimension().location().toString();
        
        Map<ChunkPos, Boolean> dimensionChunks = loadedChunks.get(dimensionKey);
        if (dimensionChunks == null || !dimensionChunks.containsKey(chunkPos)) {
            return false;
        }
        
        try {
            level.getChunkSource().removeRegionTicket(BROOM_WEAK_LOADING_TICKET, chunkPos, 0, chunkPos);
            
            dimensionChunks.remove(chunkPos);
            if (dimensionChunks.isEmpty()) {
                loadedChunks.remove(dimensionKey);
            }
            
            AshenWitchBroom.WRAPPED_LOGGER.debug("为扫帚 {} 在维度 {} 的区块 ({}, {}) 移除加载票据",
                chunkX, chunkZ, dimensionKey);
            return true;
        } catch (Exception e) {
            AshenWitchBroom.WRAPPED_LOGGER.error("移除弱加载区块失败：({}, {}) 在维度 {}：{}",
                    chunkX, chunkZ, dimensionKey, e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查区块是否被此管理器加载
     * @param level 服务端世界
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 是否已加载
     */
    public boolean isChunkLoadedByManager(ServerLevel level, int chunkX, int chunkZ) {
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        String dimensionKey = level.dimension().location().toString();
        
        Map<ChunkPos, Boolean> dimensionChunks = loadedChunks.get(dimensionKey);
        return dimensionChunks != null && dimensionChunks.containsKey(chunkPos);
    }
    
    /**
     * 获取指定维度中所有已加载的区块
     * @param level 服务端世界
     * @return 已加载的区块位置列表
     */
    public Map<ChunkPos, Boolean> getLoadedChunksInDimension(ServerLevel level) {
        String dimensionKey = level.dimension().location().toString();
        return loadedChunks.getOrDefault(dimensionKey, new ConcurrentHashMap<>());
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
            // 注释掉：使用getChunk方法检测区块是否处于FULL状态
            // load参数设为false，避免强制加载区块，只检测当前状态
            // ChunkAccess chunk = level.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
            // if (chunk == null) {
            //     return false;
            // }
            
            // 注释掉：检查区块是否为LevelChunk（完全加载的区块）并且可以安全访问实体
            // if (!(chunk instanceof LevelChunk levelChunk)) {
            //     AshenWitchBroom.LOGGER.debug("区块 ({}, {}) 在维度 {} 不是LevelChunk类型", 
            //         chunkX, chunkZ, level.dimension().location().toString());
            //     return false;
            // }
            
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
     * 添加弱加载区块并异步等待加载完成
     * @param level 服务端世界
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @param onChunkLoaded 区块加载完成后的回调函数
     * @param timeoutSeconds 超时时间（秒），默认10秒
     * @return CompletableFuture，可用于取消或等待完成
     */
    public CompletableFuture<Boolean> addWeakLoadedChunkAsync(ServerLevel level, int chunkX, int chunkZ, 
                                                             Consumer<Boolean> onChunkLoaded, int timeoutSeconds) {
        // 首先添加弱加载票据
        boolean addResult = addWeakLoadedChunk(level, chunkX, chunkZ);
        if (!addResult) {
            AshenWitchBroom.WRAPPED_LOGGER.error("添加弱加载区块失败: ({}, {}) 在维度 {}", 
                chunkX, chunkZ, level.dimension().location().toString());
            onChunkLoaded.accept(false);
            return CompletableFuture.completedFuture(false);
        }
        
        // 然后异步等待区块加载完成
        return waitForChunkLoadedAsync(level, chunkX, chunkZ, onChunkLoaded, timeoutSeconds);
    }
    
    /**
     * 添加弱加载区块并异步等待加载完成（使用默认10秒超时）
     * @param level 服务端世界
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @param onChunkLoaded 区块加载完成后的回调函数
     * @return CompletableFuture，可用于取消或等待完成
     */
    public CompletableFuture<Boolean> addWeakLoadedChunkAsync(ServerLevel level, int chunkX, int chunkZ, 
                                                             Consumer<Boolean> onChunkLoaded) {
        return addWeakLoadedChunkAsync(level, chunkX, chunkZ, onChunkLoaded, 10);
    }
    
    /**
     * 清理所有已加载的区块（服务器关闭时调用）
     */
    public void clearAllLoadedChunks() {
        // 计算清理前的总票据数量
        int totalTickets = 0;
        for (Map<ChunkPos, Boolean> chunks : loadedChunks.values()) {
            totalTickets += chunks.size();
        }
        
        loadedChunks.clear();
        AshenWitchBroom.WRAPPED_LOGGER.debug("清理所有扫帚的区块加载票据，共清理 {} 个票据", totalTickets);
    }
    
    /**
     * 关闭异步执行器（服务器关闭时调用）
     */
    public void shutdown() {
        chunkCheckExecutor.shutdown();
        try {
            if (!chunkCheckExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                chunkCheckExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            chunkCheckExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        AshenWitchBroom.WRAPPED_LOGGER.debug("ChunkLoadingManager执行器关闭完成");
    }
    
    /**
     * 获取所有已加载的区块数据
     * @return 所有维度的区块数据
     */
    public Map<String, Map<ChunkPos, Boolean>> getAllLoadedChunks() {
        return new ConcurrentHashMap<>(loadedChunks);
    }
}
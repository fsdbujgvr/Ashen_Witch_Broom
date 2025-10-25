package com.magicbroom.examplemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import com.magicbroom.examplemod.core.AshenWitchBroom;
import com.magicbroom.examplemod.core.Config;
import com.magicbroom.examplemod.data.BroomDataManager;
import com.magicbroom.examplemod.data.BroomData;
import com.magicbroom.examplemod.entity.MajoBroomEntity;
import com.magicbroom.examplemod.service.BroomSummonService;
import com.magicbroom.examplemod.chunk.ChunkLoadingManager;
import com.magicbroom.examplemod.chunk.BroomChunkTicketManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 扫帚管理命令类
 * 提供 /broom 命令及其子命令功能
 */
public class BroomCommand {
    
    /**
     * 注册命令
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("broom")
                .then(Commands.literal("detect")
                    .executes(BroomCommand::executeDetect)
                    .then(Commands.argument("player", EntityArgument.player())
                        .requires(source -> source.hasPermission(2)) // 检测其他玩家需要OP权限
                        .executes(BroomCommand::executeDetectPlayer)))
                .then(Commands.literal("stats")
                    .executes(BroomCommand::executeStats)
                    .then(Commands.argument("player", EntityArgument.player())
                        .requires(source -> source.hasPermission(2)) // 查看其他玩家stats需要OP权限
                        .executes(BroomCommand::executeStatsPlayer)))
                .then(Commands.literal("cleanup")
                    .executes(BroomCommand::executeCleanup)
                    .then(Commands.argument("player", EntityArgument.player())
                        .requires(source -> source.hasPermission(2)) // 清理其他玩家数据需要OP权限
                        .executes(BroomCommand::executeCleanupPlayer)))
                .then(Commands.literal("chunks")
                    .requires(source -> source.hasPermission(2)) // 需要管理员权限
                    .then(Commands.literal("add")
                        .then(Commands.literal("lazy")
                            .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                    .executes(BroomCommand::executeChunksAddLazy)
                                    .then(Commands.argument("w", IntegerArgumentType.integer())
                                        .then(Commands.argument("h", IntegerArgumentType.integer())
                                            .executes(BroomCommand::executeChunksAddLazy))))))
                        .then(Commands.literal("ticking")
                            .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                    .executes(BroomCommand::executeChunksAddTicking)
                                    .then(Commands.argument("w", IntegerArgumentType.integer())
                                        .then(Commands.argument("h", IntegerArgumentType.integer())
                                            .executes(BroomCommand::executeChunksAddTicking)))))))
                    .then(Commands.literal("del")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                            .then(Commands.argument("z", IntegerArgumentType.integer())
                                .executes(BroomCommand::executeChunksDel)
                                .then(Commands.argument("w", IntegerArgumentType.integer())
                                    .then(Commands.argument("h", IntegerArgumentType.integer())
                                        .executes(BroomCommand::executeChunksDel))))))
                    .then(Commands.literal("list")
                        .executes(BroomCommand::executeChunksList))
                    .then(Commands.literal("help")
                        .executes(BroomCommand::executeChunksHelp)))
                .then(Commands.literal("help")
                    .executes(BroomCommand::executeHelp))
                .executes(BroomCommand::executeHelp) // 默认显示帮助
        );
    }
    
    /**
     * 执行 /broom detect 命令
     */
    private static int executeDetect(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (source.getEntity() instanceof ServerPlayer player) {
            return executeDetectForPlayer(source, player);
        } else {
            source.sendFailure(Component.translatable("command.ashenwitchbroom.broom.detect.player_only"));
            return 0;
        }
    }
    
    /**
     * 执行 /broom detect <player> 命令
     */
    private static int executeDetectPlayer(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
            return executeDetectForPlayer(source, targetPlayer);
        } catch (Exception e) {
            source.sendFailure(Component.translatable("commands.broom.detect.player_not_found"));
            return 0;
        }
    }
    
    /**
     * 为指定玩家执行检测命令
     */
    private static int executeDetectForPlayer(CommandSourceStack source, ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        UUID playerUUID = player.getUUID();
        
        // 加载玩家的扫帚数据
        List<BroomData> brooms = BroomDataManager.loadPlayerBrooms(level, playerUUID);
        
        if (brooms.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.result_title"), false);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.player_info", player.getDisplayName().getString()), false);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.no_brooms_status"), false);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
            return 1;
        }
        
        // 创建检测结果收集器
        List<BroomDetectionResult> detectionResults = new ArrayList<>();
        AtomicInteger completedDetections = new AtomicInteger(0);
        
        // 为每个扫帚创建检测结果对象
        for (int i = 0; i < brooms.size(); i++) {
            BroomData broom = brooms.get(i);
            BroomDetectionResult result = new BroomDetectionResult(i + 1, broom, level, player);
            detectionResults.add(result);
        }
        
        // 异步检测所有扫帚状态
        for (BroomDetectionResult result : detectionResults) {
            detectBroomStatus(result, player, () -> {
                int completed = completedDetections.incrementAndGet();
                if (completed == brooms.size()) {
                    // 所有检测完成，统一输出结果
                    outputDetectionResults(source, player, detectionResults);
                }
            });
        }
        
        return 1;
    }
    
    /**
     * 扫帚检测结果数据类
     */
    private static class BroomDetectionResult {
        public final int index;
        public final BroomData broomData;
        public final ServerLevel level;
        public final ServerPlayer player;
        public String statusKey = "command.ashenwitchbroom.broom.detect.status_checking";
        
        public BroomDetectionResult(int index, BroomData broomData, ServerLevel level, ServerPlayer player) {
            this.index = index;
            this.broomData = broomData;
            this.level = level;
            this.player = player;
        }
    }
    
    /**
     * 异步检测单个扫帚的状态
     */
    private static void detectBroomStatus(BroomDetectionResult result, ServerPlayer player, Runnable onComplete) {
        UUID entityUUID = result.broomData.getEntityUUID();
        
        if (entityUUID == null) {
            result.statusKey = "command.ashenwitchbroom.broom.detect.status_no_uuid";
            onComplete.run();
            return;
        }
        
        ServerLevel targetLevel = player.getServer().getLevel(result.broomData.getDimension());
        if (targetLevel == null) {
            result.statusKey = "command.ashenwitchbroom.broom.detect.status_invalid_dimension";
            onComplete.run();
            return;
        }
        
        // 使用异步弱加载方式加载包含扫帚的区块
        BlockPos broomPos = result.broomData.getPosition();
        ChunkPos broomChunk = new ChunkPos(broomPos);
        
        // 记录坐标转换日志
        AshenWitchBroom.WRAPPED_LOGGER.debug("BroomCommand(detect) - 扫帚检测坐标转换: 原始坐标 BlockPos({}, {}, {}) -> 区块坐标 ChunkPos({}, {})", 
             broomPos.getX(), broomPos.getY(), broomPos.getZ(), broomChunk.x, broomChunk.z);
        
        // 异步加载区块并等待完成
        ChunkLoadingManager.getInstance().addWeakLoadedChunkAsync(targetLevel, broomChunk.x, broomChunk.z, 
            chunkLoaded -> {
                try {
                    if (chunkLoaded) {
                        AshenWitchBroom.WRAPPED_LOGGER.debug("BroomCommand(detect) - 区块 ({}, {}) 在维度 {} 已成功加载",
                            broomChunk.x, broomChunk.z, targetLevel.dimension().location());
                        
                        // 现在检查实体是否存在
                        Entity entity = targetLevel.getEntity(entityUUID);
                        if (entity instanceof MajoBroomEntity broomEntity) {
                            // 验证扫帚名称和拥有者匹配
                            if (result.broomData.getBroomName().equals(broomEntity.getBroomName()) && 
                                player.getUUID().equals(broomEntity.getOwnerUUID())) {
                                result.statusKey = "command.ashenwitchbroom.broom.detect.status_valid";
                            } else {
                                result.statusKey = "command.ashenwitchbroom.broom.detect.status_mismatch";
                            }
                        } else {
                            result.statusKey = "command.ashenwitchbroom.broom.detect.status_invalid_entity";
                        }
                    } else {
                        AshenWitchBroom.WRAPPED_LOGGER.warn("BroomCommand(detect) - 区块 ({}, {}) 在维度 {} 加载超时", 
                             broomChunk.x, broomChunk.z, targetLevel.dimension().location());
                        result.statusKey = "command.ashenwitchbroom.broom.detect.status_chunk_timeout";
                    }
                } finally {
                    // 检测完成后立即清理加载的区块
                    ChunkLoadingManager.getInstance().removeWeakLoadedChunk(targetLevel, broomChunk.x, broomChunk.z);
                    AshenWitchBroom.WRAPPED_LOGGER.debug("BroomCommand(detect) - 已清理检测用的弱加载区块 ({}, {}) 在维度 {}", 
                         broomChunk.x, broomChunk.z, targetLevel.dimension().location());
                    onComplete.run();
                }
            });
    }
    
    /**
     * 统一输出所有检测结果
     */
    private static void outputDetectionResults(CommandSourceStack source, ServerPlayer player, List<BroomDetectionResult> results) {
        // 发送美观的标题
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.result_title"), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
        ChatFormatting broomCountColor = results.size() > 10 ? ChatFormatting.GREEN : results.size() > 5 ? ChatFormatting.YELLOW : ChatFormatting.WHITE;
        Component broomCountComponent = Component.literal(String.valueOf(results.size())).withStyle(broomCountColor);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.player_count", player.getDisplayName().getString(), broomCountComponent), false);
        source.sendSuccess(() -> Component.literal(""), false);
        
        // 显示每个扫帚的详细信息
        for (int i = 0; i < results.size(); i++) {
            BroomDetectionResult result = results.get(i);
            BroomData broom = result.broomData;
            
            // 扫帚基本信息 - 使用更美观的格式
            Component indexComponent = Component.literal(String.valueOf(result.index)).withStyle(ChatFormatting.YELLOW);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.broom_index", indexComponent, broom.getBroomName()), false);
            
            // 位置信息 - 优化显示
            BlockPos pos = broom.getPosition();
            String dimensionName = broom.getDimension().location().toString();
            String formattedDimension = formatDimensionName(dimensionName);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.position", formattedDimension, pos.getX(), pos.getY(), pos.getZ()), false);
            
            // UUID信息 - 完整显示
            UUID entityUUID = broom.getEntityUUID();
            if (entityUUID != null) {
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.entity_uuid", entityUUID.toString()), false);
            } else {
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.entity_missing"), false);
            }
            
            // 状态信息 - 显示检测结果
            source.sendSuccess(() -> Component.translatable(result.statusKey), false);
            
            // 距离信息 - 美化显示
            if (broom.getDimension().equals(result.level.dimension())) {
                double distance = Math.sqrt(broom.getDistanceSquared(player.blockPosition(), result.level.dimension()));
                ChatFormatting distanceColor = distance <= 50 ? ChatFormatting.GREEN : distance <= 200 ? ChatFormatting.YELLOW : ChatFormatting.RED;
                String formattedDistance = String.format("%.2f", distance);
                Component distanceComponent = Component.literal(formattedDistance).withStyle(distanceColor);
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.distance", distanceComponent), false);
            } else {
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.distance_cross_dimension"), false);
            }
            
            if (i < results.size() - 1) {
                source.sendSuccess(() -> Component.literal(""), false); // 空行分隔
            }
        }
        
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
    }
    
    /**
     * 执行 /broom stats 命令
     */
    private static int executeStats(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (source.getEntity() instanceof ServerPlayer player) {
            return executeStatsForPlayer(source, player);
        } else {
            source.sendFailure(Component.translatable("command.ashenwitchbroom.broom.stats.player_only"));
            return 0;
        }
    }
    
    /**
     * 执行 /broom stats <player> 命令
     */
    private static int executeStatsPlayer(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
            return executeStatsForPlayer(source, targetPlayer);
        } catch (Exception e) {
            source.sendFailure(Component.translatable("commands.broom.stats.player_not_found"));
            return 0;
        }
    }
    
    /**
     * 为指定玩家执行统计命令
     */
    private static int executeStatsForPlayer(CommandSourceStack source, ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<BroomData> brooms = BroomDataManager.loadPlayerBrooms(level, player.getUUID());
        
        final int storedBrooms = brooms.size();
        int inventoryBrooms = 0;
        
        // 统计背包中的扫帚
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() == AshenWitchBroom.MAJO_BROOM_ITEM.get()) {
                inventoryBrooms += itemStack.getCount();
            }
        }
        
        final int finalInventoryBrooms = inventoryBrooms;
        final int totalBrooms = storedBrooms + inventoryBrooms;
        final ResourceKey<Level> currentDimension = player.level().dimension();
        
        final long currentDimensionBrooms = brooms.stream()
            .filter(broom -> broom.getDimension().equals(currentDimension))
            .count();
        
        final int nearbyRange = Config.NEARBY_SEARCH_RANGE.get();
        final BlockPos playerPos = player.blockPosition();
        final long nearbyBrooms = brooms.stream()
            .filter(broom -> broom.getDimension().equals(currentDimension))
            .filter(broom -> broom.getDistanceSquared(playerPos, currentDimension) <= nearbyRange * nearbyRange)
            .count();
        
        // 发送美观的统计信息标题
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.stats.title"), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.player_info", player.getDisplayName().getString()), false);
        source.sendSuccess(() -> Component.literal(""), false);
        
        // 扫帚数量统计 - 使用图标和颜色
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.stats.count_section"), false);
        ChatFormatting totalColor = totalBrooms > 10 ? ChatFormatting.GREEN : totalBrooms > 5 ? ChatFormatting.YELLOW : ChatFormatting.WHITE;
        Component totalComponent = Component.literal(String.valueOf(totalBrooms)).withStyle(totalColor);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.stats.total", totalComponent), false);
        
        ChatFormatting inventoryColor = finalInventoryBrooms > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY;
        Component inventoryComponent = Component.literal(String.valueOf(finalInventoryBrooms)).withStyle(inventoryColor);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.stats.inventory", inventoryComponent), false);
        
        ChatFormatting nearbyColor = nearbyBrooms > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY;
        Component nearbyComponent = Component.literal(String.valueOf(nearbyBrooms)).withStyle(nearbyColor);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.stats.nearby", nearbyRange, nearbyComponent), false);
        
        ChatFormatting currentWorldColor = currentDimensionBrooms > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY;
        String dimensionName = formatDimensionName(currentDimension.location().toString());
        Component currentDimensionComponent = Component.literal(String.valueOf(currentDimensionBrooms)).withStyle(currentWorldColor);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.stats.current_dimension", dimensionName, currentDimensionComponent), false);
        
        ChatFormatting storedColor = storedBrooms > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY;
        Component storedComponent = Component.literal(String.valueOf(storedBrooms)).withStyle(storedColor);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.stats.stored", storedComponent), false);
        
        source.sendSuccess(() -> Component.literal(""), false);
        
        // 召唤功能状态 - 使用更直观的显示
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.stats.summon_section"), false);
        final Component nearbyStatus = Config.ENABLE_NEARBY_SUMMON.get() ? Component.translatable("command.ashenwitchbroom.broom.stats.enabled") : Component.translatable("command.ashenwitchbroom.broom.stats.disabled");
         final Component worldStatus = Config.ENABLE_WORLD_SUMMON.get() ? Component.translatable("command.ashenwitchbroom.broom.stats.enabled") : Component.translatable("command.ashenwitchbroom.broom.stats.disabled");
         final Component crossDimensionStatus = Config.ENABLE_CROSS_DIMENSION_SUMMON.get() ? Component.translatable("command.ashenwitchbroom.broom.stats.enabled") : Component.translatable("command.ashenwitchbroom.broom.stats.disabled");
         final Component inventoryStatus = Config.ENABLE_INVENTORY_SUMMON.get() ? Component.translatable("command.ashenwitchbroom.broom.stats.enabled") : Component.translatable("command.ashenwitchbroom.broom.stats.disabled");
        
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.stats.nearby_summon", nearbyStatus.getString()), false);
         source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.stats.world_summon", worldStatus.getString()), false);
         source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.stats.cross_dimension_summon", crossDimensionStatus.getString()), false);
         source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.stats.inventory_summon", inventoryStatus.getString()), false);
        
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
        
        return 1;
    }
    
    /**
     * 执行 /broom cleanup 命令
     */
    private static int executeCleanup(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (source.getEntity() instanceof ServerPlayer player) {
            return executeCleanupForPlayer(source, player);
        } else {
            source.sendFailure(Component.translatable("command.ashenwitchbroom.broom.cleanup.player_only"));
            return 0;
        }
    }
    
    /**
     * 执行 /broom cleanup <player> 命令
     */
    private static int executeCleanupPlayer(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
            return executeCleanupForPlayer(source, targetPlayer);
        } catch (Exception e) {
            source.sendFailure(Component.translatable("commands.broom.cleanup.player_not_found"));
            return 0;
        }
    }
    
    /**
     * 为指定玩家执行清理命令
     */
    private static int executeCleanupForPlayer(CommandSourceStack source, ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        UUID playerUUID = player.getUUID();
        
        List<BroomData> brooms = BroomDataManager.loadPlayerBrooms(level, playerUUID);
        
        if (brooms.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.result_title"), false);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.player_info", player.getDisplayName().getString()), false);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.no_brooms_status"), false);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
            return 1;
        }
        
        // 显示美观的开始提示
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.progress_title"), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
        ChatFormatting broomCountColor = brooms.size() > 10 ? ChatFormatting.GREEN : brooms.size() > 5 ? ChatFormatting.YELLOW : ChatFormatting.WHITE;
        Component broomCountComponent = Component.literal(String.valueOf(brooms.size())).withStyle(broomCountColor);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.player_broom_count", player.getDisplayName().getString(), broomCountComponent), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.start_verify"), false);
        
        // 创建清理结果收集器
        List<BroomCleanupResult> cleanupResults = new ArrayList<>();
        AtomicInteger completedVerifications = new AtomicInteger(0);
        
        // 为每个扫帚创建清理结果对象
        for (int i = 0; i < brooms.size(); i++) {
            BroomData broom = brooms.get(i);
            BroomCleanupResult result = new BroomCleanupResult(i + 1, broom);
            cleanupResults.add(result);
        }
        
        // 异步验证所有扫帚状态
        for (BroomCleanupResult result : cleanupResults) {
            verifyBroomForCleanup(result, player, () -> {
                int completed = completedVerifications.incrementAndGet();
                if (completed == brooms.size()) {
                    // 所有验证完成，统一处理结果
                    processCleanupResults(source, player, cleanupResults);
                }
            });
        }
        
        return 1;
    }
    
    /**
     * 扫帚清理结果数据类
     */
    private static class BroomCleanupResult {
        public final int index;
        public final BroomData broomData;
        public boolean isValid = false;
        public boolean verificationComplete = false;
        
        public BroomCleanupResult(int index, BroomData broomData) {
            this.index = index;
            this.broomData = broomData;
        }
    }
    
    /**
     * 异步验证单个扫帚用于清理
     */
    private static void verifyBroomForCleanup(BroomCleanupResult result, ServerPlayer player, Runnable onComplete) {
        UUID entityUUID = result.broomData.getEntityUUID();
        
        if (entityUUID == null) {
            result.isValid = false;
            result.verificationComplete = true;
            onComplete.run();
            return;
        }
        
        ServerLevel targetLevel = player.getServer().getLevel(result.broomData.getDimension());
        if (targetLevel == null) {
            result.isValid = false;
            result.verificationComplete = true;
            onComplete.run();
            return;
        }
        
        // 使用异步弱加载方式加载包含扫帚的区块
        BlockPos broomPos = result.broomData.getPosition();
        ChunkPos broomChunk = new ChunkPos(broomPos);
        
        // 记录坐标转换日志
        AshenWitchBroom.WRAPPED_LOGGER.debug("BroomCommand(cleanup) - 扫帚验证坐标转换: 原始坐标 BlockPos({}, {}, {}) -> 区块坐标 ChunkPos({}, {})", 
            broomPos.getX(), broomPos.getY(), broomPos.getZ(), broomChunk.x, broomChunk.z);
        
        // 异步加载区块并等待完成
        ChunkLoadingManager.getInstance().addWeakLoadedChunkAsync(targetLevel, broomChunk.x, broomChunk.z, 
            chunkLoaded -> {
                try {
                    if (chunkLoaded) {
                        AshenWitchBroom.WRAPPED_LOGGER.debug("BroomCommand(cleanup) - 区块 ({}, {}) 在维度 {} 已成功加载", 
                            broomChunk.x, broomChunk.z, targetLevel.dimension().location());
                        
                        // 现在检查实体是否存在
                        Entity entity = targetLevel.getEntity(entityUUID);
                        if (entity instanceof MajoBroomEntity broomEntity) {
                            // 验证扫帚名称和拥有者匹配
                            if (result.broomData.getBroomName().equals(broomEntity.getBroomName()) && 
                                player.getUUID().equals(broomEntity.getOwnerUUID())) {
                                result.isValid = true;
                            } else {
                                result.isValid = false;
                            }
                        } else {
                            result.isValid = false;
                        }
                    } else {
                        AshenWitchBroom.WRAPPED_LOGGER.warn("BroomCommand(cleanup) - 区块 ({}, {}) 在维度 {} 加载超时", 
                            broomChunk.x, broomChunk.z, targetLevel.dimension().location());
                        result.isValid = false;
                    }
                } finally {
                    // 验证完成后立即清理加载的区块
                    ChunkLoadingManager.getInstance().removeWeakLoadedChunk(targetLevel, broomChunk.x, broomChunk.z);
                    AshenWitchBroom.WRAPPED_LOGGER.debug("BroomCommand(cleanup) - 已清理验证用的弱加载区块 ({}, {}) 在维度 {}", 
                        broomChunk.x, broomChunk.z, targetLevel.dimension().location());
                    result.verificationComplete = true;
                    onComplete.run();
                }
            });
    }
    
    /**
     * 处理所有清理结果
     */
    private static void processCleanupResults(CommandSourceStack source, ServerPlayer player, List<BroomCleanupResult> results) {
        ServerLevel level = player.serverLevel();
        UUID playerUUID = player.getUUID();
        
        int removedCount = 0;
        List<String> removedBrooms = new ArrayList<>();
        
        // 处理每个验证结果
        for (BroomCleanupResult result : results) {
            if (!result.isValid) {
                // 扫帚无效，删除记录
                boolean removed = removeBroomByUUID(level, playerUUID, result.broomData);
                if (removed) {
                    removedCount++;
                    removedBrooms.add(result.broomData.getBroomName());
                    source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.invalid_removed", result.broomData.getBroomName()), false);
                }
            } else {
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.valid", result.broomData.getBroomName()), false);
            }
        }
        
        // 显示美观的结果统计
        int totalCount = results.size();
        int remaining = totalCount - removedCount;
        
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.result_section"), false);
        ChatFormatting totalColor = totalCount > 10 ? ChatFormatting.GREEN : totalCount > 5 ? ChatFormatting.YELLOW : ChatFormatting.WHITE;
        Component totalComponent = Component.literal(String.valueOf(totalCount)).withStyle(totalColor);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.total_brooms", totalComponent), false);
        
        ChatFormatting validColor = remaining > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY;
        Component validComponent = Component.literal(String.valueOf(remaining)).withStyle(validColor);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.valid_brooms", validComponent), false);
        
        ChatFormatting removedColor = removedCount > 0 ? ChatFormatting.RED : ChatFormatting.GRAY;
        Component removedComponent = Component.literal(String.valueOf(removedCount)).withStyle(removedColor);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.cleaned_brooms", removedComponent), false);
        
        source.sendSuccess(() -> Component.literal(""), false);
        if (removedCount > 0) {
            Component removedCountComponent = Component.literal(String.valueOf(removedCount)).withStyle(ChatFormatting.RED);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.success_with_removed", removedCountComponent), false);
        } else {
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.success_all_valid"), false);
        }
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
    }
    
    /**
     * 通过UUID验证扫帚是否存在 - 使用异步区块加载
     */
    private static CompletableFuture<Boolean> verifyBroomExistsByUUID(ServerPlayer player, BroomData broomData) {
        if (broomData.getEntityUUID() == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        try {
            ServerLevel targetLevel = player.getServer().getLevel(broomData.getDimension());
            if (targetLevel == null) {
                return CompletableFuture.completedFuture(false);
            }
            
            // 使用异步弱加载方式加载区块
            BlockPos broomPos = broomData.getPosition();
            ChunkPos broomChunk = new ChunkPos(broomPos);
            
            // 记录坐标转换日志
            AshenWitchBroom.WRAPPED_LOGGER.debug("BroomCommand - 扫帚验证坐标转换: 原始坐标 BlockPos({}, {}, {}) -> 区块坐标 ChunkPos({}, {})", 
                broomPos.getX(), broomPos.getY(), broomPos.getZ(), broomChunk.x, broomChunk.z);
            
            // 异步加载区块并等待完成
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            ChunkLoadingManager.getInstance().addWeakLoadedChunkAsync(targetLevel, broomChunk.x, broomChunk.z, 
                chunkLoaded -> {
                    try {
                        if (chunkLoaded) {
                            AshenWitchBroom.WRAPPED_LOGGER.debug("BroomCommand - 区块 ({}, {}) 在维度 {} 已成功加载用于验证", 
                                broomChunk.x, broomChunk.z, targetLevel.dimension().location());
                            
                            // 通过UUID查找实体
                            Entity entity = targetLevel.getEntity(broomData.getEntityUUID());
                            if (entity instanceof MajoBroomEntity broomEntity) {
                                // 验证扫帚名称和拥有者匹配
                                boolean result = broomData.getBroomName().equals(broomEntity.getBroomName()) && 
                                               player.getUUID().equals(broomEntity.getOwnerUUID());
                                future.complete(result);
                            } else {
                                future.complete(false);
                            }
                        } else {
                            AshenWitchBroom.WRAPPED_LOGGER.warn("BroomCommand - 区块 ({}, {}) 在维度 {} 加载超时，验证失败", 
                                broomChunk.x, broomChunk.z, targetLevel.dimension().location());
                            future.complete(false);
                        }
                    } finally {
                        // 无论验证成功失败都清理区块
                        ChunkLoadingManager.getInstance().removeWeakLoadedChunk(targetLevel, broomChunk.x, broomChunk.z);
                        AshenWitchBroom.WRAPPED_LOGGER.debug("已清理临时弱加载区块 ({}, {}) 在维度 {} - verifyBroomExistsByUUID", 
                            broomChunk.x, broomChunk.z, targetLevel.dimension().location());
                    }
                });
            return future;
        } catch (Exception e) {
            AshenWitchBroom.WRAPPED_LOGGER.warn("验证扫帚存在性时发生错误：{}", e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
    
    /**
     * 通过UUID精确删除扫帚记录
     */
    private static boolean removeBroomByUUID(ServerLevel level, UUID playerUUID, BroomData broomToRemove) {
        List<BroomData> brooms = BroomDataManager.loadPlayerBrooms(level, playerUUID);
        boolean removed = brooms.removeIf(broom -> 
            broom.getBroomName().equals(broomToRemove.getBroomName()) && 
            broom.getEntityUUID() != null && 
            broom.getEntityUUID().equals(broomToRemove.getEntityUUID())
        );
        
        if (removed) {
            BroomDataManager.savePlayerBrooms(level, playerUUID, brooms);
            AshenWitchBroom.WRAPPED_LOGGER.debug("为玩家 {} 移除了扫帚 '{}' (UUID: {})", 
                playerUUID, broomToRemove.getBroomName(), broomToRemove.getEntityUUID());
        }
        
        return removed;
    }
    
    /**
     * 执行 /broom help 命令
     */
    private static int executeHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        boolean isOp = source.hasPermission(2);
        
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.help.title"), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
        source.sendSuccess(() -> Component.literal(""), false);
        
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.help.player_commands"), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.help.detect_self"), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.help.stats_self"), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.help.cleanup_self"), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.help.help_command"), false);
        
        if (isOp) {
            source.sendSuccess(() -> Component.literal(""), false);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.help.admin_commands"), false);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.help.detect_player"), false);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.help.stats_player"), false);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.help.cleanup_player"), false);
        }
        
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.help.usage_tips"), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.help.tip_self_manage"), false);
        if (!isOp) {
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.help.tip_op_required"), false);
        }
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.help.tip_cleanup"), false);
        
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
        
        return 1;
    }
    
    /**
     * 格式化维度名称，更好地显示第三方模组维度
     */
    private static String formatDimensionName(String dimensionName) {
        if (dimensionName == null || dimensionName.isEmpty()) {
            return "unknown";
        }
        
        // 处理原版维度
        if (dimensionName.equals("minecraft:overworld")) {
            return "overworld";
        } else if (dimensionName.equals("minecraft:the_nether")) {
            return "the nether";
        } else if (dimensionName.equals("minecraft:the_end")) {
            return "the end";
        }
        
        // 处理第三方模组维度
        // 如果是minecraft命名空间，移除前缀并格式化
        if (dimensionName.startsWith("minecraft:")) {
            return dimensionName.substring(10).replace("_", " ");
        }
        
        // 对于第三方模组维度，保留完整名称但进行美化
        // 例如：modname:dimension_name -> modname: dimension name
        int colonIndex = dimensionName.indexOf(':');
        if (colonIndex > 0 && colonIndex < dimensionName.length() - 1) {
            String namespace = dimensionName.substring(0, colonIndex);
            String path = dimensionName.substring(colonIndex + 1);
            return namespace + ": " + path.replace("_", " ");
        }
        
        // 如果没有命名空间，直接格式化
        return dimensionName.replace("_", " ");
    }

    // ==================== 区块管理相关方法 ====================

    /**
     * 执行 /broom chunks add lazy 指令
     */
    private static int executeChunksAddLazy(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            
            // 获取参数
            int x = IntegerArgumentType.getInteger(context, "x");
            int z = IntegerArgumentType.getInteger(context, "z");
            
            // 检查是否有宽度和高度参数
            int[] args;
            try {
                int w = IntegerArgumentType.getInteger(context, "w");
                int h = IntegerArgumentType.getInteger(context, "h");
                args = new int[]{x, z, w, h};
            } catch (IllegalArgumentException e) {
                // 没有w和h参数，使用单个区块
                args = new int[]{x, z};
            }

            // 获取当前维度
            ServerLevel level = source.getLevel();
            String dimensionName = level.dimension().location().toString();

            // 解析坐标
            List<ChunkPos> chunks = BroomChunkTicketManager.parseCoordinateArgs(args);
            if (chunks.isEmpty()) {
                source.sendFailure(Component.translatable("command.ashenwitchbroom.chunks.add.lazy.invalid_coordinates")
                    .withStyle(ChatFormatting.RED));
                return 0;
            }

            // 批量添加弱加载区块
            BroomChunkTicketManager.BatchOperationResult result = 
                BroomChunkTicketManager.getInstance().addLazyLoadedChunksBatch(level, chunks);

            if (result.getSuccessCount() > 0) {
                if (result.getSuccessCount() == 1) {
                    ChunkPos chunk = result.getSuccessChunks().get(0);
                    source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.add.lazy.success", 
                        chunk.x, chunk.z, dimensionName).withStyle(ChatFormatting.GREEN), true);
                } else {
                    source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.add.lazy.batch_success", 
                        result.getSuccessCount(), dimensionName).withStyle(ChatFormatting.GREEN), true);
                }
                AshenWitchBroom.WRAPPED_LOGGER.debug("管理员 {} 在维度 {} 添加了 {} 个弱加载区块", 
                    source.getTextName(), dimensionName, result.getSuccessCount());
            }

            // 处理已存在相同类型的区块
            if (!result.getAlreadyExistsSameType().isEmpty()) {
                if (result.getAlreadyExistsSameType().size() == 1) {
                    ChunkPos chunk = result.getAlreadyExistsSameType().get(0);
                    source.sendFailure(Component.translatable("command.ashenwitchbroom.chunks.add.already_exists_same_type", 
                        chunk.x, chunk.z, "弱加载").withStyle(ChatFormatting.YELLOW));
                } else {
                    source.sendFailure(Component.translatable("command.ashenwitchbroom.chunks.add.already_exists_same_type_batch", 
                        result.getAlreadyExistsSameType().size(), "弱加载").withStyle(ChatFormatting.YELLOW));
                }
            }

            // 处理已存在不同类型的区块
            if (!result.getAlreadyExistsDifferentType().isEmpty()) {
                if (result.getAlreadyExistsDifferentType().size() == 1) {
                    ChunkPos chunk = result.getAlreadyExistsDifferentType().get(0);
                    source.sendFailure(Component.translatable("command.ashenwitchbroom.chunks.add.already_exists_different_type", 
                        chunk.x, chunk.z, "强加载", "弱加载").withStyle(ChatFormatting.RED));
                } else {
                    source.sendFailure(Component.translatable("command.ashenwitchbroom.chunks.add.already_exists_different_type_batch", 
                        result.getAlreadyExistsDifferentType().size(), "强加载", "弱加载").withStyle(ChatFormatting.RED));
                }
            }

            if (result.getFailureCount() > 0) {
                source.sendFailure(Component.translatable("command.ashenwitchbroom.chunks.add.lazy.batch_failed", 
                    result.getFailureCount()).withStyle(ChatFormatting.RED));
            }

            return result.getSuccessCount() > 0 ? 1 : 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.translatable("command.ashenwitchbroom.chunks.add.lazy.error", e.getMessage())
                .withStyle(ChatFormatting.RED));
            AshenWitchBroom.WRAPPED_LOGGER.error("执行添加弱加载区块指令时发生错误", e);
            return 0;
        }
    }

    /**
     * 执行 /broom chunks add ticking 指令
     */
    private static int executeChunksAddTicking(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            
            // 获取参数
            int x = IntegerArgumentType.getInteger(context, "x");
            int z = IntegerArgumentType.getInteger(context, "z");
            
            // 检查是否有宽度和高度参数
            int[] args;
            try {
                int w = IntegerArgumentType.getInteger(context, "w");
                int h = IntegerArgumentType.getInteger(context, "h");
                args = new int[]{x, z, w, h};
            } catch (IllegalArgumentException e) {
                // 没有w和h参数，使用单个区块
                args = new int[]{x, z};
            }

            // 获取当前维度
            ServerLevel level = source.getLevel();
            String dimensionName = level.dimension().location().toString();

            // 解析坐标
            List<ChunkPos> chunks = BroomChunkTicketManager.parseCoordinateArgs(args);
            if (chunks.isEmpty()) {
                source.sendFailure(Component.translatable("command.ashenwitchbroom.chunks.add.ticking.invalid_coordinates")
                    .withStyle(ChatFormatting.RED));
                return 0;
            }

            // 批量添加强加载区块
            BroomChunkTicketManager.BatchOperationResult result = 
                BroomChunkTicketManager.getInstance().addTickingLoadedChunksBatch(level, chunks);

            if (result.getSuccessCount() > 0) {
                if (result.getSuccessCount() == 1) {
                    ChunkPos chunk = result.getSuccessChunks().get(0);
                    source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.add.ticking.success", 
                        chunk.x, chunk.z, dimensionName).withStyle(ChatFormatting.GREEN), true);
                } else {
                    source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.add.ticking.batch_success", 
                        result.getSuccessCount(), dimensionName).withStyle(ChatFormatting.GREEN), true);
                }
                AshenWitchBroom.WRAPPED_LOGGER.debug("管理员 {} 在维度 {} 添加了 {} 个强加载区块", 
                    source.getTextName(), dimensionName, result.getSuccessCount());
            }

            // 处理已存在相同类型的区块
            if (!result.getAlreadyExistsSameType().isEmpty()) {
                if (result.getAlreadyExistsSameType().size() == 1) {
                    ChunkPos chunk = result.getAlreadyExistsSameType().get(0);
                    source.sendFailure(Component.translatable("command.ashenwitchbroom.chunks.add.already_exists_same_type", 
                        chunk.x, chunk.z, "强加载").withStyle(ChatFormatting.YELLOW));
                } else {
                    source.sendFailure(Component.translatable("command.ashenwitchbroom.chunks.add.already_exists_same_type_batch", 
                        result.getAlreadyExistsSameType().size(), "强加载").withStyle(ChatFormatting.YELLOW));
                }
            }

            // 处理已存在不同类型的区块
            if (!result.getAlreadyExistsDifferentType().isEmpty()) {
                if (result.getAlreadyExistsDifferentType().size() == 1) {
                    ChunkPos chunk = result.getAlreadyExistsDifferentType().get(0);
                    source.sendFailure(Component.translatable("command.ashenwitchbroom.chunks.add.already_exists_different_type", 
                        chunk.x, chunk.z, "弱加载", "强加载").withStyle(ChatFormatting.RED));
                } else {
                    source.sendFailure(Component.translatable("command.ashenwitchbroom.chunks.add.already_exists_different_type_batch", 
                        result.getAlreadyExistsDifferentType().size(), "弱加载", "强加载").withStyle(ChatFormatting.RED));
                }
            }

            if (result.getFailureCount() > 0) {
                source.sendFailure(Component.translatable("command.ashenwitchbroom.chunks.add.ticking.batch_failed", 
                    result.getFailureCount()).withStyle(ChatFormatting.RED));
            }

            return result.getSuccessCount() > 0 ? 1 : 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.translatable("command.ashenwitchbroom.chunks.add.ticking.error", e.getMessage())
                .withStyle(ChatFormatting.RED));
            AshenWitchBroom.WRAPPED_LOGGER.error("执行添加强加载区块指令时发生错误", e);
            return 0;
        }
    }

    /**
     * 执行 /broom chunks del 指令（统一删除，不区分类型）
     */
    private static int executeChunksDel(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            
            // 获取参数
            int x = IntegerArgumentType.getInteger(context, "x");
            int z = IntegerArgumentType.getInteger(context, "z");
            
            // 检查是否有宽度和高度参数
            int[] args;
            try {
                int w = IntegerArgumentType.getInteger(context, "w");
                int h = IntegerArgumentType.getInteger(context, "h");
                args = new int[]{x, z, w, h};
            } catch (IllegalArgumentException e) {
                // 没有w和h参数，使用单个区块
                args = new int[]{x, z};
            }

            // 获取当前维度
            ServerLevel level = source.getLevel();
            String dimensionName = level.dimension().location().toString();

            // 解析坐标
            List<ChunkPos> chunks = BroomChunkTicketManager.parseCoordinateArgs(args);
            if (chunks.isEmpty()) {
                source.sendFailure(Component.translatable("command.ashenwitchbroom.chunks.del.invalid_coordinates")
                    .withStyle(ChatFormatting.RED));
                return 0;
            }

            // 批量删除区块（自动检测类型）
            BroomChunkTicketManager.BatchOperationResult result = 
                BroomChunkTicketManager.getInstance().removeLoadedChunksBatch(level, chunks, null);

            if (result.getSuccessCount() > 0) {
                if (result.getSuccessCount() == 1) {
                    ChunkPos chunk = result.getSuccessChunks().get(0);
                    source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.del.success", 
                        chunk.x, chunk.z, dimensionName).withStyle(ChatFormatting.GREEN), true);
                } else {
                    source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.del.batch_success", 
                        result.getSuccessCount(), dimensionName).withStyle(ChatFormatting.GREEN), true);
                }
                AshenWitchBroom.WRAPPED_LOGGER.debug("管理员 {} 在维度 {} 删除了 {} 个区块", 
                    source.getTextName(), dimensionName, result.getSuccessCount());
            }

            if (result.getFailureCount() > 0) {
                source.sendFailure(Component.translatable("command.ashenwitchbroom.chunks.del.batch_failed", 
                    result.getFailureCount()).withStyle(ChatFormatting.RED));
            }

            return result.getSuccessCount() > 0 ? 1 : 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.translatable("command.ashenwitchbroom.chunks.del.error", e.getMessage())
                .withStyle(ChatFormatting.RED));
            AshenWitchBroom.WRAPPED_LOGGER.error("执行删除区块指令时发生错误", e);
            return 0;
        }
    }




    /**
     * 执行 /broom chunks list 指令
     */
    private static int executeChunksList(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            
            // 获取当前维度
            ServerLevel level = source.getLevel();
            String dimensionName = level.dimension().location().toString();

            // 获取统计信息
            int[] stats = BroomChunkTicketManager.getInstance().getStatistics(level);
            int total = stats[0];
            int strongCount = stats[1];
            int weakCount = stats[2];

            if (total == 0) {
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.list.empty", dimensionName)
                    .withStyle(ChatFormatting.YELLOW), false);
                return 1;
            }

            // 构建消息
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.list.title", dimensionName)
                .withStyle(ChatFormatting.GREEN), false);

            // 显示强加载区块（分组显示）
            List<BroomChunkTicketManager.ChunkRegion> strongRegions = 
                BroomChunkTicketManager.getInstance().getGroupedChunksByType(level, true);
            if (!strongRegions.isEmpty()) {
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.list.ticking_header")
                    .withStyle(ChatFormatting.AQUA), false);
                for (BroomChunkTicketManager.ChunkRegion region : strongRegions) {
                    if (region.getChunkCount() == 1) {
                        // 单个区块使用原有的显示方式
                        ChunkPos chunk = region.getChunks().get(0);
                        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.list.ticking_entry", chunk.x, chunk.z)
                            .withStyle(ChatFormatting.GRAY), false);
                    } else {
                        // 多个区块显示为区域
                        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.list.ticking_region", 
                            region.getMinX(), region.getMinZ(), region.getMaxX(), region.getMaxZ())
                            .withStyle(ChatFormatting.GRAY), false);
                    }
                }
            }

            // 显示弱加载区块（分组显示）
            List<BroomChunkTicketManager.ChunkRegion> weakRegions = 
                BroomChunkTicketManager.getInstance().getGroupedChunksByType(level, false);
            if (!weakRegions.isEmpty()) {
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.list.lazy_header")
                    .withStyle(ChatFormatting.YELLOW), false);
                for (BroomChunkTicketManager.ChunkRegion region : weakRegions) {
                    if (region.getChunkCount() == 1) {
                        // 单个区块使用原有的显示方式
                        ChunkPos chunk = region.getChunks().get(0);
                        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.list.lazy_entry", chunk.x, chunk.z)
                            .withStyle(ChatFormatting.GRAY), false);
                    } else {
                        // 多个区块显示为区域
                        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.list.lazy_region", 
                            region.getMinX(), region.getMinZ(), region.getMaxX(), region.getMaxZ())
                            .withStyle(ChatFormatting.GRAY), false);
                    }
                }
            }

            // 显示统计信息
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.list.total", total, strongCount, weakCount)
                .withStyle(ChatFormatting.GREEN), false);

            AshenWitchBroom.WRAPPED_LOGGER.debug("管理员 {} 查看了维度 {} 的区块列表，共 {} 个区块", 
                source.getTextName(), dimensionName, total);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.translatable("command.ashenwitchbroom.chunks.list.error", e.getMessage())
                .withStyle(ChatFormatting.RED));
            AshenWitchBroom.WRAPPED_LOGGER.error("执行列表区块指令时发生错误", e);
            return 0;
        }
    }

    /**
     * 执行 /broom chunks help 指令
     */
    private static int executeChunksHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.help.title")
            .withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.help.add_lazy")
            .withStyle(ChatFormatting.GREEN), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.help.add_ticking")
            .withStyle(ChatFormatting.GREEN), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.help.del")
            .withStyle(ChatFormatting.GREEN), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.help.list")
            .withStyle(ChatFormatting.GREEN), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.help.help")
            .withStyle(ChatFormatting.GREEN), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.help.note")
            .withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.help.coordinate_note")
            .withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.chunks.help.batch_note")
            .withStyle(ChatFormatting.YELLOW), false);

        return 1;
    }
}
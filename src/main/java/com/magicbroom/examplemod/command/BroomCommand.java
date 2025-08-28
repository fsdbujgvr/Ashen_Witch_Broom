package com.magicbroom.examplemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
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
        
        // 发送美观的标题
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.result_title"), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
        ChatFormatting broomCountColor = brooms.size() > 10 ? ChatFormatting.GREEN : brooms.size() > 5 ? ChatFormatting.YELLOW : ChatFormatting.WHITE;
        Component broomCountComponent = Component.literal(String.valueOf(brooms.size())).withStyle(broomCountColor);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.player_count", player.getDisplayName().getString(), broomCountComponent), false);
        source.sendSuccess(() -> Component.literal(""), false);
        
        // 显示每个扫帚的详细信息
        for (int i = 0; i < brooms.size(); i++) {
            BroomData broom = brooms.get(i);
            int index = i + 1;
            
            // 扫帚基本信息 - 使用更美观的格式
            Component indexComponent = Component.literal(String.valueOf(index)).withStyle(ChatFormatting.YELLOW);
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.broom_index", indexComponent, broom.getBroomName()), false);
            
            // 位置信息 - 优化显示
            BlockPos pos = broom.getPosition();
            String dimensionName = broom.getDimension().location().toString();
            String formattedDimension = dimensionName.replace("minecraft:", "").replace("_", " ");
            source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.position", formattedDimension, pos.getX(), pos.getY(), pos.getZ()), false);
            
            // UUID信息 - 完整显示
            UUID entityUUID = broom.getEntityUUID();
            if (entityUUID != null) {
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.entity_uuid", entityUUID.toString()), false);
                
                // 检查实体是否存在 - 使用更直观的状态显示
                ServerLevel targetLevel = player.getServer().getLevel(broom.getDimension());
                if (targetLevel != null) {
                    Entity entity = targetLevel.getEntity(entityUUID);
                    if (entity instanceof MajoBroomEntity broomEntity) {
                        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.status_valid"), false);
                    } else {
                        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.status_invalid_entity"), false);
                    }
                } else {
                    source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.status_invalid_dimension"), false);
                }
            } else {
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.entity_missing"), false);
                 source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.status_no_uuid"), false);
            }
            
            // 距离信息 - 美化显示
            if (broom.getDimension().equals(level.dimension())) {
                double distance = Math.sqrt(broom.getDistanceSquared(player.blockPosition(), level.dimension()));
                ChatFormatting distanceColor = distance <= 50 ? ChatFormatting.GREEN : distance <= 200 ? ChatFormatting.YELLOW : ChatFormatting.RED;
                String formattedDistance = String.format("%.2f", distance);
                Component distanceComponent = Component.literal(formattedDistance).withStyle(distanceColor);
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.distance", distanceComponent), false);
            } else {
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.distance_cross_dimension"), false);
            }
            
            if (i < brooms.size() - 1) {
                source.sendSuccess(() -> Component.literal(""), false); // 空行分隔
            }
        }
        
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
        return 1;
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
        String dimensionName = currentDimension.location().toString().replace("minecraft:", "").replace("_", " ");
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
        
        final AtomicInteger removedCount = new AtomicInteger(0);
        final AtomicInteger totalCount = new AtomicInteger(brooms.size());
        final List<String> removedBrooms = new ArrayList<>();
        
        // 异步验证所有扫帚，使用UUID进行精确验证
        CompletableFuture<Void> cleanupFuture = CompletableFuture.completedFuture(null);
        
        for (BroomData broom : brooms) {
            cleanupFuture = cleanupFuture.thenCompose(v -> 
                verifyBroomExistsByUUID(player, broom)
                    .thenAccept(exists -> {
                        if (!exists) {
                            // 扫帚不存在，删除记录（使用UUID精确删除）
                            boolean removed = removeBroomByUUID(level, playerUUID, broom);
                            if (removed) {
                                removedCount.incrementAndGet();
                                removedBrooms.add(broom.getBroomName());
                                
                                level.getServer().execute(() -> {
                                    source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.invalid_removed", broom.getBroomName()), false);
                                });
                            }
                        } else {
                            level.getServer().execute(() -> {
                                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.valid", broom.getBroomName()), false);
                            });
                        }
                    })
            );
        }
        
        // 完成后显示美观的结果
        cleanupFuture.thenRun(() -> {
            level.getServer().execute(() -> {
                int removed = removedCount.get();
                int remaining = totalCount.get() - removed;
                
                source.sendSuccess(() -> Component.literal(""), false);
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.result_section"), false);
                ChatFormatting totalColor = totalCount.get() > 10 ? ChatFormatting.GREEN : totalCount.get() > 5 ? ChatFormatting.YELLOW : ChatFormatting.WHITE;
                Component totalComponent = Component.literal(String.valueOf(totalCount.get())).withStyle(totalColor);
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.total_brooms", totalComponent), false);
                
                ChatFormatting validColor = remaining > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY;
                Component validComponent = Component.literal(String.valueOf(remaining)).withStyle(validColor);
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.valid_brooms", validComponent), false);
                
                ChatFormatting removedColor = removed > 0 ? ChatFormatting.RED : ChatFormatting.GRAY;
                Component removedComponent = Component.literal(String.valueOf(removed)).withStyle(removedColor);
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.cleaned_brooms", removedComponent), false);
                
                source.sendSuccess(() -> Component.literal(""), false);
                if (removed > 0) {
                    Component removedCountComponent = Component.literal(String.valueOf(removed)).withStyle(ChatFormatting.RED);
                    source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.success_with_removed", removedCountComponent), false);
                } else {
                    source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.cleanup.success_all_valid"), false);
                }
                source.sendSuccess(() -> Component.literal(""), false);
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
            });
        }).exceptionally(throwable -> {
            level.getServer().execute(() -> {
                source.sendSuccess(() -> Component.literal(""), false);
                source.sendFailure(Component.translatable("command.ashenwitchbroom.broom.cleanup.error", throwable.getMessage()));
                source.sendSuccess(() -> Component.literal(""), false);
                source.sendSuccess(() -> Component.translatable("command.ashenwitchbroom.broom.detect.separator"), false);
            });
            return null;
        });
        
        return 1;
    }
    
    /**
     * 通过UUID验证扫帚是否存在
     */
    private static CompletableFuture<Boolean> verifyBroomExistsByUUID(ServerPlayer player, BroomData broomData) {
        if (broomData.getEntityUUID() == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                ServerLevel targetLevel = player.getServer().getLevel(broomData.getDimension());
                if (targetLevel == null) {
                    return false;
                }
                
                // 加载区块
                BlockPos broomPos = broomData.getPosition();
                targetLevel.getChunk(broomPos);
                
                // 通过UUID查找实体
                Entity entity = targetLevel.getEntity(broomData.getEntityUUID());
                if (entity instanceof MajoBroomEntity broomEntity) {
                    // 验证扫帚名称和拥有者匹配
                    return broomData.getBroomName().equals(broomEntity.getBroomName()) && 
                           player.getUUID().equals(broomEntity.getOwnerUUID());
                }
                
                return false;
            } catch (Exception e) {
                AshenWitchBroom.LOGGER.warn("Error verifying broom existence: {}", e.getMessage());
                return false;
            }
        });
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
            AshenWitchBroom.LOGGER.info("Removed broom '{}' with UUID {} for player {}", 
                broomToRemove.getBroomName(), broomToRemove.getEntityUUID(), playerUUID);
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
}
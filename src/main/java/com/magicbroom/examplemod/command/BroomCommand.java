package com.magicbroom.examplemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
            source.sendFailure(Component.translatable("commands.broom.detect.player_only"));
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
            source.sendSuccess(() -> Component.literal("§6✦ §e扫帚检测结果 §6✦"), false);
            source.sendSuccess(() -> Component.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
            source.sendSuccess(() -> Component.literal("§f玩家: §b" + player.getDisplayName().getString()), false);
            source.sendSuccess(() -> Component.literal("§f状态: §c未发现任何扫帚记录"), false);
            source.sendSuccess(() -> Component.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
            return 1;
        }
        
        // 发送美观的标题
        source.sendSuccess(() -> Component.literal("§6✦ §e扫帚检测结果 §6✦"), false);
        source.sendSuccess(() -> Component.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
        source.sendSuccess(() -> Component.literal("§f玩家: §b" + player.getDisplayName().getString() + " §7| §f扫帚数量: §a" + brooms.size()), false);
        source.sendSuccess(() -> Component.literal(""), false);
        
        // 显示每个扫帚的详细信息
        for (int i = 0; i < brooms.size(); i++) {
            BroomData broom = brooms.get(i);
            int index = i + 1;
            
            // 扫帚基本信息 - 使用更美观的格式
            source.sendSuccess(() -> Component.literal("§6▶ §e第" + index + "把扫帚: §f" + broom.getBroomName()), false);
            
            // 位置信息 - 优化显示
            BlockPos pos = broom.getPosition();
            String dimensionName = broom.getDimension().location().toString();
            String formattedDimension = dimensionName.replace("minecraft:", "").replace("_", " ");
            source.sendSuccess(() -> Component.literal("  §7┣ §b位置: §f" + formattedDimension + " §7(§f" + pos.getX() + "§7, §f" + pos.getY() + "§7, §f" + pos.getZ() + "§7)"), false);
            
            // UUID信息 - 完整显示
            UUID entityUUID = broom.getEntityUUID();
            if (entityUUID != null) {
                source.sendSuccess(() -> Component.literal("  §7┣ §b实体UUID: §f" + entityUUID.toString()), false);
                
                // 检查实体是否存在 - 使用更直观的状态显示
                ServerLevel targetLevel = player.getServer().getLevel(broom.getDimension());
                if (targetLevel != null) {
                    Entity entity = targetLevel.getEntity(entityUUID);
                    if (entity instanceof MajoBroomEntity broomEntity) {
                        source.sendSuccess(() -> Component.literal("  §7┣ §b状态: §a✓ 有效 §7(实体存在)"), false);
                    } else {
                        source.sendSuccess(() -> Component.literal("  §7┣ §b状态: §c✗ 无效 §7(实体丢失)"), false);
                    }
                } else {
                    source.sendSuccess(() -> Component.literal("  §7┣ §b状态: §c✗ 无效 §7(维度不存在)"), false);
                }
            } else {
                source.sendSuccess(() -> Component.literal("  §7┣ §b实体ID: §c缺失"), false);
                source.sendSuccess(() -> Component.literal("  §7┣ §b状态: §c✗ 无效 §7(缺少UUID)"), false);
            }
            
            // 距离信息 - 美化显示
            if (broom.getDimension().equals(level.dimension())) {
                double distance = Math.sqrt(broom.getDistanceSquared(player.blockPosition(), level.dimension()));
                String distanceColor = distance <= 50 ? "§a" : distance <= 200 ? "§e" : "§c";
                source.sendSuccess(() -> Component.literal("  §7┗ §b距离: " + distanceColor + String.format("%.1f", distance) + " §7格"), false);
            } else {
                source.sendSuccess(() -> Component.literal("  §7┗ §b距离: §d跨维度"), false);
            }
            
            if (i < brooms.size() - 1) {
                source.sendSuccess(() -> Component.literal(""), false); // 空行分隔
            }
        }
        
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
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
            source.sendFailure(Component.translatable("commands.broom.stats.player_only"));
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
        source.sendSuccess(() -> Component.literal("§6✦ §e扫帚统计信息 §6✦"), false);
        source.sendSuccess(() -> Component.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
        source.sendSuccess(() -> Component.literal("§f玩家: §b" + player.getDisplayName().getString()), false);
        source.sendSuccess(() -> Component.literal(""), false);
        
        // 扫帚数量统计 - 使用图标和颜色
        source.sendSuccess(() -> Component.literal("§6▶ §e扫帚数量统计"), false);
        String totalColor = totalBrooms > 10 ? "§a" : totalBrooms > 5 ? "§e" : "§f";
        source.sendSuccess(() -> Component.literal("  §7┣ §b总计: " + totalColor + totalBrooms + " §7把"), false);
        
        String inventoryColor = finalInventoryBrooms > 0 ? "§a" : "§7";
        source.sendSuccess(() -> Component.literal("  §7┣ §b背包中: " + inventoryColor + finalInventoryBrooms + " §7把"), false);
        
        String nearbyColor = nearbyBrooms > 0 ? "§a" : "§7";
        source.sendSuccess(() -> Component.literal("  §7┣ §b附近(" + nearbyRange + "格内): " + nearbyColor + nearbyBrooms + " §7把"), false);
        
        String currentWorldColor = currentDimensionBrooms > 0 ? "§a" : "§7";
        String dimensionName = currentDimension.location().toString().replace("minecraft:", "").replace("_", " ");
        source.sendSuccess(() -> Component.literal("  §7┣ §b当前维度(" + dimensionName + "): " + currentWorldColor + currentDimensionBrooms + " §7把"), false);
        
        String storedColor = storedBrooms > 0 ? "§a" : "§7";
        source.sendSuccess(() -> Component.literal("  §7┗ §b已存储: " + storedColor + storedBrooms + " §7把"), false);
        
        source.sendSuccess(() -> Component.literal(""), false);
        
        // 召唤功能状态 - 使用更直观的显示
        source.sendSuccess(() -> Component.literal("§6▶ §e召唤功能状态"), false);
        final String nearbyStatus = Config.ENABLE_NEARBY_SUMMON.get() ? "§a✓ 启用" : "§c✗ 禁用";
        final String worldStatus = Config.ENABLE_WORLD_SUMMON.get() ? "§a✓ 启用" : "§c✗ 禁用";
        final String crossDimensionStatus = Config.ENABLE_CROSS_DIMENSION_SUMMON.get() ? "§a✓ 启用" : "§c✗ 禁用";
        final String inventoryStatus = Config.ENABLE_INVENTORY_SUMMON.get() ? "§a✓ 启用" : "§c✗ 禁用";
        
        source.sendSuccess(() -> Component.literal("  §7┣ §b附近召唤: " + nearbyStatus), false);
        source.sendSuccess(() -> Component.literal("  §7┣ §b同维度召唤: " + worldStatus), false);
        source.sendSuccess(() -> Component.literal("  §7┣ §b跨维度召唤: " + crossDimensionStatus), false);
        source.sendSuccess(() -> Component.literal("  §7┗ §b背包召唤: " + inventoryStatus), false);
        
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
        
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
            source.sendFailure(Component.translatable("commands.broom.cleanup.player_only"));
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
            source.sendSuccess(() -> Component.literal("§6✦ §e扫帚清理结果 §6✦"), false);
            source.sendSuccess(() -> Component.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
            source.sendSuccess(() -> Component.literal("§f玩家: §b" + player.getDisplayName().getString()), false);
            source.sendSuccess(() -> Component.literal("§f状态: §c未发现任何扫帚记录"), false);
            source.sendSuccess(() -> Component.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
            return 1;
        }
        
        // 显示美观的开始提示
        source.sendSuccess(() -> Component.literal("§6✦ §e扫帚清理进度 §6✦"), false);
        source.sendSuccess(() -> Component.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
        source.sendSuccess(() -> Component.literal("§f玩家: §b" + player.getDisplayName().getString() + " §7| §f扫帚数量: §e" + brooms.size()), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§6▶ §e开始验证扫帚有效性..."), false);
        
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
                                    source.sendSuccess(() -> Component.literal("  §7┣ §c✗ 无效: §f" + broom.getBroomName() + " §7(已移除)"), false);
                                });
                            }
                        } else {
                            level.getServer().execute(() -> {
                                source.sendSuccess(() -> Component.literal("  §7┣ §a✓ 有效: §f" + broom.getBroomName()), false);
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
                source.sendSuccess(() -> Component.literal("§6▶ §e清理结果统计"), false);
                source.sendSuccess(() -> Component.literal("  §7┣ §b总计扫帚: §f" + totalCount.get() + " §7把"), false);
                source.sendSuccess(() -> Component.literal("  §7┣ §b有效扫帚: §a" + remaining + " §7把"), false);
                source.sendSuccess(() -> Component.literal("  §7┗ §b已清理: §c" + removed + " §7把无效记录"), false);
                
                source.sendSuccess(() -> Component.literal(""), false);
                if (removed > 0) {
                    source.sendSuccess(() -> Component.literal("§a✓ 清理完成！成功移除 §c" + removed + " §a个无效扫帚记录"), false);
                } else {
                    source.sendSuccess(() -> Component.literal("§a✓ 清理完成！所有扫帚记录都是有效的"), false);
                }
                source.sendSuccess(() -> Component.literal(""), false);
                source.sendSuccess(() -> Component.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
            });
        }).exceptionally(throwable -> {
            level.getServer().execute(() -> {
                source.sendSuccess(() -> Component.literal(""), false);
                source.sendFailure(Component.literal("§c✗ 清理过程中发生错误: " + throwable.getMessage()));
                source.sendSuccess(() -> Component.literal(""), false);
                source.sendSuccess(() -> Component.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
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
        
        source.sendSuccess(() -> Component.literal("§6✦ §e扫帚命令帮助 §6✦"), false);
        source.sendSuccess(() -> Component.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
        source.sendSuccess(() -> Component.literal(""), false);
        
        source.sendSuccess(() -> Component.literal("§6▶ §e普通玩家可用命令"), false);
        source.sendSuccess(() -> Component.literal("  §7┣ §b/broom detect §7- §f检测自己的所有扫帚详细信息"), false);
        source.sendSuccess(() -> Component.literal("  §7┣ §b/broom stats §7- §f查看自己的扫帚统计信息"), false);
        source.sendSuccess(() -> Component.literal("  §7┣ §b/broom cleanup §7- §f清理自己的无效扫帚记录"), false);
        source.sendSuccess(() -> Component.literal("  §7┗ §b/broom help §7- §f显示此帮助信息"), false);
        
        if (isOp) {
            source.sendSuccess(() -> Component.literal(""), false);
            source.sendSuccess(() -> Component.literal("§6▶ §e管理员专用命令"), false);
            source.sendSuccess(() -> Component.literal("  §7┣ §b/broom detect <player> §7- §f检测指定玩家的扫帚"), false);
            source.sendSuccess(() -> Component.literal("  §7┣ §b/broom stats <player> §7- §f查看指定玩家的统计"), false);
            source.sendSuccess(() -> Component.literal("  §7┗ §b/broom cleanup <player> §7- §f清理指定玩家的数据"), false);
        }
        
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§6▶ §e使用提示"), false);
        source.sendSuccess(() -> Component.literal("  §7┣ §b💡 普通玩家可以自由管理自己的扫帚数据"), false);
        if (!isOp) {
            source.sendSuccess(() -> Component.literal("  §7┣ §b🔒 管理其他玩家的数据需要OP权限"), false);
        }
        source.sendSuccess(() -> Component.literal("  §7┗ §b📋 使用 §e/broom cleanup §b定期清理无效记录"), false);
        
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
        
        return 1;
    }
}
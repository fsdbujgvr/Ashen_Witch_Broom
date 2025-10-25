package com.magicbroom.examplemod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;

import com.magicbroom.examplemod.core.AshenWitchBroom;
import com.magicbroom.examplemod.entity.MajoBroomEntity;

/**
 * 扫帚上下马网络包集合
 * 包含客户端请求包和服务端响应包
 */
public class BroomMountPackets {
    
    /**
     * 客户端发送的上下马请求包
     */
    public record BroomMountRequestPack(int entityId, boolean mount) implements CustomPacketPayload {
        
        public static final Type<BroomMountRequestPack> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AshenWitchBroom.MODID, "broom_mount_request"));
        
        public static final StreamCodec<RegistryFriendlyByteBuf, BroomMountRequestPack> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.VAR_INT, BroomMountRequestPack::entityId,
            net.minecraft.network.codec.ByteBufCodecs.BOOL, BroomMountRequestPack::mount,
            BroomMountRequestPack::new
        );
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
        
        /**
         * 服务端处理上下马请求
         */
        public static void handleServer(BroomMountRequestPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                ServerPlayer player = (ServerPlayer) context.player();
                ServerLevel level = player.serverLevel();
                
                AshenWitchBroom.WRAPPED_LOGGER.debug("服务端收到玩家 {} 的{}请求，实体ID: {}", 
                    player.getName().getString(), 
                    packet.mount ? "上马" : "下马", 
                    packet.entityId);
                
                Entity entity = level.getEntity(packet.entityId);
                if (!(entity instanceof MajoBroomEntity broomEntity)) {
                    AshenWitchBroom.WRAPPED_LOGGER.warn("服务端验证失败：实体ID {} 不是扫帚实体", packet.entityId);
                    return;
                }
                
                boolean success = false;
                
                if (packet.mount) {
                    // 检查扫帚所有权并发送聊天提醒（仅在上马时）
                    if (broomEntity.getOwnerUUID() != null && !broomEntity.getOwnerUUID().equals(player.getUUID())) {
                        // 获取扫帚拥有者信息
                        String ownerName = "未知玩家";
                        ServerPlayer ownerPlayer = player.getServer().getPlayerList().getPlayer(broomEntity.getOwnerUUID());
                        if (ownerPlayer != null) {
                            ownerName = ownerPlayer.getName().getString();
                        }
                        
                        // 发送聊天提醒
                        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                            "message.ashenwitchbroom.using_others_broom", ownerName, broomEntity.getBroomName()));
                        
                        AshenWitchBroom.WRAPPED_LOGGER.debug("玩家 {} 正在使用 {} 的扫帚 '{}'", 
                            player.getName().getString(), ownerName, broomEntity.getBroomName());
                    }
                    // 上马请求
                    if (!broomEntity.isVehicle() && !player.isPassenger()) {
                        player.startRiding(broomEntity);
                        success = true;
                        AshenWitchBroom.WRAPPED_LOGGER.debug("服务端执行上马成功：玩家 {} 骑乘扫帚 {}", 
                            player.getName().getString(), broomEntity.getBroomName());
                    } else {
                        AshenWitchBroom.WRAPPED_LOGGER.debug("服务端上马失败：扫帚已有乘客或玩家已在骑乘状态");
                    }
                } else {
                    // 下马请求
                    if (player.getVehicle() == broomEntity) {
                        player.stopRiding();
                        success = true;
                        // 下扫帚时立即同步更新位置数据
                        if (broomEntity.getBroomName() != null && broomEntity.getOwnerUUID() != null) {
                            broomEntity.updateBroomPositionSync();
                        }
                        // 关键修复：向刚刚下马的玩家立即发送一次实体传送包，刷新扫帚位置
                        ClientboundTeleportEntityPacket teleportPacket = new ClientboundTeleportEntityPacket(broomEntity);
                        player.connection.send(teleportPacket);
                        AshenWitchBroom.WRAPPED_LOGGER.debug("服务端执行下马成功并发送Teleport包：玩家 {} -> 扫帚 {}", 
                            player.getName().getString(), broomEntity.getBroomName());
                    } else {
                        AshenWitchBroom.WRAPPED_LOGGER.debug("服务端下马失败：玩家不在该扫帚上");
                    }
                }
                
                // 发送响应包给客户端
                BroomMountResponsePack response = new BroomMountResponsePack(
                    packet.entityId, 
                    packet.mount, 
                    success,
                    broomEntity.getX(),
                    broomEntity.getY(), 
                    broomEntity.getZ(),
                    broomEntity.getYRot(),
                    broomEntity.getXRot()
                );
                context.reply(response);
                
                AshenWitchBroom.WRAPPED_LOGGER.debug("服务端发送{}响应给玩家 {}：{}", 
                    packet.mount ? "上马" : "下马", 
                    player.getName().getString(), 
                    success ? "允许" : "拒绝");
            });
        }
    }
    
    /**
     * 服务端响应上下马请求的网络包
     * 包含扫帚的精确位置信息和旋转方向，用于下马时的完整状态同步
     */
    public record BroomMountResponsePack(int entityId, boolean mount, boolean success, double x, double y, double z, float yaw, float pitch) implements CustomPacketPayload {
        
        public static final Type<BroomMountResponsePack> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AshenWitchBroom.MODID, "broom_mount_response"));
        
        public static final StreamCodec<RegistryFriendlyByteBuf, BroomMountResponsePack> STREAM_CODEC = StreamCodec.ofMember(
            BroomMountResponsePack::encode,
            BroomMountResponsePack::new
        );
        
        // 手动编码方法
        public void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(this.entityId);
            buffer.writeBoolean(this.mount);
            buffer.writeBoolean(this.success);
            buffer.writeDouble(this.x);
            buffer.writeDouble(this.y);
            buffer.writeDouble(this.z);
            buffer.writeFloat(this.yaw);
            buffer.writeFloat(this.pitch);
        }
        
        // 手动解码构造函数
        public BroomMountResponsePack(RegistryFriendlyByteBuf buffer) {
            this(
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readFloat(),
                buffer.readFloat()
            );
        }
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
        
        /**
         * 客户端处理服务端的上下马响应
         */
        public static void handleClient(BroomMountResponsePack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null || mc.level == null) {
                    return;
                }
                
                AshenWitchBroom.WRAPPED_LOGGER.debug("客户端收到服务端{}响应：实体ID {} - {}", 
                    packet.mount ? "上马" : "下马", 
                    packet.entityId, 
                    packet.success ? "允许" : "拒绝");
                
                if (!packet.success) {
                    AshenWitchBroom.WRAPPED_LOGGER.debug("客户端收到服务端拒绝{}请求", packet.mount ? "上马" : "下马");
                    return;
                }
                
                Entity entity = mc.level.getEntity(packet.entityId);
                if (!(entity instanceof MajoBroomEntity broomEntity)) {
                    AshenWitchBroom.WRAPPED_LOGGER.warn("客户端响应处理失败：实体ID {} 不是扫帚实体", packet.entityId);
                    return;
                }
                
                if (packet.mount) {
                    // 服务端允许上马，客户端执行上马
                    if (!broomEntity.isVehicle() && !mc.player.isPassenger()) {
                        mc.player.startRiding(broomEntity);
                        AshenWitchBroom.WRAPPED_LOGGER.debug("客户端执行上马：玩家骑乘扫帚 {}", broomEntity.getBroomName());
                    }
                } else {
                    // 服务端允许下马，客户端执行下马
                    if (mc.player.getVehicle() == broomEntity) {
                        // 在下马前清除扫帚的所有动量，防止滑行
                        broomEntity.setDeltaMovement(0, 0, 0);
                        mc.player.stopRiding();
                        
                        // 强制传送扫帚到服务端指定的精确位置和旋转方向
                        broomEntity.setPos(packet.x, packet.y, packet.z);
                        broomEntity.setYRot(packet.yaw);
                        broomEntity.setXRot(packet.pitch);
                        
                        AshenWitchBroom.WRAPPED_LOGGER.debug("客户端执行下马：玩家从扫帚 {} 下马，位置同步到 ({}, {}, {})，旋转同步到 (yaw: {}, pitch: {})", 
                            broomEntity.getBroomName(), packet.x, packet.y, packet.z, packet.yaw, packet.pitch);
                    }
                }
            });
        }
    }
}
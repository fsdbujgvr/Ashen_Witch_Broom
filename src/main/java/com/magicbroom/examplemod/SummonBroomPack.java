package com.magicbroom.examplemod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 扫帚召唤网络包
 * 重构后的召唤系统，基于文件存储和验证机制
 */
public record SummonBroomPack(double x, double y, double z) implements CustomPacketPayload {
    
    public static final Type<SummonBroomPack> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AshenWitchBroom.MODID, "summon_broom_pack"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, SummonBroomPack> STREAM_CODEC = StreamCodec.composite(
        net.minecraft.network.codec.ByteBufCodecs.DOUBLE, SummonBroomPack::x,
        net.minecraft.network.codec.ByteBufCodecs.DOUBLE, SummonBroomPack::y,
        net.minecraft.network.codec.ByteBufCodecs.DOUBLE, SummonBroomPack::z,
        SummonBroomPack::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    /**
     * 服务端处理召唤请求
     */
    public static void handleServer(SummonBroomPack packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            
            // 使用新的召唤服务处理召唤逻辑
            BroomSummonService.summonBroom(player, packet.x, packet.y, packet.z);
        });
    }
    
}
package com.magicbroom.examplemod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RidePack(int eid, boolean ride) implements CustomPacketPayload {
    
    public static final Type<RidePack> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AshenWitchBroom.MODID, "ride_pack"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, RidePack> STREAM_CODEC = StreamCodec.composite(
        net.minecraft.network.codec.ByteBufCodecs.VAR_INT, RidePack::eid,
        net.minecraft.network.codec.ByteBufCodecs.BOOL, RidePack::ride,
        RidePack::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handleServer(RidePack packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ServerLevel level = player.serverLevel();
            
            Entity entity = level.getEntity(packet.eid);
            if (entity instanceof MajoBroomEntity broomEntity) {
                if (packet.ride) {
                    // 骑乘扫帚
                    if (broomEntity.getPassengers().isEmpty()) {
                        player.startRiding(broomEntity);
                    }
                } else {
                    // 下马
                    if (player.getVehicle() == broomEntity) {
                        player.stopRiding();
                    }
                }
            }
        });
    }
}
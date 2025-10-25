package com.magicbroom.examplemod.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import com.magicbroom.examplemod.core.AshenWitchBroom;

@EventBusSubscriber(modid = AshenWitchBroom.MODID)
public class Networking {
    
    @SubscribeEvent
    public static void registerMessage(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(AshenWitchBroom.MODID);
        
        
        // 注册召唤扫帚包
        registrar.playToServer(SummonBroomPack.TYPE, SummonBroomPack.STREAM_CODEC, SummonBroomPack::handleServer);
        
        // 注册新的上下马请求包（客户端到服务端）
        registrar.playToServer(BroomMountPackets.BroomMountRequestPack.TYPE, BroomMountPackets.BroomMountRequestPack.STREAM_CODEC, BroomMountPackets.BroomMountRequestPack::handleServer);
        
        // 注册扫帚上下马响应包（服务端到客户端）
        registrar.playToClient(BroomMountPackets.BroomMountResponsePack.TYPE, BroomMountPackets.BroomMountResponsePack.STREAM_CODEC, BroomMountPackets.BroomMountResponsePack::handleClient);
    }
}
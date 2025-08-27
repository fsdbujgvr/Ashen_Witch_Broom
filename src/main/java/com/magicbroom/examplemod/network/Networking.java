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
        
        // 注册骑乘控制包
        registrar.playToServer(RidePack.TYPE, RidePack.STREAM_CODEC, RidePack::handleServer);
        
        // 注册召唤扫帚包
        registrar.playToServer(SummonBroomPack.TYPE, SummonBroomPack.STREAM_CODEC, SummonBroomPack::handleServer);
    }
}
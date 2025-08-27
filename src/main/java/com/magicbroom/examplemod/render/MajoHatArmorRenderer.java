package com.magicbroom.examplemod.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import com.magicbroom.examplemod.model.MajoHatModel;

public class MajoHatArmorRenderer implements IClientItemExtensions {
    
    @Override
    public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        if (equipmentSlot == EquipmentSlot.HEAD) {
            MajoHatModel<?> model = new MajoHatModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(MajoHatModel.LAYER_LOCATION));
            // 复制原始模型的属性以确保染色等效果正确应用
            model.crouching = original.crouching;
            model.riding = original.riding;
            model.young = original.young;
            model.leftArmPose = original.leftArmPose;
            model.rightArmPose = original.rightArmPose;
            return model;
        }
        return original;
    }
    

}
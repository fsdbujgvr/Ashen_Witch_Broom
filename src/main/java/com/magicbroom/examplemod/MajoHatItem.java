package com.magicbroom.examplemod;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import java.util.function.Consumer;

public class MajoHatItem extends ArmorItem {
    
    public MajoHatItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }
    
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new MajoHatArmorRenderer());
    }
    
    // 获取物品的颜色，用于染色功能
    public int getColor(ItemStack stack) {
        DyedItemColor dyedItemColor = stack.get(DataComponents.DYED_COLOR);
        return dyedItemColor != null ? dyedItemColor.rgb() : 0xdda3c7; // 默认淡粉紫色
    }
}
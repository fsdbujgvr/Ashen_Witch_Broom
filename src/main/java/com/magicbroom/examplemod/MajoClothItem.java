package com.magicbroom.examplemod;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import java.util.function.Consumer;

public class MajoClothItem extends ArmorItem {
    
    public MajoClothItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }
    
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new MajoClothArmorRenderer());
    }
}
package com.magicbroom.examplemod.data;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import java.util.UUID;

import com.magicbroom.examplemod.core.AshenWitchBroom;

/**
 * 扫帚数据结构类
 * 存储扫帚的基本信息：名称、维度、坐标、实体UUID
 */
public class BroomData {
    private String broomName;
    private ResourceKey<Level> dimension;
    private BlockPos position;
    private UUID entityUUID; // Minecraft实体UUID，用于精确匹配
    
    public BroomData(String broomName, ResourceKey<Level> dimension, BlockPos position) {
        this.broomName = broomName;
        this.dimension = dimension;
        this.position = position;
        this.entityUUID = null; // 初始为null，创建实体后设置
    }
    
    public BroomData(String broomName, ResourceKey<Level> dimension, BlockPos position, UUID entityUUID) {
        this.broomName = broomName;
        this.dimension = dimension;
        this.position = position;
        this.entityUUID = entityUUID;
    }
    
    // 从NBT读取数据
    public static BroomData fromNBT(CompoundTag tag) {
        String broomName = tag.getString("broomName");
        String dimensionStr = tag.getString("dimension");
        BlockPos position = new BlockPos(
            tag.getInt("x"),
            tag.getInt("y"),
            tag.getInt("z")
        );
        
        // 正确处理所有维度，包括第三方模组维度
        ResourceKey<Level> dimension;
        try {
            // 使用ResourceLocation解析维度字符串，支持所有维度
            net.minecraft.resources.ResourceLocation dimensionLocation = 
                net.minecraft.resources.ResourceLocation.parse(dimensionStr);
            dimension = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimensionLocation);
            
            // AshenWitchBroom.WRAPPED_LOGGER.debug("成功解析维度：{} -> {}", dimensionStr, dimension.location());
        } catch (Exception e) {
            // 如果解析失败，记录警告并使用主世界作为默认值
            AshenWitchBroom.WRAPPED_LOGGER.warn("解析维度 '{}' 失败，默认使用主世界：{}", dimensionStr, e.getMessage());
            dimension = Level.OVERWORLD;
        }
        
        // 读取实体UUID（必须存在）
        UUID entityUUID = null;
        if (tag.contains("entityUUID", 8)) { // 8 = String type
            try {
                entityUUID = UUID.fromString(tag.getString("entityUUID"));
            } catch (IllegalArgumentException e) {
                AshenWitchBroom.WRAPPED_LOGGER.warn("扫帚数据中的实体UUID无效：{}", tag.getString("entityUUID"));
            }
        }
        
        if (entityUUID == null) {
            AshenWitchBroom.WRAPPED_LOGGER.warn("扫帚数据缺少必需的实体UUID，此数据将无效");
        }
        
        return new BroomData(broomName, dimension, position, entityUUID);
    }
    
    // 转换为NBT
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("broomName", broomName);
        tag.putString("dimension", dimension.location().toString());
        tag.putInt("x", position.getX());
        tag.putInt("y", position.getY());
        tag.putInt("z", position.getZ());
        
        // 保存实体UUID（如果存在）
        if (entityUUID != null) {
            tag.putString("entityUUID", entityUUID.toString());
        }
        
        return tag;
    }
    
    // 计算与指定位置的距离（平方）
    public double getDistanceSquared(BlockPos playerPos, ResourceKey<Level> playerDimension) {
        // 如果不在同一维度，返回极大值
        if (!this.dimension.equals(playerDimension)) {
            return Double.MAX_VALUE;
        }
        
        double dx = this.position.getX() - playerPos.getX();
        double dy = this.position.getY() - playerPos.getY();
        double dz = this.position.getZ() - playerPos.getZ();
        
        return dx * dx + dy * dy + dz * dz;
    }
    
    // Getters
    public String getBroomName() {
        return broomName;
    }
    
    public ResourceKey<Level> getDimension() {
        return dimension;
    }
    
    public BlockPos getPosition() {
        return position;
    }
    
    public UUID getEntityUUID() {
        return entityUUID;
    }
    
    // Setters
    public void setBroomName(String broomName) {
        this.broomName = broomName;
    }
    
    public void setDimension(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }
    
    public void setPosition(BlockPos position) {
        this.position = position;
    }
    
    public void setEntityUUID(UUID entityUUID) {
        this.entityUUID = entityUUID;
    }
    
    @Override
    public String toString() {
        return String.format("BroomData{name='%s', dimension='%s', pos=%s, entityUUID=%s}", 
            broomName, dimension.location().toString(), position.toString(), entityUUID);
    }
}


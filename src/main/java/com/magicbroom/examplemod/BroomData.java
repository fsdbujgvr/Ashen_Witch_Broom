package com.magicbroom.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import java.util.UUID;

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
        
        ResourceKey<Level> dimension = Level.OVERWORLD; // 默认主世界
        if ("minecraft:the_nether".equals(dimensionStr)) {
            dimension = Level.NETHER;
        } else if ("minecraft:the_end".equals(dimensionStr)) {
            dimension = Level.END;
        } else if ("minecraft:overworld".equals(dimensionStr)) {
            dimension = Level.OVERWORLD;
        }
        
        // 读取实体UUID（必须存在）
        UUID entityUUID = null;
        if (tag.contains("entityUUID", 8)) { // 8 = String type
            try {
                entityUUID = UUID.fromString(tag.getString("entityUUID"));
            } catch (IllegalArgumentException e) {
                AshenWitchBroom.LOGGER.warn("Invalid entityUUID in broom data: {}", tag.getString("entityUUID"));
            }
        }
        
        if (entityUUID == null) {
            AshenWitchBroom.LOGGER.warn("Broom data missing required entityUUID, this data will be invalid");
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


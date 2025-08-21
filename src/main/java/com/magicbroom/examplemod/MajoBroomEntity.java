package com.magicbroom.examplemod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import java.util.UUID;

/**
 * 魔女扫帚实体类
 * 固定生成在方块上方一格，保持静止，可以被潜行左键收回，支持骑乘
 */
public class MajoBroomEntity extends Entity {
    
    // 数据同步器用于同步玩家输入
    private static final EntityDataAccessor<Float> FORWARD_INPUT = 
        SynchedEntityData.defineId(MajoBroomEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STRAFE_INPUT = 
        SynchedEntityData.defineId(MajoBroomEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> JUMPING = 
        SynchedEntityData.defineId(MajoBroomEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHIFT_KEY_DOWN = 
        SynchedEntityData.defineId(MajoBroomEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SPRINTING = 
        SynchedEntityData.defineId(MajoBroomEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CUSTOM_UP = 
        SynchedEntityData.defineId(MajoBroomEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CUSTOM_DOWN = 
        SynchedEntityData.defineId(MajoBroomEntity.class, EntityDataSerializers.BOOLEAN);
    
    // 配置同步数据
    private static final EntityDataAccessor<Boolean> CONTROL_MODE = 
        SynchedEntityData.defineId(MajoBroomEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PLAYER_LEVEL = 
        SynchedEntityData.defineId(MajoBroomEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CONFIG_SPEED = 
        SynchedEntityData.defineId(MajoBroomEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CONFIG_ADVANCED_MODE = 
        SynchedEntityData.defineId(MajoBroomEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SPEED_BOOST_ACTIVE = 
        SynchedEntityData.defineId(MajoBroomEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SPEED_BOOST_TOGGLED = 
        SynchedEntityData.defineId(MajoBroomEntity.class, EntityDataSerializers.BOOLEAN);
    
    // 控制状态变量
    private boolean forward = false;
    private boolean backward = false;
    private boolean left = false;
    private boolean right = false;
    private boolean up = false;
    private boolean down = false;
    
    // 获取速度加成状态
    public boolean isSpeedBoostActive() {
        return this.entityData.get(SPEED_BOOST_ACTIVE);
    }
    
    // 设置速度加成状态
    public void setSpeedBoostActive(boolean active) {
        this.entityData.set(SPEED_BOOST_ACTIVE, active);
    }
    
    // 切换速度加成状态
    public void toggleSpeedBoost() {
        setSpeedBoostActive(!isSpeedBoostActive());
    }
    
    // 处理Ctrl键切换逻辑（类似疾跑切换）
    private boolean lastCtrlState = false;
    
    // 转向机制相关变量
    private float deltaRotation = 0.0F;  // 转向增量
    private static final float TURN_SPEED = 2.5F;  // 转向速度
    private static final double ROTATION_DECAY = 0.70;  // 转向衰减率 (70%)
    
    // 扫帚名称（用于数据管理）
    private String broomName = null;
    private UUID ownerUUID = null;
    
    // 位置更新计数器，用于控制写入频率（每5tick写入一次）
    private int positionUpdateCounter = 0;
    
    // 设置扫帚信息
    public void setBroomInfo(String broomName, UUID ownerUUID) {
        this.broomName = broomName;
        this.ownerUUID = ownerUUID;
    }
    
    // 获取扫帚名称
    public String getBroomName() {
        return broomName;
    }
    
    // 获取拥有者UUID
    public UUID getOwnerUUID() {
        return ownerUUID;
    }
    
    // 获取动量衰减率（从配置中读取）设置扫帚状态【正常：95，运动：98】
    private double getMomentumDecay() {
        return Config.BROOM_MODE.get().getMomentumDecay();
    }
    
    public void handleCtrlToggle(boolean ctrlPressed) {
        // 检查是否有任何移动键被按下（WASD、上升、下降）
        boolean hasMovementInput = this.entityData.get(FORWARD_INPUT) != 0.0f ||
                                  this.entityData.get(STRAFE_INPUT) != 0.0f ||
                                  this.entityData.get(JUMPING) ||
                                  this.entityData.get(CUSTOM_UP) ||
                                  this.entityData.get(CUSTOM_DOWN);

        // 如果没有移动输入，重置加速开关
        if (!hasMovementInput) {
            this.entityData.set(SPEED_BOOST_TOGGLED, false);
        }
        // 否则，处理Ctrl切换
        else {
            if (ctrlPressed && !this.lastCtrlState) {
                this.entityData.set(SPEED_BOOST_TOGGLED, !this.entityData.get(SPEED_BOOST_TOGGLED));
            }
        }
        this.lastCtrlState = ctrlPressed;

        // 根据最终的toggled状态和移动输入决定是否激活加速
        setSpeedBoostActive(this.entityData.get(SPEED_BOOST_TOGGLED) && hasMovementInput);
    }
    
    public MajoBroomEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true); // 禁用重力
        this.recalculateBoundingBox(); // 初始化时设置一次碰撞箱
    }
    
    /**
     * 这是一个新的辅助方法，专门用于计算和设置我们自定义的碰撞箱。
     */
    private void recalculateBoundingBox() {
        // 扫帚的碰撞体积：长1.5格，宽0.3格，高0.2格
        float length = 1.0F; // X 轴
        float height = 0.5F; // Y 轴
        float width = 1.0F;  // Z 轴
        
        // 添加Y轴和Z轴偏移，使碰撞箱与扫帚模型对齐
        float yOffset = 0.5F; // 向上偏移0.5格，使碰撞箱在扫帚模型上
        float zOffset = 0.0F; // Z轴偏移0.3格

        // 创建一个基于实体当前位置和我们自定义尺寸的AABB
        AABB aabb = new AABB(
            this.getX() - length/2, this.getY() + yOffset, this.getZ() - width/2 + zOffset,
            this.getX() + length/2, this.getY() + yOffset + height, this.getZ() + width/2 + zOffset
        );
        this.setBoundingBox(aabb);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FORWARD_INPUT, 0.0F);
        builder.define(STRAFE_INPUT, 0.0F);
        builder.define(JUMPING, false);
        builder.define(SHIFT_KEY_DOWN, false);
        builder.define(SPRINTING, false);
        builder.define(CUSTOM_UP, false);
        builder.define(CUSTOM_DOWN, false);
        builder.define(CONTROL_MODE, false);
        builder.define(PLAYER_LEVEL, 0);
        builder.define(CONFIG_SPEED, 100);
        builder.define(CONFIG_ADVANCED_MODE, false);
        builder.define(SPEED_BOOST_ACTIVE, false);
        builder.define(SPEED_BOOST_TOGGLED, false);
    }
    
    /**
     * 重写 setPos 方法，这是解决闪烁的关键。
     * 每当实体位置被更新时，这个方法都会被调用。
     */
    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z); // 首先，让原版逻辑处理位置更新（这会重置碰撞箱）
        // 然后，我们立即重新计算并设置我们自己的碰撞箱，将其覆盖回来
        if (this.level() != null) { // 确保世界已加载
             this.recalculateBoundingBox();
        }
    }

    @Override
    public void tick() {
        super.tick();
        
        // 检查是否由本地实例控制（关键的控制逻辑）
        if (this.isControlledByLocalInstance()) {
            // 更新控制状态
            this.updateControlFromInput();
            
            // 执行控制逻辑
            if (this.level().isClientSide) {
                this.controlBoat();  // 客户端执行控制逻辑
            }
            
            // 移动实体
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
        }
        
        // 重置加成状态的条件检查
        if (!this.level().isClientSide) {
            // 检查是否需要重置速度加成状态
            boolean shouldResetBoost = false;
            
            // 条件1：没有乘客时重置
            if (!this.isVehicle()) {
                shouldResetBoost = true;
            }
            // 条件2：速度为0时重置
            else if (this.getDeltaMovement().lengthSqr() < 0.001) {
                shouldResetBoost = true;
            }
            
            if (shouldResetBoost && isSpeedBoostActive()) {
                setSpeedBoostActive(false);
                this.lastCtrlState = false; // 重置Ctrl键状态
            }
            
            // 如果有乘客，处理移动逻辑（保留原有逻辑作为备用）
            if (this.isVehicle() && this.getControllingPassenger() instanceof Player player) {
                // 服务端也需要更新控制状态
                this.updateControlFromInput();
            } else {
                // 没有乘客时保持静止状态
                this.setDeltaMovement(Vec3.ZERO);
            }
        }
    }
    
    /**
     * 更新控制状态（从输入数据同步器获取）
     */
    private void updateControlFromInput() {
        // 从数据同步器获取玩家输入
        float forwardInput = this.entityData.get(FORWARD_INPUT);
        float strafeInput = this.entityData.get(STRAFE_INPUT);
        boolean jumping = this.entityData.get(JUMPING);
        boolean shiftKeyDown = this.entityData.get(SHIFT_KEY_DOWN);
        boolean sprinting = this.entityData.get(SPRINTING);
        
        // 更新控制状态
        this.forward = forwardInput > 0;
        this.backward = forwardInput < 0;
        this.left = strafeInput > 0;
        this.right = strafeInput < 0;
        this.up = this.entityData.get(CUSTOM_UP);
        this.down = this.entityData.get(CUSTOM_DOWN);
        
        // Ctrl键状态检测已在handleCtrlToggle()方法中处理
        // 这里不再重复处理，避免逻辑冲突
    }
    
    /**
     * 扫帚控制逻辑（基于技术报告的实现）
     */
    public void controlBoat() {
        if (this.isVehicle()) {
            float f = 0.0F;
            
            // 基础速度值
            float baseSpeed = 0.9f;
            // 获取配置化的基础速度百分比
            float baseSpeedPercentage = Config.BASE_SPEED_PERCENTAGE.get().floatValue() / 100.0f;
            // 获取速度加成倍数（加速模式时）
            float speedBoostMultiplier = this.isSpeedBoostActive() ? Config.SPEED_BOOST_MULTIPLIER.get().floatValue() : 1.0f;
            // 最终速度倍数 = 基础速度 × 基础速度百分比 × 速度加成倍数
            float finalSpeedMultiplier = baseSpeed * baseSpeedPercentage * speedBoostMultiplier;
            

            
            // 转向控制 - 使用增量转向机制
            if (left) {
                deltaRotation -= TURN_SPEED;  // 左转增量
            }
            if (right) {
                deltaRotation += TURN_SPEED;  // 右转增量
            }
            
            // 应用转向增量到实际朝向（即时方向更新）
            if (Math.abs(deltaRotation) > 0.01F) {
                float oldYRot = this.getYRot();
                this.setYRot(this.getYRot() + deltaRotation);
                
                // 同步玩家视角转向
                syncPlayerViewWithBroomTurning(deltaRotation);
                
                // 转向衰减，防止无限转向
                deltaRotation *= ROTATION_DECAY;
            }
            
            // 转向时的轻微推进（基于转向强度）
            if (Math.abs(deltaRotation) > 0.1F && !forward && !backward) {
                f += 0.005F * finalSpeedMultiplier * Math.abs(deltaRotation) / TURN_SPEED;
            }
            
            // 前进后退控制
            if (forward) {
                f += 0.072F * finalSpeedMultiplier;  // 前进加速
            }
            if (backward) {
                f -= 0.036F * finalSpeedMultiplier;  // 后退减速
            }
            
            // 获取当前速度向量
            Vec3 currentVelocity = this.getDeltaMovement();
            
            // 基于新朝向计算推进力向量（即时重定向）
            Vec3 thrustVector = new Vec3(
                (double)(Mth.sin(-this.getYRot() * ((float)Math.PI / 180F)) * f),
                0.0D,
                (double)(Mth.cos(this.getYRot() * ((float)Math.PI / 180F)) * f)
            );
            
            // 智能动量衰减：只在转向时应用衰减，直线飞行时保持原有累积方式
            Vec3 v3d;
            if (Math.abs(deltaRotation) > 0.1F) {
                // 转向时：应用动量衰减以实现流畅转向
                Vec3 decayedVelocity = new Vec3(
                    currentVelocity.x * getMomentumDecay(),
                    currentVelocity.y,
                    currentVelocity.z * getMomentumDecay()
                );
                v3d = decayedVelocity.add(thrustVector);
            } else {
                // 直线飞行时：保持原有的速度累积方式以达到最大速度
                v3d = currentVelocity.add(thrustVector);
            }
            
            // 水平最大速度限制（包含速度加成）
            double maxHorizontalSpeed = finalSpeedMultiplier;
            double currentHorizontalSpeed = Math.sqrt(v3d.x * v3d.x + v3d.z * v3d.z);
            
            if (currentHorizontalSpeed > maxHorizontalSpeed) {
                // 限制水平速度到最大值，保持方向
                double scale = maxHorizontalSpeed / currentHorizontalSpeed;
                v3d = new Vec3(v3d.x * scale, v3d.y, v3d.z * scale);
            }
            
            // 水平无输入时的额外减速处理
            if (!forward && !backward && !left && !right && Math.abs(deltaRotation) < 0.1F) {
                // 没有任何输入时，应用更强的减速
                v3d = new Vec3(v3d.x * 0.95, v3d.y, v3d.z * 0.95);
                
                // 自然减速
                double naturalDeceleration = 0.02;
                if (Math.abs(v3d.x) > naturalDeceleration) {
                    v3d = new Vec3(v3d.x - Math.signum(v3d.x) * naturalDeceleration, v3d.y, v3d.z);
                }
                if (Math.abs(v3d.z) > naturalDeceleration) {
                    v3d = new Vec3(v3d.x, v3d.y, v3d.z - Math.signum(v3d.z) * naturalDeceleration);
                }
                
                // 微小速度归零，避免无限滑行
                if (Math.abs(v3d.x) < 0.01 && Math.abs(v3d.z) < 0.01) {
                    v3d = new Vec3(0, v3d.y, 0);
                }
            }
            
            // 垂直移动控制
            float currentY = (float) v3d.y;
            float maxYspeed = 0.6f * finalSpeedMultiplier;     // 上下垂直速度最大值应用配置
            float yacc = 0.05f * finalSpeedMultiplier;         // 垂直加速度应用配置
            float ydec = 0.02f;                                // 垂直减速度固定值
            
            // 先应用70%的垂直衰减
            if (!up && !down) {
                currentY *= 0.95f;  // 每帧90%衰减
            }
            
            // 上升状态处理
            if (currentY > 0) {
                if (up && down) {
                    // 同时按上下键，保持当前状态
                } else if (up) {
                    // 上升加速，限制最大速度
                    if (currentY + yacc > maxYspeed) {
                        currentY = maxYspeed;
                    } else {
                        currentY += yacc;
                    }
                } else if (down) {
                    currentY -= yacc;  // 下降
                } else {
                    currentY -= ydec;  // 自然减速
                }
            }
            // 下降状态处理
            else {
                if (up && down) {
                    // 同时按上下键，保持当前状态
                } else if (down) {
                    // 下降加速，限制最大速度
                    if (currentY - yacc < -maxYspeed) {
                        currentY = -maxYspeed;
                    } else {
                        currentY -= yacc;
                    }
                } else if (up) {
                    currentY += yacc;  // 上升
                } else {
                    currentY += ydec;  // 自然减速
                }
            }
            
            // 微小速度归零
            if (Math.abs(currentY) <= 0.03) {
                currentY = 0;
            }
            // 在这里加入打印语句
            // System.out.println("ControlBoat Tick -> Current Y Speed: " + currentY + " | Max Y Speed: " + maxYspeed + " | Y Acceleration: " + yacc + " | Y Deceleration: " + ydec);
            
            // 应用移动
            this.setDeltaMovement(v3d.x, currentY, v3d.z);
        }
    }

    /**
     * 更新玩家输入（客户端调用）
     */
    public void updatePlayerInput(float forward, float strafe, boolean jumping, boolean shiftKeyDown, boolean sprinting, boolean customUp, boolean customDown, boolean ctrlPressed) {
        this.entityData.set(FORWARD_INPUT, forward);
        this.entityData.set(STRAFE_INPUT, strafe);
        this.entityData.set(JUMPING, jumping);
        this.entityData.set(SHIFT_KEY_DOWN, shiftKeyDown);
        this.entityData.set(SPRINTING, sprinting);
        this.entityData.set(CUSTOM_UP, customUp);
        this.entityData.set(CUSTOM_DOWN, customDown);
        
        // 处理Ctrl键切换逻辑
        handleCtrlToggle(ctrlPressed);
    }
    
    /**
     * 玩家攻击扫帚时的处理（潜行+左键回收）
     */
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource damageSource, float amount) {
        if (!this.level().isClientSide && damageSource.getEntity() instanceof Player player) {
            // 检查玩家是否潜行
            if (player.isShiftKeyDown()) {
                // 删除扫帚数据（如果有的话）
                if (this.broomName != null && this.ownerUUID != null && this.ownerUUID.equals(player.getUUID())) {
                    // 验证实体UUID匹配（如果不匹配会自动删除过期数据）
                    boolean isValid = BroomDataManager.validateBroomEntityUUID((net.minecraft.server.level.ServerLevel) this.level(), 
                        this.ownerUUID, this.broomName, this.getUUID());
                    
                    if (isValid) {
                        BroomDataManager.removeBroom((net.minecraft.server.level.ServerLevel) this.level(), 
                            this.ownerUUID, this.broomName);
                        
                        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                            "message.ashenwitchbroom.broom_collected", this.broomName));
                        
                        AshenWitchBroom.LOGGER.info("Player {} collected broom '{}' at {}", 
                            player.getName().getString(), this.broomName, this.blockPosition());
                    } else {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                            "message.ashenwitchbroom.broom_data_mismatch"));
                        AshenWitchBroom.LOGGER.warn("UUID validation failed when collecting broom '{}', data already cleaned up", this.broomName);
                    }
                } else if (this.broomName != null && this.ownerUUID != null && !this.ownerUUID.equals(player.getUUID())) {
                    // 不是拥有者，无法收回
                    player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                        "message.ashenwitchbroom.not_owner"));
                    return false;
                }
                
                // 创建扫帚物品
                ItemStack broomItem = new ItemStack(AshenWitchBroom.MAJO_BROOM_ITEM.get());
                
                // 检查玩家主手是否为空
                ItemStack mainHandItem = player.getMainHandItem();
                if (mainHandItem.isEmpty()) {
                    // 空手：直接放到当前选择的槽位
                    player.getInventory().setItem(player.getInventory().selected, broomItem);
                    this.discard();
                    return false; // 不造成实际伤害
                } else {
                    // 手持物品：尝试添加到背包
                    if (player.getInventory().add(broomItem)) {
                        // 成功添加到背包
                        this.discard();
                        return false;
                    } else {
                        // 背包满了，掉落物品
                        player.drop(broomItem, false);
                        this.discard();
                        return false;
                    }
                }
            }
        }
        
        // 非潜行攻击或其他伤害源，不处理
        return false;
    }
    
    /**
     * 玩家与扫帚交互（骑乘功能）
     */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        // 首先，判断玩家是否潜行。这个判断在客户端和服务端都会执行。
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS; // 如果潜行，则不处理骑乘，直接返回PASS
        }

        // 如果玩家没有潜行，我们就在服务端执行骑乘/下马逻辑
        if (!this.level().isClientSide) {
            if (!this.isVehicle()) {
                // 让玩家骑乘扫帚
                player.startRiding(this);
            } else {
                // 如果已经有乘客，让当前乘客下来
                this.ejectPassengers();
                // 下扫帚时立即同步更新位置数据，确保验证时数据准确
                if (this.broomName != null && this.ownerUUID != null) {
                    updateBroomPositionSync();
                }
            }
        }
        
        // 关键：无论在客户端还是服务端，只要玩家没有潜行，都返回SUCCESS
        // 这会告诉客户端："交互成功了，不要做多余的默认动作，等待服务器的指令"
        return InteractionResult.SUCCESS;
    }
    
    /**
     * 当乘客被移除时（下扫帚），立即同步更新位置数据
     */
    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        
        // 下扫帚时立即同步更新位置数据，确保验证时数据准确
        if (!this.level().isClientSide && this.broomName != null && this.ownerUUID != null) {
            updateBroomPositionSync();
            AshenWitchBroom.LOGGER.debug("Player {} dismounted broom '{}', position synced", 
                passenger.getName().getString(), this.broomName);
        }
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("BroomName", 8)) { // 8 = String type
            this.broomName = compound.getString("BroomName");
        }
        if (compound.contains("OwnerUUID", 8)) { // 8 = String type
            try {
                this.ownerUUID = UUID.fromString(compound.getString("OwnerUUID"));
            } catch (IllegalArgumentException e) {
                AshenWitchBroom.LOGGER.warn("Invalid owner UUID in broom data: {}", compound.getString("OwnerUUID"));
            }
        }
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.broomName != null) {
            compound.putString("BroomName", this.broomName);
        }
        if (this.ownerUUID != null) {
            compound.putString("OwnerUUID", this.ownerUUID.toString());
        }
    }
    
    @Override
    public boolean isPickable() {
        return true; // 可以被选中交互
    }
    
    @Override
    public boolean isPushable() {
        return true; // 不能被推动
    }
    
    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty() && passenger instanceof Player; // 只允许一个玩家乘坐
    }
    
    /**
     * 获取控制实体的乘客
     */
    @Override
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        if (passenger instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        return null;
    }
    
    /**
     * 检查是否由本地实例控制
     */
    public boolean isControlledByLocalInstance() {
        return this.isVehicle() && this.getControllingPassenger() != null;
    }
    
    /**
     * 设置乘客位置
     */
    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            // --- 动画与物理同步的关键 ---
            // 1. 获取与渲染器中相同的浮动偏移量
            //    我们使用 tickCount 来模拟时间流逝，这与渲染器中的逻辑一致。
            float time = this.tickCount;
            float hoverOffset = Mth.sin(time * 0.1F) * 0.05F;

            // 2. 将基础高度偏移与动画浮动偏移相加
            double yOffset = 0.2 + hoverOffset; // 乘客的基础高度 + 实时浮动高度

            // --- 其他位置计算保持不变 ---
            double forwardOffset = 0.1; // 向前的偏移量
            
            // 计算向前偏移的坐标
            float yaw = this.getYRot() * ((float)Math.PI / 180F);
            double xOffset = -Math.sin(yaw) * forwardOffset;
            double zOffset = Math.cos(yaw) * forwardOffset;
            
            // 3. 使用包含动画偏移的新Y坐标来设置乘客位置
            moveFunction.accept(passenger, 
                this.getX() + xOffset, 
                this.getY() + yOffset, 
                this.getZ() + zOffset);
            
            // 4. 飞行时每5tick异步更新位置信息（不阻塞主线程）
            if (!this.level().isClientSide && this.broomName != null && this.ownerUUID != null) {
                positionUpdateCounter++;
                if (positionUpdateCounter >= 5) {
                    updateBroomPositionAsync();
                    positionUpdateCounter = 0; // 重置计数器
                }
            }
        }
    }
    
    /**
     * 同步更新扫帚在数据文件中的位置（用于关键时刻，确保数据一致性）
     */
    private void updateBroomPositionSync() {
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.minecraft.core.BlockPos currentPos = this.blockPosition();
            BroomDataManager.updateBroom(serverLevel, this.ownerUUID, this.broomName, 
                this.level().dimension(), currentPos, this.getUUID());
            
            AshenWitchBroom.LOGGER.debug("Sync updated broom '{}' position to {} in {}", 
                this.broomName, currentPos, this.level().dimension().location());
        }
    }
    
    /**
     * 异步更新扫帚在数据文件中的位置（用于飞行时，不阻塞主线程）
     */
    private void updateBroomPositionAsync() {
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.minecraft.core.BlockPos currentPos = this.blockPosition();
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension = this.level().dimension();
            String broomName = this.broomName;
            UUID ownerUUID = this.ownerUUID;
            
            // 异步执行，不阻塞主线程
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                BroomDataManager.updateBroom(serverLevel, ownerUUID, broomName, dimension, currentPos, this.getUUID());
            });
        }
    }
    
    /**
     * 扫帚被移除时清理数据
     */
    @Override
    public void remove(RemovalReason reason) {
        // 如果扫帚因为非正常原因被移除（比如/kill命令），清理数据
        if (!this.level().isClientSide && reason != RemovalReason.DISCARDED && 
            this.broomName != null && this.ownerUUID != null) {
            // 验证实体UUID匹配（如果不匹配会自动删除过期数据）
            boolean isValid = BroomDataManager.validateBroomEntityUUID((net.minecraft.server.level.ServerLevel) this.level(), 
                this.ownerUUID, this.broomName, this.getUUID());
            
            if (isValid) {
                BroomDataManager.removeBroom((net.minecraft.server.level.ServerLevel) this.level(), 
                    this.ownerUUID, this.broomName);
                
                AshenWitchBroom.LOGGER.info("Broom '{}' owned by {} was removed due to: {}", 
                    this.broomName, this.ownerUUID, reason);
            } else {
                AshenWitchBroom.LOGGER.warn("UUID validation failed when removing broom '{}', data already cleaned up", 
                    this.broomName);
            }
        }
        
        super.remove(reason);
    }
    
    @Override
    public boolean canBeCollidedWith() {
        return true; // 可以碰撞
    }
    
    /**
     * 扫帚不需要浮在水面上，保持空中飞行状态
     */
    public void floatBoat() {
        // 应用转向衰减，确保转向响应性
        if (Math.abs(deltaRotation) > 0.001F) {
            deltaRotation *= ROTATION_DECAY;
        } else {
            deltaRotation = 0.0F; // 完全停止微小的转向
        }
    }
    
    /**
     * 同步玩家视角与扫帚转向
     * 只在客户端执行，使玩家视角跟随扫帚转向
     */
    @OnlyIn(Dist.CLIENT)
    private void syncPlayerViewWithBroomTurning(float deltaRotation) {
        if (this.level().isClientSide) {
            Player controllingPlayer = (Player) this.getControllingPassenger();
            if (controllingPlayer != null && controllingPlayer == Minecraft.getInstance().player) {
                // 获取当前玩家的视角
                float currentYaw = controllingPlayer.getYRot();
                // 应用与扫帚相同的转向增量
                controllingPlayer.setYRot(currentYaw + deltaRotation);
                // 同步到服务端
                controllingPlayer.yRotO = controllingPlayer.getYRot();
            }
        }
    }
    
    /**
     * 重置转向状态，用于优化转向响应性
     */
    public void resetTurningState() {
        deltaRotation = 0.0F;
    }
}
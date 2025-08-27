package com.magicbroom.examplemod.core;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 扫帚相关配置


    public static final ModConfigSpec.DoubleValue BASE_SPEED_PERCENTAGE = BUILDER
            .comment("基础速度百分比")
            .defineInRange("baseSpeedPercentage", 100.0, 0.0, Double.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue ADVANCED_MODE = BUILDER
            .comment("启用第三人称视角 (骑乘扫帚时自动切换到第三人称视角)")
            .define("advancedMode", false);



    public static final ModConfigSpec.DoubleValue SPEED_BOOST_MULTIPLIER = BUILDER
            .comment("按下Ctrl键时的速度加成倍数")
            .defineInRange("speedBoostMultiplier", 1.5, 0.0, Double.MAX_VALUE);

    public static final ModConfigSpec.EnumValue<BroomMode> BROOM_MODE = BUILDER
            .comment("扫帚飞行模式 (NORMAL: 正常模式, SPORT: 运动模式)")
            .defineEnum("broomMode", BroomMode.NORMAL);

    // 新的召唤系统配置
    public static final ModConfigSpec.IntValue NEARBY_SEARCH_RANGE = BUILDER
            .comment("当前范围内搜索扫帚的距离（格数）")
            .defineInRange("nearbySearchRange", 16, 1, 256);

    public static final ModConfigSpec.BooleanValue ENABLE_NEARBY_SUMMON = BUILDER
            .comment("是否启用当前范围内召唤")
            .define("enableNearbySummon", true);

    public static final ModConfigSpec.BooleanValue ENABLE_WORLD_SUMMON = BUILDER
            .comment("是否启用本世界召唤")
            .define("enableWorldSummon", true);

    public static final ModConfigSpec.BooleanValue ENABLE_CROSS_DIMENSION_SUMMON = BUILDER
            .comment("是否启用全维度召唤")
            .define("enableCrossDimensionSummon", true);

    public static final ModConfigSpec.BooleanValue ENABLE_INVENTORY_SUMMON = BUILDER
            .comment("是否启用背包召唤")
            .define("enableInventorySummon", true);

    public static final ModConfigSpec.BooleanValue ENABLE_DETAILED_INFO = BUILDER
            .comment("是否启用详细信息显示")
            .define("enableDetailedInfo", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public enum BroomMode {
        NORMAL(0.95),  // 正常模式
        SPORT(0.98);   // 运动模式

        private final double momentumDecay;

        BroomMode(double momentumDecay) {
            this.momentumDecay = momentumDecay;
        }

        public double getMomentumDecay() {
            return momentumDecay;
        }

        @Override
        public String toString() {
            return "ashenwitchbroom.broommode." + name().toLowerCase();
        }
    }


}

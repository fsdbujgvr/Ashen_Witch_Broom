package com.magicbroom.examplemod.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import com.magicbroom.examplemod.core.AshenWitchBroom;
import net.minecraft.world.entity.Entity;

/**
 * BroomModel 类，由 Blockbench 导出的模型转化而来。
 * 这个模型可以用于任何自定义实体。
 */
public class MajoBroomModel<T extends Entity> extends EntityModel<T> {

    // 模型的唯一层级位置。这是注册和获取模型的关键。
    // 我们将 "modid" 替换为您的 MODID，并将 "model" 替换为 "broom" 以方便识别。
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AshenWitchBroom.MODID, "broom"), "main");
    
    // 模型的各个主要部件
    private final ModelPart bone;
    private final ModelPart bone2;
    private final ModelPart bone3;
    private final ModelPart bone4;
    private final ModelPart bone6;

    public MajoBroomModel(ModelPart root) {
        // 从烘焙好的根部件中获取所有子部件的引用
        this.bone = root.getChild("bone");
        this.bone2 = root.getChild("bone2");
        this.bone3 = root.getChild("bone3");
        this.bone4 = root.getChild("bone4");
        this.bone6 = root.getChild("bone6");
    }

    /**
     * 创建模型的几何定义。这部分代码由 Blockbench 自动生成，包含了所有的顶点和结构信息。
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // VVVV 以下是 Blockbench 生成的几何数据，无需修改 VVVV
		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 15).addBox(-1.0F, -12.6308F, -7.0F, 2.0F, 2.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-1.0F, -13.5308F, -24.3F, 2.0F, 2.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(39, 42).addBox(0.4188F, -10.5929F, -3.3461F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -2.4678F, -10.2816F, -0.192F, 0.0F, -0.0436F));

		PartDefinition bone2 = partdefinition.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(1.0F, 24.0F, 0.0F));

		PartDefinition cube_r2 = bone2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(22, 27).addBox(7.4846F, 0.0F, 7.7625F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -2.0F, 1.5F, 0.0F, 0.3927F, -1.5708F));

		PartDefinition cube_r3 = bone2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(27, 2).addBox(-2.3369F, 8.6308F, 3.6943F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -2.0F, 1.5F, 0.0F, 0.3927F, 3.1416F));

		PartDefinition cube_r4 = bone2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(27, 12).addBox(-10.3108F, -2.0F, 0.3914F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -2.0F, 1.5F, 0.0F, 0.3927F, 1.5708F));

		PartDefinition cube_r5 = bone2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 30).addBox(5.771F, -6.7694F, 7.0527F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -2.0F, 1.5F, 0.0F, 0.3927F, -0.829F));

		PartDefinition cube_r6 = bone2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(17, 17).addBox(4.2252F, 6.5171F, 6.4124F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -2.0F, 1.5F, 0.0F, 0.3927F, -2.3562F));

		PartDefinition cube_r7 = bone2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(17, 0).addBox(-7.0514F, -8.5171F, 1.7415F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -2.0F, 1.5F, 0.0F, 0.3927F, 0.7854F));

		PartDefinition cube_r8 = bone2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(10, 32).addBox(-8.3579F, 5.1029F, 1.2003F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -2.0F, 1.5F, 0.0F, 0.3927F, 2.3562F));

		PartDefinition cube_r9 = bone2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(32, 32).addBox(-0.4892F, -10.6308F, 4.4597F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -2.0F, 1.5F, 0.0F, 0.3927F, 0.0F));

		PartDefinition bone3 = partdefinition.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(0, 49).addBox(-1.8F, -12.2308F, 3.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(48, 27).addBox(-1.8F, -12.0308F, 3.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(48, 39).addBox(-2.2F, -12.2308F, 3.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(20, 30).addBox(-2.2F, -12.0308F, 3.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 23.5F, 0.0F));

		PartDefinition bone4 = partdefinition.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 22.0F, 2.1F, 0.0F, 0.0F, -0.3491F));

		PartDefinition cube_r10 = bone4.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(32, 22).addBox(6.6319F, 3.2339F, 7.4093F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.0F, 0.3927F, -1.5708F));

		PartDefinition cube_r11 = bone4.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(21, 37).addBox(-5.3246F, 7.7078F, 2.4567F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.0F, 0.3927F, 3.1416F));

		PartDefinition cube_r12 = bone4.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(37, 0).addBox(-9.4581F, -5.2339F, 0.4446F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.0F, 0.3927F, 1.5708F));

		PartDefinition cube_r13 = bone4.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(38, 9).addBox(7.1608F, -3.7616F, 7.6284F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.0F, 0.3927F, -0.829F));

		PartDefinition cube_r14 = bone4.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 40).addBox(1.5096F, 8.1512F, 5.0876F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.0F, 0.3927F, -2.3562F));

		PartDefinition cube_r15 = bone4.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(41, 18).addBox(-4.3358F, -10.1512F, 2.5663F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.0F, 0.3927F, 0.7854F));

		PartDefinition cube_r16 = bone4.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(9, 42).addBox(-9.8676F, 2.1636F, 0.575F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.0F, 0.3927F, 2.3562F));

		PartDefinition cube_r17 = bone4.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(30, 42).addBox(2.4985F, -9.7078F, 5.5972F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.0F, 0.3927F, 0.0F));

		PartDefinition bone6 = partdefinition.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offset(-1.0F, 22.0F, 1.5F));

		PartDefinition cube_r26 = bone6.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(17, 17).addBox(10.3333F, -2.942F, 2.4361F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2967F, -1.2654F));

		PartDefinition cube_r27 = bone6.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(17, 17).addBox(6.1099F, 7.2494F, 3.1273F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2967F, -2.4871F));

		PartDefinition cube_r28 = bone6.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(17, 17).addBox(-0.9897F, -10.3919F, 4.6978F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2967F, 0.3491F));

		PartDefinition cube_r29 = bone6.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(17, 17).addBox(-7.8817F, 0.5267F, 6.9048F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2967F, 1.8326F));

		PartDefinition cube_r30 = bone6.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(17, 17).addBox(-5.373F, 7.8845F, 4.4764F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.1222F, 2.8362F));

		PartDefinition cube_r31 = bone6.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(17, 17).addBox(6.2402F, 6.4527F, 5.2174F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, -0.096F, -2.3562F));
        // ^^^^ 结束 Blockbench 生成的代码 ^^^^

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    /**
     * 设置模型的动画。这个方法在每一帧都会被调用。
     * @param entity          应用这个模型的实体
     * @param limbSwing       实体肢体摆动幅度
     * @param limbSwingAmount 实体肢体摆动量
     * @param ageInTicks      实体存在的时间 (ticks)
     * @param netHeadYaw      实体头的水平朝向
     * @param headPitch       实体头的垂直朝向
     */
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // 示例：添加一个简单的上下悬浮动画
        // 让主干 (bone) 随着时间正弦波动，实现平滑的上下浮动效果
        this.bone.y = Mth.sin(ageInTicks * 0.1F) * 0.5F;
    }

    /**
     * 渲染模型到缓存中。
     * PoseStack 用于处理模型的位置、旋转和缩放。
     */
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        // 渲染所有在根下的主要部件
        bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        bone2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        bone3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        bone4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        bone6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
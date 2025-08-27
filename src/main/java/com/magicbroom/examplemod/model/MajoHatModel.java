package com.magicbroom.examplemod.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import com.magicbroom.examplemod.core.AshenWitchBroom;

public class MajoHatModel<T extends LivingEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AshenWitchBroom.MODID, "majo_hat"), "main");
    
    private final ModelPart bone18;
    private final ModelPart bone17;
    private final ModelPart hat_base;
    private final ModelPart cube_r1;
    private final ModelPart cube_r2;
    private final ModelPart cube_r3;
    private final ModelPart cube_r4;
    private final ModelPart cube_r5;
    private final ModelPart hat_base2;
    private final ModelPart cube_r6;
    private final ModelPart cube_r7;
    private final ModelPart cube_r8;
    private final ModelPart cube_r9;
    private final ModelPart cube_r10;
    private final ModelPart jian;
    private final ModelPart cube_r11;
    private final ModelPart cube_r12;
    private final ModelPart cube_r13;
    private final ModelPart cube_r14;
    private final ModelPart cube_r15;
    private final ModelPart cube_r16;
    private final ModelPart diaozhui;
    private final ModelPart cube_r17;
    private final ModelPart cube_r18;
    private final ModelPart cube_r19;
    private final ModelPart cycle;
    private final ModelPart bone;
    private final ModelPart bone2;
    private final ModelPart bone3;
    private final ModelPart bone4;
    private final ModelPart bone5;
    private final ModelPart bone6;
    private final ModelPart bone7;
    private final ModelPart bone8;
    private final ModelPart cycle2;
    private final ModelPart bone9;
    private final ModelPart bone10;
    private final ModelPart bone11;
    private final ModelPart bone12;
    private final ModelPart bone13;
    private final ModelPart bone14;
    private final ModelPart bone15;
    private final ModelPart bone16;

    public MajoHatModel(ModelPart root) {
        super(root);
        
        // bone18现在是head的子部件
        ModelPart head = root.getChild("head");
        this.bone18 = head.getChild("bone18");
        this.bone17 = this.bone18.getChild("bone17");
        this.hat_base = this.bone17.getChild("hat_base");
        this.cube_r1 = this.hat_base.getChild("cube_r1");
        this.cube_r2 = this.hat_base.getChild("cube_r2");
        this.cube_r3 = this.hat_base.getChild("cube_r3");
        this.cube_r4 = this.hat_base.getChild("cube_r4");
        this.cube_r5 = this.hat_base.getChild("cube_r5");
        this.hat_base2 = this.bone17.getChild("hat_base2");
        this.cube_r6 = this.hat_base2.getChild("cube_r6");
        this.cube_r7 = this.hat_base2.getChild("cube_r7");
        this.cube_r8 = this.hat_base2.getChild("cube_r8");
        this.cube_r9 = this.hat_base2.getChild("cube_r9");
        this.cube_r10 = this.hat_base2.getChild("cube_r10");
        this.jian = this.bone17.getChild("jian");
        this.cube_r11 = this.jian.getChild("cube_r11");
        this.cube_r12 = this.jian.getChild("cube_r12");
        this.cube_r13 = this.jian.getChild("cube_r13");
        this.cube_r14 = this.jian.getChild("cube_r14");
        this.cube_r15 = this.jian.getChild("cube_r15");
        this.cube_r16 = this.jian.getChild("cube_r16");
        this.diaozhui = this.bone17.getChild("diaozhui");
        this.cube_r17 = this.diaozhui.getChild("cube_r17");
        this.cube_r18 = this.diaozhui.getChild("cube_r18");
        this.cube_r19 = this.diaozhui.getChild("cube_r19");
        this.cycle = this.bone17.getChild("cycle");
        this.bone = this.cycle.getChild("bone");
        this.bone2 = this.cycle.getChild("bone2");
        this.bone3 = this.cycle.getChild("bone3");
        this.bone4 = this.cycle.getChild("bone4");
        this.bone5 = this.cycle.getChild("bone5");
        this.bone6 = this.cycle.getChild("bone6");
        this.bone7 = this.cycle.getChild("bone7");
        this.bone8 = this.cycle.getChild("bone8");
        this.cycle2 = this.bone17.getChild("cycle2");
        this.bone9 = this.cycle2.getChild("bone9");
        this.bone10 = this.cycle2.getChild("bone10");
        this.bone11 = this.cycle2.getChild("bone11");
        this.bone12 = this.cycle2.getChild("bone12");
        this.bone13 = this.cycle2.getChild("bone13");
        this.bone14 = this.cycle2.getChild("bone14");
        this.bone15 = this.cycle2.getChild("bone15");
        this.bone16 = this.cycle2.getChild("bone16");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 添加HumanoidModel所需的标准部件
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

        // 将自定义帽子模型作为head的子部件
        PartDefinition bone18 = head.addOrReplaceChild("bone18", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -3.6F, 0.0F, -0.0785F, 0.0F, 0.0F));

        PartDefinition bone17 = bone18.addOrReplaceChild("bone17", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 9.0123F, -0.3137F, 0.0F, 2.0508F, 0.0F));

        PartDefinition hat_base = bone17.addOrReplaceChild("hat_base", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = hat_base.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(67, 165).addBox(9.1F, -13.0F, -1.0F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1257F, 0.0F));

        PartDefinition cube_r2 = hat_base.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 186).addBox(-14.0F, -13.0F, -5.0F, 28.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.6632F, 0.0F));

        PartDefinition cube_r3 = hat_base.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 174).addBox(-14.0F, -13.0F, -5.0F, 28.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0873F, 0.0F));

        PartDefinition cube_r4 = hat_base.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 162).addBox(-14.0F, -13.0F, -5.0F, 28.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.829F, 0.0F));

        PartDefinition cube_r5 = hat_base.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 150).addBox(-14.0F, -13.0F, -5.0F, 28.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition hat_base2 = bone17.addOrReplaceChild("hat_base2", CubeListBuilder.create(), PartPose.offset(0.1F, -0.5F, 0.3F));

        PartDefinition cube_r6 = hat_base2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(67, 168).addBox(-2.5F, -1.0F, -1.5F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.4022F, -12.0F, 10.1645F, 0.0F, -1.3003F, 0.0F));

        PartDefinition cube_r7 = hat_base2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(60, 199).addBox(-12.0F, -13.0F, -5.0F, 25.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.6632F, 0.0F));

        PartDefinition cube_r8 = hat_base2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(60, 210).addBox(-12.0F, -13.0F, -5.0F, 25.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0873F, 0.0F));

        PartDefinition cube_r9 = hat_base2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 209).addBox(-12.0F, -13.0F, -5.0F, 25.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.829F, 0.0F));

        PartDefinition cube_r10 = hat_base2.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 198).addBox(-12.0F, -13.0F, -5.0F, 25.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition jian = bone17.addOrReplaceChild("jian", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r11 = jian.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(5, 180).addBox(-0.6F, -2.0F, -0.6F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.332F, -21.9265F, 1.8281F, -1.2654F, -1.2566F, 0.0F));

        PartDefinition cube_r12 = jian.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 150).addBox(-0.9F, -7.8F, -2.9F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0309F, -16.4F, 0.0951F, -0.9861F, -1.2566F, 0.0F));

        PartDefinition cube_r13 = jian.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(67, 186).addBox(-1.9F, -6.1F, -1.7F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0309F, -16.4F, 0.0951F, -0.6981F, -1.2566F, 0.0F));

        PartDefinition cube_r14 = jian.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(67, 174).addBox(-2.5F, -4.4F, -2.2F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0309F, -16.4F, 0.0951F, -0.5672F, -1.2566F, 0.0F));

        PartDefinition cube_r15 = jian.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(0, 220).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0309F, -16.4F, 0.0951F, -0.3054F, -1.2566F, 0.0F));

        PartDefinition cube_r16 = jian.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(68, 152).addBox(-4.9F, -15.1F, -5.0F, 10.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.2566F, 0.0F));

        PartDefinition diaozhui = bone17.addOrReplaceChild("diaozhui", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r17 = diaozhui.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, 176).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.3F, -18.6F, 3.1F, 0.0F, 0.3491F, 0.0F));

        PartDefinition cube_r18 = diaozhui.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(0, 179).addBox(-1.0F, -0.7F, -0.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.6571F, -20.4565F, 3.0726F, -0.1745F, 0.1309F, -1.2566F));

        PartDefinition cube_r19 = diaozhui.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(5, 178).addBox(1.6923F, -2.4F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.5F, -19.5F, 3.5F, 0.0F, 0.3054F, -0.384F));

        PartDefinition cycle = bone17.addOrReplaceChild("cycle", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.5F, -17.0F, 0.3F, -0.0873F, 0.0F, -0.2094F));

        PartDefinition bone = cycle.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 174).addBox(-1.9F, -1.2F, -4.8F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.3562F, 0.0F));

        PartDefinition bone2 = cycle.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 170).addBox(-1.9F, -1.2F, -4.8F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone3 = cycle.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(0, 168).addBox(-2.0F, -1.2F, -4.6769F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition bone4 = cycle.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(0, 166).addBox(-2.0F, -1.2F, -4.8F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition bone5 = cycle.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(0, 164).addBox(-1.9F, -1.2F, -4.8F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone6 = cycle.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(0, 162).addBox(-1.9F, -1.2F, -4.8F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone7 = cycle.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(0, 158).addBox(-1.9F, 133.8F, -4.8F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -135.0F, 0.0F));

        PartDefinition bone8 = cycle.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(0, 156).addBox(-2.0F, -1.2F, -4.8F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.3562F, 0.0F));

        PartDefinition cycle2 = bone17.addOrReplaceChild("cycle2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -14.0F, 0.0F, 0.0F, 0.0F, -0.1309F));

        PartDefinition bone9 = cycle2.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(19, 222).addBox(-2.7F, -1.7F, -6.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.3562F, 0.0F));

        PartDefinition bone10 = cycle2.addOrReplaceChild("bone10", CubeListBuilder.create().texOffs(66, 221).addBox(-2.7F, -1.7F, -6.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone11 = cycle2.addOrReplaceChild("bone11", CubeListBuilder.create().texOffs(54, 221).addBox(-2.7F, -1.7F, -6.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition bone12 = cycle2.addOrReplaceChild("bone12", CubeListBuilder.create().texOffs(43, 220).addBox(-2.7F, -1.7F, -6.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition bone13 = cycle2.addOrReplaceChild("bone13", CubeListBuilder.create().texOffs(31, 220).addBox(-2.7F, -1.7F, -6.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone14 = cycle2.addOrReplaceChild("bone14", CubeListBuilder.create().texOffs(19, 220).addBox(-2.7F, -1.7F, -6.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone15 = cycle2.addOrReplaceChild("bone15", CubeListBuilder.create().texOffs(67, 195).addBox(-2.7F, 133.3F, -6.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -135.0F, 0.0F));

        PartDefinition bone16 = cycle2.addOrReplaceChild("bone16", CubeListBuilder.create().texOffs(67, 150).addBox(-2.7F, -1.7F, -6.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.3562F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        // 帽子跟随头部旋转
        this.bone18.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.bone18.xRot = headPitch * ((float)Math.PI / 180F);
    }
}
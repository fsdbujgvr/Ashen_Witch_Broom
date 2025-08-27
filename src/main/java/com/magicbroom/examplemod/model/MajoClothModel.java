package com.magicbroom.examplemod.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import com.magicbroom.examplemod.core.AshenWitchBroom;

public class MajoClothModel<T extends LivingEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AshenWitchBroom.MODID, "majo_cloth"), "main");
    
    // 自定义模型部件
    private final ModelPart left;
    private final ModelPart bone56;
    private final ModelPart bone57;
    private final ModelPart bone58;
    private final ModelPart bone59;
    private final ModelPart bone60;
    private final ModelPart bone61;
    private final ModelPart bone62;
    private final ModelPart bone63;
    private final ModelPart bone64;
    private final ModelPart bone65;
    private final ModelPart bone66;
    private final ModelPart bone67;
    private final ModelPart bone70;
    private final ModelPart bone71;
    private final ModelPart bone72;
    private final ModelPart bone73;
    private final ModelPart bone74;
    private final ModelPart bone2;
    private final ModelPart bone19;
    private final ModelPart right;
    private final ModelPart bone39;
    private final ModelPart bone40;
    private final ModelPart bone41;
    private final ModelPart bone42;
    private final ModelPart bone43;
    private final ModelPart bone44;
    private final ModelPart bone45;
    private final ModelPart bone46;
    private final ModelPart bone47;
    private final ModelPart bone48;
    private final ModelPart bone49;
    private final ModelPart bone50;
    private final ModelPart bone51;
    private final ModelPart bone52;
    private final ModelPart bone53;
    private final ModelPart bone54;
    private final ModelPart bone55;
    private final ModelPart bone21;
    private final ModelPart bone20;
    private final ModelPart bigBody;
    private final ModelPart bone3;
    private final ModelPart bone5;
    private final ModelPart bone6;
    private final ModelPart bone7;
    private final ModelPart bone8;
    private final ModelPart bone9;
    private final ModelPart bone;
    private final ModelPart bone10;
    private final ModelPart bone11;
    private final ModelPart bone12;
    private final ModelPart bone13;
    private final ModelPart sithide2;
    private final ModelPart bone37;
    private final ModelPart bone38;
    private final ModelPart bone75;
    private final ModelPart bone78;
    private final ModelPart sithide1;
    private final ModelPart bone77;
    private final ModelPart bone80;
    private final ModelPart bone119;
    private final ModelPart bone79;
    private final ModelPart bone14;
    private final ModelPart bone18;
    private final ModelPart bone22;
    private final ModelPart bone26;
    private final ModelPart bone27;
    private final ModelPart bone68;
    private final ModelPart bone69;
    private final ModelPart bone15;
    private final ModelPart bone16;
    private final ModelPart bone25;
    private final ModelPart bone17;
    private final ModelPart bone28;
    private final ModelPart bone29;
    private final ModelPart bone23;
    private final ModelPart lingzi;
    private final ModelPart bone34;
    private final ModelPart bone35;
    private final ModelPart bone31;
    private final ModelPart bone32;
    private final ModelPart bone30;
    private final ModelPart bone4;
    private final ModelPart bone33;
    private final ModelPart bone24;
    private final ModelPart epic;
    private final ModelPart li;
    private final ModelPart wai;
    private final ModelPart dress;
    private final ModelPart bone82;
    private final ModelPart bone83;
    private final ModelPart bone84;
    private final ModelPart bone85;
    private final ModelPart bone86;
    private final ModelPart bone87;
    private final ModelPart bone88;
    private final ModelPart bone89;
    private final ModelPart bone90;
    private final ModelPart bone91;
    private final ModelPart bone92;
    private final ModelPart bone93;
    private final ModelPart bone94;
    private final ModelPart bone95;
    private final ModelPart bone96;
    private final ModelPart bone97;
    private final ModelPart bone98;
    private final ModelPart bone99;
    private final ModelPart bone100;
    private final ModelPart bone101;
    private final ModelPart bone102;
    private final ModelPart bone103;
    private final ModelPart bone104;
    private final ModelPart bone105;
    private final ModelPart bone106;
    private final ModelPart bone107;
    private final ModelPart bone108;
    private final ModelPart bone109;
    private final ModelPart bone110;
    private final ModelPart bone111;
    private final ModelPart bone112;
    private final ModelPart bone113;
    private final ModelPart bone114;
    private final ModelPart bone115;
    private final ModelPart bone116;
    private final ModelPart bone117;
    private final ModelPart bone118;

    public MajoClothModel(ModelPart root) {
        super(root);
        
        // 获取自定义模型部件
        // 直接从标准身体部件获取自定义模型部件
        this.left = this.leftArm.getChild("left");
        this.bone56 = this.left.getChild("bone56");
        this.bone57 = this.bone56.getChild("bone57");
        this.bone58 = this.bone57.getChild("bone58");
        this.bone59 = this.bone58.getChild("bone59");
        this.bone60 = this.bone58.getChild("bone60");
        this.bone61 = this.bone56.getChild("bone61");
        this.bone62 = this.bone61.getChild("bone62");
        this.bone63 = this.bone62.getChild("bone63");
        this.bone64 = this.bone62.getChild("bone64");
        this.bone65 = this.bone56.getChild("bone65");
        this.bone66 = this.bone65.getChild("bone66");
        this.bone67 = this.bone66.getChild("bone67");
        this.bone70 = this.bone66.getChild("bone70");
        this.bone71 = this.bone56.getChild("bone71");
        this.bone72 = this.bone71.getChild("bone72");
        this.bone73 = this.bone72.getChild("bone73");
        this.bone74 = this.bone72.getChild("bone74");
        this.bone2 = this.left.getChild("bone2");
        this.bone19 = this.left.getChild("bone19");
        this.right = this.rightArm.getChild("right");
        this.bone39 = this.right.getChild("bone39");
        this.bone40 = this.bone39.getChild("bone40");
        this.bone41 = this.bone40.getChild("bone41");
        this.bone42 = this.bone41.getChild("bone42");
        this.bone43 = this.bone41.getChild("bone43");
        this.bone44 = this.bone39.getChild("bone44");
        this.bone45 = this.bone44.getChild("bone45");
        this.bone46 = this.bone45.getChild("bone46");
        this.bone47 = this.bone45.getChild("bone47");
        this.bone48 = this.bone39.getChild("bone48");
        this.bone49 = this.bone48.getChild("bone49");
        this.bone50 = this.bone49.getChild("bone50");
        this.bone51 = this.bone49.getChild("bone51");
        this.bone52 = this.bone39.getChild("bone52");
        this.bone53 = this.bone52.getChild("bone53");
        this.bone54 = this.bone53.getChild("bone54");
        this.bone55 = this.bone53.getChild("bone55");
        this.bone21 = this.right.getChild("bone21");
        this.bone20 = this.right.getChild("bone20");
        this.bigBody = this.body.getChild("bigBody");
        this.bone3 = this.bigBody.getChild("bone3");
        this.bone5 = this.bone3.getChild("bone5");
        this.bone6 = this.bone5.getChild("bone6");
        this.bone7 = this.bone6.getChild("bone7");
        this.bone8 = this.bone7.getChild("bone8");
        this.bone9 = this.bone7.getChild("bone9");
        this.bone = this.bone7.getChild("bone");
        this.bone10 = this.bone5.getChild("bone10");
        this.bone11 = this.bone10.getChild("bone11");
        this.bone12 = this.bone11.getChild("bone12");
        this.bone13 = this.bone11.getChild("bone13");
        this.sithide2 = this.bone5.getChild("sithide2");
        this.bone37 = this.sithide2.getChild("bone37");
        this.bone38 = this.bone37.getChild("bone38");
        this.bone75 = this.bone37.getChild("bone75");
        this.bone78 = this.bone37.getChild("bone78");
        this.sithide1 = this.bone5.getChild("sithide1");
        this.bone77 = this.sithide1.getChild("bone77");
        this.bone80 = this.bone77.getChild("bone80");
        this.bone119 = this.bone77.getChild("bone119");
        this.bone79 = this.bone77.getChild("bone79");
        this.bone14 = this.bone5.getChild("bone14");
        this.bone18 = this.bone5.getChild("bone18");
        this.bone22 = this.bone5.getChild("bone22");
        this.bone26 = this.bone5.getChild("bone26");
        this.bone27 = this.bone26.getChild("bone27");
        this.bone68 = this.bone27.getChild("bone68");
        this.bone69 = this.bone27.getChild("bone69");
        this.bone15 = this.bone3.getChild("bone15");
        this.bone16 = this.bone15.getChild("bone16");
        this.bone25 = this.bone15.getChild("bone25");
        this.bone17 = this.bone15.getChild("bone17");
        this.bone28 = this.bone15.getChild("bone28");
        this.bone29 = this.bone15.getChild("bone29");
        this.bone23 = this.bone15.getChild("bone23");
        this.lingzi = this.bigBody.getChild("lingzi");
        this.bone34 = this.lingzi.getChild("bone34");
        this.bone35 = this.lingzi.getChild("bone35");
        this.bone31 = this.lingzi.getChild("bone31");
        this.bone32 = this.lingzi.getChild("bone32");
        this.bone30 = this.lingzi.getChild("bone30");
        this.bone4 = this.lingzi.getChild("bone4");
        this.bone33 = this.lingzi.getChild("bone33");
        this.bone24 = this.bigBody.getChild("bone24");
        this.epic = this.body.getChild("epic");
        this.li = this.epic.getChild("li");
        this.wai = this.epic.getChild("wai");
        this.dress = this.body.getChild("dress");
        this.bone82 = this.dress.getChild("bone82");
        this.bone83 = this.bone82.getChild("bone83");
        this.bone84 = this.bone83.getChild("bone84");
        this.bone85 = this.bone84.getChild("bone85");
        this.bone86 = this.bone82.getChild("bone86");
        this.bone87 = this.bone86.getChild("bone87");
        this.bone88 = this.bone87.getChild("bone88");
        this.bone89 = this.bone82.getChild("bone89");
        this.bone90 = this.bone89.getChild("bone90");
        this.bone91 = this.bone90.getChild("bone91");
        this.bone92 = this.bone82.getChild("bone92");
        this.bone93 = this.bone92.getChild("bone93");
        this.bone94 = this.bone93.getChild("bone94");
        this.bone95 = this.bone82.getChild("bone95");
        this.bone96 = this.bone95.getChild("bone96");
        this.bone97 = this.bone96.getChild("bone97");
        this.bone98 = this.bone82.getChild("bone98");
        this.bone99 = this.bone98.getChild("bone99");
        this.bone100 = this.bone99.getChild("bone100");
        this.bone101 = this.bone82.getChild("bone101");
        this.bone102 = this.bone101.getChild("bone102");
        this.bone103 = this.bone102.getChild("bone103");
        this.bone104 = this.bone82.getChild("bone104");
        this.bone105 = this.bone104.getChild("bone105");
        this.bone106 = this.bone105.getChild("bone106");
        this.bone107 = this.bone82.getChild("bone107");
        this.bone108 = this.bone107.getChild("bone108");
        this.bone109 = this.bone108.getChild("bone109");
        this.bone110 = this.bone82.getChild("bone110");
        this.bone111 = this.bone110.getChild("bone111");
        this.bone112 = this.bone111.getChild("bone112");
        this.bone113 = this.bone82.getChild("bone113");
        this.bone114 = this.bone113.getChild("bone114");
        this.bone115 = this.bone114.getChild("bone115");
        this.bone116 = this.bone82.getChild("bone116");
        this.bone117 = this.bone116.getChild("bone117");
        this.bone118 = this.bone117.getChild("bone118");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 添加HumanoidModel所需的标准部件（隐藏的基础模型）
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

        // 添加自定义衣服模型部件
        // 将左手模型直接挂载到标准left_arm下
        PartDefinition left = left_arm.addOrReplaceChild("left", CubeListBuilder.create(), PartPose.offset(2.0F, 0.0F, 0.0F));

        PartDefinition bone56 = left.addOrReplaceChild("bone56", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.6098F, 11.2927F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition bone57 = bone56.addOrReplaceChild("bone57", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone58 = bone57.addOrReplaceChild("bone58", CubeListBuilder.create().texOffs(56, 75).addBox(-1.0F, -13.8263F, 0.0F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.9924F, 0.0873F, 0.0F, 0.0F));

        PartDefinition bone59 = bone58.addOrReplaceChild("bone59", CubeListBuilder.create().texOffs(74, 65).addBox(-2.0F, -14.0F, -0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, 0.0869F));

        PartDefinition bone60 = bone58.addOrReplaceChild("bone60", CubeListBuilder.create().texOffs(74, 39).addBox(0.0F, -14.0F, 0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, -0.0869F));

        PartDefinition bone61 = bone56.addOrReplaceChild("bone61", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone62 = bone61.addOrReplaceChild("bone62", CubeListBuilder.create().texOffs(74, 29).addBox(-1.0F, -13.8263F, 0.0F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.9924F, 0.0873F, 0.0F, 0.0F));

        PartDefinition bone63 = bone62.addOrReplaceChild("bone63", CubeListBuilder.create().texOffs(52, 73).addBox(-2.0F, -14.0F, -0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, 0.0869F));

        PartDefinition bone64 = bone62.addOrReplaceChild("bone64", CubeListBuilder.create().texOffs(48, 73).addBox(0.0F, -14.0F, 0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, -0.0869F));

        PartDefinition bone65 = bone56.addOrReplaceChild("bone65", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -3.1416F, 0.0F));

        PartDefinition bone66 = bone65.addOrReplaceChild("bone66", CubeListBuilder.create().texOffs(44, 73).addBox(-1.0F, -13.8263F, 0.0F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.9924F, 0.0873F, 0.0F, 0.0F));

        PartDefinition bone67 = bone66.addOrReplaceChild("bone67", CubeListBuilder.create().texOffs(72, 55).addBox(-2.0F, -14.0F, -0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, 0.0869F));

        PartDefinition bone70 = bone66.addOrReplaceChild("bone70", CubeListBuilder.create().texOffs(72, 19).addBox(0.0F, -14.0F, 0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, -0.0869F));

        PartDefinition bone71 = bone56.addOrReplaceChild("bone71", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -4.7124F, 0.0F));

        PartDefinition bone72 = bone71.addOrReplaceChild("bone72", CubeListBuilder.create().texOffs(72, 9).addBox(-1.0F, -13.8263F, 0.0F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.9924F, 0.0873F, 0.0F, 0.0F));

        PartDefinition bone73 = bone72.addOrReplaceChild("bone73", CubeListBuilder.create().texOffs(70, 66).addBox(-2.0F, -14.0F, -0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, 0.0869F));

        PartDefinition bone74 = bone72.addOrReplaceChild("bone74", CubeListBuilder.create().texOffs(40, 68).addBox(0.0F, -14.0F, 0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, -0.0869F));

        PartDefinition bone2 = left.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(64, 0).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.1951F)), PartPose.offset(0.1951F, 0.0F, 0.0F));

        PartDefinition bone19 = left.addOrReplaceChild("bone19", CubeListBuilder.create().texOffs(20, 36).addBox(-3.5854F, 0.0F, -2.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.4878F)), PartPose.offsetAndRotation(0.1951F, 7.0F, 0.0F, 0.0F, 0.0F, 0.1309F));

        // 将右手模型直接挂载到标准right_arm下
        PartDefinition right = right_arm.addOrReplaceChild("right", CubeListBuilder.create(), PartPose.offset(-2.0F, 0.0F, 0.0F));

        PartDefinition bone39 = right.addOrReplaceChild("bone39", CubeListBuilder.create(), PartPose.offsetAndRotation(1.7805F, 9.2927F, 0.0F, 0.0F, 0.0F, -0.0873F));

        PartDefinition bone40 = bone39.addOrReplaceChild("bone40", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone41 = bone40.addOrReplaceChild("bone41", CubeListBuilder.create().texOffs(36, 68).addBox(-1.0F, -11.8263F, 0.0F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.9924F, 0.0873F, 0.0F, 0.0F));

        PartDefinition bone42 = bone41.addOrReplaceChild("bone42", CubeListBuilder.create().texOffs(32, 68).addBox(-2.0F, -12.0F, -0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, 0.0869F));

        PartDefinition bone43 = bone41.addOrReplaceChild("bone43", CubeListBuilder.create().texOffs(28, 68).addBox(0.0F, -12.0F, 0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, -0.0869F));

        PartDefinition bone44 = bone39.addOrReplaceChild("bone44", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone45 = bone44.addOrReplaceChild("bone45", CubeListBuilder.create().texOffs(24, 68).addBox(-1.0F, -11.8263F, 0.0F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.9924F, 0.0873F, 0.0F, 0.0F));

        PartDefinition bone46 = bone45.addOrReplaceChild("bone46", CubeListBuilder.create().texOffs(20, 68).addBox(-2.0F, -12.0F, -0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, 0.0869F));

        PartDefinition bone47 = bone45.addOrReplaceChild("bone47", CubeListBuilder.create().texOffs(16, 68).addBox(0.0F, -12.0F, 0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, -0.0869F));

        PartDefinition bone48 = bone39.addOrReplaceChild("bone48", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.0976F, 2.0F, 0.0F, 0.0F, -3.1416F, 0.0F));

        PartDefinition bone49 = bone48.addOrReplaceChild("bone49", CubeListBuilder.create().texOffs(12, 68).addBox(-1.0F, -13.8263F, 0.0F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0615F, 0.0F, 2.9924F, 0.0873F, 0.0F, 0.0F));

        PartDefinition bone50 = bone49.addOrReplaceChild("bone50", CubeListBuilder.create().texOffs(8, 68).addBox(-2.0615F, -14.0F, -0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, 0.0869F));

        PartDefinition bone51 = bone49.addOrReplaceChild("bone51", CubeListBuilder.create().texOffs(4, 68).addBox(0.0F, -14.0F, 0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, -0.0869F));

        PartDefinition bone52 = bone39.addOrReplaceChild("bone52", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -4.7124F, 0.0F));

        PartDefinition bone53 = bone52.addOrReplaceChild("bone53", CubeListBuilder.create().texOffs(0, 68).addBox(-1.0F, -11.8263F, 0.0F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.7973F, 0.0873F, 0.0F, 0.0F));

        PartDefinition bone54 = bone53.addOrReplaceChild("bone54", CubeListBuilder.create().texOffs(66, 66).addBox(-2.0F, -12.0F, -0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, 0.0869F));

        PartDefinition bone55 = bone53.addOrReplaceChild("bone55", CubeListBuilder.create().texOffs(62, 66).addBox(0.0F, -12.0F, 0.001F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.1737F, 0.0F, 0.0F, 0.0F, -0.0869F));

        PartDefinition bone21 = right.addOrReplaceChild("bone21", CubeListBuilder.create().texOffs(20, 36).addBox(-3.878F, 0.0F, -2.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.4878F)), PartPose.offsetAndRotation(3.3659F, 6.3171F, -0.0976F, 0.0F, 0.0F, -0.1309F));

        PartDefinition bone20 = right.addOrReplaceChild("bone20", CubeListBuilder.create().texOffs(56, 57).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.1951F)), PartPose.offset(2.0976F, 0.0F, 0.0F));

        // 将身体模型挂载到标准body下
        PartDefinition bigBody = body.addOrReplaceChild("bigBody", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone3 = bigBody.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0732F));

        PartDefinition bone5 = bone3.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 11.0F, 0.2927F, 0.1309F, 0.0F, 0.0F));

        PartDefinition bone6 = bone5.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone7 = bone6.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(54, 0).addBox(-2.0F, -2.7022F, 1.1798F, 4.0F, 14.0F, 1.0F, new CubeDeformation(-0.1951F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.9777F, 0.2618F, 0.0F, 0.0F));

        PartDefinition bone8 = bone7.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(56, 36).addBox(-2.0F, -8.0F, 1.1788F, 3.0F, 20.0F, 1.0F, new CubeDeformation(-0.1951F)), PartPose.offsetAndRotation(-1.0F, 0.2978F, 0.0F, 0.0F, 0.0F, 0.1495F));

        PartDefinition bone9 = bone7.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(56, 36).addBox(-1.0F, -8.0F, 1.1808F, 3.0F, 20.0F, 1.0F, new CubeDeformation(-0.1951F)), PartPose.offsetAndRotation(1.0F, 0.2978F, 0.0F, 0.0F, 0.0F, -0.1495F));

        PartDefinition bone = bone7.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone10 = bone5.addOrReplaceChild("bone10", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition bone11 = bone10.addOrReplaceChild("bone11", CubeListBuilder.create().texOffs(46, 52).addBox(-2.0F, -7.7022F, 1.1798F, 4.0F, 20.0F, 1.0F, new CubeDeformation(-0.1951F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.9777F, 0.2618F, 0.0F, 0.0F));

        PartDefinition bone12 = bone11.addOrReplaceChild("bone12", CubeListBuilder.create().texOffs(56, 36).addBox(-2.0F, -8.0F, 1.1788F, 3.0F, 20.0F, 1.0F, new CubeDeformation(-0.1951F)), PartPose.offsetAndRotation(-1.0F, 0.2978F, 0.0F, 0.0F, 0.0F, 0.1495F));

        PartDefinition bone13 = bone11.addOrReplaceChild("bone13", CubeListBuilder.create().texOffs(56, 36).addBox(-1.0F, -8.0F, 1.1808F, 3.0F, 20.0F, 1.0F, new CubeDeformation(-0.1951F)), PartPose.offsetAndRotation(1.0F, 0.2978F, 0.0F, 0.0F, 0.0F, -0.1495F));

        PartDefinition sithide2 = bone5.addOrReplaceChild("sithide2", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2927F, 0.3902F, 0.7805F, 0.2618F, -1.9199F, -0.1745F));

        PartDefinition bone37 = sithide2.addOrReplaceChild("bone37", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 2.9777F, 0.2618F, 0.0F, 0.0F));

        PartDefinition bone38 = bone37.addOrReplaceChild("bone38", CubeListBuilder.create().texOffs(56, 36).addBox(-2.0F, -8.0F, 1.1788F, 3.0F, 20.0F, 1.0F, new CubeDeformation(-0.1951F)), PartPose.offsetAndRotation(-1.0F, 0.2978F, 0.0F, 0.0F, 0.0F, 0.1495F));

        PartDefinition bone75 = bone37.addOrReplaceChild("bone75", CubeListBuilder.create().texOffs(46, 52).addBox(-2.0F, -7.7022F, 1.1798F, 4.0F, 20.0F, 1.0F, new CubeDeformation(-0.1951F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone78 = bone37.addOrReplaceChild("bone78", CubeListBuilder.create().texOffs(46, 52).addBox(-1.8769F, -7.7022F, 1.1798F, 4.0F, 20.0F, 1.0F, new CubeDeformation(-0.1951F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.0976F, 0.0F, 0.0F, -0.0873F));

        PartDefinition sithide1 = bone5.addOrReplaceChild("sithide1", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.3902F, 0.0F, -1.3659F, 0.2618F, 1.9199F, 0.1745F));

        PartDefinition bone77 = sithide1.addOrReplaceChild("bone77", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.3415F, 0.0F, 2.9777F, 0.2618F, 0.0F, 0.0F));

        PartDefinition bone80 = bone77.addOrReplaceChild("bone80", CubeListBuilder.create().texOffs(46, 52).addBox(-4.7317F, -20.7022F, 3.1575F, 4.0F, 20.0F, 1.0F, new CubeDeformation(-0.1951F)), PartPose.offset(2.7317F, 13.0F, -1.9777F));

        PartDefinition bone119 = bone77.addOrReplaceChild("bone119", CubeListBuilder.create().texOffs(46, 52).addBox(-4.8293F, -20.6046F, 3.06F, 4.0F, 20.0F, 1.0F, new CubeDeformation(-0.1951F)), PartPose.offsetAndRotation(2.0488F, 13.0F, -1.9777F, 0.0F, 0.0F, 0.0436F));

        PartDefinition bone79 = bone77.addOrReplaceChild("bone79", CubeListBuilder.create().texOffs(56, 36).addBox(-1.0F, -8.0F, 1.1808F, 3.0F, 20.0F, 1.0F, new CubeDeformation(-0.1951F)), PartPose.offsetAndRotation(1.0F, 0.2978F, 0.0F, 0.0F, 0.0F, -0.1495F));

        PartDefinition bone14 = bone5.addOrReplaceChild("bone14", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        PartDefinition bone18 = bone5.addOrReplaceChild("bone18", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -3.1416F, 0.0F));

        PartDefinition bone22 = bone5.addOrReplaceChild("bone22", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -4.1888F, 0.0F));

        PartDefinition bone26 = bone5.addOrReplaceChild("bone26", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -5.236F, 0.0F));

        PartDefinition bone27 = bone26.addOrReplaceChild("bone27", CubeListBuilder.create().texOffs(46, 52).addBox(-2.0F, -7.7022F, 1.1798F, 4.0F, 20.0F, 1.0F, new CubeDeformation(-0.1951F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.9777F, 0.2618F, 0.0F, 0.0F));

        PartDefinition bone68 = bone27.addOrReplaceChild("bone68", CubeListBuilder.create().texOffs(56, 36).addBox(-2.0F, -8.0F, 1.1788F, 3.0F, 20.0F, 1.0F, new CubeDeformation(-0.1951F)), PartPose.offsetAndRotation(-1.0F, 0.2978F, 0.0F, 0.0F, 0.0F, 0.1495F));

        PartDefinition bone69 = bone27.addOrReplaceChild("bone69", CubeListBuilder.create().texOffs(56, 36).addBox(-1.0F, -8.0F, 1.1808F, 3.0F, 20.0F, 1.0F, new CubeDeformation(-0.1951F)), PartPose.offsetAndRotation(1.0F, 0.2978F, 0.0F, 0.0F, 0.0F, -0.1495F));

        PartDefinition bone15 = bone3.addOrReplaceChild("bone15", CubeListBuilder.create(), PartPose.offset(0.0F, 4.7805F, -0.5854F));

        PartDefinition bone16 = bone15.addOrReplaceChild("bone16", CubeListBuilder.create().texOffs(64, 36).addBox(-2.5F, -5.0F, 4.3301F, 5.0F, 14.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone25 = bone15.addOrReplaceChild("bone25", CubeListBuilder.create().texOffs(64, 50).addBox(-2.5F, -2.5F, 0.0F, 5.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.939F, 1.8378F, 1.5708F, 0.0F, 0.0F));

        PartDefinition bone17 = bone15.addOrReplaceChild("bone17", CubeListBuilder.create().texOffs(56, 66).addBox(-0.5F, -5.0F, 4.3301F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3659F, 0.0F, -0.3902F, 0.0F, -0.6981F, 0.0F));

        PartDefinition bone28 = bone15.addOrReplaceChild("bone28", CubeListBuilder.create().texOffs(4, 16).addBox(0.5F, 2.0F, 4.3301F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3659F, -0.6342F, 0.0F, 1.5708F, -0.6981F, 0.0F));

        PartDefinition bone29 = bone15.addOrReplaceChild("bone29", CubeListBuilder.create().texOffs(0, 16).addBox(0.5F, 2.0F, 4.3301F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5122F, -0.6342F, 1.6098F, 1.5708F, 0.6981F, 0.0F));

        PartDefinition bone23 = bone15.addOrReplaceChild("bone23", CubeListBuilder.create().texOffs(64, 9).addBox(-2.5F, -5.0F, 4.3301F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.4634F, 0.0F, -0.4878F, 0.0F, 0.6981F, 0.0F));

        PartDefinition lingzi = bigBody.addOrReplaceChild("lingzi", CubeListBuilder.create(), PartPose.offset(0.0976F, -1.3658F, 0.2567F));

        PartDefinition bone34 = lingzi.addOrReplaceChild("bone34", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -0.3781F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2926F)), PartPose.offsetAndRotation(1.1F, 1.4231F, -2.2272F, -0.3054F, 0.0F, 0.7854F));

        PartDefinition bone35 = lingzi.addOrReplaceChild("bone35", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -0.3781F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2926F)), PartPose.offsetAndRotation(-1.339F, 1.4231F, -2.2272F, -0.3054F, 0.0F, -0.7854F));

        PartDefinition bone31 = lingzi.addOrReplaceChild("bone31", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -0.378F, -0.4024F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.0615F)), PartPose.offsetAndRotation(0.378F, 3.1098F, -2.5244F, 0.0F, 0.0F, -0.3491F));

        PartDefinition bone32 = lingzi.addOrReplaceChild("bone32", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -0.3781F, -0.3769F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.0615F)), PartPose.offsetAndRotation(-0.4568F, 3.0024F, -2.5244F, 0.0F, 0.0F, 0.3491F));

        PartDefinition bone30 = lingzi.addOrReplaceChild("bone30", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.3781F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1464F)), PartPose.offsetAndRotation(-0.5F, 2.5F, -2.5244F, 0.0F, 0.0F, 1.5708F));

        PartDefinition bone4 = lingzi.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5976F, -0.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.0976F)), PartPose.offsetAndRotation(1.0378F, 4.8384F, -2.5244F, 0.0F, 0.0F, -0.1745F));

        PartDefinition bone33 = lingzi.addOrReplaceChild("bone33", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -0.5F, -0.4385F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.0976F)), PartPose.offsetAndRotation(-1.0644F, 4.7458F, -2.5244F, 0.0F, 0.0F, 0.1745F));

        PartDefinition bone24 = bigBody.addOrReplaceChild("bone24", CubeListBuilder.create().texOffs(11, 3).addBox(-1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        // 将epic部件挂载到标准body下
        PartDefinition epic = body.addOrReplaceChild("epic", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition li = epic.addOrReplaceChild("li", CubeListBuilder.create().texOffs(32, 36).addBox(-4.0F, -24.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.3476F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition wai = epic.addOrReplaceChild("wai", CubeListBuilder.create().texOffs(24, 52).addBox(-3.5122F, -24.0F, -2.0F, 7.0F, 12.0F, 4.0F, new CubeDeformation(0.8754F))
        .texOffs(24, 52).addBox(-3.5122F, -24.0F, -2.0F, 7.0F, 12.0F, 4.0F, new CubeDeformation(0.8754F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        // 将dress部件挂载到标准body下
        PartDefinition dress = body.addOrReplaceChild("dress", CubeListBuilder.create(), PartPose.offset(0.0F, 10.0F, 0.0F));

        PartDefinition bone82 = dress.addOrReplaceChild("bone82", CubeListBuilder.create(), PartPose.offset(0.0F, 7.0F, 0.0F));

        PartDefinition bone83 = bone82.addOrReplaceChild("bone83", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone84 = bone83.addOrReplaceChild("bone84", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 5.2779F, 0.4014F, 0.0F, 0.0F));

        PartDefinition bone85 = bone84.addOrReplaceChild("bone85", CubeListBuilder.create().texOffs(0, 36).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.2195F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone86 = bone82.addOrReplaceChild("bone86", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition bone87 = bone86.addOrReplaceChild("bone87", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 5.2779F, 0.4014F, 0.0F, 0.0F));

        PartDefinition bone88 = bone87.addOrReplaceChild("bone88", CubeListBuilder.create().texOffs(0, 36).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.2195F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone89 = bone82.addOrReplaceChild("bone89", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition bone90 = bone89.addOrReplaceChild("bone90", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 5.2779F, 0.4014F, 0.0F, 0.0F));

        PartDefinition bone91 = bone90.addOrReplaceChild("bone91", CubeListBuilder.create().texOffs(0, 36).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.2195F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone92 = bone82.addOrReplaceChild("bone92", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone93 = bone92.addOrReplaceChild("bone93", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 5.2779F, 0.4014F, 0.0F, 0.0F));

        PartDefinition bone94 = bone93.addOrReplaceChild("bone94", CubeListBuilder.create().texOffs(0, 36).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.2195F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone95 = bone82.addOrReplaceChild("bone95", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        PartDefinition bone96 = bone95.addOrReplaceChild("bone96", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 5.2779F, 0.4014F, 0.0F, 0.0F));

        PartDefinition bone97 = bone96.addOrReplaceChild("bone97", CubeListBuilder.create().texOffs(0, 36).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.2195F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone98 = bone82.addOrReplaceChild("bone98", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.618F, 0.0F));

        PartDefinition bone99 = bone98.addOrReplaceChild("bone99", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 5.2779F, 0.4014F, 0.0F, 0.0F));

        PartDefinition bone100 = bone99.addOrReplaceChild("bone100", CubeListBuilder.create().texOffs(0, 36).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.2195F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone101 = bone82.addOrReplaceChild("bone101", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -3.1416F, 0.0F));

        PartDefinition bone102 = bone101.addOrReplaceChild("bone102", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 5.2779F, 0.4014F, 0.0F, 0.0F));

        PartDefinition bone103 = bone102.addOrReplaceChild("bone103", CubeListBuilder.create().texOffs(0, 36).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.2195F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone104 = bone82.addOrReplaceChild("bone104", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.665F, 0.0F));

        PartDefinition bone105 = bone104.addOrReplaceChild("bone105", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 5.2779F, 0.4014F, 0.0F, 0.0F));

        PartDefinition bone106 = bone105.addOrReplaceChild("bone106", CubeListBuilder.create().texOffs(0, 36).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.2195F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone107 = bone82.addOrReplaceChild("bone107", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition bone108 = bone107.addOrReplaceChild("bone108", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 5.2779F, 0.4014F, 0.0F, 0.0F));

        PartDefinition bone109 = bone108.addOrReplaceChild("bone109", CubeListBuilder.create().texOffs(0, 36).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.2195F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone110 = bone82.addOrReplaceChild("bone110", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.618F, 0.0F));

        PartDefinition bone111 = bone110.addOrReplaceChild("bone111", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 5.2779F, 0.4014F, 0.0F, 0.0F));

        PartDefinition bone112 = bone111.addOrReplaceChild("bone112", CubeListBuilder.create().texOffs(0, 36).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.2195F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone113 = bone82.addOrReplaceChild("bone113", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

        PartDefinition bone114 = bone113.addOrReplaceChild("bone114", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 5.2779F, 0.4014F, 0.0F, 0.0F));

        PartDefinition bone115 = bone114.addOrReplaceChild("bone115", CubeListBuilder.create().texOffs(0, 36).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.2195F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone116 = bone82.addOrReplaceChild("bone116", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition bone117 = bone116.addOrReplaceChild("bone117", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 5.2779F, 0.4014F, 0.0F, 0.0F));

        PartDefinition bone118 = bone117.addOrReplaceChild("bone118", CubeListBuilder.create().texOffs(0, 36).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.2195F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        // 衣服模型跟随身体动画
    }
}
package com.magicbroom.examplemod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * 魔女扫帚实体渲染器
 * 使用MajoBroomModel来渲染扫帚实体
 */
public class MajoBroomEntityRenderer extends EntityRenderer<MajoBroomEntity> {
    
    private static final ResourceLocation TEXTURE_LOCATION = 
        ResourceLocation.fromNamespaceAndPath(AshenWitchBroom.MODID, "textures/entity/majo_broom.png");
    
    private final MajoBroomModel<MajoBroomEntity> model;
    
    public MajoBroomEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MajoBroomModel<>(context.bakeLayer(MajoBroomModel.LAYER_LOCATION));
    }
    
    @Override
    public void render(MajoBroomEntity entity, float entityYaw, float partialTicks, 
                      PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        
        poseStack.pushPose();
        
        // 【关键修改】添加轻微的悬浮动画并向上平移模型以修正位置
        float time = entity.tickCount + partialTicks;
        float hoverOffset = Mth.sin(time * 0.1F) * 0.05F;
        // 向上移动1.0个单位，并应用悬浮效果
        poseStack.translate(0.0D, 1.5D + hoverOffset, 0.0D);
        
        // 设置扫帚的旋转（可以根据需要调整）
        // 沿Y轴旋转，使其朝向正确
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        // 【关键修改】沿X轴旋转180度，修正模型上下颠倒的问题
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        
        // 缩放扫帚（如果需要）
        poseStack.scale(1.0F, 1.0F, 1.0F);
        
        // 渲染模型
        var vertexConsumer = bufferSource.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, 
                                 net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 
                                 0xFFFFFFFF);
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }
    
    @Override
    public ResourceLocation getTextureLocation(MajoBroomEntity entity) {
        return TEXTURE_LOCATION;
    }
}
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.EventSystem;
import exhibition.event.impl.EventNametagRender;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventRenderEntity;
import exhibition.module.Module;
import exhibition.module.ModuleManager;
import exhibition.module.impl.combat.Killaura;
import exhibition.module.impl.gta.AntiAim;
import exhibition.module.impl.other.Animations;

import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public abstract class RendererLivingEntity extends Render {
   private static final Logger logger = LogManager.getLogger();
   private static final DynamicTexture field_177096_e = new DynamicTexture(16, 16);
   protected ModelBase mainModel;
   private FloatBuffer field_177095_g = GLAllocation.createDirectFloatBuffer(4);
   List field_177097_h = Lists.newArrayList();
   private static boolean field_177098_i = false;
   public static boolean renderLayers = true;
   public static boolean ignoreChams = false;
   private static float pitchy = 0.0F;
   public RendererLivingEntity(RenderManager p_i46156_1_, ModelBase p_i46156_2_, float p_i46156_3_) {
      super(p_i46156_1_);
      this.mainModel = p_i46156_2_;
      this.shadowSize = p_i46156_3_;
   }
   public static void SetPitchY(float y) {
       pitchy = y;
    }
   public static float getPitchY() {
       return pitchy;
    }
   protected boolean addLayer(LayerRenderer p_177094_1_) {
      return this.field_177097_h.add(p_177094_1_);
   }

   protected boolean func_177089_b(LayerRenderer p_177089_1_) {
      return this.field_177097_h.remove(p_177089_1_);
   }

   public ModelBase getMainModel() {
      return this.mainModel;
   }

   protected float interpolateRotation(float p_77034_1_, float p_77034_2_, float p_77034_3_) {
      float var4;
      for(var4 = p_77034_2_ - p_77034_1_; var4 < -180.0F; var4 += 360.0F) {
         ;
      }

      while(var4 >= 180.0F) {
         var4 -= 360.0F;
      }

      return p_77034_1_ + p_77034_3_ * var4;
   }

   public void func_82422_c() {
   }

   public void doRender(EntityLivingBase p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {
      GlStateManager.pushMatrix();
      GlStateManager.disableCull();
      this.mainModel.swingProgress = this.getSwingProgress(p_76986_1_, p_76986_9_);
      this.mainModel.isRiding = p_76986_1_.isRiding();
      this.mainModel.isChild = p_76986_1_.isChild();
      EventRenderEntity em = (EventRenderEntity)EventSystem.getInstance(EventRenderEntity.class);

      try {
         float var10 = this.interpolateRotation(p_76986_1_.prevRenderYawOffset, p_76986_1_.renderYawOffset, p_76986_9_);
         float var11 = this.interpolateRotation(p_76986_1_.prevRotationYawHead, p_76986_1_.rotationYawHead, p_76986_9_);
         float var12 = var11 - var10;
         float var14;
         if (p_76986_1_.isRiding() && p_76986_1_.ridingEntity instanceof EntityLivingBase) {
            EntityLivingBase var13 = (EntityLivingBase)p_76986_1_.ridingEntity;
            var10 = this.interpolateRotation(var13.prevRenderYawOffset, var13.renderYawOffset, p_76986_9_);
            var12 = var11 - var10;
            var14 = MathHelper.wrapAngleTo180_float(var12);
            if (var14 < -85.0F) {
               var14 = -85.0F;
            }

            if (var14 >= 85.0F) {
               var14 = 85.0F;
            }

            var10 = var11 - var14;
            if (var14 * var14 > 2500.0F) {
               var10 += var14 * 0.2F;
            }
         }
        
		
		float var20;
		Killaura Killaura1 = (Killaura)Client.getModuleManager().get(Killaura.class);
        
		if(Killaura1.isEnabled()&& (Killaura.target != null || !Killaura.loaded.isEmpty())) {
        	 var20 = getPitchY();
         }else {
		 var20 = Minecraft.getMinecraft().gameSettings.thirdPersonView != 0 && ((Module)Client.getModuleManager().get(AntiAim.class)).isEnabled() && p_76986_1_ == Minecraft.getMinecraft().thePlayer ? p_76986_1_.prevRotationPitch + ((AntiAim.rotationPitch != 0.0F ? AntiAim.rotationPitch : p_76986_1_.rotationPitch) - p_76986_1_.prevRotationPitch) : p_76986_1_.prevRotationPitch + (p_76986_1_.rotationPitch - p_76986_1_.prevRotationPitch) * p_76986_9_;
         }this.renderLivingAt(p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_);
         var14 = this.handleRotationFloat(p_76986_1_, p_76986_9_);
         this.rotateCorpse(p_76986_1_, var14, var10, p_76986_9_);
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(-1.0F, -1.0F, 1.0F);
         this.preRenderCallback(p_76986_1_, p_76986_9_);
         float var15 = 0.0625F;
         GlStateManager.translate(0.0F, -1.5078125F, 0.0F);
         float var16 = p_76986_1_.prevLimbSwingAmount + (p_76986_1_.limbSwingAmount - p_76986_1_.prevLimbSwingAmount) * p_76986_9_;
         float var17 = p_76986_1_.limbSwing - p_76986_1_.limbSwingAmount * (1.0F - p_76986_9_);
         if (p_76986_1_ instanceof EntityPlayer && !ignoreChams) {
            em.fire(p_76986_1_, true, var17, var16, var14, var12, var20, var10, 0.0625F);
            if (em.isCancelled()) {
               return;
            }
         }

         if (p_76986_1_.isChild()) {
            var17 *= 3.0F;
         }

         if (var16 > 1.0F) {
            var16 = 1.0F;
         }

         GlStateManager.enableAlpha();
         this.mainModel.setLivingAnimations(p_76986_1_, var17, var16, p_76986_9_);
         this.mainModel.setRotationAngles(var17, var16, var14, var12, var20, 0.0625F, p_76986_1_);
         boolean var18;
         if (field_177098_i) {
            var18 = this.func_177088_c(p_76986_1_);
            this.renderModel(p_76986_1_, var17, var16, var14, var12, var20, 0.0625F);
            if (var18) {
               this.func_180565_e();
            }
         } else {
            var18 = this.func_177090_c(p_76986_1_, p_76986_9_);
            this.renderModel(p_76986_1_, var17, var16, var14, var12, var20, 0.0625F);
            if (var18) {
               this.func_177091_f();
            }

            GlStateManager.depthMask(true);
            if (!(p_76986_1_ instanceof EntityPlayer) || !((EntityPlayer)p_76986_1_).func_175149_v()) {
               this.func_177093_a(p_76986_1_, var17, var16, p_76986_9_, var14, var12, var20, 0.0625F);
            }
         }

         GlStateManager.disableRescaleNormal();
      } catch (Exception var20) {
         logger.error("Couldn't render entity", var20);
      }

      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.enableTextures();
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      GlStateManager.enableCull();
      GlStateManager.popMatrix();
      if (!field_177098_i) {
         super.doRender(p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
      }

      if (!ignoreChams) {
         em.fire(p_76986_1_, false);
      }

   }

   protected boolean func_177088_c(EntityLivingBase p_177088_1_) {
      int var2 = 16777215;
      if (p_177088_1_ instanceof EntityPlayer) {
         ScorePlayerTeam var3 = (ScorePlayerTeam)p_177088_1_.getTeam();
         if (var3 != null) {
            String var4 = FontRenderer.getFormatFromString(var3.getColorPrefix());
            if (var4.length() >= 2) {
               var2 = this.getFontRendererFromRenderManager().func_175064_b(var4.charAt(1));
            }
         }
      }

      float var6 = (float)(var2 >> 16 & 255) / 255.0F;
      float var7 = (float)(var2 >> 8 & 255) / 255.0F;
      float var5 = (float)(var2 & 255) / 255.0F;
      GlStateManager.disableLighting();
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      GlStateManager.color(var6, var7, var5, 1.0F);
      GlStateManager.disableTextures();
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.disableTextures();
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      return true;
   }

   protected void func_180565_e() {
      GlStateManager.enableLighting();
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      GlStateManager.enableTextures();
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.enableTextures();
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
   }

   public void renderModel(EntityLivingBase p_77036_1_, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float p_77036_7_) {
      boolean var8 = !p_77036_1_.isInvisible();
      boolean var9 = !var8 && !p_77036_1_.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer);
      if (var8 || var9) {
         if (!this.bindEntityTexture(p_77036_1_)) {
            return;
         }

         if (var9) {
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.15F);
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.alphaFunc(516, 0.003921569F);
         }

         this.mainModel.render(p_77036_1_, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
         if (var9) {
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);
         }
      }

   }

   protected boolean func_177090_c(EntityLivingBase p_177090_1_, float p_177090_2_) {
      return this.func_177092_a(p_177090_1_, p_177090_2_, true);
   }

   protected boolean func_177092_a(EntityLivingBase p_177092_1_, float p_177092_2_, boolean p_177092_3_) {
      float var4 = p_177092_1_.getBrightness(p_177092_2_);
      int var5 = this.getColorMultiplier(p_177092_1_, var4, p_177092_2_);
      boolean var6 = (var5 >> 24 & 255) > 0;
      boolean var7 = p_177092_1_.hurtTime > 0 || p_177092_1_.deathTime > 0;
      if (!var6 && !var7) {
         return false;
      } else if (!var6 && !p_177092_3_) {
         return false;
      } else {
         GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
         GlStateManager.enableTextures();
         GL11.glTexEnvi(8960, 8704, OpenGlHelper.field_176095_s);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176099_x, 8448);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176098_y, OpenGlHelper.defaultTexUnit);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176097_z, OpenGlHelper.field_176093_u);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176081_B, 768);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176082_C, 768);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176077_E, 7681);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176078_F, OpenGlHelper.defaultTexUnit);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176085_I, 770);
         GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
         GlStateManager.enableTextures();
         GL11.glTexEnvi(8960, 8704, OpenGlHelper.field_176095_s);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176099_x, OpenGlHelper.field_176094_t);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176098_y, OpenGlHelper.field_176092_v);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176097_z, OpenGlHelper.field_176091_w);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176080_A, OpenGlHelper.field_176092_v);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176081_B, 768);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176082_C, 768);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176076_D, 770);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176077_E, 7681);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176078_F, OpenGlHelper.field_176091_w);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176085_I, 770);
         this.field_177095_g.position(0);
         if (var7) {
            this.field_177095_g.put(1.0F);
            this.field_177095_g.put(0.0F);
            this.field_177095_g.put(0.0F);
            this.field_177095_g.put(0.3F);
         } else {
            float var8 = (float)(var5 >> 24 & 255) / 255.0F;
            float var9 = (float)(var5 >> 16 & 255) / 255.0F;
            float var10 = (float)(var5 >> 8 & 255) / 255.0F;
            float var11 = (float)(var5 & 255) / 255.0F;
            this.field_177095_g.put(var9);
            this.field_177095_g.put(var10);
            this.field_177095_g.put(var11);
            this.field_177095_g.put(1.0F - var8);
         }

         this.field_177095_g.flip();
         GL11.glTexEnv(8960, 8705, this.field_177095_g);
         GlStateManager.setActiveTexture(OpenGlHelper.field_176096_r);
         GlStateManager.enableTextures();
         GlStateManager.func_179144_i(field_177096_e.getGlTextureId());
         GL11.glTexEnvi(8960, 8704, OpenGlHelper.field_176095_s);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176099_x, 8448);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176098_y, OpenGlHelper.field_176091_w);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176097_z, OpenGlHelper.lightmapTexUnit);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176081_B, 768);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176082_C, 768);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176077_E, 7681);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176078_F, OpenGlHelper.field_176091_w);
         GL11.glTexEnvi(8960, OpenGlHelper.field_176085_I, 770);
         GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
         return true;
      }
   }

   protected void func_177091_f() {
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      GlStateManager.enableTextures();
      GL11.glTexEnvi(8960, 8704, OpenGlHelper.field_176095_s);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176099_x, 8448);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176098_y, OpenGlHelper.defaultTexUnit);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176097_z, OpenGlHelper.field_176093_u);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176081_B, 768);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176082_C, 768);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176077_E, 8448);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176078_F, OpenGlHelper.defaultTexUnit);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176079_G, OpenGlHelper.field_176093_u);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176085_I, 770);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176086_J, 770);
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GL11.glTexEnvi(8960, 8704, OpenGlHelper.field_176095_s);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176099_x, 8448);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176081_B, 768);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176082_C, 768);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176098_y, 5890);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176097_z, OpenGlHelper.field_176091_w);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176077_E, 8448);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176085_I, 770);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176078_F, 5890);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.setActiveTexture(OpenGlHelper.field_176096_r);
      GlStateManager.disableTextures();
      GlStateManager.func_179144_i(0);
      GL11.glTexEnvi(8960, 8704, OpenGlHelper.field_176095_s);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176099_x, 8448);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176081_B, 768);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176082_C, 768);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176098_y, 5890);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176097_z, OpenGlHelper.field_176091_w);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176077_E, 8448);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176085_I, 770);
      GL11.glTexEnvi(8960, OpenGlHelper.field_176078_F, 5890);
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
   }

   protected void renderLivingAt(EntityLivingBase p_77039_1_, double p_77039_2_, double p_77039_4_, double p_77039_6_) {
      GlStateManager.translate((float)p_77039_2_, (float)p_77039_4_, (float)p_77039_6_);
   }

   protected void rotateCorpse(EntityLivingBase p_77043_1_, float p_77043_2_, float p_77043_3_, float p_77043_4_) {
      GlStateManager.rotate(180.0F - p_77043_3_, 0.0F, 1.0F, 0.0F);
      if (p_77043_1_.deathTime > 0) {
         float var5 = ((float)p_77043_1_.deathTime + p_77043_4_ - 1.0F) / 20.0F * 1.6F;
         var5 = MathHelper.sqrt_float(var5);
         if (var5 > 1.0F) {
            var5 = 1.0F;
         }

         GlStateManager.rotate(var5 * this.getDeathMaxRotation(p_77043_1_), 0.0F, 0.0F, 1.0F);
      } else {
         String var6 = EnumChatFormatting.getTextWithoutFormattingCodes(p_77043_1_.getName());
         if (var6 != null && (var6.equals("Dinnerbone") || var6.equals("Grumm")) && (!(p_77043_1_ instanceof EntityPlayer) || ((EntityPlayer)p_77043_1_).func_175148_a(EnumPlayerModelParts.CAPE))) {
            GlStateManager.translate(0.0F, p_77043_1_.height + 0.1F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
         }
      }

   }

   protected float getSwingProgress(EntityLivingBase p_77040_1_, float p_77040_2_) {
      return p_77040_1_.getSwingProgress(p_77040_2_);
   }

   protected float handleRotationFloat(EntityLivingBase p_77044_1_, float p_77044_2_) {
      return (float)p_77044_1_.ticksExisted + p_77044_2_;
   }

   public void func_177093_a(EntityLivingBase p_177093_1_, float p_177093_2_, float p_177093_3_, float p_177093_4_, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_) {
      Iterator var9 = this.field_177097_h.iterator();

      while(var9.hasNext()) {
         LayerRenderer var10 = (LayerRenderer)var9.next();
         boolean var11 = this.func_177092_a(p_177093_1_, p_177093_4_, var10.shouldCombineTextures());
         if (renderLayers) {
            var10.doRenderLayer(p_177093_1_, p_177093_2_, p_177093_3_, p_177093_4_, p_177093_5_, p_177093_6_, p_177093_7_, p_177093_8_);
         }

         if (var11) {
            this.func_177091_f();
         }
      }

   }

   protected float getDeathMaxRotation(EntityLivingBase p_77037_1_) {
      return 90.0F;
   }

   protected int getColorMultiplier(EntityLivingBase p_77030_1_, float p_77030_2_, float p_77030_3_) {
      return 0;
   }

   protected void preRenderCallback(EntityLivingBase p_77041_1_, float p_77041_2_) {
   }

   public void passSpecialRender(EntityLivingBase p_77033_1_, double p_77033_2_, double p_77033_4_, double p_77033_6_) {
      if (this.canRenderName(p_77033_1_)) {
         double var8 = p_77033_1_.getDistanceSqToEntity(this.renderManager.livingPlayer);
         float var10 = p_77033_1_.isSneaking() ? 32.0F : 64.0F;
         if (var8 < (double)(var10 * var10)) {
            String var11 = p_77033_1_.getDisplayName().getFormattedText();
            float var12 = 0.02666667F;
            GlStateManager.alphaFunc(516, 0.1F);
            if (p_77033_1_.isSneaking()) {
               Event event = EventSystem.getInstance(EventNametagRender.class);
               event.fire();
               if (event.isCancelled()) {
                  return;
               }

               FontRenderer var13 = this.getFontRendererFromRenderManager();
               GlStateManager.pushMatrix();
               GlStateManager.translate((float)p_77033_2_, (float)p_77033_4_ + p_77033_1_.height + 0.5F - (p_77033_1_.isChild() ? p_77033_1_.height / 2.0F : 0.0F), (float)p_77033_6_);
               GL11.glNormal3f(0.0F, 1.0F, 0.0F);
               RenderManager var10000 = this.renderManager;
               GlStateManager.rotate(-RenderManager.playerViewY, 0.0F, 1.0F, 0.0F);
               var10000 = this.renderManager;
               GlStateManager.rotate(RenderManager.playerViewX, 1.0F, 0.0F, 0.0F);
               GlStateManager.scale(-0.02666667F, -0.02666667F, 0.02666667F);
               GlStateManager.translate(0.0F, 9.374999F, 0.0F);
               GlStateManager.disableLighting();
               GlStateManager.depthMask(false);
               GlStateManager.enableBlend();
               GlStateManager.disableTextures();
               GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
               Tessellator var14 = Tessellator.getInstance();
               WorldRenderer var15 = var14.getWorldRenderer();
               var15.startDrawingQuads();
               int var16 = var13.getStringWidth(var11) / 2;
               var15.setColorRGBA(0.0F, 0.0F, 0.0F, 0.25F);
               var15.addVertex((double)(-var16 - 1), -1.0D, 0.0D);
               var15.addVertex((double)(-var16 - 1), 8.0D, 0.0D);
               var15.addVertex((double)(var16 + 1), 8.0D, 0.0D);
               var15.addVertex((double)(var16 + 1), -1.0D, 0.0D);
               var14.draw();
               GlStateManager.enableTextures();
               GlStateManager.depthMask(true);
               var13.drawString(var11, (float)(-var13.getStringWidth(var11) / 2), 0.0F, 553648127);
               GlStateManager.enableLighting();
               GlStateManager.disableBlend();
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               GlStateManager.popMatrix();
            } else {
               this.func_177069_a(p_77033_1_, p_77033_2_, p_77033_4_ - (p_77033_1_.isChild() ? (double)(p_77033_1_.height / 2.0F) : 0.0D), p_77033_6_, var11, 0.02666667F, var8);
            }
         }
      }

   }

   protected boolean canRenderName(EntityLivingBase targetEntity) {
      EntityPlayerSP var2 = Minecraft.getMinecraft().thePlayer;
      if (targetEntity instanceof EntityPlayer && targetEntity != var2) {
         Team var3 = targetEntity.getTeam();
         Team var4 = var2.getTeam();
         if (var3 != null) {
            Team.EnumVisible var5 = var3.func_178770_i();
            switch(RendererLivingEntity.SwitchEnumVisible.field_178679_a[var5.ordinal()]) {
            case 1:
               return true;
            case 2:
               return false;
            case 3:
               return var4 == null || var3.isSameTeam(var4);
            case 4:
               return var4 == null || !var3.isSameTeam(var4);
            default:
               return true;
            }
         }
      }

      return Minecraft.isGuiEnabled() && targetEntity != this.renderManager.livingPlayer && !targetEntity.isInvisibleToPlayer(var2) && targetEntity.riddenByEntity == null;
   }

   public void func_177086_a(boolean p_177086_1_) {
      field_177098_i = p_177086_1_;
   }

   protected boolean func_177070_b(Entity p_177070_1_) {
      return this.canRenderName((EntityLivingBase)p_177070_1_);
   }

   public void func_177067_a(Entity p_177067_1_, double p_177067_2_, double p_177067_4_, double p_177067_6_) {
      this.passSpecialRender((EntityLivingBase)p_177067_1_, p_177067_2_, p_177067_4_, p_177067_6_);
   }

   public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {
      this.doRender((EntityLivingBase)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
   }

   static {
      int[] var0 = field_177096_e.getTextureData();

      for(int var1 = 0; var1 < 256; ++var1) {
         var0[var1] = -1;
      }

      field_177096_e.updateDynamicTexture();
   }

   static final class SwitchEnumVisible {
      static final int[] field_178679_a = new int[Team.EnumVisible.values().length];

      static {
         try {
            field_178679_a[Team.EnumVisible.ALWAYS.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            field_178679_a[Team.EnumVisible.NEVER.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            field_178679_a[Team.EnumVisible.HIDE_FOR_OTHER_TEAMS.ordinal()] = 3;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            field_178679_a[Team.EnumVisible.HIDE_FOR_OWN_TEAM.ordinal()] = 4;
         } catch (NoSuchFieldError var1) {
            ;
         }

      }
   }
}

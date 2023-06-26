package me.rebirthclient.mod.modules.impl.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ibm.icu.math.BigDecimal;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class DMGParticles
extends Module {
    private final Map<Integer, Float> hpData = Maps.newHashMap();
    private final List<Particle> particles = Lists.newArrayList();
    private final Setting<Integer> deleteAfter = this.add(new Setting<>("Remove Ticks", 7, 1, 60));

    public DMGParticles() {
        super("DMGParticles", "show damage", Category.RENDER);
    }

    @Override
    public void onUpdate() {
        if (!this.particles.isEmpty()) {
            for (Particle p : this.particles) {
                if (p == null) continue;
                ++p.ticks;
            }
        }
        for (Entity entity : DMGParticles.mc.world.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase)) continue;
            EntityLivingBase ent = (EntityLivingBase)entity;
            double lastHp = this.hpData.getOrDefault(ent.getEntityId(), ent.getMaxHealth());
            this.hpData.remove(entity.getEntityId());
            this.hpData.put(entity.getEntityId(), ent.getHealth());
            if (lastHp == (double)ent.getHealth()) continue;
            Color color = Color.YELLOW;
            Vec3d loc = new Vec3d(entity.posX + Math.random() * 0.5 * (double)(Math.random() > 0.5 ? -1 : 1), entity.getEntityBoundingBox().minY + (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) * 0.5, entity.posZ + Math.random() * 0.5 * (double)(Math.random() > 0.5 ? -1 : 1));
            double str = new BigDecimal(Math.abs(lastHp - (double)ent.getHealth())).setScale(1, 4).doubleValue();
            this.particles.add(new Particle(String.valueOf(str), loc.x, loc.y, loc.z, color));
        }
    }

    @Override
    public void onDisable() {
        this.particles.clear();
    }

    @Override
    @SubscribeEvent
    public void onRender3D(Render3DEvent event) {
        boolean canClear = true;
        if (!this.particles.isEmpty()) {
            for (Particle p : this.particles) {
                if (p == null || p.ticks > this.deleteAfter.getValue()) continue;
                canClear = false;
                GlStateManager.pushMatrix();
                GlStateManager.disableDepth();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
                tessellator.draw();
                GL11.glDisable(2848);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.enablePolygonOffset();
                GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
                GlStateManager.translate(p.posX - DMGParticles.mc.getRenderManager().renderPosX, p.posY - DMGParticles.mc.getRenderManager().renderPosY, p.posZ - DMGParticles.mc.getRenderManager().renderPosZ);
                GlStateManager.rotate(-DMGParticles.mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
                GlStateManager.rotate(DMGParticles.mc.getRenderManager().playerViewX, DMGParticles.mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
                GlStateManager.scale(-0.03, -0.03, 0.03);
                GL11.glDepthMask(false);
                DMGParticles.mc.fontRenderer.drawStringWithShadow(p.str, (float)((double)(-DMGParticles.mc.fontRenderer.getStringWidth(p.str)) * 0.5), (float)(-DMGParticles.mc.fontRenderer.FONT_HEIGHT + 1), p.color.getRGB());
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                GL11.glDepthMask(true);
                GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
                GlStateManager.disablePolygonOffset();
                GlStateManager.resetColor();
                GlStateManager.popMatrix();
            }
        }
        if (canClear) {
            this.particles.clear();
        }
    }

    static class Particle {
        public final String str;
        public final double posX;
        public final double posY;
        public final double posZ;
        public final Color color;
        public int ticks;

        public Particle(String str, double posX, double posY, double posZ, Color color) {
            this.str = str;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.color = color;
            this.ticks = 0;
        }
    }
}


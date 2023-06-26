package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class AutoEsu
extends Module {
    private final Setting<Integer> offsetX = this.add(new Setting<>("OffsetX", -42, -500, 500));
    private final Setting<Integer> offsetY = this.add(new Setting<>("OffsetY", -27, -500, 500));
    private final Setting<Integer> width = this.add(new Setting<>("Width", 84, 0, 500));
    private final Setting<Integer> height = this.add(new Setting<>("Height", 40, 0, 500));
    private final Setting<Boolean> noFriend = this.add(new Setting<>("NoFriend", true));
    private final Setting<Mode> mode = this.add(new Setting<>("Mode", Mode.RuiNan));

    public AutoEsu() {
        super("AutoEsu", "IQ RuiNan", Category.RENDER);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        for (EntityPlayer target : AutoEsu.mc.world.playerEntities) {
            if (this.invalid(target)) continue;
            this.drawBurrowESP(target.posX, target.posY + 1.5, target.posZ);
        }
    }

    private void drawBurrowESP(double x, double y, double z) {
        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        double scale = 0.0245;
        GlStateManager.translate(x - AutoEsu.mc.getRenderManager().renderPosX, y - AutoEsu.mc.getRenderManager().renderPosY, z - AutoEsu.mc.getRenderManager().renderPosZ);
        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-AutoEsu.mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(AutoEsu.mc.getRenderManager().playerViewX, AutoEsu.mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, -scale);
        RenderUtil.glColor(new Color(255, 255, 255, 120));
        RenderUtil.drawCircle(1.5f, -5.0f, 16.0f, ColorUtil.injectAlpha(new Color(255, 255, 255, 120).getRGB(), 0));
        GlStateManager.enableAlpha();
        if (this.mode.getValue() == Mode.ShengJie) {
            mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/mugshot/shengjie.png"));
        } else if (this.mode.getValue() == Mode.RuiNan) {
            mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/mugshot/ruinan.png"));
        } else if (this.mode.getValue() == Mode.ShanZhu) {
            mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/mugshot/shanzhu.png"));
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        RenderUtil.drawModalRect(this.offsetX.getValue(), this.offsetY.getValue(), 0.0f, 0.0f, 12, 12, this.width.getValue(), this.height.getValue(), 12.0f, 12.0f);
        GlStateManager.disableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.popMatrix();
    }

    private boolean invalid(Entity entity) {
        return entity == null || EntityUtil.isDead(entity) || entity.equals(AutoEsu.mc.player) || entity instanceof EntityPlayer && Managers.FRIENDS.isFriend(entity.getName()) && this.noFriend.getValue() || AutoEsu.mc.player.getDistanceSq(entity) < MathUtil.square(0.5);
    }

    public static enum Mode {
        RuiNan,
        ShengJie,
        ShanZhu

    }
}


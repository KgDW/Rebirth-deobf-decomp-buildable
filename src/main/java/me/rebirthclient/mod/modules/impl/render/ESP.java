package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.InterpolationUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class ESP
extends Module {
    private final Setting<Page> page = this.add(new Setting<>("Settings", Page.GLOBAL));
    private final Setting<Items> items = this.add(new Setting<>("Items", Items.BOX, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> xpOrbs = this.add(new Setting<>("ExpOrbs", false, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> xp = this.add(new Setting<>("ExpBottles", false, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> pearls = this.add(new Setting<>("Pearls", true, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Players> players = this.add(new Setting<>("Players", Players.BOX, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Burrow> burrow = this.add(new Setting<>("Burrow", Burrow.PRETTY, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Color> textColor = this.add(new Setting<>("TextColor", new Color(-1), v -> this.page.getValue() == Page.COLORS).injectBoolean(false));
    private final Setting<Color> color = this.add(new Setting<>("Color", new Color(125, 125, 213, 150), v -> this.page.getValue() == Page.COLORS));
    private final Setting<Color> lineColor = this.add(new Setting<>("LineColor", new Color(-1493172225, true), v -> this.page.getValue() == Page.COLORS).injectBoolean(false));

    public ESP() {
        super("ESP", "Highlights entities through walls in several modes", Category.RENDER);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (ESP.fullNullCheck()) {
            return;
        }
        for (Entity entity : ESP.mc.world.loadedEntityList) {
            if (!(entity != ESP.mc.player && entity instanceof EntityPlayer && this.players.getValue() == Players.BOX && !((EntityPlayer)entity).isSpectator() || entity instanceof EntityExpBottle && this.xp.getValue() || entity instanceof EntityXPOrb && this.xpOrbs.getValue() || entity instanceof EntityEnderPearl && this.pearls.getValue()) && (!(entity instanceof EntityItem) || this.items.getValue() != Items.BOX)) continue;
            RenderUtil.drawEntityBoxESP(entity, this.color.getValue(), this.lineColor.booleanValue, this.lineColor.getValue(), 1.0f, true, true, this.color.getValue().getAlpha());
        }
        for (Entity entity : ESP.mc.world.loadedEntityList) {
            if (!(entity instanceof EntityItem) || this.items.getValue() != Items.TEXT) continue;
            ItemStack stack = ((EntityItem)entity).getItem();
            String text2 = stack.getDisplayName() + (stack.isStackable() && stack.getCount() >= 2 ? " x" + stack.getCount() : "");
            Vec3d vec = InterpolationUtil.getInterpolatedPos(entity, mc.getRenderPartialTicks(), true);
            this.drawNameTag(text2, vec);
        }
        for (EntityPlayer player : ESP.mc.world.playerEntities) {
            BlockPos feetPos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY + 0.2), Math.floor(player.posZ));
            if (player.isSpectator() || player.isRiding() || player == ESP.mc.player || BlockUtil.getBlock(feetPos) == Blocks.AIR || BlockUtil.canReplace(feetPos) || BlockUtil.isStair(BlockUtil.getBlock(feetPos)) || BlockUtil.isSlab(BlockUtil.getBlock(feetPos)) || BlockUtil.isFence(BlockUtil.getBlock(feetPos)) || !(ESP.mc.player.getDistanceSq(feetPos) <= 200.0)) continue;
            if (this.burrow.getValue() == Burrow.PRETTY) {
                this.drawBurrowESP(feetPos);
                continue;
            }
            if (this.burrow.getValue() != Burrow.TEXT) continue;
            this.drawNameTag(BlockUtil.getBlock(feetPos) == Blocks.WEB ? "Web" : "Burrow", feetPos);
        }
    }

    private void drawBurrowESP(BlockPos pos) {
        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        double x = (double)pos.getX() + 0.5;
        double y = (double)pos.getY() + 0.4;
        double z = (double)pos.getZ() + 0.5;
        int distance = (int)ESP.mc.player.getDistance(x, y, z);
        double scale = 0.0018f + 0.002f * (float)distance;
        if ((double)distance <= 8.0) {
            scale = 0.0245;
        }
        GlStateManager.translate(x - ESP.mc.getRenderManager().renderPosX, y - ESP.mc.getRenderManager().renderPosY, z - ESP.mc.getRenderManager().renderPosZ);
        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-ESP.mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(ESP.mc.getRenderManager().playerViewX, ESP.mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, -scale);
        RenderUtil.glColor(this.color.getValue());
        RenderUtil.drawCircle(1.5f, -5.0f, 16.0f, ColorUtil.injectAlpha(this.color.getValue().getRGB(), 100));
        GlStateManager.enableAlpha();
        Block block = BlockUtil.getBlock(pos);
        if (block == Blocks.ENDER_CHEST) {
            mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/constant/ingame/echest.png"));
        } else if (block == Blocks.WEB) {
            mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/constant/ingame/web.png"));
        } else {
            mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/constant/ingame/obby.png"));
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        RenderUtil.drawModalRect(-10, -17, 0.0f, 0.0f, 12, 12, 24, 24, 12.0f, 12.0f);
        GlStateManager.disableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.popMatrix();
    }

    private void drawNameTag(String text2, BlockPos pos) {
        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GL11.glEnable(3553);
        double x = (double)pos.getX() + 0.5;
        double y = (double)pos.getY() + 0.7;
        double z = (double)pos.getZ() + 0.5;
        float scale = 0.030833336f;
        GlStateManager.translate(x - ESP.mc.getRenderManager().renderPosX, y - ESP.mc.getRenderManager().renderPosY, z - ESP.mc.getRenderManager().renderPosZ);
        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-ESP.mc.player.rotationYaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(ESP.mc.player.rotationPitch, ESP.mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, scale);
        int distance = (int)ESP.mc.player.getDistance(x, y, z);
        float scaleD = (float)distance / 2.0f / 3.0f;
        if (scaleD < 1.0f) {
            scaleD = 1.0f;
        }
        GlStateManager.scale(scaleD, scaleD, scaleD);
        GlStateManager.translate(-((double)Managers.TEXT.getStringWidth(text2) / 2.0), 0.0, 0.0);
        Managers.TEXT.drawStringWithShadow(text2, 0.0f, 6.0f, this.textColor.booleanValue ? this.textColor.getValue().getRGB() : -1);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.popMatrix();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawNameTag(String text2, Vec3d vec) {
        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GL11.glEnable(3553);
        double x = vec.x;
        double y = vec.y;
        double z = vec.z;
        Entity camera = mc.getRenderViewEntity();
        assert (camera != null);
        double distance = camera.getDistance(x + ESP.mc.getRenderManager().viewerPosX, y + ESP.mc.getRenderManager().viewerPosY, z + ESP.mc.getRenderManager().viewerPosZ);
        double scale = 0.0018 + (double)0.003f * distance;
        int textWidth = Managers.TEXT.getStringWidth(text2) / 2;
        if (distance <= 8.0) {
            scale = 0.0245;
        }
        GlStateManager.translate(x, y + (double)0.4f, z);
        GlStateManager.rotate(-ESP.mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(ESP.mc.getRenderManager().playerViewX, ESP.mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, scale);
        Managers.TEXT.drawStringWithShadow(text2, (float)(-textWidth) - 0.1f, -(ESP.mc.fontRenderer.FONT_HEIGHT - 1), this.textColor.booleanValue ? this.textColor.getValue().getRGB() : -1);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.popMatrix();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static enum Players {
        BOX,
        OFF

    }

    public static enum Burrow {
        PRETTY,
        TEXT,
        OFF

    }

    public static enum Items {
        BOX,
        TEXT,
        OFF

    }

    public static enum Page {
        COLORS,
        GLOBAL

    }
}


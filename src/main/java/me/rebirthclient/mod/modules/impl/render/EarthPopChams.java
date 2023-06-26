package me.rebirthclient.mod.modules.impl.render;

import com.mojang.authlib.GameProfile;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.entity.StaticModelPlayer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class EarthPopChams
extends Module {
    public static EarthPopChams INSTANCE;
    private final Setting<Float> lineWidth = this.add(new Setting<>("LineWidth", 1.0f, 0.1f, 3.0f));
    private final Setting<Color> color = this.add(new Setting<>("Color", new Color(80, 80, 255, 80)));
    private final Setting<Color> outline = this.add(new Setting<>("Outline", new Color(80, 80, 255, 255)));
    private final Setting<Boolean> copyAnimations = this.add(new Setting<>("Copy-Animations", true));
    private final Setting<Double> yAnimations = this.add(new Setting<>("Y-Animation", 0.0, -7.0, 7.0));
    private final Setting<Integer> fadeTime = this.add(new Setting<>("Fade-Time", 1500, 0, 5000));
    private final Setting<Boolean> selfPop = this.add(new Setting<>("Self-Pop", false).setParent());
    private final Setting<Color> selfColor = this.add(new Setting<>("Self-Color", new Color(80, 80, 255, 80), v -> this.selfPop.isOpen()));
    private final Setting<Color> selfOutline = this.add(new Setting<>("Self-Outline", new Color(80, 80, 255, 255), v -> this.selfPop.isOpen()));
    private final Setting<Boolean> friendPop = this.add(new Setting<>("Friend-Pop", false).setParent());
    private final Setting<Color> friendColor = this.add(new Setting<>("Friend-Color", new Color(45, 255, 45, 80), v -> this.friendPop.isOpen()));
    private final Setting<Color> friendOutline = this.add(new Setting<>("Friend-Outline", new Color(45, 255, 45, 255), v -> this.friendPop.isOpen()));
    private final List<PopData> popDataList = new ArrayList<>();

    public EarthPopChams() {
        super("3arPopChams", "Pop rendering", Category.RENDER);
        INSTANCE = this;
    }

    private void color(Color color) {
        GL11.glColor4f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
    }

    private void startRender() {
        GL11.glPushAttrib(1048575);
        GL11.glPushMatrix();
        GL11.glDisable(3008);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glEnable(2884);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4353);
        GL11.glDisable(2896);
    }

    private void endRender() {
        GL11.glEnable(2896);
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glEnable(3008);
        GL11.glDepthMask(true);
        GL11.glCullFace(1029);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private EntityPlayer copyPlayer(EntityPlayer playerIn, boolean animations) {
        final int count = playerIn.getItemInUseCount();
        EntityPlayer copy = new EntityPlayer(EarthPopChams.mc.world, new GameProfile(UUID.randomUUID(), playerIn.getName())){

            public boolean isSpectator() {
                return false;
            }

            public boolean isCreative() {
                return false;
            }

            public int getItemInUseCount() {
                return count;
            }
        };
        if (animations) {
            copy.setSneaking(playerIn.isSneaking());
            copy.swingProgress = playerIn.swingProgress;
            copy.limbSwing = playerIn.limbSwing;
            copy.limbSwingAmount = playerIn.prevLimbSwingAmount;
            copy.inventory.copyInventory(playerIn.inventory);
        }
        copy.setPrimaryHand(playerIn.getPrimaryHand());
        copy.ticksExisted = playerIn.ticksExisted;
        copy.setEntityId(playerIn.getEntityId());
        copy.copyLocationAndAnglesFrom(playerIn);
        return copy;
    }

    private Color getColor(EntityPlayer entity) {
        if (entity.equals(EarthPopChams.mc.player)) {
            return this.selfColor.getValue();
        }
        if (Managers.FRIENDS.isFriend(entity)) {
            return this.friendColor.getValue();
        }
        return this.color.getValue();
    }

    private Color getOutlineColor(EntityPlayer entity) {
        if (entity.equals(EarthPopChams.mc.player)) {
            return this.selfOutline.getValue();
        }
        if (Managers.FRIENDS.isFriend(entity)) {
            return this.friendOutline.getValue();
        }
        return this.outline.getValue();
    }

    private boolean isValidEntity(EntityPlayer entity) {
        return !(entity == EarthPopChams.mc.player && !this.selfPop.getValue() || Managers.FRIENDS.isFriend(entity) && entity != EarthPopChams.mc.player && !this.friendPop.getValue());
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (this.popDataList.isEmpty()) {
            return;
        }
        for (PopData data : this.popDataList) {
            EntityPlayer player = data.getPlayer();
            StaticModelPlayer model = data.getModel();
            double x = data.getX() - EarthPopChams.mc.getRenderManager().viewerPosX;
            double y = data.getY() - EarthPopChams.mc.getRenderManager().viewerPosY;
            double z = data.getZ() - EarthPopChams.mc.getRenderManager().viewerPosZ;
            GlStateManager.pushMatrix();
            this.startRender();
            GlStateManager.translate(x, y += this.yAnimations.getValue() * (double)(System.currentTimeMillis() - data.getTime()) / this.fadeTime.getValue().doubleValue(), z);
            GlStateManager.rotate(180.0f - model.getYaw(), 0.0f, 1.0f, 0.0f);
            Color boxColor = this.getColor(data.getPlayer());
            Color outlineColor = this.getOutlineColor(data.getPlayer());
            float maxBoxAlpha = boxColor.getAlpha();
            float maxOutlineAlpha = outlineColor.getAlpha();
            float alphaBoxAmount = maxBoxAlpha / (float) this.fadeTime.getValue();
            float alphaOutlineAmount = maxOutlineAlpha / (float) this.fadeTime.getValue();
            int fadeBoxAlpha = MathHelper.clamp((int)(alphaBoxAmount * (float)(data.getTime() + (long) this.fadeTime.getValue() - System.currentTimeMillis())), 0, (int)maxBoxAlpha);
            int fadeOutlineAlpha = MathHelper.clamp((int)(alphaOutlineAmount * (float)(data.getTime() + (long) this.fadeTime.getValue() - System.currentTimeMillis())), 0, (int)maxOutlineAlpha);
            Color box = new Color(boxColor.getRed(), boxColor.getGreen(), boxColor.getBlue(), fadeBoxAlpha);
            Color out = new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), fadeOutlineAlpha);
            GlStateManager.enableRescaleNormal();
            GlStateManager.scale(-1.0f, -1.0f, 1.0f);
            double widthX = player.getEntityBoundingBox().maxX - player.getRenderBoundingBox().minX + 1.0;
            double widthZ = player.getEntityBoundingBox().maxZ - player.getEntityBoundingBox().minZ + 1.0;
            GlStateManager.scale(widthX, player.height, widthZ);
            GlStateManager.translate(0.0f, -1.501f, 0.0f);
            this.color(box);
            GL11.glPolygonMode(1032, 6914);
            model.render(0.0625f);
            this.color(out);
            GL11.glLineWidth(this.lineWidth.getValue());
            GL11.glPolygonMode(1032, 6913);
            model.render(0.0625f);
            this.endRender();
            GlStateManager.popMatrix();
        }
        this.popDataList.removeIf(e -> e.getTime() + (long) this.fadeTime.getValue() < System.currentTimeMillis());
    }

    @Override
    public void onTotemPop(EntityPlayer player) {
        if (!this.isValidEntity(player)) {
            return;
        }
        this.popDataList.add(new PopData(this.copyPlayer(player, this.copyAnimations.getValue()), System.currentTimeMillis(), player.posX, player.posY, player.posZ, player instanceof AbstractClientPlayer && ((AbstractClientPlayer)player).getSkinType().equals("slim")));
    }

    public static class PopData {
        private final EntityPlayer player;
        private final StaticModelPlayer model;
        private final long time;
        private final double x;
        private final double y;
        private final double z;

        public PopData(EntityPlayer player, long time, double x, double y, double z, boolean slim) {
            this.player = player;
            this.time = time;
            this.x = x;
            this.y = y - (player.isSneaking() ? 0.125 : 0.0);
            this.z = z;
            this.model = new StaticModelPlayer(player, slim, 0.0f);
            this.model.disableArmorLayers();
        }

        public EntityPlayer getPlayer() {
            return this.player;
        }

        public long getTime() {
            return this.time;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getZ() {
            return this.z;
        }

        public StaticModelPlayer getModel() {
            return this.model;
        }
    }
}


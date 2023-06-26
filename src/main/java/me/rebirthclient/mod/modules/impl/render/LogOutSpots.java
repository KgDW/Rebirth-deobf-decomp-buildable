package me.rebirthclient.mod.modules.impl.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.rebirthclient.api.events.impl.ConnectionEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InterpolationUtil;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.api.util.render.entity.StaticModelPlayer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.client.FontMod;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class LogOutSpots
extends Module {
    protected final Map<UUID, LogOutSpot> spots = new ConcurrentHashMap<>();
    final Date date = new Date();
    private final Setting<Boolean> text = this.add(new Setting<>("Text", true));
    private final Setting<Float> range = this.add(new Setting<>("Range", 150.0f, 50.0f, 500.0f));
    private final Setting<Boolean> rect = this.add(new Setting<>("Rectangle", true));
    private final Setting<Boolean> outline = this.add(new Setting<>("Outline", true));
    private final Setting<Boolean> time = this.add(new Setting<>("Time", true));
    private final Setting<Boolean> coords = this.add(new Setting<>("Coords", true));
    private final Setting<Boolean> box = this.add(new Setting<>("Box", true));
    private final Setting<Color> color = this.add(new Setting<>("Color", new Color(-1766449377, true)));
    private final Setting<Boolean> chams = this.add(new Setting<>("Chams", true).setParent());
    private final Setting<Color> fillColor = this.add(new Setting<>("ChamsColor", new Color(190, 0, 0, 100), v -> this.chams.isOpen()));
    private final Setting<Color> lineColor = this.add(new Setting<>("LineColor", new Color(255, 255, 255, 120), v -> this.chams.isOpen()).injectBoolean(false));

    public LogOutSpots() {
        super("LogOutSpots", "Displays logout spots for players", Category.RENDER);
    }

    @Override
    public void onEnable() {
        this.spots.clear();
    }

    @Override
    public void onDisable() {
        this.spots.clear();
    }

    @Override
    public void onLogout() {
        this.spots.clear();
    }

    @Override
    public void onTick() {
        for (LogOutSpot spot : this.spots.values()) {
            if (!(LogOutSpots.mc.player.getDistanceSq(spot.getPlayer()) >= (double) this.range.getValue())) continue;
            this.spots.remove(spot.getPlayer().getUniqueID());
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        for (LogOutSpot spot : this.spots.values()) {
            AxisAlignedBB bb = InterpolationUtil.getInterpolatedAxis(spot.getBoundingBox());
            if (this.chams.getValue()) {
                StaticModelPlayer model = spot.getModel();
                double x = spot.getX() - LogOutSpots.mc.getRenderManager().viewerPosX;
                double y = spot.getY() - LogOutSpots.mc.getRenderManager().viewerPosY;
                double z = spot.getZ() - LogOutSpots.mc.getRenderManager().viewerPosZ;
                GL11.glPushMatrix();
                GL11.glPushAttrib(1048575);
                GL11.glDisable(3553);
                GL11.glDisable(2896);
                GL11.glDisable(2929);
                GL11.glEnable(2848);
                GL11.glEnable(3042);
                GlStateManager.blendFunc(770, 771);
                GlStateManager.translate(x, y, z);
                GlStateManager.rotate(180.0f - model.getYaw(), 0.0f, 1.0f, 0.0f);
                GlStateManager.enableRescaleNormal();
                GlStateManager.scale(-1.0f, -1.0f, 1.0f);
                double widthX = bb.maxX - bb.minX + 1.0;
                double widthZ = bb.maxZ - bb.minZ + 1.0;
                GlStateManager.scale(widthX, bb.maxY - bb.minY, widthZ);
                GlStateManager.translate(0.0f, -1.501f, 0.0f);
                Color fill = this.fillColor.getValue();
                Color line = this.lineColor.booleanValue ? this.lineColor.getValue() : this.fillColor.getValue();
                RenderUtil.glColor(fill);
                GL11.glPolygonMode(1032, 6914);
                model.render(0.0625f);
                RenderUtil.glColor(line);
                GL11.glLineWidth(1.0f);
                GL11.glPolygonMode(1032, 6913);
                model.render(0.0625f);
                GL11.glPopAttrib();
                GL11.glPopMatrix();
            }
            if (this.box.getValue()) {
                RenderUtil.drawBlockOutline(bb, this.color.getValue(), 1.0f, false);
            }
            double x = InterpolationUtil.getInterpolatedDouble(spot.getPlayer().lastTickPosX, spot.getPlayer().posX, event.getPartialTicks()) - LogOutSpots.mc.getRenderManager().renderPosX;
            double y = InterpolationUtil.getInterpolatedDouble(spot.getPlayer().lastTickPosY, spot.getPlayer().posY, event.getPartialTicks()) - LogOutSpots.mc.getRenderManager().renderPosY;
            double z = InterpolationUtil.getInterpolatedDouble(spot.getPlayer().lastTickPosZ, spot.getPlayer().posZ, event.getPartialTicks()) - LogOutSpots.mc.getRenderManager().renderPosZ;
            this.drawNameTag(spot.getName(), x, y, z, spot.getX(), spot.getY(), spot.getZ());
        }
    }

    @SubscribeEvent
    public void onConnection(ConnectionEvent event) {
        if (LogOutSpots.mc.world.getPlayerEntityByUUID(event.getUuid()) != null && LogOutSpots.mc.world.getPlayerEntityByUUID(event.getUuid()).getName().equals("\u00a7a\u00a7l[\u8d5e\u52a9\u672c\u670d]")) {
            return;
        }
        if (event.getStage() == 0) {
            UUID uuid = event.getUuid();
            EntityPlayer entity = LogOutSpots.mc.world.getPlayerEntityByUUID(uuid);
            if (entity != null && this.text.getValue()) {
                this.sendMessage("\u00a7a" + entity.getName() + " just logged in" + (this.coords.getValue() ? " at (" + (int)entity.posX + ", " + (int)entity.posY + ", " + (int)entity.posZ + ")!" : "!"));
            }
            this.spots.remove(event.getUuid());
        } else if (event.getStage() == 1) {
            EntityPlayer player = event.getPlayer();
            if (player == null || this.spots.containsKey(player.getUniqueID())) {
                return;
            }
            if (this.text.getValue()) {
                this.sendMessage("\u00a7c" + event.getName() + " just logged out" + (this.coords.getValue() ? " at (" + (int)player.posX + ", " + (int)player.posY + ", " + (int)player.posZ + ")!" : "!"));
            }
            LogOutSpot spot = new LogOutSpot(player);
            this.spots.put(player.getUniqueID(), spot);
        }
    }

    private void drawNameTag(String name, double x, double y, double z, double x2, double y2, double z2) {
        y += 0.7;
        Entity camera = mc.getRenderViewEntity();
        assert (camera != null);
        double originalPositionX = camera.posX;
        double originalPositionY = camera.posY;
        double originalPositionZ = camera.posZ;
        camera.posX = InterpolationUtil.getInterpolatedDouble(camera.prevPosX, camera.posX, mc.getRenderPartialTicks());
        camera.posY = InterpolationUtil.getInterpolatedDouble(camera.prevPosY, camera.posY, mc.getRenderPartialTicks());
        camera.posZ = InterpolationUtil.getInterpolatedDouble(camera.prevPosZ, camera.posZ, mc.getRenderPartialTicks());
        String displayTag = name + (this.coords.getValue() ? " XYZ: " + (int)x2 + ", " + (int)y2 + ", " + (int)z2 : "") + (this.time.getValue() ? " " + ChatFormatting.GRAY + "(" + this.getLogOutTime() + ")" : "");
        double distance = camera.getDistance(x + LogOutSpots.mc.getRenderManager().viewerPosX, y + LogOutSpots.mc.getRenderManager().viewerPosY, z + LogOutSpots.mc.getRenderManager().viewerPosZ);
        int width = Managers.TEXT.getStringWidth(displayTag) / 2;
        double scale = (0.0018 + 5.0 * (distance * (double)0.6f)) / 1000.0;
        if (distance <= 8.0) {
            scale = 0.0245;
        }
        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
        GlStateManager.disableLighting();
        GlStateManager.translate((float)x, (float)y + 1.4f, (float)z);
        GlStateManager.rotate(-LogOutSpots.mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(LogOutSpots.mc.getRenderManager().playerViewX, LogOutSpots.mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.enableBlend();
        if (this.rect.getValue()) {
            RenderUtil.drawRect(-width - 2, -(LogOutSpots.mc.fontRenderer.FONT_HEIGHT + 1), (float)width + 2.0f, 1.5f, 0x55000000);
        }
        if (this.outline.getValue()) {
            RenderUtil.drawNameTagOutline(-width - 2, -(LogOutSpots.mc.fontRenderer.FONT_HEIGHT + 1), (float)width + 2.0f, 1.5f, 0.8f, this.color.getValue().getRGB(), this.color.getValue().darker().getRGB(), false);
        }
        GlStateManager.disableBlend();
        Managers.TEXT.drawMCString(displayTag, -width, FontMod.INSTANCE.isOn() ? (float)(-(LogOutSpots.mc.fontRenderer.FONT_HEIGHT + 1)) : (float)(-(LogOutSpots.mc.fontRenderer.FONT_HEIGHT - 1)), this.color.getValue().getRGB(), true);
        camera.posX = originalPositionX;
        camera.posY = originalPositionY;
        camera.posZ = originalPositionZ;
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.popMatrix();
    }

    private String getLogOutTime() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");
        return dateFormatter.format(this.date);
    }

    protected static class LogOutSpot
    implements Wrapper {
        private final String name;
        private final StaticModelPlayer model;
        private final AxisAlignedBB boundingBox;
        private final EntityPlayer player;
        private final double x;
        private final double y;
        private final double z;

        public LogOutSpot(EntityPlayer player) {
            this.name = player.getName();
            this.model = new StaticModelPlayer(EntityUtil.getCopiedPlayer(player), player instanceof AbstractClientPlayer && ((AbstractClientPlayer)player).getSkinType().equals("slim"), 0.0f);
            this.model.disableArmorLayers();
            this.boundingBox = player.getEntityBoundingBox();
            this.x = player.posX;
            this.y = player.posY;
            this.z = player.posZ;
            this.player = player;
        }

        public String getName() {
            return this.name;
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

        public double getDistance() {
            return LogOutSpot.mc.player.getDistance(this.x, this.y, this.z);
        }

        public AxisAlignedBB getBoundingBox() {
            return this.boundingBox;
        }

        public StaticModelPlayer getModel() {
            return this.model;
        }

        public EntityPlayer getPlayer() {
            return this.player;
        }
    }
}


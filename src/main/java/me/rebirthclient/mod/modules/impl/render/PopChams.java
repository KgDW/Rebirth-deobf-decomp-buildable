package me.rebirthclient.mod.modules.impl.render;

import com.mojang.authlib.GameProfile;
import java.awt.Color;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.render.EarthPopChams;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class PopChams
extends Module {
    public static Setting<Boolean> self;
    public static Setting<Boolean> elevator;
    public static Setting<Integer> fadestart;
    public static Setting<Float> fadetime;
    public static Setting<Boolean> onlyOneEsp;
    public static Setting<ElevatorMode> elevatorMode;
    public static PopChams INSTANCE;
    private final Setting<Color> outlineColor = this.add(new Setting<>("Outline Color", new Color(255, 255, 255, 100)));
    private final Setting<Color> fillColor = this.add(new Setting<>("Fill Color", new Color(255, 255, 255, 100)));
    EntityOtherPlayerMP player;
    ModelPlayer playerModel;
    Long startTime;
    double alphaFill;
    double alphaLine;

    public PopChams() {
        super("PopChams", "Pop rendering", Category.RENDER);
        INSTANCE = this;
        self = this.add(new Setting<>("Self", true));
        elevator = this.add(new Setting<>("Travel", true).setParent());
        elevatorMode = this.add(new Setting<>("Elevator", ElevatorMode.UP, v -> elevator.isOpen()));
        fadestart = this.add(new Setting<>("Fade Start", 0, 0, 255));
        fadetime = this.add(new Setting<>("Fade Time", 0.5f, 0.0f, 2.0f));
        onlyOneEsp = this.add(new Setting<>("Only Render One", true));
    }

    public static void renderEntity(EntityLivingBase entity, ModelBase modelBase, float limbSwing, float limbSwingAmount, int scale) {
        float partialTicks = mc.getRenderPartialTicks();
        double x = entity.posX - PopChams.mc.getRenderManager().viewerPosX;
        double y = entity.posY - PopChams.mc.getRenderManager().viewerPosY;
        double z = entity.posZ - PopChams.mc.getRenderManager().viewerPosZ;
        GlStateManager.pushMatrix();
        if (entity.isSneaking()) {
            y -= 0.125;
        }
        PopChams.renderLivingAt(x, y, z);
        float f8 = PopChams.handleRotationFloat();
        PopChams.prepareRotations(entity);
        float f9 = PopChams.prepareScale(entity, scale);
        GlStateManager.enableAlpha();
        modelBase.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        modelBase.setRotationAngles(limbSwing, limbSwingAmount, f8, entity.rotationYaw, entity.rotationPitch, f9, entity);
        modelBase.render(entity, limbSwing, limbSwingAmount, f8, entity.rotationYaw, entity.rotationPitch, f9);
        GlStateManager.popMatrix();
    }

    public static void renderLivingAt(double x, double y, double z) {
        GlStateManager.translate(x, y, z);
    }

    public static float prepareScale(EntityLivingBase entity, float scale) {
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0f, -1.0f, 1.0f);
        double widthX = entity.getRenderBoundingBox().maxX - entity.getRenderBoundingBox().minX;
        double widthZ = entity.getRenderBoundingBox().maxZ - entity.getRenderBoundingBox().minZ;
        GlStateManager.scale((double)scale + widthX, scale * entity.height, (double)scale + widthZ);
        GlStateManager.translate(0.0f, -1.501f, 0.0f);
        return 0.0625f;
    }

    public static void prepareRotations(EntityLivingBase entityLivingBase) {
        GlStateManager.rotate(180.0f - entityLivingBase.rotationYaw, 0.0f, 1.0f, 0.0f);
    }

    public static Color newAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static void glColor(Color color) {
        GL11.glColor4f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
    }

    public static float handleRotationFloat() {
        return 0.0f;
    }

    public static void prepareGL() {
        GL11.glBlendFunc(770, 771);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(1.5f);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
    }

    public static void releaseGL() {
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        SPacketEntityStatus packet;
        if (event.isCanceled() || PopChams.fullNullCheck()) {
            return;
        }
        if (EarthPopChams.INSTANCE.isOn()) {
            this.disable();
            return;
        }
        if (event.getPacket() instanceof SPacketEntityStatus && (packet = event.getPacket()).getOpCode() == 35) {
            packet.getEntity(PopChams.mc.world);
            if (self.getValue() || packet.getEntity(PopChams.mc.world).getEntityId() != PopChams.mc.player.getEntityId()) {
                GameProfile profile = new GameProfile(PopChams.mc.player.getUniqueID(), "");
                this.player = new EntityOtherPlayerMP(PopChams.mc.world, profile);
                this.player.copyLocationAndAnglesFrom(packet.getEntity(PopChams.mc.world));
                this.playerModel = new ModelPlayer(0.0f, false);
                this.startTime = System.currentTimeMillis();
                this.playerModel.bipedHead.showModel = false;
                this.playerModel.bipedBody.showModel = false;
                this.playerModel.bipedLeftArmwear.showModel = false;
                this.playerModel.bipedLeftLegwear.showModel = false;
                this.playerModel.bipedRightArmwear.showModel = false;
                this.playerModel.bipedRightLegwear.showModel = false;
                this.alphaFill = this.fillColor.getValue().getAlpha();
                this.alphaLine = this.outlineColor.getValue().getAlpha();
                if (!onlyOneEsp.getValue()) {
                    new TotemPopChams(this.player, this.playerModel, this.startTime, this.alphaFill);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (PopChams.fullNullCheck()) {
            return;
        }
        if (EarthPopChams.INSTANCE.isOn()) {
            this.disable();
            return;
        }
        if (onlyOneEsp.getValue()) {
            if (this.player == null || PopChams.mc.world == null || PopChams.mc.player == null) {
                return;
            }
            if (elevator.getValue()) {
                if (elevatorMode.getValue() == ElevatorMode.UP) {
                    this.player.posY += 0.05f * event.getPartialTicks();
                } else if (elevatorMode.getValue() == ElevatorMode.DOWN) {
                    this.player.posY -= 0.05f * event.getPartialTicks();
                }
            }
            GL11.glLineWidth(1.0f);
            Color lineColorS = this.outlineColor.getValue();
            Color fillColorS = this.fillColor.getValue();
            int lineA = lineColorS.getAlpha();
            int fillA = fillColorS.getAlpha();
            long time = System.currentTimeMillis() - this.startTime - ((Number)fadestart.getValue()).longValue();
            if (System.currentTimeMillis() - this.startTime > ((Number)fadestart.getValue()).longValue()) {
                double normal = this.normalize(time, ((Number)fadetime.getValue()).doubleValue());
                normal = MathHelper.clamp(normal, 0.0, 1.0);
                normal = -normal + 1.0;
                lineA *= (int)normal;
                fillA *= (int)normal;
            }
            Color lineColor = PopChams.newAlpha(lineColorS, lineA);
            Color fillColor = PopChams.newAlpha(fillColorS, fillA);
            if (this.player != null && this.playerModel != null) {
                PopChams.prepareGL();
                GL11.glPushAttrib(1048575);
                GL11.glEnable(2881);
                GL11.glEnable(2848);
                if (this.alphaFill > 1.0) {
                    this.alphaFill -= fadetime.getValue();
                }
                Color fillFinal = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), (int)this.alphaFill);
                if (this.alphaLine > 1.0) {
                    this.alphaLine -= fadetime.getValue();
                }
                Color outlineFinal = new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), (int)this.alphaLine);
                PopChams.glColor(fillFinal);
                GL11.glPolygonMode(1032, 6914);
                PopChams.renderEntity(this.player, this.playerModel, this.player.limbSwing, this.player.limbSwingAmount, 1);
                PopChams.glColor(outlineFinal);
                GL11.glPolygonMode(1032, 6913);
                PopChams.renderEntity(this.player, this.playerModel, this.player.limbSwing, this.player.limbSwingAmount, 1);
                GL11.glPolygonMode(1032, 6914);
                GL11.glPopAttrib();
                PopChams.releaseGL();
            }
        }
    }

    double normalize(double value, double max) {
        return (value - 0.0) / (max - 0.0);
    }

    public static class TotemPopChams {
        final EntityOtherPlayerMP player;
        final ModelPlayer playerModel;
        final Long startTime;
        double alphaFill;
        double alphaLine;

        public TotemPopChams(EntityOtherPlayerMP player, ModelPlayer playerModel, Long startTime, double alphaFill) {
            MinecraftForge.EVENT_BUS.register(this);
            this.player = player;
            this.playerModel = playerModel;
            this.startTime = startTime;
            this.alphaFill = alphaFill;
            this.alphaLine = alphaFill;
        }

        public static void renderEntity(EntityLivingBase entity, ModelBase modelBase, float limbSwing, float limbSwingAmount, float scale) {
            float partialTicks = Wrapper.mc.getRenderPartialTicks();
            double x = entity.posX - Wrapper.mc.getRenderManager().viewerPosX;
            double y = entity.posY - Wrapper.mc.getRenderManager().viewerPosY;
            double z = entity.posZ - Wrapper.mc.getRenderManager().viewerPosZ;
            GlStateManager.pushMatrix();
            if (entity.isSneaking()) {
                y -= 0.125;
            }
            TotemPopChams.renderLivingAt(x, y, z);
            float f8 = TotemPopChams.handleRotationFloat();
            TotemPopChams.prepareRotations(entity);
            float f9 = TotemPopChams.prepareScale(entity, scale);
            GlStateManager.enableAlpha();
            modelBase.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
            modelBase.setRotationAngles(limbSwing, limbSwingAmount, f8, entity.rotationYawHead, entity.rotationPitch, f9, entity);
            modelBase.render(entity, limbSwing, limbSwingAmount, f8, entity.rotationYawHead, entity.rotationPitch, f9);
            GlStateManager.popMatrix();
        }

        public static void renderLivingAt(double x, double y, double z) {
            GlStateManager.translate((float)x, (float)y, (float)z);
        }

        public static float prepareScale(EntityLivingBase entity, float scale) {
            GlStateManager.enableRescaleNormal();
            GlStateManager.scale(-1.0f, -1.0f, 1.0f);
            double widthX = entity.getRenderBoundingBox().maxX - entity.getRenderBoundingBox().minX;
            double widthZ = entity.getRenderBoundingBox().maxZ - entity.getRenderBoundingBox().minZ;
            GlStateManager.scale((double)scale + widthX, scale * entity.height, (double)scale + widthZ);
            GlStateManager.translate(0.0f, -1.501f, 0.0f);
            return 0.0625f;
        }

        public static void prepareRotations(EntityLivingBase entityLivingBase) {
            GlStateManager.rotate(180.0f - entityLivingBase.rotationYaw, 0.0f, 1.0f, 0.0f);
        }

        public static Color newAlpha(Color color, int alpha) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        }

        public static void glColor(Color color) {
            GL11.glColor4f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        }

        public static float handleRotationFloat() {
            return 0.0f;
        }

        @SubscribeEvent
        public void onRenderWorld(RenderWorldLastEvent event) {
            if (this.player == null || Wrapper.mc.world == null || Wrapper.mc.player == null) {
                return;
            }
            GL11.glLineWidth(1.0f);
            Color lineColorS = INSTANCE.outlineColor.getValue();
            Color fillColorS = INSTANCE.fillColor.getValue();
            int lineA = lineColorS.getAlpha();
            int fillA = fillColorS.getAlpha();
            long time = System.currentTimeMillis() - this.startTime - fadestart.getValue().longValue();
            if (System.currentTimeMillis() - this.startTime > fadestart.getValue().longValue()) {
                double normal = this.normalize(time, fadetime.getValue().doubleValue());
                normal = MathHelper.clamp(normal, 0.0, 1.0);
                normal = -normal + 1.0;
                lineA *= (int)normal;
                fillA *= (int)normal;
            }
            Color lineColor = TotemPopChams.newAlpha(lineColorS, lineA);
            Color fillColor = TotemPopChams.newAlpha(fillColorS, fillA);
            if (this.playerModel != null) {
                PopChams.prepareGL();
                GL11.glPushAttrib(1048575);
                GL11.glEnable(2881);
                GL11.glEnable(2848);
                if (this.alphaFill > 1.0) {
                    this.alphaFill -= fadetime.getValue();
                }
                Color fillFinal = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), (int)this.alphaFill);
                if (this.alphaLine > 1.0) {
                    this.alphaLine -= fadetime.getValue();
                }
                Color outlineFinal = new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), (int)this.alphaLine);
                TotemPopChams.glColor(fillFinal);
                GL11.glPolygonMode(1032, 6914);
                TotemPopChams.renderEntity(this.player, this.playerModel, this.player.limbSwing, this.player.limbSwingAmount, 1.0f);
                TotemPopChams.glColor(outlineFinal);
                GL11.glPolygonMode(1032, 6913);
                TotemPopChams.renderEntity(this.player, this.playerModel, this.player.limbSwing, this.player.limbSwingAmount, 1.0f);
                GL11.glPolygonMode(1032, 6914);
                GL11.glPopAttrib();
                PopChams.releaseGL();
            }
        }

        double normalize(double value, double max) {
            return (value - 0.0) / (max - 0.0);
        }
    }

    public static enum ElevatorMode {
        UP,
        DOWN

    }
}


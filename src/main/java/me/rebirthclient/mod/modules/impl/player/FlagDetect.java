package me.rebirthclient.mod.modules.impl.player;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.CopyOfPlayer;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.api.util.render.entity.StaticModelPlayer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.exploit.Clip;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class FlagDetect
extends Module {
    private final Setting<Boolean> notify = this.add(new Setting<>("ChatNotify", true));
    private final Setting<Boolean> chams = this.add(new Setting<>("Chams", true).setParent());
    private final Setting<Integer> fadeTime = this.add(new Setting<>("FadeTime", 15, 1, 50, v -> this.chams.isOpen()));
    private final Setting<Color> color = this.add(new Setting<>("Color", new Color(190, 0, 0, 100), v -> this.chams.isOpen()));
    private final Setting<Color> lineColor = this.add(new Setting<>("LineColor", new Color(255, 255, 255, 120), v -> this.chams.isOpen()).injectBoolean(false));
    private CopyOfPlayer player;

    public FlagDetect() {
        super("FlagDetect", "Detects & notifies you when your player is being flagged", Category.PLAYER);
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (!FlagDetect.fullNullCheck() && FlagDetect.spawnCheck() && !Clip.INSTANCE.isOn() && event.getPacket() instanceof SPacketPlayerPosLook) {
            if (this.notify.getValue()) {
                this.sendMessageWithID(ChatFormatting.RED + "Server lagged you back!", -123);
            }
            if (this.chams.getValue()) {
                this.player = new CopyOfPlayer(EntityUtil.getCopiedPlayer(FlagDetect.mc.player), System.currentTimeMillis(), FlagDetect.mc.player.posX, FlagDetect.mc.player.posY, FlagDetect.mc.player.posZ, FlagDetect.mc.player.getSkinType().equals("slim"));
            }
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (FlagDetect.fullNullCheck() || !this.chams.getValue() || this.player == null) {
            return;
        }
        EntityPlayer player = this.player.getPlayer();
        StaticModelPlayer model = this.player.getModel();
        double x = this.player.getX() - FlagDetect.mc.getRenderManager().viewerPosX;
        double y = this.player.getY() - FlagDetect.mc.getRenderManager().viewerPosY;
        double z = this.player.getZ() - FlagDetect.mc.getRenderManager().viewerPosZ;
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
        Color boxColor = this.color.getValue();
        Color outlineColor = this.lineColor.booleanValue ? this.lineColor.getValue() : this.color.getValue();
        float maxBoxAlpha = boxColor.getAlpha();
        float maxOutlineAlpha = outlineColor.getAlpha();
        float alphaBoxAmount = maxBoxAlpha / (float)(this.fadeTime.getValue() * 100);
        float alphaOutlineAmount = maxOutlineAlpha / (float)(this.fadeTime.getValue() * 100);
        int fadeBoxAlpha = MathHelper.clamp((int)(alphaBoxAmount * (float)(this.player.getTime() + (long)(this.fadeTime.getValue() * 100) - System.currentTimeMillis())), 0, (int)maxBoxAlpha);
        int fadeOutlineAlpha = MathHelper.clamp((int)(alphaOutlineAmount * (float)(this.player.getTime() + (long)(this.fadeTime.getValue() * 100) - System.currentTimeMillis())), 0, (int)maxOutlineAlpha);
        Color box = ColorUtil.injectAlpha(boxColor, fadeBoxAlpha);
        Color line = ColorUtil.injectAlpha(outlineColor, fadeOutlineAlpha);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0f, -1.0f, 1.0f);
        double widthX = player.getEntityBoundingBox().maxX - player.getRenderBoundingBox().minX + 1.0;
        double widthZ = player.getEntityBoundingBox().maxZ - player.getEntityBoundingBox().minZ + 1.0;
        GlStateManager.scale(widthX, player.height, widthZ);
        GlStateManager.translate(0.0f, -1.501f, 0.0f);
        RenderUtil.glColor(box);
        GL11.glPolygonMode(1032, 6914);
        model.render(0.0625f);
        RenderUtil.glColor(line);
        GL11.glLineWidth(0.8f);
        GL11.glPolygonMode(1032, 6913);
        model.render(0.0625f);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
        if (this.player.getTime() + (long)(this.fadeTime.getValue() * 100) < System.currentTimeMillis()) {
            this.player = null;
        }
    }
}


package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.render.shaders.FramebufferShader;
import me.rebirthclient.api.util.render.shaders.ShaderMode;
import me.rebirthclient.asm.accessors.IEntityRenderer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class ShaderChams
extends Module {
    public static ShaderChams INSTANCE;
    private final Setting<ShaderMode> shader = this.add(new Setting<>("Shader Mode", ShaderMode.Aqua));
    private final Setting<Boolean> animation = this.add(new Setting<>("Animation", true));
    private final Setting<Integer> animationSpeed = this.add(new Setting<>("Animation Speed", 1, 1, 10));
    private final Setting<Float> radius = this.add(new Setting<>("Glow Radius", 3.3f, 1.0f, 10.0f));
    private final Setting<Float> divider = this.add(new Setting<>("Glow Divider", 158.6f, 1.0f, 1000.0f));
    private final Setting<Float> maxSample = this.add(new Setting<>("Glow MaxSample", 10.0f, 1.0f, 20.0f));
    private final Setting<Boolean> players = this.add(new Setting<>("Player", false));
    private final Setting<Boolean> crystals = this.add(new Setting<>("Crystal", false));
    private final Setting<Boolean> xp = this.add(new Setting<>("Exp", false));
    private final Setting<Boolean> items = this.add(new Setting<>("DroppedItem", false));
    private final Setting<Boolean> self = this.add(new Setting<>("ItemShaderChams", true));
    private final Setting<Boolean> fovOnly = this.add(new Setting<>("FOVOnly", false));
    private final Setting<Color> color = this.add(new Setting<>("Color", new Color(-8553003, true)));
    private final Setting<Integer> range = this.add(new Setting<>("Range", 50, 5, 250));
    private Boolean criticalSection = false;

    public ShaderChams() {
        super("ShaderChams", "good render", Category.RENDER);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        if (ShaderChams.fullNullCheck()) {
            return;
        }
        if (!this.criticalSection && this.self.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderWorldLastEvent event) {
        if (ShaderChams.fullNullCheck()) {
            return;
        }
        if (Display.isActive() || Display.isVisible()) {
            FramebufferShader shader = this.shader.getValue().getShader();
            if (shader == null) {
                return;
            }
            GL11.glBlendFunc(770, 771);
            shader.setShaderParams(this.animation.getValue(), this.animationSpeed.getValue(), this.color.getValue(), this.radius.getValue(), this.divider.getValue(), this.maxSample.getValue());
            this.criticalSection = true;
            shader.startDraw(mc.getRenderPartialTicks());
            ShaderChams.mc.world.loadedEntityList.stream().filter(entity -> entity != null && (entity != ShaderChams.mc.player || entity != mc.getRenderViewEntity()) && mc.getRenderManager().getEntityRenderObject(entity) != null && (entity instanceof EntityPlayer && this.players.getValue() && !((EntityPlayer)entity).isSpectator() || entity instanceof EntityEnderCrystal && this.crystals.getValue() || entity instanceof EntityExpBottle && this.xp.getValue() || entity instanceof EntityItem && this.items.getValue())).forEach(entity -> {
                Render render;
                if (entity.getDistance(ShaderChams.mc.player) > (float) this.range.getValue() || this.fovOnly.getValue() && !Managers.ROTATIONS.isInFov(entity.getPosition())) {
                    return;
                }
                Vec3d vector = EntityUtil.getInterpolatedRenderPos(entity, event.getPartialTicks());
                if (entity instanceof EntityPlayer) {
                    ((EntityPlayer)entity).hurtTime = 0;
                }
                if ((render = mc.getRenderManager().getEntityRenderObject(entity)) != null) {
                    try {
                        render.doRender(entity, vector.x, vector.y, vector.z, entity.rotationYaw, event.getPartialTicks());
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            });
            if (this.self.getValue() && !BlockUtil.getBlock(EntityUtil.getPlayerPos().up()).equals(Blocks.WATER)) {
                ((IEntityRenderer)ShaderChams.mc.entityRenderer).invokeRenderHand(mc.getRenderPartialTicks(), 2);
            }
            shader.stopDraw();
            this.criticalSection = false;
        }
    }

    @Override
    public String getInfo() {
        return this.shader.getValue().getName();
    }

    @Override
    public void onLogin() {
        if (this.isOn()) {
            this.disable();
            this.enable();
        }
    }
}


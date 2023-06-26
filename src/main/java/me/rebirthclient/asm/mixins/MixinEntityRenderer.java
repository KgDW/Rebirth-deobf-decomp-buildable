package me.rebirthclient.asm.mixins;

import com.google.common.base.Predicate;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import me.rebirthclient.api.events.impl.FreecamEvent;
import me.rebirthclient.api.events.impl.PerspectiveEvent;
import me.rebirthclient.api.util.Vector3f;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.mod.modules.impl.exploit.NoHitBox;
import me.rebirthclient.mod.modules.impl.render.Ambience;
import me.rebirthclient.mod.modules.impl.render.CameraClip;
import me.rebirthclient.mod.modules.impl.render.NoRender;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.glu.Project;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={EntityRenderer.class})
public class MixinEntityRenderer {
    final Minecraft mc = Minecraft.getMinecraft();
    @Shadow
    private ItemStack itemActivationItem;
    @Shadow
    @Final
    private int[] lightmapColors;

    @Redirect(method={"getMouseOver"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcluding(WorldClient worldClient, Entity entityIn, AxisAlignedBB boundingBox, Predicate predicate) {
        NoHitBox mod = NoHitBox.INSTANCE;
        if (mod.isOn() && (this.mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe && mod.pickaxe.getValue() || this.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL && mod.crystal.getValue() || this.mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_APPLE && mod.gapple.getValue() || this.mc.player.getHeldItemMainhand().getItem() == Item.getItemFromBlock(Blocks.OBSIDIAN) && mod.obby.getValue() || this.mc.player.getHeldItemMainhand().getItem() == Items.FLINT_AND_STEEL || this.mc.player.getHeldItemMainhand().getItem() == Items.TNT_MINECART)) {
            return new ArrayList<>();
        }
        return worldClient.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }

    @Inject(method={"hurtCameraEffect"}, at={@At(value="HEAD")}, cancellable=true)
    public void hurtCameraEffect(float ticks, CallbackInfo info) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.hurtCam.getValue()) {
            info.cancel();
        }
    }

    @Inject(method={"renderItemActivation"}, at={@At(value="HEAD")}, cancellable=true)
    public void renderItemActivationHook(CallbackInfo info) {
        if (this.itemActivationItem != null && NoRender.INSTANCE.isOn() && NoRender.INSTANCE.totemPops.getValue() && this.itemActivationItem.getItem() == Items.TOTEM_OF_UNDYING) {
            info.cancel();
        }
    }

    @ModifyVariable(method={"orientCamera"}, ordinal=3, at=@At(value="STORE", ordinal=0), require=1)
    public double changeCameraDistanceHook(double range) {
        if (CameraClip.INSTANCE.isOn()) {
            return CameraClip.INSTANCE.distance.getValue();
        }
        return range;
    }

    @ModifyVariable(method={"orientCamera"}, ordinal=7, at=@At(value="STORE", ordinal=0), require=1)
    public double orientCameraHook(double range) {
        if (CameraClip.INSTANCE.isOn()) {
            return CameraClip.INSTANCE.distance.getValue();
        }
        return range;
    }

    @Redirect(method={"setupCameraTransform"}, at=@At(value="INVOKE", target="Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void onSetupCameraTransform(float f, float f2, float f3, float f4) {
        PerspectiveEvent perspectiveEvent = new PerspectiveEvent((float)this.mc.displayWidth / (float)this.mc.displayHeight);
        MinecraftForge.EVENT_BUS.post(perspectiveEvent);
        Project.gluPerspective(f, perspectiveEvent.getAngle(), f3, f4);
    }

    @Redirect(method={"renderWorldPass"}, at=@At(value="INVOKE", target="Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void onRenderWorldPass(float f, float f2, float f3, float f4) {
        PerspectiveEvent perspectiveEvent = new PerspectiveEvent((float)this.mc.displayWidth / (float)this.mc.displayHeight);
        MinecraftForge.EVENT_BUS.post(perspectiveEvent);
        Project.gluPerspective(f, perspectiveEvent.getAngle(), f3, f4);
    }

    @Redirect(method={"renderCloudsCheck"}, at=@At(value="INVOKE", target="Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void onRenderCloudsCheck(float f, float f2, float f3, float f4) {
        PerspectiveEvent perspectiveEvent = new PerspectiveEvent((float)this.mc.displayWidth / (float)this.mc.displayHeight);
        MinecraftForge.EVENT_BUS.post(perspectiveEvent);
        Project.gluPerspective(f, perspectiveEvent.getAngle(), f3, f4);
    }

    @Inject(method={"updateLightmap"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/texture/DynamicTexture;updateDynamicTexture()V", shift=At.Shift.BEFORE)})
    public void updateTextureHook(float partialTicks, CallbackInfo ci) {
        if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.lightMap.booleanValue) {
            for (int i = 0; i < this.lightmapColors.length; ++i) {
                Color ambientColor = Ambience.INSTANCE.getColor();
                int alpha = ambientColor.getAlpha();
                float modifier = (float)alpha / 255.0f;
                int color = this.lightmapColors[i];
                int[] bgr = MathUtil.toRGBAArray(color);
                Vector3f values = new Vector3f((float)bgr[2] / 255.0f, (float)bgr[1] / 255.0f, (float)bgr[0] / 255.0f);
                Vector3f newValues = new Vector3f((float)ambientColor.getRed() / 255.0f, (float)ambientColor.getGreen() / 255.0f, (float)ambientColor.getBlue() / 255.0f);
                Vector3f finalValues = MathUtil.mix(values, newValues, modifier);
                int red = (int)(finalValues.x * 255.0f);
                int green = (int)(finalValues.y * 255.0f);
                int blue = (int)(finalValues.z * 255.0f);
                this.lightmapColors[i] = 0xFF000000 | red << 16 | green << 8 | blue;
            }
        }
    }

    @Redirect(method={"getMouseOver"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;"))
    private Entity redirectMouseOver(Minecraft mc) {
        FreecamEvent event = new FreecamEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled() && Keyboard.isKeyDown(56)) {
            return mc.player;
        }
        return mc.getRenderViewEntity();
    }

    @Redirect(method={"updateCameraAndRender"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/entity/EntityPlayerSP;turn(FF)V"))
    private void redirectTurn(EntityPlayerSP entityPlayerSP, float yaw, float pitch) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            FreecamEvent event = new FreecamEvent();
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                if (Keyboard.isKeyDown(56)) {
                    mc.player.turn(yaw, pitch);
                } else {
                    Objects.requireNonNull(mc.getRenderViewEntity(), "Render Entity").turn(yaw, pitch);
                }
                return;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        entityPlayerSP.turn(yaw, pitch);
    }

    @Redirect(method={"renderWorldPass"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/entity/EntityPlayerSP;isSpectator()Z"))
    public boolean redirectIsSpectator(EntityPlayerSP entityPlayerSP) {
        FreecamEvent event = new FreecamEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return true;
        }
        if (entityPlayerSP != null) {
            return entityPlayerSP.isSpectator();
        }
        return false;
    }
}


package me.rebirthclient.asm.mixins;

import java.awt.Color;
import me.rebirthclient.api.events.impl.FreecamEntityEvent;
import me.rebirthclient.api.events.impl.FreecamEvent;
import me.rebirthclient.api.events.impl.RenderItemInFirstPersonEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.impl.render.Chams;
import me.rebirthclient.mod.modules.impl.render.ItemModel;
import me.rebirthclient.mod.modules.impl.render.Shader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemRenderer.class})
public abstract class MixinItemRenderer {
    @Shadow
    @Final
    public Minecraft mc;
    private boolean injection = true;

    @Shadow
    public abstract void renderItemInFirstPerson(AbstractClientPlayer var1, float var2, float var3, EnumHand var4, float var5, ItemStack var6, float var7);

    @Redirect(method={"renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/ItemRenderer;renderItemSide(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V"))
    public void renderItemInFirstPerson(ItemRenderer itemRenderer, EntityLivingBase entitylivingbaseIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform, boolean leftHanded) {
        RenderItemInFirstPersonEvent eventPre = new RenderItemInFirstPersonEvent(entitylivingbaseIn, heldStack, transform, leftHanded, 0);
        MinecraftForge.EVENT_BUS.post(eventPre);
        if (!eventPre.isCanceled()) {
            itemRenderer.renderItemSide(entitylivingbaseIn, eventPre.getStack(), eventPre.getTransformType(), leftHanded);
        }
        RenderItemInFirstPersonEvent eventPost = new RenderItemInFirstPersonEvent(entitylivingbaseIn, heldStack, transform, leftHanded, 1);
        MinecraftForge.EVENT_BUS.post(eventPost);
    }

    @Inject(method={"renderFireInFirstPerson"}, at={@At(value="HEAD")}, cancellable=true)
    public void renderFireInFirstPersonHook(CallbackInfo info) {
        if (Shader.INSTANCE.isOn()) {
            info.cancel();
        }
    }

    @Inject(method={"renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void renderItemInFirstPersonHook(AbstractClientPlayer player, float partialTicks, float rotationPitch, EnumHand hand, float swingProgress, ItemStack stack, float equippedProgress, CallbackInfo info) {
        Chams mod = Chams.INSTANCE;
        if (this.injection) {
            info.cancel();
            boolean isFriend = Managers.FRIENDS.isFriend(player.getName());
            this.injection = false;
            if (mod.isOn() && mod.self.getValue() && hand == EnumHand.MAIN_HAND && stack.isEmpty()) {
                Color color;
                if (mod.model.getValue() == Chams.Model.VANILLA) {
                    this.renderItemInFirstPerson(player, partialTicks, rotationPitch, hand, swingProgress, stack, equippedProgress);
                } else if (mod.model.getValue() == Chams.Model.XQZ) {
                    GL11.glEnable(32823);
                    GlStateManager.enablePolygonOffset();
                    GL11.glPolygonOffset(1.0f, -1000000.0f);
                    if (mod.modelColor.booleanValue) {
                        color = isFriend ? Managers.COLORS.getFriendColor(mod.modelColor.getValue().getAlpha()) : new Color(mod.modelColor.getValue().getRed(), mod.modelColor.getValue().getGreen(), mod.modelColor.getValue().getBlue(), mod.modelColor.getValue().getAlpha());
                        RenderUtil.glColor(color);
                    }
                    this.renderItemInFirstPerson(player, partialTicks, rotationPitch, hand, swingProgress, stack, equippedProgress);
                    GL11.glDisable(32823);
                    GlStateManager.disablePolygonOffset();
                    GL11.glPolygonOffset(1.0f, 1000000.0f);
                }
                if (mod.wireframe.getValue()) {
                    color = isFriend ? Managers.COLORS.getFriendColor(mod.lineColor.booleanValue ? mod.lineColor.getValue().getAlpha() : mod.color.getValue().getAlpha()) : (mod.lineColor.booleanValue ? new Color(mod.lineColor.getValue().getRed(), mod.lineColor.getValue().getGreen(), mod.lineColor.getValue().getBlue(), mod.lineColor.getValue().getAlpha()) : new Color(mod.color.getValue().getRed(), mod.color.getValue().getGreen(), mod.color.getValue().getBlue(), mod.color.getValue().getAlpha()));
                    GL11.glPushMatrix();
                    GL11.glPushAttrib(1048575);
                    GL11.glPolygonMode(1032, 6913);
                    GL11.glDisable(3553);
                    GL11.glDisable(2896);
                    GL11.glDisable(2929);
                    GL11.glEnable(2848);
                    GL11.glEnable(3042);
                    GlStateManager.blendFunc(770, 771);
                    RenderUtil.glColor(color);
                    GlStateManager.glLineWidth(mod.lineWidth.getValue());
                    this.renderItemInFirstPerson(player, partialTicks, rotationPitch, hand, swingProgress, stack, equippedProgress);
                    GL11.glPopAttrib();
                    GL11.glPopMatrix();
                }
                if (mod.fill.getValue()) {
                    color = isFriend ? Managers.COLORS.getFriendColor(mod.color.getValue().getAlpha()) : new Color(mod.color.getValue().getRed(), mod.color.getValue().getGreen(), mod.color.getValue().getBlue(), mod.color.getValue().getAlpha());
                    GL11.glPushAttrib(1048575);
                    GL11.glDisable(3008);
                    GL11.glDisable(3553);
                    GL11.glDisable(2896);
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                    GL11.glLineWidth(1.5f);
                    GL11.glEnable(2960);
                    if (mod.xqz.getValue()) {
                        GL11.glDisable(2929);
                        GL11.glDepthMask(false);
                    }
                    GL11.glEnable(10754);
                    RenderUtil.glColor(color);
                    this.renderItemInFirstPerson(player, partialTicks, rotationPitch, hand, swingProgress, stack, equippedProgress);
                    if (mod.xqz.getValue()) {
                        GL11.glEnable(2929);
                        GL11.glDepthMask(true);
                    }
                    GL11.glEnable(3042);
                    GL11.glEnable(2896);
                    GL11.glEnable(3553);
                    GL11.glEnable(3008);
                    GL11.glPopAttrib();
                }
                if (mod.glint.getValue()) {
                    color = isFriend ? Managers.COLORS.getFriendColor(mod.color.getValue().getAlpha()) : new Color(mod.color.getValue().getRed(), mod.color.getValue().getGreen(), mod.color.getValue().getBlue(), mod.color.getValue().getAlpha());
                    GL11.glPushMatrix();
                    GL11.glPushAttrib(1048575);
                    GL11.glPolygonMode(1032, 6914);
                    GL11.glDisable(2896);
                    GL11.glDepthRange(0.0, 0.1);
                    GL11.glEnable(3042);
                    RenderUtil.glColor(color);
                    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
                    float f = (float)player.ticksExisted + this.mc.getRenderPartialTicks();
                    this.mc.getRenderManager().renderEngine.bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));
                    for (int i = 0; i < 2; ++i) {
                        GlStateManager.matrixMode(5890);
                        GlStateManager.loadIdentity();
                        GL11.glScalef(1.0f, 1.0f, 1.0f);
                        GlStateManager.rotate(30.0f - (float)i * 60.0f, 0.0f, 0.0f, 1.0f);
                        GlStateManager.translate(0.0f, f * (0.001f + (float)i * 0.003f) * 20.0f, 0.0f);
                        GlStateManager.matrixMode(5888);
                        this.renderItemInFirstPerson(player, partialTicks, rotationPitch, hand, swingProgress, stack, equippedProgress);
                    }
                    GlStateManager.matrixMode(5890);
                    GlStateManager.loadIdentity();
                    GlStateManager.matrixMode(5888);
                    GL11.glDisable(3042);
                    GL11.glDepthRange(0.0, 1.0);
                    GL11.glEnable(2896);
                    GL11.glPopAttrib();
                    GL11.glPopMatrix();
                }
            } else {
                this.renderItemInFirstPerson(player, partialTicks, rotationPitch, hand, swingProgress, stack, equippedProgress);
            }
            this.injection = true;
        }
    }

    @Inject(method={"rotateArm"}, at={@At(value="HEAD")}, cancellable=true)
    public void rotateArmHook(float partialTicks, CallbackInfo info) {
        ItemModel mod = ItemModel.INSTANCE;
        if (mod.isOn() && mod.noSway.getValue()) {
            info.cancel();
        }
    }

    @Inject(method={"transformSideFirstPerson"}, at={@At(value="HEAD")}, cancellable=true)
    public void transformSideFirstPerson(EnumHandSide hand, float p_187459_2_, CallbackInfo cancel) {
        if (ItemModel.INSTANCE.isOn()) {
            boolean bob = ItemModel.INSTANCE.doBob.getValue();
            int i = hand == EnumHandSide.RIGHT ? 1 : -1;
            GlStateManager.translate((float)i * 0.56f, -0.52f + (bob ? p_187459_2_ : 0.0f) * -0.6f, -0.72f);
            if (hand == EnumHandSide.RIGHT) {
                GlStateManager.translate(ItemModel.INSTANCE.mainX.getValue(), ItemModel.INSTANCE.mainY.getValue(), ItemModel.INSTANCE.mainZ.getValue());
            } else {
                GlStateManager.translate(ItemModel.INSTANCE.offX.getValue(), ItemModel.INSTANCE.offY.getValue(), ItemModel.INSTANCE.offZ.getValue());
            }
            cancel.cancel();
        }
    }

    @Inject(method={"transformEatFirstPerson"}, at={@At(value="HEAD")}, cancellable=true)
    private void transformEatFirstPerson(float p_187454_1_, EnumHandSide hand, ItemStack stack, CallbackInfo cancel) {
        if (ItemModel.INSTANCE.isOn()) {
            if (!ItemModel.INSTANCE.noEatAnimation.getValue()) {
                float f3;
                float f = (float)Minecraft.getMinecraft().player.getItemInUseCount() - p_187454_1_ + 1.0f;
                float f2 = f / (float)stack.getMaxItemUseDuration();
                if (f2 < 0.8f) {
                    f3 = MathHelper.abs(MathHelper.cos(f / 4.0f * (float)Math.PI) * 0.1f);
                    GlStateManager.translate(0.0f, f3, 0.0f);
                }
                f3 = 1.0f - (float)Math.pow(f2, 27.0);
                int i = hand == EnumHandSide.RIGHT ? 1 : -1;
                GlStateManager.translate((double)(f3 * 0.6f * (float)i) * ItemModel.INSTANCE.eatX.getValue(), (double)(f3 * 0.5f) * -ItemModel.INSTANCE.eatY.getValue(), 0.0);
                GlStateManager.rotate((float)i * f3 * 90.0f, 0.0f, 1.0f, 0.0f);
                GlStateManager.rotate(f3 * 10.0f, 1.0f, 0.0f, 0.0f);
                GlStateManager.rotate((float)i * f3 * 30.0f, 0.0f, 0.0f, 1.0f);
            }
            cancel.cancel();
        }
    }

    @Inject(method={"renderOverlays"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderOverlaysInject(float partialTicks, CallbackInfo ci) {
        FreecamEvent event = new FreecamEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Redirect(method={"setLightmap"}, at=@At(value="FIELD", target="Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/entity/EntityPlayerSP;"))
    private EntityPlayerSP redirectLightmapPlayer(Minecraft mc) {
        FreecamEntityEvent event = new FreecamEntityEvent(mc.player);
        MinecraftForge.EVENT_BUS.post(event);
        return (EntityPlayerSP)event.getEntity();
    }

    @Redirect(method={"rotateArm"}, at=@At(value="FIELD", target="Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/entity/EntityPlayerSP;"))
    private EntityPlayerSP rotateArmPlayer(Minecraft mc) {
        FreecamEntityEvent event = new FreecamEntityEvent(mc.player);
        MinecraftForge.EVENT_BUS.post(event);
        return (EntityPlayerSP)event.getEntity();
    }

    @Redirect(method={"renderItemInFirstPerson(F)V"}, at=@At(value="FIELD", target="Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/entity/EntityPlayerSP;"))
    private EntityPlayerSP redirectPlayer(Minecraft mc) {
        FreecamEntityEvent event = new FreecamEntityEvent(mc.player);
        MinecraftForge.EVENT_BUS.post(event);
        return (EntityPlayerSP)event.getEntity();
    }

    @Redirect(method={"renderOverlays"}, at=@At(value="FIELD", target="Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/entity/EntityPlayerSP;"))
    private EntityPlayerSP renderOverlaysPlayer(Minecraft mc) {
        FreecamEntityEvent event = new FreecamEntityEvent(mc.player);
        MinecraftForge.EVENT_BUS.post(event);
        return (EntityPlayerSP)event.getEntity();
    }
}


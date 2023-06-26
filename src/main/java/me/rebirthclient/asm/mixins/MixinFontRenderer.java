package me.rebirthclient.asm.mixins;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.modules.impl.client.FontMod;
import me.rebirthclient.mod.modules.impl.client.NameProtect;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={FontRenderer.class})
public abstract class MixinFontRenderer {
    @Shadow
    protected abstract void renderStringAtPos(String var1, boolean var2);

    @Inject(method={"drawString(Ljava/lang/String;FFIZ)I"}, at={@At(value="HEAD")}, cancellable=true)
    public void renderStringHook(String text2, float x, float y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> info) {
        FontMod fontMod;
        if (FontMod.INSTANCE == null) {
            FontMod.INSTANCE = new FontMod();
        }
        if ((fontMod = FontMod.INSTANCE).isOn() && fontMod.global.getValue() && Managers.TEXT != null) {
            int result = (int) Managers.TEXT.drawString(text2, x, y, color, dropShadow);
            info.setReturnValue(result);
        }
    }

    @Redirect(method={"renderString(Ljava/lang/String;FFIZ)I"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/FontRenderer;renderStringAtPos(Ljava/lang/String;Z)V"))
    public void renderStringAtPosHook(FontRenderer renderer, String text2, boolean shadow) {
        NameProtect nameProtect;
        if (NameProtect.INSTANCE == null) {
            NameProtect.INSTANCE = new NameProtect();
        }
        text2 = (nameProtect = NameProtect.INSTANCE).isOn() ? text2.replaceAll(Wrapper.mc.getSession().getUsername(), nameProtect.name.getValue()) : text2;
        this.renderStringAtPos(text2, shadow);
    }
}


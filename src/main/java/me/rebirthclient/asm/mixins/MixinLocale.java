package me.rebirthclient.asm.mixins;

import net.minecraft.client.resources.Locale;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Locale.class})
public class MixinLocale {
    @Inject(method={"checkUnicode"}, at={@At(value="HEAD")}, cancellable=true)
    public void checkUnicode(CallbackInfo ci) {
        ci.cancel();
    }
}


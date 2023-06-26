package me.rebirthclient.asm.mixins;

import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.commands.Command;
import me.rebirthclient.mod.modules.impl.misc.SilentDisconnect;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={NetHandlerPlayClient.class})
public class MixinNetHandlerPlayClient {
    @Inject(method={"onDisconnect"}, at={@At(value="HEAD")}, cancellable=true)
    private void onDisconnect(ITextComponent reason, CallbackInfo callbackInfo) {
        if (Wrapper.mc.player != null && Wrapper.mc.world != null && SilentDisconnect.INSTANCE.isOn()) {
            Command.sendMessage(reason.getFormattedText());
            callbackInfo.cancel();
        }
    }
}


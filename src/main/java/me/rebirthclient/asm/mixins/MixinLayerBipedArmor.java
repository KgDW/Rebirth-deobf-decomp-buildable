package me.rebirthclient.asm.mixins;

import me.rebirthclient.mod.modules.impl.render.NoRender;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LayerBipedArmor.class})
public class MixinLayerBipedArmor {
    @Inject(method={"setModelSlotVisible*"}, at={@At(value="HEAD")}, cancellable=true)
    protected void setModelSlotVisible(ModelBiped model, EntityEquipmentSlot slotIn, CallbackInfo ci) {
        if (NoRender.INSTANCE.isOff() || !NoRender.INSTANCE.armor.getValue()) {
            return;
        }
        ci.cancel();
        model.bipedHead.showModel = false;
        model.bipedHeadwear.showModel = false;
        model.bipedRightArm.showModel = false;
        model.bipedLeftArm.showModel = false;
        model.bipedBody.showModel = false;
        model.bipedRightLeg.showModel = false;
        model.bipedLeftLeg.showModel = false;
    }
}


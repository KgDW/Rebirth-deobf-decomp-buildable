package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.movement.AutoCenter;
import me.rebirthclient.mod.modules.impl.movement.HoleSnap;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.init.Blocks;
import net.minecraft.util.MovementInput;

public class AntiGlide
extends Module {
    private final Setting<Boolean> onGround = this.add(new Setting<>("OnGround", true));
    private final Setting<Boolean> ice = this.add(new Setting<>("Ice", true));

    public AntiGlide() {
        super("AntiGlide", "Prevents inertial moving", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        this.setIceSlipperiness(0.98f);
    }

    @Override
    public void onUpdate() {
        if (HoleSnap.INSTANCE.isOn() || AutoCenter.INSTANCE.isOn()) {
            return;
        }
        if (this.onGround.getValue() && !AntiGlide.mc.player.onGround) {
            return;
        }
        MovementInput input = AntiGlide.mc.player.movementInput;
        if ((double)input.moveForward == 0.0 && (double)input.moveStrafe == 0.0) {
            AntiGlide.mc.player.motionX = 0.0;
            AntiGlide.mc.player.motionZ = 0.0;
        }
        if (this.ice.getValue() && AntiGlide.mc.player.getRidingEntity() == null) {
            this.setIceSlipperiness(0.6f);
        } else {
            this.setIceSlipperiness(0.98f);
        }
    }

    private void setIceSlipperiness(float in) {
        Blocks.ICE.setDefaultSlipperiness(in);
        Blocks.FROSTED_ICE.setDefaultSlipperiness(in);
        Blocks.PACKED_ICE.setDefaultSlipperiness(in);
    }
}


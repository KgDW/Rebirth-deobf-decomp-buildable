package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiAim
extends Module {
    private final Setting<Mode> pitchMode = this.register(new Setting<>("PitchMode", Mode.None));
    private final Setting<Mode> yawMode = this.register(new Setting<>("YawMode", Mode.None));
    public final Setting<Integer> Speed = this.register(new Setting<>("Speed", 1, 1, 45));
    public final Setting<Integer> yawDelta = this.register(new Setting<>("YawDelta", 60, -360, 360));
    public final Setting<Integer> pitchDelta = this.register(new Setting<>("PitchDelta", 10, -90, 90));
    public final Setting<Boolean> allowInteract = this.register(new Setting<>("AllowInteract", true));
    private float rotationYaw;
    private float rotationPitch;
    private float pitch_sinus_step;
    private float yaw_sinus_step;

    public AntiAim() {
        super("AntiAim", "fun", Category.PLAYER);
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onSync(MotionEvent e) {
        if (this.allowInteract.getValue() && (AntiAim.mc.gameSettings.keyBindAttack.isKeyDown() || AntiAim.mc.gameSettings.keyBindUseItem.isKeyDown())) {
            return;
        }
        if (this.yawMode.getValue() != Mode.None) {
            e.setYaw(this.rotationYaw);
        }
        if (this.pitchMode.getValue() != Mode.None) {
            e.setPitch(this.rotationPitch);
        }
    }

    @Override
    public void onUpdate() {
        if (this.pitchMode.getValue() == Mode.RandomAngle && AntiAim.mc.player.ticksExisted % this.Speed.getValue() == 0) {
            this.rotationPitch = MathUtil.random(90.0f, -90.0f);
        }
        if (this.yawMode.getValue() == Mode.RandomAngle && AntiAim.mc.player.ticksExisted % this.Speed.getValue() == 0) {
            this.rotationYaw = MathUtil.random(0.0f, 360.0f);
        }
        if (this.yawMode.getValue() == Mode.Spin && AntiAim.mc.player.ticksExisted % this.Speed.getValue() == 0) {
            this.rotationYaw += (float) this.yawDelta.getValue();
            if (this.rotationYaw > 360.0f) {
                this.rotationYaw = 0.0f;
            }
            if (this.rotationYaw < 0.0f) {
                this.rotationYaw = 360.0f;
            }
        }
        if (this.pitchMode.getValue() == Mode.Spin && AntiAim.mc.player.ticksExisted % this.Speed.getValue() == 0) {
            this.rotationPitch += (float) this.pitchDelta.getValue();
            if (this.rotationPitch > 90.0f) {
                this.rotationPitch = -90.0f;
            }
            if (this.rotationPitch < -90.0f) {
                this.rotationPitch = 90.0f;
            }
        }
        if (this.pitchMode.getValue() == Mode.Sinus) {
            this.pitch_sinus_step += (float) this.Speed.getValue() / 10.0f;
            this.rotationPitch = (float)((double)AntiAim.mc.player.rotationPitch + (double) this.pitchDelta.getValue() * Math.sin(this.pitch_sinus_step));
            this.rotationPitch = MathUtil.clamp(this.rotationPitch, -90.0f, 90.0f);
        }
        if (this.yawMode.getValue() == Mode.Sinus) {
            this.yaw_sinus_step += (float) this.Speed.getValue() / 10.0f;
            this.rotationYaw = (float)((double)AntiAim.mc.player.rotationYaw + (double) this.yawDelta.getValue() * Math.sin(this.yaw_sinus_step));
        }
        if (this.pitchMode.getValue() == Mode.Fixed) {
            this.rotationPitch = this.pitchDelta.getValue();
        }
        if (this.yawMode.getValue() == Mode.Fixed) {
            this.rotationYaw = this.yawDelta.getValue();
        }
        if (this.pitchMode.getValue() == Mode.Static) {
            this.rotationPitch = AntiAim.mc.player.rotationPitch + (float) this.pitchDelta.getValue();
            this.rotationPitch = MathUtil.clamp(this.rotationPitch, -90.0f, 90.0f);
        }
        if (this.yawMode.getValue() == Mode.Static) {
            this.rotationYaw = AntiAim.mc.player.rotationYaw % 360.0f + (float) this.yawDelta.getValue();
        }
    }

    public static enum Mode {
        None,
        RandomAngle,
        Spin,
        Sinus,
        Fixed,
        Static

    }
}


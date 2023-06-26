package me.rebirthclient.mod.modules.impl.movement;

import java.util.HashMap;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.movement.ElytraFly;
import me.rebirthclient.mod.modules.impl.movement.NewStep;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FastFall
extends Module {
    private final Setting<Mode> mode = this.add(new Setting<>("Mode", Mode.FAST));
    private final Setting<Boolean> noLag = this.add(new Setting<>("NoLag", true, v -> this.mode.getValue() == Mode.FAST));
    private final Setting<Integer> height = this.add(new Setting<>("Height", 10, 1, 20));
    private final Timer lagTimer = new Timer();
    private boolean useTimer;

    public FastFall() {
        super("FastFall", "Miyagi son simulator", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        Managers.TIMER.reset();
        this.useTimer = false;
    }

    @Override
    public String getInfo() {
        return Managers.TEXT.normalizeCases(this.mode.getValue());
    }

    @Override
    public void onTick() {
        if (this.height.getValue() > 0 && this.traceDown() > this.height.getValue() || FastFall.mc.player.isEntityInsideOpaqueBlock() || FastFall.mc.player.isInWater() || FastFall.mc.player.isInLava() || FastFall.mc.player.isOnLadder() || !this.lagTimer.passedMs(1000L) || FastFall.fullNullCheck()) {
            if (!(!ElytraFly.INSTANCE.isOff() && ElytraFly.INSTANCE.boostTimer.getValue() || NewStep.timer)) {
                Managers.TIMER.reset();
            }
            return;
        }
        if (FastFall.mc.player.isInWeb) {
            return;
        }
        if (FastFall.mc.player.onGround && this.mode.getValue() == Mode.FAST) {
            FastFall.mc.player.motionY = FastFall.mc.player.motionY - (double)(this.noLag.getValue() ? 0.62f : 1.0f);
        }
        if (this.traceDown() != 0 && this.traceDown() <= this.height.getValue() && this.trace() && FastFall.mc.player.onGround) {
            FastFall.mc.player.motionX *= 0.05f;
            FastFall.mc.player.motionZ *= 0.05f;
        }
        if (this.mode.getValue() == Mode.STRICT) {
            if (!FastFall.mc.player.onGround) {
                if (FastFall.mc.player.motionY < 0.0 && this.useTimer) {
                    Managers.TIMER.set(2.5f);
                    return;
                }
                this.useTimer = false;
            } else {
                FastFall.mc.player.motionY = -0.08;
                this.useTimer = true;
            }
        }
        Managers.TIMER.reset();
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (!FastFall.fullNullCheck() && event.getPacket() instanceof SPacketPlayerPosLook) {
            this.lagTimer.reset();
        }
    }

    private int traceDown() {
        int y;
        int retval = 0;
        for (int tracey = y = (int)Math.round(FastFall.mc.player.posY) - 1; tracey >= 0; --tracey) {
            RayTraceResult trace = FastFall.mc.world.rayTraceBlocks(FastFall.mc.player.getPositionVector(), new Vec3d(FastFall.mc.player.posX, tracey, FastFall.mc.player.posZ), false);
            if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK) {
                return retval;
            }
            ++retval;
        }
        return retval;
    }

    private boolean trace() {
        AxisAlignedBB bbox = FastFall.mc.player.getEntityBoundingBox();
        Vec3d basepos = bbox.getCenter();
        double minX = bbox.minX;
        double minZ = bbox.minZ;
        double maxX = bbox.maxX;
        double maxZ = bbox.maxZ;
        HashMap<Vec3d, Vec3d> positions = new HashMap<>();
        positions.put(basepos, new Vec3d(basepos.x, basepos.y - 1.0, basepos.z));
        positions.put(new Vec3d(minX, basepos.y, minZ), new Vec3d(minX, basepos.y - 1.0, minZ));
        positions.put(new Vec3d(maxX, basepos.y, minZ), new Vec3d(maxX, basepos.y - 1.0, minZ));
        positions.put(new Vec3d(minX, basepos.y, maxZ), new Vec3d(minX, basepos.y - 1.0, maxZ));
        positions.put(new Vec3d(maxX, basepos.y, maxZ), new Vec3d(maxX, basepos.y - 1.0, maxZ));
        for (Vec3d key : positions.keySet()) {
            RayTraceResult result = FastFall.mc.world.rayTraceBlocks(key, positions.get(key), true);
            if (result == null || result.typeOfHit != RayTraceResult.Type.BLOCK) continue;
            return false;
        }
        IBlockState state = FastFall.mc.world.getBlockState(new BlockPos(FastFall.mc.player.posX, FastFall.mc.player.posY - 1.0, FastFall.mc.player.posZ));
        return state.getBlock() == Blocks.AIR;
    }

    private static enum Mode {
        FAST,
        STRICT

    }
}


package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class AnvilAura
extends Module {
    public final Setting<Float> targetRange = this.add(new Setting<>("TargetRange", 5.0f, 0.0f, 10.0f));
    public final Setting<Float> placeRange = this.add(new Setting<>("PlaceRange", 5.0f, 0.0f, 10.0f));
    public final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
    public final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
    private final Setting<Integer> delay = this.add(new Setting<>("Delay", 50, 0, 2000));
    private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 1, 8));
    private final Setting<Double> maxTargetSpeed = this.add(new Setting<>("MaxTargetSpeed", 10.0, 0.0, 30.0));
    private final Timer delayTimer = new Timer();
    private int progress = 0;

    public AnvilAura() {
        super("AnvilAura", "Useless", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (!this.delayTimer.passedMs(this.delay.getValue())) {
            return;
        }
        this.progress = 0;
        block0: for (EntityPlayer player : AnvilAura.mc.world.playerEntities) {
            BlockPos pos;
            if (Managers.SPEED.getPlayerSpeed(player) > this.maxTargetSpeed.getValue() || EntityUtil.invalid(player, this.targetRange.getValue()) || !AnvilAura.mc.world.isAirBlock((pos = EntityUtil.getEntityPos(player)).up())) continue;
            for (int i = 10; i > 1; --i) {
                if (!this.checkAnvil(pos.up(i), pos.up())) continue;
                this.placeAnvil(pos.up(i));
                continue block0;
            }
        }
    }

    private boolean checkAnvil(BlockPos anvilPos, BlockPos targetPos) {
        if (!this.canPlace(anvilPos)) {
            return false;
        }
        for (int i = 0; i < anvilPos.getY() - targetPos.getY(); ++i) {
            if (AnvilAura.mc.world.isAirBlock(anvilPos.down(i))) continue;
            return false;
        }
        return true;
    }

    private boolean canPlace(BlockPos pos) {
        if (!BlockUtil.canPlace(pos)) {
            return false;
        }
        return !(AnvilAura.mc.player.getDistance((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5) > (double) this.placeRange.getValue());
    }

    private void placeAnvil(BlockPos pos) {
        if (this.progress >= this.multiPlace.getValue()) {
            return;
        }
        if (InventoryUtil.findHotbarBlock(Blocks.ANVIL) == -1) {
            return;
        }
        if (!this.canPlace(pos)) {
            return;
        }
        int old = AnvilAura.mc.player.inventory.currentItem;
        this.delayTimer.reset();
        ++this.progress;
        InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.ANVIL));
        BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
        InventoryUtil.doSwap(old);
    }
}


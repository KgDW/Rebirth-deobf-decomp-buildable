package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.managers.impl.BreakManager;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.Surround;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class AntiCity
extends Module {
    private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
    private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
    private final Setting<Boolean> breakCrystal = this.add(new Setting<>("AttackCrystal", true).setParent());
    private final Setting<Boolean> eatingPause = this.add(new Setting<>("EatingPause", true, v -> this.breakCrystal.isOpen()));
    private final Setting<Boolean> onlySurround = this.add(new Setting<>("OnlySurround", true));
    private final Setting<Integer> delay = this.add(new Setting<>("Delay", 100, 0, 1000));
    private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 1, 8));
    private final Setting<Mode> mode = this.add(new Setting<>("Mode", Mode.Semi));
    final Timer delayTimer = new Timer();
    private int progress = 0;

    public AntiCity() {
        super("AntiCity", "test", Category.COMBAT);
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @Override
    public void onTick() {
        if (!Surround.INSTANCE.isOn() && this.onlySurround.getValue() || !this.delayTimer.passedMs(this.delay.getValue())) {
            return;
        }
        this.progress = 0;
        BlockPos player = EntityUtil.getPlayerPos();
        for (EnumFacing i : EnumFacing.VALUES) {
            if (i == EnumFacing.DOWN || i == EnumFacing.UP || this.getBlock(player.offset(i)) != Blocks.OBSIDIAN || !BreakManager.isMine(player.offset(i)) && !BlockUtil.checkEntity(player.offset(i))) continue;
            if (this.mode.getValue() == Mode.Single) {
                this.placeBlock(player.offset(i, 2));
                continue;
            }
            for (EnumFacing i2 : EnumFacing.VALUES) {
                if (i2 == EnumFacing.DOWN || i2 == EnumFacing.UP) continue;
                this.placeBlock(player.offset(i).offset(i2));
            }
            if (this.mode.getValue() != Mode.Full) continue;
            this.placeBlock(player.offset(i).up());
        }
    }

    private void placeBlock(BlockPos pos) {
        if (!BlockUtil.canPlace(pos)) {
            if (this.breakCrystal.getValue()) {
                CombatUtil.attackCrystal(pos, this.rotate.getValue(), this.eatingPause.getValue());
            }
            return;
        }
        if (this.progress >= this.multiPlace.getValue()) {
            return;
        }
        int old = AntiCity.mc.player.inventory.currentItem;
        if (InventoryUtil.findHotbarClass(BlockObsidian.class) != -1) {
            InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockObsidian.class));
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
            InventoryUtil.doSwap(old);
            this.delayTimer.reset();
            ++this.progress;
        }
    }

    private Block getBlock(BlockPos pos) {
        return AntiCity.mc.world.getBlockState(pos).getBlock();
    }

    public static enum Mode {
        Single,
        Semi,
        Full

    }
}


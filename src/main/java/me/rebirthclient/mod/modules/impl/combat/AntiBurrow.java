package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.CombatSetting;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class AntiBurrow
extends Module {
    private final Setting<Block> blockSetting = this.add(new Setting<>("Block", Block.RedStone));
    public final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
    public final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
    public final Setting<Integer> delay = this.add(new Setting<>("Delay", 50, 0, 500));
    private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 1, 4));
    private final Timer timer = new Timer();
    private final Setting<Boolean> onlyGround = this.add(new Setting<>("SelfGround", true));
    private final Setting<Float> range = this.add(new Setting<>("Range", 5.0f, 1.0f, 6.0f));
    public EntityPlayer target;
    private int progress = 0;

    public AntiBurrow() {
        super("AntiBurrow", "put something under foot", Category.COMBAT);
    }

    @Override
    public String getInfo() {
        if (this.target != null) {
            return this.target.getName();
        }
        return null;
    }

    @Override
    public void onTick() {
        if (!this.timer.passedMs(this.delay.getValue())) {
            return;
        }
        this.target = CombatUtil.getTarget(this.range.getValue());
        if (this.target == null) {
            return;
        }
        if (this.onlyGround.getValue() && !AntiBurrow.mc.player.onGround) {
            return;
        }
        this.progress = 0;
        this.placeBlock(new BlockPos(this.target.posX + 0.2, this.target.posY + 0.5, this.target.posZ + 0.2));
        this.placeBlock(new BlockPos(this.target.posX - 0.2, this.target.posY + 0.5, this.target.posZ + 0.2));
        this.placeBlock(new BlockPos(this.target.posX - 0.2, this.target.posY + 0.5, this.target.posZ - 0.2));
        this.placeBlock(new BlockPos(this.target.posX + 0.2, this.target.posY + 0.5, this.target.posZ - 0.2));
    }

    private boolean checkEntity(BlockPos pos) {
        for (Entity entity : AntiBurrow.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (!(entity instanceof EntityPlayer) || entity == AntiBurrow.mc.player) continue;
            return true;
        }
        return false;
    }

    private void placeBlock(BlockPos pos) {
        if (this.progress >= this.multiPlace.getValue()) {
            return;
        }
        if (!this.checkEntity(pos)) {
            return;
        }
        if (!this.canPlace(pos)) {
            return;
        }
        int old = AntiBurrow.mc.player.inventory.currentItem;
        if (this.blockSetting.getValue() == Block.Button && (InventoryUtil.findHotbarBlock(Blocks.STONE_BUTTON) != -1 || InventoryUtil.findHotbarBlock(Blocks.WOODEN_BUTTON) != -1)) {
            if (InventoryUtil.findHotbarBlock(Blocks.STONE_BUTTON) != -1) {
                InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.STONE_BUTTON));
            } else {
                InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.WOODEN_BUTTON));
            }
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
        } else if (this.blockSetting.getValue() == Block.RedStone && InventoryUtil.findItemInHotbar(Items.REDSTONE) != -1) {
            InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.REDSTONE));
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
        } else {
            return;
        }
        ++this.progress;
        InventoryUtil.doSwap(old);
        this.timer.reset();
    }

    public boolean canPlace(BlockPos pos) {
        if (BlockUtil.canBlockReplace(pos.down())) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        if (!CombatSetting.INSTANCE.strictPlace.getValue()) {
            return true;
        }
        for (EnumFacing side : BlockUtil.getPlacableFacings(pos, true, CombatSetting.INSTANCE.checkRaytrace.getValue())) {
            if (!BlockUtil.canClick(pos.offset(side))) continue;
            return true;
        }
        return false;
    }

    private static enum Block {
        Button,
        RedStone

    }
}


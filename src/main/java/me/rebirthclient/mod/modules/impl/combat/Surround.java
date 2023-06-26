package me.rebirthclient.mod.modules.impl.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.movement.AutoCenter;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class Surround
extends Module {
    public static Surround INSTANCE = new Surround();
    public final Setting<Boolean> enableInHole = this.add(new Setting<>("EnableInHole", false));
    final Timer timer = new Timer();
    private final Setting<Integer> delay = this.add(new Setting<>("Delay", 50, 0, 500));
    private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 1, 8));
    private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
    private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
    private final Setting<Boolean> breakCrystal = this.add(new Setting<>("BreakCrystal", true).setParent());
    public final Setting<Float> safeHealth = this.add(new Setting<>("SafeHealth", 16.0f, 0.0f, 36.0f, v -> this.breakCrystal.isOpen()));
    private final Setting<Boolean> center = this.add(new Setting<>("Center", true));
    private final Setting<Boolean> extend = this.add(new Setting<>("Extend", true));
    private final Setting<Boolean> onlyGround = this.add(new Setting<>("OnlyGround", true));
    private final Setting<Boolean> moveDisable = this.add(new Setting<>("MoveDisable", true).setParent());
    private final Setting<Boolean> strictDisable = this.add(new Setting<>("StrictDisable", false, v -> this.moveDisable.isOpen()));
    private final Setting<Boolean> isMoving = this.add(new Setting<>("isMoving", true, v -> this.moveDisable.isOpen()));
    private final Setting<Boolean> jumpDisable = this.add(new Setting<>("JumpDisable", true).setParent());
    private final Setting<Boolean> inMoving = this.add(new Setting<>("inMoving", true, v -> this.jumpDisable.isOpen()));
    double startX = 0.0;
    double startY = 0.0;
    double startZ = 0.0;
    int progress = 0;
    BlockPos startPos = null;

    public Surround() {
        super("Surround", "Surrounds you with Obsidian", Category.COMBAT);
        INSTANCE = this;
    }

    static boolean checkSelf(BlockPos pos) {
        Entity test = null;
        for (Entity entity : Surround.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (entity != Surround.mc.player || test != null) continue;
            test = entity;
        }
        return test != null;
    }

    @Override
    public void onEnable() {
        this.startPos = EntityUtil.getPlayerPos();
        this.startX = Surround.mc.player.posX;
        this.startY = Surround.mc.player.posY;
        this.startZ = Surround.mc.player.posZ;
        if (this.center.getValue() && InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN) != -1) {
            AutoCenter.INSTANCE.enable();
        }
    }

    @Override
    public void onTick() {
        if (!this.timer.passedMs(this.delay.getValue())) {
            return;
        }
        this.progress = 0;
        BlockPos pos = EntityUtil.getPlayerPos();
        if (this.startPos == null || !EntityUtil.getPlayerPos().equals(this.startPos) && this.moveDisable.getValue() && this.strictDisable.getValue() && (!this.isMoving.getValue() || MovementUtil.isMoving()) || Surround.mc.player.getDistance(this.startX, this.startY, this.startZ) > 1.3 && this.moveDisable.getValue() && !this.strictDisable.getValue() && (!this.isMoving.getValue() || MovementUtil.isMoving()) || this.jumpDisable.getValue() && (this.startY - Surround.mc.player.posY > 0.5 || this.startY - Surround.mc.player.posY < -0.5) && (!this.inMoving.getValue() || MovementUtil.isMoving())) {
            this.disable();
            return;
        }
        if (InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN) == -1) {
            this.sendMessage(ChatFormatting.RED + "Obsidian?");
            this.disable();
            return;
        }
        if (this.onlyGround.getValue() && !Surround.mc.player.onGround) {
            return;
        }
        for (EnumFacing i : EnumFacing.VALUES) {
            if (i == EnumFacing.UP || this.isGod(pos.offset(i), i)) continue;
            BlockPos offsetPos = pos.offset(i);
            if (BlockUtil.canPlaceEnum(offsetPos)) {
                this.placeBlock(offsetPos);
            } else if (BlockUtil.canReplace(offsetPos)) {
                this.placeBlock(offsetPos.down());
            }
            if (!Surround.checkSelf(offsetPos) || !this.extend.getValue()) continue;
            for (EnumFacing i2 : EnumFacing.VALUES) {
                if (i2 == EnumFacing.UP) continue;
                BlockPos offsetPos2 = offsetPos.offset(i2);
                if (Surround.checkSelf(offsetPos2)) {
                    for (EnumFacing i3 : EnumFacing.VALUES) {
                        if (i3 == EnumFacing.UP) continue;
                        this.placeBlock(offsetPos2);
                        BlockPos offsetPos3 = offsetPos2.offset(i3);
                        this.placeBlock(BlockUtil.canPlaceEnum(offsetPos3) ? offsetPos3 : offsetPos3.down());
                    }
                }
                this.placeBlock(BlockUtil.canPlaceEnum(offsetPos2) ? offsetPos2 : offsetPos2.down());
            }
        }
    }

    private boolean isGod(BlockPos pos, EnumFacing facing) {
        if (Surround.mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK) {
            return true;
        }
        for (EnumFacing i : EnumFacing.VALUES) {
            if (i == facing.getOpposite() || Surround.mc.world.getBlockState(pos.offset(i)).getBlock() == Blocks.BEDROCK) continue;
            return false;
        }
        return true;
    }

    private void placeBlock(BlockPos pos) {
        if (!BlockUtil.canPlace3(pos)) {
            return;
        }
        if (!(this.breakCrystal.getValue() && EntityUtil.getHealth(Surround.mc.player) >= this.safeHealth.getValue() || BlockUtil.canPlace(pos))) {
            return;
        }
        if (this.progress >= this.multiPlace.getValue()) {
            return;
        }
        int old = Surround.mc.player.inventory.currentItem;
        if (InventoryUtil.findHotbarClass(BlockObsidian.class) == -1) {
            return;
        }
        if (this.breakCrystal.getValue() && EntityUtil.getHealth(Surround.mc.player) >= this.safeHealth.getValue()) {
            CombatUtil.attackCrystal(pos, this.rotate.getValue(), false);
        }
        InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockObsidian.class));
        BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), this.breakCrystal.getValue() && EntityUtil.getHealth(Surround.mc.player) >= this.safeHealth.getValue(), false);
        InventoryUtil.doSwap(old);
        ++this.progress;
        this.timer.reset();
    }
}


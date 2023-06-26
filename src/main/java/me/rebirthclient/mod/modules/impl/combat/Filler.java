package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.AutoWeb;
import me.rebirthclient.mod.modules.impl.combat.CombatSetting;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class Filler
extends Module {
    private final Timer timer = new Timer();
    private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 0, 8));
    private final Setting<Integer> delay = this.add(new Setting<>("Delay", 50, 0, 500));
    private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
    private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
    private final Setting<Boolean> holeCheck = this.add(new Setting<>("HoleCheck", true).setParent());
    private final Setting<Float> holeRange = this.add(new Setting<>("HoleRange", 2.0f, 0.5f, 3.0f, v -> this.holeCheck.isOpen()));
    private final Setting<Integer> check = this.add(new Setting<>("Check", 3, 2, 4, v -> this.holeCheck.isOpen()));
    private final Setting<Boolean> anyBlock = this.add(new Setting<>("anyBlock", true, v -> this.holeCheck.isOpen()));
    private final Setting<Boolean> onlyCanStand = this.add(new Setting<>("OnlyCanStand", false, v -> this.holeCheck.isOpen()));
    private final Setting<Boolean> allowUp = this.add(new Setting<>("AllowUp", false, v -> this.holeCheck.isOpen()));
    private final Setting<Float> minSelfRange = this.add(new Setting<>("MinSelfRange", 2.0f, 1.0f, 4.0f, v -> this.holeCheck.isOpen()));
    private final Setting<Boolean> raytrace = this.add(new Setting<>("Raytrace", false));
    private final Setting<Boolean> web = this.add(new Setting<>("Web", true));
    private final Setting<Boolean> noInWeb = this.add(new Setting<>("NoInWeb", false));
    private final Setting<Float> range = this.add(new Setting<>("Range", 5.0f, 1.0f, 6.0f));
    private final Setting<Float> placeRange = this.add(new Setting<>("PlaceRange", 4.0f, 1.0f, 6.0f));
    private final Setting<Double> maxSelfSpeed = this.add(new Setting<>("MaxSelfSpeed", 20.0, 1.0, 30.0));
    private final Setting<Double> minTargetSpeed = this.add(new Setting<>("MinTargetSpeed", 6.0, 0.0, 20.0));
    private final Setting<Boolean> air = this.add(new Setting<>("SelfGround", false));
    public EntityPlayer target;
    int progress = 0;

    public Filler() {
        super("Filler", "Automatically pave the road for the enemy", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
        if (!this.timer.passedMs(this.delay.getValue())) {
            return;
        }
        this.progress = 0;
        if (this.air.getValue() && !Filler.mc.player.onGround) {
            this.target = null;
            return;
        }
        if (Managers.SPEED.getPlayerSpeed(Filler.mc.player) > this.maxSelfSpeed.getValue()) {
            this.target = null;
            return;
        }
        boolean found = false;
        for (EntityPlayer player : Filler.mc.world.playerEntities) {
            if (this.progress >= this.multiPlace.getValue()) {
                return;
            }
            if (EntityUtil.invalid(player, this.range.getValue()) || AutoWeb.isInWeb(player) && this.noInWeb.getValue() || Managers.SPEED.getPlayerSpeed(player) < this.minTargetSpeed.getValue()) continue;
            this.target = player;
            found = true;
            if (this.holeCheck.getValue()) {
                for (BlockPos pos : BlockUtil.getBox(this.holeRange.getValue() + 2.0f, EntityUtil.getEntityPos(player).down())) {
                    if (pos.getY() >= EntityUtil.getEntityPos(player).getY() && !this.allowUp.getValue() || pos.equals(EntityUtil.getEntityPos(player))) continue;
                    if (this.allowUp.getValue()) {
                        boolean skip = false;
                        for (EnumFacing side : EnumFacing.values()) {
                            if (side == EnumFacing.UP || side == EnumFacing.DOWN || !pos.equals(EntityUtil.getEntityPos(player).offset(side))) continue;
                            skip = true;
                            break;
                        }
                        if (skip) continue;
                    }
                    if (player.getDistance((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5) > (double) this.holeRange.getValue() && player.getDistance((double)pos.getX() + 0.5, pos.getY() + 1, (double)pos.getZ() + 0.5) > (double) this.holeRange.getValue() || Filler.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > (double) this.placeRange.getValue()) continue;
                    this.placeBlock(pos);
                }
                continue;
            }
            BlockPos feet = new BlockPos(this.target.posX, this.target.posY + 0.5, this.target.posZ);
            this.placeBlock(feet.down());
            for (EnumFacing side : EnumFacing.values()) {
                if (side == EnumFacing.UP || side == EnumFacing.DOWN) continue;
                this.placeBlock(feet.offset(side).down());
            }
        }
        if (!found) {
            this.target = null;
        }
    }

    private void placeBlock(BlockPos pos) {
        if (this.progress >= this.multiPlace.getValue()) {
            return;
        }
        if (Filler.mc.player.getDistance((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5) > (double) this.placeRange.getValue()) {
            return;
        }
        if (Filler.mc.player.getDistance((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5) <= (double) this.minSelfRange.getValue()) {
            return;
        }
        if (this.holeCheck.getValue() && !CombatUtil.isHole(pos, this.anyBlock.getValue(), this.check.getValue(), this.onlyCanStand.getValue())) {
            return;
        }
        if (!this.canPlace(pos)) {
            return;
        }
        int old = Filler.mc.player.inventory.currentItem;
        if (this.web.getValue() && InventoryUtil.findHotbarClass(BlockWeb.class) != -1) {
            InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockWeb.class));
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
            InventoryUtil.doSwap(old);
        } else if (InventoryUtil.findHotbarClass(BlockObsidian.class) != -1) {
            InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockObsidian.class));
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
            InventoryUtil.doSwap(old);
        }
        this.timer.reset();
        ++this.progress;
    }

    @Override
    public String getInfo() {
        if (this.target != null) {
            return this.target.getName();
        }
        return null;
    }

    private boolean canPlace(BlockPos pos) {
        if (!BlockUtil.canBlockFacing(pos)) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        if (!this.strictPlaceCheck(pos)) {
            return false;
        }
        if (this.web.getValue() && InventoryUtil.findHotbarBlock(Blocks.WEB) != -1) {
            return true;
        }
        return !BlockUtil.checkEntity(pos);
    }

    private boolean strictPlaceCheck(BlockPos pos) {
        if (!CombatSetting.INSTANCE.strictPlace.getValue() && this.raytrace.getValue()) {
            return true;
        }
        for (EnumFacing side : BlockUtil.getPlacableFacings(pos, true, !this.raytrace.getValue() || CombatSetting.INSTANCE.checkRaytrace.getValue())) {
            if (!BlockUtil.canClick(pos.offset(side))) continue;
            return true;
        }
        return false;
    }
}


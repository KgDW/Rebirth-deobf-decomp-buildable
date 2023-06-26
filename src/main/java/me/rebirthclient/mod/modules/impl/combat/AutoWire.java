package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.DamageUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;

public class AutoWire
extends Module {
    private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
    private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
    private final Setting<Boolean> onlyGround = this.add(new Setting<>("OnlyGround", true));
    private final Setting<Boolean> face = this.add(new Setting<>("Face", true));
    private final Setting<Boolean> checkDamage = this.add(new Setting<>("CheckDamage", true).setParent());
    private final Setting<Float> crystalRange = this.add(new Setting<>("CrystalRange", 6.0f, 0.0f, 16.0f, v -> this.checkDamage.isOpen()));
    private final Setting<Double> minDamage = this.add(new Setting<>("MinDamage", 5.0, 0.0, 20.0, v -> this.checkDamage.isOpen()));
    private final Setting<Double> maxSelfSpeed = this.add(new Setting<>("MaxSelfSpeed", 12.0, 1.0, 30.0));
    boolean active = false;

    public AutoWire() {
        super("AutoWire", "", Category.COMBAT);
    }

    @Override
    public void onTick() {
        int old;
        if (InventoryUtil.findItemInHotbar(Items.STRING) == -1) {
            this.active = false;
            return;
        }
        if (this.onlyGround.getValue() && (!AutoWire.mc.player.onGround || MovementUtil.isJumping())) {
            this.active = false;
            return;
        }
        if (Managers.SPEED.getPlayerSpeed(AutoWire.mc.player) > this.maxSelfSpeed.getValue()) {
            this.active = false;
            return;
        }
        if (this.checkDamage.getValue()) {
            boolean shouldReturn = true;
            for (Entity crystal : AutoWire.mc.world.loadedEntityList) {
                float selfDamage;
                if (!(crystal instanceof EntityEnderCrystal) || AutoWire.mc.player.getDistance(crystal) > this.crystalRange.getValue() || !((double)(selfDamage = DamageUtil.calculateDamage(crystal, AutoWire.mc.player)) > this.minDamage.getValue())) continue;
                shouldReturn = false;
                break;
            }
            if (shouldReturn) {
                this.active = false;
                return;
            }
        }
        this.active = true;
        if (BlockUtil.canBlockFacing(EntityUtil.getPlayerPos()) && AutoWire.mc.world.isAirBlock(EntityUtil.getPlayerPos())) {
            old = AutoWire.mc.player.inventory.currentItem;
            if (InventoryUtil.findItemInHotbar(Items.STRING) == -1) {
                return;
            }
            InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.STRING));
            BlockUtil.placeBlock(EntityUtil.getPlayerPos(), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
            InventoryUtil.doSwap(old);
        }
        if (this.face.getValue() && BlockUtil.canBlockFacing(EntityUtil.getPlayerPos().up()) && AutoWire.mc.world.isAirBlock(EntityUtil.getPlayerPos().up())) {
            old = AutoWire.mc.player.inventory.currentItem;
            if (InventoryUtil.findItemInHotbar(Items.STRING) == -1) {
                return;
            }
            InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.STRING));
            BlockUtil.placeBlock(EntityUtil.getPlayerPos().up(), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
            InventoryUtil.doSwap(old);
        }
    }

    @Override
    public String getInfo() {
        if (this.active) {
            return "Active";
        }
        return null;
    }
}


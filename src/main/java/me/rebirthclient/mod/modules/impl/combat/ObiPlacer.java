package me.rebirthclient.mod.modules.impl.combat;

import java.awt.Color;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.DamageUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.CatCrystal;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ObiPlacer
extends Module {
    public final Setting<Boolean> render = this.add(new Setting<>("Render", true).setParent());
    public final Setting<Boolean> outline = this.add(new Setting<>("Outline", true, v -> this.render.isOpen()).setParent());
    public final Setting<Integer> outlineAlpha = this.add(new Setting<>("OutlineAlpha", 150, 0, 255, v -> this.render.isOpen() && this.outline.isOpen()));
    public final Setting<Boolean> box = this.add(new Setting<>("Box", true, v -> this.render.isOpen()).setParent());
    public final Setting<Integer> boxAlpha = this.add(new Setting<>("BoxAlpha", 70, 0, 255, v -> this.render.isOpen() && this.box.isOpen()));
    private final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255), v -> this.render.isOpen()).hideAlpha());
    private final Setting<Integer> renderTime = this.add(new Setting<>("RenderTime", 3000, 0, 5000, v -> this.render.isOpen()));
    private final Setting<Integer> shrinkTime = this.add(new Setting<>("ShrinkTime", 600, 0, 5000, v -> this.render.isOpen()));
    private FadeUtils shrinkTimer = new FadeUtils(this.shrinkTime.getValue());
    private final Timer delayTimer = new Timer();
    private final Timer renderTimer = new Timer();
    private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
    private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
    private final Setting<Float> range = this.add(new Setting<>("Range", 6.0f, 0.0f, 10.0f));
    private final Setting<Float> wallsRange = this.add(new Setting<>("WallsRange", 3.5f, 0.0f, 10.0f));
    private final Setting<Integer> placeDelay = this.add(new Setting<>("PlaceDelay", 100, 0, 2000));
    private final Setting<Float> minDmg = this.add(new Setting<>("MinDmg", 6.0f, 0.0f, 10.0f));
    private final Setting<Float> placeRange = this.add(new Setting<>("PlaceRange", 4.0f, 1.0f, 6.0f));
    BlockPos placePos;

    public ObiPlacer() {
        super("ObiPlacer", "auto place obi of crystal", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
        EntityPlayer target = CombatUtil.getTarget(this.range.getValue());
        if (!this.delayTimer.passedMs(this.placeDelay.getValue()) || target == null) {
            return;
        }
        this.placePos = this.getPlaceTarget(target);
        if (this.placePos != null && BlockUtil.canPlace(this.placePos)) {
            this.shrinkTimer = new FadeUtils(this.shrinkTime.getValue());
            this.delayTimer.reset();
            this.renderTimer.reset();
            this.placeBlock(this.placePos);
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (this.placePos == null || this.renderTimer.passedMs(this.renderTime.getValue()) || this.renderTimer.passedMs(this.renderTime.getValue()) || !this.render.getValue()) {
            return;
        }
        if (ObiPlacer.mc.world.getBlockState(this.placePos).getBlock() == Blocks.AIR) {
            return;
        }
        if (ObiPlacer.mc.world.getBlockState(this.placePos).getBlock() == Blocks.FIRE) {
            return;
        }
        AxisAlignedBB axisAlignedBB = ObiPlacer.mc.world.getBlockState(this.placePos).getSelectedBoundingBox(ObiPlacer.mc.world, this.placePos).grow(this.shrinkTimer.easeInQuad() / 2.0 - 1.0);
        if (this.outline.getValue()) {
            RenderUtil.drawBBBox(axisAlignedBB, this.color.getValue(), this.outlineAlpha.getValue());
        }
        if (this.box.getValue()) {
            RenderUtil.drawBBFill(axisAlignedBB, this.color.getValue(), this.boxAlpha.getValue());
        }
    }

    private void placeBlock(BlockPos pos) {
        if (!BlockUtil.canPlace(pos)) {
            return;
        }
        int old = ObiPlacer.mc.player.inventory.currentItem;
        if (InventoryUtil.findHotbarClass(BlockObsidian.class) == -1) {
            return;
        }
        InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockObsidian.class));
        BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
        InventoryUtil.doSwap(old);
    }

    @Override
    public String getInfo() {
        if (ObiPlacer.fullNullCheck()) {
            return null;
        }
        EntityPlayer target = CombatUtil.getTarget(this.range.getValue());
        if (target != null) {
            return target.getName();
        }
        return null;
    }

    private boolean canPlaceCrystal(BlockPos pos) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        BlockPos boost2 = obsPos.up(2);
        return (this.getBlock(obsPos) == Blocks.BEDROCK || this.getBlock(obsPos) == Blocks.OBSIDIAN) && this.getBlock(boost) == Blocks.AIR && this.getBlock(boost2) == Blocks.AIR && !this.checkEntity(boost2) && !this.checkEntity(boost);
    }

    private Block getBlock(BlockPos pos) {
        return ObiPlacer.mc.world.getBlockState(pos).getBlock();
    }

    private boolean checkEntity(BlockPos pos) {
        for (Entity entity : ObiPlacer.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (entity.isDead || entity instanceof EntityEnderCrystal) continue;
            return true;
        }
        return false;
    }

    private BlockPos getPlaceTarget(Entity target) {
        for (BlockPos pos : BlockUtil.getBox(5.0f, EntityUtil.getEntityPos(target).down())) {
            float damage;
            if (!this.canPlaceCrystal(pos) || CatCrystal.behindWall(pos) || ObiPlacer.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > (double) this.placeRange.getValue() || !((damage = DamageUtil.calculateDamage(pos.down(), target)) > this.minDmg.getValue())) continue;
            return null;
        }
        BlockPos bestPos = null;
        float bestDamage = 0.0f;
        for (BlockPos pos : BlockUtil.getBox(this.range.getValue())) {
            float damage;
            if (!ObiPlacer.mc.world.isAirBlock(pos) || !ObiPlacer.mc.world.isAirBlock(pos.up()) || !ObiPlacer.mc.world.isAirBlock(pos.up(2)) || ObiPlacer.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > (double) this.placeRange.getValue() || this.checkEntity(pos.up()) || this.checkEntity(pos.up(2)) || !BlockUtil.canPlace(pos) || (double)pos.getY() > (double)new BlockPos(target.posX, target.posY + 0.5, target.posZ).getY() - 0.5 || (damage = DamageUtil.calculateDamage(pos, target)) < this.minDmg.getValue() || ObiPlacer.mc.player.getDistanceSq(pos) >= MathUtil.square(this.wallsRange.getValue()) && BlockUtil.rayTracePlaceCheck(pos, true, 1.0f)) continue;
            if (bestPos == null) {
                bestDamage = damage;
                bestPos = pos;
                continue;
            }
            if (damage < bestDamage) continue;
            bestDamage = damage;
            bestPos = pos;
        }
        return bestPos;
    }
}


package me.rebirthclient.mod.modules.impl.combat;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.events.impl.UpdateWalkingPlayerEvent;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.CrystalUtil;
import me.rebirthclient.api.util.DamageUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CatCrystal
extends Module {
    public static CatCrystal INSTANCE;
    private final Setting<Pages> page = this.add(new Setting<>("Page", Pages.General));
    private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true, v -> this.page.getValue() == Pages.General));
    private final Setting<Boolean> noUsing = this.add(new Setting<>("NoUsing", true, v -> this.page.getValue() == Pages.General).setParent());
    private final Setting<Boolean> onlyFood = this.add(new Setting<>("OnlyFood", false, v -> this.noUsing.isOpen() && this.page.getValue() == Pages.General));
    private final Setting<Integer> switchCooldown = this.add(new Setting<>("SwitchCooldown", 100, 0, 1000, v -> this.page.getValue() == Pages.General));
    private final Setting<Double> antiSuicide = this.add(new Setting<>("AntiSuicide", 3.0, 0.0, 10.0, v -> this.page.getValue() == Pages.General));
    private final Setting<Double> wallRange = this.add(new Setting<>("WallRange", 3.0, 0.0, 6.0, v -> this.page.getValue() == Pages.General));
    private final Setting<Integer> maxTarget = this.add(new Setting<>("MaxTarget", 3, 1, 6, v -> this.page.getValue() == Pages.General));
    private final Setting<Float> crystalRange = this.add(new Setting<>("CrystalRange", 6.0f, 0.0f, 16.0f, v -> this.page.getValue() == Pages.General));
    private final Setting<Double> targetRange = this.add(new Setting<>("TargetRange", 6.0, 0.0, 16.0, v -> this.page.getValue() == Pages.General));
    private final Setting<Integer> updateDelay = this.add(new Setting<>("UpdateDelay", 50, 0, 1000, v -> this.page.getValue() == Pages.General));
    private final Setting<Boolean> place = this.add(new Setting<>("Place", true, v -> this.page.getValue() == Pages.Place));
    private final Setting<Integer> placeDelay = this.add(new Setting<>("PlaceDelay", 300, 0, 1000, v -> this.page.getValue() == Pages.Place && this.place.getValue()));
    private final Setting<Double> placeRange = this.add(new Setting<Number>("PlaceRange", 5.0, 0.0, 6, v -> this.page.getValue() == Pages.Place && this.place.getValue()));
    private final Setting<Double> placeMinDamage = this.add(new Setting<>("PlaceMin", 5.0, 0.0, 36.0, v -> this.page.getValue() == Pages.Place && this.place.getValue()));
    private final Setting<Double> placeMaxSelf = this.add(new Setting<>("PlaceMaxSelf", 12.0, 0.0, 36.0, v -> this.page.getValue() == Pages.Place && this.place.getValue()));
    private final Setting<SwapMode> autoSwap = this.add(new Setting<>("AutoSwap", SwapMode.OFF, v -> this.page.getValue() == Pages.Place && this.place.getValue()));
    private final Setting<Boolean> extraPlace = this.add(new Setting<>("ExtraPlace", true, v -> this.page.getValue() == Pages.Place && this.place.getValue()).setParent());
    private final Setting<Double> extraPlaceDamage = this.add(new Setting<>("ExtraDamage", 8.0, 0.0, 36.0, v -> this.page.getValue() == Pages.Place && this.place.getValue() && this.extraPlace.isOpen()));
    private final Setting<Boolean> Break = this.add(new Setting<>("Break", true, v -> this.page.getValue() == Pages.Break));
    private final Setting<Integer> breakDelay = this.add(new Setting<>("BreakDelay", 300, 0, 1000, v -> this.page.getValue() == Pages.Break && this.Break.getValue()));
    private final Setting<Double> breakRange = this.add(new Setting<>("BreakRange", 5.0, 0.0, 6.0, v -> this.page.getValue() == Pages.Break && this.Break.getValue()));
    private final Setting<Double> breakMinDamage = this.add(new Setting<>("BreakMin", 4.0, 0.0, 36.0, v -> this.page.getValue() == Pages.Break && this.Break.getValue()));
    private final Setting<Double> breakMaxSelf = this.add(new Setting<>("SelfBreak", 12.0, 0.0, 36.0, v -> this.page.getValue() == Pages.Break && this.Break.getValue()));
    private final Setting<Double> preferBreakDamage = this.add(new Setting<>("PreferBreak", 8.0, 0.0, 36.0, v -> this.page.getValue() == Pages.Break && this.Break.getValue()));
    private final Setting<Boolean> breakOnlyHasCrystal = this.add(new Setting<>("OnlyHasCrystal", false, v -> this.page.getValue() == Pages.Break && this.Break.getValue()));
    private final Setting<Boolean> render = this.add(new Setting<>("Render", true, v -> this.page.getValue() == Pages.Render));
    private final Setting<Boolean> outline = this.add(new Setting<>("Outline", true, v -> this.page.getValue() == Pages.Render && this.render.getValue()).setParent());
    private final Setting<Integer> outlineAlpha = this.add(new Setting<>("OutlineAlpha", 150, 0, 255, v -> this.outline.isOpen() && this.page.getValue() == Pages.Render && this.render.getValue()));
    private final Setting<Boolean> box = this.add(new Setting<>("Box", true, v -> this.page.getValue() == Pages.Render && this.render.getValue()).setParent());
    private final Setting<Integer> boxAlpha = this.add(new Setting<>("BoxAlpha", 70, 0, 255, v -> this.box.isOpen() && this.page.getValue() == Pages.Render && this.render.getValue()));
    private final Setting<Boolean> text = this.add(new Setting<>("DamageText", true, v -> this.page.getValue() == Pages.Render && this.render.getValue()));
    private final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255), v -> this.page.getValue() == Pages.Render && this.render.getValue()).hideAlpha());
    private final Setting<Integer> animationTime = this.add(new Setting<>("AnimationTime", 500, 0, 3000, v -> this.page.getValue() == Pages.Render && this.render.getValue()));
    private final Setting<Integer> startTime = this.add(new Setting<>("StartFadeTime", 300, 0, 2000, v -> this.page.getValue() == Pages.Render && this.render.getValue()));
    private final Setting<Integer> fadeTime = this.add(new Setting<>("FadeTime", 300, 0, 2000, v -> this.page.getValue() == Pages.Render && this.render.getValue()));
    private final Setting<Integer> predictTicks = this.add(new Setting<>("PredictTicks", 4, 0, 10, v -> this.page.getValue() == Pages.Predict));
    private final Setting<Boolean> collision = this.add(new Setting<>("Collision", false, v -> this.page.getValue() == Pages.Predict));
    private final Setting<Boolean> terrainIgnore = this.add(new Setting<>("TerrainIgnore", false, v -> this.page.getValue() == Pages.Predict));
    private final Setting<Boolean> slowFace = this.add(new Setting<>("SlowFace", true, v -> this.page.getValue() == Pages.Misc).setParent());
    private final Setting<Integer> slowDelay = this.add(new Setting<>("SlowDelay", 600, 0, 2000, v -> this.page.getValue() == Pages.Misc && this.slowFace.isOpen()));
    private final Setting<Double> slowMinDamage = this.add(new Setting<>("SlowMin", 3.0, 0.0, 36.0, v -> this.page.getValue() == Pages.Misc && this.slowFace.isOpen()));
    private final Setting<Boolean> armorBreaker = this.add(new Setting<>("ArmorBreaker", true, v -> this.page.getValue() == Pages.Misc).setParent());
    private final Setting<Integer> maxDura = this.add(new Setting<>("MaxDura", 8, 0, 100, v -> this.page.getValue() == Pages.Misc && this.armorBreaker.isOpen()));
    private final Setting<Double> armorBreakerDamage = this.add(new Setting<>("BreakerDamage", 3.0, 0.0, 36.0, v -> this.page.getValue() == Pages.Misc && this.armorBreaker.isOpen()));
    private final Timer switchTimer = new Timer();
    private final Timer delayTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private final Timer faceTimer = new Timer();
    private EntityPlayer displayTarget;
    public static BlockPos lastPos;
    private float lastYaw = 0.0f;
    private float lastPitch = 0.0f;
    private int lastHotbar = -1;
    private BigDecimal lastDamage;
    private final Timer noPosTimer = new Timer();
    private BlockPos renderPos = null;
    private AxisAlignedBB lastBB = null;
    private AxisAlignedBB nowBB = null;
    private final FadeUtils fadeUtils = new FadeUtils(500L);
    private final FadeUtils animation = new FadeUtils(500L);

    public CatCrystal() {
        super("CatCrystal", "ez", Category.COMBAT);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        this.update();
    }

    @Override
    public void onUpdate() {
        this.update();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        this.update();
        if (this.nowBB != null && this.render.getValue() && this.fadeUtils.easeOutQuad() < 1.0) {
            if (this.box.getValue()) {
                RenderUtil.drawBBFill(this.nowBB, this.color.getValue(), (int)((double) this.boxAlpha.getValue() * Math.abs(this.fadeUtils.easeOutQuad() - 1.0)));
            }
            if (this.outline.getValue()) {
                RenderUtil.drawBBBox(this.nowBB, this.color.getValue(), (int)((double) this.outlineAlpha.getValue() * Math.abs(this.fadeUtils.easeOutQuad() - 1.0)));
            }
            if (this.text.getValue() && lastPos != null) {
                RenderUtil.drawText(this.nowBB, String.valueOf(this.lastDamage));
            }
        }
    }

    @SubscribeEvent
    public void onUpdateWalk(UpdateWalkingPlayerEvent event) {
        this.update();
    }

    @SubscribeEvent
    public void onMotion(MotionEvent event) {
        if (CatCrystal.fullNullCheck()) {
            return;
        }
        this.update();
        if (lastPos == null || !this.rotate.getValue()) {
            return;
        }
        event.setYaw(this.lastYaw);
        event.setPitch(this.lastPitch);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (CatCrystal.fullNullCheck()) {
            return;
        }
        if (event.getPacket() instanceof CPacketHeldItemChange && ((CPacketHeldItemChange)event.getPacket()).getSlotId() != this.lastHotbar) {
            this.lastHotbar = ((CPacketHeldItemChange)event.getPacket()).getSlotId();
            this.switchTimer.reset();
        }
    }

    private void update() {
        if (CatCrystal.fullNullCheck()) {
            return;
        }
        this.fadeUtils.setLength(this.fadeTime.getValue());
        if (lastPos != null) {
            this.lastBB = CatCrystal.mc.world.getBlockState(new BlockPos(lastPos.down())).getSelectedBoundingBox(CatCrystal.mc.world, new BlockPos(lastPos.down()));
            this.noPosTimer.reset();
            if (this.nowBB == null) {
                this.nowBB = this.lastBB;
            }
            if (this.renderPos == null || !this.renderPos.equals(lastPos)) {
                this.animation.setLength(this.animationTime.getValue() <= 0 ? 0L : (long)((Math.abs(this.nowBB.minX - this.lastBB.minX) + Math.abs(this.nowBB.minY - this.lastBB.minY) + Math.abs(this.nowBB.minZ - this.lastBB.minZ)) * (double) this.animationTime.getValue()));
                this.animation.reset();
                this.renderPos = lastPos;
            }
        }
        if (!this.noPosTimer.passedMs(this.startTime.getValue())) {
            this.fadeUtils.reset();
        }
        double size = this.animation.easeInQuad();
        if (this.nowBB != null && this.lastBB != null) {
            if (Math.abs(this.nowBB.minX - this.lastBB.minX) + Math.abs(this.nowBB.minY - this.lastBB.minY) + Math.abs(this.nowBB.minZ - this.lastBB.minZ) > 5.0) {
                this.nowBB = this.lastBB;
            }
            this.nowBB = new AxisAlignedBB(this.nowBB.minX + (this.lastBB.minX - this.nowBB.minX) * size, this.nowBB.minY + (this.lastBB.minY - this.nowBB.minY) * size, this.nowBB.minZ + (this.lastBB.minZ - this.nowBB.minZ) * size, this.nowBB.maxX + (this.lastBB.maxX - this.nowBB.maxX) * size, this.nowBB.maxY + (this.lastBB.maxY - this.nowBB.maxY) * size, this.nowBB.maxZ + (this.lastBB.maxZ - this.nowBB.maxZ) * size);
        }
        if (!this.delayTimer.passedMs(this.updateDelay.getValue())) {
            return;
        }
        if (this.noUsing.getValue() && (EntityUtil.isEating() || CatCrystal.mc.player.isHandActive() && !this.onlyFood.getValue())) {
            lastPos = null;
            return;
        }
        if (!this.switchTimer.passedMs(this.switchCooldown.getValue())) {
            lastPos = null;
            return;
        }
        if (this.breakOnlyHasCrystal.getValue() && !CatCrystal.mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) && !CatCrystal.mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL) && !this.findCrystal()) {
            lastPos = null;
            return;
        }
        this.delayTimer.reset();
        int times = 0;
        EntityEnderCrystal bestBreakCrystal = null;
        float bestBreakDamage = 0.0f;
        BlockPos bestPlacePos = null;
        float bestPlaceDamage = 0.0f;
        EntityPlayer placeTarget = null;
        EntityPlayer breakTarget = null;
        for (EntityPlayer target : CatCrystal.mc.world.playerEntities) {
            float selfDamage;
            float damage;
            if (EntityUtil.invalid(target, this.targetRange.getValue())) continue;
            if (times >= this.maxTarget.getValue()) break;
            ++times;
            for (Entity crystal : CatCrystal.mc.world.loadedEntityList) {
                if (!(crystal instanceof EntityEnderCrystal) || target.getDistance(crystal) > this.crystalRange.getValue() || (double)CatCrystal.mc.player.getDistance(crystal) > this.breakRange.getValue() || !CatCrystal.mc.player.canEntityBeSeen(crystal) && (double)CatCrystal.mc.player.getDistance(crystal) > this.wallRange.getValue()) continue;
                damage = CrystalUtil.calculateDamage((EntityEnderCrystal)crystal, target, this.predictTicks.getValue(), this.collision.getValue(), this.terrainIgnore.getValue());
                selfDamage = DamageUtil.calculateDamage(crystal, CatCrystal.mc.player);
                if ((double)selfDamage > this.breakMaxSelf.getValue() || this.antiSuicide.getValue() > 0.0 && (double)selfDamage > (double)(CatCrystal.mc.player.getHealth() + CatCrystal.mc.player.getAbsorptionAmount()) - this.antiSuicide.getValue() || (double)damage < this.getBreakDamage(target) || bestBreakCrystal != null && !(damage > bestBreakDamage)) continue;
                breakTarget = target;
                bestBreakCrystal = (EntityEnderCrystal)crystal;
                bestBreakDamage = damage;
            }
            if (!CatCrystal.mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) && !CatCrystal.mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL) && !this.findCrystal()) continue;
            for (BlockPos pos : BlockUtil.getBox(this.crystalRange.getValue(), EntityUtil.getEntityPos(target).down())) {
                if (!BlockUtil.canPlaceCrystal(pos) || CatCrystal.behindWall(pos) || CatCrystal.mc.player.getDistance((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5) > this.placeRange.getValue()) continue;
                damage = CrystalUtil.calculateDamage(pos.down(), target, this.predictTicks.getValue(), this.collision.getValue(), this.terrainIgnore.getValue());
                selfDamage = DamageUtil.calculateDamage(pos.down(), CatCrystal.mc.player);
                if ((double)selfDamage > this.placeMaxSelf.getValue() || this.antiSuicide.getValue() > 0.0 && (double)selfDamage > (double)(CatCrystal.mc.player.getHealth() + CatCrystal.mc.player.getAbsorptionAmount()) - this.antiSuicide.getValue() || (double)damage < this.getPlaceDamage(target) || bestPlacePos != null && !(damage > bestPlaceDamage)) continue;
                placeTarget = target;
                bestPlacePos = pos;
                bestPlaceDamage = damage;
            }
        }
        if (bestPlacePos == null && bestBreakCrystal == null) {
            lastPos = null;
            this.displayTarget = null;
            return;
        }
        if (bestBreakCrystal == null) {
            this.displayTarget = placeTarget;
            this.doPlace(bestPlacePos);
            return;
        }
        if (bestPlacePos == null) {
            this.displayTarget = breakTarget;
            this.doBreak(bestBreakCrystal, bestBreakDamage);
            return;
        }
        if (bestBreakDamage >= bestPlaceDamage || (double)bestBreakDamage >= this.preferBreakDamage.getValue()) {
            this.displayTarget = breakTarget;
            this.doBreak(bestBreakCrystal, bestBreakDamage);
        } else {
            this.displayTarget = placeTarget;
            this.doPlace(bestPlacePos);
        }
    }

    private double getPlaceDamage(EntityPlayer target) {
        if (this.slowFace.getValue() && this.faceTimer.passedMs(this.slowDelay.getValue())) {
            return this.slowMinDamage.getValue();
        }
        if (this.armorBreaker.getValue()) {
            ItemStack helm = target.inventoryContainer.getSlot(5).getStack();
            ItemStack chest = target.inventoryContainer.getSlot(6).getStack();
            ItemStack legging = target.inventoryContainer.getSlot(7).getStack();
            ItemStack feet = target.inventoryContainer.getSlot(8).getStack();
            if (!helm.isEmpty && EntityUtil.getDamagePercent(helm) <= this.maxDura.getValue() || !chest.isEmpty && EntityUtil.getDamagePercent(chest) <= this.maxDura.getValue() || !legging.isEmpty && EntityUtil.getDamagePercent(legging) <= this.maxDura.getValue() || !feet.isEmpty && EntityUtil.getDamagePercent(feet) <= this.maxDura.getValue()) {
                return this.armorBreakerDamage.getValue();
            }
        }
        return this.placeMinDamage.getValue();
    }

    private double getBreakDamage(EntityPlayer target) {
        if (this.slowFace.getValue() && this.faceTimer.passedMs(this.slowDelay.getValue())) {
            return this.slowMinDamage.getValue();
        }
        if (this.armorBreaker.getValue()) {
            ItemStack helm = target.inventoryContainer.getSlot(5).getStack();
            ItemStack chest = target.inventoryContainer.getSlot(6).getStack();
            ItemStack legging = target.inventoryContainer.getSlot(7).getStack();
            ItemStack feet = target.inventoryContainer.getSlot(8).getStack();
            if (!helm.isEmpty && EntityUtil.getDamagePercent(helm) <= this.maxDura.getValue() || !chest.isEmpty && EntityUtil.getDamagePercent(chest) <= this.maxDura.getValue() || !legging.isEmpty && EntityUtil.getDamagePercent(legging) <= this.maxDura.getValue() || !feet.isEmpty && EntityUtil.getDamagePercent(feet) <= this.maxDura.getValue()) {
                return this.armorBreakerDamage.getValue();
            }
        }
        return this.breakMinDamage.getValue();
    }

    private boolean findCrystal() {
        if (this.autoSwap.getValue() == SwapMode.OFF) {
            return false;
        }
        if (this.autoSwap.getValue() == SwapMode.NORMAL || this.autoSwap.getValue() == SwapMode.SILENT) {
            return InventoryUtil.findItemInHotbar(Items.END_CRYSTAL) != -1;
        }
        return InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL, true, true) != -1;
    }

    private void doBreak(EntityEnderCrystal entity, double bestBreakDamage) {
        this.faceTimer.reset();
        if (!this.Break.getValue()) {
            return;
        }
        lastPos = EntityUtil.getEntityPos(entity);
        float[] angle = MathUtil.calcAngle(CatCrystal.mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(entity.posX, entity.posY + 0.25, entity.posZ));
        this.lastYaw = angle[0];
        this.lastPitch = angle[1];
        this.lastDamage = BigDecimal.valueOf(bestBreakDamage).setScale(1, RoundingMode.UP);
        if (!CombatUtil.breakTimer.passedMs(this.breakDelay.getValue())) {
            return;
        }
        CombatUtil.breakTimer.reset();
        CatCrystal.mc.player.connection.sendPacket(new CPacketUseEntity(entity));
        CatCrystal.mc.player.swingArm(EnumHand.MAIN_HAND);
        if (this.rotate.getValue()) {
            EntityUtil.faceXYZ(entity.posX, entity.posY + 0.25, entity.posZ);
        }
        if (!this.placeTimer.passedMs(this.placeDelay.getValue()) || !this.extraPlace.getValue() || bestBreakDamage < this.extraPlaceDamage.getValue()) {
            return;
        }
        this.placeTimer.reset();
        if (CatCrystal.mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) || CatCrystal.mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL)) {
            BlockUtil.placeCrystal(lastPos, this.rotate.getValue());
        } else if (this.findCrystal()) {
            int old = CatCrystal.mc.player.inventory.currentItem;
            int crystal = -1;
            if (this.autoSwap.getValue() == SwapMode.NORMAL || this.autoSwap.getValue() == SwapMode.SILENT) {
                crystal = InventoryUtil.findItemInHotbar(Items.END_CRYSTAL);
                if (crystal == -1) {
                    return;
                }
                InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.END_CRYSTAL));
            }
            if (this.autoSwap.getValue() == SwapMode.BYPASS) {
                crystal = InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL, true, true);
                if (crystal == -1) {
                    return;
                }
                CatCrystal.mc.playerController.windowClick(0, crystal, old, ClickType.SWAP, CatCrystal.mc.player);
            }
            BlockUtil.placeCrystal(lastPos, this.rotate.getValue());
            if (this.autoSwap.getValue() == SwapMode.SILENT && crystal != -1) {
                InventoryUtil.doSwap(old);
            }
            if (this.autoSwap.getValue() == SwapMode.BYPASS && crystal != -1) {
                CatCrystal.mc.playerController.windowClick(0, crystal, old, ClickType.SWAP, CatCrystal.mc.player);
            }
        }
    }

    private void doPlace(BlockPos pos) {
        if (!this.place.getValue()) {
            return;
        }
        if (!(CatCrystal.mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) || CatCrystal.mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL) || this.findCrystal())) {
            lastPos = null;
            return;
        }
        lastPos = pos;
        RayTraceResult result = CatCrystal.mc.world.rayTraceBlocks(new Vec3d(CatCrystal.mc.player.posX, CatCrystal.mc.player.posY + (double)CatCrystal.mc.player.getEyeHeight(), CatCrystal.mc.player.posZ), new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() - 0.5, (double)pos.getZ() + 0.5));
        EnumFacing facing = result == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;
        pos = pos.down();
        this.lastDamage = BigDecimal.valueOf(CrystalUtil.calculateDamage(pos, this.displayTarget, this.predictTicks.getValue(), this.collision.getValue(), this.terrainIgnore.getValue())).setScale(1, RoundingMode.UP);
        EnumFacing opposite = facing.getOpposite();
        Vec3d vec = new Vec3d(pos.up()).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        float[] angle = EntityUtil.getLegitRotations(vec);
        this.lastYaw = angle[0];
        this.lastPitch = angle[1];
        if (!this.placeTimer.passedMs(this.placeDelay.getValue())) {
            return;
        }
        this.placeTimer.reset();
        if (CatCrystal.mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) || CatCrystal.mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL)) {
            BlockUtil.placeCrystal(pos.up(), this.rotate.getValue());
        } else if (this.findCrystal()) {
            int old = CatCrystal.mc.player.inventory.currentItem;
            int crystal = -1;
            if (this.autoSwap.getValue() == SwapMode.NORMAL || this.autoSwap.getValue() == SwapMode.SILENT) {
                crystal = InventoryUtil.findItemInHotbar(Items.END_CRYSTAL);
                if (crystal == -1) {
                    return;
                }
                InventoryUtil.doSwap(crystal);
            }
            if (this.autoSwap.getValue() == SwapMode.BYPASS) {
                crystal = InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL, true, true);
                if (crystal == -1) {
                    return;
                }
                CatCrystal.mc.playerController.windowClick(0, crystal, old, ClickType.SWAP, CatCrystal.mc.player);
            }
            BlockUtil.placeCrystal(pos.up(), this.rotate.getValue());
            if (this.autoSwap.getValue() == SwapMode.SILENT && crystal != -1) {
                InventoryUtil.doSwap(old);
            }
            if (this.autoSwap.getValue() == SwapMode.BYPASS && crystal != -1) {
                CatCrystal.mc.playerController.windowClick(0, crystal, old, ClickType.SWAP, CatCrystal.mc.player);
            }
        }
    }

    public static boolean behindWall(BlockPos pos) {
        RayTraceResult result = CatCrystal.mc.world.rayTraceBlocks(new Vec3d(CatCrystal.mc.player.posX, CatCrystal.mc.player.posY + (double)CatCrystal.mc.player.getEyeHeight(), CatCrystal.mc.player.posZ), new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() - 0.5, (double)pos.getZ() + 0.5));
        if (result != null && result.sideHit != null && CatCrystal.mc.world.rayTraceBlocks(new Vec3d(CatCrystal.mc.player.posX, CatCrystal.mc.player.posY + (double)CatCrystal.mc.player.getEyeHeight(), CatCrystal.mc.player.posZ), new Vec3d((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5), false, true, false) == null) {
            return false;
        }
        return CatCrystal.mc.player.getDistance((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5) > CatCrystal.INSTANCE.wallRange.getValue();
    }

    @Override
    public String getInfo() {
        if (this.displayTarget != null) {
            return this.displayTarget.getName();
        }
        return null;
    }

    public static enum SwapMode {
        OFF,
        NORMAL,
        SILENT,
        BYPASS

    }

    public static enum Pages {
        General,
        Place,
        Break,
        Misc,
        Predict,
        Render

    }
}


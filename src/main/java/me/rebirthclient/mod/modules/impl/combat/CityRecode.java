package me.rebirthclient.mod.modules.impl.combat;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.PacketMine;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class CityRecode
extends Module {
    private static EntityPlayer target;
    private final Setting<Boolean> mineBurrow = this.add(new Setting<>("MineBurrow", true).setParent());
    private final Setting<Boolean> onlyBurrow = this.add(new Setting<>("OnlyBurrow", false, v -> this.mineBurrow.isOpen()));
    private final Setting<Boolean> mineTrap = this.add(new Setting<>("MineTrap", true).setParent());
    private final Setting<Boolean> prefer = this.add(new Setting<>("Prefer", true, v -> this.mineTrap.isOpen()));
    private final Setting<Boolean> render = this.add(new Setting<>("Render", true).setParent());
    private final Setting<Boolean> outline = this.add(new Setting<>("Outline", true, v -> this.render.isOpen()).setParent());
    private final Setting<Integer> outlineAlpha = this.add(new Setting<>("OutlineAlpha", 150, 0, 255, v -> this.render.isOpen() && this.outline.isOpen()));
    private final Setting<Boolean> box = this.add(new Setting<>("Box", true, v -> this.render.isOpen()).setParent());
    private final Setting<Integer> boxAlpha = this.add(new Setting<>("BoxAlpha", 70, 0, 255, v -> this.render.isOpen() && this.box.isOpen()));
    private final Setting<Float> targetRange = this.add(new Setting<>("TargetRange", 5.0f, 1.0f, 8.0f));
    private final Setting<Double> breakRange = this.add(new Setting<>("BreakRange", 4.5, 1.0, 8.0));
    private final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255), v -> this.render.isOpen()).hideAlpha());
    private final Setting<Integer> shrinkTime = this.add(new Setting<>("ShrinkTime", 1200, 0, 3000, v -> this.render.isOpen()));
    private FadeUtils shrinkTimer = new FadeUtils(this.shrinkTime.getValue());
    private final Setting<Boolean> keyMode = this.add(new Setting<>("KeyMode", true));
    private final Setting<Bind> keyBind = this.add(new Setting<>("Enable", new Bind(-1), v -> this.keyMode.getValue()));
    private final Setting<Boolean> autoDisable = this.add(new Setting<>("autoDisable", false, v -> !this.keyMode.getValue()));
    private final Setting<Boolean> debug = this.add(new Setting<>("Debug", false));
    private final Timer renderTimer = new Timer();
    private BlockPos breakPos;
    private final List<Block> godBlocks = Arrays.asList(Blocks.AIR, Blocks.FLOWING_LAVA, Blocks.LAVA, Blocks.FLOWING_WATER, Blocks.WATER, Blocks.BEDROCK);

    public CityRecode() {
        super("CityRecode", "", Category.COMBAT);
    }

    private static boolean check(BlockPos pos, EntityPlayer player) {
        Vec3d[] vec3dList;
        for (Vec3d vec3d : vec3dList = EntityUtil.getVarOffsets(0, 0, 0)) {
            BlockPos position = new BlockPos(pos).add(vec3d.x, vec3d.y, vec3d.z);
            for (Entity entity : CityRecode.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(position))) {
                if (entity != player) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (this.breakPos == null || this.renderTimer.passedMs(this.shrinkTime.getValue()) || !this.render.getValue()) {
            this.shrinkTimer = new FadeUtils(this.shrinkTime.getValue());
            return;
        }
        AxisAlignedBB axisAlignedBB = CityRecode.mc.world.getBlockState(this.breakPos).getSelectedBoundingBox(CityRecode.mc.world, this.breakPos).grow(this.shrinkTimer.easeInQuad() / 2.0 - 1.0);
        if (this.outline.getValue()) {
            RenderUtil.drawBBBox(axisAlignedBB, this.color.getValue(), this.outlineAlpha.getValue());
        }
        if (this.box.getValue()) {
            RenderUtil.drawBBFill(axisAlignedBB, this.color.getValue(), this.boxAlpha.getValue());
        }
    }

    @Override
    public void onUpdate() {
        if (this.keyMode.getValue() && !this.keyBind.getValue().isDown()) {
            return;
        }
        target = CombatUtil.getTarget(this.targetRange.getValue(), 10.0);
        if (target == null) {
            return;
        }
        BlockPos targetPos = EntityUtil.getEntityPos(target);
        if (!this.isAir(targetPos) && this.mineBurrow.getValue()) {
            this.mineBlock(targetPos);
        } else if (!this.isAir(new BlockPos(CityRecode.target.posX + 0.1, CityRecode.target.posY + 0.5, CityRecode.target.posZ + 0.1)) && this.mineBurrow.getValue() && !PacketMine.godBlocks.contains(this.getBlock(new BlockPos(CityRecode.target.posX + 0.1, CityRecode.target.posY + 0.5, CityRecode.target.posZ + 0.1)))) {
            this.mineBlock(new BlockPos(CityRecode.target.posX + 0.1, CityRecode.target.posY + 0.5, CityRecode.target.posZ + 0.1));
        } else if (!this.isAir(new BlockPos(CityRecode.target.posX + 0.1, CityRecode.target.posY + 0.5, CityRecode.target.posZ - 0.1)) && this.mineBurrow.getValue() && !PacketMine.godBlocks.contains(this.getBlock(new BlockPos(CityRecode.target.posX + 0.1, CityRecode.target.posY + 0.5, CityRecode.target.posZ - 0.1)))) {
            this.mineBlock(new BlockPos(CityRecode.target.posX + 0.1, CityRecode.target.posY + 0.5, CityRecode.target.posZ - 0.1));
        } else if (!this.isAir(new BlockPos(CityRecode.target.posX - 0.1, CityRecode.target.posY + 0.5, CityRecode.target.posZ + 0.1)) && this.mineBurrow.getValue() && !PacketMine.godBlocks.contains(this.getBlock(new BlockPos(CityRecode.target.posX - 0.1, CityRecode.target.posY + 0.5, CityRecode.target.posZ + 0.1)))) {
            this.mineBlock(new BlockPos(CityRecode.target.posX - 0.1, CityRecode.target.posY + 0.5, CityRecode.target.posZ + 0.1));
        } else if (!this.isAir(new BlockPos(CityRecode.target.posX - 0.1, CityRecode.target.posY + 0.5, CityRecode.target.posZ - 0.1)) && this.mineBurrow.getValue() && !PacketMine.godBlocks.contains(this.getBlock(new BlockPos(CityRecode.target.posX - 0.1, CityRecode.target.posY + 0.5, CityRecode.target.posZ - 0.1)))) {
            this.mineBlock(new BlockPos(CityRecode.target.posX - 0.1, CityRecode.target.posY + 0.5, CityRecode.target.posZ - 0.1));
        } else if (!(this.mineBurrow.getValue() && this.onlyBurrow.getValue() || this.canAttack(target, targetPos))) {
            if (this.debug.getValue()) {
                this.sendMessage("do mine surround");
            }
            if (!(this.doMine(targetPos) || this.doMine(targetPos.add(0, 0, 1)) || this.doMine(targetPos.add(0, 0, -1)) || this.doMine(targetPos.add(1, 0, 0)) || this.doMine(targetPos.add(-1, 0, 0)) || this.doMine(targetPos.add(1, 0, 1)) || this.doMine(targetPos.add(-1, 0, -1)) || this.doMine(targetPos.add(-1, 0, 1)))) {
                this.doMine(targetPos.add(1, 0, -1));
            }
        }
    }

    private boolean doMine(BlockPos targetPos) {
        int progress;
        if (!CityRecode.check(targetPos, target)) {
            return false;
        }
        double distance = 0.0;
        EnumFacing best = null;
        if (this.prefer.getValue() && this.mineTrap.getValue()) {
            for (EnumFacing i : EnumFacing.VALUES) {
                if (this.doCheck(targetPos, i)) continue;
                progress = 0;
                if (this.isAir(targetPos.offset(i))) {
                    ++progress;
                }
                if (this.isAir(targetPos.offset(i).up())) {
                    ++progress;
                }
                if (progress < 1 || best != null && !(CityRecode.mc.player.getDistance((double)targetPos.offset(i).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i).getZ() + 0.5) < distance)) continue;
                distance = CityRecode.mc.player.getDistance((double)targetPos.offset(i).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i).getZ() + 0.5);
                best = i;
            }
            if (best != null && this.doMineTrap(targetPos, best)) {
                return true;
            }
            for (EnumFacing i : EnumFacing.VALUES) {
                if (this.doCheck(targetPos, i)) continue;
                progress = 0;
                if (this.isAir(targetPos.offset(i))) {
                    ++progress;
                }
                if (this.isAir(targetPos.offset(i).up())) {
                    ++progress;
                }
                if (progress < 1 || !this.doMineTrap(targetPos, i)) continue;
                return true;
            }
        }
        distance = 0.0;
        best = null;
        for (EnumFacing i : EnumFacing.VALUES) {
            if (CityRecode.check(targetPos.offset(i), target) || this.cantMine(targetPos, i, 2) || best != null && !(CityRecode.mc.player.getDistance((double)targetPos.offset(i, 2).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i, 2).getZ() + 0.5) < distance)) continue;
            distance = CityRecode.mc.player.getDistance((double)targetPos.offset(i, 2).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i, 2).getZ() + 0.5);
            best = i;
        }
        if (best != null && this.doMineExtend(targetPos, best)) {
            return true;
        }
        for (EnumFacing i : EnumFacing.VALUES) {
            if (CityRecode.check(targetPos.offset(i), target) || this.cantMine(targetPos, i, 2) || !this.doMineExtend(targetPos, i)) continue;
            return true;
        }
        if (this.mineTrap.getValue()) {
            for (EnumFacing i : EnumFacing.VALUES) {
                if (this.doCheck(targetPos, i)) continue;
                progress = 0;
                if (this.isAir(targetPos.offset(i))) {
                    ++progress;
                }
                if (this.isAir(targetPos.offset(i).up())) {
                    ++progress;
                }
                if (progress < 1 || best != null && !(CityRecode.mc.player.getDistance((double)targetPos.offset(i).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i).getZ() + 0.5) < distance)) continue;
                distance = CityRecode.mc.player.getDistance((double)targetPos.offset(i).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i).getZ() + 0.5);
                best = i;
            }
            if (best != null && this.doMineTrap(targetPos, best)) {
                return true;
            }
            for (EnumFacing i : EnumFacing.VALUES) {
                if (this.doCheck(targetPos, i)) continue;
                progress = 0;
                if (this.isAir(targetPos.offset(i))) {
                    ++progress;
                }
                if (this.isAir(targetPos.offset(i).up())) {
                    ++progress;
                }
                if (progress < 1 || !this.doMineTrap(targetPos, i)) continue;
                return true;
            }
        }
        if (this.prefer.getValue() && this.mineTrap.getValue() && this.MineTrapSurround(targetPos)) {
            return true;
        }
        distance = 0.0;
        best = null;
        for (EnumFacing i : EnumFacing.VALUES) {
            if (CityRecode.check(targetPos.offset(i), target) || this.cantMine(targetPos, i, 1) || best != null && !(CityRecode.mc.player.getDistance((double)targetPos.offset(i, 2).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i, 2).getZ() + 0.5) < distance)) continue;
            distance = CityRecode.mc.player.getDistance((double)targetPos.offset(i, 2).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i, 2).getZ() + 0.5);
            best = i;
        }
        if (best != null && this.doMineExtend(targetPos, best)) {
            return true;
        }
        for (EnumFacing i : EnumFacing.VALUES) {
            if (CityRecode.check(targetPos.offset(i), target) || this.cantMine(targetPos, i, 1) || !this.doMineExtend(targetPos, i)) continue;
            return true;
        }
        distance = 0.0;
        best = null;
        for (EnumFacing i : EnumFacing.VALUES) {
            if (this.doCheckExtend(targetPos, i) || best != null && !(CityRecode.mc.player.getDistance((double)targetPos.offset(i, 2).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i, 2).getZ() + 0.5) < distance)) continue;
            distance = CityRecode.mc.player.getDistance((double)targetPos.offset(i, 2).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i, 2).getZ() + 0.5);
            best = i;
        }
        if (best != null && this.doMineExtend(targetPos, best)) {
            return true;
        }
        for (EnumFacing i : EnumFacing.VALUES) {
            if (this.doCheckExtend(targetPos, i) || !this.doMineExtend(targetPos, i)) continue;
            return true;
        }
        if (!this.mineTrap.getValue()) {
            return true;
        }
        distance = 0.0;
        best = null;
        for (EnumFacing i : EnumFacing.VALUES) {
            if (this.doCheck(targetPos, i)) continue;
            progress = 0;
            if (this.isAir(targetPos.offset(i))) {
                ++progress;
            }
            if (this.isAir(targetPos.offset(i).up())) {
                ++progress;
            }
            if (progress < 1 || best != null && !(CityRecode.mc.player.getDistance((double)targetPos.offset(i).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i).getZ() + 0.5) < distance)) continue;
            distance = CityRecode.mc.player.getDistance((double)targetPos.offset(i).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i).getZ() + 0.5);
            best = i;
        }
        if (best != null && this.doMineTrap(targetPos, best)) {
            return true;
        }
        for (EnumFacing i : EnumFacing.VALUES) {
            if (this.doCheck(targetPos, i)) continue;
            progress = 0;
            if (this.isAir(targetPos.offset(i))) {
                ++progress;
            }
            if (this.isAir(targetPos.offset(i).up())) {
                ++progress;
            }
            if (progress < 1 || !this.doMineTrap(targetPos, i)) continue;
            return true;
        }
        return this.MineTrapSurround(targetPos);
    }

    private boolean doCheckExtend(BlockPos targetPos, EnumFacing i) {
        if (CityRecode.check(targetPos.offset(i), target)) {
            return true;
        }
        if (i == EnumFacing.UP || i == EnumFacing.DOWN) {
            return true;
        }
        return CityRecode.mc.player.getDistance((double)targetPos.offset(i, 2).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i, 2).getZ() + 0.5) > this.breakRange.getValue();
    }

    private boolean MineTrapSurround(BlockPos targetPos) {
        if (this.debug.getValue()) {
            this.sendMessage("mine trap surround");
        }
        double distance = 0.0;
        EnumFacing best = null;
        for (EnumFacing i : EnumFacing.VALUES) {
            if (this.doCheck(targetPos, i) || best != null && !(CityRecode.mc.player.getDistance((double)targetPos.offset(i).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i).getZ() + 0.5) < distance)) continue;
            distance = CityRecode.mc.player.getDistance((double)targetPos.offset(i).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i).getZ() + 0.5);
            best = i;
        }
        if (best != null && this.doMineTrap(targetPos, best)) {
            return true;
        }
        for (EnumFacing i : EnumFacing.VALUES) {
            if (this.doCheck(targetPos, i) || !this.doMineTrap(targetPos, i)) continue;
            return true;
        }
        return false;
    }

    private boolean doCheck(BlockPos targetPos, EnumFacing i) {
        if (CityRecode.check(targetPos.offset(i), target)) {
            return true;
        }
        if (i == EnumFacing.UP || i == EnumFacing.DOWN) {
            return true;
        }
        return CityRecode.mc.player.getDistance((double)targetPos.offset(i).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i).getZ() + 0.5) > this.breakRange.getValue();
    }

    private boolean cantMine(BlockPos targetPos, EnumFacing i, int needAir) {
        if (i == EnumFacing.UP || i == EnumFacing.DOWN) {
            return true;
        }
        if (CityRecode.mc.player.getDistance((double)targetPos.offset(i, 2).getX() + 0.5, CityRecode.target.posY, (double)targetPos.offset(i, 2).getZ() + 0.5) > this.breakRange.getValue()) {
            return true;
        }
        int progress = 0;
        if (this.isAir(targetPos.offset(i))) {
            ++progress;
        }
        if (this.isAir(targetPos.offset(i, 2))) {
            ++progress;
        }
        if (this.isAir(targetPos.offset(i, 2).up())) {
            ++progress;
        }
        return progress < needAir;
    }

    private boolean doMineTrap(BlockPos targetPos, EnumFacing i) {
        if (!this.isAir(targetPos.offset(i)) && this.godBlocks.contains(this.getBlock(targetPos.offset(i)))) {
            return false;
        }
        if (!this.isAir(targetPos.offset(i).up()) && this.godBlocks.contains(this.getBlock(targetPos.offset(i).up()))) {
            return false;
        }
        if (!(this.isAir(targetPos.offset(i).up()) || !this.isAir(targetPos) && this.godBlocks.contains(this.getBlock(targetPos.offset(i))) || this.godBlocks.contains(this.getBlock(targetPos.offset(i).up())))) {
            this.mineBlock(targetPos.offset(i).up());
            return true;
        }
        return this.MineSurround(targetPos, i);
    }

    private boolean MineSurround(BlockPos targetPos, EnumFacing i) {
        if (!this.isAir(targetPos.offset(i)) && !this.godBlocks.contains(this.getBlock(targetPos.offset(i)))) {
            this.mineBlock(targetPos.offset(i));
            return true;
        }
        return false;
    }

    private boolean doMineExtend(BlockPos targetPos, EnumFacing i) {
        if (!this.isAir(targetPos.offset(i)) && this.godBlocks.contains(this.getBlock(targetPos.offset(i)))) {
            return false;
        }
        if (!this.isAir(targetPos.up().offset(i, 2)) && this.godBlocks.contains(this.getBlock(targetPos.up().offset(i, 2)))) {
            return false;
        }
        if (!this.isAir(targetPos.offset(i, 2)) && this.godBlocks.contains(this.getBlock(targetPos.offset(i, 2)))) {
            return false;
        }
        if (!this.isAir(targetPos.offset(i, 2).up()) && !this.godBlocks.contains(this.getBlock(targetPos.offset(i, 2).up()))) {
            this.mineBlock(targetPos.offset(i, 2).up());
            return true;
        }
        if (!this.isAir(targetPos.offset(i, 2)) && !this.godBlocks.contains(this.getBlock(targetPos.offset(i, 2)))) {
            this.mineBlock(targetPos.offset(i, 2));
            return true;
        }
        return this.MineSurround(targetPos, i);
    }

    @Override
    public String getInfo() {
        if (target != null) {
            return target.getName();
        }
        return null;
    }

    private void mineBlock(BlockPos pos) {
        if (this.getBlock(pos) == Blocks.REDSTONE_WIRE) {
            if (this.debug.getValue()) {
                this.sendMessage("redstone!");
            }
            return;
        }
        if (this.debug.getValue()) {
            this.sendMessage("mine block");
        }
        if (PacketMine.godBlocks.contains(this.getBlock(pos))) {
            return;
        }
        if (!pos.equals(PacketMine.breakPos) && this.renderTimer.passedMs(200L)) {
            this.breakPos = pos;
            this.renderTimer.reset();
            this.shrinkTimer = new FadeUtils(this.shrinkTime.getValue());
        }
        if (this.autoDisable.getValue() && !this.keyMode.getValue()) {
            this.disable();
        }
        CombatUtil.mineBlock(pos);
    }

    private boolean canAttack(EntityPlayer target, BlockPos targetPos) {
        if (this.canAttack2(target, targetPos)) {
            return true;
        }
        if (CityRecode.check(targetPos.add(0, 0, 1), target) && this.canAttack2(target, targetPos.add(0, 0, 1))) {
            return true;
        }
        if (CityRecode.check(targetPos.add(0, 0, -1), target) && this.canAttack2(target, targetPos.add(0, 0, -1))) {
            return true;
        }
        if (CityRecode.check(targetPos.add(1, 0, 0), target) && this.canAttack2(target, targetPos.add(1, 0, 0))) {
            return true;
        }
        if (CityRecode.check(targetPos.add(-1, 0, 0), target) && this.canAttack2(target, targetPos.add(-1, 0, 0))) {
            return true;
        }
        if (CityRecode.check(targetPos.add(1, 0, 1), target) && this.canAttack2(target, targetPos.add(1, 0, 1))) {
            return true;
        }
        if (CityRecode.check(targetPos.add(1, 0, -1), target) && this.canAttack2(target, targetPos.add(1, 0, -1))) {
            return true;
        }
        if (CityRecode.check(targetPos.add(-1, 0, 1), target) && this.canAttack2(target, targetPos.add(-1, 0, 1))) {
            return true;
        }
        return CityRecode.check(targetPos.add(-1, 0, -1), target) && this.canAttack2(target, targetPos.add(-1, 0, -1));
    }

    private boolean canAttack2(EntityPlayer target, BlockPos targetPos) {
        for (EnumFacing i : EnumFacing.VALUES) {
            if (CityRecode.check(targetPos.offset(i), target) || i == EnumFacing.UP || i == EnumFacing.DOWN || CityRecode.mc.player.getDistance((double)targetPos.offset(i).getX() + 0.5, targetPos.getY(), (double)targetPos.offset(i).getZ() + 0.5) > this.breakRange.getValue()) continue;
            if (this.isAir(targetPos.offset(i)) && this.isAir(targetPos.offset(i).up())) {
                if (this.debug.getValue()) {
                    this.sendMessage("can attack");
                }
                return true;
            }
            if (CityRecode.mc.player.getDistance((double)targetPos.offset(i, 2).getX() + 0.5, targetPos.getY(), (double)targetPos.offset(i, 2).getZ() + 0.5) > this.breakRange.getValue() || !this.isAir(targetPos.offset(i)) || !this.isAir(targetPos.offset(i, 2)) || !this.isAir(targetPos.offset(i, 2).up())) continue;
            if (this.debug.getValue()) {
                this.sendMessage("can attack");
            }
            return true;
        }
        return false;
    }

    private Block getBlock(BlockPos blockPos) {
        return CityRecode.mc.world.getBlockState(blockPos).getBlock();
    }

    private boolean isAir(BlockPos pos) {
        if (this.getBlock(pos) == Blocks.REDSTONE_WIRE) {
            return true;
        }
        return CityRecode.mc.world.isAirBlock(pos);
    }
}


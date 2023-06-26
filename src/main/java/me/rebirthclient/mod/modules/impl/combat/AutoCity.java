package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class AutoCity
extends Module {
    public static EntityPlayer target;
    public final Setting<Boolean> burrow = this.add(new Setting<>("MineBurrow", true));
    private final Setting<Float> range = this.add(new Setting<>("Range", 5.0f, 1.0f, 8.0f));
    private final Setting<Boolean> keyMode = this.add(new Setting<>("KeyMode", true));
    private final Setting<Bind> keyBind = this.add(new Setting<>("Enable", new Bind(-1), v -> this.keyMode.getValue()));
    private final Setting<Boolean> toggle = this.add(new Setting<>("Toggle", false, v -> !this.keyMode.getValue()));

    public AutoCity() {
        super("AutoCity", "Automatically break the enemy's surround", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
        if (this.keyMode.getValue() && !this.keyBind.getValue().isDown()) {
            return;
        }
        target = CombatUtil.getTarget(this.range.getValue(), 10.0);
        if (target == null) {
            return;
        }
        BlockPos targetPos = new BlockPos(AutoCity.target.posX, AutoCity.target.posY + 0.5, AutoCity.target.posZ);
        if (!AutoCity.mc.world.isAirBlock(targetPos) && this.burrow.getValue()) {
            this.mineBlock(targetPos);
        } else if (!AutoCity.mc.world.isAirBlock(new BlockPos(AutoCity.target.posX + 0.3, AutoCity.target.posY + 0.5, AutoCity.target.posZ + 0.3)) && this.burrow.getValue()) {
            this.mineBlock(new BlockPos(AutoCity.target.posX + 0.3, AutoCity.target.posY + 0.5, AutoCity.target.posZ + 0.3));
        } else if (!AutoCity.mc.world.isAirBlock(new BlockPos(AutoCity.target.posX + 0.3, AutoCity.target.posY + 0.5, AutoCity.target.posZ + 0.3)) && this.burrow.getValue()) {
            this.mineBlock(new BlockPos(AutoCity.target.posX + 0.3, AutoCity.target.posY + 0.5, AutoCity.target.posZ - 0.3));
        } else if (!AutoCity.mc.world.isAirBlock(new BlockPos(AutoCity.target.posX + 0.3, AutoCity.target.posY + 0.5, AutoCity.target.posZ + 0.3)) && this.burrow.getValue()) {
            this.mineBlock(new BlockPos(AutoCity.target.posX - 0.3, AutoCity.target.posY + 0.5, AutoCity.target.posZ + 0.3));
        } else if (!AutoCity.mc.world.isAirBlock(new BlockPos(AutoCity.target.posX + 0.3, AutoCity.target.posY + 0.5, AutoCity.target.posZ + 0.3)) && this.burrow.getValue()) {
            this.mineBlock(new BlockPos(AutoCity.target.posX - 0.3, AutoCity.target.posY + 0.5, AutoCity.target.posZ - 0.3));
        } else if (!this.canAttack(target)) {
            if (this.getBlock(targetPos.add(0, 1, 2)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, 1)) != Blocks.AIR && this.getBlock(targetPos.add(0, 0, 2)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, 1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 0, 1)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 0, 1));
            } else if (this.getBlock(targetPos.add(0, 1, -2)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, -1)) != Blocks.AIR && this.getBlock(targetPos.add(0, 0, -2)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, -1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 0, -1)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 0, -1));
            } else if (this.getBlock(targetPos.add(2, 1, 0)) == Blocks.AIR && this.getBlock(targetPos.add(1, 0, 0)) != Blocks.AIR && this.getBlock(targetPos.add(2, 0, 0)) == Blocks.AIR && this.getBlock(targetPos.add(1, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(1, 0, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(1, 0, 0));
            } else if (this.getBlock(targetPos.add(-2, 1, 0)) == Blocks.AIR && this.getBlock(targetPos.add(-1, 0, 0)) != Blocks.AIR && this.getBlock(targetPos.add(-2, 0, 0)) == Blocks.AIR && this.getBlock(targetPos.add(-1, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(-1, 0, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(-1, 0, 0));
            } else if (this.getBlock(targetPos.add(2, 1, 0)) == Blocks.AIR && this.getBlock(targetPos.add(2, 0, 0)) != Blocks.AIR && this.getBlock(targetPos.add(1, 0, 0)) == Blocks.AIR && this.getBlock(targetPos.add(1, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(2, 0, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(2, 0, 0));
            } else if (this.getBlock(targetPos.add(-2, 1, 0)) == Blocks.AIR && this.getBlock(targetPos.add(-2, 0, 0)) != Blocks.AIR && this.getBlock(targetPos.add(-1, 0, 0)) == Blocks.AIR && this.getBlock(targetPos.add(-1, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(-2, 0, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(-2, 0, 0));
            } else if (this.getBlock(targetPos.add(0, 1, -2)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, -2)) != Blocks.AIR && this.getBlock(targetPos.add(0, 0, -1)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, -1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 0, -2)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 0, -2));
            } else if (this.getBlock(targetPos.add(0, 1, 2)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, 2)) != Blocks.AIR && this.getBlock(targetPos.add(0, 0, 1)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, 1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 0, 2)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 0, 2));
            } else if (this.getBlock(targetPos.add(2, 1, 0)) == Blocks.AIR && this.getBlock(targetPos.add(1, 0, 0)) != Blocks.AIR && this.getBlock(targetPos.add(2, 0, 0)) != Blocks.AIR && this.getBlock(targetPos.add(1, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(2, 0, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(2, 0, 0));
            } else if (this.getBlock(targetPos.add(-2, 1, 0)) == Blocks.AIR && this.getBlock(targetPos.add(-1, 0, 0)) != Blocks.AIR && this.getBlock(targetPos.add(-2, 0, 0)) != Blocks.AIR && this.getBlock(targetPos.add(-1, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(-2, 0, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(-2, 0, 0));
            } else if (this.getBlock(targetPos.add(0, 1, -2)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, -1)) != Blocks.AIR && this.getBlock(targetPos.add(0, 0, -2)) != Blocks.AIR && this.getBlock(targetPos.add(0, 0, -1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 0, -2)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 0, -2));
            } else if (this.getBlock(targetPos.add(0, 1, 2)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, 1)) != Blocks.AIR && this.getBlock(targetPos.add(0, 0, 2)) != Blocks.AIR && this.getBlock(targetPos.add(0, 0, 1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 0, 2)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 0, 2));
            } else if (this.getBlock(targetPos.add(0, 2, 1)) == Blocks.AIR && this.getBlock(targetPos.add(0, 1, 1)) != Blocks.AIR && this.getBlock(targetPos.add(0, 0, 1)) == Blocks.AIR && this.getBlock(targetPos.add(0, 1, 1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 1, 1)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 1, 1));
            } else if (this.getBlock(targetPos.add(0, 2, 1)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, 1)) != Blocks.AIR && this.getBlock(targetPos.add(0, 1, 1)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, 1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 0, 1)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 0, 1));
            } else if (this.getBlock(targetPos.add(0, 2, -1)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, -1)) != Blocks.AIR && this.getBlock(targetPos.add(0, 1, -1)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, -1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 0, -1)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 0, -1));
            } else if (this.getBlock(targetPos.add(1, 2, 0)) == Blocks.AIR && this.getBlock(targetPos.add(1, 0, 0)) != Blocks.AIR && this.getBlock(targetPos.add(1, 1, 0)) == Blocks.AIR && this.getBlock(targetPos.add(1, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(1, 0, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(1, 0, 0));
            } else if (this.getBlock(targetPos.add(-1, 2, 0)) == Blocks.AIR && this.getBlock(targetPos.add(-1, 0, 0)) != Blocks.AIR && this.getBlock(targetPos.add(-1, 1, 0)) == Blocks.AIR && this.getBlock(targetPos.add(-1, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(-1, 0, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(-1, 0, 0));
            } else if (this.getBlock(targetPos.add(1, 2, 0)) == Blocks.AIR && this.getBlock(targetPos.add(1, 1, 0)) != Blocks.AIR && this.getBlock(targetPos.add(1, 0, 0)) == Blocks.AIR && this.getBlock(targetPos.add(1, 1, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(1, 1, 0));
            } else if (this.getBlock(targetPos.add(-1, 2, 0)) == Blocks.AIR && this.getBlock(targetPos.add(-1, 1, 0)) != Blocks.AIR && this.getBlock(targetPos.add(-1, 0, 0)) == Blocks.AIR && this.getBlock(targetPos.add(-1, 1, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(-1, 1, 0));
            } else if (this.getBlock(targetPos.add(0, 2, -1)) == Blocks.AIR && this.getBlock(targetPos.add(0, 1, -1)) != Blocks.AIR && this.getBlock(targetPos.add(0, 0, -1)) == Blocks.AIR && this.getBlock(targetPos.add(0, 1, -1)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 1, -1));
            } else if (this.getBlock(targetPos.add(1, 2, 0)) == Blocks.AIR && this.getBlock(targetPos.add(1, 0, 0)) != Blocks.AIR && this.getBlock(targetPos.add(1, 1, 0)) != Blocks.AIR && this.getBlock(targetPos.add(1, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(1, 1, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(1, 1, 0));
            } else if (this.getBlock(targetPos.add(-1, 2, 0)) == Blocks.AIR && this.getBlock(targetPos.add(-1, 0, 0)) != Blocks.AIR && this.getBlock(targetPos.add(-1, 1, 0)) != Blocks.AIR && this.getBlock(targetPos.add(-1, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(-1, 1, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(-1, 1, 0));
            } else if (this.getBlock(targetPos.add(0, 2, -1)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, -1)) != Blocks.AIR && this.getBlock(targetPos.add(0, 1, -1)) != Blocks.AIR && this.getBlock(targetPos.add(0, 0, -1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 1, -1)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 1, -1));
            } else if (this.getBlock(targetPos.add(0, 2, 1)) == Blocks.AIR && this.getBlock(targetPos.add(0, 0, 1)) != Blocks.AIR && this.getBlock(targetPos.add(0, 1, 1)) != Blocks.AIR && this.getBlock(targetPos.add(0, 0, 1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 1, 1)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 1, 1));
            } else if (this.getBlock(targetPos.add(-1, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(-2, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(-2, 1, 0)) != Blocks.AIR && this.getBlock(targetPos.add(-2, 1, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(-2, 1, 0));
            } else if (this.getBlock(targetPos.add(1, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(2, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(2, 1, 0)) != Blocks.AIR && this.getBlock(targetPos.add(2, 1, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(2, 1, 0));
            } else if (this.getBlock(targetPos.add(0, 0, 1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 0, 2)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 1, 2)) != Blocks.AIR && this.getBlock(targetPos.add(0, 1, 2)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 1, 2));
            } else if (this.getBlock(targetPos.add(0, 0, -1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 0, -2)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 1, -2)) != Blocks.AIR && this.getBlock(targetPos.add(0, 1, -2)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 1, -2));
            } else if (this.getBlock(targetPos.add(-1, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(-1, 1, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(-1, 2, 0)) != Blocks.AIR && this.getBlock(targetPos.add(-1, 2, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(-1, 2, 0));
            } else if (this.getBlock(targetPos.add(1, 0, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(1, 1, 0)) != Blocks.BEDROCK && this.getBlock(targetPos.add(1, 2, 0)) != Blocks.AIR && this.getBlock(targetPos.add(1, 2, 0)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(1, 2, 0));
            } else if (this.getBlock(targetPos.add(0, 0, 1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 1, 1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 2, 1)) != Blocks.AIR && this.getBlock(targetPos.add(0, 2, 1)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 2, 1));
            } else if (this.getBlock(targetPos.add(0, 0, -1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 1, -1)) != Blocks.BEDROCK && this.getBlock(targetPos.add(0, 2, -1)) != Blocks.AIR && this.getBlock(targetPos.add(0, 2, -1)) != Blocks.BEDROCK) {
                this.mineBlock(targetPos.add(0, 2, -1));
            }
        }
        if (this.toggle.getValue() && !this.keyMode.getValue()) {
            this.disable();
        }
    }

    @Override
    public String getInfo() {
        if (target != null) {
            return target.getName();
        }
        return null;
    }

    private void mineBlock(BlockPos position) {
        CombatUtil.mineBlock(position);
    }

    private boolean canAttack(EntityPlayer player) {
        return this.getBlock(new BlockPos(player.posX + 1.2, player.posY, player.posZ)) == Blocks.AIR & this.getBlock(new BlockPos(player.posX + 1.2, player.posY + 1.0, player.posZ)) == Blocks.AIR || this.getBlock(new BlockPos(player.posX - 1.2, player.posY, player.posZ)) == Blocks.AIR & this.getBlock(new BlockPos(player.posX - 1.2, player.posY + 1.0, player.posZ)) == Blocks.AIR || this.getBlock(new BlockPos(player.posX, player.posY, player.posZ + 1.2)) == Blocks.AIR & this.getBlock(new BlockPos(player.posX, player.posY + 1.0, player.posZ + 1.2)) == Blocks.AIR || this.getBlock(new BlockPos(player.posX, player.posY, player.posZ - 1.2)) == Blocks.AIR & this.getBlock(new BlockPos(player.posX, player.posY + 1.0, player.posZ - 1.2)) == Blocks.AIR || this.getBlock(new BlockPos(player.posX + 2.2, player.posY + 1.0, player.posZ)) == Blocks.AIR & this.getBlock(new BlockPos(player.posX + 2.2, player.posY, player.posZ)) == Blocks.AIR & this.getBlock(new BlockPos(player.posX + 1.2, player.posY, player.posZ)) == Blocks.AIR || this.getBlock(new BlockPos(player.posX - 2.2, player.posY + 1.0, player.posZ)) == Blocks.AIR & this.getBlock(new BlockPos(player.posX - 2.2, player.posY, player.posZ)) == Blocks.AIR & this.getBlock(new BlockPos(player.posX - 1.2, player.posY, player.posZ)) == Blocks.AIR || this.getBlock(new BlockPos(player.posX, player.posY + 1.0, player.posZ + 2.2)) == Blocks.AIR & this.getBlock(new BlockPos(player.posX, player.posY, player.posZ + 2.2)) == Blocks.AIR & this.getBlock(new BlockPos(player.posX, player.posY, player.posZ + 1.2)) == Blocks.AIR || this.getBlock(new BlockPos(player.posX, player.posY + 1.0, player.posZ - 2.2)) == Blocks.AIR & this.getBlock(new BlockPos(player.posX, player.posY, player.posZ - 2.2)) == Blocks.AIR & this.getBlock(new BlockPos(player.posX, player.posY, player.posZ - 1.2)) == Blocks.AIR;
    }

    private Block getBlock(BlockPos block) {
        return AutoCity.mc.world.getBlockState(block).getBlock();
    }
}


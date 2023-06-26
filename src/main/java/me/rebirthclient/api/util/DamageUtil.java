package me.rebirthclient.api.util;

import java.util.function.BiPredicate;
import me.rebirthclient.api.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class DamageUtil
implements Wrapper {
    public static boolean isArmorLow(EntityPlayer player, int durability) {
        for (ItemStack piece : player.inventory.armorInventory) {
            if (piece == null) {
                return true;
            }
            if (DamageUtil.getItemDamage(piece) >= durability) continue;
            return true;
        }
        return false;
    }

    public static float getDifficultyMultiplier(float damage) {
        switch (DamageUtil.mc.world.getDifficulty()) {
            case PEACEFUL: {
                return 0.0f;
            }
            case EASY: {
                return Math.min(damage / 2.0f + 1.0f, damage);
            }
            case HARD: {
                return damage * 1.5f;
            }
        }
        return damage;
    }

    private static float getBlockDensity(Vec3d vec, AxisAlignedBB bb, BlockPos.MutableBlockPos mutablePos) {
        double x = 1.0 / ((bb.maxX - bb.minX) * 2.0 + 1.0);
        double y = 1.0 / ((bb.maxY - bb.minY) * 2.0 + 1.0);
        double z = 1.0 / ((bb.maxZ - bb.minZ) * 2.0 + 1.0);
        double xFloor = (1.0 - Math.floor(1.0 / x) * x) / 2.0;
        double zFloor = (1.0 - Math.floor(1.0 / z) * z) / 2.0;
        if (x >= 0.0 && y >= 0.0 && z >= 0.0) {
            int air = 0;
            int traced = 0;
            for (float a = 0.0f; a <= 1.0f; a += (float)x) {
                for (float b = 0.0f; b <= 1.0f; b += (float)y) {
                    for (float c = 0.0f; c <= 1.0f; c += (float)z) {
                        double xOff = bb.minX + (bb.maxX - bb.minX) * (double)a;
                        double yOff = bb.minY + (bb.maxY - bb.minY) * (double)b;
                        double zOff = bb.minZ + (bb.maxZ - bb.minZ) * (double)c;
                        RayTraceResult result = DamageUtil.rayTraceBlocks(DamageUtil.mc.world, new Vec3d(xOff + xFloor, yOff, zOff + zFloor), vec, mutablePos, DamageUtil::isResistant);
                        if (result == null) {
                            ++air;
                        }
                        ++traced;
                    }
                }
            }
            return (float)air / (float)traced;
        }
        return 0.0f;
    }

    private static boolean isResistant(BlockPos pos, IBlockState state) {
        return !state.getMaterial().isLiquid() && (double)state.getBlock().getExplosionResistance(DamageUtil.mc.world, pos, null, null) >= 19.7;
    }

    public static boolean isNaked(EntityPlayer player) {
        for (ItemStack piece : player.inventory.armorInventory) {
            if (piece == null || piece.isEmpty()) continue;
            return false;
        }
        return true;
    }

    private static RayTraceResult rayTraceBlocks(World world, Vec3d start, Vec3d end, BlockPos.MutableBlockPos mutablePos, BiPredicate<BlockPos, IBlockState> predicate) {
        if (!(Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z) || Double.isNaN(end.x) || Double.isNaN(end.y) || Double.isNaN(end.z))) {
            int currentX = MathHelper.floor(start.x);
            int currentY = MathHelper.floor(start.y);
            int currentZ = MathHelper.floor(start.z);
            int endXFloor = MathHelper.floor(end.x);
            int endYFloor = MathHelper.floor(end.y);
            int endZFloor = MathHelper.floor(end.z);
            IBlockState startBlockState = world.getBlockState(mutablePos.setPos(currentX, currentY, currentZ));
            Block startBlock = startBlockState.getBlock();
            if (startBlock.canCollideCheck(startBlockState, false) && predicate.test(mutablePos, startBlockState)) {
                return startBlockState.collisionRayTrace(world, mutablePos, start, end);
            }
            int counter = 20;
            while (counter-- >= 0) {
                EnumFacing side;
                if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z)) {
                    return null;
                }
                if (currentX == endXFloor && currentY == endYFloor && currentZ == endZFloor) {
                    return null;
                }
                double totalDiffX = end.x - start.x;
                double totalDiffY = end.y - start.y;
                double totalDiffZ = end.z - start.z;
                double nextX = 999.0;
                double nextY = 999.0;
                double nextZ = 999.0;
                double diffX = 999.0;
                double diffY = 999.0;
                double diffZ = 999.0;
                if (endXFloor > currentX) {
                    nextX = (double)currentX + 1.0;
                    diffX = (nextX - start.x) / totalDiffX;
                } else if (endXFloor < currentX) {
                    nextX = currentX;
                    diffX = (nextX - start.x) / totalDiffX;
                }
                if (endYFloor > currentY) {
                    nextY = (double)currentY + 1.0;
                    diffY = (nextY - start.y) / totalDiffY;
                } else if (endYFloor < currentY) {
                    nextY = currentY;
                    diffY = (nextY - start.y) / totalDiffY;
                }
                if (endZFloor > currentZ) {
                    nextZ = (double)currentZ + 1.0;
                    diffZ = (nextZ - start.z) / totalDiffZ;
                } else if (endZFloor < currentZ) {
                    nextZ = currentZ;
                    diffZ = (nextZ - start.z) / totalDiffZ;
                }
                if (diffX == -0.0) {
                    diffX = -1.0E-4;
                }
                if (diffY == -0.0) {
                    diffY = -1.0E-4;
                }
                if (diffZ == -0.0) {
                    diffZ = -1.0E-4;
                }
                if (diffX < diffY && diffX < diffZ) {
                    side = endXFloor > currentX ? EnumFacing.WEST : EnumFacing.EAST;
                    start = new Vec3d(nextX, start.y + totalDiffY * diffX, start.z + totalDiffZ * diffX);
                } else if (diffY < diffZ) {
                    side = endYFloor > currentY ? EnumFacing.DOWN : EnumFacing.UP;
                    start = new Vec3d(start.x + totalDiffX * diffY, nextY, start.z + totalDiffZ * diffY);
                } else {
                    side = endZFloor > currentZ ? EnumFacing.NORTH : EnumFacing.SOUTH;
                    start = new Vec3d(start.x + totalDiffX * diffZ, start.y + totalDiffY * diffZ, nextZ);
                }
                currentX = MathHelper.floor(start.x) - (side == EnumFacing.EAST ? 1 : 0);
                currentY = MathHelper.floor(start.y) - (side == EnumFacing.UP ? 1 : 0);
                currentZ = MathHelper.floor(start.z) - (side == EnumFacing.SOUTH ? 1 : 0);
                mutablePos.setPos(currentX, currentY, currentZ);
                IBlockState state = world.getBlockState(mutablePos);
                Block block = state.getBlock();
                if (!block.canCollideCheck(state, false) || !predicate.test(mutablePos, state)) continue;
                return state.collisionRayTrace(world, mutablePos, start, end);
            }
        }
        return null;
    }

    public static int getItemDamage(ItemStack stack) {
        return stack.getMaxDamage() - stack.getItemDamage();
    }

    public static float getDamageInPercent(ItemStack stack) {
        return (float)DamageUtil.getItemDamage(stack) / (float)stack.getMaxDamage() * 100.0f;
    }

    public static int getRoundedDamage(ItemStack stack) {
        return (int)DamageUtil.getDamageInPercent(stack);
    }

    public static boolean hasDurability(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ItemArmor || item instanceof ItemSword || item instanceof ItemTool || item instanceof ItemShield;
    }

    public static boolean canBreakWeakness() {
        int strengthAmp = 0;
        PotionEffect effect = DamageUtil.mc.player.getActivePotionEffect(MobEffects.STRENGTH);
        if (effect != null) {
            strengthAmp = effect.getAmplifier();
        }
        return !DamageUtil.mc.player.isPotionActive(MobEffects.WEAKNESS) || strengthAmp >= 1 || DamageUtil.mc.player.getHeldItemMainhand().getItem() instanceof ItemSword || DamageUtil.mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe || DamageUtil.mc.player.getHeldItemMainhand().getItem() instanceof ItemAxe || DamageUtil.mc.player.getHeldItemMainhand().getItem() instanceof ItemSpade;
    }

    public static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        float doubleExplosionSize = 12.0f;
        double distancedsize = entity.getDistance(posX, posY, posZ) / (double)doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = 0.0;
        try {
            blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        }
        catch (Exception exception) {
            // empty catch block
        }
        double v = (1.0 - distancedsize) * blockDensity;
        float damage = (int)((v * v + v) / 2.0 * 7.0 * (double)doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            finald = DamageUtil.getBlastReduction((EntityLivingBase)entity, DamageUtil.getDamageMultiplied(damage), new Explosion(DamageUtil.mc.world, null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float)finald;
    }

    public static float getBlastReduction(EntityLivingBase entity, float damageI, Explosion explosion) {
        float damage = damageI;
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer)entity;
            DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float)ep.getTotalArmorValue(), (float)ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            int k = 0;
            try {
                k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            }
            catch (Exception exception) {
                // empty catch block
            }
            float f = MathHelper.clamp((float)k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.isPotionActive(MobEffects.RESISTANCE)) {
                damage -= damage / 4.0f;
            }
            damage = Math.max(damage, 0.0f);
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float)entity.getTotalArmorValue(), (float)entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    public static float getDamageMultiplied(float damage) {
        int diff = DamageUtil.mc.world.getDifficulty().getId();
        return damage * (diff == 0 ? 0.0f : (diff == 2 ? 1.0f : (diff == 1 ? 0.5f : 1.5f)));
    }

    public static float calculateDamage(Entity crystal, Entity entity) {
        return DamageUtil.calculateDamage(crystal.posX, crystal.posY, crystal.posZ, entity);
    }

    public static float calculateDamage(BlockPos pos, Entity entity) {
        return DamageUtil.calculateDamage((double)pos.getX() + 0.5, pos.getY() + 1, (double)pos.getZ() + 0.5, entity);
    }

    public static boolean canTakeDamage(boolean suicide) {
        return !DamageUtil.mc.player.capabilities.isCreativeMode && !suicide;
    }
}


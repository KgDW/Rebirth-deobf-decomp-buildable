package me.rebirthclient.asm.mixins;

import javax.annotation.Nullable;
import me.rebirthclient.mod.modules.impl.movement.AntiWeb;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value={BlockWeb.class})
public class MixinBlockWeb
extends Block {
    protected MixinBlockWeb() {
        super(Material.WEB);
    }

    @Nullable
    @Overwrite
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        if (AntiWeb.INSTANCE.isOn() && AntiWeb.INSTANCE.antiModeSetting.getValue() == AntiWeb.AntiMode.Block) {
            return FULL_BLOCK_AABB;
        }
        return NULL_AABB;
    }
}


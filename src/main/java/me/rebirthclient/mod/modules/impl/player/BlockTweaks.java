package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockTweaks
extends Module {
    public static BlockTweaks INSTANCE;
    public final Setting<Boolean> noFriendAttack = this.add(new Setting<>("NoFriendAttack", false));
    public final Setting<Boolean> autoTool = this.add(new Setting<>("AutoTool", false));
    public final Setting<Boolean> noGhost = this.add(new Setting<>("NoGlitchBlocks", false));
    private int lastHotbarSlot = -1;
    private boolean switched = false;
    private int currentTargetSlot = -1;

    public BlockTweaks() {
        super("BlockTweaks", "Some tweaks for blocks", Category.PLAYER);
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        if (this.switched) {
            this.equip(this.lastHotbarSlot, false);
        }
        this.lastHotbarSlot = -1;
        this.currentTargetSlot = -1;
    }

    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.LeftClickBlock leftClickBlock) {
        if (this.autoTool.getValue() && !BlockTweaks.fullNullCheck()) {
            this.equipBestTool(BlockTweaks.mc.world.getBlockState(leftClickBlock.getPos()));
        }
    }

    private void equipBestTool(IBlockState blockState) {
        int n = -1;
        double n2 = 0.0;
        for (int i = 0; i < 9; ++i) {
            float f = 0;
            float f2 = 0;
            ItemStack getStackInSlot = BlockTweaks.mc.player.inventory.getStackInSlot(i);
            if (getStackInSlot.isEmpty) continue;
            float getDestroySpeed = getStackInSlot.getDestroySpeed(blockState);
            if (!(f2 > 1.0f)) continue;
            int getEnchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, getStackInSlot);
            float n3 = (float)((double)getDestroySpeed + (getEnchantmentLevel > 0 ? Math.pow(getEnchantmentLevel, 2.0) + 1.0 : 0.0));
            if (!((double)f > n2)) continue;
            n2 = n3;
            n = i;
        }
        this.equip(n, true);
    }

    private void equip(int n, boolean switched) {
        if (n != -1) {
            if (n != BlockTweaks.mc.player.inventory.currentItem) {
                this.lastHotbarSlot = BlockTweaks.mc.player.inventory.currentItem;
            }
            this.currentTargetSlot = n;
            BlockTweaks.mc.player.inventory.currentItem = n;
            this.switched = switched;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send send) {
        Entity getEntityFromWorld;
        if (BlockTweaks.fullNullCheck()) {
            return;
        }
        if (this.noFriendAttack.getValue() && send.getPacket() instanceof CPacketUseEntity && (getEntityFromWorld = ((CPacketUseEntity)send.getPacket()).getEntityFromWorld(BlockTweaks.mc.world)) != null && Managers.FRIENDS.isFriend(getEntityFromWorld.getName())) {
            send.setCanceled(true);
        }
    }

    private void removeGlitchBlocks(BlockPos blockPos) {
        for (int i = -4; i <= 4; ++i) {
            for (int j = -4; j <= 4; ++j) {
                for (int k = -4; k <= 4; ++k) {
                    BlockPos blockPos2 = new BlockPos(blockPos.getX() + i, blockPos.getY() + j, blockPos.getZ() + k);
                    if (!BlockTweaks.mc.world.getBlockState(blockPos2).getBlock().equals(Blocks.AIR)) continue;
                    BlockTweaks.mc.playerController.processRightClickBlock(BlockTweaks.mc.player, BlockTweaks.mc.world, blockPos2, EnumFacing.DOWN, new Vec3d(0.5, 0.5, 0.5), EnumHand.MAIN_HAND);
                }
            }
        }
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent breakEvent) {
        if (BlockTweaks.fullNullCheck() || !this.noGhost.getValue()) {
            return;
        }
        if (!(BlockTweaks.mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)) {
            this.removeGlitchBlocks(BlockTweaks.mc.player.getPosition());
        }
    }

    @Override
    public void onUpdate() {
        if (BlockTweaks.mc.player.inventory.currentItem != this.lastHotbarSlot && BlockTweaks.mc.player.inventory.currentItem != this.currentTargetSlot) {
            this.lastHotbarSlot = BlockTweaks.mc.player.inventory.currentItem;
        }
        if (!BlockTweaks.mc.gameSettings.keyBindAttack.isKeyDown() && this.switched) {
            this.equip(this.lastHotbarSlot, false);
        }
    }
}


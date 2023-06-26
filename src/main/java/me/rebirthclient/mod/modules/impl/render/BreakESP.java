package me.rebirthclient.mod.modules.impl.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.util.HashMap;
import me.rebirthclient.api.events.impl.DamageBlockEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.misc.TabFriends;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BreakESP
extends Module {
    static final HashMap<EntityPlayer, MinePosition> MineMap = new HashMap();
    public static BreakESP INSTANCE = new BreakESP();
    private final Setting<Boolean> renderAir = this.add(new Setting<>("RenderAir", true));
    private final Setting<Boolean> renderSelf = this.add(new Setting<>("OneSelf", true));
    private final Setting<Boolean> renderUnknown = this.add(new Setting<>("RenderUnknown", true));
    private final Setting<Double> range = this.add(new Setting<>("Range", 15.0, 0.0, 50.0));
    private final Setting<Boolean> renderName = this.add(new Setting<>("RenderName", true).setParent());
    private final Setting<TabFriends.FriendColor> nameColor = this.add(new Setting<>("Color", TabFriends.FriendColor.Gray, v -> this.renderName.isOpen()));
    private final Setting<Boolean> renderProgress = this.add(new Setting<>("Progress", true, v -> this.renderName.isOpen()).setParent());
    private final Setting<Mode> animationMode = this.add(new Setting<>("AnimationMode", Mode.Up));
    private final Setting<Integer> animationTime = this.add(new Setting<>("AnimationTime", 1000, 0, 5000));
    private final Setting<Boolean> box = this.add(new Setting<>("Box", true).setParent());
    private final Setting<Integer> boxAlpha = this.add(new Setting<>("BoxAlpha", 30, 0, 255, v -> this.box.isOpen()));
    private final Setting<Boolean> outline = this.add(new Setting<>("Outline", true).setParent());
    private final Setting<Integer> outlineAlpha = this.add(new Setting<>("OutlineAlpha", 100, 0, 255, v -> this.outline.isOpen()));
    private final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255, 100)).hideAlpha());
    private final Setting<Color> doubleRender = this.add(new Setting<>("Double", new Color(255, 255, 255, 100)).injectBoolean(true).hideAlpha());

    public BreakESP() {
        super("BreakESP", "Show mine postion", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        MineMap.clear();
    }

    @SubscribeEvent
    public void BlockBreak(DamageBlockEvent event) {
        if (event.getPosition().getY() == -1) {
            return;
        }
        EntityPlayer breaker = (EntityPlayer)BreakESP.mc.world.getEntityByID(event.getBreakerId());
        if (breaker == null || breaker.getDistance((double)event.getPosition().getX() + 0.5, event.getPosition().getY(), (double)event.getPosition().getZ() + 0.5) > 7.0) {
            return;
        }
        if (!MineMap.containsKey(breaker)) {
            MineMap.put(breaker, new MinePosition(breaker));
        }
        MineMap.get(breaker).update(event.getPosition());
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        EntityPlayer[] array;
        for (EntityPlayer entityPlayer : array = MineMap.keySet().toArray(new EntityPlayer[0])) {
            if (entityPlayer == null || entityPlayer.isEntityAlive()) continue;
            MineMap.remove(entityPlayer);
        }
        MineMap.values().forEach(miner -> {
            double z;
            double y;
            double x;
            if (!this.renderAir.getValue() && BreakESP.mc.world.isAirBlock(miner.first)) {
                miner.finishFirst();
            }
            if (!(miner.firstFinished && !this.renderAir.getValue() || miner.miner == BreakESP.mc.player && !this.renderSelf.getValue() || !(miner.first.getDistance((int)BreakESP.mc.player.posX, (int)BreakESP.mc.player.posY, (int)BreakESP.mc.player.posZ) < this.range.getValue()) || miner.miner == null && !this.renderUnknown.getValue())) {
                this.draw(miner.first, miner.firstFade.easeOutQuad(), this.color.getValue());
                if (this.renderName.getValue()) {
                    x = (double)miner.first.getX() - BreakESP.mc.getRenderManager().renderPosX + 0.5;
                    y = (double)miner.first.getY() - BreakESP.mc.getRenderManager().renderPosY - 1.0;
                    z = (double)miner.first.getZ() - BreakESP.mc.getRenderManager().renderPosZ + 0.5;
                    RenderUtil.drawText(ChatFormatting.GRAY + (miner.miner == null ? this.getColor() + "Unknown" : this.getColor() + miner.miner.getName()), x, this.renderProgress.getValue() ? y + 0.15 : y, z, new Color(255, 255, 255, 255));
                    if (this.renderProgress.getValue()) {
                        if (BreakESP.mc.world.isAirBlock(miner.first)) {
                            RenderUtil.drawText(ChatFormatting.GREEN + "Broke", x, y - 0.15, z, new Color(255, 255, 255, 255));
                        } else {
                            RenderUtil.drawText(ChatFormatting.GREEN + "Breaking", x, y - 0.15, z, new Color(255, 255, 255, 255));
                        }
                    }
                }
            }
            if ((miner.miner != BreakESP.mc.player || this.renderSelf.getValue()) && !miner.secondFinished && miner.second != null) {
                if (BreakESP.mc.world.isAirBlock(miner.second)) {
                    miner.finishSecond();
                } else if (!miner.second.equals(miner.first) && miner.second.getDistance((int)BreakESP.mc.player.posX, (int)BreakESP.mc.player.posY, (int)BreakESP.mc.player.posZ) < this.range.getValue() && (miner.miner != null || this.renderUnknown.getValue()) && this.doubleRender.booleanValue) {
                    this.draw(miner.second, miner.secondFade.easeOutQuad(), this.doubleRender.getValue());
                    if (this.renderName.getValue()) {
                        x = (double)miner.second.getX() - BreakESP.mc.getRenderManager().renderPosX + 0.5;
                        y = (double)miner.second.getY() - BreakESP.mc.getRenderManager().renderPosY - 1.0;
                        z = (double)miner.second.getZ() - BreakESP.mc.getRenderManager().renderPosZ + 0.5;
                        RenderUtil.drawText(ChatFormatting.GRAY + (miner.miner == null ? this.getColor() + "Unknown" : this.getColor() + miner.miner.getName()), x, y + 0.15, z, new Color(255, 255, 255, 255));
                        RenderUtil.drawText(ChatFormatting.GOLD + "Double", x, y - 0.15, z, new Color(255, 255, 255, 255));
                    }
                }
            }
        });
    }

    public String getColor() {
        switch (this.nameColor.getValue()) {
            case White: {
                return "\u00a7f";
            }
            case DarkRed: {
                return "\u00a74";
            }
            case Red: {
                return "\u00a7c";
            }
            case Gold: {
                return "\u00a76";
            }
            case Yellow: {
                return "\u00a7e";
            }
            case DarkGreen: {
                return "\u00a72";
            }
            case Green: {
                return "\u00a7a";
            }
            case Aqua: {
                return "\u00a7b";
            }
            case DarkAqua: {
                return "\u00a73";
            }
            case DarkBlue: {
                return "\u00a71";
            }
            case Blue: {
                return "\u00a79";
            }
            case LightPurple: {
                return "\u00a7d";
            }
            case DarkPurple: {
                return "\u00a75";
            }
            case Gray: {
                return "\u00a77";
            }
            case DarkGray: {
                return "\u00a78";
            }
            case Black: {
                return "\u00a70";
            }
        }
        return "";
    }

    public void draw(BlockPos pos, double size, Color color) {
        if (this.animationMode.getValue() != Mode.Both) {
            AxisAlignedBB axisAlignedBB;
            if (this.animationMode.getValue() == Mode.InToOut) {
                axisAlignedBB = BreakESP.mc.world.getBlockState(pos).getSelectedBoundingBox(BreakESP.mc.world, pos).grow(size / 2.0 - 0.5);
            } else if (this.animationMode.getValue() == Mode.Up) {
                AxisAlignedBB bb = BreakESP.mc.world.getBlockState(pos).getSelectedBoundingBox(BreakESP.mc.world, pos);
                axisAlignedBB = new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY + (bb.maxY - bb.minY) * size, bb.maxZ);
            } else if (this.animationMode.getValue() == Mode.Down) {
                AxisAlignedBB bb = BreakESP.mc.world.getBlockState(pos).getSelectedBoundingBox(BreakESP.mc.world, pos);
                axisAlignedBB = new AxisAlignedBB(bb.minX, bb.maxY - (bb.maxY - bb.minY) * size, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
            } else if (this.animationMode.getValue() == Mode.None) {
                axisAlignedBB = BreakESP.mc.world.getBlockState(pos).getSelectedBoundingBox(BreakESP.mc.world, pos);
            } else {
                AxisAlignedBB bb = BreakESP.mc.world.getBlockState(pos).getSelectedBoundingBox(BreakESP.mc.world, pos).grow(size / 2.0 - 0.5);
                AxisAlignedBB bb2 = BreakESP.mc.world.getBlockState(pos).getSelectedBoundingBox(BreakESP.mc.world, pos);
                axisAlignedBB = this.animationMode.getValue() == Mode.Horizontal ? new AxisAlignedBB(bb2.minX, bb.minY, bb2.minZ, bb2.maxX, bb.maxY, bb2.maxZ) : new AxisAlignedBB(bb.minX, bb2.minY, bb.minZ, bb.maxX, bb2.maxY, bb.maxZ);
            }
            if (this.outline.getValue()) {
                RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
            }
            if (this.box.getValue()) {
                RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
            }
        } else {
            AxisAlignedBB axisAlignedBB = BreakESP.mc.world.getBlockState(pos).getSelectedBoundingBox(BreakESP.mc.world, pos).grow(size / 2.0 - 0.5);
            if (this.outline.getValue()) {
                RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
            }
            if (this.box.getValue()) {
                RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
            }
            axisAlignedBB = BreakESP.mc.world.getBlockState(pos).getSelectedBoundingBox(BreakESP.mc.world, pos).grow(-Math.abs(size / 2.0 - 1.0));
            if (this.outline.getValue()) {
                RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
            }
            if (this.box.getValue()) {
                RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
            }
        }
    }

    private static class MinePosition
    {
        public final EntityPlayer miner;
        public FadeUtils firstFade;
        public FadeUtils secondFade;
        public BlockPos first;
        public BlockPos second;
        public boolean secondFinished;
        public boolean firstFinished;

        public MinePosition(final EntityPlayer player) {
            this.firstFade = new FadeUtils(BreakESP.INSTANCE.animationTime.getValue());
            this.secondFade = new FadeUtils(BreakESP.INSTANCE.animationTime.getValue());
            this.miner = player;
            this.secondFinished = true;
        }

        public void finishSecond() {
            this.secondFinished = true;
        }

        public void finishFirst() {
            this.firstFinished = true;
        }

        public void update(BlockPos pos) {
            if (this.first != null && this.first.equals(pos) && (Boolean) INSTANCE.renderAir.getValue()) {
                return;
            }
            if (this.secondFinished || this.second == null) {
                this.second = pos;
                this.secondFinished = false;
                this.secondFade = new FadeUtils((Integer) INSTANCE.animationTime.getValue());
            }
            if (this.first == null || !this.first.equals(pos) || this.firstFinished) {
                this.firstFade = new FadeUtils((Integer) INSTANCE.animationTime.getValue());
            }
            this.firstFinished = false;
            this.first = pos;
        }
    }

    public static enum Mode {
        Down,
        Up,
        InToOut,
        Both,
        Vertical,
        Horizontal,
        None

    }
}


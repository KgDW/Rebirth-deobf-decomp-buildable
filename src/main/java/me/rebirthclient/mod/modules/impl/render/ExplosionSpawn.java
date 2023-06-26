package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ExplosionSpawn
extends Module {
    public static final ArrayList<Pos> spawnList = new ArrayList();
    public final Setting<Color> color = this.add(new Setting<>("Color", new Color(-557395713, true)));
    public final Setting<Float> size = this.add(new Setting<>("Max Size", 0.5f, 0.1f, 5.0f));
    public final Setting<Double> minSize = this.add(new Setting<>("Min Size", 0.1, 0.0, 1.0));
    public final Setting<Double> up = this.add(new Setting<>("Up", 0.1, 0.0, 1.0));
    public final Setting<Double> height = this.add(new Setting<>("Height", 0.5, -1.0, 1.0));
    private final Setting<Boolean> extra = this.add(new Setting<>("Extra", true));
    private final Setting<Integer> time = this.add(new Setting<>("Time", 500, 0, 5000));
    private final Setting<Integer> animationTime = this.add(new Setting<>("animationTime", 500, 0, 5000));

    public ExplosionSpawn() {
        super("ExplosionSpawn", "Draws a circle when a crystal spawn", Category.RENDER);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            spawnList.add(new Pos(((CPacketPlayerTryUseItemOnBlock)event.getPacket()).getPos(), this.animationTime.getValue(), this.time.getValue()));
        }
    }

    @Override
    public void onEnable() {
        spawnList.clear();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (spawnList.size() != 0) {
            Color color = this.color.getValue();
            boolean canClear = true;
            for (Pos spawnPos : spawnList) {
                if (spawnPos.time.easeOutQuad() >= 1.0) continue;
                RenderUtil.drawCircle(spawnPos.pos.getX(), (float)((double)(spawnPos.pos.getY() + 1) + 1.0 * spawnPos.firstFade.easeOutQuad() * this.up.getValue() + this.height.getValue()), spawnPos.pos.getZ(), (float)(spawnPos.firstFade.easeOutQuad() > this.minSize.getValue() ? (double) this.size.getValue() * spawnPos.firstFade.easeOutQuad() : (double) this.size.getValue() * this.minSize.getValue()), color);
                canClear = false;
                if (!this.extra.getValue()) continue;
                RenderUtil.drawCircle(spawnPos.pos.getX(), (float)((double)(spawnPos.pos.getY() + 1) + 1.0 * spawnPos.firstFade.easeOutQuad() * this.up.getValue() + this.height.getValue()), spawnPos.pos.getZ(), (float)(spawnPos.firstFade.easeOutQuad() > this.minSize.getValue() ? (double) this.size.getValue() * spawnPos.firstFade.easeOutQuad() : (double) this.size.getValue() * this.minSize.getValue()) - 0.01f, color);
                RenderUtil.drawCircle(spawnPos.pos.getX(), (float)((double)(spawnPos.pos.getY() + 1) + 1.0 * spawnPos.firstFade.easeOutQuad() * this.up.getValue() + this.height.getValue()), spawnPos.pos.getZ(), (float)(spawnPos.firstFade.easeOutQuad() > this.minSize.getValue() ? (double) this.size.getValue() * spawnPos.firstFade.easeOutQuad() : (double) this.size.getValue() * this.minSize.getValue()) - 0.015f, color);
                RenderUtil.drawCircle(spawnPos.pos.getX(), (float)((double)(spawnPos.pos.getY() + 1) + 1.0 * spawnPos.firstFade.easeOutQuad() * this.up.getValue() + this.height.getValue()), spawnPos.pos.getZ(), (float)(spawnPos.firstFade.easeOutQuad() > this.minSize.getValue() ? (double) this.size.getValue() * spawnPos.firstFade.easeOutQuad() : (double) this.size.getValue() * this.minSize.getValue()) - 0.02f, color);
                RenderUtil.drawCircle(spawnPos.pos.getX(), (float)((double)(spawnPos.pos.getY() + 1) + 1.0 * spawnPos.firstFade.easeOutQuad() * this.up.getValue() + this.height.getValue()), spawnPos.pos.getZ(), (float)(spawnPos.firstFade.easeOutQuad() > this.minSize.getValue() ? (double) this.size.getValue() * spawnPos.firstFade.easeOutQuad() : (double) this.size.getValue() * this.minSize.getValue()) - 0.025f, color);
                RenderUtil.drawCircle(spawnPos.pos.getX(), (float)((double)(spawnPos.pos.getY() + 1) + 1.0 * spawnPos.firstFade.easeOutQuad() * this.up.getValue() + this.height.getValue()), spawnPos.pos.getZ(), (float)(spawnPos.firstFade.easeOutQuad() > this.minSize.getValue() ? (double) this.size.getValue() * spawnPos.firstFade.easeOutQuad() : (double) this.size.getValue() * this.minSize.getValue()) - 0.03f, color);
                RenderUtil.drawCircle(spawnPos.pos.getX(), (float)((double)(spawnPos.pos.getY() + 1) + 1.0 * spawnPos.firstFade.easeOutQuad() * this.up.getValue() + this.height.getValue()), spawnPos.pos.getZ(), (float)(spawnPos.firstFade.easeOutQuad() > this.minSize.getValue() ? (double) this.size.getValue() * spawnPos.firstFade.easeOutQuad() : (double) this.size.getValue() * this.minSize.getValue()) - 0.035f, color);
                RenderUtil.drawCircle(spawnPos.pos.getX(), (float)((double)(spawnPos.pos.getY() + 1) + 1.0 * spawnPos.firstFade.easeOutQuad() * this.up.getValue() + this.height.getValue()), spawnPos.pos.getZ(), (float)(spawnPos.firstFade.easeOutQuad() > this.minSize.getValue() ? (double) this.size.getValue() * spawnPos.firstFade.easeOutQuad() : (double) this.size.getValue() * this.minSize.getValue()) - 0.005f, color);
            }
            if (canClear) {
                spawnList.clear();
            }
        }
    }

    private static class Pos {
        public final BlockPos pos;
        public final FadeUtils firstFade;
        public final FadeUtils time;

        public Pos(BlockPos pos, int animTime, int time) {
            this.firstFade = new FadeUtils(animTime);
            this.time = new FadeUtils(time);
            this.pos = pos;
        }
    }
}


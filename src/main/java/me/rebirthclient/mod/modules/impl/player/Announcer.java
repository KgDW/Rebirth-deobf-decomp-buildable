package me.rebirthclient.mod.modules.impl.player;

import java.text.DecimalFormat;
import java.util.Random;
import me.rebirthclient.api.events.impl.BreakBlockEvent;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemFood;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Announcer
extends Module {
    private final Setting<Boolean> move = this.add(new Setting<>("Move", true));
    private final Setting<Boolean> breakBlock = this.add(new Setting<>("Break", true));
    private final Setting<Boolean> eat = this.add(new Setting<>("Eat", true));
    private final Setting<Double> delay = this.add(new Setting<>("Delay", 10.0, 1.0, 30.0));
    private final Timer delayTimer = new Timer();
    private double lastPositionX;
    private double lastPositionY;
    private double lastPositionZ;
    private int eaten;
    private int broken;

    public Announcer() {
        super("Announcer", "announces yo shit", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        this.eaten = 0;
        this.broken = 0;
        this.delayTimer.reset();
    }

    @Override
    public void onUpdate() {
        if (Announcer.fullNullCheck() || !Announcer.spawnCheck()) {
            return;
        }
        double traveledX = this.lastPositionX - Announcer.mc.player.lastTickPosX;
        double traveledY = this.lastPositionY - Announcer.mc.player.lastTickPosY;
        double traveledZ = this.lastPositionZ - Announcer.mc.player.lastTickPosZ;
        double traveledDistance = Math.sqrt(traveledX * traveledX + traveledY * traveledY + traveledZ * traveledZ);
        if (this.move.getValue() && traveledDistance >= 1.0 && traveledDistance <= 1000.0 && this.delayTimer.passedS(this.delay.getValue())) {
            Announcer.mc.player.sendChatMessage(this.getWalkMessage().replace("{blocks}", new DecimalFormat("0.00").format(traveledDistance)));
            this.lastPositionX = Announcer.mc.player.lastTickPosX;
            this.lastPositionY = Announcer.mc.player.lastTickPosY;
            this.lastPositionZ = Announcer.mc.player.lastTickPosZ;
            this.delayTimer.reset();
        }
    }

    @SubscribeEvent
    public void onUseItem(LivingEntityUseItemEvent.Finish event) {
        if (Announcer.fullNullCheck() || !Announcer.spawnCheck()) {
            return;
        }
        int random = MathUtil.randomBetween(1, 6);
        if (this.eat.getValue() && event.getEntity() == Announcer.mc.player && event.getItem().getItem() instanceof ItemFood || event.getItem().getItem() instanceof ItemAppleGold) {
            ++this.eaten;
            if (this.eaten >= random && this.delayTimer.passedS(this.delay.getValue())) {
                Announcer.mc.player.sendChatMessage(this.getEatMessage().replace("{amount}", String.valueOf(this.eaten)).replace("{name}", event.getItem().getDisplayName()));
                this.eaten = 0;
                this.delayTimer.reset();
            }
        }
    }

    @SubscribeEvent
    public void onBreakBlock(BreakBlockEvent event) {
        if (Announcer.fullNullCheck() || !Announcer.spawnCheck()) {
            return;
        }
        int random = MathUtil.randomBetween(1, 6);
        ++this.broken;
        if (this.breakBlock.getValue() && this.broken >= random && this.delayTimer.passedS(this.delay.getValue())) {
            Announcer.mc.player.sendChatMessage(this.getBreakMessage().replace("{amount}", String.valueOf(this.broken)).replace("{name}", BlockUtil.getBlock(event.getPos()).getLocalizedName()));
            this.broken = 0;
            this.delayTimer.reset();
        }
    }

    private String getWalkMessage() {
        String[] walkMessage = new String[]{"I just flew over {blocks} blocks!"};
        return walkMessage[new Random().nextInt(walkMessage.length)];
    }

    private String getBreakMessage() {
        String[] breakMessage = new String[]{"I just destroyed {amount} {name}!"};
        return breakMessage[new Random().nextInt(breakMessage.length)];
    }

    private String getEatMessage() {
        String[] eatMessage = new String[]{"I just ate {amount} {name}!"};
        return eatMessage[new Random().nextInt(eatMessage.length)];
    }
}


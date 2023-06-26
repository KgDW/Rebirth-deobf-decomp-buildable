package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.Aura;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Criticals
extends Module {
    public static Criticals INSTANCE;
    private final Setting<Mode> mode = this.add(new Setting<>("Mode", Mode.PACKET));
    private final Setting<Boolean> webs = this.add(new Setting<>("Webs", false, v -> this.mode.getValue() == Mode.NCP));
    private final Setting<Boolean> onlyAura = this.add(new Setting<>("OnlyAura", false));
    private final Setting<Boolean> onlySword = this.add(new Setting<>("OnlySword", true));
    private final Setting<Integer> delay = this.add(new Setting<>("Delay", 100, 0, 1000));
    private final Timer delayTimer = new Timer();

    public Criticals() {
        super("Criticals", "Always do as much damage as you can!", Category.COMBAT);
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return this.mode.getValue() == Mode.NCP ? String.valueOf(this.mode.getValue()) : Managers.TEXT.normalizeCases(this.mode.getValue());
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (Criticals.fullNullCheck()) {
            return;
        }
        if (event.isCanceled()) {
            return;
        }
        if (Aura.target == null && this.onlyAura.getValue()) {
            return;
        }
        if (this.onlySword.getValue() && !(Criticals.mc.player.getHeldItemMainhand().item instanceof ItemSword)) {
            return;
        }
        if (!this.delayTimer.passedMs(this.delay.getValue())) {
            return;
        }
        if (event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity)event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK && Criticals.mc.player.onGround && Criticals.mc.player.collidedVertically && !Criticals.mc.player.isInLava() && !Criticals.mc.player.isInWater()) {
            Entity attackedEntity = ((CPacketUseEntity)event.getPacket()).getEntityFromWorld(Criticals.mc.world);
            if (attackedEntity instanceof EntityEnderCrystal || attackedEntity == null) {
                return;
            }
            this.delayTimer.reset();
            switch (this.mode.getValue()) {
                case PACKET: {
                    Criticals.mc.player.connection.sendPacket(new CPacketPlayer.Position(Criticals.mc.player.posX, Criticals.mc.player.posY + 0.0625101, Criticals.mc.player.posZ, false));
                    Criticals.mc.player.connection.sendPacket(new CPacketPlayer.Position(Criticals.mc.player.posX, Criticals.mc.player.posY, Criticals.mc.player.posZ, false));
                    Criticals.mc.player.connection.sendPacket(new CPacketPlayer.Position(Criticals.mc.player.posX, Criticals.mc.player.posY + 0.0125, Criticals.mc.player.posZ, false));
                    Criticals.mc.player.connection.sendPacket(new CPacketPlayer.Position(Criticals.mc.player.posX, Criticals.mc.player.posY, Criticals.mc.player.posZ, false));
                    break;
                }
                case NCP: {
                    if (this.webs.getValue() && Criticals.mc.world.getBlockState(new BlockPos(Criticals.mc.player)).getBlock() instanceof BlockWeb) {
                        Criticals.mc.player.connection.sendPacket(new CPacketPlayer.Position(Criticals.mc.player.posX, Criticals.mc.player.posY + 0.0625101, Criticals.mc.player.posZ, false));
                        Criticals.mc.player.connection.sendPacket(new CPacketPlayer.Position(Criticals.mc.player.posX, Criticals.mc.player.posY, Criticals.mc.player.posZ, false));
                        Criticals.mc.player.connection.sendPacket(new CPacketPlayer.Position(Criticals.mc.player.posX, Criticals.mc.player.posY + 0.0125, Criticals.mc.player.posZ, false));
                        Criticals.mc.player.connection.sendPacket(new CPacketPlayer.Position(Criticals.mc.player.posX, Criticals.mc.player.posY, Criticals.mc.player.posZ, false));
                        break;
                    }
                    Criticals.mc.player.connection.sendPacket(new CPacketPlayer.Position(Criticals.mc.player.posX, Criticals.mc.player.posY + 0.11, Criticals.mc.player.posZ, false));
                    Criticals.mc.player.connection.sendPacket(new CPacketPlayer.Position(Criticals.mc.player.posX, Criticals.mc.player.posY + 0.1100013579, Criticals.mc.player.posZ, false));
                    Criticals.mc.player.connection.sendPacket(new CPacketPlayer.Position(Criticals.mc.player.posX, Criticals.mc.player.posY + 1.3579E-6, Criticals.mc.player.posZ, false));
                }
            }
            Criticals.mc.player.onCriticalHit(attackedEntity);
        }
    }

    private static enum Mode {
        PACKET,
        NCP

    }
}


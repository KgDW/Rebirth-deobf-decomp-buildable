package me.rebirthclient.mod.modules.impl.misc;

import com.mojang.authlib.GameProfile;
import java.util.Random;
import java.util.UUID;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.util.DamageUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.render.EarthPopChams;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FakePlayer
extends Module {
    private final Setting<String> name = this.add(new Setting<>("Name", ""));
    private final Setting<Boolean> move = this.add(new Setting<>("Move", false));
    private final Setting<Boolean> damage = this.add(new Setting<>("Damage", true).setParent());
    private final Setting<Integer> totemHurtTime = this.add(new Setting<>("TotemHurtTime", 25, 0, 50, v -> this.damage.isOpen()));
    private final Setting<Integer> hurtTime = this.add(new Setting<>("HurtTime", 10, 0, 50, v -> this.damage.isOpen()));
    private final Setting<Integer> gappleDelay = this.add(new Setting<>("GappleDelay", 100, 0, 200, v -> this.damage.isOpen()));
    EntityOtherPlayerMP player = null;
    int ticks3 = 0;
    boolean pop = false;
    int ticks = 0;
    int ticks2 = 0;

    public FakePlayer() {
        super("FakePlayer", "Summons a client-side fake player", Category.MISC);
    }

    @Override
    public String getInfo() {
        return this.name.getValue();
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onReceivePacket(PacketEvent.Receive event) {
        if (FakePlayer.fullNullCheck()) {
            return;
        }
        if (event.isCanceled()) {
            return;
        }
        if (event.getPacket() instanceof SPacketExplosion && this.damage.getValue()) {
            if (this.player.getDistance(((SPacketExplosion)event.getPacket()).posX, ((SPacketExplosion)event.getPacket()).posY, ((SPacketExplosion)event.getPacket()).getZ()) > 8.0) {
                return;
            }
            if (this.ticks3 > this.hurtTime.getValue() - 1 && !this.pop) {
                BlockPos pos = new BlockPos(((SPacketExplosion)event.getPacket()).getX(), ((SPacketExplosion)event.getPacket()).getY(), ((SPacketExplosion)event.getPacket()).getZ());
                float damage = DamageUtil.calculateDamage(pos.down(), this.player);
                this.doPop(damage);
                this.ticks3 = 0;
            } else if (this.ticks3 > this.totemHurtTime.getValue() - 1) {
                BlockPos pos = new BlockPos(((SPacketExplosion)event.getPacket()).getX(), ((SPacketExplosion)event.getPacket()).getY(), ((SPacketExplosion)event.getPacket()).getZ());
                float damage = DamageUtil.calculateDamage(pos.down(), this.player);
                this.doPop(damage);
                this.pop = false;
                this.ticks3 = 0;
            }
        }
    }

    private void doPop(float damage) {
        float healthDamage = damage - this.player.getAbsorptionAmount();
        if ((double)(this.player.getHealth() - healthDamage) < 0.1) {
            EarthPopChams.INSTANCE.onTotemPop(this.player);
            FakePlayer.mc.world.playSound(FakePlayer.mc.player, this.player.posX, this.player.posY, this.player.posZ, SoundEvents.ITEM_TOTEM_USE, FakePlayer.mc.player.getSoundCategory(), 1.0f, 1.0f);
            this.player.setHealth(2.0f);
            this.player.setAbsorptionAmount(8.0f);
            this.pop = true;
        } else {
            if (healthDamage < 0.0f) {
                healthDamage = 0.0f;
            }
            this.player.setHealth(this.player.getHealth() - healthDamage);
            float absorptionAmount = this.player.getAbsorptionAmount() - damage;
            if (absorptionAmount < 0.0f) {
                absorptionAmount = 0.0f;
            }
            this.player.setAbsorptionAmount(absorptionAmount);
        }
    }

    @Override
    public void onEnable() {
        this.sendMessage("Spawned a fakeplayer with the name " + this.name.getValue() + ".");
        if (FakePlayer.mc.player == null || FakePlayer.mc.player.isDead) {
            this.disable();
            return;
        }
        this.player = new EntityOtherPlayerMP(FakePlayer.mc.world, new GameProfile(UUID.fromString("0f75a81d-70e5-43c5-b892-f33c524284f2"), this.name.getValue()));
        this.player.copyLocationAndAnglesFrom(FakePlayer.mc.player);
        this.player.rotationYawHead = FakePlayer.mc.player.rotationYawHead;
        this.player.rotationYaw = FakePlayer.mc.player.rotationYaw;
        this.player.rotationPitch = FakePlayer.mc.player.rotationPitch;
        this.player.setGameType(GameType.SURVIVAL);
        this.player.inventory.copyInventory(FakePlayer.mc.player.inventory);
        this.player.setHealth(20.0f);
        this.player.setAbsorptionAmount(16.0f);
        FakePlayer.mc.world.addEntityToWorld(-12345, this.player);
        this.player.onLivingUpdate();
    }

    @Override
    public void onDisable() {
        if (FakePlayer.mc.world != null) {
            FakePlayer.mc.world.removeEntityFromWorld(-12345);
        }
    }

    @Override
    public void onLogout() {
        if (this.isOn()) {
            this.disable();
        }
    }

    @Override
    public void onLogin() {
        if (this.isOn()) {
            this.disable();
        }
    }

    @Override
    public void onTick() {
        ++this.ticks;
        ++this.ticks2;
        ++this.ticks3;
        if (this.player != null) {
            if (this.ticks > this.gappleDelay.getValue() - 1) {
                this.player.setAbsorptionAmount(16.0f);
                this.ticks = 0;
            }
            if (this.ticks2 > 19) {
                float health = this.player.getHealth() + 1.0f;
                if (health > 20.0f) {
                    health = 20.0f;
                }
                this.player.setHealth(health);
                this.ticks2 = 0;
            }
            Random random = new Random();
            this.player.moveForward = FakePlayer.mc.player.moveForward + (float)random.nextInt(5) / 10.0f;
            this.player.moveStrafing = FakePlayer.mc.player.moveStrafing + (float)random.nextInt(5) / 10.0f;
            if (this.move.getValue()) {
                this.travel(this.player.moveStrafing, this.player.moveVertical, this.player.moveForward);
            }
        }
    }

    public void travel(float strafe, float vertical, float forward) {
        double d0 = this.player.posY;
        float f1 = 0.8f;
        float f2 = 0.02f;
        float f3 = EnchantmentHelper.getDepthStriderModifier(this.player);
        if (f3 > 3.0f) {
            f3 = 3.0f;
        }
        if (!this.player.onGround) {
            f3 *= 0.5f;
        }
        if (f3 > 0.0f) {
            f1 += (0.54600006f - f1) * f3 / 3.0f;
            f2 += (this.player.getAIMoveSpeed() - f2) * f3 / 4.0f;
        }
        this.player.moveRelative(strafe, vertical, forward, f2);
        this.player.move(MoverType.SELF, this.player.motionX, this.player.motionY, this.player.motionZ);
        this.player.motionX *= f1;
        this.player.motionY *= 0.8f;
        this.player.motionZ *= f1;
        if (!this.player.hasNoGravity()) {
            this.player.motionY -= 0.02;
        }
        if (this.player.collidedHorizontally && this.player.isOffsetPositionInLiquid(this.player.motionX, this.player.motionY + (double)0.6f - this.player.posY + d0, this.player.motionZ)) {
            this.player.motionY = 0.3f;
        }
    }
}


package me.rebirthclient.mod.modules.impl.render;

import com.google.common.collect.Sets;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Set;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.RenderEntityEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoLag
extends Module {
    private static final Set<SoundEvent> BLACKLIST = Sets.newHashSet();
    public static NoLag INSTANCE;
    public final Setting<Boolean> antiSpam = this.add(new Setting<>("AntiSpam", true));
    public final Setting<Boolean> antiPopLag = this.add(new Setting<>("AntiPopLag", true));
    public final Setting<Boolean> skulls = this.add(new Setting<>("WitherSkulls", false));
    public final Setting<Boolean> tnt = this.add(new Setting<>("PrimedTNT", false));
    public final Setting<Boolean> scoreBoards = this.add(new Setting<>("ScoreBoards", true));
    public final Setting<Boolean> glowing = this.add(new Setting<>("GlowingEntities", true));
    public final Setting<Boolean> parrots = this.add(new Setting<>("Parrots", true));
    private final Setting<Boolean> antiSound = this.add(new Setting<>("AntiSound", true).setParent());
    public final Setting<Boolean> armor = this.add(new Setting<>("Armor", true, v -> this.antiSound.isOpen()));
    private final Setting<Boolean> crystals = this.add(new Setting<>("Crystals", true, v -> this.antiSound.isOpen()));

    public NoLag() {
        super("NoLag", "Removes several things that may cause fps drops", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (!this.glowing.getValue()) {
            return;
        }
        for (Entity entity : NoLag.mc.world.loadedEntityList) {
            if (!entity.isGlowing()) continue;
            entity.setGlowing(false);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onReceivePacket(PacketEvent.Receive event) {
        String chat;
        if (event.isCanceled()) {
            return;
        }
        if (event.getPacket() instanceof SPacketChat && this.antiPopLag.getValue() && ((chat = ((SPacketChat)event.getPacket()).chatComponent.getUnformattedText()).contains("\u3f01") || chat.contains("\u3801") || chat.contains("\u1b01") || chat.contains("\u1201") || chat.contains("\u0101") || chat.contains("\u5b01") || chat.contains("\u61fa"))) {
            event.setCanceled(true);
            ((SPacketChat)event.getPacket()).chatComponent = null;
            this.sendMessage(ChatFormatting.RED + "Removed lag text");
            return;
        }
        if (event.getPacket() instanceof SPacketChat && this.antiSpam.getValue() && ((chat = ((SPacketChat)event.getPacket()).chatComponent.getUnformattedText()).contains("Sorry, but you can't change that here.") || chat.contains("Sorry, but you can't place things here.") || chat.contains("Sorry, but you can't break that block here.") || chat.contains("Sorry, but you can't use that here.") || chat.contains("[AdvancedPortals] You don't have permission to build here!") || chat.contains("Sorry, but you can't PvP here.") || chat.contains("You cannot teleport while in spectator mode.") || chat.contains("\u4f60\u83ab\u5f97 S.use \u6743\u9650!"))) {
            event.setCanceled(true);
            ((SPacketChat)event.getPacket()).chatComponent = null;
            return;
        }
        if (event.getPacket() instanceof SPacketSoundEffect && this.antiSound.getValue()) {
            SPacketSoundEffect packet = event.getPacket();
            if (this.crystals.getValue() && packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                event.setCanceled(true);
                return;
            }
            if (BLACKLIST.contains(packet.getSound()) && this.armor.getValue()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onRenderEntity(RenderEntityEvent event) {
        if (event.getEntity() != null) {
            if (this.skulls.getValue() && event.getEntity() instanceof EntityWitherSkull) {
                event.setCanceled(true);
            }
            if (this.tnt.getValue() && event.getEntity() instanceof EntityTNTPrimed) {
                event.setCanceled(true);
            }
            if (this.parrots.getValue() && event.getEntity() instanceof EntityParrot) {
                event.setCanceled(true);
            }
        }
    }
}


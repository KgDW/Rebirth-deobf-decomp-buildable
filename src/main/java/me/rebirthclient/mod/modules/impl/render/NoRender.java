package me.rebirthclient.mod.modules.impl.render;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.asm.accessors.IGuiBossOverlay;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.BossInfo;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class NoRender
extends Module {
    public static NoRender INSTANCE = new NoRender();
    public final Setting<Boolean> night = this.add(new Setting<>("Night", true));
    public final Setting<Boolean> armor = this.add(new Setting<>("Armor", true));
    public final Setting<Boolean> fire = this.add(new Setting<>("Fire", true));
    public final Setting<Boolean> blind = this.add(new Setting<>("Blind", true));
    public final Setting<Boolean> nausea = this.add(new Setting<>("Nausea", true));
    public final Setting<Boolean> fog = this.add(new Setting<>("Fog", true));
    public final Setting<Boolean> noWeather = this.add(new Setting<>("Weather", true));
    public final Setting<Boolean> hurtCam = this.add(new Setting<>("HurtCam", true));
    public final Setting<Boolean> totemPops = this.add(new Setting<>("TotemPop", true));
    public final Setting<Boolean> blocks = this.add(new Setting<>("Block", true));
    public final Setting<Boolean> exp = this.add(new Setting<>("Exp", true));
    public final Setting<Boolean> explosion = this.add(new Setting<>("Explosion[!]", false));
    public final Setting<Boolean> skyLight = this.add(new Setting<>("SkyLight", false));
    public final Setting<Boolean> advancements = this.add(new Setting<>("Advancements", false));
    public final Setting<Boss> boss = this.add(new Setting<>("BossBars", Boss.NONE));
    public final Setting<Float> scale = this.add(new Setting<>("Scale", 0.5f, 0.5f, 1.0f, v -> this.boss.getValue() == Boss.MINIMIZE || this.boss.getValue() == Boss.STACK));
    boolean gamma = false;
    float oldBright;
    private static final ResourceLocation GUI_BARS_TEXTURES = new ResourceLocation("textures/gui/bars.png");

    public NoRender() {
        super("NoRender", "Prevent some animation", Category.RENDER);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onRenderPre(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && this.boss.getValue() != Boss.NONE) {
            event.setCanceled(true);
        }
    }

    @Override
    public void onUpdate() {
        if (this.blind.getValue() && NoRender.mc.player.isPotionActive(MobEffects.BLINDNESS)) {
            NoRender.mc.player.removePotionEffect(MobEffects.BLINDNESS);
        }
        if (this.noWeather.getValue() && NoRender.mc.world.isRaining()) {
            NoRender.mc.world.setRainStrength(0.0f);
        }
        if (NoRender.mc.player.isPotionActive(MobEffects.NAUSEA) && this.nausea.getValue()) {
            NoRender.mc.player.removePotionEffect(MobEffects.NAUSEA);
        }
        if (this.night.getValue()) {
            this.gamma = true;
            NoRender.mc.gameSettings.gammaSetting = 100.0f;
        } else if (this.gamma) {
            NoRender.mc.gameSettings.gammaSetting = this.oldBright;
            this.gamma = false;
        }
    }

    @SubscribeEvent
    public void fog_density(EntityViewRenderEvent.FogDensity event) {
        if (!this.fog.getValue()) {
            event.setDensity(0.0f);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void blockOverlayEventListener(RenderBlockOverlayEvent event) {
        if (NoRender.fullNullCheck()) {
            return;
        }
        if (!this.fire.getValue() && !this.blocks.getValue()) {
            return;
        }
        if (event.getOverlayType() != RenderBlockOverlayEvent.OverlayType.FIRE && !this.blocks.getValue()) {
            return;
        }
        event.setCanceled(true);
    }

    @Override
    public void onEnable() {
        this.oldBright = NoRender.mc.gameSettings.gammaSetting;
    }

    @Override
    public void onDisable() {
        NoRender.mc.gameSettings.gammaSetting = this.oldBright;
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (this.exp.getValue()) {
            for (Entity entity : NoRender.mc.world.getLoadedEntityList()) {
                if (!(entity instanceof EntityExpBottle)) continue;
                NoRender.mc.world.removeEntity(entity);
            }
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onReceivePacket(PacketEvent.Receive event) {
        SPacketSoundEffect packet;
        if (event.isCanceled()) {
            return;
        }
        if (this.explosion.getValue() && event.getPacket() instanceof SPacketExplosion) {
            event.setCanceled(true);
            return;
        }
        if (this.exp.getValue() && event.getPacket() instanceof SPacketSoundEffect && (packet = event.getPacket()).getCategory() == SoundCategory.NEUTRAL && packet.getSound() == SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderPost(RenderGameOverlayEvent.Post event) {
        block7: {
            block8: {
                if (event.getType() != RenderGameOverlayEvent.ElementType.BOSSINFO || this.boss.getValue() == Boss.NONE) break block7;
                if (this.boss.getValue() != Boss.MINIMIZE) break block8;
                Map<UUID, BossInfoClient> map = ((IGuiBossOverlay)NoRender.mc.ingameGUI.getBossOverlay()).getMapBossInfos();
                if (map == null) {
                    return;
                }
                ScaledResolution scaledresolution = new ScaledResolution(mc);
                int i = scaledresolution.getScaledWidth();
                int j = 12;
                for (Map.Entry<UUID, BossInfoClient> entry : map.entrySet()) {
                    BossInfoClient info = entry.getValue();
                    String text2 = info.getName().getFormattedText();
                    int k = (int)((float)i / this.scale.getValue() / 2.0f - 91.0f);
                    GL11.glScaled(this.scale.getValue(), this.scale.getValue(), 1.0);
                    if (!event.isCanceled()) {
                        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                        mc.getTextureManager().bindTexture(GUI_BARS_TEXTURES);
                        ((IGuiBossOverlay)NoRender.mc.ingameGUI.getBossOverlay()).invokeRender(k, j, info);
                        NoRender.mc.fontRenderer.drawStringWithShadow(text2, (float)i / this.scale.getValue() / 2.0f - (float)NoRender.mc.fontRenderer.getStringWidth(text2) / 2.0f, (float)(j - 9), 0xFFFFFF);
                    }
                    GL11.glScaled(1.0 / (double) this.scale.getValue(), 1.0 / (double) this.scale.getValue(), 1.0);
                    j += 10 + NoRender.mc.fontRenderer.FONT_HEIGHT;
                }
                break block7;
            }
            if (this.boss.getValue() != Boss.STACK) break block7;
            Map<UUID, BossInfoClient> map = ((IGuiBossOverlay)NoRender.mc.ingameGUI.getBossOverlay()).getMapBossInfos();
            HashMap<String, Pair<BossInfoClient, Integer>> to = new HashMap<>();
            for (Map.Entry<UUID, BossInfoClient> entry2 : map.entrySet()) {
                Pair<BossInfoClient, Integer> p;
                String s = entry2.getValue().getName().getFormattedText();
                if (to.containsKey(s)) {
                    p = to.get(s);
                    p = new Pair<>(p.getKey(), p.getValue() + 1);
                    to.put(s, p);
                    continue;
                }
                p = new Pair<>(entry2.getValue(), 1);
                to.put(s, p);
            }
            ScaledResolution scaledresolution2 = new ScaledResolution(mc);
            int l = scaledresolution2.getScaledWidth();
            int m = 12;
            for (Map.Entry entry3 : to.entrySet()) {
                String text3 = (String)entry3.getKey();
                BossInfoClient info2 = (BossInfoClient)((Pair)entry3.getValue()).getKey();
                int a = (Integer)((Pair)entry3.getValue()).getValue();
                text3 = text3 + " x" + a;
                int k2 = (int)((float)l / this.scale.getValue() / 2.0f - 91.0f);
                GL11.glScaled(this.scale.getValue(), this.scale.getValue(), 1.0);
                if (!event.isCanceled()) {
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    mc.getTextureManager().bindTexture(GUI_BARS_TEXTURES);
                    ((IGuiBossOverlay)NoRender.mc.ingameGUI.getBossOverlay()).invokeRender(k2, m, info2);
                    NoRender.mc.fontRenderer.drawStringWithShadow(text3, (float)l / this.scale.getValue() / 2.0f - (float)NoRender.mc.fontRenderer.getStringWidth(text3) / 2.0f, (float)(m - 9), 0xFFFFFF);
                }
                GL11.glScaled(1.0 / (double) this.scale.getValue(), 1.0 / (double) this.scale.getValue(), 1.0);
                m += 10 + NoRender.mc.fontRenderer.FONT_HEIGHT;
            }
        }
    }

    public static class Pair<T, S> {
        T key;
        S value;

        public Pair(T key, S value) {
            this.key = key;
            this.value = value;
        }

        public T getKey() {
            return this.key;
        }

        public void setKey(T key) {
            this.key = key;
        }

        public S getValue() {
            return this.value;
        }

        public void setValue(S value) {
            this.value = value;
        }
    }

    public static enum Boss {
        NONE,
        REMOVE,
        STACK,
        MINIMIZE

    }
}


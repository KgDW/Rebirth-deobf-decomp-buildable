package me.rebirthclient.api.managers.impl;

import com.google.common.base.Strings;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.events.impl.*;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.commands.Command;
import me.rebirthclient.mod.gui.click.items.other.Particle;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.impl.client.Title;
import me.rebirthclient.mod.modules.impl.combat.CombatSetting;
import me.rebirthclient.mod.modules.impl.combat.Surround;
import me.rebirthclient.mod.modules.impl.render.RenderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventManager
extends Mod {
    private final Timer logoutTimer = new Timer();
    private final AtomicBoolean tickOngoing;
    private final Particle.Util particles = new Particle.Util(300);

    public EventManager() {
        this.tickOngoing = new AtomicBoolean(false);
    }

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public boolean ticksOngoing() {
        return this.tickOngoing.get();
    }

    public void onUnload() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!EventManager.fullNullCheck() && event.getEntity().getEntityWorld().isRemote && event.getEntityLiving().equals(EventManager.mc.player)) {
            Managers.MODULES.onUpdate();
            Managers.MODULES.sortModules();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent t) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen != null) {
            if (ClickGui.INSTANCE.particles.getValue()) {
                this.particles.drawParticles();
            }
            if (Minecraft.getMinecraft().world == null) {
                Managers.TEXT.drawString("Rebirth " + ChatFormatting.WHITE + "alpha", 1.0f, screen.height - Managers.TEXT.getFontHeight2(), Managers.COLORS.getNormalCurrent().getRGB(), true);
                Managers.TEXT.drawRollingRainbowString("powered by iMadCat", 1.0f, screen.height - Managers.TEXT.getFontHeight2() * 2, true);
            } else {
                Managers.TEXT.drawString("Rebirth " + ChatFormatting.WHITE + "alpha", (float)screen.width - 1.0f - (float)Managers.TEXT.getStringWidth("Rebirth alpha"), screen.height - Managers.TEXT.getFontHeight2(), Managers.COLORS.getNormalCurrent().getRGB(), true);
                Managers.TEXT.drawRollingRainbowString("powered by iMadCat", (float)screen.width - 1.0f - (float)Managers.TEXT.getStringWidth("powered by iMadCat"), screen.height - Managers.TEXT.getFontHeight2() * 2, true);
            }
        }
    }

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.logoutTimer.reset();
        Managers.MODULES.onLogin();
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Managers.CONFIGS.saveConfig(Managers.CONFIGS.config.replaceFirst("Rebirth/", ""));
        Managers.MODULES.onLogout();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        Title.updateTitle();
        Managers.COLORS.rainbowProgress += ClickGui.INSTANCE.rainbowSpeed.getValue();
        if (EventManager.fullNullCheck()) {
            return;
        }
        Managers.MODULES.onTick();
        for (EntityPlayer player : EventManager.mc.world.playerEntities) {
            if (player == null || player.getHealth() > 0.0f) continue;
            MinecraftForge.EVENT_BUS.post(new DeathEvent(player));
            Managers.MODULES.onDeath(player);
        }
        if (CombatUtil.isHole(EntityUtil.getPlayerPos(), false, 4, true) && Surround.INSTANCE.isOff() && Surround.INSTANCE.enableInHole.getValue() && EventManager.mc.player.onGround && !MovementUtil.isJumping()) {
            Surround.INSTANCE.enable();
        }
        if (CombatSetting.INSTANCE.isOff()) {
            CombatSetting.INSTANCE.enable();
        }
        if (RenderSetting.INSTANCE.isOff()) {
            RenderSetting.INSTANCE.enable();
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (EventManager.fullNullCheck()) {
            return;
        }
        if (event.getStage() == 0) {
            Managers.SPEED.updateValues();
            Managers.ROTATIONS.updateRotations();
            Managers.POSITION.updatePosition();
        }
        if (event.getStage() == 1) {
            if (CombatSetting.INSTANCE.resetRotation.getValue()) {
                Managers.ROTATIONS.resetRotations();
            }
            if (CombatSetting.INSTANCE.resetPosition.getValue()) {
                Managers.POSITION.restorePosition();
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getStage() != 0)
            return;
        Managers.SERVER.onPacketReceived();
        if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = event.getPacket();
            if (packet.getOpCode() == 35 && packet.getEntity(mc.world) instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) packet.getEntity(mc.world);
                MinecraftForge.EVENT_BUS.post(new TotemPopEvent(player));
            }
        }
        if (event.getPacket() instanceof SPacketPlayerListItem && !fullNullCheck() && this.logoutTimer.passedS(1.0D)) {
            SPacketPlayerListItem packet = event.getPacket();
            if (!SPacketPlayerListItem.Action.ADD_PLAYER.equals(packet.getAction()) && !SPacketPlayerListItem.Action.REMOVE_PLAYER.equals(packet.getAction()))
                return;
            packet.getEntries().stream().filter(Objects::nonNull).filter(data -> (!Strings.isNullOrEmpty(data.getProfile().getName()) || data.getProfile().getId() != null))
                    .forEach(data -> {
                        String name;
                        EntityPlayer entity;
                        UUID id = data.getProfile().getId();
                        switch (packet.getAction()) {
                            case ADD_PLAYER:
                                name = data.getProfile().getName();
                                MinecraftForge.EVENT_BUS.post(new ConnectionEvent(0, id, name));
                                break;
                            case REMOVE_PLAYER:
                                entity = mc.world.getPlayerEntityByUUID(id);
                                if (entity != null) {
                                    String logoutName = entity.getName();
                                    MinecraftForge.EVENT_BUS.post(new ConnectionEvent(1, entity, id, logoutName));
                                    break;
                                }
                                MinecraftForge.EVENT_BUS.post(new ConnectionEvent(2, id, null));
                                break;
                        }
                    });
        }
        if (event.getPacket() instanceof net.minecraft.network.play.server.SPacketTimeUpdate)
            Managers.SERVER.update();
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (EventManager.fullNullCheck()) {
            return;
        }
        if (event.isCanceled()) {
            return;
        }
        Managers.FPS.update();
        EventManager.mc.profiler.startSection("Rebirth");
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(1.0f);
        Render3DEvent render3dEvent = new Render3DEvent(event.getPartialTicks());
        Managers.MODULES.onRender3D(render3dEvent);
        GlStateManager.glLineWidth(1.0f);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        EventManager.mc.profiler.endSection();
    }

    @SubscribeEvent
    public void renderHUD(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            Managers.TEXT.updateResolution();
        }
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
            ScaledResolution resolution = new ScaledResolution(mc);
            Render2DEvent render2DEvent = new Render2DEvent(event.getPartialTicks(), resolution);
            Managers.MODULES.onRender2D(render2DEvent);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    @SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState()) {
            Managers.MODULES.onKeyInput(Keyboard.getEventKey());
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onChatSent(ClientChatEvent event) {
        if (event.getMessage().startsWith(Command.getCommandPrefix())) {
            event.setCanceled(true);
            try {
                EventManager.mc.ingameGUI.getChatGUI().addToSentMessages(event.getMessage());
                if (event.getMessage().length() > 1) {
                    Managers.COMMANDS.executeCommand(event.getMessage().substring(Command.getCommandPrefix().length() - 1));
                } else {
                    Command.sendMessage("Please enter a command.");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Command.sendMessage(ChatFormatting.RED + "An error occurred while running this command. Check the log!");
            }
        }
    }

    @SubscribeEvent
    public void onFogColor(EntityViewRenderEvent.FogColors event) {
        RenderFogColorEvent fogColorEvent = new RenderFogColorEvent();
        MinecraftForge.EVENT_BUS.post(fogColorEvent);
        if (fogColorEvent.isCanceled()) {
            event.setRed((float)fogColorEvent.getColor().getRed() / 255.0f);
            event.setGreen((float)fogColorEvent.getColor().getGreen() / 255.0f);
            event.setBlue((float)fogColorEvent.getColor().getBlue() / 255.0f);
        }
    }

    @SubscribeEvent
    public void BlockBreak(DamageBlockEvent event) {
        if (event.getPosition().getY() == -1) {
            return;
        }
        EntityPlayer breaker = (EntityPlayer)EventManager.mc.world.getEntityByID(event.getBreakerId());
        if (breaker == null || breaker.getDistance((double)event.getPosition().getX() + 0.5, event.getPosition().getY(), (double)event.getPosition().getZ() + 0.5) > 7.0) {
            return;
        }
        BreakManager.MineMap.put(breaker, event.getPosition());
    }

}


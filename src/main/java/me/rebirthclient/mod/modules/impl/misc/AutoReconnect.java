package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoReconnect
extends Module {
    public static AutoReconnect INSTANCE;
    private static ServerData serverData;
    private final Setting<Integer> delay = this.add(new Setting<>("Delay", 5));

    public AutoReconnect() {
        super("AutoReconnect", "Reconnects you if you disconnect", Category.MISC);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void sendPacket(GuiOpenEvent event) {
        if (AutoReconnect.fullNullCheck()) {
            return;
        }
        if (event.getGui() instanceof GuiDisconnected) {
            this.updateLastConnectedServer();
            GuiDisconnected disconnected = (GuiDisconnected)event.getGui();
            event.setGui(new GuiDisconnectedHook(disconnected));
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (AutoReconnect.fullNullCheck()) {
            return;
        }
        this.updateLastConnectedServer();
    }

    public void updateLastConnectedServer() {
        ServerData data = mc.getCurrentServerData();
        if (data != null) {
            serverData = data;
        }
    }

    private class GuiDisconnectedHook
    extends GuiDisconnected {
        private final Timer timer;

        public GuiDisconnectedHook(GuiDisconnected disconnected) {
            super(disconnected.parentScreen, disconnected.reason, disconnected.message);
            this.timer = new Timer();
            this.timer.reset();
        }

        public void updateScreen() {
            if (this.timer.passedS((Integer) AutoReconnect.this.delay.getValue())) {
                this.mc.displayGuiScreen(new GuiConnecting(this.parentScreen, this.mc, serverData == null ? this.mc.currentServerData : serverData));
            }
        }

        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            String reconnectString = "Reconnecting in " + MathUtil.round((double)((long)(AutoReconnect.this.delay.getValue() * 1000) - this.timer.getPassedTimeMs()) / 1000.0, 1);
            this.mc.fontRenderer.drawString(reconnectString, (float)this.width / 2.0f - (float)this.mc.fontRenderer.getStringWidth(reconnectString) / 2.0f, (float)(this.height - 16), 0xFFFFFF, true);
        }
    }
}


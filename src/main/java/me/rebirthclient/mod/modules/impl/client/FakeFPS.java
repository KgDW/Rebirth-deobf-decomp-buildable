package me.rebirthclient.mod.modules.impl.client;

import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.settings.GameSettings;

public class FakeFPS
extends Module {
    public static FakeFPS INSTANCE;
    public final Setting<Integer> times = this.add(new Setting<>("times", 5, 1, 100));
    int lastFps = 0;

    public FakeFPS() {
        super("FakeFPS", "FakeFPS", Category.CLIENT);
        INSTANCE = this;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        Object[] objectArray = new Object[8];
        objectArray[0] = Minecraft.getDebugFPS();
        objectArray[1] = RenderChunk.renderChunksUpdated;
        objectArray[2] = RenderChunk.renderChunksUpdated == 1 ? "" : "s";
        objectArray[3] = (float)Minecraft.getMinecraft().gameSettings.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() ? "inf" : Integer.valueOf(Minecraft.getMinecraft().gameSettings.limitFramerate);
        objectArray[4] = Minecraft.getMinecraft().gameSettings.enableVsync ? " vsync" : "";
        Object object = objectArray[5] = Minecraft.getMinecraft().gameSettings.fancyGraphics ? "" : " fast";
        objectArray[6] = Minecraft.getMinecraft().gameSettings.clouds == 0 ? "" : (Minecraft.getMinecraft().gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds");
        objectArray[7] = OpenGlHelper.useVbo() ? " vbo" : "";
        Minecraft.getMinecraft().debug = String.format("%d fps (%d chunk update%s) T: %s%s%s%s%s", objectArray);
        if (Minecraft.getDebugFPS() == this.lastFps) {
            return;
        }
        Minecraft.debugFPS *= this.times.getValue();
        this.lastFps = Minecraft.getDebugFPS();
    }
}


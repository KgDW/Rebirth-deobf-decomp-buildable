package me.rebirthclient.asm;

import java.util.Map;
import me.rebirthclient.Rebirth;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

@IFMLLoadingPlugin.Name(value="Rebirth")
@IFMLLoadingPlugin.MCVersion(value="1.12.2")
public class MixinLoader
implements IFMLLoadingPlugin {

    public MixinLoader() {
        Rebirth.LOGGER.info("Loading rebirth mixins...\n");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.rebirth.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
        Rebirth.LOGGER.info(MixinEnvironment.getDefaultEnvironment().getObfuscationContext());
    }

    public String[] getASMTransformerClass() {
        return new String[0];
    }

    public String getModContainerClass() {
        return null;
    }

    public String getSetupClass() {
        return null;
    }

    public void injectData(Map<String, Object> data) {
        boolean isObfuscatedEnvironment = (Boolean) data.get("runtimeDeobfuscationEnabled");
    }

    public String getAccessTransformerClass() {
        return null;
    }
}


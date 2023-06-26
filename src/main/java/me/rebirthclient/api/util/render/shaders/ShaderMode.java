package me.rebirthclient.api.util.render.shaders;

import me.rebirthclient.api.util.render.shaders.FramebufferShader;
import me.rebirthclient.api.util.render.shaders.ShaderProducer;
import me.rebirthclient.api.util.render.shaders.shaders.AquaGlShader;
import me.rebirthclient.api.util.render.shaders.shaders.AquaShader;
import me.rebirthclient.api.util.render.shaders.shaders.BasicShader;
import me.rebirthclient.api.util.render.shaders.shaders.FlowGlShader;
import me.rebirthclient.api.util.render.shaders.shaders.FlowShader;
import me.rebirthclient.api.util.render.shaders.shaders.GangGlShader;
import me.rebirthclient.api.util.render.shaders.shaders.GlowShader;
import me.rebirthclient.api.util.render.shaders.shaders.HolyFuckShader;
import me.rebirthclient.api.util.render.shaders.shaders.PurpleShader;
import me.rebirthclient.api.util.render.shaders.shaders.RedShader;
import me.rebirthclient.api.util.render.shaders.shaders.SmokeShader;

public enum ShaderMode {
    Aqua("Aqua", AquaShader::INSTANCE),
    AQUAGLOW("AquaGlow", AquaGlShader::INSTANCE),
    FLOW("Flow", FlowShader::INSTANCE),
    FLOWBLUR("FlowBlur", () -> BasicShader.INSTANCE("flowglow_z.frag", 5.0E-4f)),
    FLOWGLOW("FlowGLow", FlowGlShader::INSTANCE),
    GHOST("Glow", GlowShader::INSTANCE),
    SMOKE("Smoke", SmokeShader::INSTANCE),
    RED("Red", RedShader::INSTANCE),
    HOLYFUCK("HolyFuck", HolyFuckShader::INSTANCE),
    GANG("Gang", GangGlShader::INSTANCE),
    BLUEFLAMES("BlueFlames", () -> BasicShader.INSTANCE("blueflames.frag", 0.01f)),
    GAMER("Gamer", () -> BasicShader.INSTANCE("gamer.frag", 0.03f)),
    CODEX("Codex", () -> BasicShader.INSTANCE("codex.frag")),
    GALAXY("Galaxy", () -> BasicShader.INSTANCE("galaxy33.frag", 0.001f)),
    DDEV("Ddev", () -> BasicShader.INSTANCE("ddev.frag")),
    CRAZY("Crazy", () -> BasicShader.INSTANCE("crazy.frag", 0.01f)),
    SNOW("Snow", () -> BasicShader.INSTANCE("snow.frag", 0.01f)),
    TECHNO("Techno", () -> BasicShader.INSTANCE("techno.frag", 0.01f)),
    GOLDEN("Golden", () -> BasicShader.INSTANCE("golden.frag", 0.01f)),
    HOTSHIT("HotShit", () -> BasicShader.INSTANCE("hotshit.frag", 0.005f)),
    GUISHADER("GuiShader", () -> BasicShader.INSTANCE("clickguishader.frag", 0.02f)),
    HIDEF("Hidef", () -> BasicShader.INSTANCE("hidef.frag", 0.05f)),
    HOMIE("Homie", () -> BasicShader.INSTANCE("homie.frag", 0.001f)),
    KFC("KFC", () -> BasicShader.INSTANCE("kfc.frag", 0.01f)),
    OHMYLORD("Lord", () -> BasicShader.INSTANCE("ohmylord.frag", 0.01f)),
    SHELDON("Sheldon", () -> BasicShader.INSTANCE("sheldon.frag", 0.001f)),
    SMOKY("Smoky", () -> BasicShader.INSTANCE("smoky.frag", 0.001f)),
    STROXINJAT("Stroxinjat", () -> BasicShader.INSTANCE("stroxinjat.frag")),
    WEIRD("Weird", () -> BasicShader.INSTANCE("weird.frag", 0.01f)),
    YIPPIEOWNS("YippieOwns", () -> BasicShader.INSTANCE("yippieOwns.frag")),
    PURPLE("Purple", PurpleShader::INSTANCE);

    private final String name;
    private final ShaderProducer shaderProducer;

    private ShaderMode(String name, ShaderProducer shaderProducer) {
        this.name = name;
        this.shaderProducer = shaderProducer;
    }

    public String getName() {
        return this.name;
    }

    public FramebufferShader getShader() {
        return this.shaderProducer.INSTANCE();
    }
}


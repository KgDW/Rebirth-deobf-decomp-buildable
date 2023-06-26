package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;
import me.rebirthclient.api.util.shaders.impl.fill.AquaShader;
import me.rebirthclient.api.util.shaders.impl.fill.CircleShader;
import me.rebirthclient.api.util.shaders.impl.fill.FillShader;
import me.rebirthclient.api.util.shaders.impl.fill.FlowShader;
import me.rebirthclient.api.util.shaders.impl.fill.GradientShader;
import me.rebirthclient.api.util.shaders.impl.fill.PhobosShader;
import me.rebirthclient.api.util.shaders.impl.fill.RainbowCubeShader;
import me.rebirthclient.api.util.shaders.impl.fill.SmokeShader;
import me.rebirthclient.api.util.shaders.impl.outline.AquaOutlineShader;
import me.rebirthclient.api.util.shaders.impl.outline.AstralOutlineShader;
import me.rebirthclient.api.util.shaders.impl.outline.CircleOutlineShader;
import me.rebirthclient.api.util.shaders.impl.outline.GlowShader;
import me.rebirthclient.api.util.shaders.impl.outline.GradientOutlineShader;
import me.rebirthclient.api.util.shaders.impl.outline.RainbowCubeOutlineShader;
import me.rebirthclient.api.util.shaders.impl.outline.SmokeOutlineShader;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Shaders
extends Module {
    private final Setting<fillShadermode> fillShader = this.add(new Setting<>("Fill Shader", fillShadermode.None));
    private final Setting<glowESPmode> glowESP = this.add(new Setting<>("Glow ESP", glowESPmode.None));
    public final Setting<Crystal1> crystal = this.add(new Setting<>("Crystals", Crystal1.None));
    private final Setting<Player1> player = this.add(new Setting<>("Players", Player1.None));
    private final Setting<Mob1> mob = this.add(new Setting<>("Mobs", Mob1.None));
    private final Setting<Itemsl> items = this.add(new Setting<>("Items", Itemsl.None));
    private final Setting<XPl> xpOrb = this.add(new Setting<>("XP", XPl.None));
    private final Setting<XPBl> xpBottle = this.add(new Setting<>("XPBottle", XPBl.None));
    private final Setting<EPl> enderPearl = this.add(new Setting<>("EnderPearl", EPl.None));
    private final Setting<Boolean> rangeCheck = this.add(new Setting<>("Range Check", true));
    public final Setting<Float> maxRange = this.add(new Setting<Object>("Max Range", 35.0f, 10.0f, 100.0f, object -> this.rangeCheck.getValue()));
    public final Setting<Float> minRange = this.add(new Setting<Object>("Min range", 0.0f, 0.0f, 5.0f, object -> this.rangeCheck.getValue()));
    private final Setting<Boolean> default1 = this.add(new Setting<>("Reset Setting", false));
    private final Setting<Boolean> Fpreset = this.add(new Setting<>("FutureRainbow Preset", false));
    private final Setting<Boolean> fadeFill = this.add(new Setting<>("Fade Fill", Boolean.FALSE, bl -> this.fillShader.getValue() == fillShadermode.Astral || this.glowESP.getValue() == glowESPmode.Astral));
    private final Setting<Boolean> fadeOutline = this.add(new Setting<>("FadeOL Fill", Boolean.FALSE, bl -> this.fillShader.getValue() == fillShadermode.Astral || this.glowESP.getValue() == glowESPmode.Astral));
    public final Setting<Float> duplicateOutline = this.add(new Setting<>("duplicateOutline", 1.0f, 0.0f, 20.0f));
    public final Setting<Float> duplicateFill = this.add(new Setting<>("Duplicate Fill", 1.0f, 0.0f, 5.0f));
    public final Setting<Float> speedOutline = this.add(new Setting<>("Speed Outline", 10.0f, 1.0f, 100.0f));
    public final Setting<Float> speedFill = this.add(new Setting<>("Speed Fill", 10.0f, 1.0f, 100.0f));
    public final Setting<Float> quality = this.add(new Setting<>("Shader Quality", 1.0f, 0.0f, 20.0f));
    public final Setting<Float> radius = this.add(new Setting<>("Shader Radius", 1.0f, 0.0f, 5.0f));
    public final Setting<Float> rad = this.add(new Setting<Object>("RAD Fill", 0.75f, 0.0f, 5.0f, object -> this.fillShader.getValue() == fillShadermode.Circle));
    public final Setting<Float> PI = this.add(new Setting<Object>("PI Fill", (float) Math.PI, 0.0f, 10.0f, object -> this.fillShader.getValue() == fillShadermode.Circle));
    public final Setting<Float> saturationFill = this.add(new Setting<Object>("saturation", 0.4f, 0.0f, 3.0f, object -> this.fillShader.getValue() == fillShadermode.Astral));
    public final Setting<Float> distfadingFill = this.add(new Setting<Object>("distfading", 0.56f, 0.0f, 1.0f, object -> this.fillShader.getValue() == fillShadermode.Astral));
    public final Setting<Float> titleFill = this.add(new Setting<Object>("Tile", 0.45f, 0.0f, 1.3f, object -> this.fillShader.getValue() == fillShadermode.Astral));
    public final Setting<Float> stepSizeFill = this.add(new Setting<Object>("Step Size", 0.2f, 0.0f, 0.7f, object -> this.fillShader.getValue() == fillShadermode.Astral));
    public final Setting<Float> volumStepsFill = this.add(new Setting<Object>("Volum Steps", 10.0f, 0.0f, 10.0f, object -> this.fillShader.getValue() == fillShadermode.Astral));
    public final Setting<Float> zoomFill = this.add(new Setting<Object>("Zoom", 3.9f, 0.0f, 20.0f, object -> this.fillShader.getValue() == fillShadermode.Astral));
    public final Setting<Float> formuparam2Fill = this.add(new Setting<Object>("formuparam2", 0.89f, 0.0f, 1.5f, object -> this.fillShader.getValue() == fillShadermode.Astral));
    public final Setting<Float> saturationOutline = this.add(new Setting<Object>("saturation", 0.4f, 0.0f, 3.0f, object -> this.glowESP.getValue() == glowESPmode.Astral));
    public final Setting<Integer> maxEntities = this.add(new Setting<>("Max Entities", 100, 10, 500));
    public final Setting<Integer> iterationsFill = this.add(new Setting<>("Iteration", 4, 3, 20, n -> this.fillShader.getValue() == fillShadermode.Astral));
    public final Setting<Integer> redFill = this.add(new Setting<>("Tick Regen", 0, 0, 100, n -> this.fillShader.getValue() == fillShadermode.Astral));
    public final Setting<Integer> MaxIterFill = this.add(new Setting<>("Max Iter", 5, 0, 30, n -> this.fillShader.getValue() == fillShadermode.Aqua));
    public final Setting<Integer> NUM_OCTAVESFill = this.add(new Setting<>("NUM_OCTAVES", 5, 1, 30, n -> this.fillShader.getValue() == fillShadermode.Smoke));
    public final Setting<Integer> BSTARTFIll = this.add(new Setting<>("BSTART", 0, 0, 1000, n -> this.fillShader.getValue() == fillShadermode.RainbowCube));
    public final Setting<Integer> GSTARTFill = this.add(new Setting<>("GSTART", 0, 0, 1000, n -> this.fillShader.getValue() == fillShadermode.RainbowCube));
    public final Setting<Integer> RSTARTFill = this.add(new Setting<>("RSTART", 0, 0, 1000, n -> this.fillShader.getValue() == fillShadermode.RainbowCube));
    public final Setting<Integer> WaveLenghtFIll = this.add(new Setting<>("Wave Lenght", 555, 0, 2000, n -> this.fillShader.getValue() == fillShadermode.RainbowCube));
    public final Setting<Integer> volumStepsOutline = this.add(new Setting<>("Volum Steps", 10, 0, 10, n -> this.glowESP.getValue() == glowESPmode.Astral));
    public final Setting<Integer> iterationsOutline = this.add(new Setting<>("Iteration", 4, 3, 20, n -> this.glowESP.getValue() == glowESPmode.Astral));
    public final Setting<Integer> MaxIterOutline = this.add(new Setting<>("Max Iter", 5, 0, 30, n -> this.glowESP.getValue() == glowESPmode.Aqua));
    public final Setting<Integer> NUM_OCTAVESOutline = this.add(new Setting<>("NUM_OCTAVES", 5, 1, 30, n -> this.glowESP.getValue() == glowESPmode.Smoke));
    public final Setting<Integer> BSTARTOutline = this.add(new Setting<>("BSTART", 0, 0, 1000, n -> this.glowESP.getValue() == glowESPmode.RainbowCube));
    public final Setting<Integer> GSTARTOutline = this.add(new Setting<>("GSTART", 0, 0, 1000, n -> this.glowESP.getValue() == glowESPmode.RainbowCube));
    public final Setting<Integer> RSTARTOutline = this.add(new Setting<>("RSTART", 0, 0, 1000, n -> this.glowESP.getValue() == glowESPmode.RainbowCube));
    public final Setting<Integer> alphaValue = this.add(new Setting<>("Alpha Outline", 255, 0, 255));
    public final Setting<Integer> WaveLenghtOutline = this.add(new Setting<>("Wave Lenght", 555, 0, 2000, n -> this.glowESP.getValue() == glowESPmode.RainbowCube));
    public final Setting<Integer> redOutline = this.add(new Setting<>("Red", 0, 0, 100, n -> this.glowESP.getValue() == glowESPmode.Astral));
    public final Setting<Float> alphaFill = this.add(new Setting<Object>("AlphaF", 1.0f, 0.0f, 1.0f, object -> this.fillShader.getValue() == fillShadermode.Astral || this.fillShader.getValue() == fillShadermode.Smoke));
    public final Setting<Float> blueFill = this.add(new Setting<Object>("BlueF", 0.0f, 0.0f, 5.0f, object -> this.fillShader.getValue() == fillShadermode.Astral));
    public final Setting<Float> greenFill = this.add(new Setting<Object>("GreenF", 0.0f, 0.0f, 5.0f, object -> this.fillShader.getValue() == fillShadermode.Astral));
    public final Setting<Float> tauFill = this.add(new Setting<Object>("TAU", (float) Math.PI * 2, 0.0f, 20.0f, object -> this.fillShader.getValue() == fillShadermode.Aqua));
    public final Setting<Float> creepyFill = this.add(new Setting<Object>("Creepy", 1.0f, 0.0f, 20.0f, object -> this.fillShader.getValue() == fillShadermode.Smoke));
    public final Setting<Float> moreGradientFill = this.add(new Setting<Object>("More Gradient", 1.0f, 0.0f, 10.0, object -> this.fillShader.getValue() == fillShadermode.Smoke));
    public final Setting<Float> distfadingOutline = this.add(new Setting<Object>("distfading", 0.56f, 0.0f, 1.0f, object -> this.glowESP.getValue() == glowESPmode.Astral));
    public final Setting<Float> titleOutline = this.add(new Setting<Object>("Tile", 0.45f, 0.0f, 1.3f, object -> this.glowESP.getValue() == glowESPmode.Astral));
    public final Setting<Float> stepSizeOutline = this.add(new Setting<Object>("Step Size", 0.19f, 0.0f, 0.7f, object -> this.glowESP.getValue() == glowESPmode.Astral));
    public final Setting<Float> zoomOutline = this.add(new Setting<Object>("Zoom", 3.9f, 0.0f, 20.0f, object -> this.glowESP.getValue() == glowESPmode.Astral));
    public final Setting<Float> formuparam2Outline = this.add(new Setting<Object>("formuparam2", 0.89f, 0.0f, 1.5f, object -> this.glowESP.getValue() == glowESPmode.Astral));
    public final Setting<Float> alphaOutline = this.add(new Setting<Object>("Alpha", 1.0f, 0.0f, 1.0f, object -> this.glowESP.getValue() == glowESPmode.Astral || this.glowESP.getValue() == glowESPmode.Gradient));
    public final Setting<Float> blueOutline = this.add(new Setting<Object>("Blue", 0.0f, 0.0f, 5.0f, object -> this.glowESP.getValue() == glowESPmode.Astral));
    public final Setting<Float> greenOutline = this.add(new Setting<Object>("Green", 0.0f, 0.0f, 5.0f, object -> this.glowESP.getValue() == glowESPmode.Astral));
    public final Setting<Float> tauOutline = this.add(new Setting<Object>("TAU", (float) Math.PI * 2, 0.0f, 20.0f, object -> this.glowESP.getValue() == glowESPmode.Aqua));
    public final Setting<Float> creepyOutline = this.add(new Setting<Object>("Gradient Creepy", 1.0f, 0.0f, 20.0f, object -> this.glowESP.getValue() == glowESPmode.Gradient));
    public final Setting<Float> moreGradientOutline = this.add(new Setting<Object>("More Gradient", 1.0f, 0.0f, 10.0f, object -> this.glowESP.getValue() == glowESPmode.Gradient));
    public final Setting<Float> radOutline = this.add(new Setting<Object>("RAD Outline", 0.75f, 0.0f, 5.0f, object -> this.glowESP.getValue() == glowESPmode.Circle));
    public final Setting<Float> PIOutline = this.add(new Setting<Object>("PI Outline", (float) Math.PI, 0.0f, 10.0f, object -> this.glowESP.getValue() == glowESPmode.Circle));
    public final Setting<Color> colorImgOutline = this.add(new Setting<>("ColorImgOutline", new Color(0, 0, 0, 255), n -> this.fillShader.getValue() == fillShadermode.RainbowCube || this.glowESP.getValue() == glowESPmode.RainbowCube));
    public final Setting<Color> thirdColorImgOutline = this.add(new Setting<>("ThirdColorImg", new Color(0, 0, 0, 255), n -> this.fillShader.getValue() == fillShadermode.Smoke || this.glowESP.getValue() == glowESPmode.Smoke));
    public final Setting<Color> colorESP = this.add(new Setting<>("ColorESP", new Color(0, 0, 0, 255)));
    public final Setting<Color> colorImgFill = this.add(new Setting<>("ColorImgFill", new Color(0, 0, 0, 255)));
    public final Setting<Color> thirdColorImgFIll = this.add(new Setting<>("SmokeImgFill", new Color(0, 0, 0, 255), n -> this.fillShader.getValue() == fillShadermode.Smoke || this.glowESP.getValue() == glowESPmode.Smoke));
    public final Setting<Color> secondColorImgFill = this.add(new Setting<>("SmokeFill", new Color(0, 0, 0, 255), n -> this.fillShader.getValue() == fillShadermode.Smoke || this.glowESP.getValue() == glowESPmode.Smoke));
    public boolean notShader = true;
    public static Shaders INSTANCE;

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent renderGameOverlayEvent) {
        if (Shaders.fullNullCheck()) {
            return;
        }
        if (renderGameOverlayEvent.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            if (Shaders.mc.world == null || Shaders.mc.player == null) {
                return;
            }
            GlStateManager.pushMatrix();
            this.notShader = false;
            Color color = new Color(this.colorImgFill.getValue().getRed(), this.colorImgFill.getValue().getGreen(), this.colorImgFill.getValue().getBlue(), this.colorImgFill.getValue().getAlpha());
            Color color2 = new Color(this.colorESP.getValue().getRed(), this.colorESP.getValue().getGreen(), this.colorESP.getValue().getBlue(), this.colorESP.getValue().getAlpha());
            Color color3 = new Color(this.secondColorImgFill.getValue().getRed(), this.secondColorImgFill.getValue().getGreen(), this.secondColorImgFill.getValue().getBlue(), this.secondColorImgFill.getValue().getAlpha());
            Color color4 = new Color(this.thirdColorImgOutline.getValue().getRed(), this.thirdColorImgOutline.getValue().getGreen(), this.thirdColorImgOutline.getValue().getBlue(), this.thirdColorImgOutline.getValue().getAlpha());
            Color color5 = new Color(this.thirdColorImgFIll.getValue().getRed(), this.thirdColorImgFIll.getValue().getGreen(), this.thirdColorImgFIll.getValue().getBlue(), this.thirdColorImgFIll.getValue().getAlpha());
            Color color6 = new Color(this.colorImgOutline.getValue().getRed(), this.colorImgOutline.getValue().getGreen(), this.colorImgOutline.getValue().getBlue(), this.colorImgOutline.getValue().getAlpha());
            if (this.glowESP.getValue() != glowESPmode.None && this.fillShader.getValue() != fillShadermode.None) {
                this.getFill();
                switch (this.fillShader.getValue()) {
                    case Astral: {
                        FlowShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        FlowShader.INSTANCE.stopDraw(Color.WHITE, 1.0f, 1.0f, this.duplicateFill.getValue(), this.redFill.getValue().floatValue(), this.greenFill.getValue(), this.blueFill.getValue(), this.alphaFill.getValue(), this.iterationsFill.getValue(), this.formuparam2Fill.getValue(), this.zoomFill.getValue(), this.volumStepsFill.getValue(), this.stepSizeFill.getValue(), this.titleFill.getValue(), this.distfadingFill.getValue(), this.saturationFill.getValue(), 0.0f, this.fadeFill.getValue() ? 1 : 0);
                        FlowShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                        break;
                    }
                    case Aqua: {
                        AquaShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        AquaShader.INSTANCE.stopDraw(color, 1.0f, 1.0f, this.duplicateFill.getValue(), this.MaxIterFill.getValue(), this.tauFill.getValue());
                        AquaShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                        break;
                    }
                    case Smoke: {
                        SmokeShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        SmokeShader.INSTANCE.stopDraw(Color.WHITE, 1.0f, 1.0f, this.duplicateFill.getValue(), color, color3, color5, this.NUM_OCTAVESFill.getValue());
                        SmokeShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                        break;
                    }
                    case RainbowCube: {
                        RainbowCubeShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        RainbowCubeShader.INSTANCE.stopDraw(Color.WHITE, 1.0f, 1.0f, this.duplicateFill.getValue(), color, this.WaveLenghtFIll.getValue(), this.RSTARTFill.getValue(), this.GSTARTFill.getValue(), this.BSTARTFIll.getValue());
                        RainbowCubeShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                        break;
                    }
                    case Gradient: {
                        GradientShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        GradientShader.INSTANCE.stopDraw(color2, 1.0f, 1.0f, this.duplicateFill.getValue(), this.moreGradientFill.getValue(), this.creepyFill.getValue(), this.alphaFill.getValue(), this.NUM_OCTAVESFill.getValue());
                        GradientShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                        break;
                    }
                    case Fill: {
                        FillShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        FillShader.INSTANCE.stopDraw(color);
                        FillShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                        break;
                    }
                    case Circle: {
                        CircleShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        CircleShader.INSTANCE.stopDraw(this.duplicateFill.getValue(), color, this.PI.getValue(), this.rad.getValue());
                        CircleShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                        break;
                    }
                    case Phobos: {
                        PhobosShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        PhobosShader.INSTANCE.stopDraw(color, 1.0f, 1.0f, this.duplicateFill.getValue(), this.MaxIterFill.getValue(), this.tauFill.getValue());
                        PhobosShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                    }
                }
                switch (this.glowESP.getValue()) {
                    case Color: {
                        GlowShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                        GlowShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue());
                        break;
                    }
                    case RainbowCube: {
                        RainbowCubeOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                        RainbowCubeOutlineShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue(), this.duplicateOutline.getValue(), color6, this.WaveLenghtOutline.getValue(), this.RSTARTOutline.getValue(), this.GSTARTOutline.getValue(), this.BSTARTOutline.getValue());
                        RainbowCubeOutlineShader.INSTANCE.update(this.speedOutline.getValue() / 1000.0f);
                        break;
                    }
                    case Gradient: {
                        GradientOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                        GradientOutlineShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue(), this.duplicateOutline.getValue(), this.moreGradientOutline.getValue(), this.creepyOutline.getValue(), this.alphaOutline.getValue(), this.NUM_OCTAVESOutline.getValue());
                        GradientOutlineShader.INSTANCE.update(this.speedOutline.getValue() / 1000.0f);
                        break;
                    }
                    case Astral: {
                        AstralOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                        AstralOutlineShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue(), this.duplicateOutline.getValue(), this.redOutline.getValue().floatValue(), this.greenOutline.getValue(), this.blueOutline.getValue(), this.alphaOutline.getValue(), this.iterationsOutline.getValue(), this.formuparam2Outline.getValue(), this.zoomOutline.getValue(), this.volumStepsOutline.getValue(), this.stepSizeOutline.getValue(), this.titleOutline.getValue(), this.distfadingOutline.getValue(), this.saturationOutline.getValue(), 0.0f, this.fadeOutline.getValue() ? 1 : 0);
                        AstralOutlineShader.INSTANCE.update(this.speedOutline.getValue() / 1000.0f);
                        break;
                    }
                    case Aqua: {
                        AquaOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                        AquaOutlineShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue(), this.duplicateOutline.getValue(), this.MaxIterOutline.getValue(), this.tauOutline.getValue());
                        AquaOutlineShader.INSTANCE.update(this.speedOutline.getValue() / 1000.0f);
                        break;
                    }
                    case Circle: {
                        CircleOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                        CircleOutlineShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue(), this.duplicateOutline.getValue(), this.PIOutline.getValue(), this.radOutline.getValue());
                        CircleOutlineShader.INSTANCE.update(this.speedOutline.getValue() / 1000.0f);
                        break;
                    }
                    case Smoke: {
                        SmokeOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                        SmokeOutlineShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue(), this.duplicateOutline.getValue(), color3, color4, this.NUM_OCTAVESOutline.getValue());
                        SmokeOutlineShader.INSTANCE.update(this.speedOutline.getValue() / 1000.0f);
                    }
                }
            } else {
                switch (this.glowESP.getValue()) {
                    case Color: {
                        GlowShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                        GlowShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue());
                        break;
                    }
                    case RainbowCube: {
                        RainbowCubeOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                        RainbowCubeOutlineShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue(), this.duplicateOutline.getValue(), color6, this.WaveLenghtOutline.getValue(), this.RSTARTOutline.getValue(), this.GSTARTOutline.getValue(), this.BSTARTOutline.getValue());
                        RainbowCubeOutlineShader.INSTANCE.update(this.speedOutline.getValue() / 1000.0f);
                        break;
                    }
                    case Gradient: {
                        GradientOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                        GradientOutlineShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue(), this.duplicateOutline.getValue(), this.moreGradientOutline.getValue(), this.creepyOutline.getValue(), this.alphaOutline.getValue(), this.NUM_OCTAVESOutline.getValue());
                        GradientOutlineShader.INSTANCE.update(this.speedOutline.getValue() / 1000.0f);
                        break;
                    }
                    case Astral: {
                        AstralOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                        AstralOutlineShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue(), this.duplicateOutline.getValue(), this.redOutline.getValue().floatValue(), this.greenOutline.getValue(), this.blueOutline.getValue(), this.alphaOutline.getValue(), this.iterationsOutline.getValue(), this.formuparam2Outline.getValue(), this.zoomOutline.getValue(), this.volumStepsOutline.getValue(), this.stepSizeOutline.getValue(), this.titleOutline.getValue(), this.distfadingOutline.getValue(), this.saturationOutline.getValue(), 0.0f, this.fadeOutline.getValue() ? 1 : 0);
                        AstralOutlineShader.INSTANCE.update(this.speedOutline.getValue() / 1000.0f);
                        break;
                    }
                    case Aqua: {
                        AquaOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                        AquaOutlineShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue(), this.duplicateOutline.getValue(), this.MaxIterOutline.getValue(), this.tauOutline.getValue());
                        AquaOutlineShader.INSTANCE.update(this.speedOutline.getValue() / 1000.0f);
                        break;
                    }
                    case Circle: {
                        CircleOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                        CircleOutlineShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue(), this.duplicateOutline.getValue(), this.PIOutline.getValue(), this.radOutline.getValue());
                        CircleOutlineShader.INSTANCE.update(this.speedOutline.getValue() / 1000.0f);
                        break;
                    }
                    case Smoke: {
                        SmokeOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                        SmokeOutlineShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue(), this.duplicateOutline.getValue(), color3, color4, this.NUM_OCTAVESOutline.getValue());
                        SmokeOutlineShader.INSTANCE.update(this.speedOutline.getValue() / 1000.0f);
                    }
                }
                switch (this.fillShader.getValue()) {
                    case Astral: {
                        FlowShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        FlowShader.INSTANCE.stopDraw(Color.WHITE, 1.0f, 1.0f, this.duplicateFill.getValue(), this.redFill.getValue().floatValue(), this.greenFill.getValue(), this.blueFill.getValue(), this.alphaFill.getValue(), this.iterationsFill.getValue(), this.formuparam2Fill.getValue(), this.zoomFill.getValue(), this.volumStepsFill.getValue(), this.stepSizeFill.getValue(), this.titleFill.getValue(), this.distfadingFill.getValue(), this.saturationFill.getValue(), 0.0f, this.fadeFill.getValue() ? 1 : 0);
                        FlowShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                        break;
                    }
                    case Aqua: {
                        AquaShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        AquaShader.INSTANCE.stopDraw(color, 1.0f, 1.0f, this.duplicateFill.getValue(), this.MaxIterFill.getValue(), this.tauFill.getValue());
                        AquaShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                        break;
                    }
                    case Smoke: {
                        SmokeShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        SmokeShader.INSTANCE.stopDraw(Color.WHITE, 1.0f, 1.0f, this.duplicateFill.getValue(), color, color3, color5, this.NUM_OCTAVESFill.getValue());
                        SmokeShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                        break;
                    }
                    case RainbowCube: {
                        RainbowCubeShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        RainbowCubeShader.INSTANCE.stopDraw(Color.WHITE, 1.0f, 1.0f, this.duplicateFill.getValue(), color, this.WaveLenghtFIll.getValue(), this.RSTARTFill.getValue(), this.GSTARTFill.getValue(), this.BSTARTFIll.getValue());
                        RainbowCubeShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                        break;
                    }
                    case Gradient: {
                        GradientShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        GradientShader.INSTANCE.stopDraw(color2, 1.0f, 1.0f, this.duplicateFill.getValue(), this.moreGradientFill.getValue(), this.creepyFill.getValue(), this.alphaFill.getValue(), this.NUM_OCTAVESFill.getValue());
                        GradientShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                        break;
                    }
                    case Fill: {
                        FillShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        FillShader.INSTANCE.stopDraw(color);
                        FillShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                        break;
                    }
                    case Circle: {
                        CircleShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        CircleShader.INSTANCE.stopDraw(this.duplicateFill.getValue(), color, this.PI.getValue(), this.rad.getValue());
                        CircleShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                        break;
                    }
                    case Phobos: {
                        PhobosShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                        this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                        PhobosShader.INSTANCE.stopDraw(color, 1.0f, 1.0f, this.duplicateFill.getValue(), this.MaxIterFill.getValue(), this.tauFill.getValue());
                        PhobosShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                    }
                }
            }
            this.notShader = true;
            GlStateManager.popMatrix();
        }
    }

    void renderPlayersFill(float f) {
        boolean bl = this.rangeCheck.getValue();
        double d = this.minRange.getValue() * this.minRange.getValue();
        double d2 = this.maxRange.getValue() * this.maxRange.getValue();
        AtomicInteger atomicInteger = new AtomicInteger();
        int n = this.maxEntities.getValue();
        try {
            Shaders.mc.world.loadedEntityList.stream().filter(entity -> {
                if (atomicInteger.getAndIncrement() > n) {
                    return false;
                }
                return entity instanceof EntityPlayer ? !(this.player.getValue() != Player1.Fill && this.player.getValue() != Player1.Both || entity == Shaders.mc.player && Shaders.mc.gameSettings.thirdPersonView == 0) : (entity instanceof EntityEnderPearl ? this.enderPearl.getValue() == EPl.Fill || this.enderPearl.getValue() == EPl.Both : (entity instanceof EntityExpBottle ? this.xpBottle.getValue() == XPBl.Fill || this.xpBottle.getValue() == XPBl.Both : (entity instanceof EntityXPOrb ? this.xpOrb.getValue() == XPl.Fill || this.xpOrb.getValue() == XPl.Both : (entity instanceof EntityItem ? this.items.getValue() == Itemsl.Fill || this.items.getValue() == Itemsl.Both : (entity instanceof EntityCreature ? this.mob.getValue() == Mob1.Fill || this.mob.getValue() == Mob1.Both : entity instanceof EntityEnderCrystal && (this.crystal.getValue() == Crystal1.Fill || this.crystal.getValue() == Crystal1.Both))))));
            }).filter(entity -> {
                if (!bl) {
                    return true;
                }
                double d3 = Shaders.mc.player.getDistanceSq(entity);
                return d3 > d && d3 < d2;
            }).forEach(entity -> mc.getRenderManager().renderEntityStatic(entity, f, true));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    void renderPlayersOutline(float f) {
        boolean bl = this.rangeCheck.getValue();
        double d = this.minRange.getValue() * this.minRange.getValue();
        double d2 = this.maxRange.getValue() * this.maxRange.getValue();
        AtomicInteger atomicInteger = new AtomicInteger();
        int n = this.maxEntities.getValue();
        Shaders.mc.world.addEntityToWorld(-1000, new EntityXPOrb(Shaders.mc.world, Shaders.mc.player.posX, Shaders.mc.player.posY + 1000000.0, Shaders.mc.player.posZ, 1));
        Shaders.mc.world.loadedEntityList.stream().filter(entity -> {
            if (atomicInteger.getAndIncrement() > n) {
                return false;
            }
            return entity instanceof EntityPlayer ? !(this.player.getValue() != Player1.Outline && this.player.getValue() != Player1.Both || entity == Shaders.mc.player && Shaders.mc.gameSettings.thirdPersonView == 0) : (entity instanceof EntityEnderPearl ? this.enderPearl.getValue() == EPl.Outline || this.enderPearl.getValue() == EPl.Both : (entity instanceof EntityExpBottle ? this.xpBottle.getValue() == XPBl.Outline || this.xpBottle.getValue() == XPBl.Both : (entity instanceof EntityXPOrb ? this.xpOrb.getValue() == XPl.Outline || this.xpOrb.getValue() == XPl.Both : (entity instanceof EntityItem ? this.items.getValue() == Itemsl.Outline || this.items.getValue() == Itemsl.Both : (entity instanceof EntityCreature ? this.mob.getValue() == Mob1.Outline || this.mob.getValue() == Mob1.Both : entity instanceof EntityEnderCrystal && (this.crystal.getValue() == Crystal1.Outline || this.crystal.getValue() == Crystal1.Both))))));
        }).filter(entity -> {
            if (!bl) {
                return true;
            }
            double d3 = Shaders.mc.player.getDistanceSq(entity);
            return d3 > d && d3 < d2 || entity.getEntityId() == -1000;
        }).forEach(entity -> mc.getRenderManager().renderEntityStatic(entity, f, true));
        Shaders.mc.world.removeEntityFromWorld(-1000);
    }

    public Shaders() {
        super("Shaders", "test", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (this.Fpreset.getValue()) {
            this.fillShader.setValue(fillShadermode.None);
            this.glowESP.setValue(glowESPmode.Gradient);
            this.player.setValue(Player1.Outline);
            this.crystal.setValue(Crystal1.Outline);
            this.duplicateOutline.setValue(2.0f);
            this.speedOutline.setValue(30.0f);
            this.quality.setValue(0.6f);
            this.radius.setValue(1.7f);
            this.creepyOutline.setValue(1.0f);
            this.moreGradientOutline.setValue(1.0f);
            this.Fpreset.setValue(false);
        }
        if (this.default1.getValue()) {
            this.fillShader.setValue(fillShadermode.None);
            this.glowESP.setValue(glowESPmode.None);
            this.rangeCheck.setValue(true);
            this.maxRange.setValue(35.0f);
            this.minRange.setValue(0.0f);
            this.crystal.setValue(Crystal1.None);
            this.player.setValue(Player1.None);
            this.mob.setValue(Mob1.None);
            this.items.setValue(Itemsl.None);
            this.fadeFill.setValue(false);
            this.fadeOutline.setValue(false);
            this.duplicateOutline.setValue(1.0f);
            this.duplicateFill.setValue(1.0f);
            this.speedOutline.setValue(10.0f);
            this.speedFill.setValue(10.0f);
            this.quality.setValue(1.0f);
            this.radius.setValue(1.0f);
            this.rad.setValue(0.75f);
            this.PI.setValue((float) Math.PI);
            this.saturationFill.setValue(0.4f);
            this.distfadingFill.setValue(0.56f);
            this.titleFill.setValue(0.45f);
            this.stepSizeFill.setValue(0.2f);
            this.volumStepsFill.setValue(10.0f);
            this.zoomFill.setValue(3.9f);
            this.formuparam2Fill.setValue(0.89f);
            this.saturationOutline.setValue(0.4f);
            this.maxEntities.setValue(100);
            this.iterationsFill.setValue(4);
            this.redFill.setValue(0);
            this.MaxIterFill.setValue(5);
            this.NUM_OCTAVESFill.setValue(5);
            this.BSTARTFIll.setValue(0);
            this.GSTARTFill.setValue(0);
            this.RSTARTFill.setValue(0);
            this.WaveLenghtFIll.setValue(555);
            this.volumStepsOutline.setValue(10);
            this.iterationsOutline.setValue(4);
            this.MaxIterOutline.setValue(5);
            this.NUM_OCTAVESOutline.setValue(5);
            this.BSTARTOutline.setValue(0);
            this.GSTARTOutline.setValue(0);
            this.RSTARTOutline.setValue(0);
            this.alphaValue.setValue(255);
            this.WaveLenghtOutline.setValue(555);
            this.redOutline.setValue(0);
            this.alphaFill.setValue(1.0f);
            this.blueFill.setValue(0.0f);
            this.greenFill.setValue(0.0f);
            this.tauFill.setValue((float) Math.PI * 2);
            this.creepyFill.setValue(1.0f);
            this.moreGradientFill.setValue(1.0f);
            this.distfadingOutline.setValue(0.56f);
            this.titleOutline.setValue(0.45f);
            this.stepSizeOutline.setValue(0.19f);
            this.zoomOutline.setValue(3.9f);
            this.formuparam2Outline.setValue(0.89f);
            this.alphaOutline.setValue(1.0f);
            this.blueOutline.setValue(0.0f);
            this.greenOutline.setValue(0.0f);
            this.tauOutline.setValue(0.0f);
            this.creepyOutline.setValue(1.0f);
            this.moreGradientOutline.setValue(1.0f);
            this.radOutline.setValue(0.75f);
            this.PIOutline.setValue((float) Math.PI);
            this.default1.setValue(false);
        }
    }

    void getFill() {
        switch (this.fillShader.getValue()) {
            case Astral: {
                FlowShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                break;
            }
            case Aqua: {
                AquaShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                break;
            }
            case Smoke: {
                SmokeShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                break;
            }
            case RainbowCube: {
                RainbowCubeShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                break;
            }
            case Gradient: {
                GradientShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                break;
            }
            case Fill: {
                FillShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                break;
            }
            case Circle: {
                CircleShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
                break;
            }
            case Phobos: {
                PhobosShader.INSTANCE.update(this.speedFill.getValue() / 1000.0f);
            }
        }
    }

    public static enum glowESPmode {
        None,
        Color,
        Astral,
        RainbowCube,
        Gradient,
        Circle,
        Smoke,
        Aqua

    }

    public static enum EPl {
        None,
        Fill,
        Outline,
        Both

    }

    public static enum Itemsl {
        None,
        Fill,
        Outline,
        Both

    }

    public static enum Mob1 {
        None,
        Fill,
        Outline,
        Both

    }

    public static enum XPBl {
        None,
        Fill,
        Outline,
        Both

    }

    public static enum XPl {
        None,
        Fill,
        Outline,
        Both

    }

    public static enum Crystal1 {
        None,
        Fill,
        Outline,
        Both

    }

    public static enum Player1 {
        None,
        Fill,
        Outline,
        Both

    }

    public static enum fillShadermode {
        Astral,
        Aqua,
        Smoke,
        RainbowCube,
        Gradient,
        Fill,
        Circle,
        Phobos,
        None

    }
}


package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.asm.accessors.IEntityRenderer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public final class ESP2D
extends Module {
    public static final List collectedEntities = new ArrayList();
    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)\u00a7[0-9A-FK-OR]");
    private static final int[] DISPLAY_LISTS_2D = new int[4];
    private static final Frustum frustrum = new Frustum();
    public static ESP2D INSTANCE = new ESP2D();
    public final Setting<Boolean> bbtt = this.add(new Setting<>("2B2T Mode", true));
    public final Setting<Boolean> outline = this.add(new Setting<>("Outline", true).setParent());
    public final Setting<Mode> boxMode = this.add(new Setting<>("Mode", Mode.Corners, v -> this.outline.isOpen()));
    public final Setting<Mode5> colorModeValue = this.add(new Setting<>("ColorMode", Mode5.Custom, v -> this.outline.isOpen()));
    public final Setting<Color> color = this.add(new Setting<>("Color", new Color(200, 200, 200), v -> this.colorModeValue.getValue() == Mode5.Custom && this.outline.isOpen()));
    private final Setting<Float> saturationValue = this.add(new Setting<>("Saturation", 1.0f, 0.0f, 1.0f, v -> this.colorModeValue.getValue() != Mode5.Custom && this.outline.isOpen()));
    private final Setting<Float> brightnessValue = this.add(new Setting<>("Brightness", 1.0f, 0.0f, 1.0f, v -> this.colorModeValue.getValue() != Mode5.Custom && this.outline.isOpen()));
    private final Setting<Integer> mixerSecondsValue = this.add(new Setting<>("Seconds", 2, 1, 10, v -> this.colorModeValue.getValue() != Mode5.Custom && this.outline.isOpen()));
    public final Setting<Boolean> outlineFont = this.add(new Setting<>("OutlineFont", true));
    public final Setting<Boolean> healthBar = this.add(new Setting<>("Health-bar", true).setParent());
    public final Setting<Mode2> hpBarMode = this.add(new Setting<>("HBar-Mode", Mode2.Dot, v -> this.healthBar.isOpen()));
    public final Setting<Boolean> healthNumber = this.add(new Setting<>("Health-Number", true, v -> this.healthBar.isOpen()).setParent());
    public final Setting<Mode4> hpMode = this.add(new Setting<>("HP-Mode", Mode4.Health, v -> this.healthNumber.isOpen() && this.healthBar.isOpen()));
    public final Setting<Boolean> hoverValue = this.add(new Setting<>("Details-HoverOnly", false));
    public final Setting<Boolean> itemTagsValue = this.add(new Setting<>("ItemTags", true).setParent());
    public final Setting<Boolean> itemValue = this.add(new Setting<>("Item", true, v -> this.itemTagsValue.isOpen()));
    public final Setting<Boolean> tagsValue = this.add(new Setting<>("Tags", true));
    public final Setting<Boolean> tagsBGValue = this.add(new Setting<>("Tags-Background", true, v -> this.tagsValue.getValue() || this.itemTagsValue.getValue()));
    public final Setting<Boolean> clearNameValue = this.add(new Setting<>("Use-Clear-Name", false, v -> this.tagsValue.isOpen()));
    public final Setting<Boolean> armorBar = this.add(new Setting<>("ArmorBar", true));
    public final Setting<Boolean> armorItems = this.add(new Setting<>("ArmorItems", true).setParent());
    public final Setting<Boolean> armorDur = this.add(new Setting<>("ArmorDur", true, v -> this.armorItems.isOpen()));
    public final Setting<Boolean> friendColor = this.add(new Setting<>("FriendColor", true));
    public final Setting<Boolean> localPlayer = this.add(new Setting<>("Local-Player", true));
    public final Setting<Boolean> mobs = this.add(new Setting<>("Mobs", false));
    public final Setting<Boolean> animals = this.add(new Setting<>("Animasl", false));
    public final Setting<Boolean> droppedItems = this.add(new Setting<>("Dropped-Items", false));
    private final Setting<Float> fontScaleValue = this.add(new Setting<>("Font-Scale", 0.5f, 0.0f, 1.0f));
    private final IntBuffer viewport;
    private final FloatBuffer modelview;
    private final FloatBuffer projection;
    private final FloatBuffer vector;
    private final int backgroundColor;
    private final int black;
    private final DecimalFormat dFormat = new DecimalFormat("0.0");

    public ESP2D() {
        super("ESP2D", "fixed", Category.RENDER);
        this.viewport = GLAllocation.createDirectIntBuffer(16);
        this.modelview = GLAllocation.createDirectFloatBuffer(16);
        this.projection = GLAllocation.createDirectFloatBuffer(16);
        this.vector = GLAllocation.createDirectFloatBuffer(4);
        this.backgroundColor = new Color(0, 0, 0, 120).getRGB();
        this.black = Color.BLACK.getRGB();
        INSTANCE = this;
    }

    public static Color getHealthColor(float health, float maxHealth) {
        float[] fractions = new float[]{0.0f, 0.5f, 1.0f};
        Color[] colors = new Color[]{new Color(108, 0, 0), new Color(255, 51, 0), Color.GREEN};
        float progress = health / maxHealth;
        return ESP2D.blendColors(fractions, colors, progress).brighter();
    }

    public static Color blendColors(float[] fractions, Color[] colors, float progress) {
        if (fractions.length == colors.length) {
            int[] indices = ESP2D.getFractionIndices(fractions, progress);
            float[] range = new float[]{fractions[indices[0]], fractions[indices[1]]};
            Color[] colorRange = new Color[]{colors[indices[0]], colors[indices[1]]};
            float max = range[1] - range[0];
            float value = progress - range[0];
            float weight = value / max;
            return ESP2D.blend(colorRange[0], colorRange[1], 1.0f - weight);
        }
        throw new IllegalArgumentException("Fractions and colours must have equal number of elements");
    }

    public static Color blend(Color color1, Color color2, double ratio) {
        float r = (float)ratio;
        float ir = 1.0f - r;
        float[] rgb1 = color1.getColorComponents(new float[3]);
        float[] rgb2 = color2.getColorComponents(new float[3]);
        float red = rgb1[0] * r + rgb2[0] * ir;
        float green = rgb1[1] * r + rgb2[1] * ir;
        float blue = rgb1[2] * r + rgb2[2] * ir;
        if (red < 0.0f) {
            red = 0.0f;
        } else if (red > 255.0f) {
            red = 255.0f;
        }
        if (green < 0.0f) {
            green = 0.0f;
        } else if (green > 255.0f) {
            green = 255.0f;
        }
        if (blue < 0.0f) {
            blue = 0.0f;
        } else if (blue > 255.0f) {
            blue = 255.0f;
        }
        Color color3 = null;
        try {
            color3 = new Color(red, green, blue);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        return color3;
    }

    public static int[] getFractionIndices(float[] fractions, float progress) {
        int startPoint;
        int[] range = new int[2];
        for (startPoint = 0; startPoint < fractions.length && fractions[startPoint] <= progress; ++startPoint) {
        }
        if (startPoint >= fractions.length) {
            startPoint = fractions.length - 1;
        }
        range[0] = startPoint - 1;
        range[1] = startPoint;
        return range;
    }

    public static String stripColor(String input) {
        return COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static Color fade(Color color, int index, int count) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs(((float)(System.currentTimeMillis() % 2000L) / 1000.0f + (float)index / (float)count * 2.0f) % 2.0f - 1.0f);
        brightness = 0.5f + 0.5f * brightness;
        hsb[2] = brightness % 2.0f;
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    public static Color slowlyRainbow(long time, int count, float qd, float sq) {
        Color color = new Color(Color.HSBtoRGB(((float)time + (float)count * -3000000.0f) / 2.0f / 1.0E9f, qd, sq));
        return new Color((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
    }

    public static void setColor(Color color) {
        float alpha = (float)(color.getRGB() >> 24 & 0xFF) / 255.0f;
        float red = (float)(color.getRGB() >> 16 & 0xFF) / 255.0f;
        float green = (float)(color.getRGB() >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color.getRGB() & 0xFF) / 255.0f;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static int getRainbowOpaque(int seconds, float saturation, float brightness, int index) {
        float hue = (float)((System.currentTimeMillis() + (long)index) % (long)(seconds * 1000L)) / (float)(seconds * 1000);
        return Color.HSBtoRGB(hue, saturation, brightness);
    }

    public static double interpolate(double current, double old, double scale) {
        return old + (current - old) * scale;
    }

    public static boolean isInViewFrustrum(Entity entity) {
        return ESP2D.isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }

    private static boolean isInViewFrustrum(AxisAlignedBB bb) {
        Entity current = mc.getRenderViewEntity();
        frustrum.setPosition(current.posX, current.posY, current.posZ);
        return frustrum.isBoundingBoxInFrustum(bb);
    }

    public static void quickDrawRect(float x, float y, float x2, float y2) {
        GL11.glBegin(7);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
    }

    public static void drawRect(double x, double y, double x2, double y2, int color) {
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        ESP2D.glColor(color);
        GL11.glBegin(7);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }

    public static void newDrawRect(double left, double top, double right, double bottom, int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }
        float f3 = (float)(color >> 24 & 0xFF) / 255.0f;
        float f = (float)(color >> 16 & 0xFF) / 255.0f;
        float f1 = (float)(color >> 8 & 0xFF) / 255.0f;
        float f2 = (float)(color & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0).endVertex();
        worldrenderer.pos(right, bottom, 0.0).endVertex();
        worldrenderer.pos(right, top, 0.0).endVertex();
        worldrenderer.pos(left, top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void color(Color color) {
        if (color == null) {
            color = Color.white;
        }
        ESP2D.color((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
    }

    public static void color(double red, double green, double blue, double alpha) {
        GL11.glColor4d(red, green, blue, alpha);
    }

    public static void glColor(int hex) {
        float alpha = (float)(hex >> 24 & 0xFF) / 255.0f;
        float red = (float)(hex >> 16 & 0xFF) / 255.0f;
        float green = (float)(hex >> 8 & 0xFF) / 255.0f;
        float blue = (float)(hex & 0xFF) / 255.0f;
        GlStateManager.color(red, green, blue, alpha);
    }

    public static void render(int mode2, Runnable render) {
        GL11.glBegin(mode2);
        render.run();
        GL11.glEnd();
    }

    public Color getColor(Entity entity) {
        if (entity instanceof EntityPlayer && this.friendColor.getValue() && Managers.FRIENDS.isFriend((EntityPlayer) entity)) {
            return Color.cyan;
        }
        switch (this.colorModeValue.getValue()) {
            case Custom: {
                return this.color.getValue();
            }
            case AnotherRainbow: {
                return new Color(ESP2D.getRainbowOpaque(this.mixerSecondsValue.getValue(), this.saturationValue.getValue(), this.brightnessValue.getValue(), 0));
            }
            case Slowly: {
                return ESP2D.slowlyRainbow(System.nanoTime(), 0, this.saturationValue.getValue(), this.brightnessValue.getValue());
            }
        }
        return ESP2D.fade(this.color.getValue(), 0, 100);
    }

    @Override
    public void onDisable() {
        collectedEntities.clear();
    }

    @Override
    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        GL11.glPushMatrix();
        this.collectEntities();
        float partialTicks = event.partialTicks;
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int scaleFactor = scaledResolution.getScaleFactor();
        double scaling = (double)scaleFactor / Math.pow(scaleFactor, 2.0);
        GL11.glScaled(scaling, scaling, scaling);
        int black = this.black;
        RenderManager renderMng = mc.getRenderManager();
        EntityRenderer entityRenderer = ESP2D.mc.entityRenderer;
        boolean outline = this.outline.getValue();
        boolean health = this.healthBar.getValue();
        int collectedEntitiesSize = collectedEntities.size();
        for (Object collectedEntity : collectedEntities) {
            int m;
            boolean living;
            Entity entity = (Entity) collectedEntity;
            int color = this.getColor(entity).getRGB();
            if (!ESP2D.isInViewFrustrum(entity)) continue;
            double x = ESP2D.interpolate(entity.posX, entity.lastTickPosX, partialTicks);
            double y = ESP2D.interpolate(entity.posY, entity.lastTickPosY, partialTicks);
            double z = ESP2D.interpolate(entity.posZ, entity.lastTickPosZ, partialTicks);
            double width = (double) entity.width / 1.5;
            double height = (double) entity.height + (entity.isSneaking() ? -0.3 : 0.2);
            AxisAlignedBB aabb = new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width);
            List<Vector3d> vectors = Arrays.asList(new Vector3d(aabb.minX, aabb.minY, aabb.minZ), new Vector3d(aabb.minX, aabb.maxY, aabb.minZ), new Vector3d(aabb.maxX, aabb.minY, aabb.minZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.minZ), new Vector3d(aabb.minX, aabb.minY, aabb.maxZ), new Vector3d(aabb.minX, aabb.maxY, aabb.maxZ), new Vector3d(aabb.maxX, aabb.minY, aabb.maxZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ));
            ((IEntityRenderer) ESP2D.mc.entityRenderer).invokeSetupCameraTransform(partialTicks, 0);
            Vector4d position = null;
            for (Vector3d vector3d : vectors) {
                Vector3d o;
                Vector3d vector = o = vector3d;
                vector = this.project2D(scaleFactor, vector.x - renderMng.viewerPosX, vector.y - renderMng.viewerPosY, vector.z - renderMng.viewerPosZ);
                if (vector == null || !(vector.z >= 0.0) || !(vector.z < 1.0)) continue;
                if (position == null) {
                    position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
                }
                position.x = Math.min(vector.x, position.x);
                position.y = Math.min(vector.y, position.y);
                position.z = Math.max(vector.x, position.z);
                position.w = Math.max(vector.y, position.w);
            }
            if (position == null) continue;
            entityRenderer.setupOverlayRendering();
            double posX = position.x;
            double posY = position.y;
            double endPosX = position.z;
            double endPosY = position.w;
            if (outline) {
                if (this.boxMode.getValue() == Mode.Box) {
                    ESP2D.newDrawRect(posX - 1.0, posY, posX + 0.5, endPosY + 0.5, black);
                    ESP2D.newDrawRect(posX - 1.0, posY - 0.5, endPosX + 0.5, posY + 0.5 + 0.5, black);
                    ESP2D.newDrawRect(endPosX - 0.5 - 0.5, posY, endPosX + 0.5, endPosY + 0.5, black);
                    ESP2D.newDrawRect(posX - 1.0, endPosY - 0.5 - 0.5, endPosX + 0.5, endPosY + 0.5, black);
                    ESP2D.newDrawRect(posX - 0.5, posY, posX + 0.5 - 0.5, endPosY, color);
                    ESP2D.newDrawRect(posX, endPosY - 0.5, endPosX, endPosY, color);
                    ESP2D.newDrawRect(posX - 0.5, posY, endPosX, posY + 0.5, color);
                    ESP2D.newDrawRect(endPosX - 0.5, posY, endPosX, endPosY, color);
                } else {
                    ESP2D.newDrawRect(posX + 0.5, posY, posX - 1.0, posY + (endPosY - posY) / 4.0 + 0.5, black);
                    ESP2D.newDrawRect(posX - 1.0, endPosY, posX + 0.5, endPosY - (endPosY - posY) / 4.0 - 0.5, black);
                    ESP2D.newDrawRect(posX - 1.0, posY - 0.5, posX + (endPosX - posX) / 3.0 + 0.5, posY + 1.0, black);
                    ESP2D.newDrawRect(endPosX - (endPosX - posX) / 3.0 - 0.5, posY - 0.5, endPosX, posY + 1.0, black);
                    ESP2D.newDrawRect(endPosX - 1.0, posY, endPosX + 0.5, posY + (endPosY - posY) / 4.0 + 0.5, black);
                    ESP2D.newDrawRect(endPosX - 1.0, endPosY, endPosX + 0.5, endPosY - (endPosY - posY) / 4.0 - 0.5, black);
                    ESP2D.newDrawRect(posX - 1.0, endPosY - 1.0, posX + (endPosX - posX) / 3.0 + 0.5, endPosY + 0.5, black);
                    ESP2D.newDrawRect(endPosX - (endPosX - posX) / 3.0 - 0.5, endPosY - 1.0, endPosX + 0.5, endPosY + 0.5, black);
                    ESP2D.newDrawRect(posX, posY, posX - 0.5, posY + (endPosY - posY) / 4.0, color);
                    ESP2D.newDrawRect(posX, endPosY, posX - 0.5, endPosY - (endPosY - posY) / 4.0, color);
                    ESP2D.newDrawRect(posX - 0.5, posY, posX + (endPosX - posX) / 3.0, posY + 0.5, color);
                    ESP2D.newDrawRect(endPosX - (endPosX - posX) / 3.0, posY, endPosX, posY + 0.5, color);
                    ESP2D.newDrawRect(endPosX - 0.5, posY, endPosX, posY + (endPosY - posY) / 4.0, color);
                    ESP2D.newDrawRect(endPosX - 0.5, endPosY, endPosX, endPosY - (endPosY - posY) / 4.0, color);
                    ESP2D.newDrawRect(posX, endPosY - 0.5, posX + (endPosX - posX) / 3.0, endPosY, color);
                    ESP2D.newDrawRect(endPosX - (endPosX - posX) / 3.0, endPosY - 0.5, endPosX - 0.5, endPosY, color);
                }
            }
            if (!(living = entity instanceof EntityLivingBase)) continue;
            EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
            if (health) {
                float armorValue = entityLivingBase.getHealth();
                float itemDurability = entityLivingBase.getMaxHealth();
                if (this.bbtt.getValue() && entityLivingBase instanceof EntityPlayer) {
                    armorValue = entityLivingBase.getHealth() + entityLivingBase.getAbsorptionAmount();
                    itemDurability = entityLivingBase.getMaxHealth() + 16.0f;
                }
                if (armorValue > itemDurability) {
                    armorValue = itemDurability;
                }
                double durabilityWidth = armorValue / itemDurability;
                double textWidth = (endPosY - posY) * durabilityWidth;
                String healthDisplay = this.dFormat.format(entityLivingBase.getHealth() + entityLivingBase.getAbsorptionAmount()) + " \u00a7c\u2764";
                String healthPercent = (int) (entityLivingBase.getHealth() / itemDurability * 100.0f) + "%";
                if (this.healthNumber.getValue() && (!this.hoverValue.getValue() || entity == ESP2D.mc.player || this.isHovering(posX, endPosX, posY, endPosY, scaledResolution))) {
                    this.drawScaledString(this.hpMode.getValue() == Mode4.Health ? healthDisplay : healthPercent, posX - 4.0 - (double) ((float) Managers.TEXT.getStringWidth(this.hpMode.getValue() == Mode4.Health ? healthDisplay : healthPercent) * this.fontScaleValue.getValue()), endPosY - textWidth - (double) ((float) ESP2D.mc.fontRenderer.FONT_HEIGHT / 2.0f * this.fontScaleValue.getValue()), this.fontScaleValue.getValue());
                }
                ESP2D.newDrawRect(posX - 3.5, posY - 0.5, posX - 1.5, endPosY + 0.5, this.backgroundColor);
                if (armorValue > 0.0f) {
                    int healthColor = ESP2D.getHealthColor(armorValue, itemDurability).getRGB();
                    double deltaY = endPosY - posY;
                    if (this.hpBarMode.getValue() == Mode2.Dot && deltaY >= 60.0) {
                        for (double k = 0.0; k < 10.0; k += 1.0) {
                            double reratio = MathHelper.clamp((double) armorValue - k * ((double) itemDurability / 10.0), 0.0, (double) itemDurability / 10.0) / ((double) itemDurability / 10.0);
                            double hei = (deltaY / 10.0 - 0.5) * reratio;
                            ESP2D.newDrawRect(posX - 3.0, endPosY - (deltaY + 0.5) / 10.0 * k, posX - 2.0, endPosY - (deltaY + 0.5) / 10.0 * k - hei, healthColor);
                        }
                    } else {
                        ESP2D.newDrawRect(posX - 3.0, endPosY, posX - 2.0, endPosY - textWidth, healthColor);
                    }
                }
            }
            if (this.tagsValue.getValue()) {
                String entName;
                String string = entName = this.clearNameValue.getValue() ? entityLivingBase.getName() : entityLivingBase.getDisplayName().getFormattedText();
                if (this.friendColor.getValue() && Managers.FRIENDS.isFriend(entityLivingBase.getName())) {
                    entName = "\u00a7b" + entName;
                }
                if (this.tagsBGValue.getValue()) {
                    ESP2D.newDrawRect(posX + (endPosX - posX) / 2.0 - (double) (((float) ESP2D.mc.fontRenderer.getStringWidth(entName) / 2.0f + 2.0f) * this.fontScaleValue.getValue()), posY - 1.0 - (double) (((float) ESP2D.mc.fontRenderer.FONT_HEIGHT + 2.0f) * this.fontScaleValue.getValue()), posX + (endPosX - posX) / 2.0 + (double) (((float) ESP2D.mc.fontRenderer.getStringWidth(entName) / 2.0f + 2.0f) * this.fontScaleValue.getValue()), posY - 1.0 + (double) (2.0f * this.fontScaleValue.getValue()), -1610612736);
                }
                this.drawScaledCenteredString(entName, posX + (endPosX - posX) / 2.0, posY - 1.0 - (double) ((float) ESP2D.mc.fontRenderer.FONT_HEIGHT * this.fontScaleValue.getValue()), this.fontScaleValue.getValue());
            }
            if (this.armorBar.getValue() && entity instanceof EntityPlayer) {
                double constHeight = (endPosY - posY) / 4.0;
                for (m = 4; m > 0; --m) {
                    ItemStack armorStack = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
                    if (m == 3) {
                        armorStack = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                    }
                    if (m == 2) {
                        armorStack = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
                    }
                    if (m == 1) {
                        armorStack = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.FEET);
                    }
                    double theHeight = constHeight + 0.25;
                    ESP2D.newDrawRect(endPosX + 1.5, endPosY + 0.5 - theHeight * (double) m, endPosX + 3.5, endPosY + 0.5 - theHeight * (double) (m - 1), new Color(0, 0, 0, 120).getRGB());
                    ESP2D.newDrawRect(endPosX + 2.0, endPosY + 0.5 - theHeight * (double) (m - 1) - 0.25, endPosX + 3.0, endPosY + 0.5 - theHeight * (double) (m - 1) - 0.25 - (constHeight - 0.25) * MathHelper.clamp((double) InventoryUtil.getItemDurability(armorStack) / (double) armorStack.getMaxDamage(), 0.0, 1.0), new Color(0, 255, 255).getRGB());
                }
            }
            if (this.armorItems.getValue() && (!this.hoverValue.getValue() || entity == ESP2D.mc.player || this.isHovering(posX, endPosX, posY, endPosY, scaledResolution))) {
                double yDist = (endPosY - posY) / 4.0;
                for (m = 4; m > 0; --m) {
                    ItemStack armorStack = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
                    if (m == 3) {
                        armorStack = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                    }
                    if (m == 2) {
                        armorStack = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
                    }
                    if (m == 1) {
                        armorStack = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.FEET);
                    }
                    this.renderItemStack(armorStack, endPosX + (this.armorBar.getValue() ? 4.0 : 2.0), posY + yDist * (double) (4 - m) + yDist / 2.0 - 5.0);
                    if (!this.armorDur.getValue()) continue;
                    this.drawScaledCenteredString(String.valueOf(InventoryUtil.getItemDurability(armorStack)), endPosX + (this.armorBar.getValue() ? 4.0 : 2.0) + 4.5, posY + yDist * (double) (4 - m) + yDist / 2.0 + 4.0, this.fontScaleValue.getValue(), -1);
                }
            }
            if (!this.itemTagsValue.getValue()) continue;
            if (!this.itemValue.getValue()) {
                String itemName = entityLivingBase.getHeldItem(EnumHand.MAIN_HAND).getDisplayName();
                if (this.tagsBGValue.getValue()) {
                    ESP2D.newDrawRect(posX + (endPosX - posX) / 2.0 - (double) (((float) ESP2D.mc.fontRenderer.getStringWidth(itemName) / 2.0f + 2.0f) * this.fontScaleValue.getValue()), endPosY + 1.0 - (double) (2.0f * this.fontScaleValue.getValue()), posX + (endPosX - posX) / 2.0 + (double) (((float) ESP2D.mc.fontRenderer.getStringWidth(itemName) / 2.0f + 2.0f) * this.fontScaleValue.getValue()), endPosY + 1.0 + (double) (((float) ESP2D.mc.fontRenderer.FONT_HEIGHT + 2.0f) * this.fontScaleValue.getValue()), -1610612736);
                }
                this.drawScaledCenteredString(itemName, posX + (endPosX - posX) / 2.0, endPosY + 1.0, this.fontScaleValue.getValue(), -1);
                itemName = entityLivingBase.getHeldItem(EnumHand.OFF_HAND).getDisplayName();
                float OffhandY = 7.5f;
                if (this.tagsBGValue.getValue()) {
                    ESP2D.newDrawRect(posX + (endPosX - posX) / 2.0 - (double) (((float) ESP2D.mc.fontRenderer.getStringWidth(itemName) / 2.0f + 2.0f) * this.fontScaleValue.getValue()), endPosY + (double) OffhandY - (double) (2.0f * this.fontScaleValue.getValue()), posX + (endPosX - posX) / 2.0 + (double) (((float) ESP2D.mc.fontRenderer.getStringWidth(itemName) / 2.0f + 2.0f) * this.fontScaleValue.getValue()), endPosY + (double) OffhandY + (double) (((float) ESP2D.mc.fontRenderer.FONT_HEIGHT + 2.0f) * this.fontScaleValue.getValue()), -1610612736);
                }
                this.drawScaledCenteredString(itemName, posX + (endPosX - posX) / 2.0, endPosY + (double) OffhandY, this.fontScaleValue.getValue(), -1);
                continue;
            }
            this.renderItemStack(entityLivingBase.getHeldItemMainhand(), posX, endPosY);
            this.renderItemStack(entityLivingBase.getHeldItemOffhand(), posX + (double) (entityLivingBase.getHeldItemMainhand().getItem() == Items.AIR ? 0.0f : 8.0f), endPosY);
        }
        GL11.glPopMatrix();
        GlStateManager.enableBlend();
        GlStateManager.resetColor();
        entityRenderer.setupOverlayRendering();
    }

    private void drawScaledCenteredString(String text2, double x, double y, double scale, int color) {
        this.drawScaledString(text2, x - (double)((float)ESP2D.mc.fontRenderer.getStringWidth(text2) / 2.0f) * scale, y, scale, color);
    }

    private void drawScaledString(String text2, double x, double y, double scale, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, x);
        GlStateManager.scale(scale, scale, scale);
        if (this.outlineFont.getValue()) {
            this.drawOutlineStringWithoutGL(text2, 0.0f, 0.0f, color, ESP2D.mc.fontRenderer);
        } else {
            ESP2D.mc.fontRenderer.drawStringWithShadow(text2, 0.0f, 0.0f, color);
        }
        GlStateManager.popMatrix();
    }

    private void renderItemStack(ItemStack stack, double x, double y) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, x);
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
        mc.getRenderItem().renderItemOverlays(ESP2D.mc.fontRenderer, stack, 0, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private boolean isHovering(double minX, double maxX, double minY, double maxY, ScaledResolution sc) {
        return (double)sc.getScaledWidth() / 2.0 >= minX && (double)sc.getScaledWidth() / 2.0 < maxX && (double)sc.getScaledHeight() / 2.0 >= minY && (double)sc.getScaledHeight() / 2.0 < maxY;
    }

    public void drawOutlineStringWithoutGL(String s, float x, float y, int color, FontRenderer fontRenderer) {
        fontRenderer.drawString(ESP2D.stripColor(s), (int)(x * 2.0f - 1.0f), (int)(y * 2.0f), Color.BLACK.getRGB());
        fontRenderer.drawString(ESP2D.stripColor(s), (int)(x * 2.0f + 1.0f), (int)(y * 2.0f), Color.BLACK.getRGB());
        fontRenderer.drawString(ESP2D.stripColor(s), (int)(x * 2.0f), (int)(y * 2.0f - 1.0f), Color.BLACK.getRGB());
        fontRenderer.drawString(ESP2D.stripColor(s), (int)(x * 2.0f), (int)(y * 2.0f + 1.0f), Color.BLACK.getRGB());
        fontRenderer.drawString(s, (int)(x * 2.0f), (int)(y * 2.0f), color);
    }

    private void drawScaledString(String text2, double x, double y, double scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, x);
        GlStateManager.scale(scale, scale, scale);
        if (this.outlineFont.getValue()) {
            this.drawOutlineStringWithoutGL(text2, 0.0f, 0.0f, -1, ESP2D.mc.fontRenderer);
        } else {
            ESP2D.mc.fontRenderer.drawStringWithShadow(text2, 0.0f, 0.0f, -1);
        }
        GlStateManager.popMatrix();
    }

    private void drawScaledCenteredString(String text2, double x, double y, double scale) {
        this.drawScaledString(text2, x - (double)((float)ESP2D.mc.fontRenderer.getStringWidth(text2) / 2.0f) * scale, y, scale);
    }

    private void collectEntities() {
        collectedEntities.clear();
        List playerEntities = ESP2D.mc.world.loadedEntityList;
        int playerEntitiesSize = playerEntities.size();
        for (Object playerEntity : playerEntities) {
            Entity entity = (Entity) playerEntity;
            if (!this.isSelected(entity, false) && (!this.localPlayer.getValue() || !(entity instanceof EntityPlayerSP) || ESP2D.mc.gameSettings.thirdPersonView == 0) && (!this.droppedItems.getValue() || !(entity instanceof EntityItem)))
                continue;
            collectedEntities.add(entity);
        }
    }

    public boolean isSelected(Entity entity, boolean canAttackCheck) {
        if (entity instanceof EntityLivingBase && entity.isEntityAlive() && entity != ESP2D.mc.player) {
            if (entity instanceof EntityPlayer) {
                if (canAttackCheck) {
                    if (Managers.FRIENDS.isFriend((EntityPlayer)entity)) {
                        return false;
                    }
                    if (((EntityPlayer)entity).isSpectator()) {
                        return false;
                    }
                    return !((EntityPlayer)entity).isPlayerSleeping();
                }
                return true;
            }
            return this.isMob(entity) && this.mobs.getValue() || this.isAnimal(entity) && this.animals.getValue();
        }
        return false;
    }

    public boolean isAnimal(Entity entity) {
        return entity instanceof EntityAnimal || entity instanceof EntitySquid || entity instanceof EntityGolem || entity instanceof EntityVillager || entity instanceof EntityBat;
    }

    public boolean isMob(Entity entity) {
        return entity instanceof EntityMob || entity instanceof EntitySlime || entity instanceof EntityGhast || entity instanceof EntityDragon;
    }

    private Vector3d project2D(int scaleFactor, double x, double y, double z) {
        GL11.glGetFloat(2982, this.modelview);
        GL11.glGetFloat(2983, this.projection);
        GL11.glGetInteger(2978, this.viewport);
        return GLU.gluProject((float)x, (float)y, (float)z, this.modelview, this.projection, this.viewport, this.vector) ? new Vector3d(this.vector.get(0) / (float)scaleFactor, ((float)Display.getHeight() - this.vector.get(1)) / (float)scaleFactor, this.vector.get(2)) : null;
    }

    static {
        for (int i = 0; i < DISPLAY_LISTS_2D.length; ++i) {
            ESP2D.DISPLAY_LISTS_2D[i] = GL11.glGenLists(1);
        }
        GL11.glNewList(DISPLAY_LISTS_2D[0], 4864);
        ESP2D.quickDrawRect(-7.0f, 2.0f, -4.0f, 3.0f);
        ESP2D.quickDrawRect(4.0f, 2.0f, 7.0f, 3.0f);
        ESP2D.quickDrawRect(-7.0f, 0.5f, -6.0f, 3.0f);
        ESP2D.quickDrawRect(6.0f, 0.5f, 7.0f, 3.0f);
        GL11.glEndList();
        GL11.glNewList(DISPLAY_LISTS_2D[1], 4864);
        ESP2D.quickDrawRect(-7.0f, 3.0f, -4.0f, 3.3f);
        ESP2D.quickDrawRect(4.0f, 3.0f, 7.0f, 3.3f);
        ESP2D.quickDrawRect(-7.3f, 0.5f, -7.0f, 3.3f);
        ESP2D.quickDrawRect(7.0f, 0.5f, 7.3f, 3.3f);
        GL11.glEndList();
        GL11.glNewList(DISPLAY_LISTS_2D[2], 4864);
        ESP2D.quickDrawRect(4.0f, -20.0f, 7.0f, -19.0f);
        ESP2D.quickDrawRect(-7.0f, -20.0f, -4.0f, -19.0f);
        ESP2D.quickDrawRect(6.0f, -20.0f, 7.0f, -17.5f);
        ESP2D.quickDrawRect(-7.0f, -20.0f, -6.0f, -17.5f);
        GL11.glEndList();
        GL11.glNewList(DISPLAY_LISTS_2D[3], 4864);
        ESP2D.quickDrawRect(7.0f, -20.0f, 7.3f, -17.5f);
        ESP2D.quickDrawRect(-7.3f, -20.0f, -7.0f, -17.5f);
        ESP2D.quickDrawRect(4.0f, -20.3f, 7.3f, -20.0f);
        ESP2D.quickDrawRect(-7.3f, -20.3f, -4.0f, -20.0f);
        GL11.glEndList();
    }

    public static enum Mode5 {
        Custom,
        Slowly,
        AnotherRainbow

    }

    public static enum Mode4 {
        Health,
        Percent

    }

    public static enum Mode2 {
        Dot,
        Line

    }

    public static enum Mode {
        Box,
        Corners

    }
}


package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.Objects;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.DamageUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.TextUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

public class NameTags
extends Module {
    public static NameTags INSTANCE = new NameTags();
    public final Setting<Color> max = this.add(new Setting<>("Max", new Color(255, 255, 255)).injectBoolean(true).setParent());
    public final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255)));
    public final Setting<Color> outLine = this.add(new Setting<>("outLine", new Color(255, 255, 255)).injectBoolean(false).setParent());
    public final Setting<Color> friend = this.add(new Setting<>("Friend", new Color(155, 155, 255)).injectBoolean(true));
    public final Setting<Color> invisible = this.add(new Setting<>("Invisible", new Color(200, 200, 200)).injectBoolean(true));
    public final Setting<Color> sneak = this.add(new Setting<>("Sneaking", new Color(200, 200, 0)).injectBoolean(true));
    private final Setting<Boolean> rect = this.add(new Setting<>("Rectangle", true));
    private final Setting<Boolean> armor = this.add(new Setting<>("Armor", true).setParent());
    private final Setting<Boolean> reversed = this.add(new Setting<>("ArmorReversed", false, v -> this.armor.isOpen()));
    private final Setting<Boolean> health = this.add(new Setting<>("Health", true));
    private final Setting<Boolean> ping = this.add(new Setting<>("Ping", true));
    private final Setting<Boolean> gamemode = this.add(new Setting<>("Gamemode", true));
    private final Setting<Boolean> entityID = this.add(new Setting<>("EntityID", false));
    private final Setting<Boolean> heldStackName = this.add(new Setting<>("StackName", false));
    private final Setting<Float> size = this.add(new Setting<>("Size", 2.5f, 0.1f, 15.0f));
    private final Setting<Boolean> scale = this.add(new Setting<>("Scale", true).setParent());
    private final Setting<Boolean> smartScale = this.add(new Setting<>("SmartScale", true, v -> this.scale.isOpen()));
    private final Setting<Float> factor = this.add(new Setting<>("Factor", 0.3f, 0.1f, 1.0f, v -> this.scale.isOpen()));
    private final Setting<Boolean> noMaxText = this.add(new Setting<>("NoMaxText", true, v -> this.max.isOpen()));
    private final Setting<Float> outLineWidth = this.add(new Setting<>("Width", 1.3f, 0.0f, 5.0f, v -> this.outLine.isOpen()));

    public NameTags() {
        super("NameTags", "Renders info about the player on a NameTag", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (NameTags.fullNullCheck()) {
            return;
        }
        for (EntityPlayer player : NameTags.mc.world.playerEntities) {
            if (player == null || player.equals(NameTags.mc.player) || !player.isEntityAlive() || player.isInvisible() && !this.invisible.booleanValue) continue;
            double x = this.interpolate(player.lastTickPosX, player.posX, event.getPartialTicks()) - NameTags.mc.getRenderManager().renderPosX;
            double y = this.interpolate(player.lastTickPosY, player.posY, event.getPartialTicks()) - NameTags.mc.getRenderManager().renderPosY;
            double z = this.interpolate(player.lastTickPosZ, player.posZ, event.getPartialTicks()) - NameTags.mc.getRenderManager().renderPosZ;
            this.renderNameTag(player, x, y, z, event.getPartialTicks());
        }
    }

    private void renderNameTag(EntityPlayer player, double x, double y, double z, float delta) {
        double tempY = y;
        tempY += player.isSneaking() ? 0.5 : 0.7;
        Entity camera = mc.getRenderViewEntity();
        assert (camera != null);
        double originalPositionX = camera.posX;
        double originalPositionY = camera.posY;
        double originalPositionZ = camera.posZ;
        camera.posX = this.interpolate(camera.prevPosX, camera.posX, delta);
        camera.posY = this.interpolate(camera.prevPosY, camera.posY, delta);
        camera.posZ = this.interpolate(camera.prevPosZ, camera.posZ, delta);
        String displayTag = this.getDisplayTag(player);
        double distance = camera.getDistance(x + NameTags.mc.getRenderManager().viewerPosX, y + NameTags.mc.getRenderManager().viewerPosY, z + NameTags.mc.getRenderManager().viewerPosZ);
        int width = Managers.TEXT.getMCStringWidth(displayTag) / 2;
        double scale = (0.0018 + (double) this.size.getValue() * (distance * (double) this.factor.getValue())) / 1000.0;
        if (distance <= 6.0 && this.smartScale.getValue()) {
            scale = (0.0018 + (double)(this.size.getValue() + 2.0f) * (distance * (double) this.factor.getValue())) / 1000.0;
        }
        if (distance <= 4.0 && this.smartScale.getValue()) {
            scale = (0.0018 + (double)(this.size.getValue() + 4.0f) * (distance * (double) this.factor.getValue())) / 1000.0;
        }
        if (!this.scale.getValue()) {
            scale = (double) this.size.getValue() / 100.0;
        }
        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
        GlStateManager.disableLighting();
        GlStateManager.translate((float)x, (float)tempY + 1.4f, (float)z);
        GlStateManager.rotate(-NameTags.mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(NameTags.mc.getRenderManager().playerViewX, NameTags.mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.enableBlend();
        if (this.rect.getValue()) {
            this.drawRect(-width - 2, -(NameTags.mc.fontRenderer.FONT_HEIGHT + 1), (float)width + 2.0f, 1.5f, 0x55000000);
        } else if (!this.outLine.booleanValue) {
            this.drawRect(0.0f, 0.0f, 0.0f, 0.0f, 0x55000000);
        }
        if (this.outLine.booleanValue) {
            this.drawOutlineRect(-width - 2, -(NameTags.mc.fontRenderer.FONT_HEIGHT + 1), (float)width + 2.0f, 1.5f, this.getOutlineColor());
        }
        GlStateManager.disableBlend();
        ItemStack renderMainHand = player.getHeldItemMainhand().copy();
        if (renderMainHand.hasEffect() && (renderMainHand.getItem() instanceof ItemTool || renderMainHand.getItem() instanceof ItemArmor)) {
            renderMainHand.stackSize = 1;
        }
        if (this.heldStackName.getValue() && !renderMainHand.isEmpty && renderMainHand.getItem() != Items.AIR) {
            String stackName = renderMainHand.getDisplayName();
            int stackNameWidth = Managers.TEXT.getMCStringWidth(stackName) / 2;
            GL11.glPushMatrix();
            GL11.glScalef(0.75f, 0.75f, 0.0f);
            Managers.TEXT.drawMCString(stackName, -stackNameWidth, -(this.getBiggestArmorTag(player) + 20.0f), -1, true);
            GL11.glScalef(1.5f, 1.5f, 1.0f);
            GL11.glPopMatrix();
        }
        if (this.armor.getValue()) {
            GlStateManager.pushMatrix();
            int xOffset = -6;
            for (ItemStack armourStack : player.inventory.armorInventory) {
                if (armourStack == null) continue;
                xOffset -= 8;
            }
            xOffset -= 8;
            ItemStack renderOffhand = player.getHeldItemOffhand().copy();
            if (renderOffhand.hasEffect() && (renderOffhand.getItem() instanceof ItemTool || renderOffhand.getItem() instanceof ItemArmor)) {
                renderOffhand.stackSize = 1;
            }
            this.renderItemStack(renderOffhand, xOffset);
            xOffset += 16;
            if (this.reversed.getValue()) {
                for (int index = 0; index <= 3; ++index) {
                    ItemStack armourStack = player.inventory.armorInventory.get(index);
                    if (armourStack.getItem() == Items.AIR) continue;
                    armourStack.copy();
                    this.renderItemStack(armourStack, xOffset);
                    xOffset += 16;
                }
            } else {
                for (int index = 3; index >= 0; --index) {
                    ItemStack armourStack = player.inventory.armorInventory.get(index);
                    if (armourStack.getItem() == Items.AIR) continue;
                    armourStack.copy();
                    this.renderItemStack(armourStack, xOffset);
                    xOffset += 16;
                }
            }
            this.renderItemStack(renderMainHand, xOffset);
            GlStateManager.popMatrix();
        }
        Managers.TEXT.drawMCString(displayTag, -width, -(Managers.TEXT.getFontHeight() - 1), this.getDisplayColor(player), true);
        camera.posX = originalPositionX;
        camera.posY = originalPositionY;
        camera.posZ = originalPositionZ;
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.popMatrix();
    }

    private int getDisplayColor(EntityPlayer player) {
        int displaycolor = ColorUtil.toRGBA(this.color.getValue());
        if (Managers.FRIENDS.isFriend(player) && this.friend.booleanValue) {
            return ColorUtil.toRGBA(this.friend.getValue());
        }
        if (player.isInvisible() && this.invisible.booleanValue) {
            displaycolor = ColorUtil.toRGBA(this.invisible.getValue());
        } else if (player.isSneaking() && this.sneak.booleanValue) {
            displaycolor = ColorUtil.toRGBA(this.sneak.getValue());
        }
        return displaycolor;
    }

    private int getOutlineColor() {
        return ColorUtil.toRGBA(this.outLine.getValue());
    }

    private void renderItemStack(ItemStack stack, int x) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.clear(256);
        RenderHelper.enableStandardItemLighting();
        NameTags.mc.getRenderItem().zLevel = -150.0f;
        GlStateManager.disableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.disableCull();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, -26);
        mc.getRenderItem().renderItemOverlays(NameTags.mc.fontRenderer, stack, x, -26);
        NameTags.mc.getRenderItem().zLevel = 0.0f;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        GlStateManager.disableDepth();
        this.renderEnchantmentText(stack, x);
        GlStateManager.enableDepth();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        GlStateManager.popMatrix();
    }

    private void renderEnchantmentText(ItemStack stack, int x) {
        NBTTagList enchants;
        int enchantmentY = -34;
        if (stack.getItem() == Items.GOLDEN_APPLE && stack.hasEffect()) {
            Managers.TEXT.drawMCString("god", x * 2, enchantmentY, -3977919, true);
            enchantmentY -= 8;
        }
        if ((enchants = stack.getEnchantmentTagList()).tagCount() > 2 && this.max.booleanValue) {
            if (this.noMaxText.getValue()) {
                Managers.TEXT.drawMCString("", x * 2, enchantmentY, ColorUtil.toRGBA(this.max.getValue()), true);
            } else {
                Managers.TEXT.drawMCString("max", x * 2, enchantmentY, ColorUtil.toRGBA(this.max.getValue()), true);
            }
            enchantmentY -= 8;
        } else {
            for (int index = 0; index < enchants.tagCount(); ++index) {
                short id = enchants.getCompoundTagAt(index).getShort("id");
                short level = enchants.getCompoundTagAt(index).getShort("lvl");
                Enchantment enc = Enchantment.getEnchantmentByID(id);
                if (enc == null) continue;
                String encName = enc.isCurse() ? TextFormatting.RED + enc.getTranslatedName(level).substring(0, 4).toLowerCase() : enc.getTranslatedName(level).substring(0, 2).toLowerCase();
                encName = encName + level;
                Managers.TEXT.drawMCString(encName, x * 2, enchantmentY, -1, true);
                enchantmentY -= 8;
            }
        }
        if (DamageUtil.hasDurability(stack)) {
            float green = ((float)stack.getMaxDamage() - (float)stack.getItemDamage()) / (float)stack.getMaxDamage();
            float red = 1.0f - green;
            int dmg = 100 - (int)(red * 100.0f);
            String color = dmg >= 60 ? TextUtil.GREEN : (dmg >= 25 ? TextUtil.YELLOW : TextUtil.RED);
            Managers.TEXT.drawMCString(color + dmg + "%", x * 2, enchantmentY, -1, true);
        }
    }

    private float getBiggestArmorTag(EntityPlayer player) {
        ItemStack renderOffHand;
        Enchantment enc;
        short id;
        int index;
        float enchantmentY = 0.0f;
        boolean arm = false;
        for (ItemStack stack : player.inventory.armorInventory) {
            float encY = 0.0f;
            if (stack != null) {
                NBTTagList enchants = stack.getEnchantmentTagList();
                for (index = 0; index < enchants.tagCount(); ++index) {
                    id = enchants.getCompoundTagAt(index).getShort("id");
                    enc = Enchantment.getEnchantmentByID(id);
                    if (enc == null) continue;
                    encY += 8.0f;
                    arm = true;
                }
            }
            if (!(encY > enchantmentY)) continue;
            enchantmentY = encY;
        }
        ItemStack renderMainHand = player.getHeldItemMainhand().copy();
        if (renderMainHand.hasEffect()) {
            float encY = 0.0f;
            NBTTagList enchants = renderMainHand.getEnchantmentTagList();
            for (int index2 = 0; index2 < enchants.tagCount(); ++index2) {
                id = enchants.getCompoundTagAt(index2).getShort("id");
                Enchantment enc2 = Enchantment.getEnchantmentByID(id);
                if (enc2 == null) continue;
                encY += 8.0f;
                arm = true;
            }
            if (encY > enchantmentY) {
                enchantmentY = encY;
            }
        }
        if ((renderOffHand = player.getHeldItemOffhand().copy()).hasEffect()) {
            float encY = 0.0f;
            NBTTagList enchants = renderOffHand.getEnchantmentTagList();
            for (index = 0; index < enchants.tagCount(); ++index) {
                short id2 = enchants.getCompoundTagAt(index).getShort("id");
                enc = Enchantment.getEnchantmentByID(id2);
                if (enc == null) continue;
                encY += 8.0f;
                arm = true;
            }
            if (encY > enchantmentY) {
                enchantmentY = encY;
            }
        }
        return (float)(arm ? 0 : 20) + enchantmentY;
    }

    private String getDisplayTag(EntityPlayer player) {
        float health;
        String name = player.getDisplayName().getFormattedText();
        if (name.contains(mc.getSession().getUsername())) {
            name = "You";
        }
        String color = (health = EntityUtil.getHealth(player)) > 18.0f ? TextUtil.GREEN : (health > 16.0f ? TextUtil.DARK_GREEN : (health > 12.0f ? TextUtil.YELLOW : (health > 8.0f ? TextUtil.RED : TextUtil.DARK_RED)));
        String pingStr = "";
        if (this.ping.getValue()) {
            try {
                int responseTime = Objects.requireNonNull(mc.getConnection()).getPlayerInfo(player.getUniqueID()).getResponseTime();
                pingStr = pingStr + responseTime + "ms ";
            }
            catch (Exception responseTime) {
                // empty catch block
            }
        }
        String idString = "";
        if (this.entityID.getValue()) {
            idString = idString + "ID: " + player.getEntityId() + " ";
        }
        String gameModeStr = "";
        if (this.gamemode.getValue()) {
            String string = player.isCreative() ? gameModeStr + "[C] " : (gameModeStr = player.isSpectator() || player.isInvisible() ? gameModeStr + "[I] " : gameModeStr + "[S] ");
        }
        if (this.health.getValue()) {
            name = Math.floor(health) == (double)health ? name + color + " " + (health > 0.0f ? Integer.valueOf((int)Math.floor(health)) : "dead") : name + color + " " + (health > 0.0f ? Integer.valueOf((int)health) : "dead");
        }
        return " " + pingStr + idString + gameModeStr + name + " ";
    }

    private double interpolate(double previous, double current, float delta) {
        return previous + (current - previous) * (double)delta;
    }

    public void drawOutlineRect(float x, float y, float w, float h, int color) {
        float alpha = (float)(color >> 24 & 0xFF) / 255.0f;
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth(this.outLineWidth.getValue());
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        bufferbuilder.begin(2, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, y, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public void drawRect(float x, float y, float w, float h, int color) {
        float alpha = (float)(color >> 24 & 0xFF) / 255.0f;
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth(this.outLineWidth.getValue());
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, y, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}


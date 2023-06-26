package me.rebirthclient.mod.modules.impl.hud;

import java.awt.Color;
import java.util.Comparator;
import java.util.LinkedList;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class TargetHUD
extends Module {
    private final Setting<Integer> x = this.add(new Setting<>("X", 50, 0, 2000));
    private final Setting<Integer> y = this.add(new Setting<>("Y", 50, 0, 2000));
    private final Setting<Integer> backgroundAlpha = this.add(new Setting<>("Alpha", 80, 0, 255));
    EntityLivingBase target;

    public TargetHUD() {
        super("TargetHUD", "description", Category.HUD);
        this.target = TargetHUD.mc.player;
    }

    private static double applyAsDouble(EntityLivingBase entityLivingBase) {
        return entityLivingBase.getDistance(TargetHUD.mc.player);
    }

    private static boolean checkIsNotPlayer(Entity entity) {
        return !entity.equals(TargetHUD.mc.player);
    }

    @Override
    public synchronized void onTick() {
        LinkedList entities = new LinkedList();
        TargetHUD.mc.world.loadedEntityList.stream().filter(EntityPlayer.class::isInstance).filter(TargetHUD::checkIsNotPlayer).map(EntityLivingBase.class::cast).sorted(Comparator.comparingDouble(TargetHUD::applyAsDouble)).forEach(entities::add);
        this.target = !entities.isEmpty() ? (EntityLivingBase)entities.get(0) : TargetHUD.mc.player;
        if (TargetHUD.mc.currentScreen instanceof GuiChat) {
            this.target = TargetHUD.mc.player;
        }
    }

    @Override
    public synchronized void onRender2D(Render2DEvent event) {
        if (this.target != null && !this.target.isDead) {
            FontRenderer fr = TargetHUD.mc.fontRenderer;
            int color = this.target.getHealth() / this.target.getMaxHealth() > 0.66f ? -16711936 : (this.target.getHealth() / this.target.getMaxHealth() > 0.33f ? -26368 : -65536);
            GlStateManager.color(1.0f, 1.0f, 1.0f);
            GuiInventory.drawEntityOnScreen(this.x.getValue() + 15, this.y.getValue() + 32, 15, 1.0f, 1.0f, this.target);
            LinkedList armorList = new LinkedList();
            LinkedList _armorList = new LinkedList();
            this.target.getArmorInventoryList().forEach(itemStack -> {
                if (!itemStack.isEmpty()) {
                    _armorList.add(itemStack);
                }
            });
            for (int i = _armorList.size() - 1; i >= 0; --i) {
                armorList.add(_armorList.get(i));
            }
            int armorSize = 0;
            switch (armorList.size()) {
                case 0: {
                    if (!this.target.getHeldItemMainhand().isEmpty() && !this.target.getHeldItemOffhand().isEmpty()) {
                        mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemMainhand(), this.x.getValue() + 28, this.y.getValue() + 18);
                        mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemOffhand(), this.x.getValue() + 43, this.y.getValue() + 18);
                        armorSize += 45;
                        break;
                    }
                    if (this.target.getHeldItemMainhand().isEmpty() && this.target.getHeldItemOffhand().isEmpty()) break;
                    mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemMainhand().isEmpty() ? this.target.getHeldItemOffhand() : this.target.getHeldItemMainhand(), this.x.getValue() + 28, this.y.getValue() + 18);
                    armorSize += 30;
                    break;
                }
                case 1: {
                    armorSize = 15;
                    mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(0), this.x.getValue() + 28, this.y.getValue() + 18);
                    if (!this.target.getHeldItemMainhand().isEmpty() && !this.target.getHeldItemOffhand().isEmpty()) {
                        mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemMainhand(), this.x.getValue() + 43, this.y.getValue() + 18);
                        mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemOffhand(), this.x.getValue() + 58, this.y.getValue() + 18);
                        armorSize += 45;
                        break;
                    }
                    if (this.target.getHeldItemMainhand().isEmpty() && this.target.getHeldItemOffhand().isEmpty()) break;
                    mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemMainhand().isEmpty() ? this.target.getHeldItemOffhand() : this.target.getHeldItemMainhand(), this.x.getValue() + 43, this.y.getValue() + 18);
                    armorSize += 30;
                    break;
                }
                case 2: {
                    armorSize = 30;
                    mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(0), this.x.getValue() + 28, this.y.getValue() + 18);
                    mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(1), this.x.getValue() + 43, this.y.getValue() + 18);
                    if (!this.target.getHeldItemMainhand().isEmpty() && !this.target.getHeldItemOffhand().isEmpty()) {
                        mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemMainhand(), this.x.getValue() + 58, this.y.getValue() + 18);
                        mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemOffhand(), this.x.getValue() + 73, this.y.getValue() + 18);
                        armorSize += 45;
                        break;
                    }
                    if (this.target.getHeldItemMainhand().isEmpty() && this.target.getHeldItemOffhand().isEmpty()) break;
                    mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemMainhand().isEmpty() ? this.target.getHeldItemOffhand() : this.target.getHeldItemMainhand(), this.x.getValue() + 58, this.y.getValue() + 18);
                    armorSize += 30;
                    break;
                }
                case 3: {
                    armorSize = 45;
                    mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(0), this.x.getValue() + 28, this.y.getValue() + 18);
                    mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(1), this.x.getValue() + 43, this.y.getValue() + 18);
                    mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(2), this.x.getValue() + 58, this.y.getValue() + 18);
                    if (!this.target.getHeldItemMainhand().isEmpty() && !this.target.getHeldItemOffhand().isEmpty()) {
                        mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemMainhand(), this.x.getValue() + 73, this.y.getValue() + 18);
                        mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemOffhand(), this.x.getValue() + 98, this.y.getValue() + 18);
                        armorSize += 45;
                        break;
                    }
                    if (this.target.getHeldItemMainhand().isEmpty() && this.target.getHeldItemOffhand().isEmpty()) break;
                    mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemMainhand().isEmpty() ? this.target.getHeldItemOffhand() : this.target.getHeldItemMainhand(), this.x.getValue() + 73, this.y.getValue() + 18);
                    armorSize += 30;
                    break;
                }
                case 4: {
                    armorSize = 60;
                    mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(0), this.x.getValue() + 28, this.y.getValue() + 18);
                    mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(1), this.x.getValue() + 43, this.y.getValue() + 18);
                    mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(2), this.x.getValue() + 58, this.y.getValue() + 18);
                    mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(3), this.x.getValue() + 73, this.y.getValue() + 18);
                    if (!this.target.getHeldItemMainhand().isEmpty() && !this.target.getHeldItemOffhand().isEmpty()) {
                        mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemMainhand(), this.x.getValue() + 98, this.y.getValue() + 18);
                        mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemOffhand(), this.x.getValue() + 113, this.y.getValue() + 18);
                        armorSize += 45;
                        break;
                    }
                    if (this.target.getHeldItemMainhand().isEmpty() && this.target.getHeldItemOffhand().isEmpty()) break;
                    mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemMainhand().isEmpty() ? this.target.getHeldItemOffhand() : this.target.getHeldItemMainhand(), this.x.getValue() + 98, this.y.getValue() + 18);
                    armorSize += 30;
                }
            }
            int backgroundStopY = this.y.getValue() + 35;
            int stringWidth = fr.getStringWidth(this.target.getName()) + 30;
            int backgroundStopX = fr.getStringWidth(this.target.getName()) > armorSize ? this.x.getValue() + stringWidth : this.x.getValue() + armorSize + 30;
            Gui.drawRect(this.x.getValue() - 2, this.y.getValue(), backgroundStopX += 5, backgroundStopY += 5, new Color(0, 0, 0, this.backgroundAlpha.getValue()).getRGB());
            int healthBarLength = (int)(this.target.getHealth() / this.target.getMaxHealth() * (float)(backgroundStopX - this.x.getValue()));
            Gui.drawRect(this.x.getValue() - 2, backgroundStopY - 2, this.x.getValue() + healthBarLength, backgroundStopY, color);
            fr.drawString(this.target.getName(), (float)(this.x.getValue() + 30), (float)(this.y.getValue() + 8), new Color(255, 255, 255).getRGB(), true);
        }
    }
}


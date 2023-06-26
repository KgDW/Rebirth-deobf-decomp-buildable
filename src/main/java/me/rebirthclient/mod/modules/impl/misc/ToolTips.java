package me.rebirthclient.mod.modules.impl.misc;

import java.text.DecimalFormat;
import me.rebirthclient.api.events.impl.RenderToolTipEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.misc.Peek;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ToolTips
extends Module {
    public static ToolTips INSTANCE;
    public final Setting<Boolean> shulkerPreview = this.add(new Setting<>("ShulkerPreview", true).setParent());
    public final Setting<Boolean> onlyShulker = this.add(new Setting<>("OnlyShulker", false, v -> this.shulkerPreview.isOpen()));
    public final Setting<Boolean> wheelPeek = this.add(new Setting<>("WheelPeek", true));
    private final DecimalFormat format = new DecimalFormat("#");
    private float width;
    private float height;

    public ToolTips() {
        super("ToolTips", "Advanced tool tips", Category.MISC);
        INSTANCE = this;
    }

    public static void drawShulkerGui(ItemStack stack, String name) {
        try {
            Item item = stack.getItem();
            TileEntityShulkerBox entityBox = new TileEntityShulkerBox();
            ItemShulkerBox shulker = (ItemShulkerBox)item;
            entityBox.blockType = shulker.getBlock();
            entityBox.setWorld(Peek.mc.world);
            ItemStackHelper.loadAllItems(stack.getTagCompound().getCompoundTag("BlockEntityTag"), entityBox.items);
            entityBox.readFromNBT(stack.getTagCompound().getCompoundTag("BlockEntityTag"));
            entityBox.setCustomName(name == null ? stack.getDisplayName() : name);
            new Thread(() -> {
                try {
                    Thread.sleep(200L);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                Peek.mc.player.displayGUIChest(entityBox);
            }).start();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @SubscribeEvent
    public void onRenderToolTip(RenderToolTipEvent event) {
        if (event.isCanceled() || ToolTips.nullCheck() || ToolTips.fullNullCheck()) {
            return;
        }
        if (!event.getItemStack().isEmpty()) {
            if (!(event.getItemStack().getItem() instanceof ItemShulkerBox) && this.onlyShulker.getValue()) {
                return;
            }
            event.setCanceled(true);
            int x = event.getX();
            int y = event.getY();
            if (event.getItemStack().getItem() instanceof ItemShulkerBox && this.shulkerPreview.getValue()) {
                this.drawShulkerPreview(event.getItemStack(), x + 3, y - 10);
            }
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.translate((float)(x + 10), (float)(y - 5), 0.0f);
            String title = event.getItemStack().getDisplayName();
            RenderUtil.drawRect(0.0f, -2.0f, this.width, this.height, -519565792);
            float prevWidth = this.width;
            this.width = 0.0f;
            int newY = this.drawString(title, 3, 1, this.getItemColor(event.getItemStack()));
            String itemNameDesc = this.getEnchants(event.getItemStack());
            if (itemNameDesc != null) {
                newY = this.drawString(itemNameDesc, 3, newY, 0xFF0000);
            }
            String typeString = null;
            String rightTypeString = null;
            if (event.getItemStack().getItem() instanceof ItemArmor) {
                ItemArmor armor = (ItemArmor)event.getItemStack().getItem();
                switch (armor.getEquipmentSlot()) {
                    case CHEST: {
                        typeString = "Chest";
                        break;
                    }
                    case FEET: {
                        typeString = "Feet";
                        break;
                    }
                    case HEAD: {
                        typeString = "Head";
                        break;
                    }
                    case LEGS: {
                        typeString = "Leggings";
                        break;
                    }
                }
                switch (armor.getArmorMaterial()) {
                    case CHAIN: {
                        rightTypeString = "Chain";
                        break;
                    }
                    case DIAMOND: {
                        rightTypeString = "Diamond";
                        break;
                    }
                    case GOLD: {
                        rightTypeString = "Gold";
                        break;
                    }
                    case IRON: {
                        rightTypeString = "Iron";
                        break;
                    }
                    case LEATHER: {
                        rightTypeString = "Leather";
                        break;
                    }
                }
            }
            if (event.getItemStack().getItem() instanceof ItemElytra) {
                typeString = "Chest";
            }
            if (event.getItemStack().getItem() instanceof ItemSword) {
                typeString = "Mainhand";
                rightTypeString = "Sword";
            }
            if (typeString != null) {
                int prevY = newY;
                newY = this.drawString(typeString, 3, newY, -1);
                if (rightTypeString != null) {
                    this.drawString(rightTypeString, (int)(prevWidth - (float)Managers.TEXT.getStringWidth(rightTypeString) - 3.0f), prevY, -1);
                    this.width = Math.max(48.0f, prevWidth);
                }
            }
            if (event.getItemStack().getItem() instanceof ItemSword) {
                ItemSword sword = (ItemSword)event.getItemStack().getItem();
                newY = this.drawString(sword.getAttackDamage() + " - " + sword.getAttackDamage() + " Damage", 3, newY, -1);
            }
            for (Enchantment enchant : EnchantmentHelper.getEnchantments(event.getItemStack()).keySet()) {
                String name = "+" + EnchantmentHelper.getEnchantmentLevel(enchant, event.getItemStack()) + " " + I18n.translateToLocal(enchant.getName());
                if (name.contains("Vanish") || name.contains("Binding")) continue;
                int color = -1;
                if (name.contains("Mending") || name.contains("Unbreaking")) {
                    color = 65280;
                }
                newY = this.drawString(name, 3, newY, color);
            }
            if (event.getItemStack().getMaxDamage() > 1) {
                float armorPct = (float)(event.getItemStack().getMaxDamage() - event.getItemStack().getItemDamage()) / (float)event.getItemStack().getMaxDamage() * 100.0f;
                String durability = String.format("Durability %s %s / %s", this.format.format(armorPct) + "%", event.getItemStack().getMaxDamage() - event.getItemStack().getItemDamage(), event.getItemStack().getMaxDamage());
                newY = this.drawString(durability, 3, newY, -1);
            }
            GlStateManager.enableDepth();
            ToolTips.mc.getRenderItem().zLevel = 150.0f;
            RenderHelper.enableGUIStandardItemLighting();
            RenderHelper.disableStandardItemLighting();
            ToolTips.mc.getRenderItem().zLevel = 0.0f;
            GlStateManager.enableLighting();
            GlStateManager.translate((float)(-(x + 10)), (float)(-(y - 5)), 0.0f);
            this.height = newY + 1;
        }
    }

    private void drawShulkerPreview(ItemStack stack, int x, int y) {
        NBTTagCompound blockEntityTag;
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("BlockEntityTag", 10) && (blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag")).hasKey("Items", 9)) {
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.disableDepth();
            RenderUtil.drawRect(x + 7, y + 17, x + 171, y + 57 + 16, -519565792);
            GlStateManager.enableDepth();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableColorMaterial();
            GlStateManager.enableLighting();
            NonNullList nonnulllist = NonNullList.withSize(27, (Object)ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(blockEntityTag, nonnulllist);
            for (int i = 0; i < nonnulllist.size(); ++i) {
                int iX = x + i % 9 * 18 + 8;
                int iY = y + i / 9 * 18 + 18;
                ItemStack itemStack = (ItemStack)nonnulllist.get(i);
                Peek.mc.getItemRenderer().itemRenderer.zLevel = 501.0f;
                mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, iX, iY);
                mc.getRenderItem().renderItemOverlayIntoGUI(Peek.mc.fontRenderer, itemStack, iX, iY, null);
                Peek.mc.getItemRenderer().itemRenderer.zLevel = 0.0f;
            }
            GlStateManager.disableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private int drawString(String string, int x, int y, int color) {
        Managers.TEXT.drawStringWithShadow(string, x, y, color);
        this.width = Math.max(this.width, (float)(Managers.TEXT.getStringWidth(string) + x + 3));
        return y + 9;
    }

    private int getItemColor(ItemStack stack) {
        if (stack.getItem() instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor)stack.getItem();
            switch (armor.getArmorMaterial()) {
                case CHAIN: {
                    return 28893;
                }
                case DIAMOND: {
                    return EnchantmentHelper.getEnchantments(stack).keySet().isEmpty() ? 2031360 : 10696174;
                }
                case GOLD: 
                case IRON: {
                    return 2031360;
                }
                case LEATHER: {
                    return 0x9D9D9D;
                }
            }
        } else {
            if (stack.getItem().equals(Items.GOLDEN_APPLE)) {
                if (stack.hasEffect()) {
                    return 10696174;
                }
                return 52735;
            }
            if (stack.getItem() instanceof ItemSword) {
                ItemSword sword = (ItemSword)stack.getItem();
                String material = sword.getToolMaterialName();
                if (material.equals("DIAMOND")) {
                    return 10696174;
                }
                if (material.equals("CHAIN")) {
                    return 28893;
                }
                if (material.equals("GOLD")) {
                    return 2031360;
                }
                if (material.equals("IRON")) {
                    return 2031360;
                }
                if (material.equals("LEATHER")) {
                    return 0x9D9D9D;
                }
                return -1;
            }
            if (stack.getItem().equals(Items.TOTEM_OF_UNDYING)) {
                return 0xFF8000;
            }
            if (stack.getItem().equals(Items.CHORUS_FRUIT)) {
                return 28893;
            }
            if (stack.getItem().equals(Items.ENDER_PEARL)) {
                return 28893;
            }
            if (stack.getItem().equals(Items.END_CRYSTAL)) {
                return 10696174;
            }
            if (stack.getItem().equals(Items.EXPERIENCE_BOTTLE)) {
                return 2031360;
            }
            if (stack.getItem().equals(Items.POTIONITEM)) {
                return 2031360;
            }
            if (Item.getIdFromItem(stack.getItem()) == 130) {
                return 10696174;
            }
            if (stack.getItem() instanceof ItemShulkerBox) {
                return 10696174;
            }
        }
        return -1;
    }

    private String getEnchants(ItemStack stack) {
        StringBuilder result = new StringBuilder();
        for (Enchantment enchant : EnchantmentHelper.getEnchantments(stack).keySet()) {
            if (enchant == null) continue;
            String name = enchant.getTranslatedName(EnchantmentHelper.getEnchantmentLevel(enchant, stack));
            if (name.contains("Vanish")) {
                result.append("Vanishing ");
                continue;
            }
            if (!name.contains("Binding")) continue;
            result.append("Binding ");
        }
        if (stack.getItem().equals(Items.GOLDEN_APPLE) && stack.hasEffect()) {
            return "God";
        }
        return result.length() == 0 ? null : result.toString();
    }
}


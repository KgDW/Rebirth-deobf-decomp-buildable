package me.rebirthclient.mod.modules.impl.misc;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Peek
extends Module {
    private final Map<EntityPlayer, ItemStack> spiedPlayers = new ConcurrentHashMap<>();
    private final Map<EntityPlayer, Timer> playerTimers = new ConcurrentHashMap<>();

    public Peek() {
        super("Peek", "Allows you to peek into your enemy's shulkerboxes", Category.MISC);
    }

    @Override
    public void onUpdate() {
        for (EntityPlayer player : Peek.mc.world.playerEntities) {
            if (player == null || !(player.getHeldItemMainhand().getItem() instanceof ItemShulkerBox) || Peek.mc.player == player) continue;
            ItemStack stack = player.getHeldItemMainhand();
            this.spiedPlayers.put(player, stack);
        }
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (Peek.fullNullCheck()) {
            return;
        }
        int x = Managers.TEXT.scaledWidth / 2 - 78;
        int y = 24;
        for (EntityPlayer player : Peek.mc.world.playerEntities) {
            Timer playerTimer;
            if (this.spiedPlayers.get(player) == null) continue;
            if (player.getHeldItemMainhand() == null || !(player.getHeldItemMainhand().getItem() instanceof ItemShulkerBox)) {
                playerTimer = this.playerTimers.get(player);
                if (playerTimer == null) {
                    Timer timer = new Timer();
                    timer.reset();
                    this.playerTimers.put(player, timer);
                } else if (playerTimer.passedS(3.0)) {
                    continue;
                }
            } else if (player.getHeldItemMainhand().getItem() instanceof ItemShulkerBox && (playerTimer = this.playerTimers.get(player)) != null) {
                playerTimer.reset();
                this.playerTimers.put(player, playerTimer);
            }
            ItemStack stack = this.spiedPlayers.get(player);
            this.renderShulkerToolTip(stack, x, y, player.getName());
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void makeTooltip(ItemTooltipEvent event) {
    }

    private void renderShulkerToolTip(ItemStack stack, int x, int y, String name) {
        NBTTagCompound blockEntityTag;
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("BlockEntityTag", 10) && (blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag")).hasKey("Items", 9)) {
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/constant/ingame/container.png"));
            this.drawTexturedRect(x, y, 0, 16);
            this.drawTexturedRect(x, y + 16, 16, 57);
            this.drawTexturedRect(x, y + 16 + 54, 160, 8);
            GlStateManager.disableDepth();
            Color color = new Color(ClickGui.INSTANCE.color.getValue().getRed(), ClickGui.INSTANCE.color.getValue().getGreen(), ClickGui.INSTANCE.color.getValue().getBlue(), 200);
            Managers.TEXT.drawStringWithShadow(name == null ? stack.getDisplayName() : name, x + 8, y + 6, ColorUtil.toRGBA(color));
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

    private void drawTexturedRect(int x, int y, int textureY, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder BufferBuilder2 = tessellator.getBuffer();
        BufferBuilder2.begin(7, DefaultVertexFormats.POSITION_TEX);
        BufferBuilder2.pos(x, y + height, 500.0).tex(0.0, (float)(textureY + height) * 0.00390625f).endVertex();
        BufferBuilder2.pos(x + 176, y + height, 500.0).tex(0.6875, (float)(textureY + height) * 0.00390625f).endVertex();
        BufferBuilder2.pos(x + 176, y, 500.0).tex(0.6875, (float)textureY * 0.00390625f).endVertex();
        BufferBuilder2.pos(x, y, 500.0).tex(0.0, (float)textureY * 0.00390625f).endVertex();
        tessellator.draw();
    }
}


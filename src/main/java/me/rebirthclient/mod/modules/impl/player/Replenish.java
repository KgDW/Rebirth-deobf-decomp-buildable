package me.rebirthclient.mod.modules.impl.player;

import java.util.ArrayList;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class Replenish
extends Module {
    private final Setting<Integer> delay = this.add(new Setting<>("Delay", 2, 0, 10));
    private final Setting<Integer> stack = this.add(new Setting<>("Stack", 50, 8, 64));
    private final Timer timer = new Timer();
    private final ArrayList<Item> Hotbar = new ArrayList();

    public Replenish() {
        super("Replenish", "Replenishes your hotbar", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (Replenish.fullNullCheck()) {
            return;
        }
        this.Hotbar.clear();
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = Replenish.mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && !this.Hotbar.contains(stack.getItem())) {
                this.Hotbar.add(stack.getItem());
                continue;
            }
            this.Hotbar.add(Items.AIR);
        }
    }

    @Override
    public void onUpdate() {
        if (Replenish.mc.currentScreen != null) {
            return;
        }
        if (!this.timer.passedMs(this.delay.getValue() * 1000)) {
            return;
        }
        for (int i = 0; i < 9; ++i) {
            if (!this.RefillSlotIfNeed(i)) continue;
            this.timer.reset();
            return;
        }
    }

    private boolean RefillSlotIfNeed(int slot) {
        ItemStack stack = Replenish.mc.player.inventory.getStackInSlot(slot);
        if (stack.isEmpty() || stack.getItem() == Items.AIR) {
            return false;
        }
        if (!stack.isStackable()) {
            return false;
        }
        if (stack.getCount() >= stack.getMaxStackSize()) {
            return false;
        }
        if (stack.getCount() >= this.stack.getValue()) {
            return false;
        }
        for (int i = 9; i < 36; ++i) {
            ItemStack item = Replenish.mc.player.inventory.getStackInSlot(i);
            if (item.isEmpty() || !this.CanItemBeMergedWith(stack, item)) continue;
            Replenish.mc.playerController.windowClick(Replenish.mc.player.inventoryContainer.windowId, i, 0, ClickType.QUICK_MOVE, Replenish.mc.player);
            Replenish.mc.playerController.updateController();
            return true;
        }
        return false;
    }

    private boolean CanItemBeMergedWith(ItemStack source, ItemStack stack) {
        return source.getItem() == stack.getItem() && source.getDisplayName().equals(stack.getDisplayName());
    }
}


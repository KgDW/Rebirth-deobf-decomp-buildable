package me.rebirthclient.asm.accessors;

import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.inventory.IInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={GuiShulkerBox.class})
public interface IGuiShulkerBox {
    @Accessor(value="inventory")
    public IInventory getInventory();
}


package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.PacketExp;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;

public class KeyPearl
extends Module {
    private final Setting<Mode> mode = this.add(new Setting<>("Mode", Mode.Middle));
    private final Setting<Boolean> noPlayerTrace = this.add(new Setting<>("NoPlayerTrace", true));
    private final Setting<Boolean> inventory = this.add(new Setting<>("Inventory", true).setParent());
    private final Setting<Boolean> sync = this.add(new Setting<>("Sync", true, v -> this.inventory.isOpen()));
    private final Setting<Boolean> testSync = this.add(new Setting<>("TestSync", true, v -> this.inventory.isOpen()));
    private boolean clicked;

    public KeyPearl() {
        super("KeyPearl", "Throws a pearl", Category.PLAYER);
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @Override
    public void onEnable() {
        if (!KeyPearl.fullNullCheck() && this.mode.getValue() == Mode.Key) {
            this.throwPearl();
            this.disable();
        }
    }

    @Override
    public void onTick() {
        if (this.mode.getValue() == Mode.Middle) {
            if (Mouse.isButtonDown(2) && KeyPearl.mc.currentScreen == null) {
                this.clicked = true;
            } else if (this.clicked) {
                this.throwPearl();
                this.clicked = false;
            }
        }
    }

    private void throwPearl() {
        boolean mainhand;
        RayTraceResult result;
        if (this.noPlayerTrace.getValue() && (result = KeyPearl.mc.objectMouseOver) != null && result.typeOfHit == RayTraceResult.Type.ENTITY && result.entityHit instanceof EntityPlayer) {
            return;
        }
        int pearlSlot = InventoryUtil.findHotbarClass(ItemEnderPearl.class);
        boolean offhand = KeyPearl.mc.player.getHeldItemOffhand().getItem() == Items.ENDER_PEARL;
        boolean bl = mainhand = KeyPearl.mc.player.getHeldItemOffhand().getItem() == Items.ENDER_PEARL;
        if (pearlSlot != -1 || offhand || mainhand) {
            int oldslot = KeyPearl.mc.player.inventory.currentItem;
            if (!offhand && !mainhand) {
                InventoryUtil.switchToHotbarSlot(pearlSlot, false);
            }
            KeyPearl.mc.playerController.processRightClick(KeyPearl.mc.player, KeyPearl.mc.world, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
            if (!offhand && !mainhand) {
                InventoryUtil.switchToHotbarSlot(oldslot, false);
            }
            return;
        }
        if (this.inventory.getValue() && (pearlSlot = InventoryUtil.findClassInventorySlot(ItemEnderPearl.class, false)) != -1) {
            KeyPearl.mc.playerController.windowClick(0, pearlSlot, KeyPearl.mc.player.inventory.currentItem, ClickType.SWAP, KeyPearl.mc.player);
            KeyPearl.mc.playerController.processRightClick(KeyPearl.mc.player, KeyPearl.mc.world, EnumHand.MAIN_HAND);
            KeyPearl.mc.playerController.windowClick(0, pearlSlot, KeyPearl.mc.player.inventory.currentItem, ClickType.SWAP, KeyPearl.mc.player);
            if (this.sync.getValue()) {
                PacketExp.INSTANCE.throwExp();
            }
            if (this.testSync.getValue()) {
                KeyPearl.mc.playerController.windowClick(0, pearlSlot, 0, ClickType.PICKUP, KeyPearl.mc.player);
                KeyPearl.mc.playerController.windowClick(0, pearlSlot, 0, ClickType.PICKUP, KeyPearl.mc.player);
                KeyPearl.mc.playerController.windowClick(0, 36 + KeyPearl.mc.player.inventory.currentItem, 0, ClickType.PICKUP, KeyPearl.mc.player);
                KeyPearl.mc.playerController.windowClick(0, 36 + KeyPearl.mc.player.inventory.currentItem, 0, ClickType.PICKUP, KeyPearl.mc.player);
            }
        }
    }

    private static enum Mode {
        Key,
        Middle

    }
}


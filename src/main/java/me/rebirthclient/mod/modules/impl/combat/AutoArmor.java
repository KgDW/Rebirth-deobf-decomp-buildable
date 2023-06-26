package me.rebirthclient.mod.modules.impl.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.mod.gui.screen.Gui;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.PacketExp;
import me.rebirthclient.mod.modules.impl.exploit.XCarry;
import me.rebirthclient.mod.modules.impl.movement.ElytraFly;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class AutoArmor
extends Module {
    private final Setting<Boolean> autoMend = this.add(new Setting<>("AutoMend", false).setParent());
    private final Setting<Integer> closestEnemy = this.add(new Setting<>("EnemyRange", 8, 1, 20, v -> this.autoMend.isOpen()));
    private final Setting<Integer> helmetThreshold = this.add(new Setting<>("Helmet%", 80, 1, 100, v -> this.autoMend.isOpen()));
    private final Setting<Integer> chestThreshold = this.add(new Setting<>("Chest%", 80, 1, 100, v -> this.autoMend.isOpen()));
    private final Setting<Integer> legThreshold = this.add(new Setting<>("Legs%", 80, 1, 100, v -> this.autoMend.isOpen()));
    private final Setting<Integer> bootsThreshold = this.add(new Setting<>("Boots%", 80, 1, 100, v -> this.autoMend.isOpen()));
    private final Setting<Integer> maxThreshold = this.add(new Setting<>("Max%", 90, 1, 100, v -> this.autoMend.isOpen()));
    private final Setting<Boolean> save = this.add(new Setting<>("Save", false).setParent());
    private final Setting<Integer> saveThreshold = this.add(new Setting<>("Save%", 5, 1, 10, v -> this.save.isOpen()));
    private final Setting<Integer> delay = this.add(new Setting<>("Delay", 50, 0, 500));
    private final Setting<Integer> actions = this.add(new Setting<>("Actions", 3, 1, 12));
    private final Setting<Boolean> curse = this.add(new Setting<>("CurseOfBinding", false));
    private final Setting<Boolean> tps = this.add(new Setting<>("TpsSync", true));
    private final Setting<Boolean> updateController = this.add(new Setting<>("Update", true));
    private final Setting<Boolean> shiftClick = this.add(new Setting<>("ShiftClick", false));
    private final Setting<Boolean> autoElytra = this.add(new Setting<>("AutoElytra", true));
    private final Setting<Bind> elytraBind = this.add(new Setting<>("Elytra", new Bind(-1), v -> !this.autoElytra.getValue()));
    private final Setting<Bind> noHelmBind = this.add(new Setting<>("NoHelmet", new Bind(-1)));
    private final Timer timer = new Timer();
    private final Timer elytraTimer = new Timer();
    private final Queue<InventoryUtil.QueuedTask> queuedTaskList = new ConcurrentLinkedQueue<>();
    private final List<Integer> doneSlots = new ArrayList<>();
    private boolean elytraOn;
    private boolean helmOff;

    public AutoArmor() {
        super("AutoArmor", "Puts Armor on for you", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        this.queuedTaskList.clear();
        this.doneSlots.clear();
        this.elytraOn = false;
        this.helmOff = false;
    }

    @Override
    public String getInfo() {
        if (this.elytraOn) {
            return "Elytra";
        }
        return null;
    }

    @Override
    public void onLogout() {
        this.queuedTaskList.clear();
        this.doneSlots.clear();
    }

    @Override
    public void onLogin() {
        this.timer.reset();
        this.elytraTimer.reset();
    }

    @Override
    public void onTick() {
        if (AutoArmor.mc.currentScreen instanceof GuiContainer && !(AutoArmor.mc.currentScreen instanceof GuiInventory)) {
            return;
        }
        if (this.autoElytra.getValue()) {
            this.elytraOn = !AutoArmor.mc.player.onGround && ElytraFly.INSTANCE.isOn();
        } else if (this.elytraBind.getValue().getKey() == -1) {
            this.elytraOn = false;
        }
        if (this.queuedTaskList.isEmpty()) {
            int slot;
            int slot2;
            int slot3;
            int slot4;
            ItemStack feet2;
            ItemStack legging2;
            ItemStack chest2;
            ItemStack helm;
            boolean throwingExp;
            boolean bl = throwingExp = InventoryUtil.holdingItem(ItemExpBottle.class) && AutoArmor.mc.gameSettings.keyBindUseItem.isKeyDown() || PacketExp.INSTANCE.isThrow();
            if (this.autoMend.getValue() && throwingExp && (this.isSafe() || EntityUtil.isSafe(AutoArmor.mc.player, 1, false))) {
                helm = AutoArmor.mc.player.inventoryContainer.getSlot(5).getStack();
                chest2 = AutoArmor.mc.player.inventoryContainer.getSlot(6).getStack();
                legging2 = AutoArmor.mc.player.inventoryContainer.getSlot(7).getStack();
                feet2 = AutoArmor.mc.player.inventoryContainer.getSlot(8).getStack();
                if (!helm.isEmpty && EntityUtil.getDamagePercent(helm) < this.maxThreshold.getValue() || !chest2.isEmpty && EntityUtil.getDamagePercent(chest2) < this.maxThreshold.getValue() || !legging2.isEmpty && EntityUtil.getDamagePercent(legging2) < this.maxThreshold.getValue() || !feet2.isEmpty && EntityUtil.getDamagePercent(feet2) < this.maxThreshold.getValue()) {
                    if (!helm.isEmpty && EntityUtil.getDamagePercent(helm) >= this.helmetThreshold.getValue()) {
                        this.takeOffSlot(5);
                    }
                    if (!chest2.isEmpty && EntityUtil.getDamagePercent(chest2) >= this.chestThreshold.getValue()) {
                        this.takeOffSlot(6);
                    }
                    if (!legging2.isEmpty && EntityUtil.getDamagePercent(legging2) >= this.legThreshold.getValue()) {
                        this.takeOffSlot(7);
                    }
                    if (!feet2.isEmpty && EntityUtil.getDamagePercent(feet2) >= this.bootsThreshold.getValue()) {
                        this.takeOffSlot(8);
                    }
                    return;
                }
            }
            if (this.save.getValue()) {
                helm = AutoArmor.mc.player.inventoryContainer.getSlot(5).getStack();
                if (this.save.getValue() && !helm.isEmpty && EntityUtil.getDamagePercent(helm) <= this.saveThreshold.getValue()) {
                    this.takeOffSlot(5);
                }
                chest2 = AutoArmor.mc.player.inventoryContainer.getSlot(6).getStack();
                if (this.save.getValue() && !chest2.isEmpty && EntityUtil.getDamagePercent(chest2) <= this.saveThreshold.getValue()) {
                    this.takeOffSlot(6);
                }
                legging2 = AutoArmor.mc.player.inventoryContainer.getSlot(7).getStack();
                if (this.save.getValue() && !legging2.isEmpty && EntityUtil.getDamagePercent(legging2) <= this.saveThreshold.getValue()) {
                    this.takeOffSlot(7);
                }
                feet2 = AutoArmor.mc.player.inventoryContainer.getSlot(8).getStack();
                if (this.save.getValue() && !feet2.isEmpty && EntityUtil.getDamagePercent(feet2) <= this.saveThreshold.getValue()) {
                    this.takeOffSlot(8);
                }
            }
            helm = AutoArmor.mc.player.inventoryContainer.getSlot(5).getStack();
            if (!this.helmOff && helm.getItem() == Items.AIR && (slot4 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.HEAD, this.curse.getValue(), XCarry.INSTANCE.isOn())) != -1) {
                this.getSlotOn(5, slot4);
            } else if (this.helmOff && helm.getItem() != Items.AIR) {
                this.takeOffSlot(5);
            }
            ItemStack chest = AutoArmor.mc.player.inventoryContainer.getSlot(6).getStack();
            if (chest.getItem() == Items.AIR) {
                if (this.queuedTaskList.isEmpty()) {
                    if (this.elytraOn && this.elytraTimer.passedMs(500L)) {
                        int elytraSlot = InventoryUtil.findItemInventorySlot(Items.ELYTRA, false, XCarry.INSTANCE.isOn());
                        if (elytraSlot != -1) {
                            if (elytraSlot < 5 && elytraSlot > 1 || !this.shiftClick.getValue()) {
                                this.queuedTaskList.add(new InventoryUtil.QueuedTask(elytraSlot));
                                this.queuedTaskList.add(new InventoryUtil.QueuedTask(6));
                            } else {
                                this.queuedTaskList.add(new InventoryUtil.QueuedTask(elytraSlot, true));
                            }
                            if (this.updateController.getValue()) {
                                this.queuedTaskList.add(new InventoryUtil.QueuedTask());
                            }
                            this.elytraTimer.reset();
                        }
                    } else if (!this.elytraOn && (slot3 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.CHEST, this.curse.getValue(), XCarry.INSTANCE.isOn())) != -1) {
                        this.getSlotOn(6, slot3);
                    }
                }
            } else if (this.elytraOn && chest.getItem() != Items.ELYTRA && this.elytraTimer.passedMs(500L)) {
                if (this.queuedTaskList.isEmpty()) {
                    slot3 = InventoryUtil.findItemInventorySlot(Items.ELYTRA, false, XCarry.INSTANCE.isOn());
                    if (slot3 != -1) {
                        this.queuedTaskList.add(new InventoryUtil.QueuedTask(slot3));
                        this.queuedTaskList.add(new InventoryUtil.QueuedTask(6));
                        this.queuedTaskList.add(new InventoryUtil.QueuedTask(slot3));
                        if (this.updateController.getValue()) {
                            this.queuedTaskList.add(new InventoryUtil.QueuedTask());
                        }
                    }
                    this.elytraTimer.reset();
                }
            } else if (!this.elytraOn && chest.getItem() == Items.ELYTRA && this.elytraTimer.passedMs(500L) && this.queuedTaskList.isEmpty()) {
                slot3 = InventoryUtil.findItemInventorySlot(Items.DIAMOND_CHESTPLATE, false, XCarry.INSTANCE.isOn());
                if (slot3 == -1 && (slot3 = InventoryUtil.findItemInventorySlot(Items.IRON_CHESTPLATE, false, XCarry.INSTANCE.isOn())) == -1 && (slot3 = InventoryUtil.findItemInventorySlot(Items.GOLDEN_CHESTPLATE, false, XCarry.INSTANCE.isOn())) == -1 && (slot3 = InventoryUtil.findItemInventorySlot(Items.CHAINMAIL_CHESTPLATE, false, XCarry.INSTANCE.isOn())) == -1) {
                    slot3 = InventoryUtil.findItemInventorySlot(Items.LEATHER_CHESTPLATE, false, XCarry.INSTANCE.isOn());
                }
                if (slot3 != -1) {
                    this.queuedTaskList.add(new InventoryUtil.QueuedTask(slot3));
                    this.queuedTaskList.add(new InventoryUtil.QueuedTask(6));
                    this.queuedTaskList.add(new InventoryUtil.QueuedTask(slot3));
                    if (this.updateController.getValue()) {
                        this.queuedTaskList.add(new InventoryUtil.QueuedTask());
                    }
                }
                this.elytraTimer.reset();
            }
            AutoArmor.mc.player.inventoryContainer.getSlot(7).getStack();
            if (AutoArmor.mc.player.inventoryContainer.getSlot(7).getStack().getItem() == Items.AIR && (slot2 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.LEGS, this.curse.getValue(), XCarry.INSTANCE.isOn())) != -1) {
                this.getSlotOn(7, slot2);
            }
            AutoArmor.mc.player.inventoryContainer.getSlot(8).getStack();
            if (AutoArmor.mc.player.inventoryContainer.getSlot(8).getStack().getItem() == Items.AIR && (slot = InventoryUtil.findArmorSlot(EntityEquipmentSlot.FEET, this.curse.getValue(), XCarry.INSTANCE.isOn())) != -1) {
                this.getSlotOn(8, slot);
            }
        }
        if (this.timer.passedMs((int)((float) this.delay.getValue() * (this.tps.getValue() ? Managers.SERVER.getTpsFactor() : 1.0f)))) {
            if (!this.queuedTaskList.isEmpty()) {
                for (int i = 0; i < this.actions.getValue(); ++i) {
                    InventoryUtil.QueuedTask queuedTask = this.queuedTaskList.poll();
                    if (queuedTask == null) continue;
                    queuedTask.run();
                }
            }
            this.timer.reset();
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (AutoArmor.fullNullCheck()) {
            return;
        }
        if (Keyboard.getEventKeyState() && !(AutoArmor.mc.currentScreen instanceof Gui) && this.elytraBind.getValue().getKey() == Keyboard.getEventKey() && !this.autoElytra.getValue()) {
            boolean bl = this.elytraOn = !this.elytraOn;
        }
        if (Keyboard.getEventKeyState() && !(AutoArmor.mc.currentScreen instanceof Gui) && this.noHelmBind.getValue().getKey() == Keyboard.getEventKey()) {
            this.helmOff = !this.helmOff;
        }
    }

    private void takeOffSlot(int slot) {
        if (this.queuedTaskList.isEmpty()) {
            int target = -1;
            for (int i : InventoryUtil.findEmptySlots(XCarry.INSTANCE.isOn())) {
                if (this.doneSlots.contains(target)) continue;
                target = i;
                this.doneSlots.add(i);
            }
            if (target != -1) {
                if (target < 5 && target > 0 || !this.shiftClick.getValue()) {
                    this.queuedTaskList.add(new InventoryUtil.QueuedTask(slot));
                    this.queuedTaskList.add(new InventoryUtil.QueuedTask(target));
                } else {
                    this.queuedTaskList.add(new InventoryUtil.QueuedTask(slot, true));
                }
                if (this.updateController.getValue()) {
                    this.queuedTaskList.add(new InventoryUtil.QueuedTask());
                }
            }
        }
    }

    private void getSlotOn(int slot, int target) {
        if (this.queuedTaskList.isEmpty()) {
            this.doneSlots.remove((Object)target);
            if (target < 5 && target > 0 || !this.shiftClick.getValue()) {
                this.queuedTaskList.add(new InventoryUtil.QueuedTask(target));
                this.queuedTaskList.add(new InventoryUtil.QueuedTask(slot));
            } else {
                this.queuedTaskList.add(new InventoryUtil.QueuedTask(target, true));
            }
            if (this.updateController.getValue()) {
                this.queuedTaskList.add(new InventoryUtil.QueuedTask());
            }
        }
    }

    private boolean isSafe() {
        EntityPlayer closest = EntityUtil.getClosestEnemy(this.closestEnemy.getValue());
        if (closest == null) {
            return true;
        }
        return AutoArmor.mc.player.getDistanceSq(closest) >= MathUtil.square(this.closestEnemy.getValue());
    }
}


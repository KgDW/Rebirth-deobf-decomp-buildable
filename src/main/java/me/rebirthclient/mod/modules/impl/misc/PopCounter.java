package me.rebirthclient.mod.modules.impl.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.HashMap;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.misc.AutoEZ;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;

public class PopCounter
extends Module {
    public static final HashMap<String, Integer> TotemPopContainer = new HashMap();
    public static PopCounter INSTANCE = new PopCounter();

    public PopCounter() {
        super("PopCounter", "Counts other players totem pops", Category.MISC);
        INSTANCE = this;
    }

    @Override
    public void onDeath(EntityPlayer player) {
        if (TotemPopContainer.containsKey(player.getName())) {
            int l_Count = TotemPopContainer.get(player.getName());
            TotemPopContainer.remove(player.getName());
            if (l_Count == 1) {
                if (PopCounter.mc.player.equals(player)) {
                    if (this.isOn()) {
                        this.sendMessageWithID(ChatFormatting.BLUE + "You died after popping " + ChatFormatting.RED + l_Count + ChatFormatting.RED + " Totem!", player.getEntityId());
                    }
                    if (AutoEZ.INSTANCE.isOn() && AutoEZ.INSTANCE.whenSelf.getValue()) {
                        PopCounter.mc.player.connection.sendPacket(new CPacketChatMessage(AutoEZ.INSTANCE.SelfString.getValue()));
                    }
                } else {
                    if (this.isOn()) {
                        this.sendMessageWithID(ChatFormatting.RED + player.getName() + " died after popping " + ChatFormatting.GREEN + l_Count + ChatFormatting.RED + " Totem!", player.getEntityId());
                    }
                    if (AutoEZ.INSTANCE.isOn() && (!Managers.FRIENDS.isFriend(player.getName()) || AutoEZ.INSTANCE.whenFriend.getValue())) {
                        if (AutoEZ.INSTANCE.poped.getValue()) {
                            PopCounter.mc.player.connection.sendPacket(new CPacketChatMessage(AutoEZ.INSTANCE.EzString.getValue() + " " + player.getName() + " popping" + l_Count + " Totem!"));
                        } else {
                            PopCounter.mc.player.connection.sendPacket(new CPacketChatMessage(AutoEZ.INSTANCE.EzString.getValue() + " " + player.getName()));
                        }
                    }
                }
            } else if (PopCounter.mc.player.equals(player)) {
                if (this.isOn()) {
                    this.sendMessageWithID(ChatFormatting.BLUE + "You died after popping " + ChatFormatting.RED + l_Count + ChatFormatting.RED + " Totems!", player.getEntityId());
                }
                if (AutoEZ.INSTANCE.isOn() && AutoEZ.INSTANCE.whenSelf.getValue()) {
                    PopCounter.mc.player.connection.sendPacket(new CPacketChatMessage(AutoEZ.INSTANCE.SelfString.getValue()));
                }
            } else {
                if (this.isOn()) {
                    this.sendMessageWithID(ChatFormatting.RED + player.getName() + " died after popping " + ChatFormatting.GREEN + l_Count + ChatFormatting.RED + " Totems!", player.getEntityId());
                }
                if (AutoEZ.INSTANCE.isOn() && (!Managers.FRIENDS.isFriend(player.getName()) || AutoEZ.INSTANCE.whenFriend.getValue())) {
                    if (AutoEZ.INSTANCE.poped.getValue()) {
                        PopCounter.mc.player.connection.sendPacket(new CPacketChatMessage(AutoEZ.INSTANCE.EzString.getValue() + " " + player.getName() + " popping " + l_Count + " Totem!"));
                    } else {
                        PopCounter.mc.player.connection.sendPacket(new CPacketChatMessage(AutoEZ.INSTANCE.EzString.getValue() + " " + player.getName()));
                    }
                }
            }
        } else if (AutoEZ.INSTANCE.isOn() && (!Managers.FRIENDS.isFriend(player.getName()) || AutoEZ.INSTANCE.whenFriend.getValue())) {
            if (AutoEZ.INSTANCE.poped.getValue()) {
                PopCounter.mc.player.connection.sendPacket(new CPacketChatMessage(AutoEZ.INSTANCE.EzString.getValue() + " " + player.getName() + " popping 0 Totem!"));
            } else {
                PopCounter.mc.player.connection.sendPacket(new CPacketChatMessage(AutoEZ.INSTANCE.EzString.getValue() + " " + player.getName()));
            }
        }
    }

    @Override
    public void onTotemPop(EntityPlayer player) {
        int l_Count = 1;
        if (TotemPopContainer.containsKey(player.getName())) {
            l_Count = TotemPopContainer.get(player.getName());
            TotemPopContainer.put(player.getName(), ++l_Count);
        } else {
            TotemPopContainer.put(player.getName(), l_Count);
        }
        if (l_Count == 1) {
            if (PopCounter.mc.player.equals(player)) {
                if (this.isOn()) {
                    this.sendMessageWithID(ChatFormatting.BLUE + "You popped " + ChatFormatting.RED + l_Count + ChatFormatting.RED + " Totem.", player.getEntityId());
                }
            } else if (this.isOn()) {
                this.sendMessageWithID(ChatFormatting.RED + player.getName() + " popped " + ChatFormatting.GREEN + l_Count + ChatFormatting.RED + " Totem.", player.getEntityId());
            }
        } else if (PopCounter.mc.player.equals(player)) {
            if (this.isOn()) {
                this.sendMessageWithID(ChatFormatting.BLUE + "You popped " + ChatFormatting.RED + l_Count + ChatFormatting.RED + " Totems.", player.getEntityId());
            }
        } else if (this.isOn()) {
            this.sendMessageWithID(ChatFormatting.RED + player.getName() + " popped " + ChatFormatting.GREEN + l_Count + ChatFormatting.RED + " Totems.", player.getEntityId());
        }
    }
}


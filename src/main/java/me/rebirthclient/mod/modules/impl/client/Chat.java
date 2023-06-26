package me.rebirthclient.mod.modules.impl.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Chat
extends Module {
    public static Chat INSTANCE;
    public final Setting<Boolean> animation = this.add(new Setting<>("Animation", true));
    public final Setting<Boolean> rect = this.add(new Setting<>("Rect", true).setParent());
    public final Setting<Boolean> colorRect = this.add(new Setting<>("ColorRect", false, v -> this.rect.isOpen()));
    public final Setting<Boolean> infinite = this.add(new Setting<>("InfiniteChat", true));
    public final Setting<Boolean> suffix = this.add(new Setting<>("Suffix", false).setParent());
    private final Setting<String> suffixString = this.add(new Setting<>("suffixString", "| NewRebirth", v -> this.suffix.isOpen()));
    public final Setting<Boolean> time = this.add(new Setting<>("TimeStamps", false).setParent());
    public final Setting<Bracket> bracket = this.add(new Setting<>("Bracket", Bracket.TRIANGLE, v -> this.time.isOpen()));
    public final Setting<Color> color = this.add(new Setting<>("Color", new Color(134, 173, 255, 255)).hideAlpha());

    public Chat() {
        super("Chat", "Modifies your chat", Category.CLIENT);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (Chat.fullNullCheck()) {
            return;
        }
        if (this.suffix.getValue() && event.getPacket() instanceof CPacketChatMessage) {
            CPacketChatMessage packet = event.getPacket();
            String message = packet.getMessage();
            if (message.startsWith("/") || message.startsWith("!") || message.endsWith(this.suffixString.getValue())) {
                return;
            }
            if ((message = message + " " + this.suffixString.getValue()).length() >= 256) {
                message = message.substring(0, 256);
            }
            packet.message = message;
        }
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        if (Chat.fullNullCheck()) {
            return;
        }
        if (this.time.getValue()) {
            Date date = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm");
            String strDate = dateFormatter.format(date);
            String leBracket1 = this.bracket.getValue() == Bracket.TRIANGLE ? "<" : "[";
            String leBracket2 = this.bracket.getValue() == Bracket.TRIANGLE ? ">" : "]";
            TextComponentString time = new TextComponentString(ChatFormatting.GRAY + leBracket1 + ChatFormatting.WHITE + strDate + ChatFormatting.GRAY + leBracket2 + ChatFormatting.RESET + " ");
            event.setMessage(time.appendSibling(event.getMessage()));
        }
    }

    private static enum Bracket {
        SQUARE,
        TRIANGLE

    }
}


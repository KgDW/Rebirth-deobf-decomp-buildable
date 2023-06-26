package me.rebirthclient.mod.modules.impl.misc;

import java.util.HashMap;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.commands.Command;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiSpam
extends Module {
    public static AntiSpam INSTANCE = new AntiSpam();
    private final Setting<Boolean> number = this.add(new Setting<>("Number", true));
    private static final HashMap<String, text> StringMap = new HashMap();
    int size = 2;

    public AntiSpam() {
        super("AntiSpam", "Anti Spam", Category.MISC);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (AntiSpam.fullNullCheck()) {
            return;
        }
        if (event.isCanceled()) {
            return;
        }
        if (!(event.getPacket() instanceof SPacketChat)) {
            return;
        }
        event.setCanceled(true);
        ITextComponent component = ((SPacketChat)event.getPacket()).getChatComponent();
        String message = component.getFormattedText();
        if (StringMap.containsKey(message)) {
            StringMap.get(message).addNumber();
            AntiSpam.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new Command.ChatMessage(message + (this.number.getValue() ? " \u00a77x" + AntiSpam.StringMap.get(message).number : "")), AntiSpam.StringMap.get(message).size);
            return;
        }
        ++this.size;
        AntiSpam.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(component, this.size);
        StringMap.put(message, new text(this.size));
    }

    private static class text {
        int number = 1;
        final int size;

        public text(int size) {
            this.size = size;
        }

        public void addNumber() {
            ++this.number;
        }
    }
}


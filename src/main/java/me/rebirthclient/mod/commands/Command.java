package me.rebirthclient.mod.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.modules.impl.hud.Notifications;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;

public abstract class Command
extends Mod {
    protected final String name;
    protected final String[] commands;

    public Command(String name) {
        super(name);
        this.name = name;
        this.commands = new String[]{""};
    }

    public Command(String name, String[] commands) {
        super(name);
        this.name = name;
        this.commands = commands;
    }

    public static void sendMessage(String message) {
        Notifications.notifyList.add(new Notifications.Notifys(message));
        Command.sendSilentMessage(Managers.TEXT.getPrefix() + ChatFormatting.GRAY + message);
    }

    public static void sendSilentMessage(String message) {
        if (Command.nullCheck()) {
            return;
        }
        Command.mc.player.sendMessage(new ChatMessage(message));
    }

    public static String getCommandPrefix() {
        return Managers.COMMANDS.getCommandPrefix();
    }

    public static void sendMessageWithID(String message, int id) {
        if (!Command.nullCheck()) {
            Command.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatMessage(Managers.TEXT.getPrefix() + ChatFormatting.GRAY + message), id);
        }
    }

    public abstract void execute(String[] var1);

    public String complete(String str) {
        if (this.name.toLowerCase().startsWith(str)) {
            return this.name;
        }
        for (String command : this.commands) {
            if (!command.toLowerCase().startsWith(str)) continue;
            return command;
        }
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String[] getCommands() {
        return this.commands;
    }

    public static class ChatMessage
    extends TextComponentBase {
        private final String text;

        public ChatMessage(String text2) {
            Pattern pattern = Pattern.compile("&[0123456789abcdefrlosmk]");
            Matcher matcher = pattern.matcher(text2);
            StringBuffer stringBuffer = new StringBuffer();
            while (matcher.find()) {
                String replacement = matcher.group().substring(1);
                matcher.appendReplacement(stringBuffer, replacement);
            }
            matcher.appendTail(stringBuffer);
            this.text = stringBuffer.toString();
        }

        public String getUnformattedComponentText() {
            return this.text;
        }

        public ITextComponent createCopy() {
            return null;
        }

        public ITextComponent shallowCopy() {
            return new ChatMessage(this.text);
        }
    }
}


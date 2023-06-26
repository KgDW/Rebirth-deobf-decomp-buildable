package me.rebirthclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.commands.Command;

public class PrefixCommand
extends Command {
    public PrefixCommand() {
        super("prefix", new String[]{"<char>"});
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage(ChatFormatting.GREEN + "The current prefix is " + Managers.COMMANDS.getCommandPrefix());
            return;
        }
        Managers.COMMANDS.setPrefix(commands[0]);
        Command.sendMessage("Prefix changed to " + ChatFormatting.GRAY + commands[0]);
    }
}


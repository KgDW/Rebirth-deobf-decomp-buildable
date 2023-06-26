package me.rebirthclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.commands.Command;

public class HelpCommand
extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void execute(String[] commands) {
        HelpCommand.sendMessage("Commands: ");
        for (Command command : Managers.COMMANDS.getCommands()) {
            HelpCommand.sendMessage(ChatFormatting.GRAY + Managers.COMMANDS.getCommandPrefix() + command.getName());
        }
    }
}


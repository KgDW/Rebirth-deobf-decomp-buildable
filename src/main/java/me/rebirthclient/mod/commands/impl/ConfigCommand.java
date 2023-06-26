package me.rebirthclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.commands.Command;

public class ConfigCommand
extends Command {
    public ConfigCommand() {
        super("config", new String[]{"<save/load>"});
    }

    @Override
    public void execute(final String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage("You`ll find the config files in your gameProfile directory under Rebirth/config");
            return;
        }
        if (commands.length == 2) {
            if ("list".equals(commands[0])) {
                String configs = "Configs: ";
                final File file = new File("Rebirth/");
                final List<File> directories = Arrays.stream(file.listFiles()).filter(File::isDirectory).filter(f -> !f.getName().equals("util")).collect(Collectors.toList());
                final StringBuilder builder = new StringBuilder(configs);
                for (final File file2 : directories) {
                    builder.append(file2.getName() + ", ");
                }
                configs = builder.toString();
                Command.sendMessage(configs);
            }
            else {
                Command.sendMessage("Not a valid command... Possible usage: <list>");
            }
        }
        if (commands.length >= 3) {
            final String s = commands[0];
            switch (s) {
                case "save": {
                    Managers.CONFIGS.saveConfig(commands[1]);
                    Command.sendMessage(ChatFormatting.GREEN + "Config '" + commands[1] + "' has been saved.");
                    return;
                }
                case "load": {
                    if (Managers.CONFIGS.configExists(commands[1])) {
                        Managers.CONFIGS.loadConfig(commands[1]);
                        Command.sendMessage(ChatFormatting.GREEN + "Config '" + commands[1] + "' has been loaded.");
                    }
                    else {
                        Command.sendMessage(ChatFormatting.RED + "Config '" + commands[1] + "' does not exist.");
                    }
                    return;
                }
                default: {
                    Command.sendMessage("Not a valid command... Possible usage: <save/load>");
                    break;
                }
            }
        }
    }
}


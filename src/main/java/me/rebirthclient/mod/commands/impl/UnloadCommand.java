package me.rebirthclient.mod.commands.impl;

import me.rebirthclient.Rebirth;
import me.rebirthclient.mod.commands.Command;

public class UnloadCommand
extends Command {
    public UnloadCommand() {
        super("unload", new String[0]);
    }

    @Override
    public void execute(String[] commands) {
        Rebirth.unload(true);
    }
}


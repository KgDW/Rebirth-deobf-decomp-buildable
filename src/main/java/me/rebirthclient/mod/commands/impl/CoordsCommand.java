package me.rebirthclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import me.rebirthclient.mod.commands.Command;

public class CoordsCommand
extends Command {
    String coords;

    public CoordsCommand() {
        super("coords");
    }

    @Override
    public void execute(String[] commands) {
        int posX = (int)CoordsCommand.mc.player.posX;
        int posY = (int)CoordsCommand.mc.player.posY;
        int posZ = (int)CoordsCommand.mc.player.posZ;
        String myString = this.coords = "X: " + posX + " Y: " + posY + " Z: " + posZ;
        StringSelection stringSelection = new StringSelection(myString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        Command.sendMessage(ChatFormatting.GRAY + "Coords copied.");
    }
}


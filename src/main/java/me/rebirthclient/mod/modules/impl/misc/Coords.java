package me.rebirthclient.mod.modules.impl.misc;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;

public class Coords
extends Module {
    public Coords() {
        super("Coords", "copies your current position to the clipboard", Category.MISC);
    }

    @Override
    public void onEnable() {
        int posX = (int)Coords.mc.player.posX;
        int posY = (int)Coords.mc.player.posY;
        int posZ = (int)Coords.mc.player.posZ;
        String coords = "X: " + posX + " Y: " + posY + " Z: " + posZ;
        StringSelection stringSelection = new StringSelection(coords);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        this.disable();
    }
}


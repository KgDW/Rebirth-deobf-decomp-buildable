package me.rebirthclient.api.events.impl;

import java.awt.Color;
import me.rebirthclient.api.events.Event;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class RenderSkyEvent
extends Event {
    private Color color;

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}


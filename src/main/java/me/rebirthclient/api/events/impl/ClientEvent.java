package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class ClientEvent
extends Event {
    private Mod mod;
    private Setting setting;

    public ClientEvent(int stage, Mod mod) {
        super(stage);
        this.mod = mod;
    }

    public ClientEvent(Setting setting) {
        super(2);
        this.setting = setting;
    }

    public Mod getMod() {
        return this.mod;
    }

    public Setting getSetting() {
        return this.setting;
    }
}


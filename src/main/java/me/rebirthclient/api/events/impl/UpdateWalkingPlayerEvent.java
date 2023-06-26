package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;

public class UpdateWalkingPlayerEvent
extends Event {
    public UpdateWalkingPlayerEvent(int stage) {
        super(stage);
    }
}


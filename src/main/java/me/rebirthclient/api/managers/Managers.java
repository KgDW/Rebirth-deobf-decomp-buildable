package me.rebirthclient.api.managers;

import me.rebirthclient.api.managers.impl.BreakManager;
import me.rebirthclient.api.managers.impl.ColorManager;
import me.rebirthclient.api.managers.impl.CommandManager;
import me.rebirthclient.api.managers.impl.ConfigManager;
import me.rebirthclient.api.managers.impl.EventManager;
import me.rebirthclient.api.managers.impl.FileManager;
import me.rebirthclient.api.managers.impl.FpsManager;
import me.rebirthclient.api.managers.impl.FriendManager;
import me.rebirthclient.api.managers.impl.InteractionManager;
import me.rebirthclient.api.managers.impl.ModuleManager;
import me.rebirthclient.api.managers.impl.PositionManager;
import me.rebirthclient.api.managers.impl.ReloadManager;
import me.rebirthclient.api.managers.impl.RotationManager;
import me.rebirthclient.api.managers.impl.ServerManager;
import me.rebirthclient.api.managers.impl.SneakManager;
import me.rebirthclient.api.managers.impl.SpeedManager;
import me.rebirthclient.api.managers.impl.TextManager;
import me.rebirthclient.api.managers.impl.TimerManager;

public class Managers {
    public static InteractionManager INTERACTIONS;
    public static RotationManager ROTATIONS;
    public static CommandManager COMMANDS;
    public static ModuleManager MODULES;
    public static ConfigManager CONFIGS;
    public static FriendManager FRIENDS;
    public static ColorManager COLORS;
    public static EventManager EVENTS;
    public static FileManager FILES;
    public static PositionManager POSITION;
    public static ReloadManager RELOAD;
    public static ServerManager SERVER;
    public static TimerManager TIMER;
    public static SpeedManager SPEED;
    public static TextManager TEXT;
    public static FpsManager FPS;
    public static SneakManager SNEAK;
    public static BreakManager BREAK;
    private static boolean loaded;

    public static void load() {
        loaded = true;
        if (RELOAD != null) {
            RELOAD.unload();
            RELOAD = null;
        }
        EVENTS = new EventManager();
        TEXT = new TextManager();
        INTERACTIONS = new InteractionManager();
        ROTATIONS = new RotationManager();
        POSITION = new PositionManager();
        COMMANDS = new CommandManager();
        CONFIGS = new ConfigManager();
        MODULES = new ModuleManager();
        FRIENDS = new FriendManager();
        SERVER = new ServerManager();
        COLORS = new ColorManager();
        SPEED = new SpeedManager();
        TIMER = new TimerManager();
        FILES = new FileManager();
        FPS = new FpsManager();
        SNEAK = new SneakManager();
        BREAK = new BreakManager();
        MODULES.init();
        CONFIGS.init();
        EVENTS.init();
        TEXT.init();
        SNEAK.init();
        MODULES.onLoad();
    }

    public static void unload(boolean force) {
        if (force) {
            RELOAD = new ReloadManager();
            RELOAD.init(COMMANDS != null ? COMMANDS.getCommandPrefix() : ".");
        }
        Managers.onUnload();
        INTERACTIONS = null;
        ROTATIONS = null;
        POSITION = null;
        COMMANDS = null;
        CONFIGS = null;
        MODULES = null;
        FRIENDS = null;
        SERVER = null;
        COLORS = null;
        EVENTS = null;
        SPEED = null;
        TIMER = null;
        FILES = null;
        TEXT = null;
        FPS = null;
    }

    public static void onUnload() {
        if (Managers.isLoaded()) {
            EVENTS.onUnload();
            MODULES.onUnloadPre();
            CONFIGS.saveConfig(Managers.CONFIGS.config.replaceFirst("Rebirth/", ""));
            MODULES.onUnloadPost();
            loaded = false;
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }

    static {
        loaded = true;
    }
}


package me.rebirthclient.mod.modules.impl.client;

import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import org.lwjgl.opengl.Display;

public class Title
extends Module {
    public static Title INSTANCE = new Title();
    public final Setting<String> title = this.add(new Setting<>("Title", "Rebirth alpha"));
    public final Setting<Boolean> animation = this.add(new Setting<>("Animation", true).setParent());
    public final Setting<Integer> updateTime = this.add(new Setting<>("updateTime", 300, 0, 1000, v -> this.animation.isOpen()));
    private static final Timer updateTimer = new Timer();
    private static int titleLength = 0;
    private static int breakTimer = 0;
    private static String lastTitle;
    private static boolean back;
    private static boolean original;

    public Title() {
        super("Title", "Change client title", Category.CLIENT);
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        Display.setTitle("Minecraft 1.12.2");
    }

    public static void updateTitle() {
        if (!original) {
            Display.setTitle("Minecraft 1.12.2");
            original = true;
        }
        if (INSTANCE.isOff()) {
            return;
        }
        if (lastTitle != null && !lastTitle.equals(Title.INSTANCE.title.getValue())) {
            updateTimer.reset();
            titleLength = 0;
            breakTimer = 0;
            back = false;
        }
        lastTitle = Title.INSTANCE.title.getValue();
        if (Title.INSTANCE.animation.getValue()) {
            if (lastTitle != null && updateTimer.passedMs(Title.INSTANCE.updateTime.getValue())) {
                updateTimer.reset();
                Display.setTitle(lastTitle.substring(0, lastTitle.length() - titleLength));
                if (titleLength == lastTitle.length() && breakTimer != 2 || titleLength == 0 && breakTimer != 4) {
                    ++breakTimer;
                    return;
                }
                breakTimer = 0;
                if (titleLength == lastTitle.length()) {
                    back = true;
                }
                titleLength = back ? --titleLength : ++titleLength;
                if (titleLength == 0) {
                    back = false;
                }
            }
        } else {
            Display.setTitle(lastTitle);
        }
    }

    static {
        back = false;
        original = false;
    }
}


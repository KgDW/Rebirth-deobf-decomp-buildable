package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;

public class TabFriends
extends Module {
    public static String color = "";
    public static TabFriends INSTANCE;
    public static Setting<Boolean> prefix;
    public final Setting<FriendColor> mode = this.add(new Setting<>("Color", FriendColor.Green));

    public TabFriends() {
        super("TabFriends", "Renders your friends differently in the tablist", Category.MISC);
        prefix = this.add(new Setting<>("Prefix", true));
        INSTANCE = this;
    }

    public static String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn) {
        String name;
        String string = name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
        if (Managers.FRIENDS.isFriend(name)) {
            if (prefix.getValue()) {
                return "\u00a77[" + color + "F\u00a77] " + color + name;
            }
            return color + name;
        }
        return name;
    }

    @Override
    public void onUpdate() {
        switch (this.mode.getValue()) {
            case White: {
                color = "\u00a7f";
                break;
            }
            case DarkRed: {
                color = "\u00a74";
                break;
            }
            case Red: {
                color = "\u00a7c";
                break;
            }
            case Gold: {
                color = "\u00a76";
                break;
            }
            case Yellow: {
                color = "\u00a7e";
                break;
            }
            case DarkGreen: {
                color = "\u00a72";
                break;
            }
            case Green: {
                color = "\u00a7a";
                break;
            }
            case Aqua: {
                color = "\u00a7b";
                break;
            }
            case DarkAqua: {
                color = "\u00a73";
                break;
            }
            case DarkBlue: {
                color = "\u00a71";
                break;
            }
            case Blue: {
                color = "\u00a79";
                break;
            }
            case LightPurple: {
                color = "\u00a7d";
                break;
            }
            case DarkPurple: {
                color = "\u00a75";
                break;
            }
            case Gray: {
                color = "\u00a77";
                break;
            }
            case DarkGray: {
                color = "\u00a78";
                break;
            }
            case Black: {
                color = "\u00a70";
                break;
            }
            case None: {
                color = "";
            }
        }
    }

    public static enum FriendColor {
        DarkRed,
        Red,
        Gold,
        Yellow,
        DarkGreen,
        Green,
        Aqua,
        DarkAqua,
        DarkBlue,
        Blue,
        LightPurple,
        DarkPurple,
        Gray,
        DarkGray,
        Black,
        White,
        None

    }
}


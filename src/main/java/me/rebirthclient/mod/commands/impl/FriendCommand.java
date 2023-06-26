package me.rebirthclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.managers.impl.FriendManager;
import me.rebirthclient.mod.commands.Command;

public class FriendCommand
extends Command {
    public FriendCommand() {
        super("friend", new String[]{"<add/del/name/clear>", "<name>"});
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            if (Managers.FRIENDS.getFriends().isEmpty()) {
                FriendCommand.sendMessage("Friend list empty D:.");
            } else {
                String f = "Friends: ";
                for (FriendManager.Friend friend : Managers.FRIENDS.getFriends()) {
                    try {
                        f = f + friend.getUsername() + ", ";
                    }
                    catch (Exception exception) {}
                }
                FriendCommand.sendMessage(f);
            }
            return;
        }
        if (commands.length == 2) {
            if ("reset".equals(commands[0])) {
                Managers.FRIENDS.onLoad();
                FriendCommand.sendMessage("Friends got reset.");
                return;
            }
            FriendCommand.sendMessage(commands[0] + (Managers.FRIENDS.isFriend(commands[0]) ? " is friended." : " isn't friended."));
            return;
        }
        if (commands.length >= 2) {
            switch (commands[0]) {
                case "add": {
                    Managers.FRIENDS.addFriend(commands[1]);
                    FriendCommand.sendMessage(ChatFormatting.GREEN + commands[1] + " has been friended");
                    return;
                }
                case "del": {
                    Managers.FRIENDS.removeFriend(commands[1]);
                    FriendCommand.sendMessage(ChatFormatting.RED + commands[1] + " has been unfriended");
                    return;
                }
            }
            FriendCommand.sendMessage("Unknown Command, try friend add/del (name)");
        }
    }
}


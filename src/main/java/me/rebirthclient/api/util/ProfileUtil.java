package me.rebirthclient.api.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.util.UUIDTypeAdapter;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.commands.Command;
import net.minecraft.client.network.NetworkPlayerInfo;

public class ProfileUtil
implements Wrapper {
    public static String getStringFromStream(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "/";
    }

    public static UUID getUUIDFromName(String name) {
        try {
            UUIDFinder process = new UUIDFinder(name);
            Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getUUID();
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String requestIDs(String data) {
        try {
            String query = "https://api.mojang.com/profiles/minecraft";
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes(StandardCharsets.UTF_8));
            os.close();
            BufferedInputStream stream = new BufferedInputStream(conn.getInputStream());
            String retval = ProfileUtil.getStringFromStream(stream);
            ((InputStream)stream).close();
            conn.disconnect();
            return retval;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static class UUIDFinder
    implements Runnable {
        private final String name;
        private volatile UUID uuid;

        public UUIDFinder(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            NetworkPlayerInfo profile = null;
            try {
                List<NetworkPlayerInfo> infoMap = new ArrayList<>(Objects.requireNonNull(Wrapper.mc.getConnection()).getPlayerInfoMap());
                profile = infoMap.stream()
                        .filter(networkPlayerInfo -> networkPlayerInfo.getGameProfile().getName().equalsIgnoreCase(this.name))
                        .findFirst().orElse(null);

                if (profile != null) {
                    this.uuid = profile.getGameProfile().getId();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Command.sendMessage("Player isn't online. Looking up UUID..");
            String s = ProfileUtil.requestIDs("[\"" + this.name + "\"]");
            if (s == null || s.isEmpty()) {
                Command.sendMessage("Couldn't find player ID. Are you connected to the internet? (0)");
                return;
            }

            try {
                JsonElement element = new JsonParser().parse(s);
                if (element.getAsJsonArray().size() == 0) {
                    Command.sendMessage("Couldn't find player ID. (1)");
                } else {
                    String id = element.getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
                    this.uuid = UUIDTypeAdapter.fromString(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Command.sendMessage("Couldn't find player ID. (2)");
            }
        }


        public UUID getUUID() {
            return this.uuid;
        }

        public String getName() {
            return this.name;
        }
    }
}


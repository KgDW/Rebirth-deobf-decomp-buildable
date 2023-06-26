package me.rebirthclient.api.managers.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;
import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.managers.impl.FriendManager;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.commands.Command;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.EnumConverter;
import me.rebirthclient.mod.modules.settings.Setting;

public class ConfigManager
implements Wrapper {
    public final ArrayList<Mod> mods = new ArrayList();
    public String config = "Rebirth/config/";

    public static void setValueFromJson(Mod mod, Setting setting, JsonElement element) {
        switch (setting.getType()) {
            case "Boolean": {
                setting.setValue(element.getAsBoolean());
                return;
            }
            case "Double": {
                setting.setValue(element.getAsDouble());
                return;
            }
            case "Float": {
                setting.setValue(element.getAsFloat());
                return;
            }
            case "Integer": {
                setting.setValue(element.getAsInt());
                return;
            }
            case "String": {
                try {
                    String str = element.getAsString();
                    setting.setValue(str.replace("_", " "));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return;
            }
            case "Bind": {
                try {
                    setting.setValue(new Bind.BindConverter().doBackward(element));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return;
            }
            case "Enum": {
                try {
                    EnumConverter converter = new EnumConverter(((Enum)setting.getValue()).getClass());
                    Enum value = converter.doBackward(element);
                    setting.setValue(value == null ? setting.getDefaultValue() : value);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return;
            }
            case "Color": {
                try {
                    if (setting.hasBoolean) {
                        setting.injectBoolean(element.getAsBoolean());
                    }
                    try {
                        setting.setValue(new Color(element.getAsInt(), true));
                    }
                    catch (Exception exception) {}
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return;
            }
        }
        Rebirth.LOGGER.error("Unknown Setting type for: " + mod.getName() + " : " + setting.getName());
    }

    private static void loadFile(JsonObject input, Mod mod) {
        for (Map.Entry entry : input.entrySet()) {
            String settingName = (String)entry.getKey();
            JsonElement element = (JsonElement)entry.getValue();
            if (mod instanceof FriendManager) {
                try {
                    Managers.FRIENDS.addFriend(new FriendManager.Friend(element.getAsString(), UUID.fromString(settingName)));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                continue;
            }
            for (Setting setting : mod.getSettings()) {
                if (settingName.equals(setting.getName())) {
                    try {
                        ConfigManager.setValueFromJson(mod, setting, element);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (settingName.equals("should" + setting.getName())) {
                    try {
                        ConfigManager.setValueFromJson(mod, setting, element);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (!settingName.equals("Rainbow" + setting.getName())) continue;
                if (!Mod.fullNullCheck()) {
                    Command.sendMessage("rainbow test" + element.getAsBoolean());
                }
                setting.isRainbow = element.getAsBoolean();
            }
        }
    }

    public void loadConfig(String name) {
        final List<File> files = Arrays.stream(Objects.requireNonNull(new File("Rebirth").listFiles())).filter(File::isDirectory).collect(Collectors.toList());
        this.config = files.contains(new File("Rebirth/" + name + "/")) ? "Rebirth/" + name + "/" : "Rebirth/config/";
        Managers.FRIENDS.onLoad();
        for (Mod mod : this.mods) {
            try {
                this.loadSettings(mod);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.saveCurrentConfig();
    }

    public boolean configExists(String name) {
        final List<File> files = Arrays.stream(Objects.requireNonNull(new File("Rebirth").listFiles())).filter(File::isDirectory).collect(Collectors.toList());
        return files.contains(new File("Rebirth/" + name + "/"));
    }

    public void saveConfig(String name) {
        this.config = "Rebirth/" + name + "/";
        File path = new File(this.config);
        if (!path.exists()) {
            path.mkdir();
        }
        Managers.FRIENDS.saveFriends();
        for (Mod mod : this.mods) {
            try {
                this.saveSettings(mod);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.saveCurrentConfig();
    }

    public void saveCurrentConfig() {
        File currentConfig = new File("Rebirth/currentconfig.txt");
        try {
            if (currentConfig.exists()) {
                FileWriter writer = new FileWriter(currentConfig);
                String tempConfig = this.config.replaceAll("/", "");
                writer.write(tempConfig.replaceAll("Rebirth", ""));
                writer.close();
            } else {
                currentConfig.createNewFile();
                FileWriter writer = new FileWriter(currentConfig);
                String tempConfig = this.config.replaceAll("/", "");
                writer.write(tempConfig.replaceAll("Rebirth", ""));
                writer.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String loadCurrentConfig() {
        File currentConfig = new File("Rebirth/currentconfig.txt");
        String name = "config";
        try {
            if (currentConfig.exists()) {
                Scanner reader = new Scanner(currentConfig);
                while (reader.hasNextLine()) {
                    name = reader.nextLine();
                }
                reader.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public void saveSettings(Mod mod) throws IOException {
        String modName;
        Path outputFile;
        new JsonObject();
        File directory = new File(this.config + this.getDirectory(mod));
        if (!directory.exists()) {
            directory.mkdir();
        }
        if (!Files.exists(outputFile = Paths.get(modName = this.config + this.getDirectory(mod) + mod.getName() + ".json"))) {
            Files.createFile(outputFile);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this.writeSettings(mod));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outputFile)));
        writer.write(json);
        writer.close();
    }

    public void init() {
        this.mods.addAll(Managers.MODULES.modules);
        this.mods.add(Managers.FRIENDS);
        String name = this.loadCurrentConfig();
        this.loadConfig(name);
        Rebirth.LOGGER.info("Config loaded.");
    }

    private void loadSettings(Mod mod) throws IOException {
        String modName = this.config + this.getDirectory(mod) + mod.getName() + ".json";
        Path modPath = Paths.get(modName);
        if (!Files.exists(modPath)) {
            return;
        }
        this.loadPath(modPath, mod);
    }

    private void loadPath(Path path, Mod mod) throws IOException {
        InputStream stream = Files.newInputStream(path);
        try {
            ConfigManager.loadFile(new JsonParser().parse(new InputStreamReader(stream)).getAsJsonObject(), mod);
        }
        catch (IllegalStateException e) {
            Rebirth.LOGGER.error("Bad Config File for: " + mod.getName() + ". Resetting...");
            ConfigManager.loadFile(new JsonObject(), mod);
        }
        stream.close();
    }

    public JsonObject writeSettings(Mod mod) {
        JsonObject object = new JsonObject();
        JsonParser jp = new JsonParser();
        for (Setting setting : mod.getSettings()) {
            if (setting.getValue() instanceof Color) {
                object.add(setting.getName(), jp.parse(String.valueOf(((Color)setting.getValue()).getRGB())));
                if (setting.hasBoolean) {
                    object.add("should" + setting.getName(), jp.parse(String.valueOf(setting.booleanValue)));
                }
                object.add("Rainbow" + setting.getName(), jp.parse(String.valueOf(setting.isRainbow)));
                continue;
            }
            if (setting.isEnumSetting()) {
                EnumConverter converter = new EnumConverter(((Enum)setting.getValue()).getClass());
                object.add(setting.getName(), converter.doForward((Enum)setting.getValue()));
                continue;
            }
            if (setting.isStringSetting()) {
                String str = (String)setting.getValue();
                setting.setValue(str.replace(" ", "_"));
            }
            try {
                object.add(setting.getName(), jp.parse(setting.getValue().toString()));
            }
            catch (Exception exception) {}
        }
        return object;
    }

    public String getDirectory(Mod mod) {
        String directory = "";
        if (mod instanceof Module) {
            directory = directory + ((Module)mod).getCategory().getName() + "/";
        }
        return directory;
    }
}


package me.rebirthclient.api.managers.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.gui.screen.Gui;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.client.Appearance;
import me.rebirthclient.mod.modules.impl.client.ArrayList;
import me.rebirthclient.mod.modules.impl.client.Chat;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.impl.client.Desktop;
import me.rebirthclient.mod.modules.impl.client.FakeFPS;
import me.rebirthclient.mod.modules.impl.client.FontMod;
import me.rebirthclient.mod.modules.impl.client.FovMod;
import me.rebirthclient.mod.modules.impl.client.GuiAnimation;
import me.rebirthclient.mod.modules.impl.client.HUD;
import me.rebirthclient.mod.modules.impl.client.NameProtect;
import me.rebirthclient.mod.modules.impl.client.Title;
import me.rebirthclient.mod.modules.impl.client.UnfocusedCPU;
import me.rebirthclient.mod.modules.impl.combat.AntiBurrow;
import me.rebirthclient.mod.modules.impl.combat.AntiCity;
import me.rebirthclient.mod.modules.impl.combat.AntiPiston;
import me.rebirthclient.mod.modules.impl.combat.AntiRegear;
import me.rebirthclient.mod.modules.impl.combat.AntiWeak;
import me.rebirthclient.mod.modules.impl.combat.AnvilAura;
import me.rebirthclient.mod.modules.impl.combat.Aura;
import me.rebirthclient.mod.modules.impl.combat.AutoArmor;
import me.rebirthclient.mod.modules.impl.combat.AutoCity;
import me.rebirthclient.mod.modules.impl.combat.AutoPush;
import me.rebirthclient.mod.modules.impl.combat.AutoReplenish;
import me.rebirthclient.mod.modules.impl.combat.AutoTotem;
import me.rebirthclient.mod.modules.impl.combat.AutoTrap;
import me.rebirthclient.mod.modules.impl.combat.AutoWeb;
import me.rebirthclient.mod.modules.impl.combat.AutoWire;
import me.rebirthclient.mod.modules.impl.combat.Burrow;
import me.rebirthclient.mod.modules.impl.combat.CatCrystal;
import me.rebirthclient.mod.modules.impl.combat.CityRecode;
import me.rebirthclient.mod.modules.impl.combat.CombatSetting;
import me.rebirthclient.mod.modules.impl.combat.Criticals;
import me.rebirthclient.mod.modules.impl.combat.CrystalBot;
import me.rebirthclient.mod.modules.impl.combat.Filler;
import me.rebirthclient.mod.modules.impl.combat.HoleFiller;
import me.rebirthclient.mod.modules.impl.combat.ObiPlacer;
import me.rebirthclient.mod.modules.impl.combat.PacketExp;
import me.rebirthclient.mod.modules.impl.combat.PacketMine;
import me.rebirthclient.mod.modules.impl.combat.PistonCrystal;
import me.rebirthclient.mod.modules.impl.combat.PullCrystal;
import me.rebirthclient.mod.modules.impl.combat.SelfWeb;
import me.rebirthclient.mod.modules.impl.combat.Surround;
import me.rebirthclient.mod.modules.impl.combat.TestPush;
import me.rebirthclient.mod.modules.impl.combat.TrapSelf;
import me.rebirthclient.mod.modules.impl.combat.WebTrap;
import me.rebirthclient.mod.modules.impl.exploit.BetterPortal;
import me.rebirthclient.mod.modules.impl.exploit.Blink;
import me.rebirthclient.mod.modules.impl.exploit.Clip;
import me.rebirthclient.mod.modules.impl.exploit.Crasher;
import me.rebirthclient.mod.modules.impl.exploit.FakePearl;
import me.rebirthclient.mod.modules.impl.exploit.GhostHand;
import me.rebirthclient.mod.modules.impl.exploit.GodMode;
import me.rebirthclient.mod.modules.impl.exploit.LiquidInteract;
import me.rebirthclient.mod.modules.impl.exploit.MultiTask;
import me.rebirthclient.mod.modules.impl.exploit.NoHitBox;
import me.rebirthclient.mod.modules.impl.exploit.NoInteract;
import me.rebirthclient.mod.modules.impl.exploit.PacketFly;
import me.rebirthclient.mod.modules.impl.exploit.PearlSpoof;
import me.rebirthclient.mod.modules.impl.exploit.Phase;
import me.rebirthclient.mod.modules.impl.exploit.Stresser;
import me.rebirthclient.mod.modules.impl.exploit.SuperBow;
import me.rebirthclient.mod.modules.impl.exploit.SuperThrow;
import me.rebirthclient.mod.modules.impl.exploit.TPCoordLog;
import me.rebirthclient.mod.modules.impl.exploit.XCarry;
import me.rebirthclient.mod.modules.impl.hud.BindList;
import me.rebirthclient.mod.modules.impl.hud.InventoryPreview;
import me.rebirthclient.mod.modules.impl.hud.Notifications;
import me.rebirthclient.mod.modules.impl.hud.TargetHUD;
import me.rebirthclient.mod.modules.impl.misc.AntiNullPointer;
import me.rebirthclient.mod.modules.impl.misc.AntiSpam;
import me.rebirthclient.mod.modules.impl.misc.AutoEZ;
import me.rebirthclient.mod.modules.impl.misc.AutoKit;
import me.rebirthclient.mod.modules.impl.misc.AutoLogin;
import me.rebirthclient.mod.modules.impl.misc.AutoReconnect;
import me.rebirthclient.mod.modules.impl.misc.AutoTNT;
import me.rebirthclient.mod.modules.impl.misc.Coords;
import me.rebirthclient.mod.modules.impl.misc.Debug;
import me.rebirthclient.mod.modules.impl.misc.ExtraTab;
import me.rebirthclient.mod.modules.impl.misc.FakePlayer;
import me.rebirthclient.mod.modules.impl.misc.GhastNotifier;
import me.rebirthclient.mod.modules.impl.misc.KillEffects;
import me.rebirthclient.mod.modules.impl.misc.LightningDetect;
import me.rebirthclient.mod.modules.impl.misc.MCF;
import me.rebirthclient.mod.modules.impl.misc.Message;
import me.rebirthclient.mod.modules.impl.misc.PearlNotify;
import me.rebirthclient.mod.modules.impl.misc.Peek;
import me.rebirthclient.mod.modules.impl.misc.PopCounter;
import me.rebirthclient.mod.modules.impl.misc.SilentDisconnect;
import me.rebirthclient.mod.modules.impl.misc.TNTTime;
import me.rebirthclient.mod.modules.impl.misc.TabFriends;
import me.rebirthclient.mod.modules.impl.misc.ToolTips;
import me.rebirthclient.mod.modules.impl.movement.AntiGlide;
import me.rebirthclient.mod.modules.impl.movement.AntiVoid;
import me.rebirthclient.mod.modules.impl.movement.AntiWeb;
import me.rebirthclient.mod.modules.impl.movement.AutoCenter;
import me.rebirthclient.mod.modules.impl.movement.AutoWalk;
import me.rebirthclient.mod.modules.impl.movement.ElytraFly;
import me.rebirthclient.mod.modules.impl.movement.FastFall;
import me.rebirthclient.mod.modules.impl.movement.FastSwim;
import me.rebirthclient.mod.modules.impl.movement.FastWeb;
import me.rebirthclient.mod.modules.impl.movement.Flight;
import me.rebirthclient.mod.modules.impl.movement.HoleSnap;
import me.rebirthclient.mod.modules.impl.movement.InventoryMove;
import me.rebirthclient.mod.modules.impl.movement.LongJump;
import me.rebirthclient.mod.modules.impl.movement.NewStep;
import me.rebirthclient.mod.modules.impl.movement.NoJumpDelay;
import me.rebirthclient.mod.modules.impl.movement.NoSlowDown;
import me.rebirthclient.mod.modules.impl.movement.SafeWalk;
import me.rebirthclient.mod.modules.impl.movement.Scaffold;
import me.rebirthclient.mod.modules.impl.movement.Speed;
import me.rebirthclient.mod.modules.impl.movement.Sprint;
import me.rebirthclient.mod.modules.impl.movement.Step;
import me.rebirthclient.mod.modules.impl.movement.Strafe;
import me.rebirthclient.mod.modules.impl.movement.TargetStrafe;
import me.rebirthclient.mod.modules.impl.movement.Velocity;
import me.rebirthclient.mod.modules.impl.player.Announcer;
import me.rebirthclient.mod.modules.impl.player.AntiAim;
import me.rebirthclient.mod.modules.impl.player.AntiOpen;
import me.rebirthclient.mod.modules.impl.player.ArmorWarner;
import me.rebirthclient.mod.modules.impl.player.AutoFish;
import me.rebirthclient.mod.modules.impl.player.AutoFuck;
import me.rebirthclient.mod.modules.impl.player.AutoRespawn;
import me.rebirthclient.mod.modules.impl.player.BlockTweaks;
import me.rebirthclient.mod.modules.impl.player.FastPlace;
import me.rebirthclient.mod.modules.impl.player.FlagDetect;
import me.rebirthclient.mod.modules.impl.player.FreeLook;
import me.rebirthclient.mod.modules.impl.player.Freecam;
import me.rebirthclient.mod.modules.impl.player.KeyPearl;
import me.rebirthclient.mod.modules.impl.player.NoFall;
import me.rebirthclient.mod.modules.impl.player.NoRotate;
import me.rebirthclient.mod.modules.impl.player.PacketEat;
import me.rebirthclient.mod.modules.impl.player.Replenish;
import me.rebirthclient.mod.modules.impl.player.SpeedMine;
import me.rebirthclient.mod.modules.impl.player.TimerModule;
import me.rebirthclient.mod.modules.impl.player.TpsSync;
import me.rebirthclient.mod.modules.impl.render.Ambience;
import me.rebirthclient.mod.modules.impl.render.AutoEsu;
import me.rebirthclient.mod.modules.impl.render.BreadCrumbs;
import me.rebirthclient.mod.modules.impl.render.BreakESP;
import me.rebirthclient.mod.modules.impl.render.CameraClip;
import me.rebirthclient.mod.modules.impl.render.Chams;
import me.rebirthclient.mod.modules.impl.render.ChinaHat;
import me.rebirthclient.mod.modules.impl.render.CityESP;
import me.rebirthclient.mod.modules.impl.render.CrystalChams;
import me.rebirthclient.mod.modules.impl.render.DMGParticles;
import me.rebirthclient.mod.modules.impl.render.ESP;
import me.rebirthclient.mod.modules.impl.render.ESP2D;
import me.rebirthclient.mod.modules.impl.render.EarthPopChams;
import me.rebirthclient.mod.modules.impl.render.ExplosionSpawn;
import me.rebirthclient.mod.modules.impl.render.GlintModify;
import me.rebirthclient.mod.modules.impl.render.Highlight;
import me.rebirthclient.mod.modules.impl.render.HoleESP;
import me.rebirthclient.mod.modules.impl.render.ItemModel;
import me.rebirthclient.mod.modules.impl.render.ItemPhysics;
import me.rebirthclient.mod.modules.impl.render.LogOutSpots;
import me.rebirthclient.mod.modules.impl.render.Models;
import me.rebirthclient.mod.modules.impl.render.NameTags;
import me.rebirthclient.mod.modules.impl.render.NoLag;
import me.rebirthclient.mod.modules.impl.render.NoRender;
import me.rebirthclient.mod.modules.impl.render.PlaceRender;
import me.rebirthclient.mod.modules.impl.render.PopChams;
import me.rebirthclient.mod.modules.impl.render.PortalESP;
import me.rebirthclient.mod.modules.impl.render.RenderSetting;
import me.rebirthclient.mod.modules.impl.render.Rotations;
import me.rebirthclient.mod.modules.impl.render.Search;
import me.rebirthclient.mod.modules.impl.render.Shader;
import me.rebirthclient.mod.modules.impl.render.ShaderChams;
import me.rebirthclient.mod.modules.impl.render.Shaders;
import me.rebirthclient.mod.modules.impl.render.TileESP;
import me.rebirthclient.mod.modules.impl.render.Tracers;
import me.rebirthclient.mod.modules.impl.render.Trajectories;
import me.rebirthclient.mod.modules.impl.render.VoidESP;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.lwjgl.input.Keyboard;

public class ModuleManager
extends Mod {
    public static final Minecraft mc = Minecraft.getMinecraft();
    public final java.util.ArrayList<Module> modules = new java.util.ArrayList();
    public List<Module> sortedLength = new java.util.ArrayList<>();
    public List<String> sortedAbc = new java.util.ArrayList<>();

    public void init() {
        this.registerModules();
    }

    public void sortModules() {
        this.sortedLength = this.getEnabledModules().stream().filter(Module::isDrawn).sorted(Comparator.comparing(module -> Managers.TEXT.getStringWidth(HUD.INSTANCE.lowerCase.getValue() ? module.getArrayListInfo().toLowerCase() : module.getArrayListInfo()) * -1)).collect(Collectors.toList());
        this.sortedAbc = new java.util.ArrayList<>(this.getEnabledModulesString());
        this.sortedAbc.sort(String.CASE_INSENSITIVE_ORDER);
    }

    public java.util.ArrayList<Module> getEnabledModules() {
        java.util.ArrayList<Module> modules = new java.util.ArrayList<>();
        for (Module module : this.modules) {
            if (!module.isOn() || HUD.INSTANCE.onlyBind.getValue() && module.bind.getValue().getKey() == -1) continue;
            modules.add(module);
        }
        return modules;
    }

    public java.util.ArrayList<String> getEnabledModulesString() {
        java.util.ArrayList<String> modules = new java.util.ArrayList<>();
        for (Module module : this.modules) {
            if (!module.isOn() || !module.isDrawn() || HUD.INSTANCE.onlyBind.getValue() && module.bind.getValue().getKey() == -1) continue;
            modules.add(module.getArrayListInfo());
        }
        return modules;
    }

    public Module getModuleByName(String name) {
        for (Module module : this.modules) {
            if (!module.getName().equalsIgnoreCase(name)) continue;
            return module;
        }
        return null;
    }

    public java.util.ArrayList<Module> getModulesByCategory(Category category) {
        java.util.ArrayList<Module> modules = new java.util.ArrayList<>();
        this.modules.forEach(module -> {
            if (module.getCategory() == category) {
                modules.add(module);
            }
        });
        return modules;
    }

    public java.util.ArrayList<Module> getModules() {
        return this.modules;
    }

    public List<Category> getCategories() {
        return Arrays.asList(Category.values());
    }

    public void onUnloadPre() {
        this.modules.forEach(arg_0 -> MinecraftForge.EVENT_BUS.unregister(arg_0));
        this.modules.forEach(Module::onUnload);
    }

    public void onUnloadPost() {
        for (Module module : this.modules) {
            module.enabled.setValue(false);
        }
    }

    public void onKeyInput(int key) {
        if (key == 0 || !Keyboard.getEventKeyState() || ModuleManager.mc.currentScreen instanceof Gui) {
            return;
        }
        this.modules.forEach(module -> {
            if (module.getBind().getKey() == key) {
                module.toggle();
            }
        });
    }

    public void onLoad() {
        this.modules.stream().filter(Module::isListening).forEach(arg_0 -> MinecraftForge.EVENT_BUS.register(arg_0));
        this.modules.forEach(Module::onLoad);
    }

    public void onUpdate() {
        this.modules.stream().filter(Module::isOn).forEach(Module::onUpdate);
    }

    public void onTick() {
        this.modules.stream().filter(Module::isOn).forEach(Module::onTick);
    }

    public void onRender2D(Render2DEvent event) {
        this.modules.stream().filter(Module::isOn).forEach(module -> module.onRender2D(event));
    }

    public void onRender3D(Render3DEvent event) {
        this.modules.stream().filter(Module::isOn).forEach(module -> module.onRender3D(event));
    }

    public void onTotemPop(EntityPlayer player) {
        this.modules.stream().filter(Module::isOn).forEach(module -> module.onTotemPop(player));
    }

    public void onDeath(EntityPlayer player) {
        this.modules.stream().filter(Module::isOn).forEach(module -> module.onDeath(player));
    }

    public void onLogout() {
        this.modules.forEach(Module::onLogout);
    }

    public void onLogin() {
        this.modules.forEach(Module::onLogin);
    }

    private void registerModules() {
        this.modules.add(new Chat());
        this.modules.add(new ArrayList());
        this.modules.add(new FakeFPS());
        this.modules.add(new GuiAnimation());
        this.modules.add(new UnfocusedCPU());
        this.modules.add(new NameProtect());
        this.modules.add(new ClickGui());
        this.modules.add(new FontMod());
        this.modules.add(new HUD());
        this.modules.add(new FovMod());
        this.modules.add(new Title());
        this.modules.add(new Desktop());
        this.modules.add(new Appearance());
        this.modules.add(new BindList());
        this.modules.add(new Notifications());
        this.modules.add(new InventoryPreview());
        this.modules.add(new TargetHUD());
        this.modules.add(new AutoEsu());
        this.modules.add(new RenderSetting());
        this.modules.add(new PlaceRender());
        this.modules.add(new Highlight());
        this.modules.add(new ExplosionSpawn());
        this.modules.add(new EarthPopChams());
        this.modules.add(new PopChams());
        this.modules.add(new Rotations());
        this.modules.add(new CameraClip());
        this.modules.add(new PortalESP());
        this.modules.add(new Search());
        this.modules.add(new ChinaHat());
        this.modules.add(new CityESP());
        this.modules.add(new GlintModify());
        this.modules.add(new NoRender());
        this.modules.add(new Trajectories());
        this.modules.add(new ShaderChams());
        this.modules.add(new HoleESP());
        this.modules.add(new ItemModel());
        this.modules.add(new Tracers());
        this.modules.add(new CrystalChams());
        this.modules.add(new Chams());
        this.modules.add(new DMGParticles());
        this.modules.add(new ItemPhysics());
        this.modules.add(new BreakESP());
        this.modules.add(new Models());
        this.modules.add(new NameTags());
        this.modules.add(new ESP2D());
        this.modules.add(new Ambience());
        this.modules.add(new ESP());
        this.modules.add(new BreadCrumbs());
        this.modules.add(new VoidESP());
        this.modules.add(new NoLag());
        this.modules.add(new TileESP());
        this.modules.add(new Shader());
        this.modules.add(new Shaders());
        this.modules.add(new LogOutSpots());
        this.modules.add(new CatCrystal());
        this.modules.add(new PistonCrystal());
        this.modules.add(new PullCrystal());
        this.modules.add(new CrystalBot());
        this.modules.add(new AutoTotem());
        this.modules.add(new Burrow());
        this.modules.add(new AutoWire());
        this.modules.add(new Filler());
        this.modules.add(new WebTrap());
        this.modules.add(new AutoCity());
        this.modules.add(new CityRecode());
        this.modules.add(new PacketMine());
        this.modules.add(new Surround());
        this.modules.add(new TrapSelf());
        this.modules.add(new AntiWeak());
        this.modules.add(new AntiCity());
        this.modules.add(new AntiPiston());
        this.modules.add(new CombatSetting());
        this.modules.add(new AntiBurrow());
        this.modules.add(new ObiPlacer());
        this.modules.add(new Aura());
        this.modules.add(new AntiRegear());
        this.modules.add(new AutoArmor());
        this.modules.add(new Criticals());
        this.modules.add(new SelfWeb());
        this.modules.add(new AutoTrap());
        this.modules.add(new PacketExp());
        this.modules.add(new AutoPush());
        this.modules.add(new AutoWeb());
        this.modules.add(new TestPush());
        this.modules.add(new HoleFiller());
        this.modules.add(new AnvilAura());
        this.modules.add(new AutoReplenish());
        this.modules.add(new AntiAim());
        this.modules.add(new SpeedMine());
        this.modules.add(new AutoFish());
        this.modules.add(new AutoFuck());
        this.modules.add(new KeyPearl());
        this.modules.add(new BlockTweaks());
        this.modules.add(new Freecam());
        this.modules.add(new NoFall());
        this.modules.add(new AntiOpen());
        this.modules.add(new TpsSync());
        this.modules.add(new PacketEat());
        this.modules.add(new AutoRespawn());
        this.modules.add(new TimerModule());
        this.modules.add(new NoRotate());
        this.modules.add(new FastPlace());
        this.modules.add(new Replenish());
        this.modules.add(new ArmorWarner());
        this.modules.add(new Announcer());
        this.modules.add(new FlagDetect());
        this.modules.add(new FreeLook());
        this.modules.add(new Debug());
        this.modules.add(new AntiSpam());
        this.modules.add(new SilentDisconnect());
        this.modules.add(new TabFriends());
        this.modules.add(new ExtraTab());
        this.modules.add(new AntiNullPointer());
        this.modules.add(new AutoEZ());
        this.modules.add(new PopCounter());
        this.modules.add(new LightningDetect());
        this.modules.add(new Message());
        this.modules.add(new TNTTime());
        this.modules.add(new AutoTNT());
        this.modules.add(new AutoKit());
        this.modules.add(new AutoLogin());
        this.modules.add(new FakePlayer());
        this.modules.add(new AutoReconnect());
        this.modules.add(new KillEffects());
        this.modules.add(new Coords());
        this.modules.add(new PearlNotify());
        this.modules.add(new Peek());
        this.modules.add(new GhastNotifier());
        this.modules.add(new ToolTips());
        this.modules.add(new MCF());
        this.modules.add(new TargetStrafe());
        this.modules.add(new FastSwim());
        this.modules.add(new ElytraFly());
        this.modules.add(new AntiWeb());
        this.modules.add(new NoJumpDelay());
        this.modules.add(new AutoCenter());
        this.modules.add(new Step());
        this.modules.add(new NewStep());
        this.modules.add(new Flight());
        this.modules.add(new Speed());
        this.modules.add(new Strafe());
        this.modules.add(new LongJump());
        this.modules.add(new SafeWalk());
        this.modules.add(new NoSlowDown());
        this.modules.add(new InventoryMove());
        this.modules.add(new Scaffold());
        this.modules.add(new FastWeb());
        this.modules.add(new AutoWalk());
        this.modules.add(new FastFall());
        this.modules.add(new Sprint());
        this.modules.add(new AntiVoid());
        this.modules.add(new AntiGlide());
        this.modules.add(new Velocity());
        this.modules.add(new HoleSnap());
        this.modules.add(new Blink());
        this.modules.add(new PacketFly());
        this.modules.add(new BetterPortal());
        this.modules.add(new SuperThrow());
        this.modules.add(new SuperBow());
        this.modules.add(new Phase());
        this.modules.add(new TPCoordLog());
        this.modules.add(new GodMode());
        this.modules.add(new MultiTask());
        this.modules.add(new LiquidInteract());
        this.modules.add(new NoHitBox());
        this.modules.add(new Stresser());
        this.modules.add(new GhostHand());
        this.modules.add(new Crasher());
        this.modules.add(new XCarry());
        this.modules.add(new FakePearl());
        this.modules.add(new PearlSpoof());
        this.modules.add(new Clip());
        this.modules.add(new NoInteract());
    }

    public static enum Ordering {
        ABC,
        LENGTH

    }
}


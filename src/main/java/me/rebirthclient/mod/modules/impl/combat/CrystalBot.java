package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.events.impl.UpdateWalkingPlayerEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.managers.impl.RotationManager;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.*;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.asm.accessors.IEntityPlayerSP;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class CrystalBot
extends Module {
    public static Setting<Boolean> terrainIgnore;
    public static Setting<Boolean> collision;
    public static Setting<Integer> predictTicks;
    private static float aboba;
    private static CrystalBot INSTANCE;
    public final Setting<Float> placeRange;
    public final Setting<Boolean> strictDirection;
    public final Setting<DirectionMode> directionMode;
    private final Timer placeTimer;
    private final Timer renderTimeoutTimer;
    private final Timer renderBreakingTimer;
    private final Timer breakTimer;
    private final Timer swapTimer;
    private final Setting<Bind> forceFaceplace;
    private final List<BlockPos> selfPlacePositions;
    private final Timer linearTimer;
    private final ConcurrentHashMap<BlockPos, Long> placeLocations;
    private final ConcurrentHashMap<Integer, Long> breakLocations;
    private final Map<EntityPlayer, Timer> totemPops;
    private final Timer inhibitTimer;
    private final Timer cacheTimer;
    private final AtomicBoolean shouldRunThread;
    private final Setting<Boolean> armorBreaker;
    private final AtomicBoolean lastBroken;
    private final Timer renderTargetTimer = new Timer();
    private final FadeUtils fadeUtils = new FadeUtils(500L);
    private final Timer scatterTimer;
    private final Setting<Float> depletion;
    private final List<currentPos> positions = new ArrayList<>();
    private final Setting<Pages> setting = this.add(new Setting<>("Page", Pages.General));
    private final Setting<Float> compromise = this.add(new Setting<>("Compromise", 1.0f, 0.05f, 2.0f, f -> this.setting.getValue() == Pages.Calculation));
    private final Setting<Integer> switchCooldown = this.add(new Setting<>("SwitchCooldown", 100, 0, 1000, v -> this.setting.getValue() == Pages.General));
    private final Setting<Float> range = this.add(new Setting<>("Range", 10.0f, 0.0f, 50.0f, bl -> this.setting.getValue() == Pages.Render));
    private final Setting<TargetRenderMode> targetRender = this.add(new Setting<>("TargetRender", TargetRenderMode.JELLO, bl -> this.setting.getValue() == Pages.Render));
    private final Setting<Color> targetColor = this.add(new Setting<>("TargetColor", new Color(255, 255, 255, 255), bl -> this.setting.getValue() == Pages.Render && this.targetRender.getValue() != TargetRenderMode.OFF));
    private final Setting<TimingMode> timingMode;
    private final Setting<Float> crystalRange;
    private final Setting<SyncMode> syncMode;
    private final Setting<Integer> attackFactor;
    private final Setting<Boolean> check;
    private final Setting<Float> breakRange;
    private final Setting<Boolean> limit;
    private final Setting<Float> suicideHealth;
    private final Setting<Integer> yawTicks;
    private final Setting<Boolean> protocol;
    private final Setting<Float> placeSpeed;
    private final Setting<Float> faceplaceHealth;
    private final Setting<RotationMode> rotationMode;
    private final Setting<Boolean> noGapSwitch;
    private final Setting<Float> breakSpeed;
    private final Setting<Boolean> noMineSwitch;
    private final Setting<TargetingMode> targetingMode;
    private final Setting<Float> yawAngle;
    private final Setting<Float> swapDelay;
    private final Setting<Float> minPlaceDamage;
    private final Setting<Float> security;
    private final Setting<Float> mergeOffset;
    private final Timer rotationTimer;
    private final Setting<Boolean> predictPops;
    private final Setting<Float> placeWallsRange;
    private final Setting<ACSwapMode> autoSwap;
    private final Setting<Float> breakWallsRange;
    private final Setting<Boolean> liquids;
    private final Setting<Boolean> swing;
    private final Setting<Float> maxSelfPlace;
    private final AtomicBoolean tickRunning;
    private final Setting<Float> disableUnderHealth;
    private final Setting<Float> enemyRange;
    private final Setting<Float> delay;
    private final Setting<ACAntiWeakness> antiWeakness;
    private final Setting<YawStepMode> yawStep;
    private final Setting<Boolean> fire;
    private final Setting<Boolean> rightClickGap;
    private final Setting<ConfirmMode> confirm;
    private final Setting<Boolean> inhibit;
    private final Setting<Boolean> Actualp = this.add(new Setting<>("ActualRender+", true, bl -> this.setting.getValue() == Pages.Render));
    private final Setting<Boolean> text = this.add(new Setting<>("Text", false, bl -> this.setting.getValue() == Pages.Render && this.Actualp.getValue()));
    private final Setting<Color> boxColor = this.add(new Setting<>("BoxColor", new Color(255, 255, 255, 100), bl -> this.setting.getValue() == Pages.Render && this.Actualp.getValue()).injectBoolean(true));
    private final Setting<Color> outlineColor = this.add(new Setting<>("OutlineColor", new Color(255, 255, 255, 255), bl -> this.setting.getValue() == Pages.Render && this.Actualp.getValue()).injectBoolean(true));
    private final Setting<RenderMode> renderMode = this.add(new Setting<>("APacketRenderMode", RenderMode.STATIC, bl -> this.setting.getValue() == Pages.Render && this.Actualp.getValue()));
    private final Setting<Boolean> fadeFactor = this.add(new Setting<>("APacketFade", true, bl -> this.setting.getValue() == Pages.Render && this.renderMode.getValue() == RenderMode.FADE && this.Actualp.getValue()));
    private final Setting<Boolean> scaleFactor = this.add(new Setting<>("APacketShrink", false, bl -> this.setting.getValue() == Pages.Render && this.renderMode.getValue() == RenderMode.FADE && this.Actualp.getValue()));
    private final Setting<Boolean> slabFactor = this.add(new Setting<>("APacketSlab", false, bl -> this.setting.getValue() == Pages.Render && this.renderMode.getValue() == RenderMode.FADE && this.Actualp.getValue()));
    private final Setting<Float> duration = this.add(new Setting<>("APacketDuration", 1500.0f, 0.0f, 5000.0f, bl -> this.setting.getValue() == Pages.Render && this.renderMode.getValue() == RenderMode.FADE && this.Actualp.getValue()));
    private final Setting<Integer> max = this.add(new Setting<>("APacketMaxPositions", 15, 1, 30, bl -> this.setting.getValue() == Pages.Render && this.renderMode.getValue() == RenderMode.FADE && this.Actualp.getValue()));
    private final Setting<Float> slabHeight = this.add(new Setting<>("APacketSlabDepth", 1.0f, -1.0f, 1.0f, bl -> this.setting.getValue() == Pages.Render && (this.renderMode.getValue() == RenderMode.STATIC || this.renderMode.getValue() == RenderMode.GLIDE) && this.Actualp.getValue()));
    private final Setting<Float> moveSpeed = this.add(new Setting<>("APacketSpeed", 200.0f, 0.0f, 3000.0f, bl -> this.setting.getValue() == Pages.Render && this.renderMode.getValue() == RenderMode.GLIDE && this.Actualp.getValue()));
    private final Setting<Float> accel = this.add(new Setting<>("APacketDeceleration", 0.8f, 0.0f, 5.0f, bl -> this.setting.getValue() == Pages.Render && this.renderMode.getValue() == RenderMode.GLIDE && this.Actualp.getValue()));
    private final Setting<Boolean> colorSync = this.add(new Setting<>("APacketCSync", false, bl -> this.setting.getValue() == Pages.Render && this.Actualp.getValue()));
    private final Setting<Integer> fadeTime = this.add(new Setting<>("FadeTime", 800, 0, 3000, bl -> this.setting.getValue() == Pages.Render && this.Actualp.getValue()));
    private final Setting<Integer> startFadeTime = this.add(new Setting<>("StartFadeTime", 300, 0, 2000, bl -> this.setting.getValue() == Pages.Render && this.Actualp.getValue()));
    private final Setting<Float> lineWidth = this.add(new Setting<>("LineWidth", 1.5f, 0.1f, 5.0f, bl -> this.setting.getValue() == Pages.Render && this.outlineColor.booleanValue));
    public BlockPos cachePos;
    public int oldSlotCrystal;
    public BlockPos renderBreakingPos;
    public boolean isPlacing;
    public EntityEnderCrystal inhibitEntity;
    public int oldSlotSword;
    public Vec3d rotationVector;
    public float renderDamage;
    public float[] rotations;
    public BlockPos renderBlock;
    private Vec3d bilateralVec;
    private AxisAlignedBB renderBB;
    private Thread thread;
    private BlockPos postPlacePos;
    private EntityEnderCrystal postBreakPos;
    private EntityPlayer renderTarget;
    private float timePassed;
    private RayTraceResult postResult;
    private int ticks;
    private BlockPos lastpos;
    private boolean foundDoublePop;
    private EnumFacing postFacing;
    private static final Timer switchTimer;
    private int lastSlot = -1;

    public CrystalBot() {
        super("CrystalBot", "Muse Callisto", Category.COMBAT);
        this.noMineSwitch = this.add(new Setting<>("NoMining", Boolean.FALSE, bl -> this.setting.getValue() == Pages.General));
        this.noGapSwitch = this.add(new Setting<>("NoGapping", Boolean.FALSE, bl -> this.setting.getValue() == Pages.General));
        this.rightClickGap = this.add(new Setting<>("RightClickGap", Boolean.FALSE, bl -> this.noGapSwitch.getValue() && this.setting.getValue() == Pages.General));
        this.timingMode = this.add(new Setting<>("Timing", TimingMode.SEQUENTIAL, timingMode -> this.setting.getValue() == Pages.General));
        this.inhibit = this.add(new Setting<>("Inhibit", Boolean.FALSE, bl -> this.setting.getValue() == Pages.General));
        this.limit = this.add(new Setting<>("Limit", Boolean.TRUE, bl -> this.setting.getValue() == Pages.General));
        this.rotationMode = this.add(new Setting<>("Rotate", RotationMode.TRACK, rotationMode -> this.setting.getValue() == Pages.General));
        this.yawStep = this.add(new Setting<>("YawStep", YawStepMode.OFF, yawStepMode -> this.setting.getValue() == Pages.General));
        this.yawAngle = this.add(new Setting<>("YawAngle", 0.3f, 0.1f, 1.0f, f -> this.setting.getValue() == Pages.General));
        this.yawTicks = this.add(new Setting<>("YawTicks", 1, 1, 5, n -> this.setting.getValue() == Pages.General));
        this.strictDirection = this.add(new Setting<>("StrictDirection", Boolean.TRUE, bl -> this.setting.getValue() == Pages.General));
        this.swing = this.add(new Setting<>("Swing", Boolean.TRUE, bl -> this.setting.getValue() == Pages.General));
        this.syncMode = this.add(new Setting<>("Sync", SyncMode.MERGE, syncMode -> this.setting.getValue() == Pages.General));
        this.mergeOffset = this.add(new Setting<>("MergeOffset", 0.0f, 0.0f, 8.0f, f -> this.syncMode.getValue() == SyncMode.MERGE && this.setting.getValue() == Pages.General));
        this.enemyRange = this.add(new Setting<>("EnemyRange", 8.0f, 4.0f, 15.0f, f -> this.setting.getValue() == Pages.General));
        this.crystalRange = this.add(new Setting<>("CrystalRange", 6.0f, 2.0f, 12.0f, f -> this.setting.getValue() == Pages.General));
        this.disableUnderHealth = this.add(new Setting<>("DisableHealth", 0.0f, 0.0f, 10.0f, f -> this.setting.getValue() == Pages.General));
        this.placeRange = this.add(new Setting<>("PlaceRange", 4.0f, 1.0f, 6.0f, bl -> this.setting.getValue() == Pages.Place));
        this.placeWallsRange = this.add(new Setting<>("PlaceWallsRange", 3.0f, 1.0f, 6.0f, bl -> this.setting.getValue() == Pages.Place));
        this.placeSpeed = this.add(new Setting<>("PlaceSpeed", 20.0f, 2.0f, 20.0f, bl -> this.setting.getValue() == Pages.Place));
        this.autoSwap = this.add(new Setting<>("AutoSwap", ACSwapMode.OFF, bl -> this.setting.getValue() == Pages.Place));
        this.swapDelay = this.add(new Setting<>("SwapDelay", 0.0f, 0.0f, 10.0f, bl -> this.setting.getValue() == Pages.Place));
        this.check = this.add(new Setting<>("PlacementsCheck", Boolean.TRUE, bl -> this.setting.getValue() == Pages.Place));
        this.directionMode = this.add(new Setting<>("Interact", DirectionMode.NORMAL, bl -> this.setting.getValue() == Pages.Place));
        this.protocol = this.add(new Setting<>("1.13+ Place", Boolean.FALSE, bl -> this.setting.getValue() == Pages.Place));
        this.liquids = this.add(new Setting<>("PlaceInLiquids", Boolean.FALSE, bl -> this.setting.getValue() == Pages.Place));
        this.fire = this.add(new Setting<>("PlaceInFire", Boolean.FALSE, bl -> this.setting.getValue() == Pages.Place));
        this.breakRange = this.add(new Setting<>("BreakRange", 4.3f, 1.0f, 6.0f, bl -> this.setting.getValue() == Pages.Break));
        this.breakWallsRange = this.add(new Setting<>("BreakWalls", 3.0f, 1.0f, 6.0f, bl -> this.setting.getValue() == Pages.Break));
        this.attackFactor = this.add(new Setting<>("AttackFactor", 3, 1, 20, bl -> this.setting.getValue() == Pages.Break));
        this.antiWeakness = this.add(new Setting<>("AntiWeakness", ACAntiWeakness.OFF, bl -> this.setting.getValue() == Pages.Break));
        this.breakSpeed = this.add(new Setting<>("BreakSpeed", 20.0f, 1.0f, 20.0f, bl -> this.setting.getValue() == Pages.Break));
        collision = this.add(new Setting<>("Collision", Boolean.FALSE, bl -> this.setting.getValue() == Pages.General));
        predictTicks = this.add(new Setting<>("PredictTicks", 1, 0, 10, bl -> this.setting.getValue() == Pages.General));
        terrainIgnore = this.add(new Setting<>("TerrainTrace", Boolean.FALSE, bl -> this.setting.getValue() == Pages.General));
        this.predictPops = this.add(new Setting<>("PredictPops", Boolean.FALSE, bl -> this.setting.getValue() == Pages.General));
        this.confirm = this.add(new Setting<>("Confirm", ConfirmMode.OFF, confirmMode -> this.setting.getValue() == Pages.Calculation));
        this.delay = this.add(new Setting<>("TicksExisted", 0.0f, 0.0f, 20.0f, n -> this.setting.getValue() == Pages.Calculation));
        this.targetingMode = this.add(new Setting<>("Target", TargetingMode.ALL, targetingMode -> this.setting.getValue() == Pages.Calculation));
        this.security = this.add(new Setting<>("DamageBalance", 1.0f, 0.1f, 5.0f, f -> this.setting.getValue() == Pages.Calculation));
        this.minPlaceDamage = this.add(new Setting<>("MinDamage", 6.0f, 0.0f, 20.0f, f -> this.setting.getValue() == Pages.Calculation));
        this.maxSelfPlace = this.add(new Setting<>("MaxSelfDmg", 12.0f, 0.0f, 20.0f, f -> this.setting.getValue() == Pages.Calculation));
        this.suicideHealth = this.add(new Setting<>("SuicideHealth", 2.0f, 0.0f, 10.0f, f -> this.setting.getValue() == Pages.Calculation));
        this.faceplaceHealth = this.add(new Setting<>("FacePlaceHealth", 4.0f, 0.0f, 36.0f, f -> this.setting.getValue() == Pages.Calculation));
        this.forceFaceplace = this.add(new Setting<>("FacePlace", new Bind(-1), bind -> this.setting.getValue() == Pages.Calculation));
        this.armorBreaker = this.add(new Setting<>("ArmorBreaker", Boolean.TRUE, bl -> this.setting.getValue() == Pages.Calculation));
        this.depletion = this.add(new Setting<>("ArmorDepletion", 0.9f, 0.1f, 1.0f, f -> this.armorBreaker.getValue() && this.setting.getValue() == Pages.Calculation));
        this.rotationVector = null;
        this.rotations = new float[]{0.0f, 0.0f};
        this.rotationTimer = new Timer();
        this.placeTimer = new Timer();
        this.breakTimer = new Timer();
        this.swapTimer = new Timer();
        this.renderDamage = 0.0f;
        this.renderTimeoutTimer = new Timer();
        this.renderBreakingTimer = new Timer();
        this.isPlacing = false;
        this.placeLocations = new ConcurrentHashMap();
        this.breakLocations = new ConcurrentHashMap();
        this.totemPops = new ConcurrentHashMap<>();
        this.selfPlacePositions = new CopyOnWriteArrayList<>();
        this.tickRunning = new AtomicBoolean(false);
        this.linearTimer = new Timer();
        this.cacheTimer = new Timer();
        this.cachePos = null;
        this.inhibitTimer = new Timer();
        this.inhibitEntity = null;
        this.scatterTimer = new Timer();
        this.bilateralVec = null;
        this.shouldRunThread = new AtomicBoolean(false);
        this.lastBroken = new AtomicBoolean(false);
        this.foundDoublePop = false;
        this.oldSlotCrystal = -1;
        this.oldSlotSword = -1;
        this.setInstance();
    }

    public static CrystalBot getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrystalBot();
        }
        return INSTANCE;
    }

    public static List<BlockPos> getSphere(BlockPos blockPos, float f, int n, boolean bl, boolean bl2, int n2) {
        ArrayList<BlockPos> arrayList = new ArrayList<>();
        int n3 = blockPos.getX();
        int n4 = blockPos.getY();
        int n5 = blockPos.getZ();
        int n6 = n3 - (int)f;
        while ((float)n6 <= (float)n3 + f) {
            int n7 = n5 - (int)f;
            while ((float)n7 <= (float)n5 + f) {
                int n8 = bl2 ? n4 - (int)f : n4;
                while (true) {
                    float f2;
                    float f3 = n8;
                    float f4 = f2 = bl2 ? (float)n4 + f : (float)(n4 + n);
                    if (!(f3 < f2)) break;
                    double d = (n3 - n6) * (n3 - n6) + (n5 - n7) * (n5 - n7) + (bl2 ? (n4 - n8) * (n4 - n8) : 0);
                    if (!(!(d < (double)(f * f)) || bl && d < (double)((f - 1.0f) * (f - 1.0f)))) {
                        BlockPos blockPos2 = new BlockPos(n6, n8 + n2, n7);
                        arrayList.add(blockPos2);
                    }
                    ++n8;
                }
                ++n7;
            }
            ++n6;
        }
        return arrayList;
    }

    @Override
    public void onTick() {
        if (this.lastSlot != CrystalBot.mc.player.inventory.currentItem) {
            this.lastSlot = CrystalBot.mc.player.inventory.currentItem;
            switchTimer.reset();
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayerPre(UpdateWalkingPlayerEvent updateWalkingPlayerEvent) {
        if (CrystalBot.fullNullCheck()) {
            return;
        }
        if (updateWalkingPlayerEvent.getStage() == 0) {
            this.placeLocations.forEach((blockPos, l) -> {
                if (System.currentTimeMillis() - l > 1500L) {
                    this.placeLocations.remove(blockPos);
                }
            });
            --this.ticks;
            if (this.bilateralVec != null) {
                for (Entity entity : CrystalBot.mc.world.loadedEntityList) {
                    if (!(entity instanceof EntityEnderCrystal) || !(entity.getDistance(this.bilateralVec.x, this.bilateralVec.y, this.bilateralVec.z) <= 6.0)) continue;
                    this.breakLocations.put(entity.getEntityId(), System.currentTimeMillis());
                }
                this.bilateralVec = null;
            }
            if (updateWalkingPlayerEvent.isCanceled()) {
                return;
            }
            this.postBreakPos = null;
            this.postPlacePos = null;
            this.postFacing = null;
            this.postResult = null;
            this.foundDoublePop = false;
            this.handleSequential();
            if (this.rotationMode.getValue() != RotationMode.OFF && !this.rotationTimer.passedMs(650L) && this.rotationVector != null) {
                if (this.rotationMode.getValue() == RotationMode.TRACK) {
                    this.rotations = RotationManager.calculateAngle(CrystalBot.mc.player.getPositionEyes(1.0f), this.rotationVector);
                }
                if (this.yawAngle.getValue() < 1.0f && this.yawStep.getValue() != YawStepMode.OFF && (this.postBreakPos != null || this.yawStep.getValue() == YawStepMode.FULL)) {
                    if (this.ticks > 0) {
                        this.rotations[0] = ((IEntityPlayerSP)CrystalBot.mc.player).getLastReportedYaw();
                        this.postBreakPos = null;
                        this.postPlacePos = null;
                    } else {
                        float f = MathHelper.wrapDegrees(this.rotations[0] - ((IEntityPlayerSP)CrystalBot.mc.player).getLastReportedYaw());
                        if (Math.abs(f) > 180.0f * this.yawAngle.getValue()) {
                            this.rotations[0] = ((IEntityPlayerSP)CrystalBot.mc.player).getLastReportedYaw() + f * (180.0f * this.yawAngle.getValue() / Math.abs(f));
                            this.postBreakPos = null;
                            this.postPlacePos = null;
                            this.ticks = this.yawTicks.getValue();
                        }
                    }
                }
                this.lookAtAngles(this.rotations[0], this.rotations[1]);
            }
        }
    }

    private void lookAtAngles(float f, float f2) {
        this.setPlayerRotations(f, f2);
        CrystalBot.mc.player.rotationYawHead = f;
    }

    private void setPlayerRotations(float f, float f2) {
        CrystalBot.mc.player.rotationYaw = f;
        CrystalBot.mc.player.rotationYawHead = f;
        CrystalBot.mc.player.rotationPitch = f2;
    }

    private boolean breakCrystal(EntityEnderCrystal entityEnderCrystal) {
        if (entityEnderCrystal != null) {
            if (this.antiWeakness.getValue() != ACAntiWeakness.OFF && CrystalBot.mc.player.isPotionActive(MobEffects.WEAKNESS) && !(CrystalBot.mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) && !this.switchToSword()) {
                return false;
            }
            if (!this.swapTimer.passedMs((long)(this.swapDelay.getValue() * 100.0f))) {
                return false;
            }
            CrystalBot.mc.playerController.attackEntity(CrystalBot.mc.player, entityEnderCrystal);
            CrystalBot.mc.player.connection.sendPacket(new CPacketAnimation(this.isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
            this.swingArmAfterBreaking(this.isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
            if (this.oldSlotSword != -1 && CrystalBot.mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) {
                CrystalBot.mc.player.inventory.currentItem = this.oldSlotSword;
                CrystalBot.mc.player.connection.sendPacket(new CPacketHeldItemChange(this.oldSlotSword));
                this.oldSlotSword = -1;
            }
            if (this.syncMode.getValue() == SyncMode.MERGE) {
                this.placeTimer.reset();
            }
            if (this.syncMode.getValue() == SyncMode.STRICT) {
                this.lastBroken.set(true);
            }
            this.inhibitTimer.reset();
            this.inhibitEntity = entityEnderCrystal;
            this.renderBreakingPos = new BlockPos(entityEnderCrystal).down();
            this.renderBreakingTimer.reset();
            return true;
        }
        return false;
    }

    private BlockPos findPlacePosition(List<BlockPos> list, List<EntityPlayer> list2) {
        if (list2.isEmpty()) {
            return null;
        }
        float f = 0.5f;
        EntityPlayer entityPlayer = null;
        BlockPos blockPos = null;
        this.foundDoublePop = false;
        EntityPlayer entityPlayer2 = null;
        for (BlockPos blockPos2 : list) {
            float f2 = CrystalUtil.calculateDamage(blockPos2, CrystalBot.mc.player);
            if (!((double)f2 + (double) this.suicideHealth.getValue() < (double)(CrystalBot.mc.player.getHealth() + CrystalBot.mc.player.getAbsorptionAmount())) || !(f2 <= this.maxSelfPlace.getValue())) continue;
            if (this.targetingMode.getValue() != TargetingMode.ALL) {
                entityPlayer2 = list2.get(0);
                if (entityPlayer2.getDistance((double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.5, (double)blockPos2.getZ() + 0.5) > (double) this.crystalRange.getValue()) continue;
                float f3 = CrystalUtil.calculateDamage(blockPos2, entityPlayer2);
                if (this.isDoublePoppable(entityPlayer2, f3) && (blockPos == null || entityPlayer2.getDistanceSq(blockPos2) < entityPlayer2.getDistanceSq(blockPos))) {
                    entityPlayer = entityPlayer2;
                    f = f3;
                    blockPos = blockPos2;
                    this.foundDoublePop = true;
                    continue;
                }
                if (this.foundDoublePop || !(f3 > f) || !(f3 * this.compromise.getValue() > f2) && !(f3 > entityPlayer2.getHealth() + entityPlayer2.getAbsorptionAmount()) || f3 < this.minPlaceDamage.getValue() && entityPlayer2.getHealth() + entityPlayer2.getAbsorptionAmount() > this.faceplaceHealth.getValue() && !this.forceFaceplace.getValue().isDown() && !this.shouldArmorBreak(entityPlayer2)) continue;
                f = f3;
                entityPlayer = entityPlayer2;
                blockPos = blockPos2;
                continue;
            }
            for (EntityPlayer entityPlayer3 : list2) {
                if (entityPlayer3.equals(entityPlayer2) || entityPlayer3.getDistance((double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.5, (double)blockPos2.getZ() + 0.5) > (double) this.crystalRange.getValue()) continue;
                float f4 = CrystalUtil.calculateDamage(blockPos2, entityPlayer3);
                if (this.isDoublePoppable(entityPlayer3, f4) && (blockPos == null || entityPlayer3.getDistanceSq(blockPos2) < entityPlayer3.getDistanceSq(blockPos))) {
                    entityPlayer = entityPlayer3;
                    f = f4;
                    blockPos = blockPos2;
                    this.foundDoublePop = true;
                    continue;
                }
                if (this.foundDoublePop || !(f4 > f) || !(f4 * this.compromise.getValue() > f2) && !(f4 > entityPlayer3.getHealth() + entityPlayer3.getAbsorptionAmount()) || f4 < this.minPlaceDamage.getValue() && entityPlayer3.getHealth() + entityPlayer3.getAbsorptionAmount() > this.faceplaceHealth.getValue() && !this.forceFaceplace.getValue().isDown() && !this.shouldArmorBreak(entityPlayer3)) continue;
                f = f4;
                entityPlayer = entityPlayer3;
                blockPos = blockPos2;
            }
        }
        if (entityPlayer != null) {
            this.renderTarget = entityPlayer;
            this.renderTargetTimer.reset();
            this.fadeUtils.reset();
        }
        if (blockPos != null) {
            this.renderBlock = blockPos;
            this.renderDamage = f;
        }
        this.cachePos = blockPos;
        this.cacheTimer.reset();
        return blockPos;
    }

    @Override
    public String getInfo() {
        float test1 = this.renderTargetTimer.getPassedTimeMs();
        if (this.renderTarget != null && !this.renderTargetTimer.passedMs(800L) && !this.renderTarget.isDead) {
            return this.renderTarget.getName() + " , " + (Math.floor(this.renderDamage) == (double)this.renderDamage ? Integer.valueOf((int)this.renderDamage) : String.format("%.1f", this.renderDamage));
        }
        return null;
    }

    public EnumFacing handlePlaceRotation(BlockPos blockPos) {
        if (blockPos == null || CrystalBot.mc.player == null) {
            return null;
        }
        EnumFacing enumFacing = null;
        if (this.directionMode.getValue() != DirectionMode.VANILLA) {
            double[] arrd;
            Vec3d vec3d;
            RayTraceResult rayTraceResult;
            Vec3d vec3d2;
            Vec3d vec3d3;
            float f;
            float f2;
            float f3;
            float f4;
            double[] arrd2;
            double d;
            double d2;
            double d3;
            double d4;
            double d5;
            Vec3d vec3d4;
            double d6;
            double d7;
            double d8;
            Vec3d vec3d5 = null;
            double[] arrd3 = null;
            double d9 = 0.45;
            double d10 = 0.05;
            double d11 = 0.95;
            Vec3d vec3d6 = new Vec3d(CrystalBot.mc.player.posX, CrystalBot.mc.player.getEntityBoundingBox().minY + (double)CrystalBot.mc.player.getEyeHeight(), CrystalBot.mc.player.posZ);
            for (d8 = d10; d8 <= d11; d8 += d9) {
                for (d7 = d10; d7 <= d11; d7 += d9) {
                    for (d6 = d10; d6 <= d11; d6 += d9) {
                        vec3d4 = new Vec3d(blockPos).add(d8, d7, d6);
                        d5 = vec3d6.distanceTo(vec3d4);
                        d4 = vec3d4.x - vec3d6.x;
                        d3 = vec3d4.y - vec3d6.y;
                        d2 = vec3d4.z - vec3d6.z;
                        d = MathHelper.sqrt(d4 * d4 + d2 * d2);
                        arrd2 = new double[]{MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(d2, d4)) - 90.0f), MathHelper.wrapDegrees(-Math.toDegrees(Math.atan2(d3, d)))};
                        f4 = MathHelper.cos((float) (-arrd2[0] * 0.01745329238474369 - 3.1415927410125732));
                        f3 = MathHelper.sin((float) (-arrd2[0] * 0.01745329238474369 - 3.1415927410125732));
                        f2 = -MathHelper.cos((float) (-arrd2[1] * 0.01745329238474369));
                        f = MathHelper.sin((float) (-arrd2[1] * 0.01745329238474369));
                        vec3d3 = new Vec3d(f3 * f2, f, f4 * f2);
                        vec3d2 = vec3d6.add(vec3d3.x * d5, vec3d3.y * d5, vec3d3.z * d5);
                        rayTraceResult = CrystalBot.mc.world.rayTraceBlocks(vec3d6, vec3d2, false, true, false);
                        if (!(this.placeWallsRange.getValue() >= this.placeRange.getValue() || rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK && rayTraceResult.getBlockPos().equals(blockPos))) continue;
                        vec3d = vec3d4;
                        arrd = arrd2;
                        if (this.strictDirection.getValue()) {
                            if (vec3d5 != null && arrd3 != null && (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK || enumFacing == null)) {
                                if (!(CrystalBot.mc.player.getPositionVector().add(0.0, CrystalBot.mc.player.getEyeHeight(), 0.0).distanceTo(vec3d) < CrystalBot.mc.player.getPositionVector().add(0.0, CrystalBot.mc.player.getEyeHeight(), 0.0).distanceTo(vec3d5))) continue;
                                vec3d5 = vec3d;
                                arrd3 = arrd;
                                if (rayTraceResult == null || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) continue;
                                enumFacing = rayTraceResult.sideHit;
                                this.postResult = rayTraceResult;
                                continue;
                            }
                            vec3d5 = vec3d;
                            arrd3 = arrd;
                            if (rayTraceResult == null || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) continue;
                            enumFacing = rayTraceResult.sideHit;
                            this.postResult = rayTraceResult;
                            continue;
                        }
                        if (vec3d5 != null && arrd3 != null && (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK || enumFacing == null)) {
                            if (!(Math.hypot(((arrd[0] - (double)((IEntityPlayerSP)CrystalBot.mc.player).getLastReportedYaw()) % 360.0 + 540.0) % 360.0 - 180.0, arrd[1] - (double)((IEntityPlayerSP)CrystalBot.mc.player).getLastReportedPitch()) < Math.hypot(((arrd3[0] - (double)((IEntityPlayerSP)CrystalBot.mc.player).getLastReportedYaw()) % 360.0 + 540.0) % 360.0 - 180.0, arrd3[1] - (double)((IEntityPlayerSP)CrystalBot.mc.player).getLastReportedPitch()))) continue;
                            vec3d5 = vec3d;
                            arrd3 = arrd;
                            if (rayTraceResult == null || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) continue;
                            enumFacing = rayTraceResult.sideHit;
                            this.postResult = rayTraceResult;
                            continue;
                        }
                        vec3d5 = vec3d;
                        arrd3 = arrd;
                        if (rayTraceResult == null || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) continue;
                        enumFacing = rayTraceResult.sideHit;
                        this.postResult = rayTraceResult;
                    }
                }
            }
            if (this.placeWallsRange.getValue() < this.placeRange.getValue() && this.directionMode.getValue() == DirectionMode.STRICT) {
                if (arrd3 != null && enumFacing != null) {
                    this.rotationTimer.reset();
                    this.rotationVector = vec3d5;
                    this.rotations = RotationManager.calculateAngle(CrystalBot.mc.player.getPositionEyes(1.0f), this.rotationVector);
                    return enumFacing;
                }
                for (d8 = d10; d8 <= d11; d8 += d9) {
                    for (d7 = d10; d7 <= d11; d7 += d9) {
                        for (d6 = d10; d6 <= d11; d6 += d9) {
                            vec3d4 = new Vec3d(blockPos).add(d8, d7, d6);
                            d5 = vec3d6.distanceTo(vec3d4);
                            d4 = vec3d4.x - vec3d6.x;
                            d3 = vec3d4.y - vec3d6.y;
                            d2 = vec3d4.z - vec3d6.z;
                            d = MathHelper.sqrt(d4 * d4 + d2 * d2);
                            arrd2 = new double[]{MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(d2, d4)) - 90.0f), MathHelper.wrapDegrees(-Math.toDegrees(Math.atan2(d3, d)))};
                            f4 = MathHelper.cos((float) (-arrd2[0] * 0.01745329238474369 - 3.1415927410125732));
                            f3 = MathHelper.sin((float) (-arrd2[0] * 0.01745329238474369 - 3.1415927410125732));
                            f2 = -MathHelper.cos((float) (-arrd2[1] * 0.01745329238474369));
                            f = MathHelper.sin((float) (-arrd2[1] * 0.01745329238474369));
                            vec3d3 = new Vec3d(f3 * f2, f, f4 * f2);
                            vec3d2 = vec3d6.add(vec3d3.x * d5, vec3d3.y * d5, vec3d3.z * d5);
                            rayTraceResult = CrystalBot.mc.world.rayTraceBlocks(vec3d6, vec3d2, false, true, true);
                            if (rayTraceResult == null || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) continue;
                            vec3d = vec3d4;
                            arrd = arrd2;
                            if (this.strictDirection.getValue()) {
                                if (vec3d5 != null && arrd3 != null && (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK || enumFacing == null)) {
                                    if (!(CrystalBot.mc.player.getPositionVector().add(0.0, CrystalBot.mc.player.getEyeHeight(), 0.0).distanceTo(vec3d) < CrystalBot.mc.player.getPositionVector().add(0.0, CrystalBot.mc.player.getEyeHeight(), 0.0).distanceTo(vec3d5))) continue;
                                    vec3d5 = vec3d;
                                    arrd3 = arrd;
                                    if (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) continue;
                                    enumFacing = rayTraceResult.sideHit;
                                    this.postResult = rayTraceResult;
                                    continue;
                                }
                                vec3d5 = vec3d;
                                arrd3 = arrd;
                                if (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) continue;
                                enumFacing = rayTraceResult.sideHit;
                                this.postResult = rayTraceResult;
                                continue;
                            }
                            if (vec3d5 != null && arrd3 != null && (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK || enumFacing == null)) {
                                if (!(Math.hypot(((arrd[0] - (double)((IEntityPlayerSP)CrystalBot.mc.player).getLastReportedYaw()) % 360.0 + 540.0) % 360.0 - 180.0, arrd[1] - (double)((IEntityPlayerSP)CrystalBot.mc.player).getLastReportedPitch()) < Math.hypot(((arrd3[0] - (double)((IEntityPlayerSP)CrystalBot.mc.player).getLastReportedYaw()) % 360.0 + 540.0) % 360.0 - 180.0, arrd3[1] - (double)((IEntityPlayerSP)CrystalBot.mc.player).getLastReportedPitch()))) continue;
                                vec3d5 = vec3d;
                                arrd3 = arrd;
                                if (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) continue;
                                enumFacing = rayTraceResult.sideHit;
                                this.postResult = rayTraceResult;
                                continue;
                            }
                            vec3d5 = vec3d;
                            arrd3 = arrd;
                            if (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) continue;
                            enumFacing = rayTraceResult.sideHit;
                            this.postResult = rayTraceResult;
                        }
                    }
                }
            } else {
                if (arrd3 != null) {
                    this.rotationTimer.reset();
                    this.rotationVector = vec3d5;
                    this.rotations = RotationManager.calculateAngle(CrystalBot.mc.player.getPositionEyes(1.0f), this.rotationVector);
                }
                if (enumFacing != null) {
                    return enumFacing;
                }
            }
        } else {
            Vec3d vec3d;
            EnumFacing enumFacing2 = null;
            Vec3d vec3d7 = null;
            for (EnumFacing enumFacing3 : EnumFacing.values()) {
                vec3d = new Vec3d((double)blockPos.getX() + 0.5 + (double)enumFacing3.getDirectionVec().getX() * 0.5, (double)blockPos.getY() + 0.5 + (double)enumFacing3.getDirectionVec().getY() * 0.5, (double)blockPos.getZ() + 0.5 + (double)enumFacing3.getDirectionVec().getZ() * 0.5);
                RayTraceResult rayTraceResult = CrystalBot.mc.world.rayTraceBlocks(new Vec3d(CrystalBot.mc.player.posX, CrystalBot.mc.player.posY + (double)CrystalBot.mc.player.getEyeHeight(), CrystalBot.mc.player.posZ), vec3d, false, true, false);
                if (rayTraceResult == null || !rayTraceResult.typeOfHit.equals(RayTraceResult.Type.BLOCK) || !rayTraceResult.getBlockPos().equals(blockPos)) continue;
                if (this.strictDirection.getValue()) {
                    if (vec3d7 != null && !(CrystalBot.mc.player.getPositionVector().add(0.0, CrystalBot.mc.player.getEyeHeight(), 0.0).distanceTo(vec3d) < CrystalBot.mc.player.getPositionVector().add(0.0, CrystalBot.mc.player.getEyeHeight(), 0.0).distanceTo(vec3d7))) continue;
                    vec3d7 = vec3d;
                    enumFacing2 = enumFacing3;
                    this.postResult = rayTraceResult;
                    continue;
                }
                this.rotationTimer.reset();
                this.rotationVector = vec3d;
                this.rotations = RotationManager.calculateAngle(CrystalBot.mc.player.getPositionEyes(1.0f), this.rotationVector);
                return enumFacing3;
            }
            if (enumFacing2 != null) {
                this.rotationTimer.reset();
                this.rotationVector = vec3d7;
                this.rotations = RotationManager.calculateAngle(CrystalBot.mc.player.getPositionEyes(1.0f), this.rotationVector);
                return enumFacing2;
            }
            if (this.strictDirection.getValue()) {
                for (EnumFacing enumFacing3 : EnumFacing.values()) {
                    vec3d = new Vec3d((double)blockPos.getX() + 0.5 + (double)enumFacing3.getDirectionVec().getX() * 0.5, (double)blockPos.getY() + 0.5 + (double)enumFacing3.getDirectionVec().getY() * 0.5, (double)blockPos.getZ() + 0.5 + (double)enumFacing3.getDirectionVec().getZ() * 0.5);
                    if (vec3d7 != null && !(CrystalBot.mc.player.getPositionVector().add(0.0, CrystalBot.mc.player.getEyeHeight(), 0.0).distanceTo(vec3d) < CrystalBot.mc.player.getPositionVector().add(0.0, CrystalBot.mc.player.getEyeHeight(), 0.0).distanceTo(vec3d7))) continue;
                    vec3d7 = vec3d;
                    enumFacing2 = enumFacing3;
                }
                if (enumFacing2 != null) {
                    this.rotationTimer.reset();
                    this.rotationVector = vec3d7;
                    this.rotations = RotationManager.calculateAngle(CrystalBot.mc.player.getPositionEyes(1.0f), this.rotationVector);
                    return enumFacing2;
                }
            }
        }
        if ((double)blockPos.getY() > CrystalBot.mc.player.posY + (double)CrystalBot.mc.player.getEyeHeight()) {
            this.rotationTimer.reset();
            this.rotationVector = new Vec3d((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.0, (double)blockPos.getZ() + 0.5);
            this.rotations = RotationManager.calculateAngle(CrystalBot.mc.player.getPositionEyes(1.0f), this.rotationVector);
            return EnumFacing.DOWN;
        }
        this.rotationTimer.reset();
        this.rotationVector = new Vec3d((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.0, (double)blockPos.getZ() + 0.5);
        this.rotations = RotationManager.calculateAngle(CrystalBot.mc.player.getPositionEyes(1.0f), this.rotationVector);
        return EnumFacing.UP;
    }

    private void handleSequential() {
        if (CrystalBot.mc.player.getHealth() + CrystalBot.mc.player.getAbsorptionAmount() < this.disableUnderHealth.getValue() || this.noGapSwitch.getValue() && EntityUtil.isEating() || this.noMineSwitch.getValue() && CrystalBot.mc.playerController.getIsHittingBlock() && CrystalBot.mc.player.getHeldItemMainhand().getItem() instanceof ItemTool || !switchTimer.passedMs(this.switchCooldown.getValue())) {
            this.rotationVector = null;
            return;
        }
        if (this.noGapSwitch.getValue() && this.rightClickGap.getValue() && CrystalBot.mc.gameSettings.keyBindUseItem.isKeyDown() && CrystalBot.mc.player.inventory.getCurrentItem().getItem() instanceof ItemEndCrystal) {
            int n = -1;
            for (int i = 0; i < 9; ++i) {
                if (CrystalBot.mc.player.inventory.getStackInSlot(i).getItem() != Items.GOLDEN_APPLE) continue;
                n = i;
                break;
            }
            if (n != -1 && n != CrystalBot.mc.player.inventory.currentItem) {
                CrystalBot.mc.player.inventory.currentItem = n;
                CrystalBot.mc.player.connection.sendPacket(new CPacketHeldItemChange(n));
                return;
            }
        }
        if (!this.isOffhand() && !(CrystalBot.mc.player.inventory.getCurrentItem().getItem() instanceof ItemEndCrystal) && this.autoSwap.getValue() == ACSwapMode.OFF) {
            return;
        }
        List<EntityPlayer> list = this.getTargetsInRange();
        EntityEnderCrystal entityEnderCrystal = this.findCrystalTarget(list);
        int n = (int)Math.max(100.0f, (float)(CrystalUtil.ping() + 50)) + 150;
        if (entityEnderCrystal != null && this.breakTimer.passedMs((long)(1000.0f - this.breakSpeed.getValue() * 50.0f)) && ((float)entityEnderCrystal.ticksExisted >= this.delay.getValue() || this.timingMode.getValue() == TimingMode.VANILLA)) {
            this.postBreakPos = entityEnderCrystal;
            this.handleBreakRotation(this.postBreakPos.posX, this.postBreakPos.posY, this.postBreakPos.posZ);
        }
        if (entityEnderCrystal == null && (this.confirm.getValue() != ConfirmMode.FULL || this.inhibitEntity == null || (double)this.inhibitEntity.ticksExisted >= (double)this.delay.getValue().intValue()) && (this.syncMode.getValue() != SyncMode.STRICT || this.breakTimer.passedMs((long)(950.0f - this.breakSpeed.getValue() * 50.0f - (float)CrystalUtil.ping()))) && this.placeTimer.passedMs((long)(1000.0f - this.placeSpeed.getValue() * 50.0f)) && (this.timingMode.getValue() == TimingMode.SEQUENTIAL || this.linearTimer.passedMs((long)((float)this.delay.getValue().intValue() * 5.0f)))) {
            BlockPos blockPos;
            if (this.confirm.getValue() != ConfirmMode.OFF && this.cachePos != null && !this.cacheTimer.passedMs(n + 100) && this.canPlaceCrystal(this.cachePos)) {
                this.postPlacePos = this.cachePos;
                this.postFacing = this.handlePlaceRotation(this.postPlacePos);
                this.lastBroken.set(false);
                return;
            }
            List<BlockPos> list2 = this.findCrystalBlocks();
            if (!list2.isEmpty() && (blockPos = this.findPlacePosition(list2, list)) != null) {
                this.postPlacePos = blockPos;
                this.postFacing = this.handlePlaceRotation(this.postPlacePos);
            }
        }
        this.lastBroken.set(false);
    }

    @Override
    public void onEnable() {
        this.postBreakPos = null;
        this.postPlacePos = null;
        this.postFacing = null;
        this.postResult = null;
        this.cachePos = null;
        this.bilateralVec = null;
        this.lastBroken.set(false);
        this.rotationVector = null;
        this.rotationTimer.reset();
        this.isPlacing = false;
        this.foundDoublePop = false;
        this.totemPops.clear();
        this.oldSlotCrystal = -1;
        this.oldSlotSword = -1;
    }

    public boolean canPlaceCrystal(BlockPos blockPos) {
        if (CrystalBot.mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && CrystalBot.mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
            return false;
        }
        BlockPos blockPos2 = blockPos.add(0, 1, 0);
        if (!(CrystalBot.mc.world.getBlockState(blockPos2).getBlock() == Blocks.AIR || CrystalBot.mc.world.getBlockState(blockPos2).getBlock() == Blocks.FIRE && this.fire.getValue() || CrystalBot.mc.world.getBlockState(blockPos2).getBlock() instanceof BlockLiquid && this.liquids.getValue())) {
            return false;
        }
        BlockPos blockPos3 = blockPos.add(0, 2, 0);
        if (!(this.protocol.getValue() || CrystalBot.mc.world.getBlockState(blockPos3).getBlock() == Blocks.AIR || CrystalBot.mc.world.getBlockState(blockPos2).getBlock() instanceof BlockLiquid && this.liquids.getValue())) {
            return false;
        }
        if (this.check.getValue() && !CrystalUtil.rayTraceBreak((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.0, (double)blockPos.getZ() + 0.5)) {
            Vec3d vec3d = new Vec3d((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.0, (double)blockPos.getZ() + 0.5);
            if (CrystalBot.mc.player.getPositionEyes(1.0f).distanceTo(vec3d) > (double) this.breakWallsRange.getValue()) {
                return false;
            }
        }
        if (this.placeWallsRange.getValue() < this.placeRange.getValue()) {
            if (!CrystalUtil.rayTracePlace(blockPos)) {
                if (this.strictDirection.getValue()) {
                    boolean bl;
                    block26: {
                        Vec3d vec3d = CrystalBot.mc.player.getPositionVector().add(0.0, CrystalBot.mc.player.getEyeHeight(), 0.0);
                        bl = false;
                        if (this.directionMode.getValue() == DirectionMode.VANILLA) {
                            for (EnumFacing enumFacing : EnumFacing.values()) {
                                Vec3d vec3d2 = new Vec3d((double)blockPos.getX() + 0.5 + (double)enumFacing.getDirectionVec().getX() * 0.5, (double)blockPos.getY() + 0.5 + (double)enumFacing.getDirectionVec().getY() * 0.5, (double)blockPos.getZ() + 0.5 + (double)enumFacing.getDirectionVec().getZ() * 0.5);
                                if (!(vec3d.distanceTo(vec3d2) <= (double) this.placeWallsRange.getValue())) continue;
                                bl = true;
                                break;
                            }
                        } else {
                            double d = 0.45;
                            double d2 = 0.05;
                            double d3 = 0.95;
                            for (double d4 = d2; d4 <= d3; d4 += d) {
                                for (double d5 = d2; d5 <= d3; d5 += d) {
                                    for (double d6 = d2; d6 <= d3; d6 += d) {
                                        Vec3d vec3d3 = new Vec3d(blockPos).add(d4, d5, d6);
                                        double d7 = vec3d.distanceTo(vec3d3);
                                        if (!(d7 <= (double) this.placeWallsRange.getValue())) continue;
                                        bl = true;
                                        break block26;
                                    }
                                }
                            }
                        }
                    }
                    if (!bl) {
                        return false;
                    }
                } else if ((double)blockPos.getY() > CrystalBot.mc.player.posY + (double)CrystalBot.mc.player.getEyeHeight() ? CrystalBot.mc.player.getDistance((double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5) > (double) this.placeWallsRange.getValue() : CrystalBot.mc.player.getDistance((double)blockPos.getX() + 0.5, blockPos.getY() + 1, (double)blockPos.getZ() + 0.5) > (double) this.placeWallsRange.getValue()) {
                    return false;
                }
            }
        } else if (this.strictDirection.getValue()) {
            boolean bl;
            block27: {
                Vec3d vec3d = CrystalBot.mc.player.getPositionVector().add(0.0, CrystalBot.mc.player.getEyeHeight(), 0.0);
                bl = false;
                if (this.directionMode.getValue() == DirectionMode.VANILLA) {
                    for (EnumFacing enumFacing : EnumFacing.values()) {
                        Vec3d vec3d4 = new Vec3d((double)blockPos.getX() + 0.5 + (double)enumFacing.getDirectionVec().getX() * 0.5, (double)blockPos.getY() + 0.5 + (double)enumFacing.getDirectionVec().getY() * 0.5, (double)blockPos.getZ() + 0.5 + (double)enumFacing.getDirectionVec().getZ() * 0.5);
                        if (!(vec3d.distanceTo(vec3d4) <= (double) this.placeRange.getValue())) continue;
                        bl = true;
                        break;
                    }
                } else {
                    double d = 0.45;
                    double d8 = 0.05;
                    double d9 = 0.95;
                    for (double d10 = d8; d10 <= d9; d10 += d) {
                        for (double d11 = d8; d11 <= d9; d11 += d) {
                            for (double d12 = d8; d12 <= d9; d12 += d) {
                                Vec3d vec3d5 = new Vec3d(blockPos).add(d10, d11, d12);
                                double d13 = vec3d.distanceTo(vec3d5);
                                if (!(d13 <= (double) this.placeRange.getValue())) continue;
                                bl = true;
                                break block27;
                            }
                        }
                    }
                }
            }
            if (!bl) {
                return false;
            }
        }
        return CrystalBot.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blockPos2, blockPos3.add(1, 1, 1))).stream().noneMatch(entity -> !this.breakLocations.containsKey(entity.getEntityId()) && (!(entity instanceof EntityEnderCrystal) || entity.ticksExisted > 20));
    }

    private List<EntityPlayer> getTargetsInRange() {
        List<EntityPlayer> list = CrystalBot.mc.world.playerEntities.stream().filter(entityPlayer -> entityPlayer != CrystalBot.mc.player && entityPlayer != CrystalBot.mc.getRenderViewEntity()).filter(e -> !e.isDead).filter(e -> !Managers.FRIENDS.isFriend(e.getName())).filter(e -> e.getHealth() > 0.0f).filter(e -> CrystalBot.mc.player.getDistance(e) < this.enemyRange.getValue()).sorted(Comparator.comparing(e -> CrystalBot.mc.player.getDistance(e))).collect(Collectors.toList());
        if (this.targetingMode.getValue() == TargetingMode.SMART) {
            final List<EntityPlayer> list2 = list.stream().filter(entityPlayer -> entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount() < 10.0f).sorted(Comparator.comparing(entityPlayer -> CrystalBot.mc.player.getDistance(entityPlayer))).collect(Collectors.toList());
            if (list2.size() > 0) {
                list = list2;
            }
        }
        return list;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (!this.renderTargetTimer.passedMs(this.startFadeTime.getValue())) {
            this.fadeUtils.reset();
        }
        this.fadeUtils.setLength(this.fadeTime.getValue());
        if (this.fadeUtils.easeOutQuad() == 1.0) {
            return;
        }
        if (this.renderBlock != null && this.Actualp.getValue()) {
            double size = Math.abs(this.fadeUtils.easeOutQuad() - 1.0);
            if (!this.renderTargetTimer.passedMs(this.startFadeTime.getValue()) || this.renderMode.getValue() == RenderMode.FADE) {
                size = 1.0;
            }
            Color boxC = ColorUtil.injectAlpha(this.boxColor.getValue(), (int)((double)this.boxColor.getValue().getAlpha() * size));
            Color outlineC = ColorUtil.injectAlpha(this.outlineColor.getValue(), (int)((double)this.outlineColor.getValue().getAlpha() * size));
            if (this.renderBlock != null && (this.boxColor.booleanValue || this.outlineColor.booleanValue)) {
                if (this.renderMode.getValue() == RenderMode.FADE) {
                    this.positions.removeIf(currentPos2 -> currentPos2.renderBlock().equals(this.renderBlock));
                    this.positions.add(new currentPos(this.renderBlock, 0.0f));
                }
                if (this.renderMode.getValue() == RenderMode.STATIC) {
                    RenderUtil.drawSexyBoxPhobosIsRetardedFuckYouESP(new AxisAlignedBB(this.renderBlock), boxC, outlineC, this.lineWidth.getValue(), this.outlineColor.booleanValue, this.boxColor.booleanValue, this.colorSync.getValue(), 1.0f, 1.0f, this.slabHeight.getValue());
                }
                if (this.renderMode.getValue() == RenderMode.GLIDE) {
                    if (this.positions.size() > this.max.getValue()) {
                        this.positions.remove(0);
                    }
                    if (this.lastpos == null || CrystalBot.mc.player.getDistance(this.renderBB.minX, this.renderBB.minY, this.renderBB.minZ) > (double) this.range.getValue()) {
                        this.lastpos = this.renderBlock;
                        this.renderBB = new AxisAlignedBB(this.renderBlock);
                        this.timePassed = 0.0f;
                    }
                    if (!this.lastpos.equals(this.renderBlock)) {
                        this.lastpos = this.renderBlock;
                        this.timePassed = 0.0f;
                    }
                    double xDiff = (double)this.renderBlock.getX() - this.renderBB.minX;
                    double yDiff = (double)this.renderBlock.getY() - this.renderBB.minY;
                    double zDiff = (double)this.renderBlock.getZ() - this.renderBB.minZ;
                    float multiplier = this.timePassed / this.moveSpeed.getValue() * this.accel.getValue();
                    if (multiplier > 1.0f) {
                        multiplier = 1.0f;
                    }
                    this.renderBB = this.renderBB.offset(xDiff * (double)multiplier, yDiff * (double)multiplier, zDiff * (double)multiplier);
                    RenderUtil.drawSexyBoxPhobosIsRetardedFuckYouESP(this.renderBB, boxC, outlineC, this.lineWidth.getValue(), this.outlineColor.booleanValue, this.boxColor.booleanValue, this.colorSync.getValue(), 1.0f, 1.0f, this.slabHeight.getValue());
                    if (this.text.getValue()) {
                        RenderUtil.drawText(this.renderBB.offset(0.0, (double)(1.0f - this.slabHeight.getValue() / 2.0f) - 0.4, 0.0), String.valueOf(Math.floor(this.renderDamage) == (double)this.renderDamage ? Integer.valueOf((int)this.renderDamage) : String.format("%.1f", this.renderDamage)));
                    }
                    this.timePassed = this.renderBB.equals(new AxisAlignedBB(this.renderBlock)) ? 0.0f : (this.timePassed += 50.0f);
                }
            }
            if (this.renderMode.getValue() == RenderMode.FADE) {
                this.positions.forEach(breakPos -> {
                    float factor = (this.duration.getValue() - breakPos.getRenderTime()) / this.duration.getValue();
                    RenderUtil.drawSexyBoxPhobosIsRetardedFuckYouESP(new AxisAlignedBB(breakPos.renderBlock()), boxC, outlineC, this.lineWidth.getValue(), this.outlineColor.booleanValue, this.boxColor.booleanValue, this.colorSync.getValue(), this.fadeFactor.getValue() ? factor : 1.0f, this.scaleFactor.getValue() ? factor : 1.0f, this.slabFactor.getValue() ? factor : 1.0f);
                    breakPos.setRenderTime(breakPos.getRenderTime() + 5.0f);
                });
                this.positions.removeIf(pos -> pos.getRenderTime() >= this.duration.getValue() || CrystalBot.mc.world.isAirBlock(pos.renderBlock()) || !CrystalBot.mc.world.isAirBlock(pos.renderBlock().up()));
                if (this.positions.size() > this.max.getValue()) {
                    this.positions.remove(0);
                }
            }
            if (this.renderBlock != null && this.text.getValue() && this.renderMode.getValue() != RenderMode.GLIDE) {
                RenderUtil.drawText(new AxisAlignedBB(this.renderBlock).offset(0.0, this.renderMode.getValue() != RenderMode.FADE ? (double)(1.0f - this.slabHeight.getValue() / 2.0f) - 0.4 : 0.1, 0.0), String.valueOf(Math.floor(this.renderDamage) == (double)this.renderDamage ? Integer.valueOf((int)this.renderDamage) : String.format("%.1f", this.renderDamage)));
            }
        }
        if (this.renderTarget != null) {
            if (this.targetRender.getValue() == TargetRenderMode.OLD) {
                RenderUtil.drawEntityBoxESP(this.renderTarget, this.targetColor.getValue(), true, new Color(255, 255, 255, 130), 0.7f, true, true, 35);
            } else if (this.targetRender.getValue() == TargetRenderMode.JELLO) {
                double everyTime = 1500.0;
                double drawTime = (double)System.currentTimeMillis() % everyTime;
                boolean drawMode = drawTime > everyTime / 2.0;
                double drawPercent = drawTime / (everyTime / 2.0);
                drawPercent = !drawMode ? 1.0 - drawPercent : (drawPercent -= 1.0);
                drawPercent = this.easeInOutQuad(drawPercent);
                CrystalBot.mc.entityRenderer.disableLightmap();
                GL11.glPushMatrix();
                GL11.glDisable(3553);
                GL11.glBlendFunc(770, 771);
                GL11.glEnable(2848);
                GL11.glEnable(3042);
                GL11.glDisable(2929);
                GL11.glDisable(2884);
                GL11.glShadeModel(7425);
                CrystalBot.mc.entityRenderer.disableLightmap();
                double radius = this.renderTarget.width;
                double height = (double)this.renderTarget.height + 0.1;
                double x = this.renderTarget.lastTickPosX + (this.renderTarget.posX - this.renderTarget.lastTickPosX) * (double)mc.getRenderPartialTicks() - CrystalBot.mc.renderManager.viewerPosX;
                double y = this.renderTarget.lastTickPosY + (this.renderTarget.posY - this.renderTarget.lastTickPosY) * (double)mc.getRenderPartialTicks() - CrystalBot.mc.renderManager.viewerPosY + height * drawPercent;
                double z = this.renderTarget.lastTickPosZ + (this.renderTarget.posZ - this.renderTarget.lastTickPosZ) * (double)mc.getRenderPartialTicks() - CrystalBot.mc.renderManager.viewerPosZ;
                double eased = height / 3.0 * (drawPercent > 0.5 ? 1.0 - drawPercent : drawPercent) * (double)(drawMode ? -1 : 1);
                for (int segments = 0; segments < 360; segments += 5) {
                    Color color = this.targetColor.getValue();
                    double x1 = x - Math.sin((double)segments * Math.PI / 180.0) * radius;
                    double z1 = z + Math.cos((double)segments * Math.PI / 180.0) * radius;
                    double x2 = x - Math.sin((double)(segments - 5) * Math.PI / 180.0) * radius;
                    double z2 = z + Math.cos((double)(segments - 5) * Math.PI / 180.0) * radius;
                    GL11.glBegin(7);
                    GL11.glColor4f(ColorUtil.pulseColor(color, 200, 1).getRed() / 255.0f, ColorUtil.pulseColor(color, 200, 1).getGreen() / 255.0f, ColorUtil.pulseColor(color, 200, 1).getBlue() / 255.0f, 0.0f);
                    GL11.glVertex3d(x1, y + eased, z1);
                    GL11.glVertex3d(x2, y + eased, z2);
                    GL11.glColor4f(ColorUtil.pulseColor(color, 200, 1).getRed() / 255.0f, ColorUtil.pulseColor(color, 200, 1).getGreen() / 255.0f, ColorUtil.pulseColor(color, 200, 1).getBlue() / 255.0f, 200.0f);
                    GL11.glVertex3d(x2, y, z2);
                    GL11.glVertex3d(x1, y, z1);
                    GL11.glEnd();
                    GL11.glBegin(2);
                    GL11.glVertex3d(x2, y, z2);
                    GL11.glVertex3d(x1, y, z1);
                    GL11.glEnd();
                }
                GL11.glEnable(2884);
                GL11.glShadeModel(7424);
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                GL11.glEnable(2929);
                GL11.glDisable(2848);
                GL11.glDisable(3042);
                GL11.glEnable(3553);
                GL11.glPopMatrix();
            }
        }
    }

    private double easeInOutQuad(double x) {
        return x < 0.5 ? 2.0 * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 2.0) / 2.0;
    }

    private int getSwingAnimTime(EntityLivingBase entityLivingBase) {
        if (entityLivingBase.isPotionActive(MobEffects.HASTE)) {
            return 6 - (1 + Objects.requireNonNull(entityLivingBase.getActivePotionEffect(MobEffects.HASTE)).getAmplifier());
        }
        return entityLivingBase.isPotionActive(MobEffects.MINING_FATIGUE) ? 6 + (1 + Objects.requireNonNull(entityLivingBase.getActivePotionEffect(MobEffects.MINING_FATIGUE)).getAmplifier()) * 2 : 6;
    }

    public void handleBreakRotation(double d, double d2, double d3) {
        if (this.rotationMode.getValue() != RotationMode.OFF) {
            if (this.rotationMode.getValue() == RotationMode.INTERACT && this.rotationVector != null && !this.rotationTimer.passedMs(650L)) {
                if (this.rotationVector.y < d2 - 0.1) {
                    this.rotationVector = new Vec3d(this.rotationVector.x, d2, this.rotationVector.z);
                }
                this.rotations = RotationManager.calculateAngle(CrystalBot.mc.player.getPositionEyes(1.0f), this.rotationVector);
                this.rotationTimer.reset();
                return;
            }
            AxisAlignedBB axisAlignedBB = new AxisAlignedBB(d - 1.0, d2, d3 - 1.0, d + 1.0, d2 + 2.0, d3 + 1.0);
            Vec3d vec3d = new Vec3d(CrystalBot.mc.player.posX, CrystalBot.mc.player.getEntityBoundingBox().minY + (double)CrystalBot.mc.player.getEyeHeight(), CrystalBot.mc.player.posZ);
            double d4 = 0.1;
            double d5 = 0.15;
            double d6 = 0.85;
            if (axisAlignedBB.intersects(CrystalBot.mc.player.getEntityBoundingBox())) {
                d5 = 0.4;
                d6 = 0.6;
                d4 = 0.05;
            }
            Vec3d vec3d2 = null;
            double[] arrd = null;
            boolean bl = false;
            for (double d7 = d5; d7 <= d6; d7 += d4) {
                for (double d8 = d5; d8 <= d6; d8 += d4) {
                    for (double d9 = d5; d9 <= d6; d9 += d4) {
                        boolean bl2;
                        Vec3d vec3d3 = new Vec3d(axisAlignedBB.minX + (axisAlignedBB.maxX - axisAlignedBB.minX) * d7, axisAlignedBB.minY + (axisAlignedBB.maxY - axisAlignedBB.minY) * d8, axisAlignedBB.minZ + (axisAlignedBB.maxZ - axisAlignedBB.minZ) * d9);
                        double d10 = vec3d3.x - vec3d.x;
                        double d11 = vec3d3.y - vec3d.y;
                        double d12 = vec3d3.z - vec3d.z;
                        double[] arrd2 = new double[]{MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(d12, d10)) - 90.0f), MathHelper.wrapDegrees(-Math.toDegrees(Math.atan2(d11, Math.sqrt(d10 * d10 + d12 * d12))))};
                        boolean bl3 = bl2 = this.directionMode.getValue() == DirectionMode.VANILLA || CrystalUtil.isVisible(vec3d3);
                        if (this.strictDirection.getValue()) {
                            if (vec3d2 != null) {
                                if (!bl2 && bl || !(CrystalBot.mc.player.getPositionVector().add(0.0, CrystalBot.mc.player.getEyeHeight(), 0.0).distanceTo(vec3d3) < CrystalBot.mc.player.getPositionVector().add(0.0, CrystalBot.mc.player.getEyeHeight(), 0.0).distanceTo(vec3d2))) continue;
                                vec3d2 = vec3d3;
                                arrd = arrd2;
                                continue;
                            }
                            vec3d2 = vec3d3;
                            arrd = arrd2;
                            bl = bl2;
                            continue;
                        }
                        if (vec3d2 != null) {
                            if (!bl2 && bl || !(Math.hypot(((arrd2[0] - (double)((IEntityPlayerSP)CrystalBot.mc.player).getLastReportedYaw()) % 360.0 + 540.0) % 360.0 - 180.0, arrd2[1] - (double)((IEntityPlayerSP)CrystalBot.mc.player).getLastReportedPitch()) < Math.hypot(((arrd[0] - (double)((IEntityPlayerSP)CrystalBot.mc.player).getLastReportedYaw()) % 360.0 + 540.0) % 360.0 - 180.0, arrd[1] - (double)((IEntityPlayerSP)CrystalBot.mc.player).getLastReportedPitch()))) continue;
                            vec3d2 = vec3d3;
                            arrd = arrd2;
                            continue;
                        }
                        vec3d2 = vec3d3;
                        arrd = arrd2;
                        bl = bl2;
                    }
                }
            }
            this.rotationTimer.reset();
            this.rotationVector = vec3d2;
            this.rotations = RotationManager.calculateAngle(CrystalBot.mc.player.getPositionEyes(1.0f), this.rotationVector);
        }
    }

    private boolean shouldArmorBreak(EntityPlayer entityPlayer) {
        if (!this.armorBreaker.getValue()) {
            return false;
        }
        for (int i = 3; i >= 0; --i) {
            ItemStack itemStack = entityPlayer.inventory.armorInventory.get(i);
            if (!(itemStack.getItem().getDurabilityForDisplay(itemStack) > (double) this.depletion.getValue())) continue;
            return true;
        }
        return false;
    }

    private void swingArmAfterBreaking(EnumHand enumHand) {
        if (!this.swing.getValue()) {
            return;
        }
        ItemStack itemStack = CrystalBot.mc.player.getHeldItem(enumHand);
        if (!itemStack.isEmpty() && itemStack.getItem().onEntitySwing(CrystalBot.mc.player, itemStack)) {
            return;
        }
        if (!CrystalBot.mc.player.isSwingInProgress || CrystalBot.mc.player.swingProgressInt >= this.getSwingAnimTime(CrystalBot.mc.player) / 2 || CrystalBot.mc.player.swingProgressInt < 0) {
            CrystalBot.mc.player.swingProgressInt = -1;
            CrystalBot.mc.player.isSwingInProgress = true;
            CrystalBot.mc.player.swingingHand = enumHand;
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayerPost(UpdateWalkingPlayerEvent updateWalkingPlayerEvent) {
        if (CrystalBot.fullNullCheck()) {
            return;
        }
        aboba = this.mergeOffset.getValue() / 10.0f;
        if (updateWalkingPlayerEvent.getStage() == 1) {
            if (this.postBreakPos != null) {
                if (this.breakCrystal(this.postBreakPos)) {
                    this.breakTimer.reset();
                    this.breakLocations.put(this.postBreakPos.getEntityId(), System.currentTimeMillis());
                    for (Entity entity : CrystalBot.mc.world.loadedEntityList) {
                        if (!(entity instanceof EntityEnderCrystal) || !(entity.getDistance(this.postBreakPos.posX, this.postBreakPos.posY, this.postBreakPos.posZ) <= 6.0)) continue;
                        this.breakLocations.put(entity.getEntityId(), System.currentTimeMillis());
                    }
                    this.postBreakPos = null;
                    if (this.syncMode.getValue() == SyncMode.MERGE) {
                        this.runInstantThread();
                    }
                }
            } else if (this.postPlacePos != null) {
                if (!this.placeCrystal(this.postPlacePos, this.postFacing)) {
                    this.shouldRunThread.set(false);
                    this.postPlacePos = null;
                    return;
                }
                this.placeTimer.reset();
                this.postPlacePos = null;
            }
        }
    }

    private void runInstantThread() {
        if (this.mergeOffset.getValue() == 0.0f) {
            this.doInstant();
        } else {
            this.shouldRunThread.set(true);
            if (this.thread == null || this.thread.isInterrupted() || !this.thread.isAlive()) {
                if (this.thread == null) {
                    this.thread = new Thread(InstantThread.getInstance(this));
                }
                if (this.thread.isInterrupted() || !this.thread.isAlive()) {
                    this.thread = new Thread(InstantThread.getInstance(this));
                }
                if (this.thread.getState() == Thread.State.NEW) {
                    try {
                        this.thread.start();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }
    }

    public boolean isOffhand() {
        return CrystalBot.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
    }

    private List<Entity> getCrystalInRange() {
        return CrystalBot.mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityEnderCrystal).filter(entity -> this.isValidCrystalTarget((EntityEnderCrystal)entity)).collect(Collectors.toList());
    }

    private boolean isValidCrystalTarget(EntityEnderCrystal entityEnderCrystal) {
        if (CrystalBot.mc.player.getPositionEyes(1.0f).distanceTo(entityEnderCrystal.getPositionVector()) > (double) this.breakRange.getValue()) {
            return false;
        }
        if (this.breakLocations.containsKey(entityEnderCrystal.getEntityId()) && this.limit.getValue()) {
            return false;
        }
        if (this.breakLocations.containsKey(entityEnderCrystal.getEntityId()) && (float)entityEnderCrystal.ticksExisted > this.delay.getValue() + (float) this.attackFactor.getValue()) {
            return false;
        }
        return !(CrystalUtil.calculateDamage(entityEnderCrystal, CrystalBot.mc.player) + this.suicideHealth.getValue() >= CrystalBot.mc.player.getHealth() + CrystalBot.mc.player.getAbsorptionAmount());
    }

    public boolean placeCrystal(BlockPos blockPos, EnumFacing enumFacing) {
        if (blockPos != null) {
            if (this.autoSwap.getValue() != ACSwapMode.OFF && InventoryUtil.findItemInHotbar(Items.END_CRYSTAL) == -1) {
                return false;
            }
            if (this.autoSwap.getValue() != ACSwapMode.OFF && !this.hasCrystal()) {
                return false;
            }
            if (!this.isOffhand() && CrystalBot.mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL) {
                if (this.oldSlotCrystal != -1) {
                    CrystalBot.mc.player.inventory.currentItem = this.oldSlotCrystal;
                    CrystalBot.mc.player.connection.sendPacket(new CPacketHeldItemChange(this.oldSlotCrystal));
                    this.oldSlotCrystal = -1;
                }
                return false;
            }
            if (CrystalBot.mc.world.getBlockState(blockPos.up()).getBlock() == Blocks.FIRE) {
                CrystalBot.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos.up(), EnumFacing.UP));
                CrystalBot.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos.up(), EnumFacing.UP));
                if (this.oldSlotCrystal != -1) {
                    CrystalBot.mc.player.inventory.currentItem = this.oldSlotCrystal;
                    CrystalBot.mc.player.connection.sendPacket(new CPacketHeldItemChange(this.oldSlotCrystal));
                    this.oldSlotCrystal = -1;
                }
                return true;
            }
            this.isPlacing = true;
            if (this.postResult == null) {
                BlockUtil.rightClickBlock(blockPos, CrystalBot.mc.player.getPositionVector().add(0.0, CrystalBot.mc.player.getEyeHeight(), 0.0), this.isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, enumFacing, true);
            } else {
                CrystalBot.mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(blockPos, enumFacing, this.isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, (float)this.postResult.hitVec.x, (float)this.postResult.hitVec.y, (float)this.postResult.hitVec.z));
                CrystalBot.mc.player.connection.sendPacket(new CPacketAnimation(this.isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
            }
            if (this.foundDoublePop && this.renderTarget != null) {
                this.totemPops.put(this.renderTarget, new Timer());
            }
            this.isPlacing = false;
            this.placeLocations.put(blockPos, System.currentTimeMillis());
            if (this.security.getValue() >= 0.5f) {
                this.selfPlacePositions.add(blockPos);
            }
            this.renderTimeoutTimer.reset();
            if (this.oldSlotCrystal != -1) {
                CrystalBot.mc.player.inventory.currentItem = this.oldSlotCrystal;
                CrystalBot.mc.player.connection.sendPacket(new CPacketHeldItemChange(this.oldSlotCrystal));
                this.oldSlotCrystal = -1;
            }
            return true;
        }
        return false;
    }

    private EntityEnderCrystal findCrystalTarget(List<EntityPlayer> list) {
        this.breakLocations.forEach((n, l) -> {
            if (System.currentTimeMillis() - l > 1000L) {
                this.breakLocations.remove(n);
            }
        });
        if (this.syncMode.getValue() == SyncMode.STRICT && !this.limit.getValue() && this.lastBroken.get()) {
            return null;
        }
        EntityEnderCrystal entityEnderCrystal = null;
        int n2 = (int)Math.max(100.0f, (float)(CrystalUtil.ping() + 50)) + 150;
        if (this.inhibit.getValue() && !this.limit.getValue() && !this.inhibitTimer.passedMs(n2) && this.inhibitEntity != null && CrystalBot.mc.world.getEntityByID(this.inhibitEntity.getEntityId()) != null && this.isValidCrystalTarget(this.inhibitEntity)) {
            entityEnderCrystal = this.inhibitEntity;
            return entityEnderCrystal;
        }
        List<Entity> list2 = this.getCrystalInRange();
        if (list2.isEmpty()) {
            return null;
        }
        if (this.security.getValue() >= 1.0f) {
            double d = 0.5;
            for (Entity entity2 : list2) {
                if (!(entity2.getPositionVector().distanceTo(CrystalBot.mc.player.getPositionEyes(1.0f)) < (double) this.breakWallsRange.getValue()) && !CrystalUtil.rayTraceBreak(entity2.posX, entity2.posY, entity2.posZ)) continue;
                EntityEnderCrystal entityEnderCrystal2 = (EntityEnderCrystal)entity2;
                double d2 = 0.0;
                for (EntityPlayer entityPlayer : list) {
                    double d3 = CrystalUtil.calculateDamage(entityEnderCrystal2, entityPlayer);
                    d2 += d3;
                }
                double d4 = CrystalUtil.calculateDamage(entityEnderCrystal2, CrystalBot.mc.player);
                if (d4 > d2 * (double)(this.security.getValue() - 0.8f) && !this.selfPlacePositions.contains(new BlockPos(entity2.posX, entity2.posY - 1.0, entity2.posZ)) || !(d2 > d)) continue;
                d = d2;
                entityEnderCrystal = entityEnderCrystal2;
            }
        } else {
            entityEnderCrystal = this.security.getValue() >= 0.5f ? (EntityEnderCrystal)list2.stream().filter(entity -> this.selfPlacePositions.contains(new BlockPos(entity.posX, entity.posY - 1.0, entity.posZ))).filter(entity -> entity.getPositionVector().distanceTo(CrystalBot.mc.player.getPositionEyes(1.0f)) < (double) this.breakWallsRange.getValue() || CrystalUtil.rayTraceBreak(entity.posX, entity.posY, entity.posZ)).min(Comparator.comparing(entity -> CrystalBot.mc.player.getDistance(entity))).orElse(null) : (EntityEnderCrystal)list2.stream().filter(entity -> entity.getPositionVector().distanceTo(CrystalBot.mc.player.getPositionEyes(1.0f)) < (double) this.breakWallsRange.getValue() || CrystalUtil.rayTraceBreak(entity.posX, entity.posY, entity.posZ)).min(Comparator.comparing(entity -> CrystalBot.mc.player.getDistance(entity))).orElse(null);
        }
        return entityEnderCrystal;
    }

    private double getDistance(double d, double d2, double d3, double d4, double d5, double d6) {
        double d7 = d - d4;
        double d8 = d2 - d5;
        double d9 = d3 - d6;
        return Math.sqrt(d7 * d7 + d8 * d8 + d9 * d9);
    }

    private void doInstant() {
        BlockPos blockPos;
        List<BlockPos> list;
        if (this.confirm.getValue() != ConfirmMode.OFF && (this.confirm.getValue() != ConfirmMode.FULL || this.inhibitEntity == null || (double)this.inhibitEntity.ticksExisted >= (double)this.delay.getValue().intValue())) {
            int n = (int)Math.max(100.0f, (float)(CrystalUtil.ping() + 50)) + 150;
            if (this.cachePos != null && !this.cacheTimer.passedMs(n + 100) && this.canPlaceCrystal(this.cachePos)) {
                this.postPlacePos = this.cachePos;
                this.postFacing = this.handlePlaceRotation(this.postPlacePos);
                if (this.postPlacePos != null) {
                    if (!this.placeCrystal(this.postPlacePos, this.postFacing)) {
                        this.postPlacePos = null;
                        return;
                    }
                    this.placeTimer.reset();
                    this.postPlacePos = null;
                }
                return;
            }
        }
        if (!(list = this.findCrystalBlocks()).isEmpty() && (blockPos = this.findPlacePosition(list, this.getTargetsInRange())) != null) {
            this.postPlacePos = blockPos;
            this.postFacing = this.handlePlaceRotation(this.postPlacePos);
            if (this.postPlacePos != null) {
                if (!this.placeCrystal(this.postPlacePos, this.postFacing)) {
                    this.postPlacePos = null;
                    return;
                }
                this.placeTimer.reset();
                this.postPlacePos = null;
            }
        }
    }

    @Override
    public void onDisable() {
        this.positions.clear();
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onReceivePacket(PacketEvent.Receive receive) {
        SPacketEntityStatus sPacketEntityStatus;
        if (receive.getPacket() instanceof SPacketSpawnObject) {
            SPacketSpawnObject sPacketSpawnObject = receive.getPacket();
            if (sPacketSpawnObject.getType() == 51) {
                this.placeLocations.forEach((blockPos, l) -> {
                    if (this.getDistance((double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5, sPacketSpawnObject.getX(), sPacketSpawnObject.getY() - 1.0, sPacketSpawnObject.getZ()) < 1.0) {
                        try {
                            this.placeLocations.remove(blockPos);
                            this.cachePos = null;
                            if (!this.limit.getValue() && this.inhibit.getValue()) {
                                this.scatterTimer.reset();
                            }
                        }
                        catch (ConcurrentModificationException concurrentModificationException) {
                            // empty catch block
                        }
                        if (this.timingMode.getValue() != TimingMode.VANILLA) {
                            return;
                        }
                        if (!this.swapTimer.passedMs((long)(this.swapDelay.getValue() * 100.0f))) {
                            return;
                        }
                        if (this.tickRunning.get()) {
                            return;
                        }
                        if (CrystalBot.mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                            return;
                        }
                        if (this.breakLocations.containsKey(sPacketSpawnObject.getEntityID())) {
                            return;
                        }
                        if (CrystalBot.mc.player.getHealth() + CrystalBot.mc.player.getAbsorptionAmount() < this.disableUnderHealth.getValue() || this.noGapSwitch.getValue() && EntityUtil.isEating() || this.noMineSwitch.getValue() && CrystalBot.mc.playerController.getIsHittingBlock() && CrystalBot.mc.player.getHeldItemMainhand().getItem() instanceof ItemTool) {
                            this.rotationVector = null;
                            return;
                        }
                        Vec3d vec3d = new Vec3d(sPacketSpawnObject.getX(), sPacketSpawnObject.getY(), sPacketSpawnObject.getZ());
                        if (CrystalBot.mc.player.getPositionEyes(1.0f).distanceTo(vec3d) > (double) this.breakRange.getValue()) {
                            return;
                        }
                        if (!this.breakTimer.passedMs((long)(1000.0f - this.breakSpeed.getValue() * 50.0f))) {
                            return;
                        }
                        if (CrystalUtil.calculateDamage(sPacketSpawnObject.getX(), sPacketSpawnObject.getY(), sPacketSpawnObject.getZ(), CrystalBot.mc.player) + this.suicideHealth.getValue() >= CrystalBot.mc.player.getHealth() + CrystalBot.mc.player.getAbsorptionAmount()) {
                            return;
                        }
                        this.breakLocations.put(sPacketSpawnObject.getEntityID(), System.currentTimeMillis());
                        this.bilateralVec = new Vec3d(sPacketSpawnObject.getX(), sPacketSpawnObject.getY(), sPacketSpawnObject.getZ());
                        CPacketUseEntity predict = new CPacketUseEntity();
                        SPacketSpawnObject packet = receive.getPacket();
                        predict.entityId = packet.getEntityID();
                        predict.action = CPacketUseEntity.Action.ATTACK;
                        CrystalBot.mc.player.connection.sendPacket(predict);
                        CrystalBot.mc.player.connection.sendPacket(new CPacketAnimation(this.isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
                        this.swingArmAfterBreaking(this.isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                        this.renderBreakingPos = new BlockPos(sPacketSpawnObject.getX(), sPacketSpawnObject.getY() - 1.0, sPacketSpawnObject.getZ());
                        this.renderBreakingTimer.reset();
                        this.breakTimer.reset();
                        this.linearTimer.reset();
                        if (this.syncMode.getValue() == SyncMode.MERGE) {
                            this.placeTimer.reset();
                        }
                        if (this.syncMode.getValue() == SyncMode.STRICT) {
                            this.lastBroken.set(true);
                        }
                        if (this.syncMode.getValue() == SyncMode.MERGE) {
                            this.runInstantThread();
                        }
                    }
                });
            }
        } else if (receive.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect sPacketSoundEffect = receive.getPacket();
            if (sPacketSoundEffect.getCategory() == SoundCategory.BLOCKS && sPacketSoundEffect.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                if (this.inhibitEntity != null && this.inhibitEntity.getDistance(sPacketSoundEffect.getX(), sPacketSoundEffect.getY(), sPacketSoundEffect.getZ()) < 6.0) {
                    this.inhibitEntity = null;
                }
                if (this.security.getValue() >= 0.5f) {
                    try {
                        this.selfPlacePositions.remove(new BlockPos(sPacketSoundEffect.getX(), sPacketSoundEffect.getY() - 1.0, sPacketSoundEffect.getZ()));
                    }
                    catch (ConcurrentModificationException concurrentModificationException) {}
                }
            }
        } else if (receive.getPacket() instanceof SPacketEntityStatus && (sPacketEntityStatus = receive.getPacket()).getOpCode() == 35 && sPacketEntityStatus.getEntity(CrystalBot.mc.world) instanceof EntityPlayer) {
            this.totemPops.put((EntityPlayer)sPacketEntityStatus.getEntity(CrystalBot.mc.world), new Timer());
        }
    }

    @SubscribeEvent
    public void onChangeItem(PacketEvent.Send send) {
        if (CrystalBot.fullNullCheck()) {
            return;
        }
        if (send.getPacket() instanceof CPacketHeldItemChange) {
            this.swapTimer.reset();
        }
    }

    private boolean isDoublePoppable(EntityPlayer entityPlayer, float f) {
        if (this.predictPops.getValue() && entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount() <= 2.0f && (double)f > (double)entityPlayer.getHealth() + (double)entityPlayer.getAbsorptionAmount() + 0.5 && f <= 4.0f) {
            Timer timer = this.totemPops.get(entityPlayer);
            return timer == null || timer.passedMs(500L);
        }
        return false;
    }

    public boolean switchToSword() {
        int n = CrystalUtil.getSwordSlot();
        if (CrystalBot.mc.player.inventory.currentItem != n && n != -1) {
            if (this.antiWeakness.getValue() == ACAntiWeakness.SILENT) {
                this.oldSlotSword = CrystalBot.mc.player.inventory.currentItem;
            }
            CrystalBot.mc.player.inventory.currentItem = n;
            CrystalBot.mc.player.connection.sendPacket(new CPacketHeldItemChange(n));
        }
        return n != -1;
    }

    private List<BlockPos> findCrystalBlocks() {
        NonNullList nonNullList = NonNullList.create();
        nonNullList.addAll(CrystalBot.getSphere(new BlockPos(CrystalBot.mc.player), this.strictDirection.getValue() ? this.placeRange.getValue() + 2.0f : this.placeRange.getValue(), this.placeRange.getValue().intValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
        return nonNullList;
    }

    public boolean hasCrystal() {
        if (this.isOffhand()) {
            return true;
        }
        int n = CrystalUtil.getCrystalSlot();
        if (n == -1) {
            return false;
        }
        if (CrystalBot.mc.player.inventory.currentItem == n) {
            return true;
        }
        if (this.autoSwap.getValue() == ACSwapMode.SILENT) {
            this.oldSlotCrystal = CrystalBot.mc.player.inventory.currentItem;
        }
        CrystalBot.mc.player.inventory.currentItem = n;
        CrystalBot.mc.player.connection.sendPacket(new CPacketHeldItemChange(n));
        return true;
    }

    static {
        switchTimer = new Timer();
    }

    private static class currentPos {
        private BlockPos renderBlock;
        private float renderTime;

        public currentPos(BlockPos renderBlock, float time) {
            this.renderBlock = renderBlock;
            this.renderTime = time;
        }

        public BlockPos renderBlock() {
            return this.renderBlock;
        }

        public float getRenderTime() {
            return this.renderTime;
        }

        public void setRenderTime(float time) {
            this.renderTime = time;
        }

        public void setPos(BlockPos renderBlock) {
            this.renderBlock = renderBlock;
        }
    }

    private static class InstantThread
    implements Runnable {
        private static InstantThread INSTANCE;
        private CrystalBot DeltaCrystal;

        private InstantThread() {
        }

        static InstantThread getInstance(CrystalBot deltaCrystal) {
            if (INSTANCE == null) {
                INSTANCE = new InstantThread();
                InstantThread.INSTANCE.DeltaCrystal = deltaCrystal;
            }
            return INSTANCE;
        }

        @Override
        public void run() {
            if (this.DeltaCrystal.shouldRunThread.get()) {
                try {
                    Thread.sleep((long)(aboba * 40.0f));
                }
                catch (InterruptedException interruptedException) {
                    this.DeltaCrystal.thread.interrupt();
                }
                if (!this.DeltaCrystal.shouldRunThread.get()) {
                    return;
                }
                this.DeltaCrystal.shouldRunThread.set(false);
                if (this.DeltaCrystal.tickRunning.get()) {
                    return;
                }
                this.DeltaCrystal.doInstant();
            }
        }
    }

    public static enum RenderMode {
        STATIC,
        FADE,
        GLIDE

    }

    public static enum TargetRenderMode {
        OLD,
        JELLO,
        OFF

    }

    public static enum ACAntiWeakness {
        OFF,
        NORMAL,
        SILENT

    }

    public static enum DirectionMode {
        VANILLA,
        NORMAL,
        STRICT

    }

    public static enum SyncMode {
        STRICT,
        MERGE

    }

    public static enum Pages {
        General,
        Place,
        Break,
        Calculation,
        Render

    }

    public static enum ConfirmMode {
        OFF,
        SEMI,
        FULL

    }

    private static enum RotationMode {
        OFF,
        TRACK,
        INTERACT

    }

    public static enum ACSwapMode {
        OFF,
        NORMAL,
        SILENT

    }

    private static enum TargetingMode {
        ALL,
        SMART,
        NEAREST

    }

    private static enum YawStepMode {
        OFF,
        SEMI,
        FULL

    }

    private static enum TimingMode {
        SEQUENTIAL,
        ADAPTIVE,
        VANILLA

    }
}


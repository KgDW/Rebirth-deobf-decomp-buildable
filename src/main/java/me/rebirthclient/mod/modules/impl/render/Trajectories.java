package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemLingeringPotion;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;

public class Trajectories
extends Module {
    public Trajectories() {
        super("Trajectories", "Draws trajectories", Category.RENDER);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (Trajectories.mc.player == null || Trajectories.mc.world == null || Trajectories.mc.gameSettings.thirdPersonView == 2) {
            return;
        }
        if (!(Trajectories.mc.player.getHeldItemMainhand() != ItemStack.EMPTY && Trajectories.mc.player.getHeldItemMainhand().getItem() instanceof ItemBow || Trajectories.mc.player.getHeldItemMainhand() != ItemStack.EMPTY && this.isThrowable(Trajectories.mc.player.getHeldItemMainhand().getItem()) || Trajectories.mc.player.getHeldItemOffhand() != ItemStack.EMPTY && this.isThrowable(Trajectories.mc.player.getHeldItemOffhand().getItem()) || Mouse.isButtonDown(2))) {
            return;
        }
        double renderPosX = Trajectories.mc.getRenderManager().renderPosX;
        double renderPosY = Trajectories.mc.getRenderManager().renderPosY;
        double renderPosZ = Trajectories.mc.getRenderManager().renderPosZ;
        Item item = null;
        if (Trajectories.mc.player.getHeldItemMainhand() != ItemStack.EMPTY && (Trajectories.mc.player.getHeldItemMainhand().getItem() instanceof ItemBow || this.isThrowable(Trajectories.mc.player.getHeldItemMainhand().getItem()))) {
            item = Trajectories.mc.player.getHeldItemMainhand().getItem();
        } else if (Trajectories.mc.player.getHeldItemOffhand() != ItemStack.EMPTY && this.isThrowable(Trajectories.mc.player.getHeldItemOffhand().getItem())) {
            item = Trajectories.mc.player.getHeldItemOffhand().getItem();
        }
        if (item == null && Mouse.isButtonDown(2)) {
            item = Items.ENDER_PEARL;
        } else if (item == null) {
            return;
        }
        GL11.glPushAttrib(1048575);
        GL11.glPushMatrix();
        GL11.glDisable(3008);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glEnable(2884);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4353);
        GL11.glDisable(2896);
        double posX = renderPosX - (double)(MathHelper.cos(Trajectories.mc.player.rotationYaw / 180.0f * (float)Math.PI) * 0.16f);
        double posY = renderPosY + (double)Trajectories.mc.player.getEyeHeight() - 0.1000000014901161;
        double posZ = renderPosZ - (double)(MathHelper.sin(Trajectories.mc.player.rotationYaw / 180.0f * (float)Math.PI) * 0.16f);
        float maxDist = this.getDistance(item);
        double motionX = -MathHelper.sin(Trajectories.mc.player.rotationYaw / 180.0f * (float)Math.PI) * MathHelper.cos(Trajectories.mc.player.rotationPitch / 180.0f * (float)Math.PI) * maxDist;
        double motionY = -MathHelper.sin((Trajectories.mc.player.rotationPitch - (float)this.getPitch(item)) / 180.0f * 3.141593f) * maxDist;
        double motionZ = MathHelper.cos(Trajectories.mc.player.rotationYaw / 180.0f * (float)Math.PI) * MathHelper.cos(Trajectories.mc.player.rotationPitch / 180.0f * (float)Math.PI) * maxDist;
        int var6 = 72000 - Trajectories.mc.player.getItemInUseCount();
        float power = (float)var6 / 20.0f;
        power = (power * power + power * 2.0f) / 3.0f;
        if (power > 1.0f) {
            power = 1.0f;
        }
        float distance = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        motionX /= distance;
        motionY /= distance;
        motionZ /= distance;
        float pow = (item instanceof ItemBow ? power * 2.0f : 1.0f) * this.getVelocity(item);
        motionX *= pow;
        motionY *= pow;
        motionZ *= pow;
        if (!Trajectories.mc.player.onGround) {
            motionY += Trajectories.mc.player.motionY;
        }
        RenderUtil.glColor(Managers.COLORS.getCurrent());
        GL11.glEnable(2848);
        float size = (float)(item instanceof ItemBow ? 0.3 : 0.25);
        boolean hasLanded = false;
        Entity landingOnEntity = null;
        RayTraceResult landingPosition = null;
        GL11.glBegin(3);
        while (!hasLanded && posY > 0.0) {
            Vec3d present = new Vec3d(posX, posY, posZ);
            Vec3d future = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
            RayTraceResult possibleLandingStrip = Trajectories.mc.world.rayTraceBlocks(present, future, false, true, false);
            if (possibleLandingStrip != null && possibleLandingStrip.typeOfHit != RayTraceResult.Type.MISS) {
                landingPosition = possibleLandingStrip;
                hasLanded = true;
            }
            AxisAlignedBB arrowBox = new AxisAlignedBB(posX - (double)size, posY - (double)size, posZ - (double)size, posX + (double)size, posY + (double)size, posZ + (double)size);
            List<Entity> entities = this.getEntitiesWithinAABB(arrowBox.offset(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0));
            for (Entity entity : entities) {
                if (!entity.canBeCollidedWith() || entity == Trajectories.mc.player) continue;
                float var7 = 0.3f;
                AxisAlignedBB var8 = entity.getEntityBoundingBox().expand(var7, var7, var7);
                RayTraceResult possibleEntityLanding = var8.calculateIntercept(present, future);
                if (possibleEntityLanding == null) continue;
                hasLanded = true;
                landingOnEntity = entity;
                landingPosition = possibleEntityLanding;
            }
            posX += motionX;
            posY += motionY;
            posZ += motionZ;
            float motionAdjustment = 0.99f;
            motionX *= 0.99f;
            motionY *= 0.99f;
            motionZ *= 0.99f;
            motionY -= this.getGravity(item);
            this.drawTracer(posX - renderPosX, posY - renderPosY, posZ - renderPosZ);
        }
        GL11.glEnd();
        if (landingPosition != null && landingPosition.typeOfHit == RayTraceResult.Type.BLOCK) {
            GlStateManager.translate(posX - renderPosX, posY - renderPosY, posZ - renderPosZ);
            int side = landingPosition.sideHit.getIndex();
            if (side == 2) {
                GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
            } else if (side == 3) {
                GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
            } else if (side == 4) {
                GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
            } else if (side == 5) {
                GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
            }
            Cylinder c = new Cylinder();
            GlStateManager.rotate(-90.0f, 1.0f, 0.0f, 0.0f);
            c.setDrawStyle(100011);
            c.draw(0.5f, 0.2f, 0.0f, 4, 1);
            c.setDrawStyle(100012);
            RenderUtil.glColor(ColorUtil.injectAlpha(Managers.COLORS.getCurrent(), 100));
            c.draw(0.5f, 0.2f, 0.0f, 4, 1);
        }
        GL11.glEnable(2896);
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glEnable(3008);
        GL11.glDepthMask(true);
        GL11.glCullFace(1029);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
        if (landingOnEntity != null) {
            RenderUtil.drawEntityBoxESP(landingOnEntity, Managers.COLORS.getCurrent(), false, new Color(-1), 1.0f, false, true, 100);
        }
    }

    public void drawTracer(double x, double y, double z) {
        GL11.glVertex3d(x, y, z);
    }

    private boolean isThrowable(Item item) {
        return item instanceof ItemEnderPearl || item instanceof ItemExpBottle || item instanceof ItemSnowball || item instanceof ItemEgg || item instanceof ItemSplashPotion || item instanceof ItemLingeringPotion;
    }

    private float getDistance(Item item) {
        return item instanceof ItemBow ? 1.0f : 0.4f;
    }

    private float getVelocity(Item item) {
        if (item instanceof ItemSplashPotion || item instanceof ItemLingeringPotion) {
            return 0.5f;
        }
        if (item instanceof ItemExpBottle) {
            return 0.59f;
        }
        return 1.5f;
    }

    private int getPitch(Item item) {
        if (item instanceof ItemSplashPotion || item instanceof ItemLingeringPotion || item instanceof ItemExpBottle) {
            return 20;
        }
        return 0;
    }

    private float getGravity(Item item) {
        if (item instanceof ItemBow || item instanceof ItemSplashPotion || item instanceof ItemLingeringPotion || item instanceof ItemExpBottle) {
            return 0.05f;
        }
        return 0.03f;
    }

    private List<Entity> getEntitiesWithinAABB(AxisAlignedBB bb) {
        ArrayList<Entity> list = new ArrayList<>();
        int chunkMinX = MathHelper.floor((bb.minX - 2.0) / 16.0);
        int chunkMaxX = MathHelper.floor((bb.maxX + 2.0) / 16.0);
        int chunkMinZ = MathHelper.floor((bb.minZ - 2.0) / 16.0);
        int chunkMaxZ = MathHelper.floor((bb.maxZ + 2.0) / 16.0);
        for (int x = chunkMinX; x <= chunkMaxX; ++x) {
            for (int z = chunkMinZ; z <= chunkMaxZ; ++z) {
                if (Trajectories.mc.world.getChunkProvider().getLoadedChunk(x, z) == null) continue;
                Trajectories.mc.world.getChunk(x, z).getEntitiesWithinAABBForEntity(Trajectories.mc.player, bb, list, EntitySelectors.NOT_SPECTATING);
            }
        }
        return list;
    }
}


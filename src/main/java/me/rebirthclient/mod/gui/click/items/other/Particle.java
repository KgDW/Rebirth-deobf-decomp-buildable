package me.rebirthclient.mod.gui.click.items.other;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

public class Particle {
    private static final Random random = new Random();
    private final Vector2f pos;
    private final Vector2f velocity;
    private float alpha;
    private float size;

    public Particle(Vector2f velocity, float x, float y, float size) {
        this.velocity = velocity;
        this.pos = new Vector2f(x, y);
        this.size = size;
    }

    public static Particle getParticle() {
        Vector2f velocity = new Vector2f((float)(Math.random() * 3.0 - 1.0), (float)(Math.random() * 3.0 - 1.0));
        float x = random.nextInt(Display.getWidth());
        float y = random.nextInt(Display.getHeight());
        float size = (float)(Math.random() * 4.0) + 2.0f;
        return new Particle(velocity, x, y, size);
    }

    public float getAlpha() {
        return this.alpha;
    }

    public float getDistanceTo(Particle particle) {
        return this.getDistanceTo(particle.getX(), particle.getY());
    }

    public float getDistanceTo(float f, float f2) {
        return (float)Util.getDistance(this.getX(), this.getY(), f, f2);
    }

    public float getSize() {
        return this.size;
    }

    public void setSize(float f) {
        this.size = f;
    }

    public float getX() {
        return this.pos.getX();
    }

    public void setX(float f) {
        this.pos.setX(f);
    }

    public float getY() {
        return this.pos.getY();
    }

    public void setY(float f) {
        this.pos.setY(f);
    }

    public void setup(int delta, float speed) {
        Vector2f pos = this.pos;
        pos.x += this.velocity.getX() * (float)delta * (speed / 2.0f);
        Vector2f pos2 = this.pos;
        pos2.y += this.velocity.getY() * (float)delta * (speed / 2.0f);
        if (this.alpha < 180.0f) {
            this.alpha += 0.75f;
        }
        if (this.pos.getX() > (float)Display.getWidth()) {
            this.pos.setX(0.0f);
        }
        if (this.pos.getX() < 0.0f) {
            this.pos.setX((float)Display.getWidth());
        }
        if (this.pos.getY() > (float)Display.getHeight()) {
            this.pos.setY(0.0f);
        }
        if (this.pos.getY() < 0.0f) {
            this.pos.setY((float)Display.getHeight());
        }
    }

    public static class Util {
        private final List<Particle> particles = new ArrayList<>();

        public Util(int in) {
            this.addParticle(in);
        }

        public static double getDistance(float x, float y, float x1, float y1) {
            return Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1));
        }

        public void addParticle(int in) {
            for (int i = 0; i < in; ++i) {
                this.particles.add(Particle.getParticle());
            }
        }

        private void drawTracer(float f, float f2, float f3, float f4, Color firstColor, Color secondColor, Color thirdColor) {
            GL11.glPushMatrix();
            GL11.glDisable(3553);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glShadeModel(7425);
            GL11.glColor4f((float)firstColor.getRed() / 255.0f, (float)firstColor.getGreen() / 255.0f, (float)firstColor.getBlue() / 255.0f, (float)firstColor.getAlpha() / 255.0f);
            GL11.glLineWidth(0.6f);
            GL11.glBegin(1);
            GL11.glVertex2f(f, f2);
            GL11.glColor4f((float)secondColor.getRed() / 255.0f, (float)secondColor.getGreen() / 255.0f, (float)secondColor.getBlue() / 255.0f, (float)secondColor.getAlpha() / 255.0f);
            float y = f2 >= f4 ? f4 + (f2 - f4) / 2.0f : f2 + (f4 - f2) / 2.0f;
            float x = f >= f3 ? f3 + (f - f3) / 2.0f : f + (f3 - f) / 2.0f;
            GL11.glVertex2f(x, y);
            GL11.glEnd();
            GL11.glBegin(1);
            GL11.glColor4f((float)secondColor.getRed() / 255.0f, (float)secondColor.getGreen() / 255.0f, (float)secondColor.getBlue() / 255.0f, (float)secondColor.getAlpha() / 255.0f);
            GL11.glVertex2f(x, y);
            GL11.glColor4f((float)thirdColor.getRed() / 255.0f, (float)thirdColor.getGreen() / 255.0f, (float)thirdColor.getBlue() / 255.0f, (float)thirdColor.getAlpha() / 255.0f);
            GL11.glVertex2f(f3, f4);
            GL11.glEnd();
            GL11.glPopMatrix();
        }

        public void drawParticles() {
            GL11.glPushMatrix();
            GL11.glEnable(3042);
            GL11.glDisable(3553);
            GL11.glBlendFunc(770, 771);
            GL11.glDisable(2884);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            if (Wrapper.mc.currentScreen == null) {
                return;
            }
            for (Particle particle : this.particles) {
                particle.setup(2, 0.1f);
                int width = Mouse.getEventX() * Wrapper.mc.currentScreen.width / Wrapper.mc.displayWidth;
                int height = Wrapper.mc.currentScreen.height - Mouse.getEventY() * Wrapper.mc.currentScreen.height / Wrapper.mc.displayHeight - 1;
                int maxDistance = 300;
                float alpha = (float)MathHelper.clamp((double)particle.getAlpha() - (double)(particle.getAlpha() / 300.0f) * Util.getDistance(width, height, particle.getX(), particle.getY()), 0.0, particle.getAlpha());
                Color color = ColorUtil.injectAlpha(ClickGui.INSTANCE.colorParticles.getValue() ? Managers.COLORS.getCurrent() : new Color(-1714829883), (int)alpha);
                GL11.glColor4f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
                GL11.glPointSize(particle.getSize());
                GL11.glBegin(0);
                GL11.glVertex2f(particle.getX(), particle.getY());
                GL11.glEnd();
                float nearestDistance = 0.0f;
                Particle nearestParticle = null;
                for (Particle secondParticle : this.particles) {
                    float distance = particle.getDistanceTo(secondParticle);
                    if (!(distance <= 300.0f) || !(Util.getDistance(width, height, particle.getX(), particle.getY()) <= 300.0) && !(Util.getDistance(width, height, secondParticle.getX(), secondParticle.getY()) <= 300.0) || nearestDistance > 0.0f && distance > nearestDistance) continue;
                    nearestDistance = distance;
                    nearestParticle = secondParticle;
                }
                if (nearestParticle == null) continue;
                this.drawTracer(particle.getX(), particle.getY(), nearestParticle.getX(), nearestParticle.getY(), color, ColorUtil.injectAlpha(new Color(0x838080), (int)alpha), color);
            }
            GL11.glPushMatrix();
            GL11.glTranslatef(0.5f, 0.5f, 0.5f);
            GL11.glNormal3f(0.0f, 1.0f, 0.0f);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glEnable(3553);
            GL11.glPopMatrix();
            GL11.glDepthMask(true);
            GL11.glEnable(2884);
            GL11.glEnable(2929);
            GL11.glDisable(3042);
            GL11.glPopMatrix();
        }
    }
}


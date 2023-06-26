package me.rebirthclient.api.util.render.shaders;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL20;

public abstract class Shader {
    protected final String fragmentShader;
    public int program;
    public Map<String, Integer> uniformsMap;

    public Shader(String fragmentShader) {
        int fragmentShaderID;
        int vertexShaderID;
        this.fragmentShader = fragmentShader;
        try {
            InputStream vertexStream = this.getClass().getResourceAsStream("/shader/vertex.vert");
            vertexShaderID = this.createShader(IOUtils.toString(vertexStream, Charset.defaultCharset()), 35633);
            IOUtils.closeQuietly(vertexStream);
            InputStream fragmentStream = this.getClass().getResourceAsStream("/shader/" + fragmentShader);
            fragmentShaderID = this.createShader(IOUtils.toString(fragmentStream, Charset.defaultCharset()), 35632);
            IOUtils.closeQuietly(fragmentStream);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (vertexShaderID == 0 || fragmentShaderID == 0) {
            return;
        }
        this.program = ARBShaderObjects.glCreateProgramObjectARB();
        if (this.program == 0) {
            return;
        }
        ARBShaderObjects.glAttachObjectARB(this.program, vertexShaderID);
        ARBShaderObjects.glAttachObjectARB(this.program, fragmentShaderID);
        ARBShaderObjects.glLinkProgramARB(this.program);
        ARBShaderObjects.glValidateProgramARB(this.program);
    }

    public void startShader() {
        GlStateManager.pushMatrix();
        GL20.glUseProgram(this.program);
        if (this.uniformsMap == null) {
            this.uniformsMap = new HashMap<>();
            this.setupUniforms();
        }
        this.updateUniforms();
    }

    public void stopShader() {
        GL20.glUseProgram(0);
        GlStateManager.popMatrix();
    }

    public abstract void setupUniforms();

    public abstract void updateUniforms();

    public int createShader(String shaderSource, int shaderType) {
        int shader = 0;
        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);
            if (shader == 0) {
                return 0;
            }
            ARBShaderObjects.glShaderSourceARB(shader, shaderSource);
            ARBShaderObjects.glCompileShaderARB(shader);
            if (ARBShaderObjects.glGetObjectParameteriARB(shader, 35713) == 0) {
                throw new RuntimeException("Error creating shader: " + this.getLogInfo(shader));
            }
            return shader;
        }
        catch (Exception e) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            throw e;
        }
    }

    public String getLogInfo(int i) {
        return ARBShaderObjects.glGetInfoLogARB(i, ARBShaderObjects.glGetObjectParameteriARB(i, 35716));
    }

    public void setUniform(String uniformName, int location) {
        this.uniformsMap.put(uniformName, location);
    }

    public void setupUniform(String uniformName) {
        this.setUniform(uniformName, GL20.glGetUniformLocation(this.program, uniformName));
    }

    public int getUniform(String uniformName) {
        return this.uniformsMap.get(uniformName);
    }

    public int getProgramId() {
        return this.program;
    }
}


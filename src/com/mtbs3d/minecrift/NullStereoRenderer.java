package com.mtbs3d.minecrift;

import com.mtbs3d.minecrift.api.BasePlugin;
import com.mtbs3d.minecrift.api.IStereoProvider;
import de.fruitfly.ovr.EyeRenderParams;
import de.fruitfly.ovr.enums.EyeType;
import de.fruitfly.ovr.structs.*;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.io.File;

/**
 * Created by StellaArtois on 6/26/2014.
 */
public class NullStereoRenderer extends BasePlugin implements IStereoProvider {
    @Override
    public void eventNotification(int eventId) {

    }

    @Override
    public FovTextureInfo getFovTextureSize(float renderScaleFactor) {
        return null;
    }

    @Override
    public EyeRenderParams configureRendering(Sizei InTextureSize, Sizei OutTextureSize, GLConfig glConfig) {
        return null;
    }

    @Override
    public EyeRenderParams configureRenderingDualTexture(Sizei InTexture1Size, Sizei InTexture2Size, Sizei OutDisplaySize, GLConfig glConfig) {
        return null;
    }

    @Override
    public void resetRenderConfig() {

    }

    @Override
    public EyeType eyeRenderOrder(int index) {
        return EyeType.ovrEye_Center;
    }

    @Override
    public boolean usesDistortion() {
        return false;
    }

    @Override
    public boolean isStereo() {
        return false;
    }

    @Override
    public boolean isGuiOrtho()
    {
        return true;
    }

    @Override
    public FrameTiming getFrameTiming() {
        return null;
    }

    @Override
    public Posef getEyePose(EyeType eye) {
        return null;
    }

    @Override
    public Matrix4f getMatrix4fProjection(FovPort fov, float nearClip, float farClip) {
        return null;
    }

    @Override
    public String getInitializationStatus() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean init(File nativeDir) {
        return false;
    }

    @Override
    public boolean init() {
        return false;
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void poll(float delta) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isCalibrated() {
        return true;
    }

    @Override
    public String getCalibrationStep() {
        return null;
    }

    @Override
    public void beginFrame() {

    }

    @Override
    public void endFrame() {
        GL11.glFlush();
        Display.update();
    }
}

///**
// * Copyright 2013 Mark Browning, StellaArtois
// * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
// *
// * Contains code from Minecraft, copyright Mojang AB
// */
//package com.mtbs3d.minecrift;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.nio.FloatBuffer;
//import java.util.*;
//
//import com.mtbs3d.minecrift.api.PluginManager;
//import com.mtbs3d.minecrift.render.DistortionParams;
//import com.mtbs3d.minecrift.render.QuaternionHelper;
//import com.mtbs3d.minecrift.render.ShaderHelper;
//import com.mtbs3d.minecrift.settings.VRSettings;
//import com.mtbs3d.minecrift.utils.Utils;
//import de.fruitfly.ovr.EyeRenderParams;
//import de.fruitfly.ovr.OculusRift;
//import de.fruitfly.ovr.enums.Axis;
//import de.fruitfly.ovr.enums.EyeType;
//import de.fruitfly.ovr.enums.HandedSystem;
//import de.fruitfly.ovr.enums.RotateDirection;
//import de.fruitfly.ovr.structs.*;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.audio.SoundManager;
//import net.minecraft.client.gui.achievement.GuiAchievement;
//import net.minecraft.client.renderer.EntityRenderer;
//import net.minecraft.entity.EntityLivingBase;
//import net.minecraft.potion.Potion;
//import net.minecraft.util.AxisAlignedBB;
//import net.minecraft.util.MovingObjectPosition;
//import net.minecraft.util.Vec3;
//import org.lwjgl.LWJGLUtil;
//import org.lwjgl.input.Mouse;
//import org.lwjgl.opengl.*;
//
//import com.mtbs3d.minecrift.api.IOrientationProvider;
//import com.mtbs3d.minecrift.control.JoystickAim;
//
//import org.lwjgl.util.vector.Quaternion;
//import org.lwjgl.util.vector.Vector4f;
//import paulscode.sound.SoundSystem;
//
//import static java.lang.Math.ceil;
//
//public class VRRenderer extends EntityRenderer {
//    // FBO stuff
//
//    //Status & initialization
//    int _previousDisplayWidth = 0;
//    int _previousDisplayHeight = 0;
//    public boolean _FBOInitialised = false;
//
//    boolean guiYawOrientationResetRequested = true;
//
//    // Shader Programs
////    int _Distortion_shaderProgramId = -1;
//    int _Lanczos_shaderProgramId = -1;
//    int _FXAA_shaderProgramId = -1;
//
////    int _DistortionShader_DistortionMapUniform     = -1;
////    int _DistortionShader_RenderTextureUniform     = -1;
////    int _DistortionShader_half_screenWidthUniform  = -1;
////    int _DistortionShader_LeftLensCenterUniform    = -1;
////    int _DistortionShader_RightLensCenterUniform   = -1;
////    int _DistortionShader_LeftScreenCenterUniform  = -1;
////    int _DistortionShader_RightScreenCenterUniform = -1;
////    int _DistortionShader_ScaleUniform             = -1;
////    int _DistortionShader_ScaleInUniform           = -1;
////    int _DistortionShader_HmdWarpParamUniform      = -1;
////    int _DistortionShader_ChromAbParamUniform      = -1;
//
//    int _LanczosShader_texelWidthOffsetUniform = -1;
//    int _LanczosShader_texelHeightOffsetUniform = -1;
//    int _LanczosShader_inputImageTextureUniform = -1;
//
//    int _FXAA_RenderTextureUniform = -1;
//    int _FXAA_RenderedTextureSizeUniform = -1;
//
//    FBOParams fxaaFBO; // fxaa filter
//    FBOParams guiFBO; //This is where the GUI is rendered; it is rendered into main world as an object
//    FBOParams preDistortionFBO; //This is where the world is rendered
//    FBOParams ssStartFBO;
//    FBOParams ssMidFBO;
//
//    Quaternion orientation = QuaternionHelper.IDENTITY_QUATERNION;
//    FloatBuffer cameraMatrix4f = QuaternionHelper.quatToMatrix4fFloatBuf(orientation);
//
//    AxisAlignedBB bb;
//
//    // Render
//    DistortionParams distortParams;
//
//    // Sound system
//    Field _soundManagerSndSystemField = null;
//
//    // Enable / disable GUI menu rendering. Useful to display black only
//    // during game load transitions until the world is running.
//    public boolean blankGUIUntilWorldValid = false;
//
//    // Port to 0.3.1
//    GLConfig _glConfig = new GLConfig();
//    boolean _updatedChunks = false;
//    Sizei _preDistortRes = new Sizei();
//    Sizei _superSampleRes = new Sizei();
//    Sizei _outRes = new Sizei();
//    Object _displayImpl = null;
//    Field _fHwnd = null;
//
//    // Debug
//    long lastDebugAt = 0;
//    boolean debugOutLastFrame = false;
//    long debugOutFrameStart = 0;
//
//    /*
//     * MC:    the minecraft world rendering code, below
//     * GUI:   the guiFBO, with GUI rendered into it
//	 * OUT:   graphics card output FB
//	 * preD:  preDistoritonFBO
//	 * postD: ssStartFBO
//	 * pSS  : ssMidFBO
//	 *
//     * No distortion, no supersample, output of world render is true video output FB
//     *
//     *                  +----+
//     *     MC -render-> |OUT |
//     *           ^      +----+
//     *           |
//     *         +----+
//     *    GUI->|GUI |
//     *         +----+
//     *
//     * Distortion, no supersample
//     *
//     *                  +----+            +----+
//     *     MC -render-> |preD| -distort-> |OUT |
//     *           ^      +----+            +----+
//     *           |
//     *         +----+
//     *    GUI->|GUI |
//     *         +----+
//     *
//     * Distortion, supersample
//     *
//     *                  +--------+            +--------+                 +----+
//	 *                  |        |            |        |                 |    |                 +----+
//     *     MC -render-> |  preD  | -distort-> | postD  | -supersample1-> |pSS | -supersample2-> |OUT |
//     *           ^      +--------+            +--------+                 +----+                 +----+
//     *           |
//     *         +----+
//     *    GUI->|GUI |
//     *         +----+
//	 *
//     * No distortion, supersample
//     *
//     *                  +--------+                 +----+
//	 *                  |        |                 |    |                 +----+
//     *     MC -render-> | postD  | -supersample1-> |pSS | -supersample2-> |OUT |
//     *           ^      +--------+                 +----+                 +----+
//     *           |
//     *         +----+
//     *    GUI->|GUI |
//     *         +----+
//     *
//     */
//
//    GuiAchievement guiAchievement;
//    EyeRenderParams eyeRenderParams;
//
//    double renderOriginX;
//    double renderOriginY;
//    double renderOriginZ;
//
//    float headYaw = 0.0F; //relative to head tracker reference frame, absolute
//    float headPitch = 0.0F;
//    float headRoll = 0.0F;
//
//    float prevHeadYaw = 0.0F;
//    float prevHeadPitch = 0.0F;
//    float prevHeadRoll = 0.0F;
//
//    float guiHeadYaw = 0.0f; //Not including mouse
//
//    float camRelX;
//    float camRelY;
//    float camRelZ;
//
//    float crossX;
//    float crossY;
//    float crossZ;
//
//    float lookX; //In world coordinates
//    float lookY;
//    float lookZ;
//
//    float aimX; //In world coordinates
//    float aimY;
//    float aimZ;
//
//    float aimYaw;
//    float aimPitch;
//
//    private boolean guiShowingLastFrame = false; //Used for detecting when UI is shown, fixing the guiYaw
//
//    // Calibration
//    private CalibrationHelper calibrationHelper;
//    private float INITIAL_CALIBRATION_TEXT_SCALE = 0.0065f;
//    private int CALIBRATION_TEXT_WORDWRAP_LEN = 40;
//    private boolean sndSystemReflect = true;
//
//    public VRRenderer(Minecraft par1Minecraft, GuiAchievement guiAchiv) {
//        super(par1Minecraft);
//        this.guiAchievement = guiAchiv;
//
//        if (this.mc.vrSettings.calibrationStrategy == VRSettings.CALIBRATION_STRATEGY_AT_STARTUP)
//            startCalibration();
//    }
//
//    private float checkCameraCollision(
//            double camX, double camY, double camZ,
//            double camXOffset, double camYOffset, double camZOffset, float distance) {
//        //This loop offsets at [-.1, -.1, -.1], [.1,-.1,-.1], [.1,.1,-.1] etc... for all 8 directions
//        for (int var20 = 0; var20 < 8; ++var20) {
//            final float MIN_DISTANCE = (this.mc.vrSettings.getIPD() / 2.0f) + 0.06F;
//            float var21 = (float) ((var20 & 1) * 2 - 1);
//            float var22 = (float) ((var20 >> 1 & 1) * 2 - 1);
//            float var23 = (float) ((var20 >> 2 & 1) * 2 - 1);
//            var21 *= 0.1F;
//            var22 *= 0.1F;
//            var23 *= 0.1F;
//            MovingObjectPosition var24 = this.mc.theWorld.clip(
//                    this.mc.theWorld.getWorldVec3Pool().getVecFromPool(camX + var21, camY + var22, camZ + var23),
//                    this.mc.theWorld.getWorldVec3Pool().getVecFromPool(camX - camXOffset + var21, camY - camYOffset + var22, camZ - camZOffset + var23));
//
//            if (var24 != null && this.mc.theWorld.isBlockOpaqueCube(var24.blockX, var24.blockY, var24.blockZ)) {
//                double var25 = var24.hitVec.distanceTo(this.mc.theWorld.getWorldVec3Pool().getVecFromPool(camX, camY, camZ)) - MIN_DISTANCE;
//
//                if (var25 < distance) {
//                    distance = (float) var25;
//                }
//            }
//        }
//        return distance;
//    }
//
//    /**
//     * sets up projection, view effects, camera position/rotation
//     */
//    private void setupCameraTransform(float renderPartialTicks, EyeType eye, Matrix4f proj) {
//        GL11.glMatrixMode(GL11.GL_PROJECTION);
//        GL11.glLoadIdentity();
//
//        GL11.glMultMatrix(proj.transposed().toFloatBuffer());
//        //mc.checkGLError("Set projection");
//
//        float var5;
//
//        if (this.mc.playerController != null && this.mc.playerController.enableEverythingIsScrewedUpMode()) {
//            var5 = 0.6666667F;
//            GL11.glScalef(1.0F, var5, 1.0F);
//        }
//
//        GL11.glMatrixMode(GL11.GL_MODELVIEW);
//        GL11.glLoadIdentity();
//
//        // IPD
//        GL11.glTranslatef(eyeRenderParams.Eyes[eye.value()].ViewAdjust.x, 0.0f, 0.0f);
//
//        // Camera height offset
//        float cameraYOffset = 1.62f - (this.mc.vrSettings.getPlayerEyeHeight() - this.mc.vrSettings.neckBaseToEyeHeight);
//
//        EntityLivingBase entity = this.mc.renderViewEntity;
//        if (entity != null) {
//            //Do in-game camera adjustments if renderViewEntity exists
//            //A few game effects
//            this.hurtCameraEffect(renderPartialTicks);
//
//            if (this.mc.gameSettings.viewBobbing) {
//                this.setupViewBobbing(renderPartialTicks);
//            }
//
//            var5 = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * renderPartialTicks;
//
//            if (var5 > 0.0F) {
//                byte var6 = 20;
//
//                if (this.mc.thePlayer.isPotionActive(Potion.confusion)) {
//                    var6 = 7;
//                }
//
//                float var7 = 5.0F / (var5 * var5 + 5.0F) - var5 * 0.04F;
//                var7 *= var7;
//                GL11.glRotatef(((float) this.rendererUpdateCount + renderPartialTicks) * (float) var6, 0.0F, 1.0F, 1.0F);
//                GL11.glScalef(1.0F / var7, 1.0F, 1.0F);
//                GL11.glRotatef(-((float) this.rendererUpdateCount + renderPartialTicks) * (float) var6, 0.0F, 1.0F, 1.0F);
//            }
//
//            if (this.mc.gameSettings.thirdPersonView > 0) {
//                float thirdPersonCameraDist = this.thirdPersonDistanceTemp + (this.thirdPersonDistance - this.thirdPersonDistanceTemp) * renderPartialTicks;
//                float thirdPersonYaw;
//                float thirdPersonPitch;
//
//                if (this.mc.gameSettings.debugCamEnable) {
//                    thirdPersonYaw = this.prevDebugCamYaw + (this.debugCamYaw - this.prevDebugCamYaw) * renderPartialTicks;
//                    thirdPersonPitch = this.prevDebugCamPitch + (this.debugCamPitch - this.prevDebugCamPitch) * renderPartialTicks;
//                    GL11.glTranslatef(0.0F, 0.0F, (float) (-thirdPersonCameraDist));
//                    GL11.glRotatef(thirdPersonYaw, 1.0F, 0.0F, 0.0F);
//                    GL11.glRotatef(thirdPersonPitch, 0.0F, 1.0F, 0.0F);
//                } else {
//                    thirdPersonYaw = cameraYaw;
//                    thirdPersonPitch = cameraPitch;
//
//                    if (this.mc.gameSettings.thirdPersonView == 2) {
//                        thirdPersonPitch += 180.0F;
//                    }
//
//                    float PIOVER180 = (float) (Math.PI / 180);
//
//                    //For doing camera collision detection
//                    double camX = renderOriginX + camRelX;
//                    double camY = renderOriginY + camRelY - cameraYOffset;
//                    double camZ = renderOriginZ + camRelZ;
//
//                    float camXOffset = -MathHelper.sin(thirdPersonYaw * PIOVER180) * MathHelper.cos(thirdPersonPitch * PIOVER180) * thirdPersonCameraDist;
//                    float camZOffset = MathHelper.cos(thirdPersonYaw * PIOVER180) * MathHelper.cos(thirdPersonPitch * PIOVER180) * thirdPersonCameraDist;
//                    float camYOffset = -MathHelper.sin(thirdPersonPitch * PIOVER180) * thirdPersonCameraDist;
//
//                    thirdPersonCameraDist = checkCameraCollision(camX, camY, camZ, camXOffset, camYOffset, camZOffset, thirdPersonCameraDist);
//
//                    if (this.mc.gameSettings.thirdPersonView == 2) {
//                        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
//                    }
//
//                    GL11.glRotatef(cameraPitch - thirdPersonPitch, 1.0F, 0.0F, 0.0F);
//                    GL11.glRotatef(cameraYaw - thirdPersonYaw, 0.0F, 1.0F, 0.0F);
//                    GL11.glTranslatef(0.0F, 0.0F, (float) (-thirdPersonCameraDist));
//                    GL11.glRotatef(thirdPersonYaw - cameraYaw, 0.0F, 1.0F, 0.0F);
//                    GL11.glRotatef(thirdPersonPitch - cameraPitch, 1.0F, 0.0F, 0.0F);
//                }
//            }
//        }
//
//        if (!this.mc.gameSettings.debugCamEnable) {
////        	if (this.mc.vrSettings.useQuaternions)
////            {
////                //GL11.glMultMatrix(cameraMatrix4f);   // This doesn't work currently - we still need
////                                                     // the weird +180...
////
////                // So do this instead...
////                float[] rawYawPitchRoll = OculusRift.getEulerAngles(orientation.x,
////                        orientation.y,
////                        orientation.z,
////                        orientation.w,
////                        1f,
////                        OculusRift.HANDED_L,
////                        OculusRift.ROTATE_CCW);
////
////                if (this.mc.gameSettings.thirdPersonView == 2)
////                    GL11.glRotatef(-rawYawPitchRoll[2], 0.0F, 0.0F, 1.0F);
////                else
////                    GL11.glRotatef(rawYawPitchRoll[2], 0.0F, 0.0F, 1.0F);
////
////                GL11.glRotatef(rawYawPitchRoll[1], 1.0F, 0.0F, 0.0F);
////                GL11.glRotatef(rawYawPitchRoll[0] + 180.0F, 0.0F, 1.0F, 0.0F);
////            }
////            else
////            {
//            if (this.mc.gameSettings.thirdPersonView == 2)
//                GL11.glRotatef(-this.cameraRoll, 0.0F, 0.0F, 1.0F);
//            else
//                GL11.glRotatef(this.cameraRoll, 0.0F, 0.0F, 1.0F);
//
//            GL11.glRotatef(this.cameraPitch, 1.0F, 0.0F, 0.0F);
//            GL11.glRotatef(this.cameraYaw + 180.0F, 0.0F, 1.0F, 0.0F);
////            }
//        }
//
//        GL11.glTranslated(-camRelX, cameraYOffset - camRelY, -camRelZ);
//
//        if (this.debugViewDirection > 0) {
//            int var8 = this.debugViewDirection - 1;
//
//            if (var8 == 1) {
//                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
//            }
//
//            if (var8 == 2) {
//                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
//            }
//
//            if (var8 == 3) {
//                GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
//            }
//
//            if (var8 == 4) {
//                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
//            }
//
//            if (var8 == 5) {
//                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
//            }
//        }
//    }
//
//    /**
//     * Sets the listener of sounds
//     */
//    public void setSoundListenerOrientation() {
//        SoundSystem sndSystem = null;
//
//        // Use reflection to get the sndManager
//        if (sndSystemReflect && _soundManagerSndSystemField == null) {
//            try {
//                _soundManagerSndSystemField = SoundManager.class.getDeclaredField("sndSystem");
//                System.out.println("VRRender: Reflected sndSystem");
//            } catch (NoSuchFieldException e) {
//                try {
//                    _soundManagerSndSystemField = SoundManager.class.getDeclaredField("b"); //obfuscated name
//                    System.out.println("VRRender: Reflected obfuscated b");
//                } catch (NoSuchFieldException e1) {
//                    System.out.println("VRRender: got sndSystem directly");
//                    sndSystemReflect = false;
//                }
//                ;
//            }
//            if (_soundManagerSndSystemField != null)
//                _soundManagerSndSystemField.setAccessible(true);
//        }
//        if (!sndSystemReflect) {
//            if (this.mc.sndManager != null)
//                sndSystem = this.mc.sndManager.sndSystem;
//
//        }
//
//
//        if (_soundManagerSndSystemField != null && this.mc.sndManager != null) {
//            try {
//                sndSystem = (SoundSystem) _soundManagerSndSystemField.get(this.mc.sndManager);
//            } catch (IllegalArgumentException e) {
//            } catch (IllegalAccessException e) {
//            }
//            ;
//        }
//
//        float PIOVER180 = (float) (Math.PI / 180);
//
//        Vec3 up = Vec3.createVectorHelper(0, 1, 0);
//        up.rotateAroundZ(-cameraRoll * PIOVER180);
//        up.rotateAroundX(-cameraPitch * PIOVER180);
//        up.rotateAroundY(-cameraYaw * PIOVER180);
//        if (sndSystem != null && this.mc.gameSettings.soundVolume != 0.0F) {
//            sndSystem.setListenerPosition((float) renderOriginX, (float) renderOriginY, (float) renderOriginZ);
//
//            sndSystem.setListenerOrientation(lookX, lookY, lookZ,
//                    (float) up.xCoord, (float) up.yCoord, (float) up.zCoord);
//        }
//        if (mc.mumbleLink != null) {
//            Vec3 forward = Vec3.createVectorHelper(0, 0, -1);
//            forward.rotateAroundZ(-cameraRoll * PIOVER180);
//            forward.rotateAroundX(-cameraPitch * PIOVER180);
//            forward.rotateAroundY(-cameraYaw * PIOVER180);
//            mc.mumbleLink.updateMumble(
//                    (float) renderOriginX, (float) renderOriginY, (float) renderOriginZ,
//                    (float) forward.xCoord, (float) forward.yCoord, (float) forward.zCoord,
//                    (float) up.xCoord, (float) up.yCoord, (float) up.zCoord);
//        }
//    }
//
//    public void updateCamera(float renderPartialTicks, boolean displayActive) {
//        // Do nothing - in VR Renderer we setup the camera as late as possible...
//    }
//
//    public void renderGUIandWorld(float renderPartialTicks)
//    {
//        setupFrameDebug();
//
//        //Ensure FBO are in place and initialized
//        if (!setupFBOs())
//            return;
//
//        // Mark the beginning of the frame
//        PluginManager.beginFrameAll();
//        FrameTiming frameTiming = this.mc.stereoProvider.getFrameTiming();
//
//        this.farPlaneDistance = (float) this.mc.gameSettings.ofRenderDistanceFine;
//
//        if (Config.isFogFancy()) {
//            this.farPlaneDistance *= 0.95F;
//        }
//
//        if (Config.isFogFast()) {
//            this.farPlaneDistance *= 0.83F;
//        }
//
//        float nearClip = 0.05f;
//        float farClip = this.farPlaneDistance * 2.0F;
//        if (farClip < 128.0F) {
//            farClip = 128.0F;
//        }
//
//        boolean guiShowingThisFrame = false;
//        int mouseX = 0;
//        int mouseY = 0;
//        ScaledResolution var15 = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
//        int var16 = var15.getScaledWidth();
//        int var17 = var15.getScaledHeight();
//
//        if ((this.mc.theWorld != null && !this.mc.gameSettings.hideGUI && this.mc.thePlayer.getSleepTimer() == 0) || this.mc.currentScreen != null || this.mc.loadingScreen.isEnabled()) {
//            //Render all UI elements into guiFBO
//            mouseX = Mouse.getX() * var16 / this.mc.displayWidth;
//            mouseY = var17 - Mouse.getY() * var17 / this.mc.displayHeight - 1;
//
//            guiFBO.bindRenderTarget();
//
//            GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
//            GL11.glClearColor(0, 0, 0, 0);
//            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
//            GL11.glMatrixMode(GL11.GL_PROJECTION);
//            GL11.glLoadIdentity();
//            GL11.glOrtho(0.0D, var15.getScaledWidth_double(), var15.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
//            GL11.glMatrixMode(GL11.GL_MODELVIEW);
//            GL11.glLoadIdentity();
//            GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
//            guiShowingThisFrame = true;
//        }
//
//        // Display loading / progress window if necessary
//        if (this.mc.loadingScreen.isEnabled()) {
//            this.mc.loadingScreen.vrRender(var16, var17);
//            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
//        } else if (this.mc.theWorld != null && !this.mc.gameSettings.hideGUI && !this.blankGUIUntilWorldValid) {
//            //Disable any forge gui crosshairs and helmet overlay (pumkinblur)
//            if (Reflector.ForgeGuiIngame_renderCrosshairs.exists()) {
//                Reflector.ForgeGuiIngame_renderCrosshairs.setValue(false);
//                Reflector.ForgeGuiIngame_renderHelmet.setValue(false);
//            }
//            //Draw in game GUI
//            this.mc.ingameGUI.renderGameOverlay(renderPartialTicks, this.mc.currentScreen != null, mouseX, mouseY);
//            guiAchievement.updateAchievementWindow();
//            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
//        }
//
//        if (this.blankGUIUntilWorldValid) {
//            if (this.mc.theWorld != null)
//                this.blankGUIUntilWorldValid = false;
//        }
//
//        if (this.mc.loadingScreen.isEnabled() == false && this.mc.currentScreen != null && !this.blankGUIUntilWorldValid) {
//            try {
//                this.mc.currentScreen.drawScreen(mouseX, mouseY, renderPartialTicks);
//            } catch (Throwable var13) {
//                CrashReport var11 = CrashReport.makeCrashReport(var13, "Rendering screen");
//                throw new ReportedException(var11);
//            }
//
//            GL11.glDisable(GL11.GL_LIGHTING); //inventory messes up fog color sometimes... This fixes
//            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//            drawMouseQuad(mouseX, mouseY);
//        }
//
//        //Setup render target
//        if (this.mc.vrSettings.useSupersample)
//        {
//            if (this.mc.vrSettings.useFXAA)
//                fxaaFBO.bindRenderTarget();
//            else
//                ssStartFBO.bindRenderTarget();
//        }
//        else
//        {
//            if (this.mc.vrSettings.useFXAA)
//                fxaaFBO.bindRenderTarget();
//            else
//                preDistortionFBO.bindRenderTarget();
//        }
//
//        GL11.glClearColor(0, 0, 0, 1);
//        GL11.glEnable(GL11.GL_SCISSOR_TEST);
//
//        _updatedChunks = false;
//
//        //Now, actually render world
//        for (int renderSceneNumber = 0; renderSceneNumber < 2; ++renderSceneNumber)
//        {
//            // Determine optimum eye render order
//            EyeType eye = this.mc.hmdInfo.getHMDInfo().EyeRenderOrder[renderSceneNumber];
//
//            // Get the projection matrix
//            Matrix4f proj = this.mc.stereoProvider.getMatrix4fProjection(eyeRenderParams.Eyes[eye.value()].Fov, nearClip, farClip);
//
//            // Setup our viewport
//            setupEyeViewport(eye);
//
//            // Mark the beginning of the eye render, and get our pose information
//            Posef eyeRenderPose = this.mc.stereoProvider.beginEyeRender(eye);
//
//            // Update the camera with the (probably predicted) pose
//            vrUpdateCamera(renderPartialTicks, eye, eyeRenderPose);
//
//            //Update gui Yaw
//            if (renderSceneNumber == 0)
//            {
//                if (guiShowingThisFrame && !guiShowingLastFrame) {
//                    guiHeadYaw = this.cameraYaw - this.mc.lookaimController.getBodyYawDegrees();
//                }
//                guiShowingLastFrame = guiShowingThisFrame;
//            }
//
//            //transform camera with pitch,yaw,roll + neck model + game effects
//            setupCameraTransform(renderPartialTicks, eye, proj);
//
//            if (this.mc.theWorld != null) {
//                GL11.glMatrixMode(GL11.GL_MODELVIEW);
//                GL11.glPushMatrix();
//
//                if (renderSceneNumber == 0)
//                {  // TODO: This will need to handle pos track for each eye
//                    //If we're in-game, render in-game stuff
//                    this.mc.mcProfiler.startSection("level");
//
//                    if (this.mc.renderViewEntity == null) {
//                        this.mc.renderViewEntity = this.mc.thePlayer;
//                    }
//
//                    EntityLivingBase renderViewEntity = this.mc.renderViewEntity;
//                    this.mc.mcProfiler.endStartSection("center");
//
//                    //Used by fog comparison, 3rd person camera/block collision detection
//                    renderOriginX = renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * (double) renderPartialTicks;
//                    renderOriginY = renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * (double) renderPartialTicks;
//                    renderOriginZ = renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * (double) renderPartialTicks;
//
//                    if (this.mc.currentScreen == null) {
//                        this.mc.mcProfiler.endStartSection("pick");
//                        getPointedBlock(renderPartialTicks);
//                    }
//
//                    // Update sound engine   // TODO: Should only call once per frame, but AFTER camera pos change
//                    setSoundListenerOrientation();
//                }
//
//                this.renderWorld(renderPartialTicks, 0L, eye, proj);
//                this.disableLightmap(renderPartialTicks);
//
//                GL11.glMatrixMode(GL11.GL_MODELVIEW);
//                GL11.glPopMatrix();
//            } else {
//                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);            // Clear Screen And Depth Buffer on the framebuffer to black
//                GL11.glDisable(GL11.GL_BLEND);
//            }
//
//            if (guiShowingThisFrame) {
//                GL11.glPushMatrix();
//                GL11.glEnable(GL11.GL_TEXTURE_2D);
//                guiFBO.bindTexture();
//
//                // Prevent black border at top / bottom of GUI
//                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
//                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
//
//                if (this.mc.theWorld != null && this.mc.vrSettings.hudLockToHead) {
//                    GL11.glLoadIdentity();
//
//                    GL11.glMultMatrix(proj.transposed().toFloatBuffer());
//
//                    GL11.glRotatef(180f - this.mc.vrSettings.hudYawOffset, 0f, 1f, 0f);        // TODO: What's *this* 180 for?
//                    GL11.glRotatef(-this.mc.vrSettings.hudPitchOffset, 1f, 0f, 0f);
////                    GL11.glRotatef(cameraRoll, 0f, 0f, 1f);
//
//                    GL11.glTranslatef(0.0f, 0.0f, this.mc.vrSettings.hudDistance - this.mc.vrSettings.eyeProtrusion);
//                    GL11.glRotatef(180f, 0f, 1f, 0f);//Not sure why this is necessary... normals/backface culling maybe?    // TODO: Another 180!
//                } else {
//                    float guiYaw = 0f;
//                    if (this.mc.theWorld != null) {
//                        if (this.mc.vrSettings.lookMoveDecoupled)
//                            guiYaw = this.mc.lookaimController.getBodyYawDegrees();
//                        else
//                            guiYaw = guiHeadYaw + this.mc.lookaimController.getBodyYawDegrees();
//
//                        guiYaw -= this.mc.vrSettings.hudYawOffset;
//                    } else
//                        guiYaw = guiHeadYaw + this.mc.lookaimController.getBodyYawDegrees();
//                    GL11.glRotatef(-guiYaw, 0f, 1f, 0f);
//
//                    float guiPitch = 0f;
//
//                    if (this.mc.theWorld != null)
//                        guiPitch = -this.mc.vrSettings.hudPitchOffset;
//
////                    if( this.mc.vrSettings.allowMousePitchInput)
////                        guiPitch += this.mc.lookaimController.getBodyPitchDegrees();
//
//                    GL11.glRotatef(guiPitch, 1f, 0f, 0f);
//
//                    GL11.glTranslatef(0.0f, 0.0f, this.mc.vrSettings.hudDistance);
//                    GL11.glRotatef(180f, 0f, 1f, 0f);//Not sure why this is necessary... normals/backface culling maybe?
//                }
//
//                GL11.glEnable(GL11.GL_BLEND);
//                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//                if (this.mc.theWorld != null)
//                    GL11.glColor4f(1, 1, 1, this.mc.vrSettings.hudOpacity);
//                else
//                    GL11.glColor4f(1, 1, 1, 1);
//                if (!this.mc.vrSettings.hudOcclusion)
//                    GL11.glDisable(GL11.GL_DEPTH_TEST);
//
//                drawQuad2(this.mc.displayWidth, this.mc.displayHeight, this.mc.vrSettings.hudScale * this.mc.vrSettings.hudDistance);
//                GL11.glDisable(GL11.GL_BLEND);
//                GL11.glEnable(GL11.GL_DEPTH_TEST);
//
//                GL11.glPopMatrix();
//
//                unbindTexture();
//                //mc.checkGLError("GUI");
//            }
//
//            if (calibrationHelper != null) {
//                float x = lookX * mc.vrSettings.hudDistance;
//                float y = lookY * mc.vrSettings.hudDistance;
//                float z = lookZ * mc.vrSettings.hudDistance;
//
//                GL11.glDisable(GL11.GL_DEPTH_TEST);
//                GL11.glPushMatrix();
//                GL11.glTranslatef(x, y, z);
//                GL11.glRotatef(-this.cameraYaw, 0.0F, 1.0F, 0.0F);
//                GL11.glRotatef(this.cameraPitch, 1.0F, 0.0F, 0.0F);
//                GL11.glRotatef(this.cameraRoll, 0.0F, 0.0F, 1.0F);
//                float textScale = (float) Math.sqrt((x * x + y * y + z * z));
//                GL11.glScalef(-INITIAL_CALIBRATION_TEXT_SCALE * textScale, -INITIAL_CALIBRATION_TEXT_SCALE * textScale, -INITIAL_CALIBRATION_TEXT_SCALE * textScale);
//                String calibrating = "Calibrating " + calibrationHelper.currentPlugin.getName() + "...";
//                mc.fontRenderer.drawStringWithShadow(calibrating, -mc.fontRenderer.getStringWidth(calibrating) / 2, -8, /*white*/16777215);
//                String calibrationStep = calibrationHelper.calibrationStep;
////                mc.fontRenderer.drawStringWithShadow(calibrationStep, -mc.fontRenderer.getStringWidth(calibrationStep)/2, 8, /*white*/16777215);
//
//                int column = 8;
//                ArrayList<String> wrapped = new ArrayList<String>();
//                Utils.wordWrap(calibrationStep, CALIBRATION_TEXT_WORDWRAP_LEN, wrapped);
//                for (String line : wrapped) {
//                    mc.fontRenderer.drawStringWithShadow(line, -mc.fontRenderer.getStringWidth(line) / 2, column, /*white*/16777215);
//                    column += 16;
//                }
//
//                GL11.glPopMatrix();
//                GL11.glEnable(GL11.GL_DEPTH_TEST);
//            }
//
//            // Mark end eye render
//            this.mc.stereoProvider.endEyeRender(eye);
//        }
//        GL11.glDisable(GL11.GL_SCISSOR_TEST);
//
//        //GL11.glClearColor (0.0f, 0.0f, 0.0f, 1.0f);
//        //GL11.glClearDepth(1.0D);
//        //GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);            // Clear Screen And Depth Buffer on the framebuffer to black
//
//        // Render onto the entire screen framebuffer
//        //GL11.glViewport(0, 0, this.mc.displayFBWidth, this.mc.displayFBHeight);
//
//
//        doSuperSample();
//
//        preDistortionFBO.bindTexture();
//        unbindFBORenderTarget();
//
//        GL11.glDisable(GL11.GL_CULL_FACE);  // Oculus wants CW orientations, avoid the problem by turning off culling...
//        GL11.glDisable(GL11.GL_DEPTH_TEST); // Nothing is drawn with depth test on...
//
//        // End the frame
//        PluginManager.endFrameAll();
//
//        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Unbind GL_ARRAY_BUFFER for my own vertex arrays to work...
//        GL11.glEnable(GL11.GL_CULL_FACE); // Turn back on...
//        GL11.glEnable(GL11.GL_DEPTH_TEST); // Turn back on...
//        GL11.glClearDepth(1); // Oculus set this to 0 (the near plane), return to normal...
//        ARBShaderObjects.glUseProgramObjectARB(0); // Oculus shader is still active, turn it off...
//
//        mc.checkGLError("After render world and GUI");
//    }
//
//    private void checkLatencyTester() {
////        // Latency tester
////        if (this.mc.hmdInfo != null)
////        {
////            // Get any 'test in progress' quad to draw
////            float[] rgba = this.mc.hmdInfo.latencyTesterDisplayScreenColor();
////            if (rgba != null)
////            {
////                // Latency tester is expecting a colored quad on screen...
////                drawLatencyTesterColoredQuad(rgba[0], rgba[1], rgba[2], rgba[3]);
////            }
////
////            //drawLatencyTesterColoredQuad(1f, 0f, 0f, 0.80f);
////
////            // Get any latency tester results...
////            String latencyTestResults = this.mc.hmdInfo.latencyTesterGetResultsString();
////            if (latencyTestResults != null)
////            {
////                // Display results
////                this.mc.printChatMessage(latencyTestResults);
////                System.out.println(latencyTestResults);
////            }
////        }
//    }
//
//    private boolean setupFBOs() {
//        try {
//            if (this.mc.displayFBWidth != _previousDisplayWidth || this.mc.displayFBHeight != _previousDisplayHeight || !_FBOInitialised) {
//                _FBOInitialised = false;
//
//                _previousDisplayWidth = this.mc.displayFBWidth;
//                _previousDisplayHeight = this.mc.displayFBHeight;
//
//                _preDistortRes = new Sizei();
//                _superSampleRes = new Sizei();
//                _outRes = new Sizei();
//
//                if (preDistortionFBO != null)
//                    preDistortionFBO.delete();
//                preDistortionFBO = null;
//
//                if (guiFBO != null)
//                    guiFBO.delete();
//                guiFBO = null;
//
//                if (fxaaFBO != null)
//                    fxaaFBO.delete();
//                fxaaFBO = null;
//
//                if (ssStartFBO != null)
//                    ssStartFBO.delete();
//                ssStartFBO = null;
//
//                if (ssMidFBO != null)
//                    ssMidFBO.delete();
//                ssMidFBO = null;
//
//                _LanczosShader_texelWidthOffsetUniform = -1;
//                _LanczosShader_texelHeightOffsetUniform = -1;
//                _LanczosShader_inputImageTextureUniform = -1;
//            }
//
//            if (!_FBOInitialised)
//            {
//                _glConfig = getGLConfig();
//
//                _outRes = new Sizei(this.mc.displayFBWidth, this.mc.displayFBHeight);
//
//                // TODO: Correct for aspect ratio differences...
//                FovTextureInfo fovTextureInfo = mc.hmdInfo.getFovTextureSize(1.0f);
//                _preDistortRes.w = (int)ceil((float)_outRes.w * fovTextureInfo.ScaleW);
//                _preDistortRes.h = (int)ceil((float)_outRes.h * fovTextureInfo.ScaleH);
//                _superSampleRes.w = (int)ceil((float)_preDistortRes.w * this.mc.vrSettings.superSampleScaleFactor);
//                _superSampleRes.h = (int)ceil((float)_preDistortRes.h * this.mc.vrSettings.superSampleScaleFactor);
//
//                System.out.println("[Minecrift] INITIALISE Display");
//                //System.out.println("[Minecrift] Distortion: " + (this.mc.vrSettings.useDistortion ? "ON" : "OFF"));
//                System.out.println("[Minecrift] Display w: " + this.mc.displayFBWidth + ", h: " + this.mc.displayFBHeight);
//                System.out.println("[Minecrift] Renderscale: " + fovTextureInfo.ScaleW);
//                if (this.mc.vrSettings.useSupersample)
//                    System.out.println("[Minecrift] FSAA Scale: " + this.mc.vrSettings.superSampleScaleFactor);
//                else
//                    System.out.println("[Minecrift] FSAA OFF");
//
//                preDistortionFBO = new FBOParams("preDistortionFBO", GL11.GL_TEXTURE_2D, GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_INT, _preDistortRes.w, _preDistortRes.h);
//                mc.checkGLError("FBO create");
//                _glConfig.TexId = preDistortionFBO.getColorTextureId();
//
//                eyeRenderParams = mc.hmdInfo.configureRendering(_preDistortRes,
//                        _outRes,
//                        _glConfig,
//                        this.mc.gameSettings.enableVsync,
//                        this.mc.vrSettings.useChromaticAbCorrection,
//                        this.mc.vrSettings.useTimewarp,
//                        this.mc.vrSettings.useVignette);
//
//                // GUI FBO
//                guiFBO = new FBOParams("guiFBO", GL11.GL_TEXTURE_2D, GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_INT, this.mc.displayWidth, this.mc.displayHeight);
//
//                // FXAA FBO
//                if (this.mc.vrSettings.useFXAA) {
//                    // Shader init
//                    _FXAA_shaderProgramId = ShaderHelper.initShaders(BASIC_VERTEX_SHADER, FXAA_FRAGMENT_SHADER, false);
//
//                    _FXAA_RenderTextureUniform = ARBShaderObjects.glGetUniformLocationARB(_FXAA_shaderProgramId, "sampler0");
//                    _FXAA_RenderedTextureSizeUniform = ARBShaderObjects.glGetUniformLocationARB(_FXAA_shaderProgramId, "resolution");
//
//                    if (this.mc.vrSettings.useSupersample) {
//                        fxaaFBO = new FBOParams("fxaaFBO", GL11.GL_TEXTURE_2D, GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_INT, _superSampleRes.w, _superSampleRes.h);
//                    } else {
//                        fxaaFBO = new FBOParams("fxaaFBO", GL11.GL_TEXTURE_2D, GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_INT, _preDistortRes.w, _preDistortRes.h);
//                    }
//
//                    ShaderHelper.checkGLError("Init FXAA");
//                }
//
//                if (this.mc.vrSettings.useSupersample)
//                {
//                    // Lanczos downsample FBOs
//                    ssStartFBO = new FBOParams("ssStartFBO (SS)", GL11.GL_TEXTURE_2D, GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_INT, _superSampleRes.w, _superSampleRes.h);
//                    ssMidFBO = new FBOParams("ssMidFBO (SS)", GL11.GL_TEXTURE_2D, GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_INT, _preDistortRes.w, _superSampleRes.h); // Yes this is a mix of dimensions!
//
//                    mc.checkGLError("Lanczos FBO create");
//
//                    _Lanczos_shaderProgramId = ShaderHelper.initShaders(LANCZOS_SAMPLER_VERTEX_SHADER, LANCZOS_SAMPLER_FRAGMENT_SHADER, true);
//
//                    ShaderHelper.checkGLError("@1");
//                    GL20.glValidateProgram(_Lanczos_shaderProgramId);
//
//                    // Setup uniform IDs
//                    _LanczosShader_texelWidthOffsetUniform = ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "texelWidthOffset");
//                    _LanczosShader_texelHeightOffsetUniform = ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "texelHeightOffset");
//                    _LanczosShader_inputImageTextureUniform = ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "inputImageTexture");
//
//                    ShaderHelper.checkGLError("FBO init Lanczos shader");
//                }
//                else
//                {
//                    _Lanczos_shaderProgramId = -1;
//                    _LanczosShader_texelWidthOffsetUniform = -1;
//                    _LanczosShader_texelHeightOffsetUniform = -1;
//                    _LanczosShader_inputImageTextureUniform = -1;
//                }
//
//                _FBOInitialised = true;
//            }
//        } catch (Exception ex) {
//            // We had an issue. Set the usual suspects to defaults...
//            this.mc.vrSettings.useSupersample = false;
//            this.mc.vrSettings.superSampleScaleFactor = 2.0f;
//            this.mc.vrSettings.saveOptions();
//            return false;
//        }
//
//        return true;
//    }
//
//    private void setupEyeViewport(EyeType eye)
//    {
//        this.mc.mcProfiler.endStartSection("clear");
//
//        if (!this.mc.vrSettings.useSupersample) {
//            GL11.glViewport(eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Pos.x,
//                    eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Pos.y,
//                    eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Size.w,
//                    eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Size.h
//            );
//
//            // TODO: Do we even need scissor? Isn't this detrimental to SLi / Crossfire?
//            GL11.glScissor(eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Pos.x,
//                    eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Pos.y,
//                    eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Size.w,
//                    eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Size.h
//            );
//        }
//        else
//        {
//                GL11.glViewport((int)ceil(eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Pos.x * this.mc.vrSettings.superSampleScaleFactor),
//                        (int)ceil(eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Pos.y * this.mc.vrSettings.superSampleScaleFactor),
//                        (int)ceil(eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Size.w * this.mc.vrSettings.superSampleScaleFactor),
//                        (int)ceil(eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Size.h * this.mc.vrSettings.superSampleScaleFactor)
//                );
//
//                // TODO: Do we even need scissor? Isn't this detrimental to SLi / Crossfire?
//                GL11.glScissor((int)ceil(eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Pos.x * this.mc.vrSettings.superSampleScaleFactor),
//                        (int)ceil(eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Pos.y * this.mc.vrSettings.superSampleScaleFactor),
//                        (int)ceil(eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Size.w * this.mc.vrSettings.superSampleScaleFactor),
//                        (int)ceil(eyeRenderParams.Eyes[eye.value()].EyeRenderViewport.Size.h * this.mc.vrSettings.superSampleScaleFactor)
//                );
//        }
////        GL11.glScissor((int)ceil(eyeRenderParams._leftViewPortX * eyeRenderParams._renderScale),
////                (int)ceil(eyeRenderParams._leftViewPortY * eyeRenderParams._renderScale),
////                (int)ceil(eyeRenderParams._leftViewPortW * eyeRenderParams._renderScale),
////                (int)ceil(eyeRenderParams._leftViewPortH * eyeRenderParams._renderScale));
////        }
////        else
////        {
////            // Right eye
////            GL11.glViewport((int)ceil(eyeRenderParams._rightViewPortX * eyeRenderParams._renderScale),
////                            (int)ceil(eyeRenderParams._rightViewPortY * eyeRenderParams._renderScale),
////                            (int)ceil(eyeRenderParams._rightViewPortW * eyeRenderParams._renderScale),
////                            (int)ceil(eyeRenderParams._rightViewPortH * eyeRenderParams._renderScale));
////
////            GL11.glScissor((int)ceil(eyeRenderParams._rightViewPortX * eyeRenderParams._renderScale),
////                           (int)ceil(eyeRenderParams._rightViewPortY * eyeRenderParams._renderScale),
////                           (int)ceil(eyeRenderParams._rightViewPortW * eyeRenderParams._renderScale),
////                           (int)ceil(eyeRenderParams._rightViewPortH * eyeRenderParams._renderScale));
////        }
//
//        //mc.checkGLError("FBO viewport / scissor setup");
//    }
//
//    private void doSuperSample()
//    {
//    	Sizei fxaaRes = _preDistortRes;
//
//        if (this.mc.vrSettings.useSupersample || this.mc.vrSettings.useFXAA)
//        {
//            // Setup ortho projection
//            GL11.glMatrixMode(GL11.GL_PROJECTION);
//            GL11.glLoadIdentity();
//            GL11.glMatrixMode(GL11.GL_MODELVIEW);
//            GL11.glLoadIdentity();
//
//            GL11.glTranslatef (0.0f, 0.0f, -0.7f);
//        }
//
//        if (this.mc.vrSettings.useSupersample)
//        {
//            fxaaRes = _superSampleRes;
//        }
//
//        if (this.mc.vrSettings.useFXAA)
//        {
//            fxaaFBO.bindTexture();
//
//            if (this.mc.vrSettings.useSupersample)
//            {
//                // chain into the superSample FBO
//                ssStartFBO.bindRenderTarget();
//            }
//            else
//            {
//                // Chain into preDistortion FBO
//                preDistortionFBO.bindRenderTarget();
//            }
//
//            GL11.glClearColor (1.0f, 1.0f, 1.0f, 1.0f);
//            GL11.glClearDepth(1.0D);
//            GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);            // Clear Screen And Depth Buffer on the framebuffer to black
//
//            // Render onto the entire screen framebuffer
//            GL11.glViewport(0, 0, fxaaRes.w, fxaaRes.h);
//
//            // Set the distortion shader as in use
//            ARBShaderObjects.glUseProgramObjectARB(_FXAA_shaderProgramId);
//
//            // Set up the fragment shader uniforms
//            ARBShaderObjects.glUniform1iARB(_FXAA_RenderTextureUniform, 0);
//            ARBShaderObjects.glUniform2fARB(_FXAA_RenderedTextureSizeUniform, (float) fxaaRes.w, (float) fxaaRes.h);
//
//            drawQuad();
//
//            // Stop shader use
//            ARBShaderObjects.glUseProgramObjectARB(0);
//
//            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
//            //ShaderHelper.checkGLError("After fxaa");
//        }
//
//        if (this.mc.vrSettings.useSupersample)
//        {
//            // Now switch target to 1st pass ss framebuffer
//        	ssMidFBO.bindRenderTarget();
//
//            // Bind the source texture
//       	    ssStartFBO.bindTexture();
//
//            GL11.glClearColor (1.0f, 1.0f, 1.0f, 1.0f);
//            GL11.glClearDepth(1.0D);
//            GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);            // Clear Screen And Depth Buffer on the framebuffer to black
//
//            // Render onto the entire screen framebuffer
//            GL11.glViewport(0, 0, _preDistortRes.w, _superSampleRes.h);
//
//            // Set the downsampling shader as in use
//            ARBShaderObjects.glUseProgramObjectARB(_Lanczos_shaderProgramId);
//
//            // Set up the fragment shader uniforms
//            ARBShaderObjects.glUniform1fARB(_LanczosShader_texelWidthOffsetUniform, 1.0f / (3.0f * (float)_preDistortRes.w));
//            ARBShaderObjects.glUniform1fARB(_LanczosShader_texelHeightOffsetUniform, 0.0f);
//            ARBShaderObjects.glUniform1iARB(_LanczosShader_inputImageTextureUniform, 0);
//
//            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
//
//            // Pass 1
//            drawQuad();
//
//           // mc.checkGLError("After Lanczos Pass1");
//
//            // Pass 2
//            // Now switch to 2nd pass screen framebuffer
//            preDistortionFBO.bindRenderTarget();
//            ssMidFBO.bindTexture();
//
//            GL11.glViewport(0, 0, _preDistortRes.w, _preDistortRes.h);
//            GL11.glClearColor (1.0f, 1.0f, 1.0f, 1.0f);
//            GL11.glClearDepth(1.0D);
//            GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
//
//            // Bind the texture
//            GL13.glActiveTexture(GL13.GL_TEXTURE0);
//
//            // Set up the fragment shader uniforms for pass 2
//            ARBShaderObjects.glUniform1fARB(_LanczosShader_texelWidthOffsetUniform, 0.0f);
//            ARBShaderObjects.glUniform1fARB(_LanczosShader_texelHeightOffsetUniform, 1.0f / (3.0f * (float)_preDistortRes.h));
//            ARBShaderObjects.glUniform1iARB(_LanczosShader_inputImageTextureUniform, 0);
//
//            drawQuad();
//
//            // Stop shader use
//            ARBShaderObjects.glUseProgramObjectARB(0);
//
//            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
//           // mc.checkGLError("After Lanczos Pass2");
//        }
//    }
//
//    public void renderWorld(float renderPartialTicks, long nextFrameTime, EyeType eye, Matrix4f proj) {
//        RenderGlobal renderGlobal = this.mc.renderGlobal;
//        EffectRenderer effectRenderer = this.mc.effectRenderer;
//        EntityLivingBase renderViewEntity = this.mc.renderViewEntity;
//
//        //TODO: fog color isn't quite right yet when eyes split water/air
//        this.updateFogColor(renderPartialTicks);
//        GL11.glClearColor(fogColorRed, fogColorGreen, fogColorBlue, 0.5f);
//        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
//        GL11.glEnable(GL11.GL_CULL_FACE);
//        GL11.glEnable(GL11.GL_DEPTH_TEST);
//        //mc.checkGLError("FBO init");
//
//        this.mc.mcProfiler.startSection("lightTex");
//        if (this.lightmapUpdateNeeded) {
//            this.updateLightmap(renderPartialTicks);
//        }
//
//        ActiveRenderInfo.updateRenderInfo(this.mc.thePlayer, this.mc.gameSettings.thirdPersonView == 2);
//        this.mc.mcProfiler.endStartSection("frustrum");
//        ClippingHelperImpl.getInstance(); // setup clip, using current modelview / projection matrices
//
//        if (!Config.isSkyEnabled() && !Config.isSunMoonEnabled() && !Config.isStarsEnabled()) {
//            GL11.glDisable(GL11.GL_BLEND);
//        } else {
//            this.setupFog(-1, renderPartialTicks);
//            this.mc.mcProfiler.endStartSection("sky");
//            renderGlobal.renderSky(renderPartialTicks);
//        }
//
//        GL11.glEnable(GL11.GL_FOG);
//        this.setupFog(1, renderPartialTicks);
//
//        if (this.mc.gameSettings.ambientOcclusion != 0) {
//            GL11.glShadeModel(GL11.GL_SMOOTH);
//        }
//
//        this.mc.mcProfiler.endStartSection("culling");
//        Frustrum frustrum = new Frustrum();
//        frustrum.setPosition(renderOriginX, renderOriginY, renderOriginZ);
//
//
//        this.mc.renderGlobal.clipRenderersByFrustum(frustrum, renderPartialTicks);
//
//        // Only do this once per frame
//        if (!_updatedChunks) {
//            this.mc.mcProfiler.endStartSection("updatechunks");
//
//            while (!this.mc.renderGlobal.updateRenderers(renderViewEntity, false) && nextFrameTime != 0L) {
//                long var15 = nextFrameTime - System.nanoTime();
//
//                if (var15 < 0L || var15 > 1000000000L) {
//                    break;
//                }
//            }
//            _updatedChunks = true;
//        }
//
//        if (renderViewEntity.posY < 128.0D) {
//            this.renderCloudsCheck(renderGlobal, renderPartialTicks);
//        }
//
//        this.mc.mcProfiler.endStartSection("prepareterrain");
//        this.setupFog(0, renderPartialTicks);
//        GL11.glEnable(GL11.GL_FOG);
//        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
//        RenderHelper.disableStandardItemLighting();
//        this.mc.mcProfiler.endStartSection("terrain");
//        renderGlobal.sortAndRender(renderViewEntity, 0, (double) renderPartialTicks);
//        GL11.glShadeModel(GL11.GL_FLAT);
//        boolean var16 = Reflector.ForgeHooksClient.exists();
//        EntityPlayer var18;
//
//        if (this.debugViewDirection == 0) {
//            RenderHelper.enableStandardItemLighting();
//            this.mc.mcProfiler.endStartSection("entities");
//
//            if (var16) {
//                Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, new Object[]{Integer.valueOf(0)});
//            }
//
//            //TODO: multiple render passes for entities?
//            renderGlobal.renderEntities(renderViewEntity.getPosition(renderPartialTicks), frustrum, renderPartialTicks, cameraRoll);
//
//            if (var16) {
//                Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, new Object[]{Integer.valueOf(-1)});
//            }
//
//            RenderHelper.disableStandardItemLighting();
//
//        }
//
//        GL11.glDisable(GL11.GL_BLEND);
//        GL11.glEnable(GL11.GL_CULL_FACE);
//        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GL11.glDepthMask(true);
//        this.setupFog(0, renderPartialTicks);
//        GL11.glEnable(GL11.GL_BLEND);
//        GL11.glDisable(GL11.GL_CULL_FACE);
//
//        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
//        WrUpdates.resumeBackgroundUpdates();
//
//        if (Config.isWaterFancy()) {
//            this.mc.mcProfiler.endStartSection("water");
//
//            if (this.mc.gameSettings.ambientOcclusion != 0) {
//                GL11.glShadeModel(GL11.GL_SMOOTH);
//            }
//
//            GL11.glColorMask(false, false, false, false);
//            int var17 = renderGlobal.renderAllSortedRenderers(1, (double) renderPartialTicks);
//
//            if (this.mc.gameSettings.anaglyph) {
//                if (anaglyphField == 0) {
//                    GL11.glColorMask(false, true, true, true);
//                } else {
//                    GL11.glColorMask(true, false, false, true);
//                }
//            } else {
//                GL11.glColorMask(true, true, true, true);
//            }
//
//            if (var17 > 0) {
//                renderGlobal.renderAllSortedRenderers(1, (double) renderPartialTicks);
//            }
//
//            GL11.glShadeModel(GL11.GL_FLAT);
//        } else {
//            this.mc.mcProfiler.endStartSection("water");
//            renderGlobal.renderAllSortedRenderers(1, (double) renderPartialTicks);
//        }
//
//        WrUpdates.pauseBackgroundUpdates();
//
//        if (var16 && this.debugViewDirection == 0) {
//            RenderHelper.enableStandardItemLighting();
//            this.mc.mcProfiler.endStartSection("entities");
//            Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, new Object[]{Integer.valueOf(1)});
//            this.mc.renderGlobal.renderEntities(renderViewEntity.getPosition(renderPartialTicks), frustrum, renderPartialTicks, cameraRoll);
//            Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, new Object[]{Integer.valueOf(-1)});
//            RenderHelper.disableStandardItemLighting();
//        }
//
//        GL11.glDepthMask(true);
//        GL11.glEnable(GL11.GL_CULL_FACE);
//        GL11.glDisable(GL11.GL_BLEND);
//
//        boolean renderOutline = this.mc.vrSettings.alwaysRenderBlockOutline || !this.mc.gameSettings.hideGUI;
//        if (this.mc.currentScreen == null && this.cameraZoom == 1.0D && renderViewEntity instanceof EntityPlayer && this.mc.objectMouseOver != null && !renderViewEntity.isInsideOfMaterial(Material.water) && renderOutline) {
//            var18 = (EntityPlayer) renderViewEntity;
//            GL11.glDisable(GL11.GL_ALPHA_TEST);
//            this.mc.mcProfiler.endStartSection("outline");
//
//            if (!var16 || !Reflector.callBoolean(Reflector.ForgeHooksClient_onDrawBlockHighlight, new Object[]{renderGlobal, var18, this.mc.objectMouseOver, Integer.valueOf(0), var18.inventory.getCurrentItem(), Float.valueOf(renderPartialTicks)})) {
//                renderGlobal.drawSelectionBox(var18, this.mc.objectMouseOver, 0, renderPartialTicks);
//            }
//            GL11.glEnable(GL11.GL_ALPHA_TEST);
//        }
//
//        if (this.mc.currentScreen == null && this.cameraZoom == 1.0D && renderViewEntity instanceof EntityPlayer && !renderViewEntity.isInsideOfMaterial(Material.water) && renderOutline && this.mc.vrSettings.showEntityOutline) {
//            var18 = (EntityPlayer) renderViewEntity;
//            if (var18 != null) {
//                GL11.glDisable(GL11.GL_ALPHA_TEST);
//                this.mc.mcProfiler.endStartSection("entityOutline");
//
//                if (this.bb != null)
//                    drawBoundingBox(var18, this.bb, renderPartialTicks);
//
//                GL11.glEnable(GL11.GL_ALPHA_TEST);
//            }
//        }
//
//        this.mc.mcProfiler.endStartSection("destroyProgress");
//        GL11.glEnable(GL11.GL_BLEND);
//        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//        renderGlobal.drawBlockDamageTexture(Tessellator.instance, renderViewEntity, renderPartialTicks);
//        GL11.glDisable(GL11.GL_BLEND);
//        this.mc.mcProfiler.endStartSection("weather");
//        this.renderRainSnow(renderPartialTicks);
//
//
//        GL11.glDisable(GL11.GL_FOG);
//
//        if (renderViewEntity.posY >= 128.0D) {
//            this.renderCloudsCheck(renderGlobal, renderPartialTicks);
//        }
//
//        this.enableLightmap((double) renderPartialTicks);
//        this.mc.mcProfiler.endStartSection("litParticles");
//        RenderHelper.enableStandardItemLighting();
//        effectRenderer.renderLitParticles(renderViewEntity, renderPartialTicks);
//        RenderHelper.disableStandardItemLighting();
//        this.setupFog(0, renderPartialTicks);
//        this.mc.mcProfiler.endStartSection("particles");
//        effectRenderer.renderParticles(renderViewEntity, renderPartialTicks);
//        this.disableLightmap((double) renderPartialTicks);
//
//        if (var16) {
//            this.mc.mcProfiler.endStartSection("FRenderLast");
//            Reflector.callVoid(Reflector.ForgeHooksClient_dispatchRenderLast, new Object[]{renderGlobal, Float.valueOf(renderPartialTicks)});
//        }
//
//        if (this.mc.vrSettings.renderFullFirstPersonModel == false) {
//            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
//            this.vrRenderHand(renderPartialTicks, eye, proj);
//        }
//
//        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f); //white crosshair, with blending
//        //Draw crosshair
//        boolean renderCrosshair = this.mc.vrSettings.alwaysRenderInGameCrosshair || !this.mc.gameSettings.hideGUI;
//
//        if (this.mc.currentScreen == null && this.mc.gameSettings.thirdPersonView == 0 && renderCrosshair) {
//            this.mc.mcProfiler.endStartSection("crosshair");
//            float crossDepth = (float) Math.sqrt((crossX * crossX + crossY * crossY + crossZ * crossZ));
//            float scale = 0.025f * crossDepth * this.mc.vrSettings.crosshairScale;
//
//            GL11.glPushMatrix();
//            GL11.glTranslatef(crossX, crossY, crossZ);
//            GL11.glRotatef(-this.aimYaw, 0.0F, 1.0F, 0.0F);
//            GL11.glRotatef(this.aimPitch, 1.0F, 0.0F, 0.0F);
//            if (this.mc.vrSettings.crosshairRollsWithHead)
//                GL11.glRotatef(this.cameraRoll, 0.0F, 0.0F, 1.0F);
//            GL11.glScalef(-scale, -scale, scale);
//            GL11.glDisable(GL11.GL_LIGHTING);
//            GL11.glDisable(GL11.GL_DEPTH_TEST);
//            GL11.glEnable(GL11.GL_BLEND);
//            GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
//            this.mc.getTextureManager().bindTexture(Gui.icons);
//
//            float var7 = 0.00390625F;
//            float var8 = 0.00390625F;
//            Tessellator.instance.startDrawingQuads();
//            Tessellator.instance.addVertexWithUV(-1, +1, 0, 0, 16 * var8);
//            Tessellator.instance.addVertexWithUV(+1, +1, 0, 16 * var7, 16 * var8);
//            Tessellator.instance.addVertexWithUV(+1, -1, 0, 16 * var7, 0);
//            Tessellator.instance.addVertexWithUV(-1, -1, 0, 0, 0);
//            Tessellator.instance.draw();
//            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//            GL11.glDisable(GL11.GL_BLEND);
//            GL11.glEnable(GL11.GL_DEPTH_TEST);
//            GL11.glPopMatrix();
//            //mc.checkGLError("crosshair");
//        }
//
//        this.mc.mcProfiler.endSection();
//    }
//
//    private void unbindFBORenderTarget() {
//        try {
//            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
//        } catch (IllegalStateException ex) {
//            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
//        }
//    }
//
//    private void unbindTexture() {
//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
//    }
//
//    public void drawQuad() {
//        // this func just draws a perfectly normal box with some texture coordinates
//        GL11.glBegin(GL11.GL_QUADS);
//
//        // Front Face
//        GL11.glTexCoord2f(0.0f, 0.0f);
//        GL11.glVertex3f(-1.0f, -1.0f, 0.0f);  // Bottom Left Of The Texture and Quad
//        GL11.glTexCoord2f(1.0f, 0.0f);
//        GL11.glVertex3f(1.0f, -1.0f, 0.0f);  // Bottom Right Of The Texture and Quad
//        GL11.glTexCoord2f(1.0f, 1.0f);
//        GL11.glVertex3f(1.0f, 1.0f, 0.0f);  // Top Right Of The Texture and Quad
//        GL11.glTexCoord2f(0.0f, 1.0f);
//        GL11.glVertex3f(-1.0f, 1.0f, 0.0f);  // Top Left Of The Texture and Quad
//
//        GL11.glEnd();
//    }
//
//    public void drawLatencyTesterColoredQuad(float r, float g, float b, float a) {
//        GL11.glDisable(GL11.GL_TEXTURE_2D);
//        GL11.glEnable(GL11.GL_BLEND);
//
//        // Setup ortho projection
//        GL11.glMatrixMode(GL11.GL_PROJECTION);
//        GL11.glLoadIdentity();
//        GL11.glMatrixMode(GL11.GL_MODELVIEW);
//        GL11.glLoadIdentity();
//
//        GL11.glTranslatef(0.0f, 0.0f, -0.7f);
//
//        // Cover the appropriate areas of the screen with the colored quad
//        GL11.glBegin(GL11.GL_QUADS);
//
//        GL11.glColor4f(r, g, b, a);
//
//        GL11.glVertex3f(-0.6f, -0.6f, 0.0f);  // Bottom Left Of The Texture and Quad
//        GL11.glVertex3f(0.6f, -0.6f, 0.0f);  // Bottom Right Of The Texture and Quad
//        GL11.glVertex3f(0.6f, 0.6f, 0.0f);  // Top Right Of The Texture and Quad
//        GL11.glVertex3f(-0.6f, 0.6f, 0.0f);  // Top Left Of The Texture and Quad
//
//        GL11.glEnd();
//
//        GL11.glDisable(GL11.GL_BLEND);
//        GL11.glEnable(GL11.GL_TEXTURE_2D);
//    }
//
//    public void drawQuad2(float displayWidth, float displayHeight, float scale) {
//        float aspect = displayHeight / displayWidth;
//
//        GL11.glBegin(GL11.GL_QUADS);
//
//        GL11.glTexCoord2f(0.0f, 0.0f);
//        GL11.glVertex3f(-1.0f * scale, -1.0f * aspect * scale, 0.0f);  // Bottom Left  Of The Texture and Quad
//        GL11.glTexCoord2f(1.0f, 0.0f);
//        GL11.glVertex3f(1.0f * scale, -1.0f * aspect * scale, 0.0f);  // Bottom Right Of The Texture and Quad
//        GL11.glTexCoord2f(1.0f, 1.0f);
//        GL11.glVertex3f(1.0f * scale, 1.0f * aspect * scale, 0.0f);  // Top    Right Of The Texture and Quad
//        GL11.glTexCoord2f(0.0f, 1.0f);
//        GL11.glVertex3f(-1.0f * scale, 1.0f * aspect * scale, 0.0f);  // Top    Left  Of The Texture and Quad
//
//        GL11.glEnd();
//    }
//
//    public final String BASIC_VERTEX_SHADER =
//
//            "#version 110\n" +
//                    "\n" +
//                    "varying vec4 textCoord;\n" +
//                    "\n" +
//                    "void main() {\n" +
//                    "    gl_Position = ftransform(); //Transform the vertex position\n" +
//                    "    textCoord = gl_MultiTexCoord0; // Use Texture unit 0\n" +
//                    "}\n";
//
//    public final String BASIC_VERTEX_SHADER_VBO =
//
//            "#version 120\n" +
//                    "\n" +
//                    " attribute vec4 in_Position;\n" +
//                    " attribute vec4 in_Color;\n" +
//                    " attribute vec2 in_TextureCoord;\n" +
//                    " varying vec4 textCoord;\n" +
//                    "\n" +
//                    "void main() {\n" +
//                    "    gl_Position = vec4(in_Position.x, in_Position.y, 0.0, 1.0);\n" +
//                    "    textCoord = vec4(in_TextureCoord.s, in_TextureCoord.t, 0.0, 0.0);\n" +
//                    "}\n";
//
//    public final String OCULUS_DISTORTION_FRAGMENT_SHADER_NO_CHROMATIC_ABERRATION_CORRECTION =
//
//            "#version 120\n" +
//                    "\n" +
//                    "uniform sampler2D bgl_RenderTexture;\n" +
//                    "uniform sampler2D distortionMap;\n" +
//                    "uniform int half_screenWidth;\n" +
//                    "uniform vec2 LeftLensCenter;\n" +
//                    "uniform vec2 RightLensCenter;\n" +
//                    "uniform vec2 LeftScreenCenter;\n" +
//                    "uniform vec2 RightScreenCenter;\n" +
//                    "uniform vec2 Scale;\n" +
//                    "uniform vec2 ScaleIn;\n" +
//                    "uniform vec4 HmdWarpParam;\n" +
//                    "uniform vec4 ChromAbParam;\n" +
//                    "varying vec4 textCoord;\n" +
//                    "\n" +
//                    "// Scales input texture coordinates for distortion.\n" +
//                    "vec2 HmdWarp(vec2 in01, vec2 LensCenter)\n" +
//                    "{\n" +
//                    "    vec2 theta = (in01 - LensCenter) * ScaleIn; // Scales to [-1, 1]\n" +
//                    "    float rSq = theta.x * theta.x + theta.y * theta.y;\n" +
//                    "    vec2 rvector = theta * (HmdWarpParam.x + HmdWarpParam.y * rSq +\n" +
//                    "            HmdWarpParam.z * rSq * rSq +\n" +
//                    "            HmdWarpParam.w * rSq * rSq * rSq);\n" +
//                    "    return LensCenter + Scale * rvector;\n" +
//                    "}\n" +
//                    "\n" +
//                    "void main()\n" +
//                    "{\n" +
//                    "    // The following two variables need to be set per eye\n" +
//                    "    vec2 LensCenter = gl_FragCoord.x < half_screenWidth ? LeftLensCenter : RightLensCenter;\n" +
//                    "    vec2 ScreenCenter = gl_FragCoord.x < half_screenWidth ? LeftScreenCenter : RightScreenCenter;\n" +
//                    "\n" +
//                    "    vec2 oTexCoord = textCoord.xy;\n" +
//                    "    //vec2 oTexCoord = (gl_FragCoord.xy + vec2(0.5, 0.5)) / vec2(screenWidth, screenHeight);\n" +
//                    "\n" +
//                    "    vec2 tc = HmdWarp(oTexCoord, LensCenter);\n" +
//                    "    if (any(bvec2(clamp(tc,ScreenCenter-vec2(0.25,0.5), ScreenCenter+vec2(0.25,0.5)) - tc)))\n" +
//                    "    {\n" +
//                    "        gl_FragColor = vec4(vec3(0.0), 1.0);\n" +
//                    "        return;\n" +
//                    "    }\n" +
//                    "\n" +
//                    "    //tc.x = gl_FragCoord.x < half_screenWidth ? (2.0 * tc.x) : (2.0 * (tc.x - 0.5));\n" +
//                    "    //gl_FragColor = texture2D(bgl_RenderTexture, tc).aaaa * texture2D(bgl_RenderTexture, tc);\n" +
//                    "    gl_FragColor = texture2D(bgl_RenderTexture, tc);\n" +
//                    "}\n";
//
//    public final String OCULUS_DISTORTION_FRAGMENT_SHADER_NO_CHROMATIC_ABERRATION_CORRECTION_DIST_MAP =
//
//            "#version 120\n" +
//                    "\n" +
//                    "#define highp\n" +
//                    "uniform sampler2D bgl_RenderTexture;\n" +
//                    "uniform sampler2D distortionMap;\n" +
//                    "uniform int half_screenWidth;\n" +
//                    "uniform vec2 LeftLensCenter;\n" +
//                    "uniform vec2 RightLensCenter;\n" +
//                    "uniform vec2 LeftScreenCenter;\n" +
//                    "uniform vec2 RightScreenCenter;\n" +
//                    "uniform vec2 Scale;\n" +
//                    "uniform vec2 ScaleIn;\n" +
//                    "uniform vec4 HmdWarpParam;\n" +
//                    "uniform vec4 ChromAbParam;\n" +
//                    "varying vec4 textCoord;\n" +
//                    "\n" +
//                    "void main()\n" +
//                    "{\n" +
//                    "    vec2 tc = texture2D(distortionMap, textCoord.xy).rg;\n" +
//                    "    if (tc == vec2(-1.0, -1.0))\n" +
//                    "        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);\n" +
//                    "    else\n" +
//                    "        gl_FragColor = texture2D(bgl_RenderTexture, tc);\n" +
//                    "}\n";
//
//    public final String OCULUS_DISTORTION_FRAGMENT_SHADER_WITH_CHROMATIC_ABERRATION_CORRECTION =
//
//            "#version 120\n" +
//                    "\n" +
//                    "uniform sampler2D bgl_RenderTexture;\n" +
//                    "uniform int half_screenWidth;\n" +
//                    "uniform vec2 LeftLensCenter;\n" +
//                    "uniform vec2 RightLensCenter;\n" +
//                    "uniform vec2 LeftScreenCenter;\n" +
//                    "uniform vec2 RightScreenCenter;\n" +
//                    "uniform vec2 Scale;\n" +
//                    "uniform vec2 ScaleIn;\n" +
//                    "uniform vec4 HmdWarpParam;\n" +
//                    "uniform vec4 ChromAbParam;\n" +
//                    "varying vec4 textCoord;\n" +
//                    "\n" +
//                    "void main()\n" +
//                    "{\n" +
//                    "    vec2 LensCenter = gl_FragCoord.x < half_screenWidth ? LeftLensCenter : RightLensCenter;\n" +
//                    "    vec2 ScreenCenter = gl_FragCoord.x < half_screenWidth ? LeftScreenCenter : RightScreenCenter;\n" +
//                    "\n" +
//                    "    vec2 theta = (textCoord.xy - LensCenter) * ScaleIn;\n" +
//                    "    float rSq = theta.x * theta.x + theta.y * theta.y;\n" +
//                    "    vec2 theta1 = theta * (HmdWarpParam.x + HmdWarpParam.y * rSq + HmdWarpParam.z * rSq * rSq + HmdWarpParam.w * rSq * rSq * rSq);\n" +
//                    "\n" +
//                    "    vec2 thetaBlue = theta1 * (ChromAbParam.w * rSq + ChromAbParam.z);\n" +
//                    "    vec2 tcBlue = thetaBlue * Scale + LensCenter;\n" +
//                    "\n" +
//                    "    if (any(bvec2(clamp(tcBlue, ScreenCenter-vec2(0.25,0.5), ScreenCenter+vec2(0.25,0.5)) - tcBlue))) {\n" +
//                    "        gl_FragColor = vec4(vec3(0.0), 1.0);\n" +
//                    "        return;\n" +
//                    "    }\n" +
//                    "    float blue = texture2D(bgl_RenderTexture, tcBlue).b;\n" +
//                    "\n" +
//                    "    vec2 tcGreen = theta1 * Scale + LensCenter;\n" +
//                    "    float green = texture2D(bgl_RenderTexture, tcGreen).g;\n" +
//                    "\n" +
//                    "    vec2 thetaRed = theta1 * (ChromAbParam.y * rSq + ChromAbParam.x);\n" +
//                    "    vec2 tcRed = thetaRed * Scale + LensCenter;\n" +
//                    "    float red = texture2D(bgl_RenderTexture, tcRed).r;\n" +
//                    "\n" +
//                    "    gl_FragColor = vec4(red, green, blue, 1.0);\n" +
//                    "}\n";
//
//    public final String OCULUS_DISTORTION_FRAGMENT_SHADER_WITH_CHROMATIC_ABERRATION_CORRECTION_DIST_MAP =
//
//            "#version 120\n" +
//                    "\n" +
//                    "#define highp\n" +
//                    "uniform sampler2D bgl_RenderTexture;\n" +
//                    "uniform sampler2D distortionMap;\n" +
//                    "uniform int half_screenWidth;\n" +
//                    "uniform vec2 LeftLensCenter;\n" +
//                    "uniform vec2 RightLensCenter;\n" +
//                    "uniform vec2 LeftScreenCenter;\n" +
//                    "uniform vec2 RightScreenCenter;\n" +
//                    "uniform vec2 Scale;\n" +
//                    "uniform vec2 ScaleIn;\n" +
//                    "uniform vec4 HmdWarpParam;\n" +
//                    "uniform vec4 ChromAbParam;\n" +
//                    "varying vec4 textCoord;\n" +
//                    "\n" +
//                    "void main()\n" +
//                    "{\n" +
//                    "    vec2 LensCenter = gl_FragCoord.x < half_screenWidth ? LeftLensCenter : RightLensCenter;\n" +
//                    "\n" +
//                    "    vec2 theta = (textCoord.xy - LensCenter) * ScaleIn;\n" +
//                    "    float rSq = theta.x * theta.x + theta.y * theta.y;\n" +
//                    "    vec2 theta1 = texture2D(distortionMap, textCoord.xy).rg;\n" +
//                    "    if (theta1 == vec2(-1.0, -1.0)) {\n" +
//                    "        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);\n" +
//                    "        return;\n" +
//                    "    }\n" +
//                    "\n" +
//                    "    vec2 thetaBlue = theta1 * (ChromAbParam.w * rSq + ChromAbParam.z);\n" +
//                    "    vec2 tcBlue = thetaBlue * Scale + LensCenter;\n" +
//                    "    float blue = texture2D(bgl_RenderTexture, tcBlue).b;\n" +
//                    "\n" +
//                    "    vec2 tcGreen = theta1 * Scale + LensCenter;\n" +
//                    "    float green = texture2D(bgl_RenderTexture, tcGreen).g;\n" +
//                    "\n" +
//                    "    vec2 thetaRed = theta1 * (ChromAbParam.y * rSq + ChromAbParam.x);\n" +
//                    "    vec2 tcRed = thetaRed * Scale + LensCenter;\n" +
//                    "    float red = texture2D(bgl_RenderTexture, tcRed).r;\n" +
//                    "\n" +
//                    "    gl_FragColor = vec4(red, green, blue, 1.0);\n" +
//                    "}\n";
//
//    public final String LANCZOS_SAMPLER_VERTEX_SHADER =
//            "#version 120\n" +
//                    "\n" +
//                    " uniform float texelWidthOffset;\n" +
//                    " uniform float texelHeightOffset;\n" +
//                    "\n" +
//                    " varying vec2 centerTextureCoordinate;\n" +
//                    " varying vec2 oneStepLeftTextureCoordinate;\n" +
//                    " varying vec2 twoStepsLeftTextureCoordinate;\n" +
//                    " varying vec2 threeStepsLeftTextureCoordinate;\n" +
//                    " varying vec2 fourStepsLeftTextureCoordinate;\n" +
//                    " varying vec2 oneStepRightTextureCoordinate;\n" +
//                    " varying vec2 twoStepsRightTextureCoordinate;\n" +
//                    " varying vec2 threeStepsRightTextureCoordinate;\n" +
//                    " varying vec2 fourStepsRightTextureCoordinate;\n" +
//                    "\n" +
//                    " void main()\n" +
//                    " {\n" +
//                    "     gl_Position = ftransform();\n" +
//                    "\n" +
//                    "     vec2 firstOffset = vec2(texelWidthOffset, texelHeightOffset);\n" +
//                    "     vec2 secondOffset = vec2(2.0 * texelWidthOffset, 2.0 * texelHeightOffset);\n" +
//                    "     vec2 thirdOffset = vec2(3.0 * texelWidthOffset, 3.0 * texelHeightOffset);\n" +
//                    "     vec2 fourthOffset = vec2(4.0 * texelWidthOffset, 4.0 * texelHeightOffset);\n" +
//                    "\n" +
//                    "     vec2 textCoord = gl_MultiTexCoord0.xy;\n" +
//                    "     centerTextureCoordinate = textCoord;\n" +
//                    "     oneStepLeftTextureCoordinate = textCoord - firstOffset;\n" +
//                    "     twoStepsLeftTextureCoordinate = textCoord - secondOffset;\n" +
//                    "     threeStepsLeftTextureCoordinate = textCoord - thirdOffset;\n" +
//                    "     fourStepsLeftTextureCoordinate = textCoord - fourthOffset;\n" +
//                    "     oneStepRightTextureCoordinate = textCoord + firstOffset;\n" +
//                    "     twoStepsRightTextureCoordinate = textCoord + secondOffset;\n" +
//                    "     threeStepsRightTextureCoordinate = textCoord + thirdOffset;\n" +
//                    "     fourStepsRightTextureCoordinate = textCoord + fourthOffset;\n" +
//                    " }\n";
//
//    public final String LANCZOS_SAMPLER_VERTEX_SHADER_VBO =
//            "#version 120\n" +
//                    "\n" +
//                    " attribute vec4 in_Position;\n" +
//                    " attribute vec4 in_Color;\n" +
//                    " attribute vec2 in_TextureCoord;\n" +
//                    "\n" +
//                    " uniform float texelWidthOffset;\n" +
//                    " uniform float texelHeightOffset;\n" +
//                    "\n" +
//                    " varying vec2 centerTextureCoordinate;\n" +
//                    " varying vec2 oneStepLeftTextureCoordinate;\n" +
//                    " varying vec2 twoStepsLeftTextureCoordinate;\n" +
//                    " varying vec2 threeStepsLeftTextureCoordinate;\n" +
//                    " varying vec2 fourStepsLeftTextureCoordinate;\n" +
//                    " varying vec2 oneStepRightTextureCoordinate;\n" +
//                    " varying vec2 twoStepsRightTextureCoordinate;\n" +
//                    " varying vec2 threeStepsRightTextureCoordinate;\n" +
//                    " varying vec2 fourStepsRightTextureCoordinate;\n" +
//                    "\n" +
//                    " void main()\n" +
//                    " {\n" +
//                    "     gl_Position = in_Position;\n" +
//                    "\n" +
//                    "     vec2 firstOffset = vec2(texelWidthOffset, texelHeightOffset);\n" +
//                    "     vec2 secondOffset = vec2(2.0 * texelWidthOffset, 2.0 * texelHeightOffset);\n" +
//                    "     vec2 thirdOffset = vec2(3.0 * texelWidthOffset, 3.0 * texelHeightOffset);\n" +
//                    "     vec2 fourthOffset = vec2(4.0 * texelWidthOffset, 4.0 * texelHeightOffset);\n" +
//                    "\n" +
//                    "     centerTextureCoordinate = in_TextureCoord;\n" +
//                    "     oneStepLeftTextureCoordinate = in_TextureCoord - firstOffset;\n" +
//                    "     twoStepsLeftTextureCoordinate = in_TextureCoord - secondOffset;\n" +
//                    "     threeStepsLeftTextureCoordinate = in_TextureCoord - thirdOffset;\n" +
//                    "     fourStepsLeftTextureCoordinate = in_TextureCoord - fourthOffset;\n" +
//                    "     oneStepRightTextureCoordinate = in_TextureCoord + firstOffset;\n" +
//                    "     twoStepsRightTextureCoordinate = in_TextureCoord + secondOffset;\n" +
//                    "     threeStepsRightTextureCoordinate = in_TextureCoord + thirdOffset;\n" +
//                    "     fourStepsRightTextureCoordinate = in_TextureCoord + fourthOffset;\n" +
//                    " }\n";
//
//    public final String LANCZOS_SAMPLER_FRAGMENT_SHADER =
//
//            "#version 120\n" +
//                    "\n" +
//                    " uniform sampler2D inputImageTexture;\n" +
//                    "\n" +
//                    " varying vec2 centerTextureCoordinate;\n" +
//                    " varying vec2 oneStepLeftTextureCoordinate;\n" +
//                    " varying vec2 twoStepsLeftTextureCoordinate;\n" +
//                    " varying vec2 threeStepsLeftTextureCoordinate;\n" +
//                    " varying vec2 fourStepsLeftTextureCoordinate;\n" +
//                    " varying vec2 oneStepRightTextureCoordinate;\n" +
//                    " varying vec2 twoStepsRightTextureCoordinate;\n" +
//                    " varying vec2 threeStepsRightTextureCoordinate;\n" +
//                    " varying vec2 fourStepsRightTextureCoordinate;\n" +
//                    "\n" +
//                    " // sinc(x) * sinc(x/a) = (a * sin(pi * x) * sin(pi * x / a)) / (pi^2 * x^2)\n" +
//                    " // Assuming a Lanczos constant of 2.0, and scaling values to max out at x = +/- 1.5\n" +
//                    "\n" +
//                    " void main()\n" +
//                    " {\n" +
//                    "     vec4 fragmentColor = texture2D(inputImageTexture, centerTextureCoordinate) * 0.38026;\n" +
//                    "\n" +
//                    "     fragmentColor += texture2D(inputImageTexture, oneStepLeftTextureCoordinate) * 0.27667;\n" +
//                    "     fragmentColor += texture2D(inputImageTexture, oneStepRightTextureCoordinate) * 0.27667;\n" +
//                    "\n" +
//                    "     fragmentColor += texture2D(inputImageTexture, twoStepsLeftTextureCoordinate) * 0.08074;\n" +
//                    "     fragmentColor += texture2D(inputImageTexture, twoStepsRightTextureCoordinate) * 0.08074;\n" +
//                    "\n" +
//                    "     fragmentColor += texture2D(inputImageTexture, threeStepsLeftTextureCoordinate) * -0.02612;\n" +
//                    "     fragmentColor += texture2D(inputImageTexture, threeStepsRightTextureCoordinate) * -0.02612;\n" +
//                    "\n" +
//                    "     fragmentColor += texture2D(inputImageTexture, fourStepsLeftTextureCoordinate) * -0.02143;\n" +
//                    "     fragmentColor += texture2D(inputImageTexture, fourStepsRightTextureCoordinate) * -0.02143;\n" +
//                    "\n" +
//                    "     gl_FragColor = fragmentColor;\n" +
//                    " }\n";
//
//    public final String FXAA_FRAGMENT_SHADER =
//            "/**\n" +
//                    " **   __ __|_  ___________________________________________________________________________  ___|__ __\n" +
//                    " **  //    /\\                                           _                                  /\\    \\\\  \n" +
//                    " ** //____/  \\__     __ _____ _____ _____ _____ _____  | |     __ _____ _____ __        __/  \\____\\\\ \n" +
//                    " **  \\    \\  / /  __|  |     |   __|  _  |     |  _  | | |  __|  |     |   __|  |      /\\ \\  /    /  \n" +
//                    " **   \\____\\/_/  |  |  |  |  |  |  |     | | | |   __| | | |  |  |  |  |  |  |  |__   \"  \\_\\/____/   \n" +
//                    " **  /\\    \\     |_____|_____|_____|__|__|_|_|_|__|    | | |_____|_____|_____|_____|  _  /    /\\     \n" +
//                    " ** /  \\____\\                       http://jogamp.org  |_|                              /____/  \\    \n" +
//                    " ** \\  /   \"' _________________________________________________________________________ `\"   \\  /    \n" +
//                    " **  \\/____.                                                                             .____\\/     \n" +
//                    " **\n" +
//                    " ** Modified/Ported post-processing anti-aliasing filter by Timothy Lottes. Basically removed the vertex\n" +
//                    " ** shader logic and integrated it into the fragment shader for better ease of use. For more explanation\n" +
//                    " ** regarding FXAA post processing anti-aliasing see here:\n" +
//                    " **\n" +
//                    " ** http://developer.download.nvidia.com/assets/gamedev/files/sdk/11/FXAA_WhitePaper.pdf\n" +
//                    " ** http://timothylottes.blogspot.com/2011/03/nvidia-fxaa.html\n" +
//                    " **/\n" +
//                    "#version 120\n" +
//                    "#define FXAA_REDUCE_MIN (1.0/128.0)\n" +
//                    "#define FXAA_REDUCE_MUL (1.0/8.0)\n" +
//                    "#define FXAA_SPAN_MAX 8.0\n" +
//                    "uniform sampler2D sampler0;\n" +
//                    "uniform vec2 resolution;\n" +
//                    "\n" +
//                    "void main(){\n" +
//                    "   vec2 inverse_resolution=vec2(1.0/resolution.x,1.0/resolution.y);\n" +
//                    "   vec3 rgbNW = texture2D(sampler0, (gl_FragCoord.xy + vec2(-1.0,-1.0)) * inverse_resolution).xyz;\n" +
//                    "   vec3 rgbNE = texture2D(sampler0, (gl_FragCoord.xy + vec2(1.0,-1.0)) * inverse_resolution).xyz;\n" +
//                    "   vec3 rgbSW = texture2D(sampler0, (gl_FragCoord.xy + vec2(-1.0,1.0)) * inverse_resolution).xyz;\n" +
//                    "   vec3 rgbSE = texture2D(sampler0, (gl_FragCoord.xy + vec2(1.0,1.0)) * inverse_resolution).xyz;\n" +
//                    "   vec3 rgbM  = texture2D(sampler0,  gl_FragCoord.xy  * inverse_resolution).xyz;\n" +
//                    "   vec3 luma = vec3(0.299, 0.587, 0.114);\n" +
//                    "   float lumaNW = dot(rgbNW, luma);\n" +
//                    "   float lumaNE = dot(rgbNE, luma);\n" +
//                    "   float lumaSW = dot(rgbSW, luma);\n" +
//                    "   float lumaSE = dot(rgbSE, luma);\n" +
//                    "   float lumaM  = dot(rgbM,  luma);\n" +
//                    "   float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));\n" +
//                    "   float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE))); \n" +
//                    "   vec2 dir;\n" +
//                    "   dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));\n" +
//                    "   dir.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));\n" +
//                    "   float dirReduce = max((lumaNW + lumaNE + lumaSW + lumaSE) * (0.25 * FXAA_REDUCE_MUL),FXAA_REDUCE_MIN);\n" +
//                    "   float rcpDirMin = 1.0/(min(abs(dir.x), abs(dir.y)) + dirReduce);\n" +
//                    "   dir = min(vec2( FXAA_SPAN_MAX,  FXAA_SPAN_MAX),max(vec2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX),dir * rcpDirMin)) * inverse_resolution;\n" +
//                    "   vec3 rgbA = 0.5 * (texture2D(sampler0,   gl_FragCoord.xy  * inverse_resolution + dir * (1.0/3.0 - 0.5)).xyz + texture2D(sampler0,   gl_FragCoord.xy  * inverse_resolution + dir * (2.0/3.0 - 0.5)).xyz);\n" +
//                    "   vec3 rgbB = rgbA * 0.5 + 0.25 * (texture2D(sampler0,  gl_FragCoord.xy  * inverse_resolution + dir *  - 0.5).xyz + texture2D(sampler0,  gl_FragCoord.xy  * inverse_resolution + dir * 0.5).xyz);\n" +
//                    "   float lumaB = dot(rgbB, luma);\n" +
//                    "   if((lumaB < lumaMin) || (lumaB > lumaMax)) {\n" +
//                    "      gl_FragColor = vec4(rgbA,1.0);\n" +
//                    "   } else {\n" +
//                    "      gl_FragColor = vec4(rgbB,1.0);\n" +
//                    "   }\n" +
//                    "}";
//
//    private float getDistortionFitY() {
//        float fit = 0.0f;
//
//        switch (this.mc.vrSettings.distortionFitPoint) {
//            case 0:
//                fit = 1.0f;
//                break;
//            case 1:
//                fit = 0.8f;
//                break;
//            case 2:
//                fit = 0.6f;
//                break;
//            case 3:
//                fit = 0.4f;
//                break;
//            case 4:
//                fit = 0.2f;
//                break;
//            case 5:
//            default:
//                fit = 0.0f;
//                break;
//            case 6:
//                fit = 0.0f;
//                break;
//            case 7:
//                fit = 0.0f;
//                break;
//            case 8:
//                fit = 0.0f;
//                break;
//            case 9:
//                fit = 0.0f;
//                break;
//            case 10:
//                fit = 0.0f;
//                break;
//            case 11:
//                fit = 0.0f;
//                break;
//            case 12:
//                fit = 0.0f;
//                break;
//            case 13:
//                fit = 0.0f;
//                break;
//            case 14:
//                fit = 0.0f;
//                break;
//        }
//
//        return fit;
//    }
//
//    private float getDistortionFitX() {
//        float fit = -1.0f;
//
//        switch (this.mc.vrSettings.distortionFitPoint) {
//            case 0:
//                fit = -1.0f;
//                break;
//            case 1:
//                fit = -1.0f;
//                break;
//            case 2:
//                fit = -1.0f;
//                break;
//            case 3:
//                fit = -1.0f;
//                break;
//            case 4:
//                fit = -1.0f;
//                break;
//            case 5:
//            default:
//                fit = -1.0f;
//                break;
//            case 6:
//                fit = -0.9f;
//                break;
//            case 7:
//                fit = -0.8f;
//                break;
//            case 8:
//                fit = -0.7f;
//                break;
//            case 9:
//                fit = -0.6f;
//                break;
//            case 10:
//                fit = -0.5f;
//                break;
//            case 11:
//                fit = -0.4f;
//                break;
//            case 12:
//                fit = -0.3f;
//                break;
//            case 13:
//                fit = -0.2f;
//                break;
//            case 14:
//                fit = -0.1f;
//                break;
//        }
//
//        return fit;
//    }
//
//    private void drawMouseQuad(int mouseX, int mouseY) {
//        GL11.glDisable(GL11.GL_BLEND);
//        GL11.glDisable(GL11.GL_DEPTH_TEST);
//        GL11.glColor3f(1, 1, 1);
//        this.mc.mcProfiler.endStartSection("mouse pointer");
//        this.mc.getTextureManager().bindTexture(Gui.icons);
//        this.mc.ingameGUI.drawTexturedModalRect(mouseX - 7, mouseY - 7, 0, 0, 16, 16);
//
//        GL11.glEnable(GL11.GL_BLEND);
//    }
//
//    private void getPointedBlock(float renderPartialTicks) {
//        if (this.mc.renderViewEntity != null && this.mc.theWorld != null) {
//            this.mc.pointedEntityLiving = null;
//            double blockReachDistance = (double) this.mc.playerController.getBlockReachDistance();
//            double entityReachDistance = blockReachDistance;
//            float cameraYOffset = 1.62f - (this.mc.vrSettings.getPlayerEyeHeight() - this.mc.vrSettings.neckBaseToEyeHeight);
//            Vec3 pos = Vec3.createVectorHelper(renderOriginX + camRelX, renderOriginY + camRelY - cameraYOffset, renderOriginZ + camRelZ);
//            Vec3 aim = Vec3.createVectorHelper(aimX, aimY, aimZ);
//            Vec3 endPos = pos.addVector(aim.xCoord * blockReachDistance, aim.yCoord * blockReachDistance, aim.zCoord * blockReachDistance);
//
//            this.mc.objectMouseOver = this.mc.theWorld.clip(pos, endPos);
//
//
//            double crossDistance = 0;
//            if (this.mc.objectMouseOver != null) {
//                entityReachDistance = this.mc.objectMouseOver.hitVec.distanceTo(pos);
//                crossDistance = entityReachDistance;
//            } else {
//                endPos = pos.addVector(aim.xCoord * 128, aim.yCoord * 128, aim.zCoord * 128);
//                MovingObjectPosition crossPos = this.mc.theWorld.clip(pos, endPos);
//                if (crossPos != null) {
//                    crossDistance = crossPos.hitVec.distanceTo(pos);
//                }
//            }
//
//            if (this.mc.playerController.extendedReach()) {
//                blockReachDistance = 6.0D;
//                entityReachDistance = 6.0D;
//            } else {
//                if (blockReachDistance > 3.0D) {
//                    entityReachDistance = 3.0D;
//                }
//
//                blockReachDistance = entityReachDistance;
//            }
//
//            Vec3 otherpos = mc.renderViewEntity.getPosition(renderPartialTicks);
//            otherpos.yCoord -= (1.62f - (this.mc.vrSettings.getPlayerEyeHeight()));   // Adjust for eye height - TODO: Need to account for neck model?
//            getPointedEntity(otherpos, aim, blockReachDistance, entityReachDistance);
//
//            // Get bounding box of pointedEntity
//            if (this.pointedEntity != null && this.pointedEntity.boundingBox != null) {
//                this.bb = this.pointedEntity.boundingBox.expand(this.pointedEntity.getCollisionBorderSize(),
//                        this.pointedEntity.getCollisionBorderSize(),
//                        this.pointedEntity.getCollisionBorderSize());
//
//                // TODO: How to get distance from eye pos to bounding box ray trace intercept,
//                // and draw crosshair at that depth...?
//            }
//
//            // Set up crosshair position
//            crossX = (float) (aim.xCoord * crossDistance + pos.xCoord - renderOriginX);
//            crossY = (float) (aim.yCoord * crossDistance + pos.yCoord - renderOriginY);
//            crossZ = (float) (aim.zCoord * crossDistance + pos.zCoord - renderOriginZ);
//        }
//    }
//
//    public void getMouseOver(float par1) {
//        //No-op for performance reasons (MouseOver set in render loop)
//    }
//
//    public void startCalibration() {
//        calibrationHelper = new CalibrationHelper(mc);
//    }
//
//    public void resetGuiYawOrientation() {
//        guiYawOrientationResetRequested = true;
//    }
//
//    /**
//     * Render player hand
//     */
//    private void vrRenderHand(float par1, EyeType eye, Matrix4f proj)
//    {
//        if (this.debugViewDirection <= 0) {
//            GL11.glMatrixMode(GL11.GL_PROJECTION);
//            GL11.glPushMatrix();
//            GL11.glLoadIdentity();
//            GL11.glLoadMatrix(proj.transposed().toFloatBuffer());
//
//            float var3 = 0.07F;
//
//            if (this.mc.gameSettings.anaglyph) {
//                GL11.glTranslatef((float) (-(eye.value() * 2 - 1)) * var3, 0.0F, 0.0F);
//            }
//
//            if (this.cameraZoom != 1.0D) {
//                GL11.glTranslatef((float) this.cameraYaw, (float) (-this.cameraPitch), 0.0F);
//                GL11.glScaled(this.cameraZoom, this.cameraZoom, 1.0D);
//            }
//
//            if (this.mc.playerController.enableEverythingIsScrewedUpMode()) {
//                float var4 = 0.6666667F;
//                GL11.glScalef(1.0F, var4, 1.0F);
//            }
//
//            GL11.glMatrixMode(GL11.GL_MODELVIEW);
//            GL11.glPushMatrix();
//            GL11.glLoadIdentity();
//
//            // IPD transformation
//            GL11.glTranslatef(eyeRenderParams.Eyes[eye.value()].ViewAdjust.x, 0.0f, 0.0f);
//
//            GL11.glPushMatrix();
//            this.hurtCameraEffect(par1);
//
//            if (this.mc.gameSettings.viewBobbing) {
//                this.setupViewBobbing(par1);
//            }
//
//            if (this.mc.gameSettings.thirdPersonView == 0 && !this.mc.renderViewEntity.isPlayerSleeping() && !this.mc.playerController.enableEverythingIsScrewedUpMode()) {
//                this.enableLightmap((double) par1);
//                this.itemRenderer.renderItemInFirstPerson(par1);
//                this.disableLightmap((double) par1);
//            }
//
//            GL11.glPopMatrix();
//
//            if (this.mc.gameSettings.thirdPersonView == 0 && !this.mc.renderViewEntity.isPlayerSleeping()) {
//                this.itemRenderer.renderOverlays(par1);
//                this.hurtCameraEffect(par1);
//            }
//
//            if (this.mc.gameSettings.viewBobbing) {
//                this.setupViewBobbing(par1);
//            }
//
//            GL11.glPopMatrix();
//            GL11.glMatrixMode(GL11.GL_PROJECTION);
//            GL11.glPopMatrix();
//            GL11.glMatrixMode(GL11.GL_MODELVIEW);
//        }
//    }
//
//    public void drawBoundingBox(EntityPlayer par1EntityPlayer, AxisAlignedBB bb, float par4) {
//        GL11.glEnable(GL11.GL_BLEND);
//        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
//        GL11.glLineWidth(2.0F);
//        GL11.glDisable(GL11.GL_TEXTURE_2D);
//        GL11.glDepthMask(false);
//        float var5 = 0.002F;
//        double var7 = par1EntityPlayer.lastTickPosX + (par1EntityPlayer.posX - par1EntityPlayer.lastTickPosX) * (double) par4;
//        double var9 = par1EntityPlayer.lastTickPosY + (par1EntityPlayer.posY - par1EntityPlayer.lastTickPosY) * (double) par4;
//        double var11 = par1EntityPlayer.lastTickPosZ + (par1EntityPlayer.posZ - par1EntityPlayer.lastTickPosZ) * (double) par4;
//        drawOutlinedBoundingBox(bb.expand((double) var5, (double) var5, (double) var5).getOffsetBoundingBox(-var7, -var9, -var11));
//
//        GL11.glDepthMask(true);
//        GL11.glEnable(GL11.GL_TEXTURE_2D);
//        GL11.glDisable(GL11.GL_BLEND);
//    }
//
//    public void drawLine(EntityPlayer par1EntityPlayer, Vec3 start, Vec3 end, float par4) {
//        GL11.glEnable(GL11.GL_BLEND);
//        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
//        GL11.glLineWidth(2.0F);
//        GL11.glDisable(GL11.GL_TEXTURE_2D);
//        GL11.glDepthMask(false);
//        float var5 = 0.002F;
//        double var7 = par1EntityPlayer.lastTickPosX + (par1EntityPlayer.posX - par1EntityPlayer.lastTickPosX) * (double) par4;
//        double var9 = par1EntityPlayer.lastTickPosY + (par1EntityPlayer.posY - par1EntityPlayer.lastTickPosY) * (double) par4;
//        double var11 = par1EntityPlayer.lastTickPosZ + (par1EntityPlayer.posZ - par1EntityPlayer.lastTickPosZ) * (double) par4;
//
//        Tessellator var2 = Tessellator.instance;
//        var2.startDrawing(GL11.GL_LINE_STRIP);
//        var2.addVertex(start.xCoord, start.yCoord, start.zCoord);
//        var2.addVertex(end.xCoord, end.yCoord, end.zCoord);
//        var2.draw();
//
//        GL11.glDepthMask(true);
//        GL11.glEnable(GL11.GL_TEXTURE_2D);
//        GL11.glDisable(GL11.GL_BLEND);
//    }
//
//    /**
//     * Draws lines for the edges of the bounding box.
//     */
//    public void drawOutlinedBoundingBox(AxisAlignedBB par1AxisAlignedBB) {
//        Tessellator var2 = Tessellator.instance;
//        var2.startDrawing(3);
//        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
//        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
//        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
//        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
//        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
//        var2.draw();
//        var2.startDrawing(3);
//        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
//        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
//        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
//        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
//        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
//        var2.draw();
//        var2.startDrawing(1);
//        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
//        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
//        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
//        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
//        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
//        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
//        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
//        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
//        var2.draw();
//    }
//
//    private void pollSensors()
//    {
//        // Sleep if we can, so that we poll orientation and position and start rendering at the
//        // last possible moment; and still finish before VSync
////        if (this.mc.vrSettings.frameTimingEnableVsyncSleep && this.mc.gameSettings.enableVsync)
////            if (this.mc.vrSettings.frameTimingEnableVsyncSleep && this.mc.gameSettings.enableVsync && this.vsyncPeriodNanos > 0) {
////                long pollAndRenderTimeNanos = getMedianRenderFrameTimeNanos();
////                long nanosSinceLastRefresh = System.nanoTime() - startVSyncPeriodNanos;
////                long sleepTimeNanos = vsyncPeriodNanos - pollAndRenderTimeNanos - nanosSinceLastRefresh - this.mc.vrSettings.frameTimingSleepSafetyBufferNanos;
////                if (sleepTimeNanos > 0)
////                    sleepNanos(sleepTimeNanos);
////            }
////
////        // Get timing just before orient / position reading
////        startFrameRenderNanos = System.nanoTime();
////
////        // Poll for position, orientation, setting prediction time
////        if (this.mc.vrSettings.useHeadTrackPrediction && this.mc.vrSettings.headTrackPredictionTimeSecs == 0) {
////            long timeDeltaInFutureNanos = getMedianRenderFrameTimeNanos() + this.mc.vrSettings.frameTimingPredictDeltaFromEndFrameNanos;
////            float timeDeltaInFutureSecs = ((float) timeDeltaInFutureNanos) / 1000000000f;
////            //System.out.println("Predict ahead secs: " + timeDeltaInFutureSecs);
////            PluginManager.pollAll(timeDeltaInFutureSecs);
////        } else {
//            // TODO: This needs to be called twice, but only poll keyboard, mouse, controller etc once per frame,
//            // and return the same result the second time through
//            PluginManager.pollAll(0.0f);
// //       }
//    }
//
//    private void setupFrameDebug() {
//        debugOutFrameStart = System.currentTimeMillis();
//        if (debugOutLastFrame) {
//            lastDebugAt = debugOutFrameStart;
//            debugOutLastFrame = false;
//        }
//    }
//
//    private void outputDebugEverySecond(String debug)
//    {
//        long diff = debugOutFrameStart - lastDebugAt;
//        if (diff > 1000) {
//            this.mc.printChatMessage("[Debug]: " + debug);
//            debugOutLastFrame = true;
//        }
//    }
//
//    private GLConfig getGLConfig()
//    {
//        // TODO: For LWJGL 3.0, this function may well be screwed...
//
//        // We need to retrieve certain pointers / handles from LWJGL
//        // for the Oculus SDK. However, these are not exposed by
//        // LWJGL, so use reflection to get hold of the data we need.
//
//        GLConfig glConfig = new GLConfig();
//
//        try
//        {
//            // Get underlying display implementation
//            if (_displayImpl == null)
//            {
//                Method displayMethod = Display.class.getDeclaredMethod("getImplementation");
//                displayMethod.setAccessible(true);
//                _displayImpl = displayMethod.invoke(null, null);
//                System.out.println(String.format("[Minecrift] LWJGL Display implementation class: %s", new Object[]{_displayImpl.getClass().toString()}));
//            }
//
//            switch(LWJGLUtil.getPlatform())
//            {
//                case LWJGLUtil.PLATFORM_WINDOWS:
//                {
//                    // Get HWND pointer...
//                    if (_fHwnd == null)
//                    {
//                        _fHwnd = _displayImpl.getClass().getDeclaredField("hwnd");
//                        _fHwnd.setAccessible(true);
//                    }
//                    glConfig.Window = (Long) _fHwnd.get(_displayImpl);
//                    System.out.println(String.format("[Minecrift] hWnd: 0x%X", new Object[] {glConfig.Window}));
//                    break;
//                }
////                case LWJGLUtil.PLATFORM_LINUX:
////                {
////                    // Get Display and Window pointers...
////                    // TODO:
////                    break;
////                }
////                case LWJGLUtil.PLATFORM_LINUX:
////                {
////                    // Get Display and Window pointers...
////                    // TODO:
////                    break;
////                }
//                default:
//                    throw new Exception ("Current platform not supported!");
//            }
//        }
//        catch (Exception ex)
//        {
//            ex.printStackTrace();
//            glConfig = null;
//        }
//
//        return glConfig;
//    }
//
//    private void vrUpdateCamera(float renderPartialTicks, EyeType eye, Posef eyeRenderPose)
//    {
//        float PIOVER180 = (float) (Math.PI / 180);
//        EntityLivingBase entity = this.mc.renderViewEntity;
//
//        // TODO: Calibration step should be just once per frame
//        // TODO: Pull out calibration, pos track reset, joystick aim update, collision detect
//
//        //runs a step of calibration
//        if (calibrationHelper != null && calibrationHelper.allPluginsCalibrated()) {
//            calibrationHelper = null;
//        }
//
//        if (this.mc.vrSettings.posTrackResetPosition) {
//            mc.positionTracker.resetOrigin();
//            mc.headTracker.resetOrigin();
//            resetGuiYawOrientation();
//            this.mc.vrSettings.posTrackResetPosition = false;
//        }
//
//        // Poll sensors. We may actually sleep inside this
//        // to reduce latency.
//        pollSensors();
//
//        if (JoystickAim.selectedJoystickMode != null)
//            JoystickAim.selectedJoystickMode.update(renderPartialTicks);
//
//        float lookYawOffset = mc.lookaimController.getBodyYawDegrees();
//        float lookPitchOffset = mc.lookaimController.getBodyPitchDegrees();
//
//        if (mc.headTracker.isInitialized() && this.mc.vrSettings.useHeadTracking) {
//            this.mc.mcProfiler.startSection("oculus");
//
//            prevHeadYaw = headYaw;
//            prevHeadPitch = headPitch;
//            prevHeadRoll = headRoll;
//
//            if (this.mc.vrSettings.useQuaternions == false)
//            {
//                // Get Euler angles
//                EulerOrient orient = OculusRift.getEulerAnglesDeg(eyeRenderPose.Orientation,
//                        1f,
//                        Axis.Axis_Y,
//                        Axis.Axis_X,
//                        Axis.Axis_Z,
//                        HandedSystem.Handed_L,
//                        RotateDirection.Rotate_CCW);
//
////                String sEye = "L";
////                if (eye == EyeType.ovrEye_Right)
////                    sEye = "R";
////
////                String out = QuaternionHelper.toEulerDegString(sEye, eyeRenderPose.Orientation,
////                        Axis.Axis_Y,
////                        Axis.Axis_X,
////                        Axis.Axis_Z,
////                        HandedSystem.Handed_L,
////                        RotateDirection.Rotate_CCW
////                        );
////
////                outputDebugEverySecond(out);
//
//                headRoll = orient.roll;//mc.headTracker.getHeadRollDegrees() * this.mc.vrSettings.getHeadTrackSensitivity();
//                headPitch = orient.pitch;//mc.headTracker.getHeadPitchDegrees() * this.mc.vrSettings.getHeadTrackSensitivity();
//                headYaw = orient.yaw;//mc.headTracker.getHeadYawDegrees() * this.mc.vrSettings.getHeadTrackSensitivity();
//
//                cameraPitch = (lookPitchOffset + headPitch) % 180;
//                cameraYaw = (lookYawOffset + headYaw) % 360;
//                cameraRoll = headRoll;
//
//                // Correct for gimbal lock prevention
//                if (cameraPitch > IOrientationProvider.MAXPITCH)
//                    cameraPitch = IOrientationProvider.MAXPITCH;
//                else if (cameraPitch < -IOrientationProvider.MAXPITCH)
//                    cameraPitch = -IOrientationProvider.MAXPITCH;
//
//                if (cameraRoll > IOrientationProvider.MAXROLL)
//                    cameraRoll = IOrientationProvider.MAXROLL;
//                else if (cameraRoll < -IOrientationProvider.MAXROLL)
//                    cameraRoll = -IOrientationProvider.MAXROLL;
//            } else {
//                // Get the tracker orientation quaternion
//                orientation.x = eyeRenderPose.Orientation.x;//;mc.headTracker.getOrientationQuaternion();
//                orientation.y = eyeRenderPose.Orientation.y;
//                orientation.z = eyeRenderPose.Orientation.z;
//                orientation.w = eyeRenderPose.Orientation.w;
//
//                // TODO: This does not work currently
//                // Scale the rotation if necessary
//                if (this.mc.vrSettings.getHeadTrackSensitivity() != 1f)
//                {
////                    System.out.println(String.format("Head track sensitivity: %.2f", new Object[] {Float.valueOf(this.mc.vrSettings.headTrackSensitivity)}));
////                    QuaternionHelper.dump("RAW", orientation);
////
////                    Quaternion orientation1 = QuaternionHelper.clone(orientation);
////                    orientation1.normalise();
////                    Quaternion orientation2 = QuaternionHelper.clone(orientation);
////                    orientation2.normalise();
////
////                      Test 1
////                    //orientation = QuaternionHelper.pow(orientation, (float)Math.floor(this.mc.vrSettings.headTrackSensitivity));
////
////                      Test 2
////                    //Quaternion.mul(orientation2, QuaternionHelper.IDENTITY_QUATERNION, orientation);
////                    //Quaternion.mul(orientation1, orientation, orientation);
////                    //orientation.normalise();
////
////                      Test 3
////                    //orientation = QuaternionHelper.slerp2(QuaternionHelper.IDENTITY_QUATERNION, newOrientation, this.mc.vrSettings.headTrackSensitivity);
////
////                    QuaternionHelper.dump("NEW", orientation);
//                }
//
//                // Get 'raw' tracker orientation
//                EulerOrient rawYawPitchRoll = OculusRift.getEulerAnglesDeg(eyeRenderPose.Orientation,
//                        1f,
//                        Axis.Axis_Y,
//                        Axis.Axis_X,
//                        Axis.Axis_Z,
//                        HandedSystem.Handed_L,
//                        RotateDirection.Rotate_CCW);
//
//                headYaw    = rawYawPitchRoll.yaw;
//                headPitch  = rawYawPitchRoll.pitch;
//                headRoll   = rawYawPitchRoll.roll;
//
//                // Apply pitch offset
//                Quaternion pitchCorrection = new Quaternion();
//                Vector4f vecAxisPitchAngle = new Vector4f(1f, 0f, 0f, -lookPitchOffset * PIOVER180);
//                pitchCorrection.setFromAxisAngle(vecAxisPitchAngle);
//                Quaternion.mul(pitchCorrection, orientation, orientation);
//
//                // Apply yaw offset
//                Quaternion yawCorrection   = new Quaternion();
//                Vector4f vecAxisYawAngle   = new Vector4f(0f, 1f, 0f, (-lookYawOffset * PIOVER180));
//                yawCorrection.setFromAxisAngle(vecAxisYawAngle);
//                Quaternion.mul(yawCorrection, orientation, orientation);
//
//                // Get camera orientation:
//                // Matrix
//                cameraMatrix4f = QuaternionHelper.quatToMatrix4fFloatBuf(orientation);
//
//                // Euler
//                EulerOrient correctedYawPitchRoll = OculusRift.getEulerAnglesDeg(eyeRenderPose.Orientation,
//                        1f,
//                        Axis.Axis_Y,
//                        Axis.Axis_X,
//                        Axis.Axis_Z,
//                        HandedSystem.Handed_L,
//                        RotateDirection.Rotate_CCW);
//
//                cameraYaw    = correctedYawPitchRoll.yaw;
//                cameraPitch  = correctedYawPitchRoll.pitch;
//                cameraRoll   = correctedYawPitchRoll.roll;
//            }
//
//            if (this.mc.vrSettings.debugPose)
//            {
//                System.out.println(String.format("headYaw:   %.2f, headPitch:   %.2f, headRoll:   %.2f", new Object[]{Float.valueOf(headYaw), Float.valueOf(headPitch), Float.valueOf(headRoll)}));
//                System.out.println(String.format("cameraYaw: %.2f, cameraPitch: %.2f, cameraRoll: %.2f", new Object[]{Float.valueOf(cameraYaw), Float.valueOf(cameraPitch), Float.valueOf(cameraRoll)}));
//            }
//
//            this.mc.mcProfiler.endSection();
//        } else {
//            cameraRoll = 0;
//            cameraPitch = lookPitchOffset;
//            cameraYaw = lookYawOffset;
//        }
//
//        if (entity != null) {
//            //set movement direction
//            if (this.mc.vrSettings.lookMoveDecoupled)
//                entity.rotationYaw = lookYawOffset;
//            else
//                entity.rotationYaw = cameraYaw;
//            entity.rotationYawHead = cameraYaw;
//            entity.rotationPitch = cameraPitch;
//
//        }
//
//        if (this.mc.vrSettings.aimKeyholeWidthDegrees > 0)
//            aimYaw = mc.lookaimController.getAimYaw();
//        else
//            aimYaw = cameraYaw;
//
//        if (this.mc.vrSettings.keyholeHeight > 0)
//            aimPitch = mc.lookaimController.getAimPitch();
//        else
//            aimPitch = cameraPitch;
//
//        aimPitch -= this.mc.vrSettings.aimPitchOffset;
//
//
//        //Not sure if headPitch or cameraPitch is better here... they really should be the same; silly
//        //people with their "pitch affects camera" settings.
//        //At any rate, using cameraPitch makes the UI look less silly
//
//        // TODO: This will have already been called and the data returned within the Posef
//        float dummyIpd = 0.064f;
//        mc.positionTracker.update(dummyIpd, headYaw, cameraPitch, cameraRoll, lookYawOffset, 0.0f, 0.0f);
//
//        //Do head/neck model in non-GL math so we can use camera location(between eyes)
//        Vec3 cameraOffset = mc.positionTracker.getCenterEyePosition();    // TODO: We want to use actual eye pos here instead of centre
//        cameraOffset.rotateAroundY((float) Math.PI);
//
//        //The worldOrigin is at player "eye height" (1.62) above foot position
//        camRelX = (float) cameraOffset.xCoord;
//        camRelY = (float) cameraOffset.yCoord;
//        camRelZ = (float) cameraOffset.zCoord;
//
//        // TODO: This collision check needs to extracted out and be done once. Must still use centre eye position - we don't want one
//        // eye corrected for collision, and the other not.
//        if (this.mc.theWorld != null && this.mc.gameSettings.thirdPersonView == 0) {
//            float fulldist = (float) (Math.sqrt(camRelX * camRelX + camRelY * camRelY + camRelZ * camRelZ));
//
//            float cameraYOffset = 1.62f - (this.mc.vrSettings.getPlayerEyeHeight() - this.mc.vrSettings.neckBaseToEyeHeight);
//            float colldist = checkCameraCollision(renderOriginX, renderOriginY - cameraYOffset, renderOriginZ,
//                    -camRelX, -camRelY, -camRelZ, fulldist);
//            if (colldist != fulldist) {
//                // #47 Removed additional scale factor
//                float scale = colldist / fulldist;
//                camRelX *= scale;
//                camRelY *= scale;
//                camRelZ *= scale;
//            }
//        }
//
//        Vec3 look = Vec3.createVectorHelper(0, 0, 1);
//        look.rotateAroundX(-cameraPitch * PIOVER180);
//        look.rotateAroundY(-cameraYaw * PIOVER180);
//        lookX = (float) look.xCoord;
//        lookY = (float) look.yCoord;
//        lookZ = (float) look.zCoord;
//
//        Vec3 aim = Vec3.createVectorHelper(0, 0, 1);
//        aim.rotateAroundX(-aimPitch * PIOVER180);
//        aim.rotateAroundY(-aimYaw * PIOVER180);
//        aimX = (float) aim.xCoord;
//        aimY = (float) aim.yCoord;
//        aimZ = (float) aim.zCoord;
//
//        if (guiYawOrientationResetRequested) {
//            //Hit once at startup and if reset requested (usually during calibration when an origin
//            //has been set)
//            guiHeadYaw = cameraYaw;
//            guiYawOrientationResetRequested = false;
//        }
//    }
//}

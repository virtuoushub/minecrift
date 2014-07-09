/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;


import com.mtbs3d.minecrift.api.*;

import de.fruitfly.ovr.EyeRenderParams;
import de.fruitfly.ovr.IOculusRift;
import de.fruitfly.ovr.OculusRift;
import de.fruitfly.ovr.UserProfileData;
import de.fruitfly.ovr.enums.EyeType;
import de.fruitfly.ovr.structs.*;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.vector.Quaternion;

public class MCOculus extends OculusRift //OculusRift does most of the heavy lifting 
	implements IEyePositionProvider, IOrientationProvider, IBasePlugin, IHMDInfo, IStereoProvider, IEventNotifier, IEventListener {

    public static final int NOT_CALIBRATING = 0;
    public static final int CALIBRATE_AWAITING_FIRST_ORIGIN = 1;
    public static final int CALIBRATE_AT_FIRST_ORIGIN = 2;
    public static final int CALIBRATE_COOLDOWN = 7;

    public static final long COOLDOWNTIME_MS = 1000L;

    private boolean isCalibrated = false;
    private long coolDownStart = 0L;
    private int calibrationStep = NOT_CALIBRATING;
    private int MagCalSampleCount = 0;
    private boolean forceMagCalibration = false; // Don't force mag cal initially
    private FrameTiming frameTiming = new FrameTiming();

    @Override
    public EyeType eyeRenderOrder(int index)
    {
        HmdDesc desc = getHMDInfo();
        if (index < 0 || index >= desc.EyeRenderOrder.length)
            return EyeType.ovrEye_Left;

        return desc.EyeRenderOrder[index];
    }

    @Override
    public boolean usesDistortion() {
        return true;
    }

    @Override
    public boolean isStereo() {
        return true;
    }

    @Override
    public boolean isGuiOrtho()
    {
        return false;
    }

    public FrameTiming getFrameTiming() { return frameTiming; };

    public static UserProfileData theProfileData = null;

    public void beginFrame()
    {
        frameTiming = super.beginFrameGetTiming();
    }

    public Posef beginEyeRender(EyeType eye)
    {
        return super.beginEyeRender(eye);
    }

    public Matrix4f getMatrix4fProjection(FovPort fov,
                                          float nearClip,
                                          float farClip)
    {
         return super.getMatrix4fProjection(fov, nearClip, farClip);
    }

    public void endEyeRender(EyeType eye)
    {
        super.endEyeRender(eye);
    }

    public void endFrame()
    {
        GL11.glDisable(GL11.GL_CULL_FACE);  // Oculus wants CW orientations, avoid the problem by turning off culling...
        GL11.glDisable(GL11.GL_DEPTH_TEST); // Nothing is drawn with depth test on...

        // End the frame
        super.endFrame();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Unbind GL_ARRAY_BUFFER for my own vertex arrays to work...
        GL11.glEnable(GL11.GL_CULL_FACE); // Turn back on...
        GL11.glEnable(GL11.GL_DEPTH_TEST); // Turn back on...
        GL11.glClearDepth(1); // Oculus set this to 0 (the near plane), return to normal...
        ARBShaderObjects.glUseProgramObjectARB(0); // Oculus shader is still active, turn it off...

        Display.processMessages();
    }

    @Override
    public HmdDesc getHMDInfo()
    {
        HmdDesc hmdDesc = new HmdDesc();
        if (isInitialized())
            hmdDesc = getHmdDesc();

        return hmdDesc;
    }

	@Override
	public String getName() {
		return "Oculus";
	}

	@Override
	public String getID() {
		return "oculus";
	}

    @Override
    public void update(float ipd, float yawHeadDegrees, float pitchHeadDegrees, float rollHeadDegrees, float worldYawOffsetDegrees, float worldPitchOffsetDegrees, float worldRollOffsetDegrees) {
        // TODO:
    }

    @Override
    public Vec3 getCenterEyePosition()
    {
        return null;
    }

    @Override
    public Vec3 getEyePosition(EyeType eye)
    {
        return super.getEyePosition(eye);
    }

    @Override
	public void resetOrigin() {
        // TODO:
    }

    @Override
    public void resetOriginRotation() {
        // TODO:
    }

    @Override
    public void setPrediction(float delta, boolean enable) {
        // TODO: Ignored for now
    }

    @Override
    public void beginAutomaticCalibration()
    {
        if (isInitialized())
            processCalibration();
    }

    @Override
    public void updateAutomaticCalibration()
    {
        if (isInitialized())
            processCalibration();
    }

    @Override
    public boolean isCalibrated() {
        if (!isInitialized())
            return true;  // Return true if not initialised

        return isCalibrated;
    }

	@Override
	public String getCalibrationStep()
    {
        String step = "";

        switch (calibrationStep)
        {
            case CALIBRATE_AWAITING_FIRST_ORIGIN:
            {
                step = "Look ahead and press SPACEBAR";
                break;
            }
            case CALIBRATE_AT_FIRST_ORIGIN:
            case CALIBRATE_COOLDOWN:
            {
                step = "Done!";
                break;
            }
        }

        return step;
	}

    @Override
    public void eventNotification(int eventId)
    {
        switch (eventId)
        {
            case IOrientationProvider.EVENT_ORIENTATION_AT_ORIGIN:
            {
                if (calibrationStep == CALIBRATE_AWAITING_FIRST_ORIGIN)
                {
                    calibrationStep = CALIBRATE_AT_FIRST_ORIGIN;
                    processCalibration();
                }
                break;
            }
            case IBasePlugin.EVENT_CALIBRATION_SET_ORIGIN:
            {
                resetOrigin();
            }
        }
    }

    @Override
    public synchronized void registerListener(IEventListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public synchronized void notifyListeners(int eventId)
    {
        for (IEventListener listener : listeners)
        {
            if (listener != null)
                listener.eventNotification(eventId);
        }
    }

    private void processCalibration()
    {
        switch (calibrationStep)
        {
            case NOT_CALIBRATING:
            {
                calibrationStep = CALIBRATE_AWAITING_FIRST_ORIGIN;
                isCalibrated = false;
                break;
            }
            case CALIBRATE_AT_FIRST_ORIGIN:
            {
                //_reset();

                // Calibration of Mag cal is now handled solely by the Oculus config utility.

                MagCalSampleCount = 0;
                coolDownStart = System.currentTimeMillis();
                calibrationStep = CALIBRATE_COOLDOWN;
                resetOrigin();
                notifyListeners(IBasePlugin.EVENT_CALIBRATION_SET_ORIGIN);

                break;
            }
            case CALIBRATE_COOLDOWN:
            {
                if ((System.currentTimeMillis() - coolDownStart) > COOLDOWNTIME_MS)
                {
                    coolDownStart = 0;
                    calibrationStep = NOT_CALIBRATING;
                    isCalibrated = true;
                }
                break;
            }
        }
    }

    @Override
    public void poll(/*EyeType eyeHint, */float delta)
    {
        // Do nothing
    }


	@Override
	public float getHeadYawDegrees()
    {
        return 0.0f;//getYawDegrees_LH();
	}

	@Override
	public float getHeadPitchDegrees()
    {
		return 0.0f;//getPitchDegrees_LH();
	}

	@Override
	public float getHeadRollDegrees()
    {
		return 0.0f;//getRollDegrees_LH();
	}

    @Override
    public Quaternion getOrientationQuaternion()
    {
        Quatf orient = Quatf.IDENTITY;//super.getOrientQuatf();

        // Needs x, y, z, w
        return new Quaternion(orient.x, orient.y, orient.z, orient.w);
    }

    @Override
    public UserProfileData getProfileData()
    {
        UserProfileData userProfile = null;

        if (isInitialized())
        {
            //userProfile = _getUserProfileData();   // TODO: Profiles
        }

        return userProfile;
    }

    @Override
    public String[] getUserProfiles()
    {
        String[] profileNames = null;

        if (isInitialized())
        {
            //profileNames = _getUserProfiles();    // TODO: Profiles
        }

        return profileNames;
    }

    @Override
    public boolean loadProfile(String profileName)
    {
        if (!isInitialized())
            return false;

        return false;//_loadUserProfile(profileName);     // TODO: Profiles
    }

    public EyeRenderParams getEyeRenderParams(int viewPortWidth, int viewPortHeight)
    {
        return null;
    }
    public EyeRenderParams getEyeRenderParams(int viewPortX, int viewPortY, int viewPortWidth, int viewPortHeight, float nearClip, float farClip)
    {
        return null;
    }
    public EyeRenderParams getEyeRenderParams(int viewPortX,
                                              int viewPortY,
                                              int viewPortWidth,
                                              int viewPortHeight,
                                              float clipNear,
                                              float clipFar,
                                              float eyeToScreenDistanceScaleFactor,
                                              float lensSeparationScaleFactor)
    {
        return null;
    }
    public EyeRenderParams getEyeRenderParams(int viewPortX,
                                              int viewPortY,
                                              int viewPortWidth,
                                              int viewPortHeight,
                                              float clipNear,
                                              float clipFar,
                                              float eyeToScreenDistanceScaleFactor,
                                              float lensSeparationScaleFactor,
                                              float distortionFitX,
                                              float distortionFitY,
                                              IOculusRift.AspectCorrectionType aspectCorrectionType)
    {
        return null;
    }
}

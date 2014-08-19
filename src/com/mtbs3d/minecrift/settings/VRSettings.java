/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.settings;

import java.io.*;

import com.mtbs3d.minecrift.MCHydra;
import de.fruitfly.ovr.IOculusRift;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VRSettings
{
    public static final int VERSION = 1;
    public static final Logger logger = LogManager.getLogger();
	public static VRSettings inst;
	public String defaults = new String();
    public static final int UNKNOWN_VERSION = 0;

    // Minecrift
    public static final int POS_TRACK_NECK = 0;
    public static final int POS_TRACK_HYDRA = 1;

    private static final String[] POS_TRACK_HYDRA_LOC = new String[] {"HMD (L&R sides)", "HMD (Left side)", "HMD (Top)", "HMD (Right side)", "Back Of Head", "Direct"};
    private static final String[] JOYSTICK_AIM_TYPE = new String[] {"Keyhole (tight)", "Keyhole (loose)","Recentering" };
    //TODO: Shouldn't these be an enum? 
    public static final int POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT = 0;
    public static final int POS_TRACK_HYDRA_LOC_HMD_LEFT = 1;
    public static final int POS_TRACK_HYDRA_LOC_HMD_TOP = 2;
    public static final int POS_TRACK_HYDRA_LOC_HMD_RIGHT = 3;
    public static final int POS_TRACK_HYDRA_LOC_BACK_OF_HEAD = 4;
    //public static final int POS_TRACK_HYDRA_LOC_DIRECT = 5;

    public static final int CALIBRATION_STRATEGY_AT_STARTUP = 0;

    public static final int RENDER_FIRST_PERSON_FULL = 0;
    public static final int RENDER_FIRST_PERSON_HAND = 1;
    public static final int RENDER_FIRST_PERSON_NONE = 2;
    public static final int RENDER_CROSSHAIR_MODE_ALWAYS = 0;
    public static final int RENDER_CROSSHAIR_MODE_HUD = 1;
    public static final int RENDER_CROSSHAIR_MODE_NEVER = 2;
    public static final int RENDER_BLOCK_OUTLINE_MODE_ALWAYS = 0;
    public static final int RENDER_BLOCK_OUTLINE_MODE_HUD = 1;
    public static final int RENDER_BLOCK_OUTLINE_MODE_NEVER = 2;

    public int version = UNKNOWN_VERSION;
    public boolean newlyCreated = true;
    public boolean useVRRenderer  = false; //default to false
    public boolean useQuaternions = true;
    public boolean debugPose = false;
	protected float playerEyeHeight = 1.74f;  // Use getPlayerEyeHeight()
	public float eyeProtrusion = 0.185f;
	public float neckBaseToEyeHeight = 0.225f;
    public float movementSpeedMultiplier = 1.0f;
    public boolean useDistortion = true;
    public boolean loadMumbleLib = true;
    public boolean useHeadTracking = true;
    public boolean useHeadTrackPrediction = true;
    public float headTrackPredictionTimeSecs = 0f;
    protected float ipd = 0.0635F;   // Use getIPD()
    protected float oculusProfileIpd = ipd;
    public String oculusProfileName;
    public String oculusProfileGender;
    protected float oculusProfilePlayerEyeHeight = playerEyeHeight;
    public float hudOpacity = 1.0f;
    public boolean menuBackground = false;
    public boolean renderHeadWear = false;
    public int renderFullFirstPersonModelMode = RENDER_FIRST_PERSON_FULL;
    public float renderPlayerOffset = 0.0f;
    public boolean useChromaticAbCorrection = true;
    // SDK 0.4.0
    public boolean useTimewarp = true;
    public boolean useVignette = true;
    public boolean useLowPersistence = true;
    public boolean useDynamicPrediction = false; // Default to off for now
    public float   renderScaleFactor = 1.1f; // Avoid weird star shaped shimmer at renderscale = 1
    public boolean useDirectRenderMode = false;
    public boolean useDisplayMirroring = false;
    public boolean useDisplayOverdrive = true;
    public boolean posTrackBlankOnCollision = true;

    // TODO: Clean-up all the redundant crap!
    public boolean useDistortionTextureLookupOptimisation = false;
    public boolean useFXAA = false;
    public float hudScale = 1.25f;
    public boolean allowMousePitchInput = false;
    public float hudDistance = 1.25f;
    public float hudPitchOffset = 0.0f;
    public float hudYawOffset = 0.0f;
    public boolean hudLockToHead = false;
    public float fovScaleFactor = 1.0f;
    public float lensSeparationScaleFactor = 1.0f;
    private IOculusRift.AspectCorrectionType aspectRatioCorrectionMode = IOculusRift.AspectCorrectionType.CORRECTION_AUTO;
    private int aspectRatioCorrection = aspectRatioCorrectionMode.getValue();
    public int distortionFitPoint = 5;
    protected float headTrackSensitivity = 1.0f;
    public boolean useSupersample = false;   // default to off
    public float superSampleScaleFactor = 2.0f;
    public boolean lookMoveDecoupled = false;
    public boolean useOculusProfile = true;
    public int posTrackHydraLoc = POS_TRACK_HYDRA_LOC_HMD_LEFT;
    public boolean posTrackHydraUseController1 = true;
    public boolean posTrackHydraDebugCentreEyePos = false;
    public float posTrackHydraDistanceScale = 1.00f;
    public boolean posTrackResetPosition = true;
    public float posTrackHydraLROffsetX = 0.0f;
    public float posTrackHydraLROffsetY = 0.0f;
    public float posTrackHydraLROffsetZ = 0.0f;
    public float posTrackHydraLOffsetX = -0.108f;
    public float posTrackHydraLOffsetY = 0.0f;
    public float posTrackHydraLOffsetZ = 0.0f;
    public float posTrackHydraROffsetX = 0.108f;
    public float posTrackHydraROffsetY = 0.0f;
    public float posTrackHydraROffsetZ = 0.0f;
    public float posTrackHydraTOffsetX = 0.0f;
    public float posTrackHydraTOffsetY = 0.085f;
    public float posTrackHydraTOffsetZ = 0.0f;
    public float posTrackHydraBLOffsetX = 0.05f;
    public float posTrackHydraBLOffsetY = 0.11f;
    public float posTrackHydraBLOffsetZ = -0.225f;
    public float posTrackHydraBROffsetX = -0.05f;
    public float posTrackHydraBROffsetY = 0.11f;
    public float posTrackHydraBROffsetZ = -0.225f;
    public boolean posTrackHydraBIsPointingLeft = true;
    public float posTrackHydraYAxisDistanceSkewAngleDeg = 0.0f;
	public float joystickSensitivity = 3f;
	public int joystickAimType = 0;
	public float joystickDeadzone = 0.1f;
	public float aimKeyholeWidthDegrees = 0f;
	public float keyholeHeight = 0f;
	public boolean keyholeHeadRelative = true;
    public boolean hydraUseFilter = true;
    public float magRefDistance = 0.15f;
	public String headPositionPluginID = "oculus";
	public String headTrackerPluginID = "oculus";
	public String hmdPluginID = "oculus";
    public String stereoProviderPluginID = "oculus";
	public String controllerPluginID = "mouse";
    public int calibrationStrategy = CALIBRATION_STRATEGY_AT_STARTUP;
    public float crosshairScale = 1.0f;
    public int renderInGameCrosshairMode = RENDER_CROSSHAIR_MODE_HUD;
    public int renderBlockOutlineMode = RENDER_BLOCK_OUTLINE_MODE_HUD;
    public boolean showEntityOutline = false;
    public boolean crosshairRollsWithHead = true;
    public boolean hudOcclusion = false;
    public boolean soundOrientWithHead = true;
	public float chatOffsetX = 0;
	public float chatOffsetY = 0;
	public float aimPitchOffset = 0;

    // Experimental: frame timing parameters   - to use these, generally you should have already configured Minecraft's graphics
    //                                           so that you have a frame rate as high as possible. Auto head prediction time enabled.
    public int frameTimingSmoothOverFrameCount = 11;           // Calculate the median time to render a frame over
                                                               // this number of frames. Should be an odd number.
    public int frameTimingPredictDeltaFromEndFrameNanos = -0;  // Negative numbers to move prediction time a set amount before
                                                               // the end of the frame, positive for after. This is added to the
                                                               // median frame time to generate a prediction time in the future for
                                                               // the Oculus SDK.

    // Experimental: sleep before render settings.      - Again, framerate should already be configured to be quite a bit above vsync framerate.
    //                                                    VSync must be on.

    public boolean frameTimingEnableVsyncSleep = false;        // Enable sleep before start of render. Renderer should sleep, then read sensor
                                                               // at the last possible moment before starting the render, and still
                                                               // have time to finish before vsync. Too high a sleep value means
                                                               // we may not finish the scene before vsync. Too low and the HMD poll latency
                                                               // will increase.
    public int frameTimingSleepSafetyBufferNanos = 0;          // Positive values increase the safety margin between
                                                               // end-of-render and vsync. Negative values reduce.

    private Minecraft mc;

    private File optionsVRFile;
    private File optionsVRBackupFile;
    
    public VRSettings( Minecraft minecraft, File dataDir )
    {
    	mc = minecraft;
    	inst = this;
    	storeDefaults();

        this.optionsVRFile = new File(dataDir, "optionsvr.txt");
        this.optionsVRBackupFile = new File(dataDir, "optionsvr.bak");
        this.loadOptions();
        this.setDefaults();
        this.saveOptions();  // Make sure defaults are initialised in the file
    }

    public void loadOptions()
    {
        if (!this.optionsVRFile.exists())
            return;

        try
        {
            loadOptions(new FileReader(this.optionsVRFile));
        }
        catch (IOException e)
        {
            logger.warn("Failed to load VR options: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDefaults()
    {
        StringReader sr = new StringReader(this.defaults);
        loadOptions(sr);
    }
    
    public void loadOptions(Reader reader)
    {
        // Load Minecrift options
        try
        {
            BufferedReader optionsVRReader = new BufferedReader(reader);

            String var2 = "";

            while ((var2 = optionsVRReader.readLine()) != null)
            {
                try
                {
                    String[] optionTokens = var2.split(":");

                    if (optionTokens[0].equals("version"))
                    {
                        this.version = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("newlyCreated"))
                    {
                        this.newlyCreated = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useVRRenderer"))
                    {
                        this.useVRRenderer = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useQuaternions"))
                    {
                        this.useQuaternions = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("debugPose"))
                    {
                        this.debugPose = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("playerEyeHeight"))
                    {
                        this.playerEyeHeight = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("neckBaseToEyeHeight"))
                    {
                        this.neckBaseToEyeHeight = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("eyeProtrusion"))
                    {
                        this.eyeProtrusion = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("ipd"))
                    {
                        this.ipd = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("headTrackPredictionTimeSecs"))
                    {
                        this.headTrackPredictionTimeSecs = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("headTrackerPluginID"))
                    {
                        this.headTrackerPluginID = optionTokens[1];
                    }

                    if (optionTokens[0].equals("headPositionPluginID"))
                    {
                        this.headPositionPluginID = optionTokens[1];
                    }

                    if (optionTokens[0].equals("hmdPluginID"))
                    {
                        this.hmdPluginID = optionTokens[1];
                    }

                    if (optionTokens[0].equals("stereoProviderPluginID"))
                    {
                        this.stereoProviderPluginID = optionTokens[1];
                    }

                    if (optionTokens[0].equals("controllerPluginID"))
                    {
                        this.controllerPluginID = optionTokens[1];
                    }

                    if (optionTokens[0].equals("hudOpacity"))
                    {
                        this.hudOpacity = this.parseFloat(optionTokens[1]);
                        if(hudOpacity< 0.15f)
                        	hudOpacity = 1.0f;
                    }

                    if (optionTokens[0].equals("useHeadTrackPrediction"))
                    {
                        this.useHeadTrackPrediction = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useHeadTracking"))
                    {
                        this.useHeadTracking = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useDistortion"))
                    {
                        this.useDistortion = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("loadMumbleLib"))
                    {
                        this.loadMumbleLib = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("renderHeadWear"))
                    {
                        this.renderHeadWear = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("menuBackground"))
                    {
                        this.menuBackground = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("vrHideGUI"))
                    {
                        this.mc.gameSettings.hideGUI = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("renderFullFirstPersonModelMode"))
                    {
                        this.renderFullFirstPersonModelMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("useChromaticAbCorrection"))
                    {
                        this.useChromaticAbCorrection = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useTimewarp"))
                    {
                        this.useTimewarp = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useVignette"))
                    {
                        this.useVignette = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("posTrackBlankOnCollision"))
                    {
                        this.posTrackBlankOnCollision = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useLowPersistence"))
                    {
                        this.useLowPersistence = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useDynamicPrediction"))
                    {
                        this.useDynamicPrediction = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useDisplayOverdrive"))
                    {
                        this.useDisplayOverdrive = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useDirectRenderMode"))
                    {
                        this.useDirectRenderMode = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useDisplayMirroring"))
                    {
                        this.useDisplayMirroring = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useDistortionTextureLookupOptimisation"))
                    {
                        this.useDistortionTextureLookupOptimisation = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useFXAA"))
                    {
                        this.useFXAA = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("hudScale"))
                    {
                        this.hudScale = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("renderPlayerOffset"))
                    {
                        this.renderPlayerOffset = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("renderScaleFactor"))
                    {
                        this.renderScaleFactor = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("allowMousePitchInput"))
                    {
                        this.allowMousePitchInput = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("hudLockToHead"))
                    {
                        this.hudLockToHead = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("hudDistance"))
                    {
                        this.hudDistance = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("hudPitchOffset"))
                    {
                        this.hudPitchOffset = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("hudYawOffset"))
                    {
                        this.hudYawOffset = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("useSupersample"))
                    {
                        this.useSupersample = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("superSampleScaleFactor"))
                    {
                        this.superSampleScaleFactor = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("fovScaleFactor"))
                    {
                        this.fovScaleFactor = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("lensSeparationScaleFactor"))
                    {
                        this.lensSeparationScaleFactor = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("distortionFitPoint"))
                    {
                        this.distortionFitPoint = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("aspectRatioCorrection"))
                    {
                        this.aspectRatioCorrection = Integer.parseInt(optionTokens[1]);
                        setAspectCorrectionMode(this.aspectRatioCorrection);
                    }

                    if (optionTokens[0].equals("calibrationStrategy1"))      // Deliberately using a new value to get people using the 'At startup' setting again by default.
                    {
                        this.calibrationStrategy = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("headTrackSensitivity"))
                    {
                        this.headTrackSensitivity = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("movementSpeedMultiplier"))
                    {
                        this.movementSpeedMultiplier = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("lookMoveDecoupled"))
                    {
                        this.lookMoveDecoupled = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("posTrackHydraLoc"))
                    {
                        this.posTrackHydraLoc = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("renderFullFirstPersonModelMode"))
                    {
                        this.renderFullFirstPersonModelMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("renderInGameCrosshairMode"))
                    {
                        this.renderInGameCrosshairMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("renderBlockOutlineMode"))
                    {
                        this.renderBlockOutlineMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraLROffsetX"))
                    {
                        this.posTrackHydraLROffsetX = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraLROffsetY"))
                    {
                        this.posTrackHydraLROffsetY = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraLROffsetZ"))
                    {
                        this.posTrackHydraLROffsetZ = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraLOffsetX"))
                    {
                        this.posTrackHydraLOffsetX = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraLOffsetY"))
                    {
                        this.posTrackHydraLOffsetY = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraLOffsetZ"))
                    {
                        this.posTrackHydraLOffsetZ = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraROffsetX"))
                    {
                        this.posTrackHydraROffsetX = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraROffsetY"))
                    {
                        this.posTrackHydraROffsetY = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraROffsetZ"))
                    {
                        this.posTrackHydraROffsetZ = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraTOffsetX"))
                    {
                        this.posTrackHydraTOffsetX = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraTOffsetY"))
                    {
                        this.posTrackHydraTOffsetY = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraTOffsetZ"))
                    {
                        this.posTrackHydraTOffsetZ = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraBLOffsetX"))
                    {
                        this.posTrackHydraBLOffsetX = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraBLOffsetY"))
                    {
                        this.posTrackHydraBLOffsetY = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraBLOffsetZ"))
                    {
                        this.posTrackHydraBLOffsetZ = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraBROffsetX"))
                    {
                        this.posTrackHydraBROffsetX = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraBROffsetY"))
                    {
                        this.posTrackHydraBROffsetY = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraBROffsetZ"))
                    {
                        this.posTrackHydraBROffsetZ = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraDistanceScale"))
                    {
                        this.posTrackHydraDistanceScale = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("crosshairScale"))
                    {
                        this.crosshairScale = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("posTrackHydraUseController1"))
                    {
                        this.posTrackHydraUseController1 = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useOculusProfile"))
                    {
                        this.useOculusProfile = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("posTrackHydraBIsPointingLeft"))
                    {
                        this.posTrackHydraBIsPointingLeft = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("hydraUseFilter"))
                    {
                        this.hydraUseFilter = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("renderInGameCrosshairMode"))
                    {
                        this.renderInGameCrosshairMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("renderBlockOutlineMode"))
                    {
                        this.renderBlockOutlineMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("showEntityOutline"))
                    {
                        this.showEntityOutline = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("crosshairRollsWithHead"))
                    {
                        this.crosshairRollsWithHead = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("hudOcclusion"))
                    {
                        this.hudOcclusion = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("soundOrientWithHead"))
                    {
                        this.soundOrientWithHead = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("chatOffsetX"))
                    {
                        this.chatOffsetX = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("chatOffsetY"))
                    {
                        this.chatOffsetY = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("aimPitchOffset"))
                    {
                        this.aimPitchOffset = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("joystickSensitivity"))
                    {
                        this.joystickSensitivity = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("joystickDeadzone"))
                    {
                        this.joystickDeadzone = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("joystickAimType"))
                    {
                        this.joystickAimType = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("keyholeWidth"))
                    {
                        this.aimKeyholeWidthDegrees = this.parseFloat(optionTokens[1]);
                    }
                    if (optionTokens[0].equals("keyholeHeight"))
                    {
                        this.keyholeHeight = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("keyholeHeadRelative"))
                    {
                    	this.keyholeHeadRelative = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("posTrackHydraYAxisDistanceSkewAngleDeg"))
                    {
                        this.posTrackHydraYAxisDistanceSkewAngleDeg = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("oculusProfileIpd"))
                    {
                        this.oculusProfileIpd = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("oculusProfilePlayerEyeHeight"))
                    {
                        this.oculusProfilePlayerEyeHeight = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("frameTimingEnableVsyncSleep"))
                    {
                        this.frameTimingEnableVsyncSleep = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("frameTimingSleepSafetyBufferNanos"))
                    {
                        this.frameTimingSleepSafetyBufferNanos = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("frameTimingSmoothOverFrameCount"))
                    {
                        this.frameTimingSmoothOverFrameCount = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("frameTimingPredictDeltaFromEndFrameNanos"))
                    {
                        this.frameTimingPredictDeltaFromEndFrameNanos = Integer.parseInt(optionTokens[1]);
                    }
                }
                catch (Exception var7)
                {
                    logger.warn("Skipping bad VR option: " + var2);
                    var7.printStackTrace();
                }
            }

            optionsVRReader.close();
        }
        catch (Exception var8)
        {
            logger.warn("Failed to load VR options!");
            var8.printStackTrace();
        }
    }

    public void setDefaults()
    {
        if (newlyCreated)
        {
            // Set reasonable Optifine / game defaults
            this.mc.gameSettings.limitFramerate = (int) GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
            this.mc.gameSettings.enableVsync = true;
            this.mc.gameSettings.ofChunkLoading = 1;
            this.mc.gameSettings.renderDistanceChunks = 8;
        }

        if (version == UNKNOWN_VERSION)
        {
            // Minecrift 1.6 or below --> 1.7.10 - wipe the file
            try
            {
                saveOptions(new FileWriter(this.optionsVRBackupFile));
            }
            catch (IOException e)
            {
                logger.warn("Failed to backup VR options: " + e.getMessage());
                e.printStackTrace();
            }
            loadDefaults();
        }

        version = VERSION;
    }
    
    public String getKeyBinding( VRSettings.VrOptions par1EnumOptions )
    {
        String var2 = par1EnumOptions.getEnumString();

        String var3 = var2 + ": ";
        String var4 = var3;
        String var5;

        switch( par1EnumOptions) {
	        case USE_VR:
	            return this.useVRRenderer ? var4 + "ON" : var4 + "OFF";
            case USE_QUATERNIONS:
                return this.useQuaternions ? var4 + "Quaternion" : var4 + "Euler";
	        case EYE_HEIGHT:
	            return var4 + String.format("%.2fm", new Object[] { Float.valueOf(getPlayerEyeHeight()) });
	        case EYE_PROTRUSION:
	            return var4 + String.format("%.3fm", new Object[] { Float.valueOf(this.eyeProtrusion) });
	        case NECK_LENGTH:
	            return var4 + String.format("%.3fm", new Object[] { Float.valueOf(this.neckBaseToEyeHeight) });
	        case MOVEMENT_MULTIPLIER:
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.movementSpeedMultiplier) });
	        case USE_DISTORTION:
	            return this.useDistortion ? var4 + "ON" : var4 + "OFF";
            case LOAD_MUMBLE_LIB:
                return this.loadMumbleLib ? var4 + "YES" : var4 + "NO";
	        case HEAD_TRACKING:
	            return this.useHeadTracking ? var4 + "ON" : var4 + "OFF";
	        case HEAD_TRACK_PREDICTION:
	            return this.useHeadTrackPrediction ? var4 + "ON" : var4 + "OFF";
            case DELAYED_RENDER:
                return this.frameTimingEnableVsyncSleep ? var4 + "Immediate" : var4 + "Delayed";
	        case IPD:
	            return var4 + String.format("%.1fmm", new Object[] { Float.valueOf(getIPD() * 1000) });
	        case HEAD_TRACK_PREDICTION_TIME:
                if (headTrackPredictionTimeSecs == 0.0f)
                    return var4 + "Auto";
                else
	                return var4 + String.format("%.0fms", new Object[] { Float.valueOf(this.headTrackPredictionTimeSecs * 1000) });
	        case HUD_OPACITY:
	        	if( this.hudOpacity > 0.99)
	        		return var4 +" Opaque";
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.hudOpacity) });
	        case RENDER_OWN_HEADWEAR:
	            return this.renderHeadWear ? var4 + "ON" : var4 + "OFF";
            case RENDER_MENU_BACKGROUND:
                return this.menuBackground ? var4 + "ON" : var4 + "OFF";
	        case HUD_HIDE:
	            return this.mc.gameSettings.hideGUI ? var4 + "YES" : var4 + "NO";
	        case RENDER_FULL_FIRST_PERSON_MODEL_MODE:
                if (this.renderFullFirstPersonModelMode == RENDER_FIRST_PERSON_FULL)
                    return var4 + "Full";
                else if (this.renderFullFirstPersonModelMode == RENDER_FIRST_PERSON_HAND)
                    return var4 + "Hand";
                else if (this.renderFullFirstPersonModelMode == RENDER_FIRST_PERSON_NONE)
                    return var4 + "None";
	        case CHROM_AB_CORRECTION:
	            return this.useChromaticAbCorrection ? var4 + "ON" : var4 + "OFF";
            // 0.4.0
            case TIMEWARP:
                return this.useTimewarp ? var4 + "ON" : var4 + "OFF";
            case VIGNETTE:
                return this.useVignette ? var4 + "ON" : var4 + "OFF";
            case LOW_PERSISTENCE:
                return this.useLowPersistence ? var4 + "ON" : var4 + "OFF";
            case DYNAMIC_PREDICTION:
                return this.useDynamicPrediction ? var4 + "ON" : var4 + "OFF";
            case OVERDRIVE_DISPLAY:
                return this.useDisplayOverdrive ? var4 + "ON" : var4 + "OFF";
            case ENABLE_DIRECT:
                return this.useDirectRenderMode ? var4 + "Direct" : var4 + "Extended";
            case MIRROR_DISPLAY:
                return this.useDisplayMirroring ? var4 + "ON" : var4 + "OFF";
            case POS_TRACK_HIDE_COLLISION:
                return this.posTrackBlankOnCollision ? var4 + "YES" : var4 + "NO";
            case RENDER_SCALEFACTOR:
                return var4 + String.format("%.1f", new Object[] { Float.valueOf(this.renderScaleFactor) });

            case TEXTURE_LOOKUP_OPT:
                return this.useDistortionTextureLookupOptimisation ? var4 + "Texture Lookup" : var4 + "Brute Force";
            case FXAA:
                return this.useFXAA ? var4 + "ON" : var4 + "OFF";
	        case HUD_SCALE:
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.hudScale) });
	        case RENDER_PLAYER_OFFSET:
	            if (this.renderPlayerOffset < 0.01f)
	                return var4 + "None";
	            else
	                return var4 + String.format("%.2fcm", new Object[] { Float.valueOf(this.renderPlayerOffset) });
	        case PITCH_AFFECTS_CAMERA:
	            return this.allowMousePitchInput ? var4 + "ON" : var4 + "OFF";
            case HUD_LOCK_TO:
                return this.hudLockToHead ? var4 + "Head" : var4 + "Body";
	        case HUD_DISTANCE:
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.hudDistance) });
	        case HUD_PITCH:
	            return var4 + String.format("%.0f", new Object[] { Float.valueOf(this.hudPitchOffset) });
            case HUD_YAW:
                return var4 + String.format("%.0f", new Object[] { Float.valueOf(this.hudYawOffset) });
	        case FOV_SCALE_FACTOR:
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.fovScaleFactor) });
            case LENS_SEPARATION_SCALE_FACTOR:
                return var4 + String.format("%.3f", new Object[] { Float.valueOf(this.lensSeparationScaleFactor) });
	        case DISTORTION_FIT_POINT:
	            if (this.distortionFitPoint < 1)
	                return var4 + "None";
	            else if (this.distortionFitPoint > 13)
	                return var4 + "Large";
	            else if (this.distortionFitPoint == 5)
	                return var4 + "Normal";
	            else
	                return var4 + String.format("%.0f", new Object[] { Float.valueOf(this.distortionFitPoint) });
	        case CALIBRATION_STRATEGY:
	            if (this.calibrationStrategy < 1)
	                return var4 + "At Startup";
	//            else if (this.calibrationStrategy == 1) // TODO: Some sort of cached scheme - cache Hydra hemi-sphere & controller 'hand', Rift mag-cal, origin
	//                return var4 + "Cached";
	            else
	                return var4 + "Skip";
	        case HEAD_TRACK_SENSITIVITY:
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.getHeadTrackSensitivity()) });
	        case SUPERSAMPLING:
	            return this.useSupersample ? var4 + "ON" : var4 + "OFF";
	        case SUPERSAMPLE_SCALEFACTOR:
	            return var4 + String.format("%.1f", new Object[] { Float.valueOf(this.superSampleScaleFactor) });
	        case DECOUPLE_LOOK_MOVE:
	            return this.lookMoveDecoupled? var4 + "ON" : var4 + "OFF";
	        case JOYSTICK_SENSITIVITY:
	            return var4 + String.format("%.1f", new Object[] { Float.valueOf(this.joystickSensitivity) });
	        case JOYSTICK_DEADZONE:
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.joystickDeadzone) });
	        case JOYSTICK_AIM_TYPE:
	            return var4 + JOYSTICK_AIM_TYPE[joystickAimType];
	        case KEYHOLE_WIDTH:
	        	if(this.aimKeyholeWidthDegrees>0)
		            return var4 + String.format("%.0f°", new Object[] { Float.valueOf(this.aimKeyholeWidthDegrees) });
	        	else
	        		return var4 + "Fully Coupled";
	        case KEYHOLE_HEIGHT:
	        	if(this.keyholeHeight>0)
		            return var4 + String.format("%.0f°", new Object[] { Float.valueOf(this.keyholeHeight) });
	        	else
	        		return var4 + "Fully Coupled";
            case ASPECT_RATIO_CORRECTION:
                if (this.aspectRatioCorrection == IOculusRift.AspectCorrectionType.CORRECTION_16_10_TO_16_9.getValue())
                    return var4 + "16:10->16:9";
                else if (this.aspectRatioCorrection == IOculusRift.AspectCorrectionType.CORRECTION_16_9_TO_16_10.getValue())
                    return var4 + "16:9->16:10";
                else if (this.aspectRatioCorrection == IOculusRift.AspectCorrectionType.CORRECTION_AUTO.getValue())
                    return var4 + "Auto";
                else
                    return var4 + "None";
	        case POS_TRACK_HYDRALOC:
	            String s = var4 + "Unknown";
	
	            if (this.posTrackHydraLoc >= 0 && this.posTrackHydraLoc < POS_TRACK_HYDRA_LOC.length)
	                s = var4 + POS_TRACK_HYDRA_LOC[this.posTrackHydraLoc];
	
	            return s;
	        case POS_TRACK_HYDRA_OFFSET_X:
	            return var4 + String.format("%.0fmm", new Object[] { Float.valueOf(getPosTrackHydraOffsetX() * 1000) });
	        case POS_TRACK_HYDRA_OFFSET_Y:
	            return var4 + String.format("%.0fmm", new Object[] { Float.valueOf(getPosTrackHydraOffsetY() * 1000) });
	        case POS_TRACK_HYDRA_OFFSET_Z:
	            return var4 + String.format("%.0fmm", new Object[] { Float.valueOf(getPosTrackHydraOffsetZ() * 1000) });
	        case POS_TRACK_HYDRA_DISTANCE_SCALE:
	            return var4 + String.format("%.3f", new Object[] { Float.valueOf(this.posTrackHydraDistanceScale) });
	        case CROSSHAIR_SCALE:
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.crosshairScale) });
	        case POS_TRACK_Y_AXIS_DISTANCE_SKEW:
	            return var4 + String.format("%.1f", new Object[] { Float.valueOf(this.posTrackHydraYAxisDistanceSkewAngleDeg) });
	        case POS_TRACK_HYDRA_USE_CONTROLLER_ONE:
	            if (this.posTrackHydraLoc == VRSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT)
	                return var4 + "Both";
	
	            return this.posTrackHydraUseController1? var4 + "Left" : var4 + "Right";
	        case MOVEAIM_HYDRA_USE_CONTROLLER_ONE:
	            if (this.posTrackHydraLoc == VRSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT)
	                return var4 + "Neither!";
	
	            return this.posTrackHydraUseController1? var4 + "Right" : var4 + "Left";
	        case POS_TRACK_HYDRA_AT_BACKOFHEAD_IS_POINTING_LEFT:
	            return this.posTrackHydraBIsPointingLeft ? var4 + "To the Left" : var4 + "To the Right";
	        case OCULUS_PROFILE:
	            return this.useOculusProfile ? var4 + "YES" : var4 + "NO";
	        case OCULUS_PROFILE_NAME:
	            return var4 + this.oculusProfileName;
	        case OCULUS_PROFILE_GENDER:
	            return var4 + this.oculusProfileGender;
	        case HYDRA_USE_FILTER:
	            return this.hydraUseFilter ? var4 + "ON" : var4 + "OFF";
	        case RENDER_CROSSHAIR_MODE:
                if (this.renderInGameCrosshairMode == RENDER_CROSSHAIR_MODE_HUD)
                    return var4 + "With HUD";
                else if (this.renderInGameCrosshairMode == RENDER_CROSSHAIR_MODE_ALWAYS)
                    return var4 + "Always";
                else if (this.renderInGameCrosshairMode == RENDER_CROSSHAIR_MODE_NEVER)
                    return var4 + "Never";
	        case RENDER_BLOCK_OUTLINE_MODE:
                if (this.renderBlockOutlineMode == RENDER_BLOCK_OUTLINE_MODE_HUD)
                    return var4 + "With HUD";
                else if (this.renderBlockOutlineMode == RENDER_BLOCK_OUTLINE_MODE_ALWAYS)
                    return var4 + "Always";
                else if (this.renderBlockOutlineMode == RENDER_BLOCK_OUTLINE_MODE_NEVER)
                    return var4 + "Never";
	        case CROSSHAIR_ROLL:
	            return this.crosshairRollsWithHead ? var4 + "With Head" : var4 + "With HUD";
	        case HUD_OCCLUSION:
	            return this.hudOcclusion ? var4 + "ON" : var4 + "OFF";
	        case SOUND_ORIENT:
	            return this.soundOrientWithHead ? var4 + "Headphones" : var4 + "Speakers";
	        case KEYHOLE_HEAD_RELATIVE:
	            return this.keyholeHeadRelative? var4 + "YES" : var4 + "NO";
            case VR_RENDERER:
                if (this.mc.stereoProvider != null)
                    return this.mc.stereoProvider.getName();

                return "None";
	        case VR_HEAD_ORIENTATION:
	            if (this.mc.headTracker != null)
	                return this.mc.headTracker.getName();
	
	            return "None";
	        case VR_HEAD_POSITION:
	            if (this.mc.positionTracker != null)
	            {
	                String posTrackName = this.mc.positionTracker.getName();
	                if ( this.mc.positionTracker instanceof MCHydra )
	                {
	                    if (this.posTrackHydraUseController1)
	                    {
	                        return "Left " + posTrackName;
	                    }
	                    else
	                    {
	                        return "Right " + posTrackName;
	                    }
	                }
	
	                return posTrackName;
	            }
	            return "None";
	        case VR_CONTROLLER:
	            if (this.mc.lookaimController != null)
	            {
	                String controllerName = this.mc.lookaimController.getName();
	                if ( this.mc.lookaimController instanceof MCHydra )
	                {
	                    if (this.posTrackHydraUseController1)
	                    {
	                        return "Right " + controllerName;
	                    }
	                    else
	                    {
	                        return "Left " + controllerName;
	                    }
	                }
	                return controllerName;
	            }
	            return "None";
	        case CHAT_OFFSET_X:
	            return var4 + String.format("%.0f%%", new Object[] { Float.valueOf(100*this.chatOffsetX) });
	        case CHAT_OFFSET_Y:
	            return var4 + String.format("%.0f%%", new Object[] { Float.valueOf(100*this.chatOffsetY) });
	        case AIM_PITCH_OFFSET:
	            return var4 + String.format("%.0f°", new Object[] { Float.valueOf(this.aimPitchOffset) });
	        default:
	        	return "";
        }
    }

    public float getOptionFloatValue(VRSettings.VrOptions par1EnumOptions)
    {
    	switch( par1EnumOptions ) {
			case EYE_HEIGHT :
				return getPlayerEyeHeight() ;
			case EYE_PROTRUSION :
				return this.eyeProtrusion ;
			case NECK_LENGTH :
				return this.neckBaseToEyeHeight ;
			case MOVEMENT_MULTIPLIER :
				return this.movementSpeedMultiplier ;
			case IPD :
				return getIPD() ;
			case HEAD_TRACK_PREDICTION_TIME :
				return this.headTrackPredictionTimeSecs ;
			case JOYSTICK_SENSITIVITY :
				return this.joystickSensitivity;
			case JOYSTICK_DEADZONE :
				return this.joystickDeadzone;
			case KEYHOLE_WIDTH :
				return this.aimKeyholeWidthDegrees;
			case KEYHOLE_HEIGHT :
				return this.keyholeHeight;
			case HUD_SCALE :
				return this.hudScale ;
			case HUD_OPACITY :
				return this.hudOpacity ;
			case RENDER_PLAYER_OFFSET :
				return this.renderPlayerOffset ;
            case RENDER_SCALEFACTOR:
                return this.renderScaleFactor;
			case HUD_DISTANCE :
				return this.hudDistance ;
			case HUD_PITCH :
				return this.hudPitchOffset ;
            case HUD_YAW :
                return this.hudYawOffset ;
			case FOV_SCALE_FACTOR :
				return this.fovScaleFactor ;
            case LENS_SEPARATION_SCALE_FACTOR:
                return this.lensSeparationScaleFactor ;
			case HEAD_TRACK_SENSITIVITY :
				return this.getHeadTrackSensitivity() ;
			case SUPERSAMPLE_SCALEFACTOR :
				return this.superSampleScaleFactor ;
			case DISTORTION_FIT_POINT :
				return (float)this.distortionFitPoint ;
			case POS_TRACK_HYDRA_OFFSET_X:
			  	switch( this.posTrackHydraLoc )
			  	{
			  	case POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT :
			  		return this.posTrackHydraLROffsetX ;
			  	case POS_TRACK_HYDRA_LOC_HMD_LEFT :
			  		return this.posTrackHydraLOffsetX ;
			  	case POS_TRACK_HYDRA_LOC_HMD_RIGHT :
			  		return this.posTrackHydraROffsetX ;
			  	case POS_TRACK_HYDRA_LOC_HMD_TOP :
			  		return this.posTrackHydraTOffsetX ;
			  	case POS_TRACK_HYDRA_LOC_BACK_OF_HEAD: 
			  		if(this.posTrackHydraBIsPointingLeft)
			  			return this.posTrackHydraBLOffsetX;
			  		else 
			  			return this.posTrackHydraBROffsetX;
			  	 }
			case POS_TRACK_HYDRA_OFFSET_Y:
			  	switch( this.posTrackHydraLoc )
			  	{
			  	case POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT :
			  		return this.posTrackHydraLROffsetY ;
			  	case POS_TRACK_HYDRA_LOC_HMD_LEFT :
			  		return this.posTrackHydraLOffsetY ;
			  	case POS_TRACK_HYDRA_LOC_HMD_RIGHT :
			  		return this.posTrackHydraROffsetY ;
			  	case POS_TRACK_HYDRA_LOC_HMD_TOP :
			  		return this.posTrackHydraTOffsetY ;
			  	case POS_TRACK_HYDRA_LOC_BACK_OF_HEAD: 
			  		if(this.posTrackHydraBIsPointingLeft)
			  			return this.posTrackHydraBLOffsetY;
			  		else 
			  			return this.posTrackHydraBROffsetY;
			  	}
			case POS_TRACK_HYDRA_OFFSET_Z:
			  	 switch( this.posTrackHydraLoc )
			  	 {
			  		case POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT :
			  			return this.posTrackHydraLROffsetZ ;
			  		case POS_TRACK_HYDRA_LOC_HMD_LEFT :
			  			return this.posTrackHydraLOffsetZ ;
			  		case POS_TRACK_HYDRA_LOC_HMD_RIGHT :
			  			return this.posTrackHydraROffsetZ ;
			  		case POS_TRACK_HYDRA_LOC_HMD_TOP :
			  			return this.posTrackHydraTOffsetZ ;
			  		case POS_TRACK_HYDRA_LOC_BACK_OF_HEAD: 
			  			if(this.posTrackHydraBIsPointingLeft)
			  				return this.posTrackHydraBLOffsetZ;
			  			else 
			  				return this.posTrackHydraBROffsetZ;
			  	 }
			case POS_TRACK_HYDRA_DISTANCE_SCALE :
				return this.posTrackHydraDistanceScale ;
			case CROSSHAIR_SCALE :
				return this.crosshairScale ;
			case POS_TRACK_Y_AXIS_DISTANCE_SKEW :
				return this.posTrackHydraYAxisDistanceSkewAngleDeg;
			case CHAT_OFFSET_X:
				return this.chatOffsetX;
			case CHAT_OFFSET_Y:
				return this.chatOffsetY;
			case AIM_PITCH_OFFSET:
				return aimPitchOffset;
			default:
				return 0.0f;
    	}
    }
    /**
     * For non-float options. Toggles the option on/off, or cycles through the list i.e. render distances.
     */
    public void setOptionValue(VRSettings.VrOptions par1EnumOptions, int par2)
    {
    	switch( par1EnumOptions )
    	{
	        case USE_VR:
	            this.useVRRenderer = !this.useVRRenderer;
	            //mc.setUseVRRenderer(useVRRenderer);      // TODO:
	            break;
            case USE_QUATERNIONS:
                this.useQuaternions = !this.useQuaternions;
                break;
	        case USE_DISTORTION:
	            this.useDistortion = !this.useDistortion;
	            break;
            case LOAD_MUMBLE_LIB:
                this.loadMumbleLib = !this.loadMumbleLib;
                break;
	        case HEAD_TRACKING:
	            this.useHeadTracking = !this.useHeadTracking;
	            break;
            case DELAYED_RENDER:
                this.frameTimingEnableVsyncSleep = !this.frameTimingEnableVsyncSleep;
                break;
	        case RENDER_OWN_HEADWEAR:
	            this.renderHeadWear = !this.renderHeadWear;
	            break;
            case RENDER_MENU_BACKGROUND:
                this.menuBackground = !this.menuBackground;
                break;
	        case HUD_HIDE:
	            this.mc.gameSettings.hideGUI = !this.mc.gameSettings.hideGUI;
	            break;
	        case RENDER_FULL_FIRST_PERSON_MODEL_MODE:
                this.renderFullFirstPersonModelMode++;
                if (this.renderFullFirstPersonModelMode > RENDER_FIRST_PERSON_NONE)
                    this.renderFullFirstPersonModelMode = RENDER_FIRST_PERSON_FULL;
	            break;
	        case HEAD_TRACK_PREDICTION:
	            this.useHeadTrackPrediction = !this.useHeadTrackPrediction;
	            break;
	        case CHROM_AB_CORRECTION:
	            this.useChromaticAbCorrection = !this.useChromaticAbCorrection;
	            break;
            // 0.4.0
            case TIMEWARP:
                this.useTimewarp = !this.useTimewarp;
                break;
            case VIGNETTE:
                this.useVignette = !this.useVignette;
                break;
            case LOW_PERSISTENCE:
                this.useLowPersistence = !this.useLowPersistence;
                break;
            case DYNAMIC_PREDICTION:
                this.useDynamicPrediction = !this.useDynamicPrediction;
                break;
            case OVERDRIVE_DISPLAY:
                this.useDisplayOverdrive = !this.useDisplayOverdrive;
                break;
            case ENABLE_DIRECT:
                this.useDirectRenderMode = !this.useDirectRenderMode;
                break;
            case MIRROR_DISPLAY:
                this.useDisplayMirroring = !this.useDisplayMirroring;
                break;
            case POS_TRACK_HIDE_COLLISION:
                this.posTrackBlankOnCollision = !this.posTrackBlankOnCollision;
                break;
            case TEXTURE_LOOKUP_OPT:
                this.useDistortionTextureLookupOptimisation = !this.useDistortionTextureLookupOptimisation;
                break;
            case FXAA:
                this.useFXAA = !this.useFXAA;
                break;
	        case PITCH_AFFECTS_CAMERA:
	            this.allowMousePitchInput = !this.allowMousePitchInput;
	            break;
            case HUD_LOCK_TO:
                this.hudLockToHead = !this.hudLockToHead;
                break;
	        case SUPERSAMPLING:
	            this.useSupersample = !this.useSupersample;
	            break;
	        case DECOUPLE_LOOK_MOVE:
	            this.lookMoveDecoupled = !this.lookMoveDecoupled;
	            break;
	        case ASPECT_RATIO_CORRECTION:
	            this.aspectRatioCorrection += 1;
	            if (this.aspectRatioCorrection > IOculusRift.AspectCorrectionType.CORRECTION_AUTO.getValue())
	                this.aspectRatioCorrection = IOculusRift.AspectCorrectionType.CORRECTION_NONE.getValue();

                setAspectCorrectionMode(this.aspectRatioCorrection);
	            break;
            case POS_TRACK_HYDRALOC:
                this.posTrackHydraLoc += 1;
                if (this.posTrackHydraLoc > POS_TRACK_HYDRA_LOC_BACK_OF_HEAD)
                    this.posTrackHydraLoc = POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT;
                break;
	        case POS_TRACK_HYDRA_USE_CONTROLLER_ONE:
	            this.posTrackHydraUseController1 = !this.posTrackHydraUseController1;
	            break;
	        case MOVEAIM_HYDRA_USE_CONTROLLER_ONE:
	            this.posTrackHydraUseController1 = !this.posTrackHydraUseController1;
	            break;
	        case OCULUS_PROFILE:
	            this.useOculusProfile = !this.useOculusProfile;
	            break;
	        case POS_TRACK_HYDRA_AT_BACKOFHEAD_IS_POINTING_LEFT:
	            this.posTrackHydraBIsPointingLeft = !this.posTrackHydraBIsPointingLeft;
	            break;
	        case JOYSTICK_AIM_TYPE:
	        	this.joystickAimType ++;
	        	if( joystickAimType >= JOYSTICK_AIM_TYPE.length )
	        		joystickAimType = 0;
	        	break;
	        case HYDRA_USE_FILTER:
	            this.hydraUseFilter = !this.hydraUseFilter;
	            break;
	        case RENDER_CROSSHAIR_MODE:
	            this.renderInGameCrosshairMode++;
                if (this.renderInGameCrosshairMode > RENDER_CROSSHAIR_MODE_NEVER)
                    this.renderInGameCrosshairMode = RENDER_CROSSHAIR_MODE_ALWAYS;
	            break;
	        case RENDER_BLOCK_OUTLINE_MODE:
                this.renderBlockOutlineMode++;
                if (this.renderBlockOutlineMode > RENDER_BLOCK_OUTLINE_MODE_NEVER)
                    this.renderBlockOutlineMode = RENDER_BLOCK_OUTLINE_MODE_ALWAYS;
	            break;
	        case CROSSHAIR_ROLL:
	            this.crosshairRollsWithHead = !this.crosshairRollsWithHead;
	            break;
	        case HUD_OCCLUSION:
	            this.hudOcclusion = !this.hudOcclusion;
	            break;
	        case SOUND_ORIENT:
	            this.soundOrientWithHead = !this.soundOrientWithHead;
	            break;
	        case KEYHOLE_HEAD_RELATIVE:
	        	this.keyholeHeadRelative = !this.keyholeHeadRelative;
	            break;
	        case CALIBRATION_STRATEGY:
	            this.calibrationStrategy += 1;
	            if (this.calibrationStrategy > 1)
	                this.calibrationStrategy = 0;
	            break;
	        default:
	        	break;
    	}

        this.saveOptions();
    }

    public void setOptionFloatValue(VRSettings.VrOptions par1EnumOptions, float par2)
    {
    	switch( par1EnumOptions ) {
	        case EYE_HEIGHT:
	            this.playerEyeHeight = par2;
	            break;
	        case EYE_PROTRUSION:
	            this.eyeProtrusion = par2;
	            break;
	        case NECK_LENGTH:
	            this.neckBaseToEyeHeight = par2;
	            break;
	        case MOVEMENT_MULTIPLIER:
	            this.movementSpeedMultiplier = par2;
	            break;
	        case IPD:
	            this.ipd = par2;
	        	break;
	        case HEAD_TRACK_PREDICTION_TIME:
	            this.headTrackPredictionTimeSecs = par2;
	        	break;
	        case JOYSTICK_SENSITIVITY:
	            this.joystickSensitivity = par2;
	        	break;
	        case JOYSTICK_DEADZONE:
	            this.joystickDeadzone = par2;
	        	break;
	        case KEYHOLE_WIDTH:
	            this.aimKeyholeWidthDegrees = par2;
	        	break;
	        case KEYHOLE_HEIGHT:
	            this.keyholeHeight = par2;
	        	break;
	        case HUD_SCALE:
	            this.hudScale = par2;
	        	break;
	        case HUD_OPACITY:
	            this.hudOpacity = par2;
	        	break;
	        case RENDER_PLAYER_OFFSET:
	            this.renderPlayerOffset = par2;
	        	break;
            case RENDER_SCALEFACTOR:
                this.renderScaleFactor = par2;
                break;
	        case HUD_DISTANCE:
	            this.hudDistance = par2;
	        	break;
	        case HUD_PITCH:
	            this.hudPitchOffset = par2;
	        	break;
            case HUD_YAW:
                this.hudYawOffset = par2;
                break;
	        case FOV_SCALE_FACTOR:
	            this.fovScaleFactor = par2;
	        	break;
            case LENS_SEPARATION_SCALE_FACTOR:
                this.lensSeparationScaleFactor = par2;
                break;
	        case HEAD_TRACK_SENSITIVITY:
	            this.headTrackSensitivity = par2;
	        	break;
	        case SUPERSAMPLE_SCALEFACTOR:
	            this.superSampleScaleFactor = par2;
	        	break;
	        case DISTORTION_FIT_POINT:
	            this.distortionFitPoint = (int)Math.floor(par2);
	        	break;
	        case CALIBRATION_STRATEGY:
	            this.calibrationStrategy = (int)Math.floor(par2);
	        	break;
	        case POS_TRACK_HYDRA_OFFSET_X:
	            switch (this.posTrackHydraLoc)
	            {
	                case POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT:
	                    this.posTrackHydraLROffsetX = par2;
	                    break;
	                case POS_TRACK_HYDRA_LOC_HMD_LEFT:
	                    this.posTrackHydraLOffsetX = par2;
	                    break;
	                case POS_TRACK_HYDRA_LOC_HMD_RIGHT:
	                    this.posTrackHydraROffsetX = par2;
	                    break;
	                case POS_TRACK_HYDRA_LOC_HMD_TOP:
	                    this.posTrackHydraTOffsetX = par2;
	                    break;
	                case POS_TRACK_HYDRA_LOC_BACK_OF_HEAD:
	                    if (this.posTrackHydraBIsPointingLeft)
	                        this.posTrackHydraBLOffsetX = par2;
	                    else
	                        this.posTrackHydraBROffsetX = par2;
	                    break;
	            }
	        	break;
	        case POS_TRACK_HYDRA_OFFSET_Y:
	            switch (this.posTrackHydraLoc)
	            {
	                case POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT:
	                    this.posTrackHydraLROffsetY = par2;
	                    break;
	                case POS_TRACK_HYDRA_LOC_HMD_LEFT:
	                    this.posTrackHydraLOffsetY = par2;
	                    break;
	                case POS_TRACK_HYDRA_LOC_HMD_RIGHT:
	                    this.posTrackHydraROffsetY = par2;
	                    break;
	                case POS_TRACK_HYDRA_LOC_HMD_TOP:
	                    this.posTrackHydraTOffsetY = par2;
	                    break;
	                case POS_TRACK_HYDRA_LOC_BACK_OF_HEAD:
	                    if (this.posTrackHydraBIsPointingLeft)
	                        this.posTrackHydraBLOffsetY = par2;
	                    else
	                        this.posTrackHydraBROffsetY = par2;
	                    break;
	            }
                break;
	        case POS_TRACK_HYDRA_OFFSET_Z:
	            switch (this.posTrackHydraLoc)
	            {
	                case POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT:
	                    this.posTrackHydraLROffsetZ = par2;
	                    break;
	                case POS_TRACK_HYDRA_LOC_HMD_LEFT:
	                    this.posTrackHydraLOffsetZ = par2;
	                    break;
	                case POS_TRACK_HYDRA_LOC_HMD_RIGHT:
	                    this.posTrackHydraROffsetZ = par2;
	                    break;
	                case POS_TRACK_HYDRA_LOC_HMD_TOP:
	                    this.posTrackHydraTOffsetZ = par2;
	                    break;
	                case POS_TRACK_HYDRA_LOC_BACK_OF_HEAD:
	                    if (this.posTrackHydraBIsPointingLeft)
	                        this.posTrackHydraBLOffsetZ = par2;
	                    else
	                        this.posTrackHydraBROffsetZ = par2;
	                    break;
	            }
                break;
	        case POS_TRACK_HYDRA_DISTANCE_SCALE:
	            this.posTrackHydraDistanceScale = par2;
	        	break;
	        case POS_TRACK_Y_AXIS_DISTANCE_SKEW:
	            this.posTrackHydraYAxisDistanceSkewAngleDeg = par2;
	        	break;
	        case CROSSHAIR_SCALE:
	            this.crosshairScale = par2;
	        	break;
	        case CHAT_OFFSET_X:
	        	this.chatOffsetX = par2;
	        	break;
	        case CHAT_OFFSET_Y:
	        	this.chatOffsetY = par2;
	        	break;
	        case AIM_PITCH_OFFSET:
	        	this.aimPitchOffset = par2;
                break;
	        default:
	        	break;
    	}
	
        this.saveOptions();
    }



    public float getPosTrackHydraOffsetX()
    {
        float par2 = 0.0f;

        switch (this.posTrackHydraLoc)
        {
            case POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT:
                par2 = this.posTrackHydraLROffsetX;
                break;
            case POS_TRACK_HYDRA_LOC_HMD_LEFT:
                par2 = this.posTrackHydraLOffsetX;
                break;
            case POS_TRACK_HYDRA_LOC_HMD_RIGHT:
                par2 = this.posTrackHydraROffsetX;
                break;
            case POS_TRACK_HYDRA_LOC_HMD_TOP:
                par2 = this.posTrackHydraTOffsetX;
                break;
            case POS_TRACK_HYDRA_LOC_BACK_OF_HEAD:
                if (this.posTrackHydraBIsPointingLeft)
                    par2 = this.posTrackHydraBLOffsetX;
                else
                    par2 = this.posTrackHydraBROffsetX;
                break;
        }

        return par2;
    }

    public float getPosTrackHydraOffsetY()
    {
        float par2 = 0.0f;

        switch (this.posTrackHydraLoc)
        {
            case POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT:
                par2 = this.posTrackHydraLROffsetY;
                break;
            case POS_TRACK_HYDRA_LOC_HMD_LEFT:
                par2 = this.posTrackHydraLOffsetY;
                break;
            case POS_TRACK_HYDRA_LOC_HMD_RIGHT:
                par2 = this.posTrackHydraROffsetY;
                break;
            case POS_TRACK_HYDRA_LOC_HMD_TOP:
                par2 = this.posTrackHydraTOffsetY;
                break;
            case POS_TRACK_HYDRA_LOC_BACK_OF_HEAD:
                if (this.posTrackHydraBIsPointingLeft)
                    par2 = this.posTrackHydraBLOffsetY;
                else
                    par2 = this.posTrackHydraBROffsetY;
        }

        return par2;
    }

    public float getPosTrackHydraOffsetZ()
    {
        float par2 = 0.0f;

        switch (this.posTrackHydraLoc)
        {
            case POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT:
                par2 = this.posTrackHydraLROffsetZ;
                break;
            case POS_TRACK_HYDRA_LOC_HMD_LEFT:
                par2 = this.posTrackHydraLOffsetZ;
                break;
            case POS_TRACK_HYDRA_LOC_HMD_RIGHT:
                par2 = this.posTrackHydraROffsetZ;
                break;
            case POS_TRACK_HYDRA_LOC_HMD_TOP:
                par2 = this.posTrackHydraTOffsetZ;
                break;
            case POS_TRACK_HYDRA_LOC_BACK_OF_HEAD:
                if (this.posTrackHydraBIsPointingLeft)
                    par2 = this.posTrackHydraBLOffsetZ;
                else
                    par2 = this.posTrackHydraBROffsetZ;
        }

        return par2;
    }

    public void saveOptions()
    {
        try
        {
            saveOptions(new FileWriter(this.optionsVRFile));
        }
        catch (IOException e)
        {
            logger.warn("Failed to save VR options: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void storeDefaults()
    {
        StringWriter sw = new StringWriter();
        saveOptions(sw);
        this.defaults = sw.toString();
    }

    private void saveOptions(Writer writer)
    {
        // Save Minecrift settings
        try
        {
            PrintWriter var5 = new PrintWriter(writer);

            var5.println("version:" + version);
            var5.println("newlyCreated:" + false );
            var5.println("useVRRenderer:"+ this.useVRRenderer );
            var5.println("useQuaternions:"+ this.useQuaternions );
            var5.println("debugPose:"+ this.debugPose );
            var5.println("playerEyeHeight:" + this.playerEyeHeight);
            var5.println("eyeProtrusion:" + this.eyeProtrusion );
            var5.println("neckBaseToEyeHeight:" + this.neckBaseToEyeHeight );
            var5.println("headTrackerPluginID:"+ this.headTrackerPluginID);
            var5.println("headPositionPluginID:"+ this.headPositionPluginID);
            var5.println("hmdPluginID:"+ this.hmdPluginID);
            var5.println("stereoProviderPluginID:"+ this.stereoProviderPluginID);
            var5.println("controllerPluginID:"+ this.controllerPluginID);
            var5.println("ipd:" + this.ipd);
            var5.println("headTrackPredictionTimeSecs:" + this.headTrackPredictionTimeSecs);
            var5.println("hudOpacity:" + this.hudOpacity);
            var5.println("useHeadTracking:" + this.useHeadTracking);
            var5.println("useDistortion:" + this.useDistortion);
            var5.println("loadMumbleLib:" + this.loadMumbleLib);
            var5.println("useHeadTrackPrediction:" + this.useHeadTrackPrediction);
            var5.println("renderHeadWear:" + this.renderHeadWear);
            var5.println("menuBackground:" + this.menuBackground);
            var5.println("hideGUI:" + this.mc.gameSettings.hideGUI);
            var5.println("renderFullFirstPersonModelMode:" + this.renderFullFirstPersonModelMode);
            var5.println("useChromaticAbCorrection:" + this.useChromaticAbCorrection);
            // 0.4.0
            var5.println("useTimewarp:" + this.useTimewarp);
            var5.println("useVignette:" + this.useVignette);
            var5.println("useLowPersistence:" + this.useLowPersistence);
            var5.println("useDynamicPrediction:" + this.useDynamicPrediction);
            var5.println("useDisplayOverdrive:" + this.useDisplayOverdrive);
            var5.println("useDirectRenderMode:" + this.useDirectRenderMode);
            var5.println("useDisplayMirroring:" + this.useDisplayMirroring);
            var5.println("posTrackBlankOnCollision:" + this.posTrackBlankOnCollision);

            var5.println("useDistortionTextureLookupOptimisation:" + this.useDistortionTextureLookupOptimisation);
            var5.println("useFXAA:" + this.useFXAA);
            var5.println("hudScale:" + this.hudScale);
            var5.println("renderPlayerOffset:" + this.renderPlayerOffset);
            var5.println("renderScaleFactor:" + this.renderScaleFactor);
            var5.println("allowMousePitchInput:" + this.allowMousePitchInput);
            var5.println("hudLockToHead:" + this.hudLockToHead);
            var5.println("hudDistance:" + this.hudDistance);
            var5.println("hudPitchOffset:" + this.hudPitchOffset);
            var5.println("hudYawOffset:" + this.hudYawOffset);
            var5.println("useSupersample:" + this.useSupersample);
            var5.println("superSampleScaleFactor:" + this.superSampleScaleFactor);
            var5.println("fovScaleFactor:" + this.fovScaleFactor);
            var5.println("lensSeparationScaleFactor:" + this.lensSeparationScaleFactor);
            var5.println("distortionFitPoint:" + this.distortionFitPoint);
            var5.println("calibrationStrategy1:" + this.calibrationStrategy);    // Deliberately using a new value to get people using the 'At startup' setting again by default.
            var5.println("headTrackSensitivity:" + this.headTrackSensitivity);
            var5.println("movementSpeedMultiplier:" + this.movementSpeedMultiplier);
            var5.println("lookMoveDecoupled:" + this.lookMoveDecoupled);
            var5.println("aspectRatioCorrection:" + this.aspectRatioCorrection);
            var5.println("posTrackHydraLoc:" + this.posTrackHydraLoc);
            var5.println("posTrackHydraLROffsetX:" + this.posTrackHydraLROffsetX);
            var5.println("posTrackHydraLROffsetY:" + this.posTrackHydraLROffsetY);
            var5.println("posTrackHydraLROffsetZ:" + this.posTrackHydraLROffsetZ);
            var5.println("posTrackHydraLOffsetX:" + this.posTrackHydraLOffsetX);
            var5.println("posTrackHydraLOffsetY:" + this.posTrackHydraLOffsetY);
            var5.println("posTrackHydraLOffsetZ:" + this.posTrackHydraLOffsetZ);
            var5.println("posTrackHydraROffsetX:" + this.posTrackHydraROffsetX);
            var5.println("posTrackHydraROffsetY:" + this.posTrackHydraROffsetY);
            var5.println("posTrackHydraROffsetZ:" + this.posTrackHydraROffsetZ);
            var5.println("posTrackHydraTOffsetX:" + this.posTrackHydraTOffsetX);
            var5.println("posTrackHydraTOffsetY:" + this.posTrackHydraTOffsetY);
            var5.println("posTrackHydraTOffsetZ:" + this.posTrackHydraTOffsetZ);
            var5.println("posTrackHydraBLOffsetX:" + this.posTrackHydraBLOffsetX);
            var5.println("posTrackHydraBLOffsetY:" + this.posTrackHydraBLOffsetY);
            var5.println("posTrackHydraBLOffsetZ:" + this.posTrackHydraBLOffsetZ);
            var5.println("posTrackHydraBROffsetX:" + this.posTrackHydraBROffsetX);
            var5.println("posTrackHydraBRffsetY:" + this.posTrackHydraBROffsetY);
            var5.println("posTrackHydraBRffsetZ:" + this.posTrackHydraBROffsetZ);
            var5.println("posTrackHydraDistanceScale:" + this.posTrackHydraDistanceScale);
            var5.println("posTrackHydraUseController1:" + this.posTrackHydraUseController1);
            var5.println("posTrackHydraBIsPointingLeft:" + this.posTrackHydraBIsPointingLeft);
            var5.println("posTrackHydraYAxisDistanceSkewAngleDeg:" + this.posTrackHydraYAxisDistanceSkewAngleDeg);
            var5.println("hydraUseFilter:" + this.hydraUseFilter);
            var5.println("renderInGameCrosshairMode:" + this.renderInGameCrosshairMode);
            var5.println("renderBlockOutlineMode:" + this.renderBlockOutlineMode);
            var5.println("showEntityOutline:" + this.showEntityOutline);
            var5.println("crosshairRollsWithHead:" + this.crosshairRollsWithHead);
            var5.println("hudOcclusion:" + this.hudOcclusion);
            var5.println("soundOrientWithHead:" + this.soundOrientWithHead);
            var5.println("joystickSensitivity:" + this.joystickSensitivity);
            var5.println("joystickDeadzone:" + this.joystickDeadzone);
            var5.println("joystickAimType:" + this.joystickAimType);
            var5.println("keyholeWidth:" + this.aimKeyholeWidthDegrees);
            var5.println("keyholeHeight:" + this.keyholeHeight);
            var5.println("keyholeHeadRelative:" + this.keyholeHeadRelative);
            var5.println("useOculusProfile:" + this.useOculusProfile);
            var5.println("oculusProfileIpd:" + this.oculusProfileIpd);
            var5.println("oculusProfilePlayerEyeHeight:" + this.oculusProfilePlayerEyeHeight);
            var5.println("crosshairScale:" + this.crosshairScale);
            var5.println("chatOffsetX:" + this.chatOffsetX);
            var5.println("chatOffsetY:" + this.chatOffsetY);
            var5.println("aimPitchOffset:" + this.aimPitchOffset);
            var5.println("frameTimingEnableVsyncSleep:" + this.frameTimingEnableVsyncSleep);
            var5.println("frameTimingSleepSafetyBufferNanos:" + this.frameTimingSleepSafetyBufferNanos);
            var5.println("frameTimingSmoothOverFrameCount:" + this.frameTimingSmoothOverFrameCount);
            var5.println("frameTimingPredictDeltaFromEndFrameNanos:" + this.frameTimingPredictDeltaFromEndFrameNanos);

            var5.close();
        }
        catch (Exception var3)
        {
            logger.warn("Failed to save VR options: " + var3.getMessage());
            var3.printStackTrace();
        }
    }

    public void setMinecraftIpd(float ipd)
    {
        this.ipd = ipd;
    }

    public void setOculusProfileIpd(float ipd)
    {
        this.oculusProfileIpd = ipd;
    }

    public void setMinecraftPlayerEyeHeight(float eyeHeight)
    {
        this.playerEyeHeight = eyeHeight;
    }

    public void setOculusProfilePlayerEyeHeight(float eyeHeight)
    {
        this.oculusProfilePlayerEyeHeight = eyeHeight;
    }

    public float getIPD()
    {
        if (this.useOculusProfile)
            return this.oculusProfileIpd;

        return this.ipd;
    }

    public float getPlayerEyeHeight()
    {
        if (this.useOculusProfile)
            return this.oculusProfilePlayerEyeHeight;

        return this.playerEyeHeight;
    }

    /**
     * Parses a string into a float.
     */
    private float parseFloat(String par1Str)
    {
        return par1Str.equals("true") ? 1.0F : (par1Str.equals("false") ? 0.0F : Float.parseFloat(par1Str));
    }

    private void setAspectCorrectionMode(int mode)
    {
        switch(mode)
        {
            case 1:
                this.aspectRatioCorrectionMode = IOculusRift.AspectCorrectionType.CORRECTION_16_9_TO_16_10;
                break;
            case 2:
                this.aspectRatioCorrectionMode = IOculusRift.AspectCorrectionType.CORRECTION_16_10_TO_16_9;
                break;
            case 3:
                this.aspectRatioCorrectionMode = IOculusRift.AspectCorrectionType.CORRECTION_AUTO;
                break;
            default:
                this.aspectRatioCorrectionMode = IOculusRift.AspectCorrectionType.CORRECTION_NONE;
                break;
        }
    }

    public IOculusRift.AspectCorrectionType getAspectRatioCorrectionMode()
    {
        return this.aspectRatioCorrectionMode;
    }

    public void setAspectRatioCorrectionMode(IOculusRift.AspectCorrectionType mode)
    {
        this.aspectRatioCorrection = mode.getValue();
    }

    public void setHeadTrackSensitivity(float value)
    {
        this.headTrackSensitivity = value;
    }

    public float getHeadTrackSensitivity()
    {
        if (this.useQuaternions)
            return 1.0f;

        return this.headTrackSensitivity;
    }

    public static enum VrOptions
    {
        // Minecrift below here

        // TODO: Port to Mark's excellent VROption implementation

        //General
        USE_VR("VR mode", false, true),
        HUD_SCALE("HUD Size", true, false),
        HUD_DISTANCE("HUD Distance", true, false),
        HUD_PITCH("HUD Vertical Offset", true, false),
        HUD_YAW("HUD Horiz. Offset", true, false),
        HUD_LOCK_TO("HUD Orientation Lock", false, true),
        HUD_OPACITY("HUD Opacity", true, false),
        RENDER_MENU_BACKGROUND("Menu Background", false, true),
        HUD_HIDE("Hide HUD (F1)", false, true),
        HUD_OCCLUSION("HUD Occlusion", false, true),
        SOUND_ORIENT("Sound Source", false, true),
        HEAD_TRACKING("Head Tracking", false, true),
        DUMMY("Dummy", false, true),
        VR_RENDERER("Stereo Renderer", false, true),
        VR_HEAD_ORIENTATION("Head Orientation", false, true),
        VR_HEAD_POSITION("Head Position", false, true),
        VR_CONTROLLER("Controller", false, true),
        CROSSHAIR_SCALE("Crosshair Size", true, false),
        RENDER_CROSSHAIR_MODE("Show Crosshair", false, true),
        CROSSHAIR_ROLL("Roll Crosshair", false, true),
        RENDER_BLOCK_OUTLINE_MODE("Show Block Outline", false, true),
        CHAT_OFFSET_X("Chat Offset X",true,false),
        CHAT_OFFSET_Y("Chat Offset Y",true,false),
        LOAD_MUMBLE_LIB("Load Mumble Lib", false, true),

        // Player
        EYE_HEIGHT("Eye Height", true, false),
        EYE_PROTRUSION("Eye Protrusion", true, false),
        NECK_LENGTH("Neck Length", true, false),
        RENDER_OWN_HEADWEAR("Render Own Headwear", false, true),
        RENDER_FULL_FIRST_PERSON_MODEL_MODE("First Person Model", false, true),
        RENDER_PLAYER_OFFSET("View Body Offset", true, false),
        IPD("IPD", true, false),
        OCULUS_PROFILE("Use Oculus Profile", false, false),
        OCULUS_PROFILE_NAME("Name", false, true),
        OCULUS_PROFILE_GENDER("Gender", false, true),

        //HMD/render
        USE_DISTORTION("Distortion", false, true),
        CHROM_AB_CORRECTION("Chrom. Ab. Correction", false, true),
        TIMEWARP("Timewarp", false, true),
        VIGNETTE("Vignette", false, true),
        TEXTURE_LOOKUP_OPT("Dist. Method", false, true),
        FXAA("FXAA", false, true),
        FOV_SCALE_FACTOR("FOV Scale", true, false),
        LENS_SEPARATION_SCALE_FACTOR("Lens Sep. Scale", true, false),
        DISTORTION_FIT_POINT("Distortion Border", true, false),
        ASPECT_RATIO_CORRECTION("Asp. Correction", false, false),
        SUPERSAMPLING("FSAA", false, true),
        SUPERSAMPLE_SCALEFACTOR("FSAA Render Scale", true, false),
        USE_QUATERNIONS("Orient. Mode", false, true),
        DELAYED_RENDER("Render Mode", false, true),
        // SDK 0.4.0 up
        RENDER_SCALEFACTOR("Render Scale", true, false),
        ENABLE_DIRECT("Render Mode", false, true),
        MIRROR_DISPLAY("Mirror Display", false, true),
        LOW_PERSISTENCE("Low Persistence", false, true),
        DYNAMIC_PREDICTION("Dynamic Prediction", false, true),
        OVERDRIVE_DISPLAY("Overdrive Display", false, true),
        HMD_NAME_PLACEHOLDER("", false, true),

        //Head orientation tracking
        HEAD_TRACK_PREDICTION("Head Track Prediction", false, true),
        HEAD_TRACK_SENSITIVITY("Head Track Sensitivity", true, false),
        HEAD_TRACK_PREDICTION_TIME("Prediction time", true, false),

        //eye center position tracking
        POS_TRACK_HYDRALOC("Position", false, false),
        POS_TRACK_HYDRA_OFFSET_X("Hydra X Offset", true, false),
        POS_TRACK_HYDRA_OFFSET_Y("Hydra Y Offset", true, false),
        POS_TRACK_HYDRA_OFFSET_Z("Hydra Z Offset", true, false),
        POS_TRACK_OFFSET_SET_DEFAULT("Default Offsets", false, true),
        POS_TRACK_HYDRA_DISTANCE_SCALE("Dist. Scale", true, false),
        POS_TRACK_HYDRA_USE_CONTROLLER_ONE("Controller", false, true),
        POS_TRACK_HYDRA_AT_BACKOFHEAD_IS_POINTING_LEFT("Hydra Direction", false, true),
        HYDRA_USE_FILTER("Filter", false, true),
        POS_TRACK_Y_AXIS_DISTANCE_SKEW("Distance Skew Angle", true, false),
        // SDK 0.4.0 up
        POS_TRACK_HIDE_COLLISION("Blank on collision", false, true),

        //Movement/aiming controls
        DECOUPLE_LOOK_MOVE("Decouple Look/Move", false, true),
        MOVEMENT_MULTIPLIER("Move. Speed Multiplier", true, false),
        PITCH_AFFECTS_CAMERA("Pitch Affects Camera", false, true),
        JOYSTICK_SENSITIVITY("Joystick Sensitivity",true,false),
        JOYSTICK_DEADZONE("Joystick Deadzone",true,false),
        KEYHOLE_WIDTH("Keyhole Width",true,false),
        KEYHOLE_HEIGHT("Keyhole Height",true,false),
        KEYHOLE_HEAD_RELATIVE("Keyhole Moves With Head",false,true),
        MOVEAIM_HYDRA_USE_CONTROLLER_ONE("Controller", false, true),
        JOYSTICK_AIM_TYPE("Aim Type", false, false),
        AIM_PITCH_OFFSET("Vertical Crosshair Offset",true,false),

        // Calibration
        CALIBRATION_STRATEGY("Initial Calibration", false, false);

//        ANISOTROPIC_FILTERING("options.anisotropicFiltering", true, false, 1.0F, 16.0F, 0.0F)
//                {
//                    private static final String __OBFID = "CL_00000654";
//                    protected float snapToStep(float p_148264_1_)
//                    {
//                        return (float) MathHelper.roundUpToPowerOfTwo((int) p_148264_1_);
//                    }
//                },

        private final boolean enumFloat;
        private final boolean enumBoolean;
        private final String enumString;
        private final float valueStep;
        private float valueMin;
        private float valueMax;

        private static final String __OBFID = "CL_00000653";

        public static VRSettings.VrOptions getEnumOptions(int par0)
        {
            VRSettings.VrOptions[] aoptions = values();
            int j = aoptions.length;

            for (int k = 0; k < j; ++k)
            {
                VRSettings.VrOptions options = aoptions[k];

                if (options.returnEnumOrdinal() == par0)
                {
                    return options;
                }
            }

            return null;
        }

        private VrOptions(String par3Str, boolean par4, boolean par5)
        {
            this(par3Str, par4, par5, 0.0F, 1.0F, 0.0F);
        }

        private VrOptions(String p_i45004_3_, boolean p_i45004_4_, boolean p_i45004_5_, float p_i45004_6_, float p_i45004_7_, float p_i45004_8_)
        {
            this.enumString = p_i45004_3_;
            this.enumFloat = p_i45004_4_;
            this.enumBoolean = p_i45004_5_;
            this.valueMin = p_i45004_6_;
            this.valueMax = p_i45004_7_;
            this.valueStep = p_i45004_8_;
        }

        public boolean getEnumFloat()
        {
            return this.enumFloat;
        }

        public boolean getEnumBoolean()
        {
            return this.enumBoolean;
        }

        public int returnEnumOrdinal()
        {
            return this.ordinal();
        }

        public String getEnumString()
        {
            return this.enumString;
        }

        public float getValueMax()
        {
            return this.valueMax;
        }

        public void setValueMax(float p_148263_1_)
        {
            this.valueMax = p_148263_1_;
        }

        public float normalizeValue(float p_148266_1_)
        {
            return MathHelper.clamp_float((this.snapToStepClamp(p_148266_1_) - this.valueMin) / (this.valueMax - this.valueMin), 0.0F, 1.0F);
        }

        public float denormalizeValue(float p_148262_1_)
        {
            return this.snapToStepClamp(this.valueMin + (this.valueMax - this.valueMin) * MathHelper.clamp_float(p_148262_1_, 0.0F, 1.0F));
        }

        public float snapToStepClamp(float p_148268_1_)
        {
            p_148268_1_ = this.snapToStep(p_148268_1_);
            return MathHelper.clamp_float(p_148268_1_, this.valueMin, this.valueMax);
        }

        protected float snapToStep(float p_148264_1_)
        {
            if (this.valueStep > 0.0F)
            {
                p_148264_1_ = this.valueStep * (float)Math.round(p_148264_1_ / this.valueStep);
            }

            return p_148264_1_;
        }

        VrOptions(String p_i45005_3_, boolean p_i45005_4_, boolean p_i45005_5_, float p_i45005_6_, float p_i45005_7_, float p_i45005_8_, Object p_i45005_9_)
        {
            this(p_i45005_3_, p_i45005_4_, p_i45005_5_, p_i45005_6_, p_i45005_7_, p_i45005_8_);
        }
    }
}

/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import com.mtbs3d.minecrift.MCHydra;

import net.minecraft.src.Config;
import net.minecraft.src.EnumOptions;
import net.minecraft.src.I18n;
import net.minecraft.src.Minecraft;

public class VRSettings {
	public static VRSettings inst;
    // Minecrift
    public static final int POS_TRACK_NECK = 0;
    public static final int POS_TRACK_HYDRA = 1;

    private static final String[] POS_TRACK_HYDRA_LOC = new String[] {"HMD (L&R sides)", "HMD (Left side)", "HMD (Top)", "HMD (Right side)", "Back Of Head", "Direct"};
    private static final String[] JOYSTICK_AIM_TYPE = new String[] {"Cursor", "Recenter" };
    //TODO: Shouldn't these be an enum? 
    public static final int POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT = 0;
    public static final int POS_TRACK_HYDRA_LOC_HMD_LEFT = 1;
    public static final int POS_TRACK_HYDRA_LOC_HMD_TOP = 2;
    public static final int POS_TRACK_HYDRA_LOC_HMD_RIGHT = 3;
    public static final int POS_TRACK_HYDRA_LOC_BACK_OF_HEAD = 4;
    //public static final int POS_TRACK_HYDRA_LOC_DIRECT = 5;

    public static final int CALIBRATION_STRATEGY_AT_STARTUP = 0;

    public boolean useVRRenderer    = false; //default to false
	protected float playerEyeHeight = 1.74f;  // Use getPlayerEyeHeight()
	public float eyeProtrusion = 0.185f;
	public float neckBaseToEyeHeight = 0.225f;
    public float movementSpeedMultiplier = 1.0f;
    public boolean useDistortion = true;
    public boolean useHeadTracking = true;
    public boolean useHeadTrackPrediction = true;
    public float headTrackPredictionTimeSecs = 0.015f;
    protected float ipd = 0.0635F;   // Use getIPD()
    protected float oculusProfileIpd = ipd;
    public String oculusProfileName;
    public String oculusProfileGender;
    protected float oculusProfilePlayerEyeHeight = playerEyeHeight;
    public float hudOpacity = 1.0f;
    public boolean renderHeadWear = false;
    public boolean renderFullFirstPersonModel = true;
    public float renderPlayerOffset = 0.25f;
    public boolean useChromaticAbCorrection = true;
    public float hudScale = 0.65f;
    public boolean allowMousePitchInput = false;
    public float hudDistance = 1.25f;
    public float hudPitchOffset = 0.0f;
    public float fovScaleFactor = 1.0f;
    public int distortionFitPoint = 5;
    public float headTrackSensitivity = 1.0f;
    public boolean useSupersample = false;
    public float superSampleScaleFactor = 2.0f;
    public boolean useMipMaps = false;
    public boolean lookMoveDecoupled = false;
    public boolean useOculusProfile = false;
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
	public String headPositionPluginID = "null-pos";
	public String headTrackerPluginID = "oculus";
	public String hmdPluginID = "oculus";
	public String controllerPluginID = "mouse";
    public int calibrationStrategy = CALIBRATION_STRATEGY_AT_STARTUP;
    public float crosshairScale = 1.0f;
    public boolean alwaysRenderInGameCrosshair = false;
    public boolean alwaysRenderBlockOutline = false;
    public boolean crosshairRollsWithHead = true;
    public boolean hudOcclusion = false;
    
    private Minecraft mc;

    private File optionsVRFile;
    
    public VRSettings( Minecraft minecraft, File dataDir )
    {
    	mc = minecraft;
    	inst = this;
        this.optionsVRFile = new File(dataDir, "optionsvr.txt");
        this.loadOptions();
    }
    
    public void loadOptions()
    {

        // Load Minecrift options
        try
        {
            if (!this.optionsVRFile.exists())
            {
                return;
            }

            BufferedReader optionsVRReader = new BufferedReader(new FileReader(this.optionsVRFile));

            String var2 = "";

            while ((var2 = optionsVRReader.readLine()) != null)
            {
                try
                {
                    String[] optionTokens = var2.split(":");

                    if (optionTokens[0].equals("useVRRenderer"))
                    {
                        this.useVRRenderer = optionTokens[1].equals("true");
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

                    if (optionTokens[0].equals("renderHeadWear"))
                    {
                        this.renderHeadWear = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("vrHideGUI"))
                    {
                        this.mc.gameSettings.hideGUI = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("renderFullFirstPersonModel"))
                    {
                        this.renderFullFirstPersonModel = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useChromaticAbCorrection"))
                    {
                        this.useChromaticAbCorrection = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("hudScale"))
                    {
                        this.hudScale = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("renderPlayerOffset"))
                    {
                        this.renderPlayerOffset = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("allowMousePitchInput"))
                    {
                        this.allowMousePitchInput = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("hudDistance"))
                    {
                        this.hudDistance = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("hudPitchOffset"))
                    {
                        this.hudPitchOffset = this.parseFloat(optionTokens[1]);
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

                    if (optionTokens[0].equals("distortionFitPoint"))
                    {
                        this.distortionFitPoint = Integer.parseInt(optionTokens[1]);
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

                    if (optionTokens[0].equals("alwaysRenderInGameCrosshair"))
                    {
                        this.alwaysRenderInGameCrosshair = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("alwaysRenderBlockOutline"))
                    {
                        this.alwaysRenderBlockOutline = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("crosshairRollsWithHead"))
                    {
                        this.crosshairRollsWithHead = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("hudOcclusion"))
                    {
                        this.hudOcclusion = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("joystickSensitivity"))
                    {
                        this.joystickSensitivity = this.parseFloat(optionTokens[1]);
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
                }
                catch (Exception var7)
                {
                    this.mc.getLogAgent().logWarning("Skipping bad VR option: " + var2);
                    var7.printStackTrace();
                }
            }

            optionsVRReader.close();
        }
        catch (Exception var8)
        {
            this.mc.getLogAgent().logWarning("Failed to load VR options!");
            var8.printStackTrace();
        }
    }
    
    public String getKeyBinding( EnumOptions par1EnumOptions )
    {
        String var2 = I18n.func_135053_a(par1EnumOptions.getEnumString());

        if (var2 == null)
        {
            var2 = par1EnumOptions.getEnumString();
        }

        String var3 = var2 + ": ";
        String var4 = var3;
        String var5;

        if (par1EnumOptions == EnumOptions.USE_VR)
        {
            return this.useVRRenderer ? var4 + "ON" : var4 + "OFF";
        }
        else if (par1EnumOptions == EnumOptions.EYE_HEIGHT)
        {
            return var4 + String.format("%.2fm", new Object[] { Float.valueOf(getPlayerEyeHeight()) });
        }
        else if (par1EnumOptions == EnumOptions.EYE_PROTRUSION)
        {
            return var4 + String.format("%.3fm", new Object[] { Float.valueOf(this.eyeProtrusion) });
        }
        else if (par1EnumOptions == EnumOptions.NECK_LENGTH)
        {
            return var4 + String.format("%.3fm", new Object[] { Float.valueOf(this.neckBaseToEyeHeight) });
        }
        else if (par1EnumOptions == EnumOptions.MOVEMENT_MULTIPLIER)
        {
            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.movementSpeedMultiplier) });
        }
        else if (par1EnumOptions == EnumOptions.USE_DISTORTION)
        {
            return this.useDistortion ? var4 + "ON" : var4 + "OFF";
        }
        else if (par1EnumOptions == EnumOptions.HEAD_TRACKING)
        {
            return this.useHeadTracking ? var4 + "ON" : var4 + "OFF";
        }
        else if (par1EnumOptions == EnumOptions.HEAD_TRACK_PREDICTION)
        {
            return this.useHeadTrackPrediction ? var4 + "ON" : var4 + "OFF";
        }
        else if (par1EnumOptions == EnumOptions.IPD)
        {
            return var4 + String.format("%.1fmm", new Object[] { Float.valueOf(getIPD() * 1000) });
        }
        else if (par1EnumOptions == EnumOptions.HEAD_TRACK_PREDICTION_TIME)
        {
            return var4 + String.format("%.0fms", new Object[] { Float.valueOf(this.headTrackPredictionTimeSecs * 1000) });
        }
        else if (par1EnumOptions == EnumOptions.HUD_OPACITY)
        {
        	if( this.hudOpacity > 0.99)
        		return var4 +" Opaque";
            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.hudOpacity) });
        }
        else if (par1EnumOptions == EnumOptions.RENDER_OWN_HEADWEAR)
        {
            return this.renderHeadWear ? var4 + "ON" : var4 + "OFF";
        }
        else if (par1EnumOptions == EnumOptions.HUD_HIDE)
        {
            return this.mc.gameSettings.hideGUI ? var4 + "YES" : var4 + "NO";
        }
        else if (par1EnumOptions == EnumOptions.RENDER_FULL_FIRST_PERSON_MODEL)
        {
            return this.renderFullFirstPersonModel ? var4 + "Full" : var4 + "Hand";
        }
        else if (par1EnumOptions == EnumOptions.CHROM_AB_CORRECTION)
        {
            return this.useChromaticAbCorrection ? var4 + "ON" : var4 + "OFF";
        }
        else if (par1EnumOptions == EnumOptions.HUD_SCALE)
        {
            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.hudScale) });
        }
        else if (par1EnumOptions == EnumOptions.RENDER_PLAYER_OFFSET)
        {
            if (this.renderPlayerOffset < 0.01f)
                return var4 + "None";
            else
                return var4 + String.format("%.2fcm", new Object[] { Float.valueOf(this.renderPlayerOffset) });
        }
        else if (par1EnumOptions == EnumOptions.PITCH_AFFECTS_CAMERA)
        {
            return this.allowMousePitchInput ? var4 + "ON" : var4 + "OFF";
        }
        else if (par1EnumOptions == EnumOptions.HUD_DISTANCE)
        {
            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.hudDistance) });
        }
        else if (par1EnumOptions == EnumOptions.HUD_PITCH)
        {
            return var4 + String.format("%.0f", new Object[] { Float.valueOf(this.hudPitchOffset) });
        }
        else if (par1EnumOptions == EnumOptions.FOV_SCALE_FACTOR)
        {
            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.fovScaleFactor) });
        }
        else if (par1EnumOptions == EnumOptions.DISTORTION_FIT_POINT)
        {
            if (this.distortionFitPoint < 1)
                return var4 + "None";
            else if (this.distortionFitPoint > 13)
                return var4 + "Large";
            else if (this.distortionFitPoint == 5)
                return var4 + "Normal";
            else
                return var4 + String.format("%.0f", new Object[] { Float.valueOf(this.distortionFitPoint) });
        }
        else if (par1EnumOptions == EnumOptions.CALIBRATION_STRATEGY)
        {
            if (this.calibrationStrategy < 1)
                return var4 + "At Startup";
//            else if (this.calibrationStrategy == 1) // TODO: Some sort of cached scheme - cache Hydra hemi-sphere & controller 'hand', Rift mag-cal, origin
//                return var4 + "Cached";
            else
                return var4 + "Skip";
        }
        else if (par1EnumOptions == EnumOptions.HEAD_TRACK_SENSITIVITY)
        {
            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.headTrackSensitivity) });
        }
        else if (par1EnumOptions == EnumOptions.SUPERSAMPLING)
        {
            return this.useSupersample ? var4 + "ON" : var4 + "OFF";
        }
        else if (par1EnumOptions == EnumOptions.SUPERSAMPLE_SCALEFACTOR)
        {
            return var4 + String.format("%.1f", new Object[] { Float.valueOf(this.superSampleScaleFactor) });
        }
        else if (par1EnumOptions == EnumOptions.DECOUPLE_LOOK_MOVE)
        {
            return this.lookMoveDecoupled? var4 + "ON" : var4 + "OFF";
        }
        else if (par1EnumOptions == EnumOptions.JOYSTICK_SENSITIVITY)
        {
            return var4 + String.format("%.1f", new Object[] { Float.valueOf(this.joystickSensitivity) });
        }
        else if (par1EnumOptions == EnumOptions.JOYSTICK_AIM_TYPE)
        {
            return var4 + JOYSTICK_AIM_TYPE[joystickAimType];
        }
        else if (par1EnumOptions == EnumOptions.KEYHOLE_WIDTH)
        {
        	if(this.aimKeyholeWidthDegrees>0)
	            return var4 + String.format("%.0f°", new Object[] { Float.valueOf(this.aimKeyholeWidthDegrees) });
        	else
        		return var4 + "Fully Coupled";
        }
        else if (par1EnumOptions == EnumOptions.KEYHOLE_HEIGHT)
        {
        	if(this.keyholeHeight>0)
	            return var4 + String.format("%.0f°", new Object[] { Float.valueOf(this.keyholeHeight) });
        	else
        		return var4 + "Fully Coupled";
        }
        else if (par1EnumOptions == EnumOptions.POS_TRACK_HYDRALOC)
        {
            String s = var4 + "Unknown";

            if (this.posTrackHydraLoc >= 0 && this.posTrackHydraLoc < POS_TRACK_HYDRA_LOC.length)
                s = var4 + POS_TRACK_HYDRA_LOC[this.posTrackHydraLoc];

            return s;
        }
        else if (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_X)
        {
            float value = 0.0f;

            switch (this.posTrackHydraLoc)
            {
                case POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT:
                    value = this.posTrackHydraLROffsetX;
                    break;
                case POS_TRACK_HYDRA_LOC_HMD_LEFT:
                    value = this.posTrackHydraLOffsetX;
                    break;
                case POS_TRACK_HYDRA_LOC_HMD_RIGHT:
                    value = this.posTrackHydraROffsetX;
                    break;
                case POS_TRACK_HYDRA_LOC_HMD_TOP:
                    value = this.posTrackHydraTOffsetX;
                    break;
                case POS_TRACK_HYDRA_LOC_BACK_OF_HEAD:
                    if (this.posTrackHydraBIsPointingLeft)
                        value = this.posTrackHydraBLOffsetX;
                    else
                        value = this.posTrackHydraBROffsetX;
                    break;
            }

            return var4 + String.format("%.0fmm", new Object[] { Float.valueOf(value * 1000) });
        }
        else if (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Y)
        {
            float value = 0.0f;

            switch (this.posTrackHydraLoc)
            {
                case POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT:
                    value = this.posTrackHydraLROffsetY;
                    break;
                case POS_TRACK_HYDRA_LOC_HMD_LEFT:
                    value = this.posTrackHydraLOffsetY;
                    break;
                case POS_TRACK_HYDRA_LOC_HMD_RIGHT:
                    value = this.posTrackHydraROffsetY;
                    break;
                case POS_TRACK_HYDRA_LOC_HMD_TOP:
                    value = this.posTrackHydraTOffsetY;
                    break;
                case POS_TRACK_HYDRA_LOC_BACK_OF_HEAD:
                    if (this.posTrackHydraBIsPointingLeft)
                        value = this.posTrackHydraBLOffsetY;
                    else
                        value = this.posTrackHydraBROffsetY;
                    break;
            }

            return var4 + String.format("%.0fmm", new Object[] { Float.valueOf(value * 1000) });
        }
        else if (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Z)
        {
            float value = 0.0f;

            switch (this.posTrackHydraLoc)
            {
                case POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT:
                    value = this.posTrackHydraLROffsetZ;
                    break;
                case POS_TRACK_HYDRA_LOC_HMD_LEFT:
                    value = this.posTrackHydraLOffsetZ;
                    break;
                case POS_TRACK_HYDRA_LOC_HMD_RIGHT:
                    value = this.posTrackHydraROffsetZ;
                    break;
                case POS_TRACK_HYDRA_LOC_HMD_TOP:
                    value = this.posTrackHydraTOffsetZ;
                    break;
                case POS_TRACK_HYDRA_LOC_BACK_OF_HEAD:
                    if (this.posTrackHydraBIsPointingLeft)
                        value = this.posTrackHydraBLOffsetZ;
                    else
                        value = this.posTrackHydraBROffsetZ;
                    break;
            }

            return var4 + String.format("%.0fmm", new Object[] { Float.valueOf(value * 1000) });
        }
        else if (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_DISTANCE_SCALE)
        {
            return var4 + String.format("%.3f", new Object[] { Float.valueOf(this.posTrackHydraDistanceScale) });
        }
        else if (par1EnumOptions == EnumOptions.CROSSHAIR_SCALE)
        {
            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.crosshairScale) });
        }
        else if (par1EnumOptions == EnumOptions.POS_TRACK_Y_AXIS_DISTANCE_SKEW)
        {
            return var4 + String.format("%.1f", new Object[] { Float.valueOf(this.posTrackHydraYAxisDistanceSkewAngleDeg) });
        }
        else if (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_USE_CONTROLLER_ONE)
        {
            if (this.posTrackHydraLoc == VRSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT)
                return var4 + "Both";

            return this.posTrackHydraUseController1? var4 + "Left" : var4 + "Right";
        }
        else if (par1EnumOptions == EnumOptions.MOVEAIM_HYDRA_USE_CONTROLLER_ONE)
        {
            if (this.posTrackHydraLoc == VRSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT)
                return var4 + "Neither!";

            return this.posTrackHydraUseController1? var4 + "Right" : var4 + "Left";
        }
        else if (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_AT_BACKOFHEAD_IS_POINTING_LEFT)
        {
            return this.posTrackHydraBIsPointingLeft ? var4 + "To the Left" : var4 + "To the Right";
        }
        else if (par1EnumOptions == EnumOptions.OCULUS_PROFILE)
        {
            return this.useOculusProfile ? var4 + "YES" : var4 + "NO";
        }
        else if (par1EnumOptions == EnumOptions.OCULUS_PROFILE_NAME)
        {
            return var4 + this.oculusProfileName;
        }
        else if (par1EnumOptions == EnumOptions.OCULUS_PROFILE_GENDER)
        {
            return var4 + this.oculusProfileGender;
        }
        else if (par1EnumOptions == EnumOptions.HYDRA_USE_FILTER)
        {
            return this.hydraUseFilter ? var4 + "ON" : var4 + "OFF";
        }
        else if (par1EnumOptions == EnumOptions.CROSSHAIR_ALWAYS_SHOW)
        {
            return this.alwaysRenderInGameCrosshair ? var4 + "Always" : var4 + "With HUD";
        }
        else if (par1EnumOptions == EnumOptions.BLOCK_OUTLINE_ALWAYS_SHOW)
        {
            return this.alwaysRenderBlockOutline ? var4 + "Always" : var4 + "With HUD";
        }
        else if (par1EnumOptions == EnumOptions.CROSSHAIR_ROLL)
        {
            return this.crosshairRollsWithHead ? var4 + "With Head" : var4 + "With HUD";
        }
        else if (par1EnumOptions == EnumOptions.HUD_OCCLUSION)
        {
            return this.hudOcclusion ? var4 + "ON" : var4 + "OFF";
        }
        else if (par1EnumOptions == EnumOptions.KEYHOLE_HEAD_RELATIVE)
        {
            return this.keyholeHeadRelative? var4 + "YES" : var4 + "NO";
        }
        else if (par1EnumOptions == EnumOptions.VR_HEAD_ORIENTATION)
        {
            if (this.mc.headTracker != null)
                return this.mc.headTracker.getName();

            return "None";
        }
        else if (par1EnumOptions == EnumOptions.VR_HEAD_POSITION)
        {
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
        }
        else if (par1EnumOptions == EnumOptions.VR_CONTROLLER)
        {
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
        }
		return "";
    }

    public float getOptionFloatValue(EnumOptions par1EnumOptions)
    {
        return par1EnumOptions == EnumOptions.EYE_HEIGHT ? getPlayerEyeHeight() :
              (par1EnumOptions == EnumOptions.EYE_PROTRUSION ? this.eyeProtrusion :
              (par1EnumOptions == EnumOptions.NECK_LENGTH ? this.neckBaseToEyeHeight :
              (par1EnumOptions == EnumOptions.MOVEMENT_MULTIPLIER ? this.movementSpeedMultiplier :
              (par1EnumOptions == EnumOptions.IPD ? getIPD() :
              (par1EnumOptions == EnumOptions.HEAD_TRACK_PREDICTION_TIME) ? this.headTrackPredictionTimeSecs :
              (par1EnumOptions == EnumOptions.JOYSTICK_SENSITIVITY) ? this.joystickSensitivity:
              (par1EnumOptions == EnumOptions.KEYHOLE_WIDTH) ? this.aimKeyholeWidthDegrees:
              (par1EnumOptions == EnumOptions.KEYHOLE_HEIGHT) ? this.keyholeHeight:
              (par1EnumOptions == EnumOptions.HUD_SCALE ? this.hudScale :
              (par1EnumOptions == EnumOptions.HUD_OPACITY ? this.hudOpacity :
              (par1EnumOptions == EnumOptions.RENDER_PLAYER_OFFSET ? this.renderPlayerOffset :
              (par1EnumOptions == EnumOptions.HUD_DISTANCE ? this.hudDistance :
              (par1EnumOptions == EnumOptions.HUD_PITCH ? this.hudPitchOffset :
              (par1EnumOptions == EnumOptions.FOV_SCALE_FACTOR ? this.fovScaleFactor :
              (par1EnumOptions == EnumOptions.HEAD_TRACK_SENSITIVITY ? this.headTrackSensitivity :
              (par1EnumOptions == EnumOptions.SUPERSAMPLE_SCALEFACTOR ? this.superSampleScaleFactor :
              (par1EnumOptions == EnumOptions.DISTORTION_FIT_POINT ? (float)this.distortionFitPoint :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_X && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT ? this.posTrackHydraLROffsetX :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_X && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_HMD_LEFT ? this.posTrackHydraLOffsetX :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_X && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_HMD_RIGHT ? this.posTrackHydraROffsetX :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_X && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_HMD_TOP ? this.posTrackHydraTOffsetX :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_X && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_BACK_OF_HEAD && this.posTrackHydraBIsPointingLeft ? this.posTrackHydraBLOffsetX :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_X && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_BACK_OF_HEAD && !this.posTrackHydraBIsPointingLeft ? this.posTrackHydraBROffsetX :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Y && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT ? this.posTrackHydraLROffsetY :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Y && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_HMD_LEFT ? this.posTrackHydraLOffsetY :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Y && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_HMD_RIGHT ? this.posTrackHydraROffsetY :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Y && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_HMD_TOP ? this.posTrackHydraTOffsetY :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Y && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_BACK_OF_HEAD && this.posTrackHydraBIsPointingLeft  ? this.posTrackHydraBLOffsetY :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Y && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_BACK_OF_HEAD && !this.posTrackHydraBIsPointingLeft  ? this.posTrackHydraBROffsetY :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Z && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT ? this.posTrackHydraLROffsetZ :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Z && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_HMD_LEFT ? this.posTrackHydraLOffsetZ :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Z && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_HMD_RIGHT ? this.posTrackHydraROffsetZ :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Z && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_HMD_TOP ? this.posTrackHydraTOffsetZ :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Z && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_BACK_OF_HEAD && this.posTrackHydraBIsPointingLeft  ? this.posTrackHydraBLOffsetZ :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Z && this.posTrackHydraLoc == POS_TRACK_HYDRA_LOC_BACK_OF_HEAD && !this.posTrackHydraBIsPointingLeft  ? this.posTrackHydraBROffsetZ :
              (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_DISTANCE_SCALE ? this.posTrackHydraDistanceScale :
              (par1EnumOptions == EnumOptions.CROSSHAIR_SCALE ? this.crosshairScale :
              (par1EnumOptions == EnumOptions.POS_TRACK_Y_AXIS_DISTANCE_SKEW ? this.posTrackHydraYAxisDistanceSkewAngleDeg : 0.0F))))))))))))))))))))))))))))))))));
    }
    /**
     * For non-float options. Toggles the option on/off, or cycles through the list i.e. render distances.
     */
    public void setOptionValue(EnumOptions par1EnumOptions, int par2)
    {
        if (par1EnumOptions == EnumOptions.USE_VR)
        {
            this.useVRRenderer = !this.useVRRenderer;
            mc.setUseVRRenderer(useVRRenderer);
        }

        if (par1EnumOptions == EnumOptions.USE_DISTORTION)
        {
            this.useDistortion = !this.useDistortion;
        }

        if (par1EnumOptions == EnumOptions.HEAD_TRACKING)
        {
            this.useHeadTracking = !this.useHeadTracking;
        }

        if (par1EnumOptions == EnumOptions.RENDER_OWN_HEADWEAR)
        {
            this.renderHeadWear = !this.renderHeadWear;
        }

        if (par1EnumOptions == EnumOptions.HUD_HIDE)
        {
            this.mc.gameSettings.hideGUI = !this.mc.gameSettings.hideGUI;
        }

        if (par1EnumOptions == EnumOptions.RENDER_FULL_FIRST_PERSON_MODEL)
        {
            this.renderFullFirstPersonModel = !this.renderFullFirstPersonModel;
        }

        if (par1EnumOptions == EnumOptions.HEAD_TRACK_PREDICTION)
        {
            this.useHeadTrackPrediction = !this.useHeadTrackPrediction;
        }

        if (par1EnumOptions == EnumOptions.CHROM_AB_CORRECTION)
        {
            this.useChromaticAbCorrection = !this.useChromaticAbCorrection;
        }

        if (par1EnumOptions == EnumOptions.PITCH_AFFECTS_CAMERA)
        {
            this.allowMousePitchInput = !this.allowMousePitchInput;
        }

        if (par1EnumOptions == EnumOptions.SUPERSAMPLING)
        {
            this.useSupersample = !this.useSupersample;
        }

        if (par1EnumOptions == EnumOptions.DECOUPLE_LOOK_MOVE)
        {
            this.lookMoveDecoupled = !this.lookMoveDecoupled;
        }

        if (par1EnumOptions == EnumOptions.POS_TRACK_HYDRALOC)
        {
            this.posTrackHydraLoc += 1;
            if (this.posTrackHydraLoc > POS_TRACK_HYDRA_LOC_BACK_OF_HEAD)
            {
                this.posTrackHydraLoc = POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT;
            }
        }

        if (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_USE_CONTROLLER_ONE)
        {
            this.posTrackHydraUseController1 = !this.posTrackHydraUseController1;
        }

        if (par1EnumOptions == EnumOptions.MOVEAIM_HYDRA_USE_CONTROLLER_ONE)
        {
            this.posTrackHydraUseController1 = !this.posTrackHydraUseController1;
        }

        if (par1EnumOptions == EnumOptions.OCULUS_PROFILE)
        {
            this.useOculusProfile = !this.useOculusProfile;
        }

        if (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_AT_BACKOFHEAD_IS_POINTING_LEFT)
        {
            this.posTrackHydraBIsPointingLeft = !this.posTrackHydraBIsPointingLeft;
        }
        
        if( par1EnumOptions == EnumOptions.JOYSTICK_AIM_TYPE )
        {
        	this.joystickAimType ++;
        	if( joystickAimType >= JOYSTICK_AIM_TYPE.length )
        		joystickAimType = 0;
        }

        if (par1EnumOptions == EnumOptions.HYDRA_USE_FILTER)
        {
            this.hydraUseFilter = !this.hydraUseFilter;
        }

        if (par1EnumOptions == EnumOptions.CROSSHAIR_ALWAYS_SHOW)
        {
            this.alwaysRenderInGameCrosshair = !this.alwaysRenderInGameCrosshair;
        }

        if (par1EnumOptions == EnumOptions.BLOCK_OUTLINE_ALWAYS_SHOW)
        {
            this.alwaysRenderBlockOutline = !this.alwaysRenderBlockOutline;
        }

        if (par1EnumOptions == EnumOptions.CROSSHAIR_ROLL)
        {
            this.crosshairRollsWithHead = !this.crosshairRollsWithHead;
        }

        if (par1EnumOptions == EnumOptions.HUD_OCCLUSION)
        {
            this.hudOcclusion = !this.hudOcclusion;
        }

        if (par1EnumOptions == EnumOptions.KEYHOLE_HEAD_RELATIVE)
        {
        	this.keyholeHeadRelative = !this.keyholeHeadRelative;
        }

        if (par1EnumOptions == EnumOptions.CALIBRATION_STRATEGY)
        {
            this.calibrationStrategy += 1;
            if (this.calibrationStrategy > 1)
                this.calibrationStrategy = 0;
        }

        this.saveOptions();
    }

    public void setOptionFloatValue(EnumOptions par1EnumOptions, float par2)
    {
        if (par1EnumOptions == EnumOptions.EYE_HEIGHT)
        {
            this.playerEyeHeight = par2;
        }

        if (par1EnumOptions == EnumOptions.EYE_PROTRUSION)
        {
            this.eyeProtrusion = par2;
        }

        if (par1EnumOptions == EnumOptions.NECK_LENGTH)
        {
            this.neckBaseToEyeHeight = par2;
        }

        if (par1EnumOptions == EnumOptions.MOVEMENT_MULTIPLIER)
        {
            this.movementSpeedMultiplier = par2;
        }

        if (par1EnumOptions == EnumOptions.IPD)
        {
            this.ipd = par2;
        }

        if (par1EnumOptions == EnumOptions.HEAD_TRACK_PREDICTION_TIME)
        {
            this.headTrackPredictionTimeSecs = par2;
        }

        if (par1EnumOptions == EnumOptions.JOYSTICK_SENSITIVITY)
        {
            this.joystickSensitivity = par2;
        }

        if (par1EnumOptions == EnumOptions.KEYHOLE_WIDTH)
        {
            this.aimKeyholeWidthDegrees = par2;
        }
        if (par1EnumOptions == EnumOptions.KEYHOLE_HEIGHT)
        {
            this.keyholeHeight = par2;
        }

        if (par1EnumOptions == EnumOptions.HUD_SCALE)
        {
            this.hudScale = par2;
        }

        if (par1EnumOptions == EnumOptions.HUD_OPACITY)
        {
            this.hudOpacity = par2;
        }

        if (par1EnumOptions == EnumOptions.RENDER_PLAYER_OFFSET)
        {
            this.renderPlayerOffset = par2;
        }

        if (par1EnumOptions == EnumOptions.HUD_DISTANCE)
        {
            this.hudDistance = par2;
        }

        if (par1EnumOptions == EnumOptions.HUD_PITCH)
        {
            this.hudPitchOffset = par2;
        }

        if (par1EnumOptions == EnumOptions.FOV_SCALE_FACTOR)
        {
            this.fovScaleFactor = par2;
        }

        if (par1EnumOptions == EnumOptions.HEAD_TRACK_SENSITIVITY)
        {
            this.headTrackSensitivity = par2;
        }

        if (par1EnumOptions == EnumOptions.SUPERSAMPLE_SCALEFACTOR)
        {
            this.superSampleScaleFactor = par2;
        }

        if (par1EnumOptions == EnumOptions.DISTORTION_FIT_POINT)
        {
            this.distortionFitPoint = (int)Math.floor(par2);
        }

        if (par1EnumOptions == EnumOptions.CALIBRATION_STRATEGY)
        {
            this.calibrationStrategy = (int)Math.floor(par2);
        }

        if (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_X)
        {
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
        }

        if (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Y)
        {
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
        }

        if (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_OFFSET_Z)
        {
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
        }

        if (par1EnumOptions == EnumOptions.POS_TRACK_HYDRA_DISTANCE_SCALE)
        {
            this.posTrackHydraDistanceScale = par2;
        }

        if (par1EnumOptions == EnumOptions.POS_TRACK_Y_AXIS_DISTANCE_SKEW)
        {
            this.posTrackHydraYAxisDistanceSkewAngleDeg = par2;
        }

        if (par1EnumOptions == EnumOptions.CROSSHAIR_SCALE)
        {
            this.crosshairScale = par2;
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
        // Save Minecrift settings
        try
        {
            PrintWriter var5 = new PrintWriter(new FileWriter(this.optionsVRFile));

            var5.println("useVRRenderer:"+ this.useVRRenderer );
            var5.println("playerEyeHeight:" + this.playerEyeHeight);
            var5.println("eyeProtrusion:" + this.eyeProtrusion );
            var5.println("neckBaseToEyeHeight:" + this.neckBaseToEyeHeight );
            var5.println("headTrackerPluginID:"+ this.headTrackerPluginID);
            var5.println("headPositionPluginID:"+ this.headPositionPluginID);
            var5.println("hmdPluginID:"+ this.hmdPluginID);
            var5.println("controllerPluginID:"+ this.controllerPluginID);
            var5.println("ipd:" + this.ipd);
            var5.println("headTrackPredictionTimeSecs:" + this.headTrackPredictionTimeSecs);
            var5.println("hudOpacity:" + this.hudOpacity);
            var5.println("useHeadTracking:" + this.useHeadTracking);
            var5.println("useDistortion:" + this.useDistortion);
            var5.println("useHeadTrackPrediction:" + this.useHeadTrackPrediction);
            var5.println("renderHeadWear:" + this.renderHeadWear);
            var5.println("hideGUI:" + this.mc.gameSettings.hideGUI);
            var5.println("renderFullFirstPersonModel:" + this.renderFullFirstPersonModel);
            var5.println("useChromaticAbCorrection:" + this.useChromaticAbCorrection);
            var5.println("hudScale:" + this.hudScale);
            var5.println("renderPlayerOffset:" + this.renderPlayerOffset);
            var5.println("allowMousePitchInput:" + this.allowMousePitchInput);
            var5.println("hudDistance:" + this.hudDistance);
            var5.println("hudPitchOffset:" + this.hudPitchOffset);
            var5.println("useSupersample:" + this.useSupersample);
            var5.println("superSampleScaleFactor:" + this.superSampleScaleFactor);
            var5.println("fovScaleFactor:" + this.fovScaleFactor);
            var5.println("distortionFitPoint:" + this.distortionFitPoint);
            var5.println("calibrationStrategy1:" + this.calibrationStrategy);    // Deliberately using a new value to get people using the 'At startup' setting again by default.
            var5.println("headTrackSensitivity:" + this.headTrackSensitivity);
            var5.println("movementSpeedMultiplier:" + this.movementSpeedMultiplier);
            var5.println("lookMoveDecoupled:" + this.lookMoveDecoupled);
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
            var5.println("alwaysRenderInGameCrosshair:" + this.alwaysRenderInGameCrosshair);
            var5.println("alwaysRenderBlockOutline:" + this.alwaysRenderBlockOutline);
            var5.println("crosshairRollsWithHead:" + this.crosshairRollsWithHead);
            var5.println("hudOcclusion:" + this.hudOcclusion);
            var5.println("joystickSensitivity:" + this.joystickSensitivity);
            var5.println("joystickAimType:" + this.joystickAimType);
            var5.println("keyholeWidth:" + this.aimKeyholeWidthDegrees);
            var5.println("keyholeHeight:" + this.keyholeHeight);
            var5.println("keyholeHeadRelative:" + this.keyholeHeadRelative);
            var5.println("useOculusProfile:" + this.useOculusProfile);
            var5.println("oculusProfileIpd:" + this.oculusProfileIpd);
            var5.println("oculusProfilePlayerEyeHeight:" + this.oculusProfilePlayerEyeHeight);
            var5.println("crosshairScale:" + this.crosshairScale);

            var5.close();
        }
        catch (Exception var3)
        {
            Config.dbg("Failed to save VR options");
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
}

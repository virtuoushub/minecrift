/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;

import com.mtbs3d.minecrift.api.IOrientationProvider;
import com.mtbs3d.minecrift.api.PluginManager;
import org.lwjgl.input.Keyboard;


import net.minecraft.src.Minecraft;

public class VRHotkeys {

	public static void handleKeyboardInputs(Minecraft mc)
	{                                
		// TODO: Capture oculus key events
	
    	if( mc.headTracker == null || mc.hmdInfo == null )
    	{
    		return;
    	}
	    //  Reinitialise head tracking
	    if (Keyboard.getEventKey() == Keyboard.KEY_O && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
            PluginManager.destroyAll();
	    	mc.setUseVRRenderer(mc.vrSettings.useVRRenderer);
	    }

	    // Distortion on / off
	    if (Keyboard.getEventKey() == Keyboard.KEY_P && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        // Chromatic ab correction
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            mc.vrSettings.useChromaticAbCorrection = !mc.vrSettings.useChromaticAbCorrection;
	            mc.vrSettings.saveOptions();
	            mc.vrRenderer._FBOInitialised = false; // Reinit FBO and shaders
	        }
	        else
	        {
	            mc.vrSettings.useDistortion = !mc.vrSettings.useDistortion;
	        }
	    }
	
	    // Supersampling on/off
	    if (Keyboard.getEventKey() == Keyboard.KEY_B && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        // FSAA on/off
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            mc.vrSettings.superSampleScaleFactor += 0.5f;
	            if (mc.vrSettings.superSampleScaleFactor > 4.0f)
	            {
	                mc.vrSettings.superSampleScaleFactor = 1.5f;
	            }
	            mc.vrSettings.saveOptions();
	            mc.vrRenderer._FBOInitialised = false;
	        }
	        else
	        {
	            mc.vrSettings.useSupersample = !mc.vrSettings.useSupersample;
	            mc.vrSettings.saveOptions();
	            mc.vrRenderer._FBOInitialised = false; // Reinit FBO and shaders
	        }
	    }
	
	    // Head tracking on / off
	    if (Keyboard.getEventKey() == Keyboard.KEY_L && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            mc.vrSettings.useHeadTrackPrediction = !mc.vrSettings.useHeadTrackPrediction;
                mc.headTracker.setPrediction(mc.vrSettings.headTrackPredictionTimeSecs, mc.vrSettings.useHeadTrackPrediction);
	            mc.vrSettings.saveOptions();
	        }
	        else
	        {
	            mc.vrSettings.useHeadTracking = !mc.vrSettings.useHeadTracking;
	        }
	    }
	
	    // Lock distance
	    if (Keyboard.getEventKey() == Keyboard.KEY_U && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        // HUD scale
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            mc.vrSettings.hudScale -= 0.01f;
	            if (mc.vrSettings.hudScale < 0.15f)
	            {
	                mc.vrSettings.hudScale = 1.25f;
	            }
	            mc.vrSettings.saveOptions();
	        }
	        else
	        {
	            mc.vrSettings.hudDistance -= 0.01f;
	            if (mc.vrSettings.hudDistance < 0.15f)
	            {
	                mc.vrSettings.hudDistance = 1.25f;
	            }
	            mc.vrSettings.saveOptions();
	            //mc.vrSettings.lockHud = !mc.vrSettings.lockHud; // TOOD: HUD lock removed for now
	        }
	    }
	
	    // Hud opacity on / off
	    if (Keyboard.getEventKey() == Keyboard.KEY_Y && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        mc.vrSettings.useHudOpacity = !mc.vrSettings.useHudOpacity;
	        mc.vrSettings.saveOptions();
	    }
	
	    // Render headwear / ON/off
	    if (Keyboard.getEventKey() == Keyboard.KEY_M && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        mc.vrSettings.renderHeadWear = !mc.vrSettings.renderHeadWear;
	        mc.vrSettings.saveOptions();
	    }
	
	    // Allow mouse pitch
	    if (Keyboard.getEventKey() == Keyboard.KEY_N && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        mc.vrSettings.pitchInputAffectsCamera = !mc.vrSettings.pitchInputAffectsCamera;
	        mc.vrSettings.saveOptions();
	    }
	
	    // FOV+
	    if (Keyboard.getEventKey() == Keyboard.KEY_PERIOD && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            // Distortion fit point
	            mc.vrSettings.distortionFitPoint += 1;
	            if (mc.vrSettings.distortionFitPoint > 14)
	                mc.vrSettings.distortionFitPoint = 14;
	            mc.vrSettings.saveOptions();
	            mc.vrRenderer._FBOInitialised = false; // Reinit FBO and shaders
	        }
	        else
	        {
	            // FOV
	            mc.vrSettings.fovScaleFactor += 0.001f;
	            mc.vrSettings.saveOptions();
	        }
	    }
	
	    // FOV-
	    if (Keyboard.getEventKey() == Keyboard.KEY_COMMA && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            // Distortion fit point
	            mc.vrSettings.distortionFitPoint -= 1;
	            if (mc.vrSettings.distortionFitPoint < 0)
	                mc.vrSettings.distortionFitPoint = 0;
	            mc.vrSettings.saveOptions();
	            mc.vrRenderer._FBOInitialised = false; // Reinit FBO and shaders
	        }
	        else
	        {
	            // FOV
	            mc.vrSettings.fovScaleFactor -= 0.001f;
	            mc.vrSettings.saveOptions();
	        }
	    }
	
	    // Cycle head track sensitivity
	    if (Keyboard.getEventKey() == Keyboard.KEY_V && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        mc.vrSettings.headTrackSensitivity += 0.1f;
	        if (mc.vrSettings.headTrackSensitivity > 4.05f)
	        {
	            mc.vrSettings.headTrackSensitivity = 0.5f;
	        }
	        mc.vrSettings.saveOptions();
	    }
	
	    // Increase IPD
	    if (Keyboard.getEventKey() == Keyboard.KEY_EQUALS && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        float newIpd;
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            newIpd = mc.vrSettings.getIPD() + 0.0001f;
	        }
	        else
	        {
	            newIpd = mc.vrSettings.getIPD() + 0.0005f;
	        }
	        mc.hmdInfo.setIPD(newIpd);
	        mc.vrSettings.setMinecraftIpd(newIpd);
	        mc.vrSettings.saveOptions();
	    }
	
	    // Decrease IPD
	    if (Keyboard.getEventKey() == Keyboard.KEY_MINUS && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        float newIpd;
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            newIpd = mc.vrSettings.getIPD() - 0.0001f;
	        }
	        else
	        {
	            newIpd = mc.vrSettings.getIPD() - 0.0005f;
	        }
	        mc.hmdInfo.setIPD(newIpd);
	        mc.vrSettings.setMinecraftIpd(newIpd);
	        mc.vrSettings.saveOptions();
	    }

        // Render full player model or just an disembodied hand...
        if (Keyboard.getEventKey() == Keyboard.KEY_H && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
        {
            mc.vrSettings.renderFullFirstPersonModel = !mc.vrSettings.renderFullFirstPersonModel;
            mc.vrSettings.saveOptions();
        }

        // Reset positional track origin
        if (Keyboard.getEventKey() == Keyboard.KEY_RETURN && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
        {
            mc.vrSettings.posTrackResetPosition = true;
        }

        // If an orientation plugin is performing calibration, space also sets the origin
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
        {
            PluginManager.notifyAll(IOrientationProvider.EVENT_ORIENTATION_AT_ORIGIN);
        }
	}
}

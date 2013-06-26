/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;

import com.mtbs3d.minecrift.api.IBasePlugin;
import org.lwjgl.input.Keyboard;

import com.mtbs3d.minecrift.api.BasePlugin;

import net.minecraft.client.Minecraft;

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
	    	BasePlugin.destroyAll();
	    	mc.setUseVRRenderer(mc.gameSettings.useVRRenderer);
	    }

	    // Distortion on / off
	    if (Keyboard.getEventKey() == Keyboard.KEY_P && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        // Chromatic ab correction
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            mc.gameSettings.useChromaticAbCorrection = !mc.gameSettings.useChromaticAbCorrection;
	            mc.gameSettings.saveOptions();
	            mc.vrRenderer._FBOInitialised = false; // Reinit FBO and shaders
	        }
	        else
	        {
	            mc.gameSettings.useDistortion = !mc.gameSettings.useDistortion;
	        }
	    }
	
	    // Supersampling on/off
	    if (Keyboard.getEventKey() == Keyboard.KEY_B && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        // FSAA on/off
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            mc.gameSettings.superSampleScaleFactor += 0.5f;
	            if (mc.gameSettings.superSampleScaleFactor > 4.0f)
	            {
	                mc.gameSettings.superSampleScaleFactor = 1.5f;
	            }
	            mc.gameSettings.saveOptions();
	            mc.vrRenderer._FBOInitialised = false;
	        }
	        else
	        {
	            mc.gameSettings.useSupersample = !mc.gameSettings.useSupersample;
	            mc.gameSettings.saveOptions();
	            mc.vrRenderer._FBOInitialised = false; // Reinit FBO and shaders
	        }
	    }
	
	    // Head tracking on / off
	    if (Keyboard.getEventKey() == Keyboard.KEY_L && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            mc.gameSettings.useHeadTrackPrediction = !mc.gameSettings.useHeadTrackPrediction;
                mc.headTracker.setPrediction(mc.gameSettings.headTrackPredictionTimeSecs, mc.gameSettings.useHeadTrackPrediction);
	            mc.gameSettings.saveOptions();
	        }
	        else
	        {
	            mc.gameSettings.useHeadTracking = !mc.gameSettings.useHeadTracking;
	        }
	    }
	
	    // Lock distance
	    if (Keyboard.getEventKey() == Keyboard.KEY_U && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        // HUD scale
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            mc.gameSettings.hudScale -= 0.01f;
	            if (mc.gameSettings.hudScale < 0.15f)
	            {
	                mc.gameSettings.hudScale = 1.25f;
	            }
	            mc.gameSettings.saveOptions();
	        }
	        else
	        {
	            mc.gameSettings.hudDistance -= 0.01f;
	            if (mc.gameSettings.hudDistance < 0.15f)
	            {
	                mc.gameSettings.hudDistance = 1.25f;
	            }
	            mc.gameSettings.saveOptions();
	            //mc.gameSettings.lockHud = !mc.gameSettings.lockHud; // TOOD: HUD lock removed for now
	        }
	    }
	
	    // Hud opacity on / off
	    if (Keyboard.getEventKey() == Keyboard.KEY_Y && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        mc.gameSettings.useHudOpacity = !mc.gameSettings.useHudOpacity;
	        mc.gameSettings.saveOptions();
	    }
	
	    // Render headwear / ON/off
	    if (Keyboard.getEventKey() == Keyboard.KEY_M && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        mc.gameSettings.renderHeadWear = !mc.gameSettings.renderHeadWear;
	        mc.gameSettings.saveOptions();
	    }
	
	    // Allow mouse pitch
	    if (Keyboard.getEventKey() == Keyboard.KEY_N && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        mc.gameSettings.pitchInputAffectsCamera = !mc.gameSettings.pitchInputAffectsCamera;
	        mc.gameSettings.saveOptions();
	    }
	
	    // FOV+
	    if (Keyboard.getEventKey() == Keyboard.KEY_PERIOD && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            // Distortion fit point
	            mc.gameSettings.distortionFitPoint += 1;
	            if (mc.gameSettings.distortionFitPoint > 14)
	                mc.gameSettings.distortionFitPoint = 14;
	            mc.gameSettings.saveOptions();
	            mc.vrRenderer._FBOInitialised = false; // Reinit FBO and shaders
	        }
	        else
	        {
	            // FOV
	            mc.gameSettings.fovScaleFactor += 0.001f;
	            mc.gameSettings.saveOptions();
	        }
	    }
	
	    // FOV-
	    if (Keyboard.getEventKey() == Keyboard.KEY_COMMA && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            // Distortion fit point
	            mc.gameSettings.distortionFitPoint -= 1;
	            if (mc.gameSettings.distortionFitPoint < 0)
	                mc.gameSettings.distortionFitPoint = 0;
	            mc.gameSettings.saveOptions();
	            mc.vrRenderer._FBOInitialised = false; // Reinit FBO and shaders
	        }
	        else
	        {
	            // FOV
	            mc.gameSettings.fovScaleFactor -= 0.001f;
	            mc.gameSettings.saveOptions();
	        }
	    }
	
	    // Cycle head track sensitivity
	    if (Keyboard.getEventKey() == Keyboard.KEY_V && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        mc.gameSettings.headTrackSensitivity += 0.1f;
	        if (mc.gameSettings.headTrackSensitivity > 4.05f)
	        {
	            mc.gameSettings.headTrackSensitivity = 0.5f;
	        }
	        mc.gameSettings.saveOptions();
	    }
	
	    // Increase IPD
	    if (Keyboard.getEventKey() == Keyboard.KEY_EQUALS && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        float newIpd;
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            newIpd = mc.gameSettings.ipd + 0.0001f;
	        }
	        else
	        {
	            newIpd = mc.gameSettings.ipd + 0.0005f;
	        }
	        mc.hmdInfo.setIPD(newIpd);
	        mc.gameSettings.ipd = newIpd;
	        mc.gameSettings.saveOptions();
	    }
	
	    // Decrease IPD
	    if (Keyboard.getEventKey() == Keyboard.KEY_MINUS && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        float newIpd;
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            newIpd = mc.gameSettings.ipd - 0.0001f;
	        }
	        else
	        {
	            newIpd = mc.gameSettings.ipd - 0.0005f;
	        }
	        mc.hmdInfo.setIPD(newIpd);
	        mc.gameSettings.ipd = newIpd;
	        mc.gameSettings.saveOptions();
	    }

        // Reset positional track origin
        if (Keyboard.getEventKey() == Keyboard.KEY_RETURN)
        {
            mc.gameSettings.posTrackResetPosition = true;
        }

        // If a plugin is performing calibration, space also sets the origin
        if (Keyboard.getEventKey() == Keyboard.KEY_SPACE)
        {
            BasePlugin.notifyAll(IBasePlugin.EVENT_SET_ORIGIN);
        }
	}
}

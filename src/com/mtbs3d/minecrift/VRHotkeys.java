/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;

import com.mtbs3d.minecrift.api.IBasePlugin;
import com.mtbs3d.minecrift.api.IOrientationProvider;
import com.mtbs3d.minecrift.api.PluginManager;
import com.mtbs3d.minecrift.settings.VRSettings;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

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
            mc.printChatMessage("Re-initialising all plugins...");
	    }

        if (Keyboard.getEventKey() == Keyboard.KEY_R && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
        {
            //mc.renderStereo = !mc.renderStereo;   // TODO: Disabled for now until mono works again
        }

	    // Distortion on / off
	    if (Keyboard.getEventKey() == Keyboard.KEY_P && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        // Chromatic ab correction
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            mc.vrSettings.useChromaticAbCorrection = !mc.vrSettings.useChromaticAbCorrection;
	            mc.vrSettings.saveOptions();
                Minecraft.getMinecraft().reinitFramebuffers = true; // Reinit FBO and shaders

                if (mc.vrSettings.useChromaticAbCorrection)
                    mc.printChatMessage("Chromatic Aberration Correction ON");
                else
                    mc.printChatMessage("Chromatic Aberration Correction OFF");
	        }
	        else
	        {
                if (mc.vrSettings.useDistortion == false)
                {
                    mc.vrSettings.useDistortion = true;
                    mc.vrSettings.useDistortionTextureLookupOptimisation = false;
                    mc.printChatMessage("Distortion ON [Brute Force]");
                }
                else if (mc.vrSettings.useDistortion == true && mc.vrSettings.useDistortionTextureLookupOptimisation == false)
                {
                    mc.vrSettings.useDistortion = true;
                    mc.vrSettings.useDistortionTextureLookupOptimisation = true;
                    mc.printChatMessage("Distortion ON [Texture Lookup]");
                }
                else
                {
                    mc.vrSettings.useDistortion = false;
                    mc.vrSettings.useDistortionTextureLookupOptimisation = false;
                    mc.printChatMessage("Distortion OFF");
                }

                mc.vrSettings.saveOptions();
                Minecraft.getMinecraft().reinitFramebuffers = true; // Reinit FBO and shaders
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
                Minecraft.getMinecraft().reinitFramebuffers = true;
                mc.printChatMessage(String.format("FSAA scale factor: %.1f", new Object[]{Float.valueOf(mc.vrSettings.superSampleScaleFactor)}));
	        }
	        else
	        {
	            mc.vrSettings.useSupersample = !mc.vrSettings.useSupersample;
	            mc.vrSettings.saveOptions();
                Minecraft.getMinecraft().reinitFramebuffers = true; // Reinit FBO and shaders
                mc.printChatMessage("FSAA: " + (mc.vrSettings.useSupersample ? "On" : "Off"));
	        }
	    }
	
	    // Head tracking on / off
	    if (Keyboard.getEventKey() == Keyboard.KEY_L && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
	        {
	            mc.vrSettings.useHeadTrackPrediction = !mc.vrSettings.useHeadTrackPrediction;
                //mc.headTracker.setPrediction(mc.vrSettings.headTrackPredictionTimeSecs, mc.vrSettings.useHeadTrackPrediction);   // TODO:
	            mc.vrSettings.saveOptions();
                if (mc.vrSettings.useHeadTrackPrediction)
                    mc.printChatMessage(String.format("Head tracking prediction: On (%.2fms)", new Object[]{Float.valueOf(mc.vrSettings.headTrackPredictionTimeSecs * 1000)}));
                else
                    mc.printChatMessage("Head tracking prediction: Off");
	        }
	        else
	        {
	            mc.vrSettings.useHeadTracking = !mc.vrSettings.useHeadTracking;
                mc.printChatMessage("Head tracking: " + (mc.vrSettings.useHeadTracking ? "On" : "Off"));
	        }
	    }

        // Quaternion prototype on / off
        if (Keyboard.getEventKey() == Keyboard.KEY_E && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
        {
            mc.vrSettings.useQuaternions = !mc.vrSettings.useQuaternions;
            mc.printChatMessage("useQuaternions: " + (mc.vrSettings.useQuaternions ? "On" : "Off"));
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
                mc.printChatMessage(String.format("HUD scale: %.2f", new Object[]{Float.valueOf(mc.vrSettings.hudScale)}));
	        }
	        else
	        {
	            mc.vrSettings.hudDistance -= 0.01f;
	            if (mc.vrSettings.hudDistance < 0.15f)
	            {
	                mc.vrSettings.hudDistance = 1.25f;
	            }
	            mc.vrSettings.saveOptions();
                mc.printChatMessage(String.format("HUD distance: %.2f", new Object[]{Float.valueOf(mc.vrSettings.hudDistance)}));
	        }
	    }
	
	    // Hud opacity on / off
	    if (Keyboard.getEventKey() == Keyboard.KEY_Y && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        mc.vrSettings.hudOpacity = 1-mc.vrSettings.hudOpacity;
	        if( mc.vrSettings.hudOpacity < 0.15f)
	        	mc.vrSettings.hudOpacity = 0.15f;
	        mc.vrSettings.saveOptions();
            mc.printChatMessage(String.format("HUD opacity: %.2f%%", new Object[]{Float.valueOf(mc.vrSettings.hudOpacity)}));
	    }
	
	    // Render headwear / ON/off
	    if (Keyboard.getEventKey() == Keyboard.KEY_M && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
	        mc.vrSettings.renderHeadWear = !mc.vrSettings.renderHeadWear;
	        mc.vrSettings.saveOptions();
            mc.printChatMessage("Render headwear: " + (mc.vrSettings.renderHeadWear ? "On" : "Off"));
	    }
	
	    // Allow mouse pitch
	    if (Keyboard.getEventKey() == Keyboard.KEY_N && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
            // TODO: Disabled for now until working
	        //mc.vrSettings.allowMousePitchInput = !mc.vrSettings.allowMousePitchInput;
	        mc.vrSettings.saveOptions();
            mc.printChatMessage("Allow mouse pitch input: " + (mc.vrSettings.allowMousePitchInput ? "On" : "Off"));
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
                Minecraft.getMinecraft().reinitFramebuffers = true; // Reinit FBO and shaders
                mc.printChatMessage(String.format("Distortion fit point: %.0f", new Object[]{Float.valueOf(mc.vrSettings.distortionFitPoint)}));
	        }
	        else
	        {
	            // FOV
	            mc.vrSettings.fovScaleFactor += 0.001f;
	            mc.vrSettings.saveOptions();
                Minecraft.getMinecraft().reinitFramebuffers = true; // Reinit FBO and shaders
                mc.printChatMessage(String.format("FOV scale factor: %.3f", new Object[]{Float.valueOf(mc.vrSettings.fovScaleFactor)}));
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
                Minecraft.getMinecraft().reinitFramebuffers = true; // Reinit FBO and shaders
                mc.printChatMessage(String.format("Distortion fit point: %.0f", new Object[]{Float.valueOf(mc.vrSettings.distortionFitPoint)}));
	        }
	        else
	        {
	            // FOV
	            mc.vrSettings.fovScaleFactor -= 0.001f;
	            mc.vrSettings.saveOptions();
                Minecraft.getMinecraft().reinitFramebuffers = true; // Reinit FBO and shaders
                mc.printChatMessage(String.format("FOV scale factor: %.3f", new Object[]{Float.valueOf(mc.vrSettings.fovScaleFactor)}));
	        }
	    }
	
	    // Cycle head track sensitivity
	    if (Keyboard.getEventKey() == Keyboard.KEY_V && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
            if (mc.vrSettings.useQuaternions == false) {
                float sens = mc.vrSettings.getHeadTrackSensitivity();
                sens += 0.1f;
                if (sens > 4.05f) {
                    sens = 0.5f;
                }
                mc.vrSettings.setHeadTrackSensitivity(sens);
                mc.vrSettings.saveOptions();
                mc.printChatMessage(String.format("Head track sensitivity: * %.1f", new Object[]{Float.valueOf(mc.vrSettings.getHeadTrackSensitivity())}));
            }
            else
            {
                mc.printChatMessage("Head track sensitivity LOCKED to 1.0 (orientation mode = Quaternion)");
            }
	    }
	
	    // Increase IPD
	    if (Keyboard.getEventKey() == Keyboard.KEY_EQUALS && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
            if (mc.vrSettings.useOculusProfile)
            {
                mc.printChatMessage("IPD unchanged (set via Oculus Profile)");
            }
            else
            {
                float newIpd;
                if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
                {
                    newIpd = mc.vrSettings.getIPD() + 0.0005f;
                }
                else
                {
                    newIpd = mc.vrSettings.getIPD() + 0.0001f;
                }
                //mc.hmdInfo.setIPD(newIpd);     // TODO: IPD
                mc.vrSettings.setMinecraftIpd(newIpd);
                mc.vrSettings.saveOptions();
                Minecraft.getMinecraft().reinitFramebuffers = true; // Reinit FBO and shaders
                mc.printChatMessage(String.format("IPD: %.1fmm", new Object[]{Float.valueOf(mc.vrSettings.getIPD() * 1000f)}));
            }
	    }
	
	    // Decrease IPD
	    if (Keyboard.getEventKey() == Keyboard.KEY_MINUS && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
	    {
            if (mc.vrSettings.useOculusProfile)
            {
                mc.printChatMessage("IPD unchanged (set via Oculus Profile)");
            }
            else
            {
                float newIpd;
                if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
                {
                    newIpd = mc.vrSettings.getIPD() - 0.0005f;
                }
                else
                {
                    newIpd = mc.vrSettings.getIPD() - 0.0001f;
                }
                //mc.hmdInfo.setIPD(newIpd);       // TODO: IPD
                mc.vrSettings.setMinecraftIpd(newIpd);
                mc.vrSettings.saveOptions();
                Minecraft.getMinecraft().reinitFramebuffers = true; // Reinit FBO and shaders
                mc.printChatMessage(String.format("IPD: %.1fmm", new Object[]{Float.valueOf(mc.vrSettings.getIPD() * 1000f)}));
            }
	    }

        // Render full player model or just an disembodied hand...
        if (Keyboard.getEventKey() == Keyboard.KEY_H && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
        {
            mc.vrSettings.renderFullFirstPersonModelMode++;
            if (mc.vrSettings.renderFullFirstPersonModelMode > VRSettings.RENDER_FIRST_PERSON_NONE)
                mc.vrSettings.renderFullFirstPersonModelMode = VRSettings.RENDER_FIRST_PERSON_FULL;
            mc.vrSettings.saveOptions();
            mc.printChatMessage("First person model: " + (mc.vrSettings.renderFullFirstPersonModelMode == VRSettings.RENDER_FIRST_PERSON_FULL ? "Full" :
                    (mc.vrSettings.renderFullFirstPersonModelMode == VRSettings.RENDER_FIRST_PERSON_HAND ? "Hand only" : "None")));
        }

        // Reset positional track origin
        if (Keyboard.getEventKey() == Keyboard.KEY_RETURN && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
        {
            mc.vrSettings.posTrackResetPosition = true;
            mc.printChatMessage("Reset origin: done");
        }

        // If an orientation plugin is performing calibration, space also sets the origin
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
        {
            PluginManager.notifyAll(IBasePlugin.EVENT_CALIBRATION_SET_ORIGIN);
        }
	}
}

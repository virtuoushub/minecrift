/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;

import java.io.File;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.Display;

import com.mtbs3d.minecrift.api.BasePlugin;
import com.mtbs3d.minecrift.api.ILookAimController;

public class MCMouse extends BasePlugin implements ILookAimController {

	private float lookYaw = 0f;
	private float lookPitch = 0f;
	
	private float aimYaw = 0f;
	private float aimPitch = 0f;
	
	private Minecraft mc;

	@Override
	public String getName() {
		return "Mouse";
	}

	@Override
	public String getID() {
		return "mouse";
	}
	@Override
	public String getInitializationStatus() {
		return "";
	}

	@Override
	public String getVersion() {
		return "0.28";
	}

	@Override
	public boolean init(File nativeDir) {
		return init();
	}

	@Override
	public boolean init() {
		mc = Minecraft.getMinecraft();
		return isInitialized();
	}

	@Override
	public boolean isInitialized() {
		return mc != null;
	}

	@Override
	public void poll() {
		if(this.mc.currentScreen == null && Display.isActive())
		{
            this.mc.mouseHelper.mouseXYChange();
            float mouseSensitivityMultiplier1 = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float mouseSensitivityMultiplier2 = mouseSensitivityMultiplier1 * mouseSensitivityMultiplier1 * mouseSensitivityMultiplier1 * 8.0F;
            float adjustedMouseDeltaX = (float)this.mc.mouseHelper.deltaX * mouseSensitivityMultiplier2 * 0.15f;
            float adjustedMouseDeltaY = (float)this.mc.mouseHelper.deltaY * mouseSensitivityMultiplier2 * 0.15f;
            byte yDirection = -1;

            if (this.mc.gameSettings.invertMouse)
            {
                yDirection = 1;
            }

            //Pitch
            if( this.mc.gameSettings.pitchInputAffectsCamera )
            	lookPitch  += (adjustedMouseDeltaY * (float)yDirection);
            else
            	lookPitch = 0;

            if( lookPitch > 90 )
            	lookPitch = 90;
            if( lookPitch < -90 )
            	lookPitch = -90;

            if( this.mc.gameSettings.lookAimDecoupled )
            {
            	aimPitch += (adjustedMouseDeltaY * (float)yDirection);
            }
            else
            {
            	aimPitch = lookPitch;
            }

            if( aimPitch > 90 )
            	aimPitch = 90;
            if( aimPitch < -90 )
            	aimPitch = -90;
            
            if( aimPitch != 90 && aimPitch != -90 )
            {
	            
	            //Yaw
	            if( !this.mc.gameSettings.lookAimDecoupled || Math.abs(aimYaw - lookYaw + adjustedMouseDeltaX) > this.mc.gameSettings.aimKeyholeWidthDegrees/2 )
	            {
	                lookYaw += adjustedMouseDeltaX ;
	                lookYaw %= 360;
	            }
	            if( !this.mc.gameSettings.lookAimDecoupled )
	            	aimYaw = lookYaw;
	            else
	                aimYaw += adjustedMouseDeltaX;
	                aimYaw %= 360;
	
            }
		}
	}

	@Override
	public void destroy() {/*no-op*/ }

	@Override
	public float getLookYawOffset() {
		return lookYaw;
	}

	@Override
	public float getLookPitchOffset() {
		return lookPitch;
	}

	@Override
	public float getAimYaw() {
		return aimYaw;
	}

	@Override
	public float getAimPitch() {
		return aimPitch;
	}

	@Override
	public boolean isCalibrated() {
		return true;
	}

	@Override
	public String getCalibrationStep() {
		return "";
	}

    @Override
    public void eventNotification(int eventId) {
    }
}

/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;

import java.io.File;

import net.minecraft.src.Minecraft;
import net.minecraft.src.MathHelper;

import org.lwjgl.opengl.Display;

import com.mtbs3d.minecrift.api.BasePlugin;
import com.mtbs3d.minecrift.api.IBodyAimController;

public class MCMouse extends BasePlugin implements IBodyAimController {

	private float bodyYaw = 0f;
	private float bodyPitch = 0f;
	
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
            if( this.mc.vrSettings.pitchInputAffectsCamera )
            {
            	bodyPitch  += (adjustedMouseDeltaY * (float)yDirection);
	            if( bodyPitch > 90 )
	            	bodyPitch = 90;
	            if( bodyPitch < -90 )
	            	bodyPitch = -90;
            }
            else
            	bodyPitch = 0;

            if( !this.mc.vrSettings.pitchInputAffectsCamera )
            {
            	aimPitch += (adjustedMouseDeltaY * (float)yDirection);
	            if( aimPitch > 90 )
	            	aimPitch = 90;
	            if( aimPitch < -90 )
	            	aimPitch = -90;
            }
            else
            {
            	aimPitch = bodyPitch;
            }

        	float headYaw = this.mc.headTracker.getHeadYawDegrees();
        	float cosHeadPitch = MathHelper.cos(this.mc.headTracker.getHeadPitchDegrees()*PIOVER180);
        	float keyholeYaw = this.mc.vrSettings.aimKeyholeWidthDegrees/2/cosHeadPitch;
        	
        	boolean aimYawAllowed;
        	if( this.mc.vrSettings.lookAimYawDecoupled && this.mc.vrSettings.lookAimPitchDecoupled)
        		aimYawAllowed = aimPitch != 90 && aimPitch != -90;
        	else
        		aimYawAllowed = true;

            if( aimYawAllowed && adjustedMouseDeltaX != 0 )
            {
                aimYaw += adjustedMouseDeltaX/cosHeadPitch;

	            //Yaw
	            if( !this.mc.vrSettings.lookAimYawDecoupled )
	                bodyYaw = aimYaw;
	            else if( aimYaw > (headYaw + bodyYaw + keyholeYaw) )
	            	bodyYaw += adjustedMouseDeltaX;
	            else if( aimYaw < (headYaw + bodyYaw - keyholeYaw))
	            	bodyYaw += adjustedMouseDeltaX;
                aimYaw %= 360;
                bodyYaw %= 360;
            }
            else if( this.mc.vrSettings.lookAimYawDecoupled )
            {
	            if( aimYaw > (headYaw + bodyYaw + keyholeYaw) )
	            	aimYaw = headYaw + bodyYaw + keyholeYaw;
	            else if( aimYaw < (headYaw + bodyYaw - keyholeYaw))
	            	aimYaw = headYaw + bodyYaw - keyholeYaw;
            }
		}
	}

	@Override
	public void destroy() {/*no-op*/ }

	@Override
	public float getBodyYawDegrees() {
		return bodyYaw;
	}

	@Override
	public void setBodyYawDegrees( float yawOffset ) {
		bodyYaw = yawOffset;
	}

	@Override
	public float getBodyPitchDegrees() {
		return bodyPitch;
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

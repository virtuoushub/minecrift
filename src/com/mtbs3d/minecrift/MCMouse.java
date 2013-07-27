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
import com.mtbs3d.minecrift.control.ControlBinding;

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
            float adjustedMouseDeltaY = (float)this.mc.mouseHelper.deltaY * mouseSensitivityMultiplier2 * 0.15f * (mc.gameSettings.invertMouse ?  1 : -1);

            //Pitch
            if( this.mc.vrSettings.keyholeHeight > 0 )
            {
            	float headPitch = this.mc.headTracker.getHeadPitchDegrees() + bodyPitch;
            	float keyholeBot = Math.max(-90,headPitch - this.mc.vrSettings.keyholeHeight /2);
            	float keyholeTop = Math.min(90,headPitch + this.mc.vrSettings.keyholeHeight /2);
            	if( aimPitch > keyholeTop )
            		aimPitch = keyholeTop;
            	if( aimPitch < keyholeBot )
            		aimPitch = keyholeBot;
	            if( adjustedMouseDeltaY != 0 )
	            {
	                aimPitch += adjustedMouseDeltaY;
	                if( aimPitch > keyholeTop )
	                {
	                	if( this.mc.vrSettings.allowMousePitchInput)
		                	bodyPitch += aimPitch - keyholeTop;
	                	aimPitch = keyholeTop;
	                }
	                else if( aimPitch < keyholeBot )
	                {
	                	if( this.mc.vrSettings.allowMousePitchInput)
		                	bodyPitch += aimPitch - keyholeBot;
	                	aimPitch = keyholeBot;
	                }
	            }
            }
            else if( this.mc.vrSettings.allowMousePitchInput )
            	bodyPitch  += adjustedMouseDeltaY;
            else
            	bodyPitch = 0;
            
            
            if( bodyPitch > 90 )
            	bodyPitch = 90;
            if( bodyPitch < -90 )
            	bodyPitch = -90;

            if( this.mc.vrSettings.aimKeyholeWidthDegrees > 0 )
            {
	        	float headYaw = this.mc.headTracker.getHeadYawDegrees();
	        	float cosHeadPitch = 1;//MathHelper.cos(this.mc.headTracker.getHeadPitchDegrees()*PIOVER180);
	        	float keyholeYawWidth = this.mc.vrSettings.aimKeyholeWidthDegrees/2/cosHeadPitch;
	        	float keyholeYawLeft = headYaw - keyholeYawWidth;
	        	float keyholeYawRight = headYaw + keyholeYawWidth;
	        	
	        	//Keep mouse constrained to keyhole
	            if( aimYaw > keyholeYawRight )
	            	aimYaw = keyholeYawRight;
	            else if ( aimYaw < keyholeYawLeft )
	            	aimYaw = keyholeYawLeft;
	
	            if( aimPitch != 90 && aimPitch != -90 && adjustedMouseDeltaX != 0 )
	            {
	                aimYaw += adjustedMouseDeltaX/cosHeadPitch;
	                if( aimYaw > keyholeYawRight )
	                {
	                	bodyYaw += aimYaw - keyholeYawRight;
	                	aimYaw = keyholeYawRight;
	                }
	                else if( aimYaw < keyholeYawLeft )
	                {
	                	bodyYaw += aimYaw - keyholeYawLeft;
	                	aimYaw = keyholeYawLeft;
	                }
	                bodyYaw %= 360;
	            }
            }
            else
            {
            	aimYaw = 0;
                bodyYaw += adjustedMouseDeltaX;
                bodyYaw %= 360;
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
		return bodyYaw + aimYaw;
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

	@Override
	public void mapBinding(ControlBinding binding) {
	}
}

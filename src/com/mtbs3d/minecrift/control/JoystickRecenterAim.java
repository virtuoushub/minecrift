/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.control;

import com.mtbs3d.minecrift.settings.VRSettings;

public class JoystickRecenterAim extends JoystickAim {

	public void update( float partialTicks ) {		
        float aimYawAdd = 2*aimYawRate * VRSettings.inst.joystickSensitivity * partialTicks;
		
        if( this.mc.vrSettings.keyholeHeight > 0 )
        {
        	float headPitch = this.mc.headTracker.getHeadPitchDegrees();
        	float keyHoleHeight = this.mc.vrSettings.keyholeHeight /2;
        	
        	//square the number to give more precision in the low angles
    		aimPitch = headPitch + aimPitchRate * Math.abs(aimPitchRate)* keyHoleHeight;
			if( aimPitch > 90.0f )
				aimPitch = 90.0f;
			else if( aimPitch < -90.0f )
				aimPitch = -90.0f;
        }

        if( this.mc.vrSettings.aimKeyholeWidthDegrees > 0 )
        {
        	float headYaw = this.mc.headTracker.getHeadYawDegrees();
        	float keyholeYawWidth = this.mc.vrSettings.aimKeyholeWidthDegrees/2;
        	float keyholeYawLeft = headYaw - keyholeYawWidth;
        	float keyholeYawRight = headYaw + keyholeYawWidth;
        	
        	if( Math.abs(aimYawRate) < 0.75 ) {
        		aimYaw = headYaw + 16 * aimYawRate * Math.abs(aimYawRate)* keyholeYawWidth / 9;
        	} else {
                if( aimYawRate > 0) {
                	aimYaw = keyholeYawRight;
                } else {
                	aimYaw = keyholeYawLeft;
                }

            	bodyYaw = lastBodyYaw + 0.75f*aimYawAdd;
                bodyYaw %= 360;
        	}
        }
        else
        {
        	aimYaw = 0;
            bodyYaw = lastBodyYaw + aimYawAdd;
            bodyYaw %= 360;
        }

	}
}

/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.control;

import com.mtbs3d.minecrift.settings.VRSettings;

public class JoystickAimLoose extends JoystickAim {

	public void update( float partialTicks ) {		
        float aimYawAdd = 2*aimYawRate * VRSettings.inst.joystickSensitivity * partialTicks;
        float aimPitchAdd = 2*aimPitchRate * VRSettings.inst.joystickSensitivity * partialTicks;

		
    	headPitch = this.mc.headTracker.getHeadPitchDegrees();
    	if( holdCenter ) {
        	aimPitch = headPitch;
    	} else if( this.mc.vrSettings.keyholeHeight > 0 ) {
        	float aimPitchT = lastAimPitch + aimPitchAdd;
        	float keyholeBot = headPitch - this.mc.vrSettings.keyholeHeight /2;
        	float keyholeTop = headPitch + this.mc.vrSettings.keyholeHeight /2;
        	if( ( aimPitchT < keyholeTop || aimPitchAdd < 0 ) && 
        		( aimPitchT > keyholeBot || aimPitchAdd > 0 ) )
        		aimPitch = aimPitchT;
        } else {
        	aimPitch = lastAimPitch + aimPitchAdd;
        }
		if( aimPitch > 90.0f )
			aimPitch = 90.0f;
		else if( aimPitch < -90.0f )
			aimPitch = -90.0f;

    	headYaw = this.mc.headTracker.getHeadYawDegrees();
    	if( holdCenter ) {
    		aimYaw = headYaw;
    	} else if( this.mc.vrSettings.aimKeyholeWidthDegrees > 0 ) {
        	float keyholeYawWidth = this.mc.vrSettings.aimKeyholeWidthDegrees/2;
        	float keyholeYawLeft = headYaw - keyholeYawWidth;
        	float keyholeYawRight = headYaw + keyholeYawWidth;
        	
            float aimYawT = lastAimYaw + aimYawAdd;
            if( aimYawT > keyholeYawRight )
            {
            	if( aimYawAdd > 0 )
            		bodyYaw = lastBodyYaw + 0.75f*aimYawAdd;
            	else
            		aimYaw = aimYawT;
            }
            else if( aimYaw < keyholeYawLeft )
            {
            	if( aimYawAdd < 0 )
            		bodyYaw = lastBodyYaw + 0.75f*aimYawAdd;
            	else
            		aimYaw = aimYawT;
            } else {
            	aimYaw = aimYawT;
            }
            bodyYaw %= 360;
        } else {
        	aimYaw = 0;
            bodyYaw = lastBodyYaw + aimYawAdd;
            bodyYaw %= 360;
        }
	}
}

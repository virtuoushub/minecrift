/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.control;

import net.minecraft.src.Minecraft;

import com.mtbs3d.minecrift.settings.VRSettings;

public class JoystickAim {
	public float aimPitch = 0.0f;
	public float aimYaw   = 0.0f;
	public float bodyYaw  = 0.0f;
	float lastAimPitch = 0.0f;
	float lastAimYaw   = 0.0f;
	float lastBodyYaw  = 0.0f;

	float aimPitchRate = 0.0f;
	float aimYawRate = 0.0f;
	boolean holdCenter = false;
	Minecraft mc = Minecraft.getMinecraft();
	private float headYaw;
	private float headPitch;
	public static class JoyAimPitchBinding extends ControlBinding {

		public JoyAimPitchBinding() {
			super("Aim Up/Down","axis.updown");
		}

		@Override
		public void setValue(float value) {
			selectedJoystickMode.updateJoyY(value);
		}

		@Override
		public void setState(boolean state) {
		}
	}

	public static class JoyAimYawBinding extends ControlBinding {

		public JoyAimYawBinding() {
			super("Aim Left/Right","axis.leftright");
		}

		@Override
		public void setValue(float value) {
			selectedJoystickMode.updateJoyX(value);
		}

		@Override
		public void setState(boolean state) {
		}
		
	}
	
	public static class JoyAimCenterBinding extends ControlBinding {
		
		public JoyAimCenterBinding() {
			super("Center Aim (hold)","key.aimcenter");
		}

		@Override
		public void setValue(float value) {
			setState( Math.abs(value)> 0.1 );
		}

		@Override
		public void setState(boolean state) {
			selectedJoystickMode.setHold( state );
		}
		
	}
	
	public static JoystickAim selectedJoystickMode;

	public void update( float partialTicks ) {
		
        float aimYawAdd = 2*aimYawRate * VRSettings.inst.joystickSensitivity * partialTicks;
        float aimPitchAdd = 2*aimPitchRate * VRSettings.inst.joystickSensitivity * partialTicks;

		
    	headPitch = this.mc.headTracker.getHeadPitchDegrees();
    	if( holdCenter ) {
        	aimPitch = headPitch;
    	} else if( this.mc.vrSettings.keyholeHeight > 0 )
        {
        	aimPitch = lastAimPitch + aimPitchAdd;
        	float keyholeBot = Math.max(-90,headPitch - this.mc.vrSettings.keyholeHeight /2);
        	float keyholeTop = Math.min(90,headPitch + this.mc.vrSettings.keyholeHeight /2);
        	if( aimPitch > keyholeTop )
        		aimPitch = keyholeTop;
        	if( aimPitch < keyholeBot )
        		aimPitch = keyholeBot;
        } else {
        	aimPitch = lastAimPitch + aimPitchAdd;
			if( aimPitch > 90.0f )
				aimPitch = 90.0f;
			else if( aimPitch < -90.0f )
				aimPitch = -90.0f;
        }

        if( this.mc.vrSettings.aimKeyholeWidthDegrees > 0 && !holdCenter )
        {
        	headYaw = this.mc.headTracker.getHeadYawDegrees();
        	float keyholeYawWidth = this.mc.vrSettings.aimKeyholeWidthDegrees/2;
        	float keyholeYawLeft = headYaw - keyholeYawWidth;
        	float keyholeYawRight = headYaw + keyholeYawWidth;
        	
        	//Keep mouse constrained to keyhole
            if( aimYaw > keyholeYawRight )
            	aimYaw = keyholeYawRight;
            else if ( aimYaw < keyholeYawLeft )
            	aimYaw = keyholeYawLeft;

            if( aimPitch != 90 && aimPitch != -90 && aimYawAdd != 0 )
            {
                aimYaw = lastAimYaw + aimYawAdd;
                if( aimYaw > keyholeYawRight )
                {
                	bodyYaw = lastBodyYaw + 0.75f*(aimYaw - keyholeYawRight);
                	aimYaw = keyholeYawRight;
                }
                else if( aimYaw < keyholeYawLeft )
                {
                	bodyYaw = lastBodyYaw + 0.75f*(aimYaw - keyholeYawLeft);
                	aimYaw = keyholeYawLeft;
                }
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

	public void setHold(boolean state) {
		holdCenter = state;
	}

	void updateJoyY( float joyStickValue) {
		aimPitchRate = joyStickValue;
	}

	void updateJoyX( float joyStickValue) {
		aimYawRate = joyStickValue;
	}

	public float getAimPitch() {
		return aimPitch;
	}
	
	public float getAimYaw() {
		return bodyYaw + aimYaw;
	}
	
	public float getBodyYaw() {
		return bodyYaw;
	}

	public void updateTick() {
		update(1.0f);
		lastAimPitch = aimPitch; 
		lastAimYaw = aimYaw; 
		lastBodyYaw = bodyYaw;
	}
}

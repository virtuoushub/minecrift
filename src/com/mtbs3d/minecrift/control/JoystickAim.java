package com.mtbs3d.minecrift.control;

import com.mtbs3d.minecrift.settings.VRSettings;

public class JoystickAim {
	float aimPitch = 0.0f;
	float aimYaw   = 0.0f;

	void updateJoyY( float joyStickValue) {
		aimPitch += joyStickValue * VRSettings.inst.joystickSensitivity;
		if( aimPitch > 90.0f )
			aimPitch = 90.0f;
		else if( aimPitch < -90.0f )
			aimPitch = -90.0f;
	}

	void updateJoyX( float joyStickValue) {
		aimYaw += joyStickValue * VRSettings.inst.joystickSensitivity;
		aimYaw %= 360.0f;
	}

	public float getAimPitch() {
		return aimPitch;
	}
	
	public float getAimYaw() {
		return aimYaw;
	}
}

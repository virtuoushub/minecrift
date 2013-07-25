package com.mtbs3d.minecrift.control;

import com.mtbs3d.minecrift.settings.VRSettings;

public class JoystickAim {
	public float aimPitch = 0.0f;
	public float aimYaw   = 0.0f;
	float lastAimPitch = 0.0f;
	float lastAimYaw   = 0.0f;
	float aimPitchRate = 0.0f;
	float aimYawRate = 0.0f;
	float lastTicks = 0.0f;
	public static class JoyAimPitchBinding extends ControlBinding {

		public JoyAimPitchBinding() {
			super("Aim Up/Down");
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
			super("Aim Left/Right");
		}

		@Override
		public void setValue(float value) {
			selectedJoystickMode.updateJoyX(value);
		}

		@Override
		public void setState(boolean state) {
		}
		
	}
	
	public static JoystickAim selectedJoystickMode;

	public void update( float partialTicks ) {
		if( partialTicks < lastTicks) {
			//Next tick
			lastAimPitch = aimPitch + aimPitchRate * VRSettings.inst.joystickSensitivity*(1-lastTicks); 
			lastAimYaw = aimYaw + aimYawRate * VRSettings.inst.joystickSensitivity*(1-lastTicks); 
		}
		lastTicks = partialTicks;
		aimPitch = lastAimPitch + aimPitchRate * VRSettings.inst.joystickSensitivity * partialTicks;
		if( aimPitch > 90.0f )
			aimPitch = 90.0f;
		else if( aimPitch < -90.0f )
			aimPitch = -90.0f;
		
		aimYaw += aimYawRate * VRSettings.inst.joystickSensitivity * partialTicks;
		aimYaw %= 360.0f;
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
		return aimYaw;
	}
}

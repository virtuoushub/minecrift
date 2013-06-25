/**
 * Copyright 2013 Mark Browning
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Vec3;

import com.mtbs3d.minecrift.api.BasePlugin;
import com.mtbs3d.minecrift.api.ICenterEyePositionProvider;

/**
 * "None" head position plugin
 * @author mabrowning
 *
 */
public class NullCenterEyePosition extends BasePlugin implements ICenterEyePositionProvider {

	private Vec3 headPos;

	@Override
	public String getID() {
		return "null-pos";
	}

	@Override
	public String getName() {
		return "None";
	}

	@Override
	public String getInitializationStatus() {
		return "";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public boolean init(File nativeDir) {
		return true;
	}

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public void poll() {
	}

	@Override
	public void destroy() {
	}

	@Override
	public boolean isCalibrated() {
		return true;
	}

	@Override
	public String getCalibrationStep() {
		return "";
	}

	//Basic neck model:
	@Override
	public void update(float yawHeadDegrees, float pitchHeadDegrees, float rollHeadDegrees,
                       float worldYawOffsetDegrees, float worldPitchOffsetDegrees, float worldRollOffsetDegrees)
    {
        float cameraYaw = (worldYawOffsetDegrees + yawHeadDegrees ) % 360;
        headPos = Vec3.fakePool.getVecFromPool(0, Minecraft.getMinecraft().gameSettings.neckBaseToEyeHeight, -Minecraft.getMinecraft().gameSettings.eyeProtrusion);
		headPos.rotateAroundZ( rollHeadDegrees  * PIOVER180 );
		headPos.rotateAroundX( pitchHeadDegrees * PIOVER180 );
        headPos.rotateAroundY( -cameraYaw * PIOVER180 );
	}

	@Override
	public Vec3 getCenterEyePosition() {
		return headPos;
	}


	@Override
	public void resetOrigin() {
	}

	@Override
	public void setPrediction(float delta, boolean enable) {
	}

	@Override
	public void resetOriginRotation() {
	}

}

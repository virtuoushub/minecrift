package com.mtbs3d.minecrift;


import net.minecraft.client.Minecraft;
import net.minecraft.src.Vec3;

import com.mtbs3d.minecrift.api.IBasePlugin;
import com.mtbs3d.minecrift.api.IHMDInfo;
import com.mtbs3d.minecrift.api.IHeadPositionProvider;
import com.mtbs3d.minecrift.api.IOrientationProvider;

import de.fruitfly.ovr.OculusRift;

public class MCOculus extends OculusRift //OculusRift does most of the heavy lifting 
	implements IOrientationProvider, IHeadPositionProvider, IBasePlugin, IHMDInfo {
	
	Vec3 headPos;
	@Override
	public String getName() {
		return "Oculus";
	}

	@Override
	public String getID() {
		return "oculus";
	}
	
	//Basic neck model:
	@Override
	public void update(float yawHeadDegrees, float pitchDegrees, float rollDegrees) {
		headPos = Vec3.fakePool.getVecFromPool(0, Minecraft.getMinecraft().gameSettings.neckBaseToEyeHeight, -Minecraft.getMinecraft().gameSettings.eyeProtrusion);
		headPos.rotateAroundZ( rollDegrees  * PIOVER180 );
		headPos.rotateAroundX( pitchDegrees * PIOVER180 );
	}

	@Override
	public Vec3 getHeadPosition() {
		return headPos;
	}

	@Override
	public void resetOrigin() { /*no-op*/ }


}

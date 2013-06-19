package com.mtbs3d.minecrift;


import net.minecraft.client.Minecraft;
import net.minecraft.src.Vec3;

import com.mtbs3d.minecrift.api.IBasePlugin;
import com.mtbs3d.minecrift.api.IHMDInfo;
import com.mtbs3d.minecrift.api.ICenterEyePositionProvider;
import com.mtbs3d.minecrift.api.IOrientationProvider;

import de.fruitfly.ovr.OculusRift;

public class MCOculus extends OculusRift //OculusRift does most of the heavy lifting 
	implements IOrientationProvider, ICenterEyePositionProvider, IBasePlugin, IHMDInfo {
	
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
	public void resetOrigin() { /*no-op*/ }


    @Override
    public void beginAutomaticCalibration() {
    }

    @Override
    public void updateAutomaticCalibration() {
    }

    @Override
    public boolean isCalibrated() {
        return true;
    }
}

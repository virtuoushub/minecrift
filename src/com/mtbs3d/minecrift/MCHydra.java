/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;

import java.io.File;
import com.sixense.ControllerData;
import com.sixense.Sixense;
import com.sixense.utils.ControllerManager;
import com.sixense.utils.ManagerCallback;
import com.sixense.utils.enums.EnumSetupStep;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GameSettings;
import net.minecraft.src.Vec3;

import com.mtbs3d.minecrift.api.BasePlugin;
import com.mtbs3d.minecrift.api.ICenterEyePositionProvider;
import com.mtbs3d.minecrift.api.IOrientationProvider;

/**
 * @author Mark Browning
 *
 */
public class MCHydra extends BasePlugin implements ICenterEyePositionProvider, IOrientationProvider {

    ControllerManager cm;
    public static ControllerData[] newData = {new ControllerData(), new ControllerData(), new ControllerData(), new ControllerData()};

    float headPosX = 0.0f; // in meter, relative to head yaw
    float headPosY = 0.0f;
    float headPosZ = 0.0f;

    float originX = 0.0f; // in meter, relative to hydra base station
    float originY = 0.0f;
    float originZ = 0.0f;

    float headYawOrigin = 0.0f; //in degrees, not currently used (would be nice to support)

    boolean hydraRunning = false;
	private boolean hydraInitialized = false;
	private String initStatus;

	private static boolean libraryLoaded = false;
	
    private boolean resetOrigin = true;

	//latest data
	
	private float cont1PosX;
	private float cont1PosY;
	private float cont1PosZ;
	private float cont2PosX;
	private float cont2PosY;
	private float cont2PosZ;

	private float cont1Yaw;
	private float cont1Pitch;
	private float cont1Roll;

    private final float SCALE = 0.001f; //mm -> m
    private float userScale = SCALE;

    // Previously +X 1.0f, -Y -1.0f, +Z 1.0f
    private final float XDIRECTION = 1.0f;    // +X
    private final float YDIRECTION = 1.0f;    // +Y
    private final float ZDIRECTION = 1.0f;    // +Z

	public MCHydra()
	{
		super();
		pluginID = "hydra";
		pluginName = "Razer Hydra";
		
	}
	@Override
	public String getInitializationStatus() {
		return initStatus;
	}

	@Override
	public String getVersion() {
		return "0.28";
	}

	@Override
	public boolean init(File nativeDir) {
		hydraInitialized = libraryLoaded;

		if( !libraryLoaded )
		{
			try {
				libraryLoaded = Sixense.LoadLibrary(nativeDir);
			}
			catch( UnsatisfiedLinkError e )
			{
				initStatus = e.getMessage();
			}
		}

		if( libraryLoaded )
		{
			init();
		}
		return hydraInitialized;
	}

	@Override
	public boolean init() {
		Sixense.init();
		cm = ControllerManager.getInstance();
		cm.setGameType(com.sixense.utils.enums.EnumGameType.ONE_PLAYER_TWO_CONTROLLER);
		cm.registerSetupCallback(new ManagerCallback() {
			@Override
			public void onCallback(EnumSetupStep step) {
				initStatus = cm.getStepString();
			}
		});

		Sixense.setActiveBase(0);
		Sixense.setFilterEnabled(true); // TODO: Make filter optional
		
		hydraInitialized = true;
		return isInitialized(); //no re-init method
	}

	@Override
	public boolean isInitialized() {
		return hydraInitialized;
	}

	@Override
	public void poll() {
        // Poll hydras, get position information in metres
        Sixense.getAllNewestData(newData);

        float[] controller1Pos = newData[0].pos;
		cont1Yaw   = newData[0].yaw;
		cont1Pitch = newData[0].pitch;
		cont1Roll  = newData[0].roll;

        userScale = SCALE * Minecraft.getMinecraft().gameSettings.posTrackHydraDistanceScale;

        cont1PosX = userScale * controller1Pos[0] * XDIRECTION;
        cont1PosY = userScale * controller1Pos[1] * YDIRECTION;
        cont1PosZ = userScale * controller1Pos[2] * ZDIRECTION;

        float[] controller2Pos = newData[1].pos;
        cont2PosX = userScale * controller2Pos[0] * XDIRECTION;
        cont2PosY = userScale * controller2Pos[1] * YDIRECTION;
        cont2PosZ = userScale * controller2Pos[2] * ZDIRECTION;

        cm.update(newData);
        EnumSetupStep step = cm.getCurrentStep();
        switch ( step )
        {
            case P1C2_IDLE:
                hydraRunning = true;
                break;
            default:
                hydraRunning = false;
                if (step != null)
                    System.out.println("HYDRA: " + step.toString());
                break;
        }
	}

	@Override
	public void destroy() {
		Sixense.exit();
        hydraRunning = false;
        hydraInitialized = false;
        initStatus = "Not initialised";
        cm = null;
	}

	@Override
	public void update(float yawHeadDegrees, float pitchHeadDegrees, float rollHeadDegrees,
                       float worldYawOffsetDegrees, float worldPitchOffsetDegrees, float worldRollOffsetDegrees)
    {
        if (!hydraInitialized || !hydraRunning)
        {
            headPosX = 0.0f;
            headPosY = 0.0f;
            headPosZ = 0.0f;
            return;
        }

        float contPosX = 0.0f;
        float contPosY = 0.0f;
        float contPosZ = 0.0f;

        if (Minecraft.getMinecraft().gameSettings.posTrackHydraLoc != GameSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT)
        {
            // Using a single controller. Select controller.
            if (Minecraft.getMinecraft().gameSettings.posTrackHydraUseController1)
            {
                contPosX = cont1PosX;
                contPosY = cont1PosY;
                contPosZ = cont1PosZ;
            }
            else
            {
                contPosX = cont2PosX;
                contPosY = cont2PosY;
                contPosZ = cont2PosZ;
            }
        }

        float X = 0.0f;
        float Y = 0.0f;
        float Z = 0.0f;
        float hydraXOffset = Minecraft.getMinecraft().gameSettings.posTrackHydraOffsetX;
        float hydraYOffset = Minecraft.getMinecraft().gameSettings.posTrackHydraOffsetY;
        float hydraZOffset = Minecraft.getMinecraft().gameSettings.posTrackHydraOffsetZ;

        if (Minecraft.getMinecraft().gameSettings.posTrackHydraDebugCentreEyePos)
        {
            debugCentreEyePosition(userScale, yawHeadDegrees, pitchHeadDegrees, rollHeadDegrees, hydraXOffset, hydraYOffset, hydraZOffset,
                    cont1PosX, cont1PosY, cont1PosZ, cont2PosX, cont2PosY, cont2PosZ);
        }

        switch( Minecraft.getMinecraft().gameSettings.posTrackHydraLoc)
        {
            case GameSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT:
            {
                // 2 hydras are used for positional tracking, strapped to the left and right sides of the HMD respectively
                // Get average centre eye position.

                X = (cont1PosX + cont2PosX) / 2.0f;
                Y = (cont1PosY + cont2PosY) / 2.0f;
                Z = (cont1PosZ + cont2PosZ) / 2.0f;
                break;
            }
            case GameSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT:
            case GameSettings.POS_TRACK_HYDRA_LOC_HMD_TOP:
            case GameSettings.POS_TRACK_HYDRA_LOC_HMD_RIGHT:
            {
                // 1 hydra is used for positional tracking, strapped to the left, right or top side of the HMD
                // Get centre eye position.
                Vec3 correctionToCentreEyePosition = Vec3.fakePool.getVecFromPool(hydraXOffset, hydraYOffset, hydraZOffset);

                correctionToCentreEyePosition.rotateAroundX((float)Math.toRadians(-pitchHeadDegrees));
                correctionToCentreEyePosition.rotateAroundY((float)Math.toRadians(-yawHeadDegrees));
                correctionToCentreEyePosition.rotateAroundZ((float)Math.toRadians(-rollHeadDegrees));

                X = contPosX + (float)correctionToCentreEyePosition.xCoord;
                Y = contPosY + (float)correctionToCentreEyePosition.yCoord;
                Z = contPosZ + (float)correctionToCentreEyePosition.zCoord;
                break;
            }
//            case GameSettings.POS_TRACK_HYDRA_LOC_DIRECT:
//            {
//                // 1 hydra is used for positional tracking. The hydra's location is used directly. Useful for basic tracking
//                // by sticking the hydra down the front of your t-shirt!
//
//                X = contPosX;
//                Y = contPosY;
//                Z = contPosZ;
//
//    //            X = contPosX - originX;
//    //            Z = contPosZ - originZ;
//    //            headPosY = contPosY - originY;
//    //            float cos = MathHelper.cos(yawHeadDegrees * PIOVER180);
//    //            float sin = MathHelper.sin(yawHeadDegrees * PIOVER180);
//    //            headPosX =  X * cos + Z * sin;
//    //            headPosZ = -X * sin + Z * cos;
//                break;
//            }
//            case GameSettings.POS_TRACK_HYDRA_LOC_BACK_OFF_HEAD_UNDER_TOP_STRAP:
//            {
//                // TODO: Implement
//
//                // 1 hydra is used for positional tracking, strapped to the back of the head under the top strap
//                // Get centre (eye) position.
//                // Head rotates around neck base. HMD is 'eyeProtrusion' metres directly in front of neck base. Hydra sensor is
//                // 'neckBaseToBackOfHead' metres directly behind neck base. Rotate your head left, the hydra goes right,
//                // so we need to normalise for translation due to head rotation.
//
//                float neckBaseToBackOfHead = 0.03f;
//
//                X = contPosX;
//                Y = contPosY;
//                Z = contPosZ;
//                break;
//            }
        }

        if (resetOrigin)
        {
            // Save the origin position values
            originX = X;
            originY = Y;
            originZ = Z;

            resetOrigin = false;
        }

        float offsetX = X - originX;
        float offsetY = Y - originY;
        float offsetZ = Z - originZ;

        //System.out.println(String.format("Positional Track: Centre Eye pos (offset from origin):   (l/r)x=%.3fcm, (up/down)y=%.3fcm, (in/out)z=%.3fcm", new Object[] {Float.valueOf(offsetX * 100.0f), Float.valueOf(offsetY * 100.0f), Float.valueOf(offsetZ * 100.0f)}));

        // Rotate the centre eye position around any world yaw offset
        Vec3 posRotated = Vec3.fakePool.getVecFromPool(offsetX, offsetY, offsetZ);
        posRotated.rotateAroundY((float)Math.toRadians(-worldYawOffsetDegrees)); // TODO: Account for look / move decoupled?

        headPosX = (float)posRotated.xCoord;
        headPosY = (float)posRotated.yCoord + Minecraft.getMinecraft().gameSettings.neckBaseToEyeHeight;
        headPosZ = (float)posRotated.zCoord - Minecraft.getMinecraft().gameSettings.eyeProtrusion;
	}

	@Override
	public Vec3 getCenterEyePosition() {
		return Vec3.fakePool.getVecFromPool(headPosX, headPosY, headPosZ);
	}

	@Override
	public void resetOrigin() {
		resetOrigin = true;
	}

	@Override
	public void setPrediction(float delta, boolean enable) {
        Sixense.setFilterEnabled(enable);
	}

	@Override
	public float getYawDegrees_LH() {
		return cont1Yaw;
	}

	@Override
	public float getPitchDegrees_LH() {
		return cont1Pitch;
	}

	@Override
	public float getRollDegrees_LH() {
		return cont1Roll;
	}

    protected void debugCentreEyePosition(float scale, float yaw, float pitch, float roll, float hydraXOffset, float hydraYOffset, float hydraZOffset,
                                          float cont1PosX, float cont1PosY, float cont1PosZ, float cont2PosX, float cont2PosY, float cont2PosZ)
    {
        float tempHmdPosX = 0.0f;
        float tempHmdPosY = 0.0f;
        float tempHmdPosZ = 0.0f;

        System.out.println(String.format("Positional Track: Orientation         : Yaw=%.3f, Pitch=%.3f, Roll=%.3f", new Object[] {Float.valueOf(yaw), Float.valueOf(pitch), Float.valueOf(roll)}));

        // The centre eye position returned by all off these methods should agree with each other...

        switch(Minecraft.getMinecraft().gameSettings.posTrackHydraLoc)
        {
            case GameSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT:
            {
                // 2 hydras are used for positional tracking, strapped to the left and right sides of the HMD respectively
                // Get average centre (eye) position.

                tempHmdPosX = ((cont1PosX + cont2PosX) / 2.0f);
                tempHmdPosY = ((cont1PosY + cont2PosY) / 2.0f);
                tempHmdPosZ = ((cont1PosZ + cont2PosZ) / 2.0f);

                //System.out.println(String.format("Positional Track: Centre Eye pos (L&R): (l/r)x=%.3fcm, (up/down)y=%.3fcm, (in/out)z=%.3fcm", new Object[] {Float.valueOf(tempHmdPosX * 100.0f), Float.valueOf(tempHmdPosY * 100.0f), Float.valueOf(tempHmdPosZ * 100.0f)}));
                break;
            }

            case GameSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT:
            {
                // 1 hydra is used for positional tracking, strapped to the left side of the HMD
                // Get centre (eye) position.

                tempHmdPosX = cont1PosX;
                tempHmdPosY = cont1PosY;
                tempHmdPosZ = cont1PosZ;

                Vec3 correctionToCentreEyePosition = Vec3.fakePool.getVecFromPool(hydraXOffset, hydraYOffset, hydraZOffset);

                correctionToCentreEyePosition.rotateAroundX((float)Math.toRadians(-pitch));
                correctionToCentreEyePosition.rotateAroundY((float)Math.toRadians(-yaw));
                correctionToCentreEyePosition.rotateAroundZ((float)Math.toRadians(-roll));

                tempHmdPosX = tempHmdPosX + (float)correctionToCentreEyePosition.xCoord;
                tempHmdPosY = tempHmdPosY + (float)correctionToCentreEyePosition.yCoord;
                tempHmdPosZ = tempHmdPosZ + (float)correctionToCentreEyePosition.zCoord;

                System.out.println(String.format("Positional Track: Centre Eye pos (L):   (l/r)x=%.3fcm, (up/down)y=%.3fcm, (in/out)z=%.3fcm", new Object[] {Float.valueOf(tempHmdPosX * 100.0f), Float.valueOf(tempHmdPosY * 100.0f), Float.valueOf(tempHmdPosZ * 100.0f)}));
                break;
            }
            case GameSettings.POS_TRACK_HYDRA_LOC_HMD_RIGHT:
            {
                // 1 hydra is used for positional tracking, strapped to the right side of the HMD
                // Get centre (eye) position.

                tempHmdPosX = cont2PosX;
                tempHmdPosY = cont2PosY;
                tempHmdPosZ = cont2PosZ;

                Vec3 correctionToCentreEyePosition = Vec3.fakePool.getVecFromPool(hydraXOffset, hydraYOffset, hydraZOffset);

                correctionToCentreEyePosition.rotateAroundX((float)Math.toRadians(-pitch));
                correctionToCentreEyePosition.rotateAroundY((float)Math.toRadians(-yaw));
                correctionToCentreEyePosition.rotateAroundZ((float)Math.toRadians(-roll));

                tempHmdPosX = tempHmdPosX + (float)correctionToCentreEyePosition.xCoord;
                tempHmdPosY = tempHmdPosY + (float)correctionToCentreEyePosition.yCoord;
                tempHmdPosZ = tempHmdPosZ + (float)correctionToCentreEyePosition.zCoord;

                //System.out.println(String.format("Positional Track: Centre Eye pos (R):   (l/r)x=%.3fcm, (up/down)y=%.3fcm, (in/out)z=%.3fcm", new Object[] {Float.valueOf(tempHmdPosX * 100.0f), Float.valueOf(tempHmdPosY * 100.0f), Float.valueOf(tempHmdPosZ * 100.0f)}));
                break;
            }

            case GameSettings.POS_TRACK_HYDRA_LOC_HMD_TOP:
            {
                // 1 hydra is used for positional tracking, strapped to the top of the HMD
                // Get centre (eye) position.

                tempHmdPosX = cont2PosX;
                tempHmdPosY = cont2PosY;
                tempHmdPosZ = cont2PosZ;

                Vec3 correctionToCentreEyePosition = Vec3.fakePool.getVecFromPool(hydraXOffset, hydraYOffset, hydraZOffset);

                correctionToCentreEyePosition.rotateAroundX((float)Math.toRadians(-pitch));
                correctionToCentreEyePosition.rotateAroundY((float)Math.toRadians(-yaw));
                correctionToCentreEyePosition.rotateAroundZ((float)Math.toRadians(-roll));

                tempHmdPosX = tempHmdPosX + (float)correctionToCentreEyePosition.xCoord;
                tempHmdPosY = tempHmdPosY + (float)correctionToCentreEyePosition.yCoord;
                tempHmdPosZ = tempHmdPosZ + (float)correctionToCentreEyePosition.zCoord;

                System.out.println(String.format("Positional Track: Centre Eye pos (Top): (l/r)x=%.3fcm, (up/down)y=%.3fcm, (in/out)z=%.3fcm", new Object[] {Float.valueOf(tempHmdPosX * 100.0f), Float.valueOf(tempHmdPosY * 100.0f), Float.valueOf(tempHmdPosZ * 100.0f)}));
                break;
            }
        }
    }
	@Override
	public void beginAutomaticCalibration() { /*no-op*/ }
	@Override
	public void updateAutomaticCalibration() {/*no-op*/ }
	@Override
	public boolean isCalibrated() { return true;}
}

/**
 * Copyright 2013 Mark Browning
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;

import java.io.File;
import com.sixense.ControllerData;
import com.sixense.Sixense;
import com.sixense.utils.ControllerManager;
import com.sixense.utils.ManagerCallback;
import com.sixense.utils.enums.EnumSetupStep;

import net.minecraft.src.MathHelper;
import net.minecraft.src.Vec3;

import com.mtbs3d.minecrift.api.BasePlugin;
import com.mtbs3d.minecrift.api.IHeadPositionProvider;
import com.mtbs3d.minecrift.api.IOrientationProvider;

/**
 * @author Mark Browning
 *
 */
public class MCHydra extends BasePlugin implements IHeadPositionProvider, IOrientationProvider {

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
	
	
	//latest data
	
	private float latestHeadYaw;
	private float cont1PosX;
	private float cont1PosY;
	private float cont1PosZ;
	/*
	private float cont2PosX;
	private float cont2PosY;
	private float cont2PosZ;
	*/

	private float cont1Yaw;
	private float cont1Pitch;
	private float cont1Roll;
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

		if( libraryLoaded && cm == null )
		{
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
		}
		return hydraInitialized;
	}

	@Override
	public boolean init() {
		return isInitialized(); //no re-init method
	}

	@Override
	public boolean isInitialized() {
		return hydraInitialized;
	}

	@Override
	public void poll() {
        // Poll hydras, get average eye-position
        Sixense.getAllNewestData(newData);

        float[] controller1Pos = newData[0].pos;
		cont1Yaw   = newData[0].yaw;
		cont1Pitch = newData[0].pitch;
		cont1Roll  = newData[0].roll;

		float scale = 0.001f;
        cont1PosX = scale * controller1Pos[0];
        cont1PosY = scale * controller1Pos[1];
        cont1PosZ = scale * controller1Pos[2];

        /*
        float[] controller2Pos = newData[1].pos;
        cont2PosX = controller2Pos[0];
        cont2PosY = controller2Pos[1];
        cont2PosZ = controller2Pos[2];
        */
        
        cm.update(newData);
        EnumSetupStep step = cm.getCurrentStep();
        switch ( step )
        {
            case P1C2_IDLE:
                hydraRunning = true;
                break;
            default:
                hydraRunning = false;
                break;
        }
	}

	@Override
	public void destroy() {
		Sixense.exit();
	}

	@Override
	public void update(float yawHeadDegrees, float pitchDegrees, float rollDegrees) {
		latestHeadYaw = yawHeadDegrees;
		float X = cont1PosX - originX;
		float Z = cont1PosZ - originZ;
		headPosY = cont1PosY - originY;
		float cos = MathHelper.cos(yawHeadDegrees * PIOVER180);
		float sin = MathHelper.sin(yawHeadDegrees * PIOVER180);
		headPosX =  X * cos + Z * sin;
		headPosZ = -X * sin + Z * cos;
	}

	@Override
	public Vec3 getHeadPosition() {
		return Vec3.fakePool.getVecFromPool(headPosX, headPosY, headPosZ);
	}

	@Override
	public void resetOrigin() {
		originX = cont1PosX;
		originY = cont1PosY;
		originZ = cont1PosZ;
		headYawOrigin = latestHeadYaw;
		update( headYawOrigin, 0, 0 );
	}

	@Override
	public void setPrediction(float delta, boolean enable) {
        //Sixense.setFilterEnabled(enable);
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

}

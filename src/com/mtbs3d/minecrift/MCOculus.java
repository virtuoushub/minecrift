/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;


import com.mtbs3d.minecrift.api.IBasePlugin;
import com.mtbs3d.minecrift.api.IHMDInfo;
import com.mtbs3d.minecrift.api.IOrientationProvider;

import de.fruitfly.ovr.OculusRift;

public class MCOculus extends OculusRift //OculusRift does most of the heavy lifting 
	implements IOrientationProvider, IBasePlugin, IHMDInfo {

    public static final int NOT_CALIBRATING = 0;
    public static final int CALIBRATE_AWAITING_ORIGIN = 1;
    public static final int CALIBRATE_AT_ORIGIN = 2;
    public static final int CALIBRATE_AWAITING_MAG_CAL = 3;
    public static final int CALIBRATE_MAG_CAL_COOLDOWN = 4;

    private boolean isCalibrated = false;
    private long coolDownStart = 0;
    private int calibrationStep = NOT_CALIBRATING;

	@Override
	public String getName() {
		return "Oculus";
	}

	@Override
	public String getID() {
		return "oculus";
	}
	
	@Override
	public void resetOrigin() {
        _setCalibrationReference();
    }

    @Override
    public void beginAutomaticCalibration()
    {
        processCalibration();
    }

    @Override
    public void updateAutomaticCalibration()
    {
        processCalibration();
    }

    @Override
    public boolean isCalibrated() {
        if (!isInitialized())
            return true;  // Return true if not initialised

        return isCalibrated;
    }

	@Override
	public String getCalibrationStep()
    {
        String step = "";

        switch (calibrationStep)
        {
            case CALIBRATE_AWAITING_ORIGIN:
            {
                step = "Look ahead and press SPACEBAR";
                break;
            }
            case CALIBRATE_AT_ORIGIN:
            case CALIBRATE_AWAITING_MAG_CAL:
            {
                step = "Slowly look left, right, up";
                break;
            }
            case CALIBRATE_MAG_CAL_COOLDOWN:
            {
                step = "Done!";
                break;
            }
        }

        return step;
	}

    @Override
    public void eventNotification(int eventId)
    {
        if (eventId == IOrientationProvider.EVENT_SET_ORIGIN)
        {
            resetOrigin();
            if (calibrationStep == CALIBRATE_AWAITING_ORIGIN)
            {
                calibrationStep = CALIBRATE_AT_ORIGIN;
                processCalibration();
            }
        }
    }

    private void processCalibration()
    {
        switch (calibrationStep)
        {
            case NOT_CALIBRATING:
            {
                calibrationStep = CALIBRATE_AWAITING_ORIGIN;
                isCalibrated = false;
                break;
            }
            case CALIBRATE_AT_ORIGIN:
            {
                _beginAutomaticCalibration();
                calibrationStep = CALIBRATE_AWAITING_MAG_CAL;
                break;
            }
            case CALIBRATE_AWAITING_MAG_CAL:
            {
                _updateAutomaticCalibration();
                if (_isCalibrated())
                {
                    coolDownStart = System.currentTimeMillis();
                    calibrationStep = CALIBRATE_MAG_CAL_COOLDOWN;
                }
                break;
            }
            case CALIBRATE_MAG_CAL_COOLDOWN:
            {
                if ((System.currentTimeMillis() - coolDownStart) > 1000L)
                {
                    calibrationStep = NOT_CALIBRATING;
                    isCalibrated = true;
                }
                break;
            }
        }
    }
}

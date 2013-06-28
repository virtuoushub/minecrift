/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;


import com.mtbs3d.minecrift.api.*;

import de.fruitfly.ovr.OculusRift;

public class MCOculus extends OculusRift //OculusRift does most of the heavy lifting 
	implements IOrientationProvider, IBasePlugin, IHMDInfo, IEventNotifier, IEventListener {

    public static final int NOT_CALIBRATING = 0;
    public static final int CALIBRATE_AWAITING_FIRST_ORIGIN = 1;
    public static final int CALIBRATE_AT_FIRST_ORIGIN = 2;
    public static final int CALIBRATE_AWAITING_MAG_CAL = 3;
    public static final int CALIBRATE_MAG_CAL_WAIT = 4;
    public static final int CALIBRATE_AWAITING_SECOND_ORIGIN = 5;
    public static final int CALIBRATE_AT_SECOND_ORIGIN = 6;
    public static final int CALIBRATE_COOLDOWN = 7;

    public static final long COOLDOWNTIME_MS = 1000L;
    public static final long MAG_CAL_REPOLL_TIME_MS = 200L;

    private boolean isCalibrated = false;
    private long coolDownStart = 0L;
    private long lastUpdateAt = 0L;
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
        if (isInitialized() && _isCalibrated())
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
            case CALIBRATE_AWAITING_FIRST_ORIGIN:
            case CALIBRATE_AWAITING_SECOND_ORIGIN:
            {
                step = "Look ahead and press SPACEBAR";
                break;
            }
            case CALIBRATE_AT_FIRST_ORIGIN:
            case CALIBRATE_AWAITING_MAG_CAL:
            case CALIBRATE_MAG_CAL_WAIT:
            {
                step = "Slowly look left, right, up";
                break;
            }
            case CALIBRATE_COOLDOWN:
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
        switch (eventId)
        {
            case IOrientationProvider.EVENT_ORIENTATION_AT_ORIGIN:
            {
                if (calibrationStep == CALIBRATE_AWAITING_FIRST_ORIGIN)
                {
                    calibrationStep = CALIBRATE_AT_FIRST_ORIGIN;
                    processCalibration();
                }
                else if (calibrationStep == CALIBRATE_AWAITING_SECOND_ORIGIN)
                {
                    calibrationStep = CALIBRATE_AT_SECOND_ORIGIN;
                    processCalibration();
                }
                break;
            }
            case IBasePlugin.EVENT_CALIBRATION_SET_ORIGIN:
            {
                resetOrigin();
            }
        }
    }

    @Override
    public synchronized void registerListener(IEventListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public synchronized void notifyListeners(int eventId)
    {
        for (IEventListener listener : listeners)
        {
            if (listener != null)
                listener.eventNotification(eventId);
        }
    }

    private void processCalibration()
    {
        switch (calibrationStep)
        {
            case NOT_CALIBRATING:
            {
                calibrationStep = CALIBRATE_AWAITING_FIRST_ORIGIN;
                isCalibrated = false;
                break;
            }
            case CALIBRATE_AT_FIRST_ORIGIN:
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
                    lastUpdateAt = 0;
                    calibrationStep = CALIBRATE_AWAITING_SECOND_ORIGIN;
                }
                else
                {
                    lastUpdateAt = System.currentTimeMillis();
                    calibrationStep = CALIBRATE_MAG_CAL_WAIT;
                }
                break;
            }
            case CALIBRATE_MAG_CAL_WAIT:
            {
                if ((System.currentTimeMillis() - lastUpdateAt) > MAG_CAL_REPOLL_TIME_MS)
                {
                    lastUpdateAt = 0;
                    calibrationStep = CALIBRATE_AWAITING_MAG_CAL;
                }
                break;
            }
            case CALIBRATE_AT_SECOND_ORIGIN:
            {
                coolDownStart = System.currentTimeMillis();
                calibrationStep = CALIBRATE_COOLDOWN;
                resetOrigin();
                notifyListeners(IBasePlugin.EVENT_CALIBRATION_SET_ORIGIN);
                break;
            }
            case CALIBRATE_COOLDOWN:
            {
                if ((System.currentTimeMillis() - coolDownStart) > COOLDOWNTIME_MS)
                {
                    coolDownStart = 0;
                    calibrationStep = NOT_CALIBRATING;
                    isCalibrated = true;
                }
                break;
            }
        }
    }
}

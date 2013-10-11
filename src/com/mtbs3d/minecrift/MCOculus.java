/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;


import com.mtbs3d.minecrift.api.*;

import de.fruitfly.ovr.OculusRift;
import de.fruitfly.ovr.UserProfileData;

public class MCOculus extends OculusRift //OculusRift does most of the heavy lifting 
	implements IOrientationProvider, IBasePlugin, IHMDInfo, IEventNotifier, IEventListener {

    public static final int NOT_CALIBRATING = 0;
    public static final int CALIBRATE_AWAITING_FIRST_ORIGIN = 1;
    public static final int CALIBRATE_AT_FIRST_ORIGIN = 2;
    public static final int CALIBRATE_COOLDOWN = 7;

    public static final long COOLDOWNTIME_MS = 1000L;

    private boolean isCalibrated = false;
    private long coolDownStart = 0L;
    private int calibrationStep = NOT_CALIBRATING;
    private int MagCalSampleCount = 0;
    private boolean forceMagCalibration = false; // Don't force mag cal initially

    public static UserProfileData theProfileData = null;

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
    }

    @Override
    public void beginAutomaticCalibration()
    {
        if (isInitialized())
            processCalibration();
    }

    @Override
    public void updateAutomaticCalibration()
    {
        if (isInitialized())
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
            {
                step = "Look ahead and press SPACEBAR";
                break;
            }
            case CALIBRATE_AT_FIRST_ORIGIN:
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
                _reset();

                // Calibration of Mag cal is now handled solely by the Oculus config utility.

                MagCalSampleCount = 0;
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

	@Override
	public float getHeadYawDegrees() {
		return getYawDegrees_LH();
	}

	@Override
	public float getHeadPitchDegrees() {
		return getPitchDegrees_LH();
	}

	@Override
	public float getHeadRollDegrees() {
		return getRollDegrees_LH();
	}

    @Override
    public UserProfileData getProfileData()
    {
        UserProfileData userProfile = null;

        if (isInitialized())
        {
            userProfile = _getUserProfileData();
        }

        return userProfile;
    }

    @Override
    public String[] getUserProfiles()
    {
        String[] profileNames = null;

        if (isInitialized())
        {
            profileNames = _getUserProfiles();
        }

        return profileNames;
    }

    @Override
    public boolean loadProfile(String profileName)
    {
        if (!isInitialized())
            return false;

        return _loadUserProfile(profileName);
    }
}

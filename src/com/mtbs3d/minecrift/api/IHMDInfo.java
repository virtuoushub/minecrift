/**
 * Copyright 2013 Mark Browning
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.api;

import de.fruitfly.ovr.*;
import de.fruitfly.ovr.structs.FovTextureInfo;
import de.fruitfly.ovr.structs.GLConfig;
import de.fruitfly.ovr.structs.HmdDesc;
import de.fruitfly.ovr.structs.Sizei;

/**
 * Implement this class to inform Minecrift of distortion, resolution, and
 * chromatic aberration parameters
 * 
 * @author Mark Browning
 *
 */
public interface IHMDInfo extends IBasePlugin
{
	public HmdDesc getHMDInfo();
	//public SensorInfo getSensorInfo();

    /**
     * Sets/saves the IPD for use in the eyeRenderParams
     * 
     * @param ipd Interpupillary distance, in meters.
     */
    //public void setIPD(float ipd);

    /**
     * Gets the IPD
     * 
     * @return Interpupillary distance, in meters.
     */
   // public float getIPD();

    /* Gets the current user profile data */
    public UserProfileData getProfileData();

    /* Gets a list of all available user profiles */
    public String[] getUserProfiles();

    /* Select the named user profile */
    public boolean loadProfile(String profileName);

    /* Oculus latency tester info */
    //public float[] latencyTesterDisplayScreenColor();    // TODO:
    //public String latencyTesterGetResultsString();       // TODO:
}
/**
 * Copyright 2013 Mark Browning
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.api;

import de.fruitfly.ovr.*;

/**
 * Implement this class to inform Minecrift of distortion, resolution, and
 * chromatic aberration parameters
 * 
 * @author Mark Browning
 *
 */
public interface IHMDInfo extends IBasePlugin
{
	public HMDInfo getHMDInfo();
	public SensorInfo getSensorInfo();
    public EyeRenderParams getEyeRenderParams(int viewPortWidth, int viewPortHeight);
    public EyeRenderParams getEyeRenderParams(int viewPortX, int viewPortY, int viewPortWidth, int viewPortHeight, float nearClip, float farClip);
    public EyeRenderParams getEyeRenderParams(int viewPortX,
                                              int viewPortY,
                                              int viewPortWidth,
                                              int viewPortHeight,
                                              float clipNear,
                                              float clipFar,
                                              float eyeToScreenDistanceScaleFactor,
                                              float lensSeparationScaleFactor);
    public EyeRenderParams getEyeRenderParams(int viewPortX,
                                              int viewPortY,
                                              int viewPortWidth,
                                              int viewPortHeight,
                                              float clipNear,
                                              float clipFar,
                                              float eyeToScreenDistanceScaleFactor,
                                              float lensSeparationScaleFactor,
                                              float distortionFitX,
                                              float distortionFitY,
                                              IOculusRift.AspectCorrectionType aspectCorrectionType);
    /**
     * Sets/saves the IPD for use in the eyeRenderParams
     * 
     * @param ipd Interpupillary distance, in meters.
     */
    public void setIPD(float ipd);

    /**
     * Gets the IPD
     * 
     * @return Interpupillary distance, in meters.
     */
    public float getIPD();

    /* Gets the current user profile data */
    public UserProfileData getProfileData();

    /* Gets a list of all available user profiles */
    public String[] getUserProfiles();

    /* Select the named user profile */
    public boolean loadProfile(String profileName);
}
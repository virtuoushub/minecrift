/**
 * Copyright 2013 Mark Browning
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.api;

import de.fruitfly.ovr.EyeRenderParams;
import de.fruitfly.ovr.HMDInfo;
import de.fruitfly.ovr.SensorInfo;
import de.fruitfly.ovr.UserProfileData;

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
                                              float eyeToScreenDistanceScaleFactor );
    public EyeRenderParams getEyeRenderParams(int viewPortX,
                                              int viewPortY,
                                              int viewPortWidth,
                                              int viewPortHeight,
                                              float clipNear,
                                              float clipFar,
                                              float eyeToScreenDistanceScaleFactor,
                                              float distortionFitX,
                                              float distortionFitY);
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

    /* Get the profile data */
    public UserProfileData getProfileData();
}
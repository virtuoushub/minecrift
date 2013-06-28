/**
 * Copyright 2013 Mark Browning
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.api;

/**
 * The "head-tracking" module. Provides orientation fixed to the real-world reference frame.
 * 
 * @author Mark Browning
 *
 */
public interface IOrientationProvider extends IBasePlugin {
	
    public final float MAXPITCH = (90 * 0.98f);
    public final float MAXROLL = (180 * 0.98f);

    public static final int EVENT_ORIENTATION_AT_ORIGIN = 0;

	/**
	 * Enables/Disables prediction. On the Oculus Rift, this improves performance significantly.
	 * 
	 * @param delta ??
	 * @param enable True if prediction should be enabled
	 */
    public void setPrediction(float delta, boolean enable);
    
    /**
     * Gets the Yaw(Y) from YXZ Euler angle representation of orientation
     * 
     * @return The Yaw degrees 
     */
    public float getYawDegrees_LH();

    /**
     * Gets the Pitch(X) from YXZ Euler angle representation of orientation
     * 
     * @return The Pitch degrees 
     */
    public float getPitchDegrees_LH();

    /**
     * Gets the Roll(Z) from YXZ Euler angle representation of orientation
     * 
     * @return The Roll degrees 
     */
    public float getRollDegrees_LH();

	void beginAutomaticCalibration();

	void updateAutomaticCalibration();

    /**
     * Resets the current position/orientation to the origin
     */
    public void resetOrigin();
}

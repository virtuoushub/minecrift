/**
 * Copyright 2013 Mark Browning
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.api;

import net.minecraft.src.Vec3;

/**
 * The "head-position" module. Provides center eye position relative to the eye orientation reference frame co-ordinates.
 *
 * @author Mark Browning
 *
 */
public interface ICenterEyePositionProvider extends IBasePlugin {

    /**
     * Updates the model with the current head orientation
     * @param yawHeadDegrees Yaw of head only
     * @param pitchHeadDegrees Pitch of head only
     * @param rollHeadDegrees Roll of head only
     * @param worldYawOffsetDegrees Additional yaw input (e.g. mouse)
     * @param worldPitchOffsetDegrees Additional pitch input (e.g. mouse)
     * @param worldRollOffsetDegrees Additional roll input
     */
    public void update(float yawHeadDegrees, float pitchHeadDegrees, float rollHeadDegrees,
                       float worldYawOffsetDegrees, float worldPitchOffsetDegrees, float worldRollOffsetDegrees);

    /**
     * @return The coordinate of the center eye position relative to the head yaw plane
     */
    public Vec3 getCenterEyePosition();


    /**
     * Resets the current origin position
     */
    public void resetOrigin();
    
    /**
     * Resets the current origin rotation 
     */
    public void resetOriginRotation();

    /**
     * Enables prediction/filtering
     * @param delta
     * @param enable
     */
    public void setPrediction(float delta, boolean enable);
}

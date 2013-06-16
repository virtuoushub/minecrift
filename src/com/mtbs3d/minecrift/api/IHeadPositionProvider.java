/**
 * Copyright 2013 Mark Browning
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.api;

import net.minecraft.src.Vec3;

/**
 * The "head-position" module. Provides head position relative to the head orientation reference frame.
 * 
 * @author Mark Browning
 *
 */
public interface IHeadPositionProvider extends IBasePlugin {

	/**
	 * Updates the model with the current head orientation
	 * @param yawHeadDegrees Yaw of head only
	 * @param pitchDegrees Yaw of head + mouse
	 * @param rollDegrees Roll of head
	 */
	public void update(float yawHeadDegrees, float pitchDegrees, float rollDegrees );
	
	/**
	 * @return The coordinate of the head position relative to the head yaw plane
	 */
	public Vec3 getHeadPosition();

	
	/**
	 * Resets the current position/orientation to the origin
	 */
	public void resetOrigin();

}

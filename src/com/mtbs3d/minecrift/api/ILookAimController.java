/**
 * Copyright 2013 Mark Browning
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.api;

/**
 * @author Mark Browning
 *
 */
public interface ILookAimController extends IBasePlugin {
	
	/**
	 * Gets the "look" yaw. This is the direction the player will "look" or the camera will face. Head yaw is added to this.
	 * @return Yaw, in absolute world space degrees
	 */
	public float getLookYawOffset();
	/**
	 * Sets the "look" yaw. Used in conjunction with origin setting.
	 */
	public void setLookYawOffset(float yawOffset);
	/**
	 * Gets the "look" pitch. This is the direction the player will "look" or the camera will face. Head pitch is added to this.
	 * @return Pitch, in absolute world space degrees
	 */
	public float getLookPitchOffset();

	/**
	 * Gets the "aim" yaw. This is the direction the player will "mine" or attack.
	 * @return Yaw, in absolute world space degrees
	 */
	public float getAimYaw();
	/**
	 * Gets the "aim" pitch. This is the direction the player will "mine" or attack.
	 * @return Pitch, in absolute world space degrees
	 */
	public float getAimPitch();
}

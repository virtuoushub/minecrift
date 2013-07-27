/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.control;

import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.Minecraft;

public class WalkBackwardBinding extends ControlBinding {

	@Override
	public boolean isAxis() { return true; }

	public WalkBackwardBinding() {
		super("key.back","key.back");
	}

	@Override
	public void setValue(float value) {
        EntityClientPlayerMP thePlayer = Minecraft.getMinecraft().thePlayer;
        if( thePlayer != null )
        	thePlayer.movementInput.baseMoveForward = -Math.abs(value);
	}

	@Override
	public void setState(boolean state) {
		setValue( state ? Minecraft.getMinecraft().vrSettings.movementSpeedMultiplier: 0.0f );
	}
}

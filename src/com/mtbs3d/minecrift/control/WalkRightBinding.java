package com.mtbs3d.minecrift.control;

import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.Minecraft;

public class WalkRightBinding extends ControlBinding {

	@Override
	public boolean isAxis() { return true; }

	public WalkRightBinding() {
		super("Strafe right");
	}

	@Override
	public void setValue(float value) {
        EntityClientPlayerMP thePlayer = Minecraft.getMinecraft().thePlayer;
        if( thePlayer != null )
        	thePlayer.movementInput.baseMoveStrafe = -Math.abs(value);
	}

	@Override
	public void setState(boolean state) {
		setValue( state ? Minecraft.getMinecraft().vrSettings.movementSpeedMultiplier: 0.0f );
	}
}

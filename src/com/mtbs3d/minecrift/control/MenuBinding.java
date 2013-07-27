/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.control;

import net.minecraft.src.Minecraft;

public class MenuBinding extends ControlBinding {

	private boolean pressed = false;

	public MenuBinding() {
		super("Menu", "key.menu");
	}

	@Override
	public void setValue(float value) {
		if( Math.abs(value) > 0.1 )
		{
			if(!pressed )
				setState( true );
		} else {
			setState(false);
		}
	}

	@Override
	public void setState(boolean state) {
		pressed = state;
		if( state ) {
			if( Minecraft.getMinecraft().currentScreen == null ) {
				Minecraft.getMinecraft().displayInGameMenu();
			} else {
				Minecraft.getMinecraft().displayGuiScreen(null);
			}
		}
	}

}

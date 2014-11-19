/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;

import com.mtbs3d.minecrift.api.IBasePlugin;
import com.mtbs3d.minecrift.api.PluginManager;
import com.mtbs3d.minecrift.settings.VRSettings;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class VRHotkeys {

	public static void handleKeyboardInputs(Minecraft mc)
	{                                
		// TODO: Capture oculus key events
	
    	if( mc.headTracker == null || mc.hmdInfo == null )
    	{
    		return;
    	}

	    //  Reinitialise head tracking
	    if (Keyboard.getEventKey() == Keyboard.KEY_BACK && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
	    {
            PluginManager.destroyAll();
            mc.printChatMessage("Re-initialising all plugins (RCTRL+BACK): done");
	    }

        // Reset positional track origin
        if (Keyboard.getEventKey() == Keyboard.KEY_RETURN && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
        {
            mc.vrSettings.posTrackResetPosition = true;
            mc.printChatMessage("Reset origin (RCTRL+RET): done");
        }

        // Debug aim
        if (Keyboard.getEventKey() == Keyboard.KEY_RSHIFT && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
        {
            mc.vrSettings.storeDebugAim = true;
            mc.printChatMessage("Show aim (RCTRL+RSHIFT): done");
        }

        // If an orientation plugin is performing calibration, space also sets the origin
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
        {
            PluginManager.notifyAll(IBasePlugin.EVENT_CALIBRATION_SET_ORIGIN);
        }
	}
}

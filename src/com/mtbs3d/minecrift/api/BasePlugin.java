/**
 * Copyright 2013 Mark Browning
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

/**
 * A base API for plugins. Plugins can be loaded, initialized, queried, and polled.
 * 
 * @author Mark Browning
 *
 */
public abstract class BasePlugin implements IBasePlugin, IEventListener
{
	@Override
	public String getID() { return pluginID; };
	public static String pluginID = "BasePlugin";

	@Override
	public String getName(){ return pluginName; };
	public static String pluginName = "BasePlugin - Not Named!";
	
	/**
	 * Constructs, initializes, and registers plugin
	 */
	public BasePlugin()
	{
		PluginManager.register(this);
	}
}

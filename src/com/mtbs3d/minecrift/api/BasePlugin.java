/**
 * Copyright 2013 Mark Browning
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.api;

import java.util.ArrayList;
import java.util.List;

/**
 * A base API for plugins. Plugins can be loaded, initialized, queried, and polled.
 * 
 * @author Mark Browning
 *
 */
public abstract class BasePlugin implements IBasePlugin {
	static public List<IBasePlugin> allPlugins = new ArrayList<IBasePlugin>();
	static public List<IHMDInfo> hmdInfoPlugins = new ArrayList<IHMDInfo>();
	static public List<IOrientationProvider> orientPlugins = new ArrayList<IOrientationProvider>();
	static public List<IHeadPositionProvider> positionPlugins = new ArrayList<IHeadPositionProvider>();

	@Override
	public String getID() { return pluginID; };
	protected String pluginID = "BasePlugin";

	@Override
	public String getName(){ return pluginName; };
	protected String pluginName = "BasePlugin - Not Named!";
	
	/**
	 * Constructs, initializes, and registers plugin
	 */
	public BasePlugin()
	{
		register(this);
	}
	
	public static void register( IBasePlugin that )
	{
		
		if( that instanceof IHMDInfo )
			hmdInfoPlugins.add((IHMDInfo) that);
		if( that instanceof IOrientationProvider )
			orientPlugins.add((IOrientationProvider) that);
		if( that instanceof IHeadPositionProvider )
			positionPlugins.add((IHeadPositionProvider) that);
		allPlugins.add(that);
	}

	public static void pollAll()
	{
		for( IBasePlugin p : allPlugins )
		{
			if( p.isInitialized() )
				p.poll();
		}
	}
	
	public static void destroyAll()
	{
		for( IBasePlugin p : allPlugins )
		{
			p.destroy();
		}
	}
	public static void initAll() 
	{
		for( IBasePlugin p : allPlugins )
		{
			p.init();
		}
	}
}

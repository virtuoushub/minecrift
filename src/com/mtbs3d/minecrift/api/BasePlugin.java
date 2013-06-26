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
public abstract class BasePlugin implements IBasePlugin {
	static public List<IBasePlugin> allPlugins = new ArrayList<IBasePlugin>();
	static public List<IHMDInfo> hmdInfoPlugins = new ArrayList<IHMDInfo>();
	static public List<IOrientationProvider> orientPlugins = new ArrayList<IOrientationProvider>();
	static public List<ICenterEyePositionProvider> positionPlugins = new ArrayList<ICenterEyePositionProvider>();
	static public List<ILookAimController> controllerPlugins = new ArrayList<ILookAimController>();

	@Override
	public String getID() { return pluginID; };
	public static String pluginID = "BasePlugin";

	@Override
	public String getName(){ return pluginName; };
	public static String pluginName = "BasePlugin - Not Named!";
	
	
	public static IHMDInfo configureHMD( String pluginID )
	{
		IHMDInfo hmdInfo = null;
    	for( IHMDInfo hmd : hmdInfoPlugins )
    	{
    		if( hmd.getID().equals(pluginID) )
    		{
    			hmdInfo = hmd;
    			break;
    		}
    	}
    	//If we still don't have one
    	if( hmdInfo == null && hmdInfoPlugins.size() > 0 )
    	{
    		hmdInfo = hmdInfoPlugins.get(0);
    	}

    	if( hmdInfo != null  )
    	{
    		initForMinecrift( hmdInfo );
    	}
    	return hmdInfo;
	}
	
	public static IOrientationProvider configureOrientation( String pluginID )
	{
		IOrientationProvider headTracker = null;
    	for( IOrientationProvider tracker: orientPlugins )
    	{
    		if( tracker.getID().equals( pluginID ) )
    		{
    			headTracker = tracker;
    			break;
    		}
    	}
    	//If we still don't have one, try to use the first in the list
    	if( headTracker == null && orientPlugins.size() > 0 )
    	{
    		headTracker = orientPlugins.get(0);
    	}
    	
    	if( headTracker != null )
    	{
    		initForMinecrift( headTracker );
    	}
    	return headTracker;
	}
	
	public static ICenterEyePositionProvider configurePosition( String pluginID )
	{
		ICenterEyePositionProvider positionTracker = null;
    	for( ICenterEyePositionProvider posTracker: positionPlugins )
    	{
    		if( posTracker.getID().equals( pluginID ) )
    		{
    			positionTracker = posTracker;
    			break;
    		}
    	}
    	//If we still don't have one, try to use the first in the list
    	if( positionTracker == null && positionPlugins.size() > 0 )
    	{
    		positionTracker = positionPlugins.get(0);
    	}
		
    	if( positionTracker != null )
    	{
    		initForMinecrift( positionTracker );
    	}
    	return positionTracker;
	}
	
	public static ILookAimController configureController( String pluginID )
	{
	
		ILookAimController lookaimController = null;
    	for( ILookAimController controller: controllerPlugins )
    	{
    		if( controller.getID().equals( pluginID ) )
    		{
    			lookaimController = controller;
    			break;
    		}
    	}
    	//If we still don't have one, try to use the first in the list
    	if( lookaimController == null && controllerPlugins.size() > 0 )
    	{
    		lookaimController = controllerPlugins.get(0);
    	}

    	if( lookaimController != null )
    	{
    		initForMinecrift( lookaimController );
    	}
    	return lookaimController;
	}
	
	private static void initForMinecrift(IBasePlugin plugin) 
	{
		if( !plugin.isInitialized() && !plugin.init(new File(new File(Minecraft.getMinecraftDir(),"bin"),"natives")) )
		{
			System.err.println("Error! Couldn't load "+ plugin.getName()+": "+plugin.getInitializationStatus() );
		}
	}
	
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
		if( that instanceof ICenterEyePositionProvider )
			positionPlugins.add((ICenterEyePositionProvider) that);
		if( that instanceof ILookAimController )
			controllerPlugins.add((ILookAimController) that);
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

    public static void notifyAll(int eventId)
    {
        for( IBasePlugin p : allPlugins )
        {
            if( p.isInitialized() )
                p.eventNotification(eventId);
        }
    }
	
	public static void destroyAll()
	{
		for( IBasePlugin p : allPlugins )
		{
			if( p.isInitialized() )
				p.destroy();
		}
	}
}

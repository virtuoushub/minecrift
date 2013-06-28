package com.mtbs3d.minecrift.api;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Pete
 * Date: 6/28/13
 * Time: 7:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class PluginManager implements IEventListener
{
    static public PluginManager thePluginManager = new PluginManager();

    public List<IBasePlugin> allPlugins = new ArrayList<IBasePlugin>();
    public List<IHMDInfo> hmdInfoPlugins = new ArrayList<IHMDInfo>();
    public List<IOrientationProvider> orientPlugins = new ArrayList<IOrientationProvider>();
    public List<ICenterEyePositionProvider> positionPlugins = new ArrayList<ICenterEyePositionProvider>();
    public List<ILookAimController> controllerPlugins = new ArrayList<ILookAimController>();

    public static void create()
    {
        thePluginManager = new PluginManager();
    }

    public static IHMDInfo configureHMD( String pluginID )
    {
        IHMDInfo hmdInfo = null;
        for( IHMDInfo hmd : thePluginManager.hmdInfoPlugins )
        {
            if( hmd.getID().equals(pluginID) )
            {
                hmdInfo = hmd;
                break;
            }
        }
        //If we still don't have one
        if( hmdInfo == null && thePluginManager.hmdInfoPlugins.size() > 0 )
        {
            hmdInfo = thePluginManager.hmdInfoPlugins.get(0);
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
        for( IOrientationProvider tracker: thePluginManager.orientPlugins )
        {
            if( tracker.getID().equals( pluginID ) )
            {
                headTracker = tracker;
                break;
            }
        }
        //If we still don't have one, try to use the first in the list
        if( headTracker == null && thePluginManager.orientPlugins.size() > 0 )
        {
            headTracker = thePluginManager.orientPlugins.get(0);
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
        for( ICenterEyePositionProvider posTracker: thePluginManager.positionPlugins )
        {
            if( posTracker.getID().equals( pluginID ) )
            {
                positionTracker = posTracker;
                break;
            }
        }
        //If we still don't have one, try to use the first in the list
        if( positionTracker == null && thePluginManager.positionPlugins.size() > 0 )
        {
            positionTracker = thePluginManager.positionPlugins.get(0);
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
        for( ILookAimController controller: thePluginManager.controllerPlugins )
        {
            if( controller.getID().equals( pluginID ) )
            {
                lookaimController = controller;
                break;
            }
        }
        //If we still don't have one, try to use the first in the list
        if( lookaimController == null && thePluginManager.controllerPlugins.size() > 0 )
        {
            lookaimController = thePluginManager.controllerPlugins.get(0);
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

    public static void register( IBasePlugin that )
    {
        if( that instanceof IHMDInfo )
            thePluginManager.hmdInfoPlugins.add((IHMDInfo) that);
        if( that instanceof IOrientationProvider )
        {
            thePluginManager.orientPlugins.add((IOrientationProvider) that);
            if (that instanceof IEventNotifier)
            {
                ((IEventNotifier)that).registerListener(thePluginManager);
            }
        }
        if( that instanceof ICenterEyePositionProvider )
            thePluginManager.positionPlugins.add((ICenterEyePositionProvider) that);
        if( that instanceof ILookAimController )
            thePluginManager.controllerPlugins.add((ILookAimController) that);
        thePluginManager.allPlugins.add(that);
    }

    public static void pollAll()
    {
        for( IBasePlugin p : thePluginManager.allPlugins )
        {
            if( p.isInitialized() )
                p.poll();
        }
    }

    public static void notifyAll(int eventId)
    {
        for( IBasePlugin p : thePluginManager.allPlugins )
        {
            if( p.isInitialized() && p instanceof IEventListener)
                ((IEventListener)p).eventNotification(eventId);
        }
    }

    public static void destroyAll()
    {
        for( IBasePlugin p : thePluginManager.allPlugins )
        {
            if( p.isInitialized() )
                p.destroy();
        }
    }

    @Override
    public void eventNotification(int eventId)
    {
        switch (eventId)
        {
            case IOrientationProvider.EVENT_CALIBRATION_SET_ORIGIN:
            {
                Minecraft.getMinecraft().gameSettings.posTrackResetPosition = true;
                break;
            }
        }
    }
}

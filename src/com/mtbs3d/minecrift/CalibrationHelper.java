/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;

import java.util.*;

import net.minecraft.src.Minecraft;

import com.mtbs3d.minecrift.api.IBasePlugin;
import com.mtbs3d.minecrift.api.IOrientationProvider;

public class CalibrationHelper {
	public ArrayList<IBasePlugin> pluginsToCalibrate = new ArrayList<IBasePlugin>();
	
	public IBasePlugin currentPlugin = null;
	public Iterator<IBasePlugin> iterator = null;
	public String calibrationStep = "";

	public CalibrationHelper(Minecraft mc) {

        // TODO: Will need a rethink to avoid multiple initialisation when Oculus
        // is used for both pos track and orientation. Maybe static instance of
        // Oculus lib?

        // Never calibrate HMDInfo; not required currently
        pluginsToCalibrate.add(mc.lookaimController);
        pluginsToCalibrate.add(mc.positionTracker);
		pluginsToCalibrate.add(mc.headTracker);

        iterator = pluginsToCalibrate.iterator();
		currentPlugin = null;
	}
	
	public boolean allPluginsCalibrated()
	{
		do
		{
			if( currentPlugin == null)
			{
				currentPlugin = iterator.next();
				if( currentPlugin instanceof IOrientationProvider )
				{
					((IOrientationProvider)currentPlugin).beginAutomaticCalibration();
				}
			}

			if( currentPlugin instanceof IOrientationProvider )
			{
				((IOrientationProvider)currentPlugin).updateAutomaticCalibration();
			}

			if( currentPlugin.isCalibrated() )
			{
				currentPlugin = null;
			}
		} while( currentPlugin == null && iterator.hasNext() );
		
		if( currentPlugin != null )
		{
			calibrationStep = currentPlugin.getCalibrationStep();
			return false; //Not done yet
		}
		//Otherwise, all are calibrated
		return true;
	}
}

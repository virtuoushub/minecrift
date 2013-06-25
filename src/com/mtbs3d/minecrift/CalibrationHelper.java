/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.client.Minecraft;

import com.mtbs3d.minecrift.api.IBasePlugin;
import com.mtbs3d.minecrift.api.IOrientationProvider;

public class CalibrationHelper {
	public Set<IBasePlugin> pluginsToCalibrate = new HashSet<IBasePlugin>();
	
	public IBasePlugin currentPlugin = null;
	public Iterator<IBasePlugin> iterator = null;
	public String calibrationStep = "";

	public CalibrationHelper(Minecraft mc) {
		pluginsToCalibrate.add(mc.hmdInfo);
		pluginsToCalibrate.add(mc.headTracker);
		pluginsToCalibrate.add(mc.positionTracker);
		pluginsToCalibrate.add(mc.lookaimController);
		iterator = pluginsToCalibrate.iterator();
		currentPlugin  = iterator.next();
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

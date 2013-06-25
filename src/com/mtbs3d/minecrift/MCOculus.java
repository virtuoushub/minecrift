/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;


import com.mtbs3d.minecrift.api.IBasePlugin;
import com.mtbs3d.minecrift.api.IHMDInfo;
import com.mtbs3d.minecrift.api.IOrientationProvider;

import de.fruitfly.ovr.OculusRift;

public class MCOculus extends OculusRift //OculusRift does most of the heavy lifting 
	implements IOrientationProvider, IBasePlugin, IHMDInfo {
	
	@Override
	public String getName() {
		return "Oculus";
	}

	@Override
	public String getID() {
		return "oculus";
	}
	
	@Override
	public void resetOrigin() {
        _setCalibrationReference();
    }

    @Override
    public void beginAutomaticCalibration() {
//        _reset();
//        _pollSubsystem();
//        _setCalibrationReference();
        _beginAutomaticCalibration();
    }

    @Override
    public void updateAutomaticCalibration() {
        _updateAutomaticCalibration();
    }

    @Override
    public boolean isCalibrated() {
        if (!isInitialized())
            return true;  // Return true if not initialised

        return _isCalibrated();
    }

	@Override
	public String getCalibrationStep() {
		return "Look left, right, up";
	}
}

/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;

import java.io.File;
import java.util.HashMap;

import net.minecraft.src.GuiScreen;
import net.minecraft.src.Minecraft;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

import com.mtbs3d.minecrift.api.BasePlugin;
import com.mtbs3d.minecrift.api.IBodyAimController;
import com.mtbs3d.minecrift.control.ControlBinding;
import com.mtbs3d.minecrift.control.GuiScreenNaviator;
import com.mtbs3d.minecrift.control.JoystickAim;
import com.mtbs3d.minecrift.settings.VRSettings;


public class MCController extends BasePlugin implements IBodyAimController {

	float aimOffset = 0.0f;
	JoystickAim joyAim;
	boolean hasControllers = false;
	ControlBinding nextBind = null;
	class BindingMap {
		HashMap<Pair<Integer,Boolean>,ControlBinding> axisBinds = new HashMap<Pair<Integer,Boolean>,ControlBinding>();
		HashMap<Integer,ControlBinding> buttonBinds = new HashMap<Integer,ControlBinding>();
		HashMap<ControlBinding,Pair<Integer,Boolean>> revAxisBinds = new HashMap<ControlBinding,Pair<Integer,Boolean>>();
		HashMap<ControlBinding,Integer> revButtonBinds = new HashMap<ControlBinding,Integer>();
		ControlBinding povXBindPos;
		ControlBinding povXBindNeg;
		ControlBinding povYBindPos;
		ControlBinding povYBindNeg;
		
		boolean bind( ControlBinding nextBind) {
			boolean bound = false;
			Controller cont = Controllers.getEventSource();
			String alreadyBound = "";
			int index = Controllers.getEventControlIndex();
			if( revAxisBinds.containsKey(nextBind)) {
				alreadyBound = cont.getAxisName(revAxisBinds.get(nextBind).getLeft())+" axis";
				if( nextBind.isAxis() )
					axisBinds.remove( revAxisBinds.get(nextBind));
				else {
					Integer axisIndex = revAxisBinds.get(nextBind).getKey();
					axisBinds.remove( Pair.of( axisIndex, true));
					axisBinds.remove( Pair.of( axisIndex, false));
				}
				revAxisBinds.remove( nextBind );
			}
			if( revButtonBinds.containsKey(nextBind)) {
				Integer button =  revButtonBinds.get(nextBind);
				alreadyBound = cont.getButtonName(button)+" button";
				buttonBinds.remove(button);
				revButtonBinds.remove( nextBind );
			}
			if( povXBindPos == nextBind ) {
				povXBindPos = null;
				alreadyBound = "POV X+";
			} else if( povYBindPos == nextBind ) {
				povYBindPos = null;
				alreadyBound = "POV Y+";
			} else if( povXBindNeg == nextBind ) {
				povXBindNeg = null;
				alreadyBound = "POV X-";
			} else if( povYBindNeg == nextBind ) {
				povYBindNeg = null;
				alreadyBound = "POV Y-";
			} 
			if( !alreadyBound.isEmpty() )
				System.out.println( nextBind.getDescription()+" already bound to "+alreadyBound+". Removing.");

			if( Controllers.isEventAxis() ) {
				float joyVal = cont.getAxisValue(index);
				if(Math.abs(joyVal)>0.5f) {
					if( nextBind.isAxis() ) {
						Pair<Integer,Boolean> key= Pair.of(index,joyVal>0);
						if( axisBinds.get( key ) == null ) {
							nextBind.bindTo(cont.getAxisName(index)+(joyVal>0?"+":"-")+" axis");
							axisBinds.put(key , nextBind);
							revAxisBinds.put(nextBind, key);
							nextBind.setValid(true);
							bound = true;
						} else {
							nextBind.bindTo("Conflict!");
							nextBind.setValid(false);
							bound = true;
						}
					} else {
						if( axisBinds.get( Pair.of(index,true) ) == null &&
							axisBinds.get( Pair.of(index,false)) == null) {
							nextBind.bindTo(cont.getAxisName(index)+" axis");
							axisBinds.put(Pair.of(index,true) , nextBind);
							axisBinds.put(Pair.of(index,false) , nextBind);
							revAxisBinds.put(nextBind, Pair.of(index,true));
							nextBind.setValid(true);
							bound = true;
						} else {
							nextBind.bindTo("Conflict!");
							nextBind.setValid(false);
							bound = true;
						}
					}
				}
			} else if( Controllers.isEventButton() ) {
				if( cont.isButtonPressed(index)) {
					if( buttonBinds.get( index ) == null ) {
						nextBind.bindTo(cont.getButtonName(index)+" button");
						buttonBinds.put(index, nextBind);
						revButtonBinds.put(nextBind, index);
						nextBind.setValid(true);
						bound = true;
					} else {
						nextBind.bindTo("Conflict!");
						nextBind.setValid(false);
						bound = true;
					}
				}
			} else if( Controllers.isEventPovX()) {
				if( cont.getPovX() > 0) {
					if( povXBindPos == null ) {
						nextBind.bindTo("POV X+");
						povXBindPos = nextBind;
						povXBindPos.setValid(true);
						bound = true;
					} else {
						nextBind.bindTo("Conflict!");
						povXBindPos.setValid(false);
						bound = true;
					}
				} else if( cont.getPovX() < 0 ) {
					if( povXBindNeg == null ) {
						nextBind.bindTo("POV X-");
						povXBindNeg = nextBind;
						povXBindNeg.setValid(true);
						bound = true;
					} else {
						nextBind.bindTo("Conflict!");
						povXBindNeg.setValid(false);
						bound = true;
					}
				}
			} else if( Controllers.isEventPovY()) {
				if( cont.getPovY() > 0) {
					if( povYBindPos == null ) {
						nextBind.bindTo("POV Y+");
						povYBindPos = nextBind;
						povYBindPos.setValid(true);
						bound = true;
					} else {
						nextBind.bindTo("Conflict!");
						povYBindPos.setValid(false);
						bound = true;
					}
				} else if( cont.getPovY() < 0 ) {
					if( povYBindNeg == null ) {
						nextBind.bindTo("POV Y-");
						povYBindNeg = nextBind;
						povYBindNeg.setValid(true);
						bound = true;
					} else {
						nextBind.bindTo("Conflict!");
						povYBindNeg.setValid(false);
						bound = true;
					}
				}
			}
			return bound;
		}
		
		void activate() {
			Controller cont = Controllers.getEventSource();
			int index = Controllers.getEventControlIndex();
			if( Controllers.isEventAxis() ) {
				float joyVal = cont.getAxisValue(index);
				Pair<Integer,Boolean> key = Pair.of(index,joyVal>0);
				ControlBinding bind = axisBinds.get( key );
				if( bind != null) {
					bind.setValue( cont.getAxisValue( index ));
				}
			} else if( Controllers.isEventButton() ) {
				ControlBinding bind = buttonBinds.get(index);
				if( bind != null ) {
					bind.setState( cont.isButtonPressed(index));
				}
			} else if( Controllers.isEventPovX()) {
				if( cont.getPovX() > 0) {
					if( povXBindPos != null)
						povXBindPos.setState(true);
				} else if ( cont.getPovX() < 0 ) {
					if( povXBindNeg != null)
						povXBindNeg.setState(true);
				} else {
					if( povXBindPos != null)
						povXBindPos.setState(false);
					if( povXBindNeg != null)
						povXBindNeg.setState(false);
				}
			} else if( Controllers.isEventPovY()) {
				if( cont.getPovY() > 0) {
					if( povYBindPos != null)
						povYBindPos.setState(true);
				} else if ( cont.getPovY() < 0 ) {
					if( povYBindNeg != null)
						povYBindNeg.setState(true);
				} else {
					if( povYBindPos != null)
						povYBindPos.setState(false);
					if( povYBindNeg != null)
						povYBindNeg.setState(false);
				}
			}
		}
	}
	BindingMap ingame = new BindingMap();
	BindingMap GUI    = new BindingMap();
	private Minecraft mc;
	private GuiScreenNaviator screenNavigator;
	public MCController() {
		super();
		mc = Minecraft.getMinecraft();
		System.out.println("Created Controller plugin");
        pluginID = "controller";
        pluginName = "Controller";
        joyAim = new JoystickAim();
        JoystickAim.selectedJoystickMode = joyAim;
	}
	@Override
	public String getInitializationStatus() {
		return hasControllers ? "Ready." : "No Controllers found.";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public boolean init(File nativeDir) {
		return init();
	}

	@Override
	public boolean init() {
		try {
			Controllers.create();
			hasControllers = Controllers.getControllerCount()> 0;
			for( int c = 0; c < Controllers.getControllerCount();c++) {
				Controller cont = Controllers.getController(c);
				for( int a = 0; a < cont.getAxisCount(); a++ ) {
					cont.setDeadZone(a, Minecraft.getMinecraft().vrSettings.joystickDeadzone);
				}
			}
			System.out.println("Initialized controllers: "+getInitializationStatus());
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		return isInitialized();
	}
	
	@Override
	public boolean isInitialized() {
		return hasControllers;
	}

	@Override
	public void poll() {
        if( this.mc.currentScreen != null && (this.screenNavigator == null || this.screenNavigator.screen != this.mc.currentScreen) )
        	this.screenNavigator = new GuiScreenNaviator(this.mc.currentScreen );
		Controllers.poll();
		while (Controllers.next()) {
			if( nextBind != null ) {
				boolean bound = false;
				if( nextBind.isGUI()) 
					bound = GUI.bind(nextBind);
				 else 
					bound = ingame.bind(nextBind);
				
				if(bound) {
					nextBind.doneBinding();
					nextBind = null;
				}
			} else if( mc.currentScreen == null ) {
				ingame.activate();
			} else {
				GUI.activate();
			}
		}
		Controllers.clearEvents();
	}

	@Override
	public void destroy() {
		Controllers.destroy();
	}

	@Override
	public boolean isCalibrated() {
		return true;
	}

	@Override
	public String getCalibrationStep() {
		return "";
	}

	@Override
	public void eventNotification(int eventId) {
	}

	@Override
	public float getBodyYawDegrees() {
		return joyAim.getAimYaw();
	}

	@Override
	public void setBodyYawDegrees(float yawOffset) {
		this.aimOffset = yawOffset;
	}

	@Override
	public float getBodyPitchDegrees() {
		if( VRSettings.inst.allowMousePitchInput)
			return joyAim.getAimPitch();
		return 0.0f;
	}

	@Override
	public float getAimYaw() {
		return aimOffset + joyAim.getAimYaw();
	}

	@Override
	public float getAimPitch() {
		return joyAim.getAimPitch();
	}
	@Override
	public void mapBinding(ControlBinding binding) {
		nextBind = binding;
	}
}

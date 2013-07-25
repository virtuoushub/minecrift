package com.mtbs3d.minecrift;

import java.io.File;
import java.util.HashMap;

import net.minecraft.src.Minecraft;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

import com.mtbs3d.minecrift.api.BasePlugin;
import com.mtbs3d.minecrift.api.IBodyAimController;
import com.mtbs3d.minecrift.control.ControlBinding;
import com.mtbs3d.minecrift.control.JoystickAim;
import com.mtbs3d.minecrift.settings.VRSettings;


public class MCController extends BasePlugin implements IBodyAimController {

	float aimOffset = 0.0f;
	JoystickAim joyAim;
	boolean hasControllers = false;
	ControlBinding nextBind = null;
	HashMap<Pair<Integer,Boolean>,ControlBinding> axisBinds = new HashMap<Pair<Integer,Boolean>,ControlBinding>();
	HashMap<Integer,ControlBinding> buttonBinds = new HashMap<Integer,ControlBinding>();
	HashMap<ControlBinding,Pair<Integer,Boolean>> revAxisBinds = new HashMap<ControlBinding,Pair<Integer,Boolean>>();
	HashMap<ControlBinding,Integer> revButtonBinds = new HashMap<ControlBinding,Integer>();
	public MCController() {
		super();
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
		Controllers.poll();
		while (Controllers.next()) {
			Controller cont = Controllers.getEventSource();
			int index = Controllers.getEventControlIndex();
			
			if( nextBind != null ) {
				if( revAxisBinds.containsKey(nextBind)) {
					System.out.println( nextBind.getDescription()+" already bound to "+cont.getAxisName(revAxisBinds.get(nextBind).getLeft())+" axis. Removing.");
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
					System.out.println( nextBind.getDescription()+" already bound to "+cont.getButtonName(button)+" button. Removing.");
					buttonBinds.remove(button);
					revButtonBinds.remove( nextBind );
				}
				if( Controllers.isEventAxis() ) {
					float joyVal = cont.getAxisValue(index);
					if(Math.abs(joyVal)>0.5f) {
						if( nextBind.isAxis() ) {
							Pair<Integer,Boolean> key= Pair.of(index,joyVal>0);
							nextBind.bindTo(cont.getAxisName(index)+(joyVal>0?"+":"-")+" axis");
							if( axisBinds.get( key ) == null ) {
								System.out.println(nextBind.getDescription()+" bound to "+cont.getAxisName(index) +" axis!");
								axisBinds.put(key , nextBind);
								revAxisBinds.put(nextBind, key);
								nextBind.setValid(true);
							} else {
								nextBind.setValid(false);
							}
						} else {
							nextBind.bindTo(cont.getAxisName(index)+" axis");
							if( axisBinds.get( Pair.of(index,true) ) == null &&
								axisBinds.get( Pair.of(index,false)) == null) {
								System.out.println(nextBind.getDescription()+" bound to "+cont.getAxisName(index) +" axis!");
								axisBinds.put(Pair.of(index,true) , nextBind);
								axisBinds.put(Pair.of(index,false) , nextBind);
								revAxisBinds.put(nextBind, Pair.of(index,true));
								nextBind.setValid(true);
							} else {
								nextBind.setValid(false);
							}
						}
						nextBind.doneBinding();
						nextBind = null;
					}
				} else if( Controllers.isEventButton() ) {
					if( cont.isButtonPressed(index)) {
						nextBind.bindTo(cont.getButtonName(index)+" button");
						if( buttonBinds.get( index ) == null ) {
							System.out.println(nextBind.getDescription()+" bound to "+cont.getButtonName(index) +" button!");
							buttonBinds.put(index, nextBind);
							revButtonBinds.put(nextBind, index);
							nextBind.setValid(true);
						} else {
							nextBind.setValid(false);
						}
						nextBind.doneBinding();
						nextBind = null;
					}
				}
			} else {
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
				}
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

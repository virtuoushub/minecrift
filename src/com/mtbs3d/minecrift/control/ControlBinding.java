package com.mtbs3d.minecrift.control;

import java.util.ArrayList;

import net.minecraft.src.GameSettings;

public abstract class ControlBinding {
	public interface ControlBindCallback {
		public void doneBinding();
	}
	
	boolean valid = true;
	
	public void setValid( boolean val ) { valid = val; }
	public boolean isValid() { return valid; }
	
	public static ArrayList<ControlBinding> bindings = new ArrayList<ControlBinding>();
	
	public static void CreateBindingList( GameSettings settings )
	{
		bindings.add( new WalkForwardBinding());
		bindings.add( new WalkLeftBinding());
		bindings.add( new WalkBackwardBinding());
		bindings.add( new WalkRightBinding());
		bindings.add( new KeyControlBinding( settings.keyBindAttack ));
		bindings.add( new KeyControlBinding( settings.keyBindJump ));
		bindings.add( new KeyControlBinding( settings.keyBindInventory ));
		bindings.add( new KeyControlBinding( settings.keyBindDrop ));
		bindings.add( new KeyControlBinding( settings.keyBindChat ));
		bindings.add( new KeyControlBinding( settings.keyBindSneak ));
		bindings.add( new KeyControlBinding( settings.keyBindUseItem ));
		bindings.add( new KeyControlBinding( settings.keyBindPlayerList ));
		bindings.add( new KeyControlBinding( settings.keyBindPickBlock ));
		bindings.add( new KeyControlBinding( settings.keyBindCommand ));
	}
	
	public boolean isAxis() { return false; }
	
	ControlBindCallback callback = null;
	
	public void setDoneBindingCallback( ControlBindCallback callback ) {
		this.callback = callback;
	}
	
	public void doneBinding() {
		if( callback != null )
			callback.doneBinding();
	}
	
	public ControlBinding( String desc ) {
		this.description = desc;
	}

	String description;
	public abstract void setValue( float value );
	public abstract void setState( boolean state );
	public String getDescription() {
		return description;
	}
	
	String bound = "None";
	public String boundTo() {
		return bound;
	}
	public void bindTo(String name) {
		bound = name;
	}
}

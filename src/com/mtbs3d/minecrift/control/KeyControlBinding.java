package com.mtbs3d.minecrift.control;

import net.minecraft.src.KeyBinding;

public class KeyControlBinding extends ControlBinding {

	KeyBinding key;
	public KeyControlBinding(KeyBinding binding ) {
		super(binding.keyDescription);
		key = binding;
	}

	@Override
	public void setValue(float value) {
		if( value > 0.1 )
			key.pressed = true;
		else
			key.pressed = false;
	}

	@Override
	public void setState(boolean state) {
		key.pressed = state;
	}
}

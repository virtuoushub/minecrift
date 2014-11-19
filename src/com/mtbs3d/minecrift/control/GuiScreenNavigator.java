/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 * 
 * Contains code from Minecraft, copyright Mojang AB
 */
package com.mtbs3d.minecrift.control;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class GuiScreenNavigator {
	public ArrayList<Pair<Integer,Integer>> points = new ArrayList<Pair<Integer,Integer>>();
	private GuiSlot slot;
	private int slotIndex = -1;
	private boolean onSlot = false;
	public GuiScreen screen;
	private GuiScreen parentScreen;
	private Minecraft mc;
	
	private Pair<Integer,Integer> curPoint;
	private static Field guiLeft = null;
	private static Field guiTop = null;
	private static Field keyDownField = null;
	
	private boolean selectDepressed = false;
	private boolean altselectDepressed = false;
	private boolean shiftDepressed;
	
	static final int AXIS_PREFERENCE = 5;
	
	private static GuiScreenNavigator nav;
	static abstract class GuiControlBinding extends ControlBinding {

		public GuiControlBinding(String desc) {
			super("GUI "+desc,"gui."+desc);
		}

		boolean floatActive = false;
		@Override
		public boolean isGUI(){ return true; };

		@Override
		public void setValue(float value) {
			if( Math.abs(value) >0.5 )
			{
				if( !floatActive )
					setState( true );
				floatActive = true;
			} else {
				setState(false);
				floatActive = false;
			}
		}
	}
	static class GuiUpBinding extends GuiControlBinding {

		public GuiUpBinding() {
			super("Up");
		}

		@Override
		public void setState(boolean state) {
			if( state ) nav.up();
		}
	}
	static class GuiDownBinding extends GuiControlBinding {

		public GuiDownBinding() {
			super("Down");
		}

		@Override
		public void setState(boolean state) {
			if(state)nav.down();
		}
	}
	static class GuiRightBinding extends GuiControlBinding {

		public GuiRightBinding() {
			super("Right");
		}

		@Override
		public void setState(boolean state) {
			if(state)nav.right();
		}
	}
	static class GuiLeftBinding extends GuiControlBinding {

		public GuiLeftBinding() {
			super("Left");
		}

		@Override
		public void setState(boolean state) {
			if(state)nav.left();
		}
	}
	static class GuiSelectBinding extends GuiControlBinding {

		public GuiSelectBinding() {
			super("Select");
		}

		@Override
		public void setState(boolean state) {
			nav.select(state);
		}
	}
	static class GuiAltSelectBinding extends GuiControlBinding {

		public GuiAltSelectBinding() {
			super("Alt. Select");
		}

		@Override
		public void setState(boolean state) {
			nav.altselect(state);
		}
	}
	static class GuiBackBinding extends GuiControlBinding {

		public GuiBackBinding() {
			super("Back");
		}

		@Override
		public void setState(boolean state) {
			if(state)nav.back();
		}
	}
	static class GuiShiftBinding extends GuiControlBinding {

		public GuiShiftBinding() {
			super("Shift");
		}

		@Override
		public void setState(boolean state) {
			nav.shift(state);
		}
	}
	
	public GuiScreenNavigator(GuiScreen screen) {
		mc = Minecraft.getMinecraft();
		nav = this;
		
		if( guiLeft == null )
		{
	        try {
				keyDownField = Keyboard.class.getDeclaredField("keyDownBuffer");
				keyDownField.setAccessible(true);
	        	guiLeft = GuiContainer.class.getDeclaredField("field_147003_i");  // was guiLeft
	        	guiTop  = GuiContainer.class.getDeclaredField("field_147009_r");  // was guiTop
	        	System.out.println("[Minecrift] GuiScreenNavigator: Reflected guiLeft/guiTop");
	        }
	        catch (NoSuchFieldException e) {
		        try {
		        	guiLeft = GuiContainer.class.getDeclaredField("i"); //obfuscated name  was p
		        	guiTop  = GuiContainer.class.getDeclaredField("r"); //obfuscated name  was q
		        	System.out.println("[Minecrift] GuiScreenNavigator: Reflected obfuscated guiLeft/guiTop (i/r)");
		        }
		        catch (NoSuchFieldException e1) { 
		        	System.out.println("[Minecrift] GuiScreenNavigator: Couldn't get guiLeft/guiTop via reflection! Joystick navigation of inventories may be inaccurate.");
		        };
	        }
	       	if ( guiLeft != null)
	       		guiLeft.setAccessible(true);
	       	if ( guiTop != null)
	       		guiTop.setAccessible(true);
		}
		
		this.screen = screen;
        for( Field field : screen.getClass().getDeclaredFields() ) {
        	if( field.getType().getSuperclass() == GuiSlot.class ) {
        		field.setAccessible(true);
        		try {
					slot = (GuiSlot)field.get(screen);
					break;
				} catch (Exception e) { e.printStackTrace(); }
        	}
        }
        Class<?> screenclazz = screen.getClass();
        parentScreen = null;
        while( parentScreen == null && screenclazz != null ) {
		    for( Field field : screenclazz.getDeclaredFields() ) {
		    	if( GuiScreen.class.isAssignableFrom(field.getType()) ) {
		    		field.setAccessible(true);
		    		try {
		    			//Assume the first declared GuiScreen object is the "parent"
		    			//Might not always work.
						parentScreen = (GuiScreen)field.get(screen);
						break;
					} catch (Exception e) { e.printStackTrace(); }
		    	}
		    }
		    screenclazz = screenclazz.getSuperclass();
        }
        curPoint = Pair.of(	Mouse.getX() * screen.width / this.mc.displayWidth,
        					screen.height - Mouse.getY() * screen.height / this.mc.displayHeight - 1);
        
        if( slot != null && slot.publicGetSize() > 0 ) {
        	slotIndex = 0;
        	slot.select(0, false);
        	onSlot = true;
        }
	}
	
	public void back() {
		if( screen instanceof GuiIngameMenu)
			mc.displayGuiScreen(null);
		else if(parentScreen != null)
			mc.displayGuiScreen(parentScreen);
		
	}

	public void select(boolean state) {
		if( state && onSlot ) {
			slot.select(slotIndex,true);
			return;
		}
		selectDepressed = state;
		if( curPoint != null) {
			if( keyDownField != null)
				try {
					((ByteBuffer)keyDownField.get(null)).put(Keyboard.KEY_RSHIFT,(byte) (shiftDepressed?1:0));
				} catch (Exception e) { }
			if(state)
				mc.currentScreen.mouseGuiDown(curPoint.getLeft(), curPoint.getRight(), 0); //Left click
			else
				mc.currentScreen.mouseGuiUp(  curPoint.getLeft(), curPoint.getRight(), 0); //Left click
		}
	}

	public void altselect(boolean state) {
		altselectDepressed = state;
		if( curPoint != null) {
			if( keyDownField != null)
				try {
					((ByteBuffer)keyDownField.get(null)).put(Keyboard.KEY_RSHIFT,(byte) (shiftDepressed?1:0));
				} catch (Exception e) { }
			if(state)
				mc.currentScreen.mouseGuiDown(curPoint.getLeft(), curPoint.getRight(), 1); //Right click
			else
				mc.currentScreen.mouseGuiUp(  curPoint.getLeft(), curPoint.getRight(), 1); //Right click
		}
	}

	public void shift(boolean state) {
		shiftDepressed = state;
	}
	
	private float dist( Pair<Integer,Integer> a, Pair<Integer,Integer> b, float xScale, float yScale) {
		float x = xScale * (a.getLeft() - b.getLeft());
		float y = yScale * (a.getRight() - b.getRight());
		return (float) Math.sqrt( x * x + y * y);
	}

	public void left() {
		onSlot = false;
        parsePoints();
		if( curPoint != null ) {
			Pair<Integer,Integer> nextBest = null;
			for( Pair<Integer,Integer> point : points ) {
				if( point.getLeft() < curPoint.getLeft() ) {
					if(nextBest == null || dist( nextBest, curPoint, 1, AXIS_PREFERENCE ) > dist( point, curPoint , 1, AXIS_PREFERENCE ) )
						nextBest = point;
				}
			}
			if( nextBest != null) {
				mc.currentScreen.mouseGuiDrag(curPoint.getLeft(), curPoint.getRight());
				curPoint = nextBest;
				mouseto();
			}
		}
	}

	public void right() {
		onSlot = false;
        parsePoints();
		if( curPoint != null ) {
			Pair<Integer,Integer> nextBest = null;
			for( Pair<Integer,Integer> point : points ) {
				if( point.getLeft() > curPoint.getLeft() ) {
					if(nextBest == null || dist( nextBest, curPoint, 1, AXIS_PREFERENCE ) > dist( point, curPoint, 1, AXIS_PREFERENCE ) ) 
						nextBest = point;
				}
			}
			if( nextBest != null) {
				mc.currentScreen.mouseGuiDrag(curPoint.getLeft(), curPoint.getRight());
				curPoint = nextBest;
				mouseto();
			}
		}
	}

	public void down() {
		if( onSlot && slot != null && slotIndex != slot.publicGetSize()- 1 ) {
			slotIndex++;
			int slotY = slot.select(slotIndex, false);
			curPoint = Pair.of( screen.width/2, slotY);
			mouseto();
			return;
		}
		onSlot = false;
        parsePoints();
		if( curPoint != null ) {
			Pair<Integer,Integer> nextBest = null;
			for( Pair<Integer,Integer> point : points ) {
				if( point.getRight() > curPoint.getRight() ) {
					if(nextBest == null || dist( nextBest, curPoint, AXIS_PREFERENCE, 1 ) > dist( point, curPoint, AXIS_PREFERENCE, 1 ) )
						nextBest = point;
				}
			}
			if( nextBest != null) {
				mc.currentScreen.mouseGuiDrag(curPoint.getLeft(), curPoint.getRight());
				curPoint = nextBest;
				mouseto();
			}
		}
	}

	public void up() {
        parsePoints();
		if( curPoint != null ) {
			Pair<Integer,Integer> nextBest = null;
			for( Pair<Integer,Integer> point : points ) {
				if( point.getRight() < curPoint.getRight() ) {
					if(nextBest == null || dist( nextBest, curPoint, AXIS_PREFERENCE, 1 ) > dist( point, curPoint, AXIS_PREFERENCE, 1 ) )
						nextBest = point;
				}
			}
			if( nextBest != null) {

				mc.currentScreen.mouseGuiDrag(curPoint.getLeft(), curPoint.getRight());
				curPoint = nextBest;
				mouseto();
				onSlot = false;
			} else if( slot != null ) {
				if( onSlot && slotIndex != 0 )
					slotIndex--;
				int slotY = slot.select(slotIndex, false);
				curPoint = Pair.of( screen.width/2, slotY);
				mouseto();
				onSlot = true;
			}
		}
		
	}

	private void mouseto() {
		int mouseGUIX = curPoint.getLeft();
		int mouseGUIY = curPoint.getRight();
		
        int mouseScreenX = (int)(mouseGUIX * mc.displayFBWidth / (float)mc.currentScreen.width );
        int mouseScreenY = (int)(mc.displayFBHeight / (float)mc.currentScreen.height * (mc.currentScreen.height - mouseGUIY - 1 ) );
        Mouse.setCursorPosition(mouseScreenX, mouseScreenY);

        int mouseFBX = (int)(mouseScreenX * mc.displayFBWidth / (float)mc.displayWidth );
        int mouseFBY = (int)(mouseScreenY * mc.displayFBHeight / (float)mc.displayHeight );
        mc.resetMousePos( mouseFBX, mouseFBY );
		
		if(altselectDepressed || selectDepressed ) {
			if( keyDownField != null)
				try {
					((ByteBuffer)keyDownField.get(null)).put(Keyboard.KEY_RSHIFT,(byte) (shiftDepressed?1:0));
				} catch (Exception e) { }
			mc.currentScreen.mouseGuiDrag(mouseGUIX, mouseGUIY);
		}
	}


	@SuppressWarnings("unchecked")
	protected void parsePoints() {
		points.clear();
        for (GuiButton button : screen.buttonList ) {
        	if( button.field_146125_m )
        		points.add(Pair.of(button.field_146128_h+5,button.field_146129_i+5));
        }
        if( screen instanceof GuiContainer ) {
        	GuiContainer container = (GuiContainer)screen;
        	int xOffset = 125; //These are the offsets for at least the inventory screen and chest
        	int yOffset = 48; 
        	if( guiLeft != null ) {
        		try {
					xOffset = guiLeft.getInt(container);
					yOffset = guiTop.getInt(container);
				} catch (Exception e) { e.printStackTrace(); }
        	}
        	for( Slot slot : (List<Slot>)container.field_147002_h.inventorySlots )
        	{
        		points.add(Pair.of(xOffset + slot.xDisplayPosition + 8, yOffset + slot.yDisplayPosition + 8 ));
        	}
        }
	}
}

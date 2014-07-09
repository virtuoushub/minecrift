/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.gui;

import java.util.List;

import com.mtbs3d.minecrift.MCController;
import com.mtbs3d.minecrift.MCHydra;
import com.mtbs3d.minecrift.MCMouse;
import com.mtbs3d.minecrift.api.IBasePlugin;
import com.mtbs3d.minecrift.api.PluginManager;
import com.mtbs3d.minecrift.settings.VRSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.src.*;
import net.minecraft.util.StringTranslate;

public class GuiMoveAimSettings extends BaseGuiSettings
{
    /** An array of all of EnumOption's movement options relevant to the hydra. */
    static VRSettings.VrOptions[] hydraMoveAimOptions = new VRSettings.VrOptions[] {
            VRSettings.VrOptions.KEYHOLE_WIDTH,
            VRSettings.VrOptions.KEYHOLE_HEAD_RELATIVE,
            VRSettings.VrOptions.DECOUPLE_LOOK_MOVE,
            VRSettings.VrOptions.JOYSTICK_SENSITIVITY,
            VRSettings.VrOptions.MOVEAIM_HYDRA_USE_CONTROLLER_ONE,
    };
    /** An array of all of EnumOption's movement options relevant to the mouse. */
    static VRSettings.VrOptions[] mouseMoveAimOptions = new VRSettings.VrOptions[] {
            VRSettings.VrOptions.KEYHOLE_WIDTH,
            VRSettings.VrOptions.KEYHOLE_HEIGHT,
            VRSettings.VrOptions.DECOUPLE_LOOK_MOVE,
            VRSettings.VrOptions.PITCH_AFFECTS_CAMERA,
            VRSettings.VrOptions.AIM_PITCH_OFFSET,
    };
    /** An array of all of EnumOption's movement options relevant to the controller. */
    static VRSettings.VrOptions[] controllerMoveAimOptions = new VRSettings.VrOptions[] {
            VRSettings.VrOptions.KEYHOLE_WIDTH,
            VRSettings.VrOptions.KEYHOLE_HEIGHT,
            VRSettings.VrOptions.DECOUPLE_LOOK_MOVE,
            VRSettings.VrOptions.PITCH_AFFECTS_CAMERA,
            VRSettings.VrOptions.JOYSTICK_SENSITIVITY,
            VRSettings.VrOptions.JOYSTICK_DEADZONE,
            VRSettings.VrOptions.JOYSTICK_AIM_TYPE,
    };
	private PluginModeChangeButton pluginModeChangeutton;
	private boolean reinit;

    public GuiMoveAimSettings(GuiScreen par1GuiScreen,
                            VRSettings par2vrSettings)
    {
    	super( par1GuiScreen, par2vrSettings );
        screenTitle = "Move/Look/Aim Controls Configuration";
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.buttonList.clear();
        this.buttonList.add(new GuiButtonEx(202, this.width / 2 - 100, this.height / 6 + 148, "Reset To Defaults"));
        this.buttonList.add(new GuiButtonEx(200, this.width / 2 - 100, this.height / 6 + 168, "Done"));
        if(! ( Minecraft.getMinecraft().lookaimController instanceof MCMouse )  )
        {
        	this.buttonList.add(new GuiButtonEx(203, this.width / 2 - 100, this.height / 6 + 128, "Remap Controls"));
        }
        
        pluginModeChangeutton = new PluginModeChangeButton(201, this.width / 2 - 78, this.height / 6 - 14, (List<IBasePlugin>)(List<?>) PluginManager.thePluginManager.controllerPlugins, this.guivrSettings.controllerPluginID );
        this.buttonList.add(pluginModeChangeutton);

        VRSettings.VrOptions[] var10;

        if( Minecraft.getMinecraft().lookaimController instanceof MCHydra )
        	var10 = hydraMoveAimOptions;
    	else if( Minecraft.getMinecraft().lookaimController instanceof MCController )
    		var10 = controllerMoveAimOptions;
    	else
        	var10 = mouseMoveAimOptions ;
        int var11 = var10.length;

        for (int var12 = 2; var12 < var11 + 2; ++var12)
        {
            VRSettings.VrOptions var8 = var10[var12 - 2];
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10;

            if (var8.getEnumFloat())
            {
                float minValue = 0.0f;
                float maxValue = 1.0f;
                float increment = 0.01f;

                if (var8 == VRSettings.VrOptions.MOVEMENT_MULTIPLIER)
                {
                    minValue = 0.15f;
                    maxValue = 1.0f;
                    increment = 0.01f;
                }
                else if( var8 == VRSettings.VrOptions.JOYSTICK_SENSITIVITY )
                {
                	minValue = 0.5f;
                	maxValue = 10.0f;
                	increment= 0.1f;
                }
                else if( var8 == VRSettings.VrOptions.JOYSTICK_DEADZONE )
                {
                	minValue = 0.0f;
                	maxValue = 0.4f;
                	increment= 0.01f;
                }
                else if( var8 == VRSettings.VrOptions.KEYHOLE_WIDTH)
                {
                	minValue = 0.0f;
                	maxValue = 90f;
                	increment= 1.0f;
                }
                else if( var8 == VRSettings.VrOptions.KEYHOLE_HEIGHT)
                {
                	minValue = 0.0f;
                	maxValue = 180f;
                	increment= 1.0f;
                }
                else if( var8 == VRSettings.VrOptions.AIM_PITCH_OFFSET )
                {
                	minValue = -90f;
                	maxValue = 90f;
                	increment= 1.0f;
                }

                GuiSliderEx slider = new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guivrSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guivrSettings.getOptionFloatValue(var8));
                this.buttonList.add(slider);
            }
            else
            {
                String keyText = this.guivrSettings.getKeyBinding(var8);
                GuiSmallButtonEx smallButton = new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, keyText);
                this.buttonList.add(smallButton);
            }
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        if (reinit)
        {
            initGui();
            reinit = false;
        }
        super.drawScreen(par1,par2,par3);
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        VRSettings.VrOptions num = VRSettings.VrOptions.getEnumOptions(par1GuiButton.id);

        if (par1GuiButton.enabled)
        {
            if (par1GuiButton.id < 200 && par1GuiButton instanceof GuiSmallButtonEx)
            {
                this.guivrSettings.setOptionValue(((GuiSmallButtonEx)par1GuiButton).returnVrEnumOptions(), 1);
                par1GuiButton.displayString = this.guivrSettings.getKeyBinding(VRSettings.VrOptions.getEnumOptions(par1GuiButton.id));
            }
            else if (par1GuiButton.id == 200)
            {
                this.guivrSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }
            else if (par1GuiButton.id == 201) // Mode Change
            {
            	this.guivrSettings.controllerPluginID = pluginModeChangeutton.getSelectedID();
                this.guivrSettings.saveOptions();
                Minecraft.getMinecraft().lookaimController = PluginManager.configureController(this.guivrSettings.controllerPluginID);
            	this.reinit = true;
            }
            else if (par1GuiButton.id == 202) // Defaults
            {
                if (Minecraft.getMinecraft().lookaimController instanceof MCHydra )
                {
                    this.guivrSettings.aimKeyholeWidthDegrees = 90f;
                    this.guivrSettings.keyholeHeadRelative = true;
                    this.guivrSettings.lookMoveDecoupled = false;
                    this.guivrSettings.joystickSensitivity = 3f;
                }
                else
                {
                    this.guivrSettings.aimKeyholeWidthDegrees = 0f;
                    this.guivrSettings.keyholeHeight = 0f;
                    this.guivrSettings.keyholeHeadRelative = true;
                    this.guivrSettings.lookMoveDecoupled = false;
                    this.guivrSettings.allowMousePitchInput = true;
                }
                this.guivrSettings.aimPitchOffset = 0;
                this.guivrSettings.saveOptions();
                this.reinit = true;
            }
            else if (par1GuiButton.id == 203) // Remap
            {
                this.guivrSettings.saveOptions();
                this.mc.displayGuiScreen( new GuiVRControls(this, this.guivrSettings));
            }
        }
    }

    @Override
    protected String[] getTooltipLines(String displayString, int buttonId)
    {
        VRSettings.VrOptions e = VRSettings.VrOptions.getEnumOptions(buttonId);
        if( e != null )
            switch(e)
            {
                case JOYSTICK_SENSITIVITY:
                    return new String[] {
                            "Joystick Sensitivity",
                            "The higher the value, the more you turn.",
                            "  Doesn't affect forward/backward speed",
                            "  Recommended value: 4.0" } ;
                case JOYSTICK_DEADZONE:
                    return new String[] {
                            "The higher the value, small joystick motions are ignored",
                            "  Recommended value: 0.1" } ;
                case JOYSTICK_AIM_TYPE:
                    return new String[] {
                            "How joystick Aiming works.",
                            "  Keyhole (tight): the crosshair stays in place ",
                            "     like a mouse cursor",
                            "  Keyhole (loose): You don't push the crosshair outside",
                            "     the keyhole, but it stays after turning head back.",
                            "  Recenter: the crosshair moves back to center when ",
                            "     you let off the joystick." } ;
                case DECOUPLE_LOOK_MOVE:
                    return new String[] {
                            "Decouple Movement from Looking - \"Tank mode\"",
                            "  OFF: You always move in the direction you are facing",
                            "  ON: You move in the direction of the GUI - turning",
                            "     your head will not affect movement direction",
                            "  Recommended value: ON" } ;
                case KEYHOLE_WIDTH:
                    return new String[] {
                            "Allows the mouse some flexibility within a \"keyhole\"",
                            "  that doesn't turn the player.",
                            "If set to \"Fully Coupled\", any mouse movement will",
                            "  turn the camera.",
                            "Otherwise, this value is the horizontal width (in degrees)",
                            "  of the keyhole in which the crosshair can freely move.",
                            "  Recommended value: > 60°"} ;
                case KEYHOLE_HEIGHT:
                    return new String[] {
                            "Allows the mouse some flexibility within a \"keyhole\"",
                            "  that allows the mouse to be constrained.",
                            "If set to \"Fully Coupled\", any mouse movement will",
                            "  adjust the camera (if allowed).",
                            "Otherwise, this value is the vertical height (in degrees)",
                            "  of the keyhole in which the crosshair can freely move.",
                            "  Recommended value: 45°"} ;
                case KEYHOLE_HEAD_RELATIVE:
                    return new String[] {
                            "Determines if the \"keyhole\" used for aiming moves ",
                            "  with your head.",
                            "  If YES, you'll need to move the aiming hydra along",
                            "  with your head in order to prevent counter-rotation.",
                            "  Recommended value: YES"} ;
                case PITCH_AFFECTS_CAMERA:
                    return new String[] {
                            "Adjusts whether the mouse can control the camera pitch",
                            "  OFF: No, the only way to control pitch is your head",
                            "  ON: Yes, moving the mouse up and down will move the",
                            "     camera up and down", };
                case MOVEAIM_HYDRA_USE_CONTROLLER_ONE:
                    return new String[] {
                            "Sets the controller used for move/aim control.",
                            "  Sets which controller is used. Will be the opposite",
                            "  controller to that used for positional tracking."
                    } ;
                case AIM_PITCH_OFFSET:
                	return new String[] {
                			"Vertical Crosshair Offset",
                			"  If your headset doesn't sit straight on your face,",
                			"  this setting allows you to set the 'fixed' crosshair",
                			"  lower or higher than center of the screen.",
                			"  Recommended value: 0.0"
                	} ;
                default:
                    return null;
            }
        else
            switch(buttonId)
            {
                case 201:
                    return new String[] {
                            "Changes the method for controlling aiming, looking, ",
                            "  and movement."
                    };
                default:
                    return null;
            }
    }
}

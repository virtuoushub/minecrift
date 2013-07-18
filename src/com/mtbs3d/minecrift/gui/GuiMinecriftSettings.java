/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.gui;

import net.minecraft.src.EnumOptions;
import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.StringTranslate;

public class GuiMinecriftSettings extends BaseGuiSettings
{
    static VROption[] vrOffDeviceList = new VROption[]
        {
            new VROption(201,                             VROption.Position.POS_LEFT,   1,    VROption.DISABLED, "Player Preferences..."),
            new VROption(202,                             VROption.Position.POS_RIGHT,  1,    VROption.DISABLED, "HUD / Overlay Settings..."),
            new VROption(206,                             VROption.Position.POS_LEFT,   2,    VROption.DISABLED, "Optics/Rendering..."),
            new VROption(203,                             VROption.Position.POS_RIGHT,  2,    VROption.DISABLED, "Device Calibration..."),
            new VROption(205,                             VROption.Position.POS_LEFT,   3.25f, VROption.DISABLED, "Head Orientation Tracking..."),
            new VROption(EnumOptions.VR_HEAD_ORIENTATION, VROption.Position.POS_RIGHT,  3.25f, VROption.DISABLED, null),
            new VROption(207,                             VROption.Position.POS_LEFT,   4.25f, VROption.DISABLED, "Head Position Tracking..."),
            new VROption(EnumOptions.VR_HEAD_POSITION,    VROption.Position.POS_RIGHT,  4.25f, VROption.DISABLED, null),
            new VROption(208,                             VROption.Position.POS_LEFT,   5.25f, VROption.DISABLED, "Move/Aim Control..."),
            new VROption(EnumOptions.VR_CONTROLLER,       VROption.Position.POS_RIGHT,  5.25f, VROption.DISABLED, null),
        };

    static VROption[] vrOnDeviceList = new VROption[]
        {
            new VROption(201,                             VROption.Position.POS_LEFT,   1,    VROption.ENABLED, "Player Preferences..."),
            new VROption(202,                             VROption.Position.POS_RIGHT,  1,    VROption.ENABLED, "HUD / Overlay Settings..."),
            new VROption(206,                             VROption.Position.POS_LEFT,   2,    VROption.ENABLED, "Optics/Rendering..."),
            new VROption(203,                             VROption.Position.POS_RIGHT,  2,    VROption.ENABLED, "Device Calibration..."),
            new VROption(205,                             VROption.Position.POS_LEFT,   3.25f, VROption.ENABLED, "Head Orientation Tracking..."),
            new VROption(EnumOptions.VR_HEAD_ORIENTATION, VROption.Position.POS_RIGHT,  3.25f, VROption.DISABLED, null),
            new VROption(207,                             VROption.Position.POS_LEFT,   4.25f, VROption.ENABLED, "Head Position Tracking..."),
            new VROption(EnumOptions.VR_HEAD_POSITION,    VROption.Position.POS_RIGHT,  4.25f, VROption.DISABLED, null),
            new VROption(208,                             VROption.Position.POS_LEFT,   5.25f, VROption.ENABLED, "Move/Aim Control..."),
            new VROption(EnumOptions.VR_CONTROLLER,       VROption.Position.POS_RIGHT,  5.25f, VROption.DISABLED, null),
        };

    /** An array of all of EnumOption's video options. */

    public GuiMinecriftSettings( GuiScreen par1GuiScreen,
                                GameSettings par2GameSettings)
    {
    	super( par1GuiScreen, par2GameSettings );
    	screenTitle = "VR Settings";
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        StringTranslate stringTranslate = StringTranslate.getInstance();
        this.buttonList.clear();
        this.buttonList.add(new GuiSmallButtonEx(EnumOptions.USE_VR.returnEnumOrdinal(), this.width / 2 - 155 + 1 * 160 / 2, this.height / 6 - 14, EnumOptions.USE_VR, this.guiGameSettings.getKeyBinding(EnumOptions.USE_VR)));
        this.buttonList.add(new GuiButtonEx(211, this.width / 2 - 100, this.height / 6 + 128, stringTranslate.translateKey("Reset Origin")));
        this.buttonList.add(new GuiButtonEx(210, this.width / 2 - 100, this.height / 6 + 148, stringTranslate.translateKey("Recalibrate...")));
        this.buttonList.add(new GuiButtonEx(200, this.width / 2 - 100, this.height / 6 + 168, stringTranslate.translateKey("gui.done")));
        VROption[] buttons = null;
        if (this.guiGameSettings.useVRRenderer)
            buttons = vrOnDeviceList;
        else
            buttons = vrOffDeviceList;

        for (VROption var8 : buttons)
        {
            int width = var8.getWidth(this.width);
            int height = var8.getHeight(this.height);

//            if (var8._e == EnumOptions.DUMMY)
//                continue;

//            if (var8._e.getEnumFloat())
//            {
//                float minValue = 0.0f;
//                float maxValue = 1.0f;
//                float increment = 0.01f;
//
//                GuiSliderEx slider = new GuiSliderEx(var8.getOrdinal(), width, height, var8._e, var8.getButtonText(), minValue, maxValue, increment, 0.0f);
//                slider.enabled = var8._enabled;
//                this.buttonList.add(slider);
//            }
//            else
            {
                GuiSmallButtonEx button = new GuiSmallButtonEx(var8.getOrdinal(), width, height, var8._e, var8.getButtonText());
                button.enabled = var8._enabled;
                this.buttonList.add(button);
            }
        }
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        if (par1GuiButton.enabled)
        {
            if (par1GuiButton.id < 200 && par1GuiButton instanceof GuiSmallButtonEx)
            {
                EnumOptions num = EnumOptions.getEnumOptions(par1GuiButton.id);
                this.guiGameSettings.setOptionValue(((GuiSmallButtonEx)par1GuiButton).returnEnumOptions(), 1);
                par1GuiButton.displayString = this.guiGameSettings.getKeyBinding(EnumOptions.getEnumOptions(par1GuiButton.id));

                if (num == EnumOptions.USE_VR)
                {
                    if (vrRenderer != null)
                        vrRenderer._FBOInitialised = false;
                    this.reinit = true;
                }
            }
            else if (par1GuiButton.id == 201)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiPlayerPreferenceSettings(this, this.guiGameSettings));
            }
            else if (par1GuiButton.id == 202)
            {
                if( mc.headTracker != null )
                {
                    this.mc.gameSettings.saveOptions();
                    this.mc.displayGuiScreen(new GuiHUDSettings(this, this.guiGameSettings));
                }
            }
            else if (par1GuiButton.id == 203)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiCalibrationSettings(this, this.guiGameSettings));
            }
            else if (par1GuiButton.id == 205)
            {
            	if( mc.headTracker != null )
            	{
	                this.mc.gameSettings.saveOptions();
	                this.mc.displayGuiScreen(new GuiHeadOrientationSettings(this, this.guiGameSettings));
            	}
            } 
            else if (par1GuiButton.id == 206)
            {
            	if( mc.headTracker != null && mc.hmdInfo != null && mc.positionTracker != null )
            	{
	                this.mc.gameSettings.saveOptions();
	                this.mc.displayGuiScreen(new GuiRenderOpticsSettings(this, this.guiGameSettings));
            	}
            } 
            else if (par1GuiButton.id == 207)
            {
            	if( mc.positionTracker != null )
            	{
	                this.mc.gameSettings.saveOptions();
	                this.mc.displayGuiScreen(new GuiHeadPositionSettings(this, this.guiGameSettings));
            	}
            } 
            else if (par1GuiButton.id == 208)
            {
            	if( mc.lookaimController != null )
            	{
	                this.mc.gameSettings.saveOptions();
	                this.mc.displayGuiScreen(new GuiMoveAimSettings(this, this.guiGameSettings));
            	}
            }
            else if (par1GuiButton.id == 210)
            {
                this.mc.gameSettings.saveOptions();
                if (vrRenderer != null)
                    vrRenderer.startCalibration();
            }
            else if (par1GuiButton.id == 211)
            {
                this.mc.gameSettings.saveOptions();
                this.guiGameSettings.posTrackResetPosition = true;
                if (vrRenderer != null)
                    vrRenderer.resetGuiYawOrientation();
            }
            else if (par1GuiButton.id == 200)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }
        }
    }

    @Override
    protected String[] getTooltipLines(String displayString, int buttonId)
    {
    	EnumOptions e = EnumOptions.getEnumOptions(buttonId);
    	if( e != null )
    	switch(e)
    	{
    	case USE_VR:
    		return new String[] {
				"Whether to enable all the fun new Virtual Reality features",
				"  ON: Yay Fun!",
				"  OFF: Sad vanilla panda: gameplay unchanged"
    		};
    	default:
    		return null;
    	}
    	else
    	switch(buttonId)
    	{
            case 201:
                return new String[] {
                        "Open this configuration screen to adjust the Player",
                        "  avatar preferences, select Oculus profiles etc.",
                        "  Ex: IPD, Player (Eye) Height"
                };
            case 202:
                return new String[] {
                        "Open this configuration screen to adjust the Head",
                        "Up Display (HUD) overlay properties.",
                        "  Ex: HUD size, HUD distance, Crosshair options"
                };
            case 203:
                return new String[] {
                        "Open this configuration screen to adjust device",
                        "calibration settings.",
                        "  Ex: Initial calibration time"
                };
	    	case 205:
	    		return new String[] {
	    			"Open this configuration screen to adjust the Head",
	    			"  Tracker orientation (direction) settings. ",
	    			"  Ex: Head Tracking Selection (Hydra/Oculus), Prediction"
	    		};
	    	case 206:
	    		return new String[] {
	    			"Open this configuration screen to adjust the Head ",
	    			"  Mounted Display optics or other rendering features.",
	    			"  Ex: FOV, Distortion, FSAA, Chromatic Abberation"
	    		};
	    	case 207:
	    		return new String[] {
	    			"Open this configuration screen to adjust the Head",
	    			"  Tracker position settings. ",
	    			"  Ex: Head Position Selection (Hydra/None), " ,
	    			"       Hydra head placement (left, right, top etc)"
	    		};
	    	case 208:
	    		return new String[] {
	    			"Open this configuration screen to adjust how the ",
	    			"  character is controlled. ",
	    			"  Ex: Look/move/aim decouple, joystick sensitivty, " ,
	    			"     Keyhole width, Mouse-pitch-affects camera" ,
	    		};
            case 211:
                return new String[] {
                        "Resets the origin point to your current head",
                        "position."
                };
    		default:
    			return null;
    	}
    }

}

/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.gui;

import java.util.List;

import com.mtbs3d.minecrift.api.BasePlugin;
import com.mtbs3d.minecrift.api.IBasePlugin;

import com.mtbs3d.minecrift.api.PluginManager;
import net.minecraft.src.*;

public class GuiHeadOrientationSettings  extends BaseGuiSettings implements GuiEventEx
{
    /** An array of all of EnumOption's video options. */
    static EnumOptions[] headOrientationOptions = new EnumOptions[] {
            EnumOptions.HEAD_TRACKING,
            EnumOptions.HEAD_TRACK_PREDICTION,
            EnumOptions.HEAD_TRACK_SENSITIVITY,
            EnumOptions.HEAD_TRACK_PREDICTION_TIME,
    };
	private PluginModeChangeButton pluginModeChangeutton;
    public GuiHeadOrientationSettings(GuiScreen par1GuiScreen,
                                       GameSettings par2GameSettings)
    {
    	super( par1GuiScreen, par2GameSettings );
        screenTitle = "Head Tracking Settings";
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        StringTranslate stringTranslate = StringTranslate.getInstance();
        this.buttonList.clear();
        this.buttonList.add(new GuiButtonEx(200, this.width / 2 - 100, this.height / 6 + 168, stringTranslate.translateKey("gui.done")));
        this.buttonList.add(new GuiButtonEx(201, this.width / 2 - 100, this.height / 6 + 128, "Reset to defaults"));
        this.buttonList.add(new GuiButtonEx(202, this.width / 2 - 100, this.height / 6 + 148, "Recalibrate (Look left, right, up)"));
        pluginModeChangeutton = new PluginModeChangeButton(203, this.width / 2 - 78, this.height / 6 - 14, (List<IBasePlugin>)(List<?>) PluginManager.thePluginManager.orientPlugins, this.guiGameSettings.headTrackerPluginID );
        this.buttonList.add(pluginModeChangeutton);
        EnumOptions[] var10 = headOrientationOptions;
        int var11 = var10.length;

        for (int var12 = 2; var12 < var11 + 2; ++var12)
        {
            EnumOptions var8 = var10[var12-2];
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10;

            if (var8.getEnumFloat())
            {
                float minValue = 0.0f;
                float maxValue = 1.0f;
                float increment = 0.001f;

                if (var8 == EnumOptions.HEAD_TRACK_SENSITIVITY)
                {
                    minValue = 0.5f;
                    maxValue = 3.0f;
                    increment = 0.01f;
                }
                else if (var8 == EnumOptions.HEAD_TRACK_PREDICTION_TIME)
                {
                    minValue = 0.001f;
                    maxValue = 0.100f;
                    increment = 0.001f;
                }

                GuiSliderEx slider = new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guiGameSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guiGameSettings.getOptionFloatValue(var8));
                slider.setEventHandler(this);
                this.buttonList.add(slider);
            }
            else
            {
                this.buttonList.add(new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, this.guiGameSettings.getKeyBinding(var8)));
            }
        }
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        EnumOptions num = EnumOptions.getEnumOptions(par1GuiButton.id);

        if (par1GuiButton.enabled)
        {
            if (par1GuiButton.id < 200 && par1GuiButton instanceof GuiSmallButtonEx)
            {
                this.guiGameSettings.setOptionValue(((GuiSmallButtonEx)par1GuiButton).returnEnumOptions(), 1);
                par1GuiButton.displayString = this.guiGameSettings.getKeyBinding(EnumOptions.getEnumOptions(par1GuiButton.id));

                if (num == EnumOptions.HEAD_TRACK_PREDICTION)
                {
                    mc.headTracker.setPrediction(this.mc.gameSettings.headTrackPredictionTimeSecs, this.mc.gameSettings.useHeadTrackPrediction);
                }
            }
            else if (par1GuiButton.id == 200)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }
            else if (par1GuiButton.id == 201)
            {
			    this.mc.gameSettings.useHeadTracking = true;
			    this.mc.gameSettings.useHeadTrackPrediction = true;
                this.mc.gameSettings.headTrackPredictionTimeSecs = 0.015f;
                mc.headTracker.setPrediction(this.mc.gameSettings.headTrackPredictionTimeSecs, this.mc.gameSettings.useHeadTrackPrediction);
			    this.mc.gameSettings.headTrackSensitivity = 1.0f;
            }
            else if (par1GuiButton.id == 202)
            {
	            if (vrRenderer != null)
	            	vrRenderer.startCalibration();
            }
            else if (par1GuiButton.id == 203) // Mode Change
            {
            	this.mc.gameSettings.headTrackerPluginID = pluginModeChangeutton.getSelectedID();
                this.mc.gameSettings.saveOptions();
            	this.mc.headTracker = PluginManager.configureOrientation(this.mc.gameSettings.headTrackerPluginID);
            }
        }
    }

    @Override
    public void event(int id, EnumOptions enumm)
    {
        if (enumm == EnumOptions.HEAD_TRACK_PREDICTION_TIME)
        {
            mc.headTracker.setPrediction(this.mc.gameSettings.headTrackPredictionTimeSecs, this.mc.gameSettings.useHeadTrackPrediction);
        }
    }

    @Override
    protected String[] getTooltipLines(String displayString, int buttonId)
    {
    	EnumOptions e = EnumOptions.getEnumOptions(buttonId);
    	if( e != null )
    	switch(e)
    	{
    	case HEAD_TRACKING:
    		return new String[] {
    				"If head tracking should be enabled or not", 
    				"  OFF: No head tracking",
    				"  ON: Head tracking enabled",
    				"  Recommended: ON"} ;
    	case HEAD_TRACK_PREDICTION:
    		return new String[] {
    				"For the Oculus Rift, enable Prediction?", 
    				" OFF: Prediction disabled",
    				" ON:  Prediction enabled",
    				" Recommended value: ON" } ;
    	case HEAD_TRACK_SENSITIVITY:
    		return new String[] {
    				"In-game camera orientation multiplied by this value.", 
    				"  Recommended value: 1.0"} ;   		
    	case HEAD_TRACK_PREDICTION_TIME:
    		return new String[] {
    				"Number of seconds to predict motion. Higher values will",
    				"enhance the perceived precision of slow movements, but ",
    				"cause issues with sudden movements",
    				"  Recommended value: 0.015"};
    	default:
    		return null;
    	}
    	else
    	switch(buttonId)
    	{
	    	case 201:
	    		return new String[] {
	    			"Resets all values on this screen to their defaults"
	    		};
	    	case 202:
	    		return new String[] {
	    			"Starts calibration of the Oculus Rift headset",
	    			"  Press this button while facing forward.",
	    			"  Then, look to the left.",
	    			"  Then, look to the right.",
	    			"  Then, look up."
	    		};
    		default:
    			return null;
    	}
    }
}

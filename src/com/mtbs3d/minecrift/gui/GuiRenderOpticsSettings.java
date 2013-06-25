/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.gui;

import net.minecraft.src.*;

public class GuiRenderOpticsSettings  extends BaseGuiSettings implements GuiEventEx
{
    /** An array of all of EnumOption's video options. */
    static EnumOptions[] minecriftDisplayOptions = new EnumOptions[] {
            EnumOptions.IPD,
            EnumOptions.FOV_SCALE_FACTOR,
            EnumOptions.USE_DISTORTION,
            EnumOptions.DISTORTION_FIT_POINT,
            EnumOptions.SUPERSAMPLING,
            EnumOptions.SUPERSAMPLE_SCALEFACTOR,
            EnumOptions.CHROM_AB_CORRECTION,
    };


    public GuiRenderOpticsSettings(GuiScreen par1GuiScreen, GameSettings par2GameSettings)
    {
    	super( par1GuiScreen, par2GameSettings);
        screenTitle = "Oculus Settings";
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        StringTranslate stringTranslate = StringTranslate.getInstance();
        // this.screenTitle = var1.translateKey("options.videoTitle");
        this.buttonList.clear();
        this.buttonList.add(new GuiButtonEx(200, this.width / 2 - 100, this.height / 6 + 168, stringTranslate.translateKey("gui.done")));
        this.buttonList.add(new GuiButtonEx(201, this.width / 2 - 100, this.height / 6 + 128, "Reset"));
        this.buttonList.add(new GuiButtonEx(202, this.width / 2 - 100, this.height / 6 + 148, "Recalibrate (Look left, right, up)"));
        int var9 = 0;
        EnumOptions[] var10 = minecriftDisplayOptions;
        int var11 = var10.length;

        for (int var12 = 0; var12 < var11; ++var12)
        {
            EnumOptions var8 = var10[var12];
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10;

            if (var8.getEnumFloat())
            {
                float minValue = 0.0f;
                float maxValue = 1.0f;
                float increment = 0.001f;

                if (var8 == EnumOptions.IPD)
                {
                    minValue = 0.055f;
                    maxValue = 0.075f;
                    increment = 0.0001f;
                }
                else if (var8 == EnumOptions.FOV_SCALE_FACTOR)
                {
                    minValue = 0.5f;
                    maxValue = 1.5f;
                    increment = 0.01f;
                }
                else if (var8 == EnumOptions.SUPERSAMPLE_SCALEFACTOR)
                {
                    minValue = 1.5f;
                    maxValue = 4.0f;
                    increment = 0.5f;
                }
                else if (var8 == EnumOptions.DISTORTION_FIT_POINT)
                {
                    minValue = 0.0f;
                    maxValue = 14.0f;
                    increment = 1.0f;
                }
                GuiSliderEx slider = new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guiGameSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guiGameSettings.getOptionFloatValue(var8));
                slider.setEventHandler(this);
                this.buttonList.add(slider);
            }
            else
            {
                this.buttonList.add(new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, this.guiGameSettings.getKeyBinding(var8)));
            }

            ++var9;
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
            }
            else if (par1GuiButton.id == 200)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }
            else if (par1GuiButton.id == 201)
            {
			    this.mc.gameSettings.useDistortion = true;
			    this.mc.gameSettings.ipd = 0.0635F;
                mc.hmdInfo.setIPD(this.mc.gameSettings.ipd);
			    this.mc.gameSettings.useChromaticAbCorrection = false;
			    this.mc.gameSettings.fovScaleFactor = 1.0f;
			    this.mc.gameSettings.distortionFitPoint = 5;
			    this.mc.gameSettings.useSupersample = false;
			    this.mc.gameSettings.superSampleScaleFactor = 2.0f;
	            if (vrRenderer != null)
	                vrRenderer._FBOInitialised = false;
			    this.mc.setUseVRRenderer(mc.gameSettings.useVRRenderer);
			    this.guiGameSettings.saveOptions();
            }
            else if (par1GuiButton.id == 202)
            {
	            if (vrRenderer != null)
	            	vrRenderer.startCalibration();
            }

            if (num == EnumOptions.USE_DISTORTION ||
	            num == EnumOptions.SUPERSAMPLING ||
	            num == EnumOptions.IPD ||
	            num == EnumOptions.CHROM_AB_CORRECTION)
	        {
	            if (vrRenderer != null)
	                vrRenderer._FBOInitialised = false;
	        }
        }
    }

    @Override
    public void event(int id, EnumOptions enumm)
    {
        if (enumm == EnumOptions.IPD)
        {
            mc.hmdInfo.setIPD(this.mc.gameSettings.ipd);
        }
        if (enumm == EnumOptions.DISTORTION_FIT_POINT ||
            enumm == EnumOptions.SUPERSAMPLE_SCALEFACTOR)
        {
            if (vrRenderer != null)
                vrRenderer._FBOInitialised = false;
        }
    }

    @Override
    protected String[] getTooltipLines(String displayString, int buttonId)
    {
    	EnumOptions e = EnumOptions.getEnumOptions(buttonId);
    	if( e != null )
    	switch(e)
    	{
    	case CHROM_AB_CORRECTION:
    		return new String[] {
    				"Chromatic aberration correction", 
    				"Corrects for color distortion due to lenses", 
    				"  OFF - no correction", 
    				"  ON - correction applied"} ;
    	case IPD:
    		return new String[] {
    				"Interpupillary Distance", 
    				"  How far apart your eye pupils are, in millimeters"} ;
    	case FOV_SCALE_FACTOR:
    		return new String[] {
    				"Set this to override the computed Field-of-View", 
    				"  You might set this if your eyes aren't at the usual", 
    				"  distance from the lenses (closer or further away)"} ;
    		
    	case USE_DISTORTION:
    		return new String[] {
    				"Apply barrel distortion to counteract lenses",
    				"  ON: apply distortion",
    				"  OFF: leave disabled"};
    	case DISTORTION_FIT_POINT:
    		return new String[] {
    				"The amount of space around the peripheral to leave empty",
    				"  Lower values render more peripherial view. Going too" ,
    				"    low negatively impacts performance.",
    				"  Higher values limit the amount of rendering if the FOV",
    				"    is constrained for some reason (e.g. wearing glasses)"
    				};
    	case SUPERSAMPLING:
    		return new String[] {
    				"Full-Screen AntiAliasing (supersampling)",
    				"  Recommended: ON; greatly improves visual quality",
    				"  ON:  game is rendered at a higher resolution",
    				"  OFF: game is rendered at the native resolution"};
    	case SUPERSAMPLE_SCALEFACTOR:
    		return new String[] {
    				"Full-Screen AntiAliasing Render Scale",
    				"  What multiple of native resolution should be rendered?",
    				"  Recommended value: 2.0"};
    	default:
    		return null;
    	}
    	else
    	switch(buttonId)
    	{
	    	case 201:
	    		return new String[] {
	    			"Resets all values to default"
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

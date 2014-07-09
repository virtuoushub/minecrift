/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.gui;

import com.mtbs3d.minecrift.settings.VRSettings;

import de.fruitfly.ovr.IOculusRift;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.*;
import net.minecraft.util.StringTranslate;

public class GuiRenderOpticsSettings  extends BaseGuiSettings implements GuiEventEx
{
    /** An array of all of EnumOption's video options. */
    static VRSettings.VrOptions[] minecriftDisplayOptions = new VRSettings.VrOptions[] {
            VRSettings.VrOptions.CHROM_AB_CORRECTION,
            VRSettings.VrOptions.TIMEWARP,
            VRSettings.VrOptions.VIGNETTE,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.USE_QUATERNIONS,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.SUPERSAMPLING,
            VRSettings.VrOptions.SUPERSAMPLE_SCALEFACTOR,
            VRSettings.VrOptions.FXAA,
    };

    GameSettings settings;

    public GuiRenderOpticsSettings(GuiScreen par1GuiScreen, VRSettings par2vrSettings, GameSettings gameSettings)
    {
    	super( par1GuiScreen, par2vrSettings);
        screenTitle = "Optics / Render Settings";
        settings = gameSettings;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        // this.screenTitle = var1.translateKey("options.videoTitle");
        this.buttonList.clear();
        this.buttonList.add(new GuiButtonEx(200, this.width / 2 - 100, this.height / 6 + 168, "Done"));
        this.buttonList.add(new GuiButtonEx(201, this.width / 2 - 100, this.height / 6 + 148, "Reset To Defaults"));

        VRSettings.VrOptions[] var10 = minecriftDisplayOptions;
        int var11 = var10.length;

        for (int var12 = 0; var12 < var11; ++var12)
        {
            VRSettings.VrOptions var8 = var10[var12];
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10;

            if (var8 == VRSettings.VrOptions.DUMMY)
                continue;

            if (var8.getEnumFloat())
            {
                float minValue = 0.0f;
                float maxValue = 1.0f;
                float increment = 0.001f;

                if (var8 == VRSettings.VrOptions.FOV_SCALE_FACTOR)
                {
                    minValue = 0.5f;
                    maxValue = 1.5f;
                    increment = 0.01f;
                }
                if (var8 == VRSettings.VrOptions.LENS_SEPARATION_SCALE_FACTOR)
                {
                    minValue = 0.5f;
                    maxValue = 1.5f;
                    increment = 0.001f;
                }
                if (var8 == VRSettings.VrOptions.SUPERSAMPLE_SCALEFACTOR)
                {
                    minValue = 1.5f;
                    maxValue = 4.0f;
                    increment = 0.5f;
                }
                if (var8 == VRSettings.VrOptions.DISTORTION_FIT_POINT)
                {
                    minValue = 0.0f;
                    maxValue = 14.0f;
                    increment = 1.0f;
                }
                GuiSliderEx slider = new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guivrSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guivrSettings.getOptionFloatValue(var8));
                slider.setEventHandler(this);
                slider.enabled = getEnabledState(var8);
                this.buttonList.add(slider);
            }
            else
            {
                GuiSmallButtonEx button = null;
                String keyBinding = this.guivrSettings.getKeyBinding(var8);
                if (var8 == VRSettings.VrOptions.DELAYED_RENDER && !settings.enableVsync)
                {
                    button = new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, "Render Mode: Immediate");
                }
                else
                {
                    button = new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, keyBinding);
                }
                button.enabled = getEnabledState(var8);
                this.buttonList.add(button);
            }
        }
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        VRSettings.VrOptions num = VRSettings.VrOptions.getEnumOptions(par1GuiButton.id);
        Minecraft minecraft = Minecraft.getMinecraft();

        if (par1GuiButton.enabled)
        {
            if (par1GuiButton.id < 200 && par1GuiButton instanceof GuiSmallButtonEx)
            {
                this.guivrSettings.setOptionValue(((GuiSmallButtonEx)par1GuiButton).returnVrEnumOptions(), 1);
                par1GuiButton.displayString = this.guivrSettings.getKeyBinding(VRSettings.VrOptions.getEnumOptions(par1GuiButton.id));
            }
            else if (par1GuiButton.id == 200)
            {
                minecraft.vrSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }
            else if (par1GuiButton.id == 201)
            {
                minecraft.vrSettings.useDistortion = true;
                minecraft.vrSettings.useTimewarp = true;
                minecraft.vrSettings.useVignette = true;
                minecraft.vrSettings.useQuaternions = false;
                minecraft.vrSettings.useChromaticAbCorrection = true;
                minecraft.vrSettings.fovScaleFactor = 1.0f;
                minecraft.vrSettings.lensSeparationScaleFactor = 1.0f;
                minecraft.vrSettings.distortionFitPoint = 5;
                minecraft.vrSettings.frameTimingEnableVsyncSleep = false;
                minecraft.vrSettings.useSupersample = false;
                minecraft.vrSettings.superSampleScaleFactor = 2.0f;
                minecraft.vrSettings.useDistortionTextureLookupOptimisation = false;
                minecraft.vrSettings.useFXAA = false;
                minecraft.vrSettings.setAspectRatioCorrectionMode(IOculusRift.AspectCorrectionType.CORRECTION_AUTO);
                minecraft.reinitFramebuffers = true;
			    Minecraft.getMinecraft().setUseVRRenderer(Minecraft.getMinecraft().vrSettings.useVRRenderer);
			    this.guivrSettings.saveOptions();
            }

            if (num == VRSettings.VrOptions.USE_DISTORTION ||
	            num == VRSettings.VrOptions.SUPERSAMPLING ||
	            num == VRSettings.VrOptions.CHROM_AB_CORRECTION ||
                num == VRSettings.VrOptions.TIMEWARP ||
                num == VRSettings.VrOptions.VIGNETTE ||
                num == VRSettings.VrOptions.TEXTURE_LOOKUP_OPT ||
                num == VRSettings.VrOptions.FXAA ||
                num == VRSettings.VrOptions.ASPECT_RATIO_CORRECTION)
	        {
                minecraft.reinitFramebuffers = true;
	        }
        }
    }

    @Override
    public void event(int id, VRSettings.VrOptions enumm)
    {
        if (enumm == VRSettings.VrOptions.DISTORTION_FIT_POINT ||
            enumm == VRSettings.VrOptions.SUPERSAMPLE_SCALEFACTOR ||
            enumm == VRSettings.VrOptions.FOV_SCALE_FACTOR ||
            enumm == VRSettings.VrOptions.LENS_SEPARATION_SCALE_FACTOR)
        {
            Minecraft.getMinecraft().reinitFramebuffers = true;
        }
    }

    @Override
    protected String[] getTooltipLines(String displayString, int buttonId)
    {
        VRSettings.VrOptions e = VRSettings.VrOptions.getEnumOptions(buttonId);
    	if( e != null )
    	switch(e)
    	{
    	case CHROM_AB_CORRECTION:
    		return new String[] {
    				"Chromatic aberration correction", 
    				"Corrects for color distortion due to lenses", 
    				"  OFF - no correction", 
    				"  ON - correction applied"} ;
    	case FOV_SCALE_FACTOR:
            return new String[] {
                    "Set this to override the computed Field-of-View",
                    "  You might set this if your eyes aren't at the usual",
                    "  distance from the lenses (closer or further away)"} ;
        case LENS_SEPARATION_SCALE_FACTOR:
            return new String[] {
                    "[Advanced] Allows you to set the lens separation",
                    "scale factor. Useful for modified HMDs with a",
                    "different lens separation distance from standard.",
                    "Defaults to 1.0 (recommended)."
            };
    	case USE_DISTORTION:
    		return new String[] {
    				"Apply barrel distortion to counteract lenses",
    				"  ON: apply distortion",
    				"  OFF: leave disabled"};
    	case DISTORTION_FIT_POINT:
    		return new String[] {
    				"The amount of space around the peripheral to leave empty",
    				"  Lower values render more peripheral view. Going too" ,
    				"    low negatively impacts performance.",
    				"  Higher values limit the amount of rendering if the FOV",
    				"    is constrained for some reason (e.g. wearing glasses)"
    				};
        case TEXTURE_LOOKUP_OPT:
            return new String[] {
                    "Method for applying the barrel distortion lens.",
                    "  Brute Force: Distortion is calculated every frame.",
                    "  Texture Lookup: Distortion is pre-calculated.",
                    "Choose whichever option gives the highest FPS on your",
                    "graphics hardware. Pre-calculation is not necessarily",
                    "faster!"
                    };
    	case SUPERSAMPLING:
    		return new String[] {
    				"Full-Scene AntiAliasing (supersampling)",
    				"  Recommended: ON; greatly improves visual quality",
    				"  ON:  game is rendered at a higher resolution",
    				"  OFF: game is rendered at the native resolution",
                    "Will only be available if supported by your graphics",
                    "driver."};
        case USE_QUATERNIONS:
            return new String[] {
                    "Orientation render mode",
                    "  Recommended: Euler; standard Minecraft view",
                    "  orientation method",
                    "  Euler: Standard; at extreme head angles view may",
                    "  'skip' somewhat",
                    "  Quaternion: [EXPERIMENTAL] Smooth motion through",
                    "  extreme angles. However, currently disables head",
                    "  track sensitivity (sets to 1.0)"};
    	case SUPERSAMPLE_SCALEFACTOR:
    		return new String[] {
    				"Full-Screen AntiAliasing Render Scale",
    				"  What multiple of native resolution should be rendered?",
    				"  Recommended value: 2.0"};
        case FXAA:
            return new String[] {
                    "Fast approXimate AntiAliasing",
                    "  Recommended: OFF; use FSAA instead if performance",
                    "  allows. Will improve visual quality to some degree.",
                    "  However the image may be somewhat blurred. Very",
                    "  fast however; use when FSAA is too slow.",
                    "  ON:  game is rendered with FXAA filter.",
                    "  OFF: no FXAA filter applied."
                };
        case ASPECT_RATIO_CORRECTION:
            return new String[] {
                    "Sets the type of aspect ratio correction applied",
                    "during creation of the stereo views.",
                    "  AUTO: Recommended. Will in most cases set the",
                    "  correct aspect ratio for your HMD dependent upon",
                    "  the input resolution. This is the default.",
                    "  OTHERS: With some resolutions the view may exhibit",
                    "  distortion (most noticable on head roll). If so, try the",
                    "  other settings. It *may* correct the aspect ratio."
            };
        case DELAYED_RENDER:
            return new String[] {
                    "Whether to render a frame as soon as possible, or",
                    "delay the start of the rendering.",
                    "  Immediate: The sensors are polled and the frame",
                    "  is rendered as soon as possible.",
                    "  Delayed: [EXPERIMENTAL] Requires VSync ON. The thread",
                    "  sleeps then polls/starts rendering, timing ",
                    "  the end of the render to complete just before",
                    "  VSync. Lower latency if frame time is low."
            };
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

    private boolean getEnabledState(VRSettings.VrOptions var8)
    {
        if (var8 == VRSettings.VrOptions.DELAYED_RENDER)
        {
            if (!this.mc.gameSettings.enableVsync)
            {
                return false;
            }
        }
        return true;
    }
}

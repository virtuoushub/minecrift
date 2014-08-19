/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.gui;

import com.mtbs3d.minecrift.MCOculus;
import com.mtbs3d.minecrift.api.IBasePlugin;
import com.mtbs3d.minecrift.api.PluginManager;
import com.mtbs3d.minecrift.settings.VRSettings;

import de.fruitfly.ovr.structs.HmdDesc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;

import java.util.List;

public class GuiRenderOpticsSettings  extends BaseGuiSettings implements GuiEventEx
{
    protected boolean reinit = false;
    private PluginModeChangeButton pluginModeChangeButton;

    static VRSettings.VrOptions[] defaultDisplayOptions = new VRSettings.VrOptions[] {
    };

    static VRSettings.VrOptions[] oculusDK2DisplayOptions = new VRSettings.VrOptions[]{
            VRSettings.VrOptions.HMD_NAME_PLACEHOLDER,
            VRSettings.VrOptions.ENABLE_DIRECT,
            VRSettings.VrOptions.RENDER_SCALEFACTOR,
            VRSettings.VrOptions.MIRROR_DISPLAY,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.CHROM_AB_CORRECTION,
            VRSettings.VrOptions.TIMEWARP,
            VRSettings.VrOptions.VIGNETTE,
            VRSettings.VrOptions.LOW_PERSISTENCE,
            VRSettings.VrOptions.DYNAMIC_PREDICTION,
            VRSettings.VrOptions.OVERDRIVE_DISPLAY,
    };

    static VRSettings.VrOptions[] oculusDK1DisplayOptions = new VRSettings.VrOptions[] {
            VRSettings.VrOptions.HMD_NAME_PLACEHOLDER,
            VRSettings.VrOptions.ENABLE_DIRECT,
            VRSettings.VrOptions.RENDER_SCALEFACTOR,
            VRSettings.VrOptions.MIRROR_DISPLAY,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.CHROM_AB_CORRECTION,
            VRSettings.VrOptions.TIMEWARP,
            VRSettings.VrOptions.VIGNETTE,
    };

    GameSettings settings;

    public GuiRenderOpticsSettings(GuiScreen par1GuiScreen, VRSettings par2vrSettings, GameSettings gameSettings)
    {
    	super( par1GuiScreen, par2vrSettings);
        screenTitle = "Stereo Renderer Settings";
        settings = gameSettings;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        String productName = "";

        // this.screenTitle = var1.translateKey("options.videoTitle");
        this.buttonList.clear();
        this.buttonList.add(new GuiButtonEx(200, this.width / 2 - 100, this.height / 6 + 168, "Done"));
        this.buttonList.add(new GuiButtonEx(201, this.width / 2 - 100, this.height / 6 + 148, "Reset To Defaults"));

        pluginModeChangeButton = new PluginModeChangeButton(202, this.width / 2 - 78, this.height / 6 - 14, (List<IBasePlugin>)(List<?>) PluginManager.thePluginManager.stereoProviderPlugins, this.guivrSettings.stereoProviderPluginID);
        this.buttonList.add(pluginModeChangeButton);
        pluginModeChangeButton.enabled = false; // TODO: Allow changes once mono provider working

        VRSettings.VrOptions[] var10 = null;
        if( Minecraft.getMinecraft().stereoProvider instanceof MCOculus )
        {
            HmdDesc hmd = Minecraft.getMinecraft().hmdInfo.getHMDInfo();
            productName = hmd.ProductName;
            if (!hmd.IsReal)
                productName += " (Debug)";

            if (hmd.ProductName.contains("DK2"))      // Hacky. Improve.
                var10 = oculusDK2DisplayOptions;
            else
                var10 = oculusDK1DisplayOptions;
        }
        else
            var10 = defaultDisplayOptions;

        int var11 = var10.length;

        for (int var12 = 2; var12 < var11 + 2; ++var12)
        {
            VRSettings.VrOptions var8 = var10[var12 - 2];
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10;

            if (var8 == VRSettings.VrOptions.DUMMY)
                continue;

            if (var8.getEnumFloat())
            {
                float minValue = 0.0f;
                float maxValue = 1.0f;
                float increment = 0.001f;

                if (var8 == VRSettings.VrOptions.RENDER_SCALEFACTOR)
                {
                    minValue = 0.5f;
                    maxValue = 2.5f;
                    increment = 0.1f;
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
                if (var8 == VRSettings.VrOptions.HMD_NAME_PLACEHOLDER)
                {
                    GuiSmallButtonEx button = new GuiSmallButtonEx(999, width, height, var8, productName);
                    button.enabled = false;
                    this.buttonList.add(button);
                }
                else
                {
                    String keyBinding = this.guivrSettings.getKeyBinding(var8);
                    GuiSmallButtonEx button = new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, keyBinding);
                    button.enabled = getEnabledState(var8);
                    this.buttonList.add(button);
                }
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
                minecraft.vrSettings.useChromaticAbCorrection = true;
                minecraft.vrSettings.distortionFitPoint = 5;

                minecraft.vrSettings.useTimewarp = true;
                minecraft.vrSettings.useVignette = true;
                minecraft.vrSettings.useLowPersistence = true;
                minecraft.vrSettings.useDynamicPrediction = false; // Can cause judder with OVR 0.4.1...
                minecraft.vrSettings.renderScaleFactor = 1.1f;
                minecraft.vrSettings.useDirectRenderMode = false;
                minecraft.vrSettings.useDisplayMirroring = false;
                minecraft.vrSettings.useDisplayOverdrive = true;

                minecraft.reinitFramebuffers = true;
			    this.guivrSettings.saveOptions();
            }
            else if (par1GuiButton.id == 202) // Mode Change
            {
                Minecraft.getMinecraft().vrSettings.stereoProviderPluginID = pluginModeChangeButton.getSelectedID();
                Minecraft.getMinecraft().vrSettings.saveOptions();
                Minecraft.getMinecraft().stereoProvider = PluginManager.configureStereoProvider(Minecraft.getMinecraft().vrSettings.stereoProviderPluginID);
                this.reinit = true;
            }

            if (num == VRSettings.VrOptions.CHROM_AB_CORRECTION ||
                num == VRSettings.VrOptions.TIMEWARP ||
                num == VRSettings.VrOptions.VIGNETTE ||
                num == VRSettings.VrOptions.RENDER_SCALEFACTOR ||
                num == VRSettings.VrOptions.MIRROR_DISPLAY ||
                num == VRSettings.VrOptions.ENABLE_DIRECT ||
                num == VRSettings.VrOptions.LOW_PERSISTENCE ||
                num == VRSettings.VrOptions.DYNAMIC_PREDICTION ||
                num == VRSettings.VrOptions.OVERDRIVE_DISPLAY)
	        {
                minecraft.reinitFramebuffers = true;
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

    @Override
    public void event(int id, VRSettings.VrOptions enumm)
    {
        if (enumm == VRSettings.VrOptions.DISTORTION_FIT_POINT ||
            enumm == VRSettings.VrOptions.RENDER_SCALEFACTOR)
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
        case TIMEWARP:
            return new String[] {
                    "Reduces perceived head track latency by sampling sensor",
                    "position just before the view is presented to your eyes,",
                    "and rotating the rendered view subtly to match the new",
                    "sensor orientation.",
                    "  ON  - Timewarp applied. Some ghosting may be observed",
                    "        during fast changes of head position.",
                    "  OFF - No timewarp applied, higher latency head",
                    "        tracking."
            };
            case RENDER_SCALEFACTOR:
                return new String[] {
                        "Determines quality of rendered image. Higher values",
                        "increase quality but increase rendering load.",
                        "Defaults to 1.1X."
                };
            case ENABLE_DIRECT:
                return new String[] {
                        "Direct rendering to HMD. Not currently working so",
                        "OFF currently."
                };
            case MIRROR_DISPLAY:
                return new String[] {
                        "Mirrors image on HMD to separate desktop window.",
                        "Only relevant if Direct mode is ON. Not currently",
                        " working so set to NO currently."
                };
            case DYNAMIC_PREDICTION:
                return new String[]{
                        "If supported by your HMD, reduces perceived head",
                        " track latency by continually monitoring to head",
                        "to screen latency, and adjusting the frame-timing",
                        "appropriately.",
                        "  ON  - Dynamic prediction applied. A small",
                        "        coloured square will be seen in the top",
                        "        right of any mirrored display",
                        "  OFF - Not applied."
                };
            case OVERDRIVE_DISPLAY:
                return new String[] {
                        "If supported by your HMD, attempts to reduce",
                        "perceived image smearing during black-to-",
                        "bright transitions.",
                        "  ON  - Smearing may be reduced.",
                        "  OFF - Not applied."
                };
            case LOW_PERSISTENCE:
                return new String[] {
                        "If supported by your HMD, displays each frame on the",
                        "the HMDs OLED screen for a very short period of time.",
                        "This greatly reduces perceived blurring during head",
                        "motion.",
                        "  ON  - Low persistence reduces image blur on",
                        "        head movement.",
                        "  OFF - Not applied."
                };
            case VIGNETTE:
                return new String[] {
                        "If enabled, blurs the edges of the distortion",
                        "displayed for each eye, making the edges less",
                        "noticeable at the edges of your field of view.",
                        "  ON  - FOV edges blurred.",
                        "  OFF - No burring of distortion edges. The edge",
                        "        of distortion may be more noticeable",
                        "        within your field of view."
                };
    	// TODO: Add others
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
    		default:
    			return null;
    	}
    }

    private boolean getEnabledState(VRSettings.VrOptions var8)
    {
        String s = var8.getEnumString();

        if( ! (Minecraft.getMinecraft().stereoProvider instanceof MCOculus) )
        {
            return true;
        }

        if (var8 == VRSettings.VrOptions.ENABLE_DIRECT ||
            var8 == VRSettings.VrOptions.MIRROR_DISPLAY)
        {
            return false;
        }

        return true;
    }
}

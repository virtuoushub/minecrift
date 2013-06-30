/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.gui;

import java.util.List;

import com.mtbs3d.minecrift.MCHydra;
import com.mtbs3d.minecrift.VRRenderer;
import com.mtbs3d.minecrift.api.BasePlugin;
import com.mtbs3d.minecrift.api.IBasePlugin;

import com.mtbs3d.minecrift.api.PluginManager;
import net.minecraft.src.*;

public class GuiHeadPositionSettings extends BaseGuiSettings implements GuiEventEx
{
    /** An array of all of EnumOption's head position options. */

    static EnumOptions[] neckModelOptions = new EnumOptions[] {
            EnumOptions.EYE_PROTRUSION,
            EnumOptions.NECK_LENGTH,
    };

    static EnumOptions[] hydraOptions = new EnumOptions[] {
        EnumOptions.POS_TRACK_HYDRALOC,
        EnumOptions.POS_TRACK_HYDRA_DISTANCE_SCALE,
        EnumOptions.POS_TRACK_HYDRA_USE_CONTROLLER_ONE,
        EnumOptions.POS_TRACK_HYDRA_OFFSET_X,
        EnumOptions.HYDRA_USE_FILTER,
        EnumOptions.POS_TRACK_HYDRA_OFFSET_Y,
        EnumOptions.EYE_PROTRUSION,
        EnumOptions.POS_TRACK_HYDRA_OFFSET_Z,
        EnumOptions.NECK_LENGTH,   //EnumOptions.POS_TRACK_Y_AXIS_DISTANCE_SKEW,
        EnumOptions.POS_TRACK_HYDRA_OFFSET_SET_DEFAULT,
    };

    protected boolean reinit = false;
    protected boolean reinitOffsetDefaults = false;

	private PluginModeChangeButton pluginModeChangeutton;

    public GuiHeadPositionSettings(GuiScreen par1GuiScreen,
                            GameSettings par2GameSettings)
    {
    	super(par1GuiScreen, par2GameSettings );
        screenTitle = "Positional Tracking Configuration";
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        if (this.reinitOffsetDefaults)
        {
            this.reinitOffsetDefaults = false;
            setHydraLocOffsetDefaults();
        }

        StringTranslate stringTranslate = StringTranslate.getInstance();
        this.buttonList.clear();
        this.buttonList.add(new GuiButtonEx(200, this.width / 2 - 100, this.height / 6 + 168, stringTranslate.translateKey("gui.done")));

        pluginModeChangeutton = new PluginModeChangeButton(201, this.width / 2 - 78, this.height / 6 - 14, (List<IBasePlugin>)(List<?>) PluginManager.thePluginManager.positionPlugins, this.guiGameSettings.headPositionPluginID );
        this.buttonList.add(pluginModeChangeutton);

        GuiButtonEx resetPosButton = new GuiButtonEx(202, this.width / 2 - 100, this.height / 6 + 128, "Reset Origin");
        if( this.guiGameSettings.headPositionPluginID.equalsIgnoreCase(MCHydra.pluginID))
            this.buttonList.add(resetPosButton);

        GuiButtonEx recalibrate = new GuiButtonEx(203, this.width / 2 - 100, this.height / 6 + 148, "Recalibrate (look ahead now)");
        this.buttonList.add(recalibrate);
        EnumOptions[] var10 = null;

        if( this.guiGameSettings.headPositionPluginID.equalsIgnoreCase(MCHydra.pluginID))
            var10 = hydraOptions;
        else
            var10 = neckModelOptions;

        int var11 = var10.length;

        for (int var12 = 2; var12 < var11 + 2; ++var12)
        {
            EnumOptions var8 = var10[var12 - 2];
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10;

            if (isVisible(var8))
            {
                if (var8.getEnumFloat())
                {
                    float minValue = 0.0f;
                    float maxValue = 1.0f;
                    float increment = 0.01f;

                    if (var8 == EnumOptions.POS_TRACK_HYDRA_OFFSET_X || var8 == EnumOptions.POS_TRACK_HYDRA_OFFSET_Y)
                    {
                        minValue = -0.30f;
                        maxValue = 0.30f;
                        increment = 0.001f;
                    }
                    else if (var8 == EnumOptions.POS_TRACK_HYDRA_OFFSET_Z)
                    {
                        minValue = -0.30f;
                        maxValue = 0.30f;
                        increment = 0.001f;
                    }
                    else if (var8 == EnumOptions.POS_TRACK_HYDRA_DISTANCE_SCALE)
                    {
                        minValue = 0.8f;
                        maxValue = 1.2f;
                        increment = 0.001f;
                    }
                    else if (var8 == EnumOptions.POS_TRACK_Y_AXIS_DISTANCE_SKEW)
                    {
                        minValue = -45.0f;
                        maxValue = 45.0f;
                        increment = 0.1f;
                    }
                    if (var8 == EnumOptions.EYE_PROTRUSION)
                    {
                        minValue = 0.00f;
                        maxValue = 0.25f;
                        increment = 0.001f;
                    }
                    if (var8 == EnumOptions.NECK_LENGTH)
                    {
                        minValue = 0.00f;
                        maxValue = 0.25f;
                        increment = 0.001f;
                    }

                    GuiSliderEx slider = new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guiGameSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guiGameSettings.getOptionFloatValue(var8));
                    slider.setEventHandler(this);
                    slider.enabled = getEnabledState(var8);
                    this.buttonList.add(slider);
                }
                else
                {
                    String keyText = this.guiGameSettings.getKeyBinding(var8);
                    if (var8 == EnumOptions.POS_TRACK_HYDRA_OFFSET_SET_DEFAULT)
                    {
                        keyText = "Set Default Offsets";
                    }

                    GuiSmallButtonEx smallButton = new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, keyText);
                    smallButton.setEventHandler(this);
                    smallButton.enabled = getEnabledState(var8);
                    this.buttonList.add(smallButton);
                }
            }
        }
    }

    private boolean isVisible(EnumOptions var8)
    {
        if (var8 == EnumOptions.DUMMY)
            return false;

        //These don't really apply to Oculus head position (which is just neck model)
        if (this.guiGameSettings.headPositionPluginID.equalsIgnoreCase(MCHydra.pluginID))
        {
            if (this.guiGameSettings.posTrackHydraLoc != GameSettings.POS_TRACK_HYDRA_LOC_BACK_OF_HEAD && var8 == EnumOptions.NECK_LENGTH)
                return false;
        }

        return true;
    }

    private boolean getEnabledState(EnumOptions var8)
    {
        String s = var8.getEnumString();

        if (!this.guiGameSettings.headPositionPluginID.equalsIgnoreCase(MCHydra.pluginID))
        {
            return true;
        }

        if (var8 == EnumOptions.POS_TRACK_HYDRALOC ||
            var8 == EnumOptions.POS_TRACK_HYDRA_DISTANCE_SCALE ||
            var8 == EnumOptions.HYDRA_USE_FILTER)
            return true;

        if (this.guiGameSettings.posTrackHydraLoc == GameSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT &&
            var8 == EnumOptions.POS_TRACK_HYDRA_USE_CONTROLLER_ONE)
        {
            return false;
        }

        return true;
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
            else if (par1GuiButton.id == 201) // Mode Change
            {
            	this.mc.gameSettings.headPositionPluginID = pluginModeChangeutton.getSelectedID();
                this.mc.gameSettings.saveOptions();
            	this.mc.positionTracker = PluginManager.configurePosition(this.mc.gameSettings.headPositionPluginID);
            	this.reinit = true;
            }
            else if (par1GuiButton.id == 202) // Reset origin
            {
                this.guiGameSettings.posTrackResetPosition = true;
                if (vrRenderer != null)
                    vrRenderer.resetGuiYawOrientation();
            }
            else if (par1GuiButton.id == 203)
            {
                if (vrRenderer != null)
                    vrRenderer.startCalibration();
            }

            if (num == EnumOptions.HYDRA_USE_FILTER)
            {
                if (mc.positionTracker.getID() == MCHydra.pluginID)
                    mc.positionTracker.setPrediction(0.0f, this.guiGameSettings.hydraUseFilter);
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
    public void event(int id, EnumOptions enumm)
    {
        if (enumm == EnumOptions.POS_TRACK_HYDRA_OFFSET_SET_DEFAULT)
        {
            this.reinitOffsetDefaults = true;
            this.reinit = true;
        }

        if (enumm == EnumOptions.POS_TRACK_HYDRALOC)
        {
            this.reinit = true;
        }
    }

    private void setHydraLocOffsetDefaults()
    {
        switch (this.guiGameSettings.posTrackHydraLoc)
        {
            case GameSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT:
                this.guiGameSettings.posTrackHydraLROffsetX = 0.0f;
                this.guiGameSettings.posTrackHydraLROffsetY = 0.0f;
                this.guiGameSettings.posTrackHydraLROffsetZ = 0.0f;
                break;
            case GameSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT:
                this.guiGameSettings.posTrackHydraLOffsetX = -0.108f;
                this.guiGameSettings.posTrackHydraLOffsetY = 0.0f;
                this.guiGameSettings.posTrackHydraLOffsetZ = 0.0f;
                break;
            case GameSettings.POS_TRACK_HYDRA_LOC_HMD_TOP:
                this.guiGameSettings.posTrackHydraTOffsetX = 0.0f;
                this.guiGameSettings.posTrackHydraTOffsetY = 0.085f;
                this.guiGameSettings.posTrackHydraTOffsetZ = 0.0f;
                break;
            case GameSettings.POS_TRACK_HYDRA_LOC_HMD_RIGHT:
                this.guiGameSettings.posTrackHydraROffsetX = 0.108f;
                this.guiGameSettings.posTrackHydraROffsetY = 0.0f;
                this.guiGameSettings.posTrackHydraROffsetZ = 0.0f;
                break;
        }
    }

    @Override
    protected String[] getTooltipLines(String displayString, int buttonId)
    {
        EnumOptions e = EnumOptions.getEnumOptions(buttonId);
        if( e != null )
            switch(e)
            {
                case POS_TRACK_HYDRALOC:
                    return new String[] {
                            "Location(s) of the Hydra(s) used for pos track.",
                            "  L&R - One hydra is mounted to the left side of the ",
                            "        HMD, one to the right side. The Hydra center",
                            "        point is the average of the two reported locations.",
                            "  L   - One Hydra is mounted to the left side of the HMD.",
                            "  R   - One Hydra is mounted to the right side of the HMD.",
                            "  T   - One Hydra is mounted to the top of the HMD.",
                            "  B   - One hydra is mounted to the back of your head."
                    } ;
                case POS_TRACK_HYDRA_DISTANCE_SCALE:
                    return new String[] {
                            "Sets the distance scale factor.",
                            "  Allows adjustment of your perceived body movement",
                            " in-game by the selected factor. Adjust this if the",
                            " distance moved in game does not seem to match actual",
                            " body distance travelled."
                            } ;
                case POS_TRACK_HYDRA_USE_CONTROLLER_ONE:
                    return new String[] {
                            "Sets the controller used for positional tracking.",
                            "  If only one Hydra is used for positional tracking, sets",
                            "  which controller is used. Left / right are as set during",
                            "  the Hydra calibration process."
                    } ;
                case POS_TRACK_HYDRA_OFFSET_X:
                    return new String[] {
                            "Sets the left/right offset in mm of the Hydra center point",
                            "from the HMD center (eye) point. Adjust these offsets if ",
                            "rotational movement in-game does not quite match actual",
                            "bodily rotation.",
                            "  Negative values - The Hydra center is x mm to the left",
                            "                    of the HMD (eye) center point.",
                            "  Positive values - The Hydra center is x mm to the right",
                            "                    of the HMD (eye) center point."
                    };
                case POS_TRACK_HYDRA_OFFSET_Y:
                    return new String[] {
                            "Sets the above/below offset in mm of the Hydra center",
                            "point from the HMD center (eye) point. Adjust these",
                            "offsets if rotational movement in-game does not quite",
                            "match actual bodily rotation.",
                            "  Negative values - The Hydra center point is y mm below",
                            "                    the HMD (eye) center point.",
                            "  Positive values - The Hydra center point is y mm above",
                            "                    the HMD (eye) center."
                    };
                case POS_TRACK_HYDRA_OFFSET_Z:
                    return new String[] {
                            "Sets the towards head/away from head offset in mm of",
                            "the Hydra center point from the HMD (eye) center point.",
                            "Adjust these offsets if rotational movement in-game does",
                            "not quite match actual bodily rotation.",
                            "  Negative values - The Hydra center point is z mm behind",
                            "                    the HMD center (eye) point.",
                            "  Positive values - The Hydra center point is z mm in front",
                            "                    of the HMD center (eye) point."
                    };
                case HYDRA_USE_FILTER:
                    return new String[] {
                            "Use the Hydra positional filter.",
                            "  OFF - No filter is used; less latency but more positional",
                            "        'jitter' may be noticed, especially at a greater",
                            "        distance from the Hydra base unit.",
                            "  ON  - Filter used. Less positional 'jitter', more latency."
                    };
                case POS_TRACK_HYDRA_OFFSET_SET_DEFAULT:
                    return new String[] {
                            "Set offset defaults for the selected Hydra location."
                    };
                case EYE_PROTRUSION:
                    return new String[] {
                            "Distance from \"head-center\" to your eyes (in meters)",
                            "  Get it close for the best experience",
                            " (\"X\" distance below)     ____  ",
                            "                              /      \\ ",
                            "                              |    XXo ",
                            "                              |      _\\",
                            "                               \\   /",
                            "                                 | |"
                    };
                case NECK_LENGTH:
                    return new String[] {
                            "Distance from \"head-center\" to your shoulders",
                            "  Get it close for the best experience",
                            " (\"Y\" distance below)     ____  ",
                            "                              /      \\ ",
                            "                              |   Y  o ",
                            "                              |   Y  _\\",
                            "                               \\ Y /",
                            "                                 |Y|"
                    };
//                case POS_TRACK_Y_AXIS_DISTANCE_SKEW:
//                    return new String[] {
//                            "Explain this! Good luck!"
//                    };
                default:
                    return null;
            }
        else
            switch(buttonId)
            {
                case 201:
                    return new String[] {
                            "Changes the method used for positional tracking."
                    };
                case 202:
                    return new String[] {
                            "Resets the origin point of the positional tracking",
                            "to your current head position."
                    };
                case 203:
                    return new String[] {
                            "Starts calibration of the Oculus Rift headset",
                            "  Press this button then follow the on screen",
                            "  instructions."
                    };
                default:
                    return null;
            }
    }
}

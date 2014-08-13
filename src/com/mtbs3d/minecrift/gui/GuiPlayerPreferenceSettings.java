package com.mtbs3d.minecrift.gui;

import com.mtbs3d.minecrift.settings.VRSettings;
import de.fruitfly.ovr.UserProfileData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StringTranslate;

public class GuiPlayerPreferenceSettings extends BaseGuiSettings implements GuiEventEx
{
//    static VRSettings.VrOptions[] playerOptionsNoProfile = new VRSettings.VrOptions[] {
//            VRSettings.VrOptions.DUMMY,
//            VRSettings.VrOptions.DUMMY,
//            VRSettings.VrOptions.IPD,
//            VRSettings.VrOptions.EYE_HEIGHT,
//            VRSettings.VrOptions.EYE_PROTRUSION,
//            VRSettings.VrOptions.NECK_LENGTH,
//            VRSettings.VrOptions.RENDER_OWN_HEADWEAR,
//            VRSettings.VrOptions.RENDER_PLAYER_OFFSET,
//            VRSettings.VrOptions.RENDER_FULL_FIRST_PERSON_MODEL,
//    };

    static VRSettings.VrOptions[] playerOptionsWithProfile = new VRSettings.VrOptions[] {
            VRSettings.VrOptions.OCULUS_PROFILE_NAME,
            VRSettings.VrOptions.OCULUS_PROFILE_GENDER,
            VRSettings.VrOptions.IPD,
            VRSettings.VrOptions.EYE_HEIGHT,
            VRSettings.VrOptions.EYE_PROTRUSION,
            VRSettings.VrOptions.NECK_LENGTH,
            VRSettings.VrOptions.RENDER_OWN_HEADWEAR,
            VRSettings.VrOptions.RENDER_PLAYER_OFFSET,
            VRSettings.VrOptions.RENDER_FULL_FIRST_PERSON_MODEL_MODE,
            VRSettings.VrOptions.CALIBRATION_STRATEGY

    };

    public GuiPlayerPreferenceSettings(GuiScreen guiScreen, VRSettings guivrSettings) {
        super( guiScreen, guivrSettings );
        screenTitle = "Player Preferences";
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        UserProfileData profile = null;
        this.guivrSettings.useOculusProfile = true;

        if (Minecraft.getMinecraft().hmdInfo != null)
        {
            // Gets the current profile
            profile = Minecraft.getMinecraft().hmdInfo.getProfileData();
            if (profile != null)
            {
                this.guivrSettings.setOculusProfileIpd(profile._ipd);
                this.guivrSettings.setOculusProfilePlayerEyeHeight(profile._eyeHeight);
                this.guivrSettings.oculusProfileName = profile._name;
                this.guivrSettings.oculusProfileGender = profile.getGenderString();
            }
        }
        else
        {
            profile = new UserProfileData();
        }

        this.buttonList.clear();

        // Profile on/off
        GuiSmallButtonEx profileOnOff = new GuiSmallButtonEx(VRSettings.VrOptions.OCULUS_PROFILE.returnEnumOrdinal(), this.width / 2 - 78, this.height / 6 - 14, VRSettings.VrOptions.OCULUS_PROFILE, this.guivrSettings.getKeyBinding(VRSettings.VrOptions.OCULUS_PROFILE));
        profileOnOff.setEventHandler(this);
        profileOnOff.enabled = false;
        this.buttonList.add(profileOnOff);

        this.buttonList.add(new GuiButtonEx(201, this.width / 2 - 100, this.height / 6 + 148, "Reset To Defaults"));
        this.buttonList.add(new GuiButtonEx(200, this.width / 2 - 100, this.height / 6 + 168, "Done"));
        VRSettings.VrOptions[] buttons = playerOptionsWithProfile;   // Only use profile supported

        for (int var12 = 2; var12 < buttons.length + 2; ++var12)
        {
            VRSettings.VrOptions var8 = buttons[var12 - 2];
            if (var8 == VRSettings.VrOptions.DUMMY)
                continue;

            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10;

            if (var8.getEnumFloat())
            {
                float minValue = 0.0f;
                float maxValue = 1.0f;
                float increment = 0.01f;

                // TODO: Set min, max, step directly in VrOptions enum
                if (var8 == VRSettings.VrOptions.EYE_HEIGHT)
                {
                    minValue = 1.62f;
                    maxValue = 1.85f;
                    increment = 0.01f;
                }

                if (var8 == VRSettings.VrOptions.RENDER_PLAYER_OFFSET)
                {
                    minValue = 0.0f;
                    maxValue = 0.25f;
                    increment = 0.01f;
                }

                if (var8 == VRSettings.VrOptions.IPD)
                {
                    minValue = 0.055f;
                    maxValue = 0.075f;
                    increment = 0.0001f;
                }

                if (var8 == VRSettings.VrOptions.EYE_PROTRUSION)
                {
                    minValue = 0.00f;
                    maxValue = 0.25f;
                    increment = 0.001f;
                }

                if (var8 == VRSettings.VrOptions.NECK_LENGTH)
                {
                    minValue = 0.00f;
                    maxValue = 0.25f;
                    increment = 0.001f;
                }

                GuiSliderEx slider = new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guivrSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guivrSettings.getOptionFloatValue(var8));
                slider.enabled = getEnabledState(var8);
                slider.setEventHandler(this);
                this.buttonList.add(slider);
            }
            else
            {
                GuiSmallButtonEx button = new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, this.guivrSettings.getKeyBinding(var8));
                button.enabled = getEnabledState(var8);
                button.setEventHandler(this);
                this.buttonList.add(button);
            }
        }
    }

    private boolean getEnabledState(VRSettings.VrOptions e)
    {
        if (this.guivrSettings.useOculusProfile)
        {
            if (e == VRSettings.VrOptions.IPD ||
                e == VRSettings.VrOptions.EYE_HEIGHT ||
                e == VRSettings.VrOptions.OCULUS_PROFILE_NAME ||
                e == VRSettings.VrOptions.OCULUS_PROFILE_GENDER)
            {
                return false;
            }
        }

        return true;
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
                VRSettings.VrOptions num = VRSettings.VrOptions.getEnumOptions(par1GuiButton.id);
                this.guivrSettings.setOptionValue(((GuiSmallButtonEx)par1GuiButton).returnVrEnumOptions(), 1);
                par1GuiButton.displayString = this.guivrSettings.getKeyBinding(VRSettings.VrOptions.getEnumOptions(par1GuiButton.id));
            }
            else if (par1GuiButton.id == 200)
            {
                Minecraft.getMinecraft().vrSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }
            else if (par1GuiButton.id == 201)
            {
                // Set defaults
                this.guivrSettings.setMinecraftIpd(0.0635F);
                //mc.hmdInfo.setIPD(this.guivrSettings.getIPD());    // TODO: IPD
                this.guivrSettings.setMinecraftPlayerEyeHeight(1.74f);
                this.guivrSettings.renderHeadWear = false;
                this.guivrSettings.renderFullFirstPersonModelMode = VRSettings.RENDER_FIRST_PERSON_FULL;
                this.guivrSettings.renderPlayerOffset = 0.25f;
                this.guivrSettings.eyeProtrusion = 0.185f;
                this.guivrSettings.neckBaseToEyeHeight = 0.225f;
                this.guivrSettings.calibrationStrategy = 1;

                this.guivrSettings.saveOptions();
                Minecraft.getMinecraft().reinitFramebuffers = true;
                this.reinit = true;
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
                case IPD:
                    return new String[] {
                            "Interpupillary Distance",
                            "  How far apart your eye pupils are, in millimeters"} ;
                case RENDER_OWN_HEADWEAR:
                    return new String[] {
                            "Whether to render the player's own headwear or not",
                            "  ON:  Headwear is rendered. May obscure your view!",
                            "  OFF: Not rendered."
                    };
                case RENDER_FULL_FIRST_PERSON_MODEL_MODE:
                    return new String[] {
                            "Whether to render the full first-person model or",
                            "other variants (Ctrl-H).",
                            "  Full: A full first-person model is rendered.",
                            "        However some animations may not yet be",
                            "        supported (e.g. holding a map).",
                            "  Hand: Only the held item is rendered. You will",
                            "        have no torso!",
                            "  None: No first person model is rendered."
                    };
                case RENDER_PLAYER_OFFSET:
                    return new String[] {
                            "Distance your body is rendered back from the normal",
                            "position.",
                            "  The current Steve player model can obscure your",
                            "  peripheral view when rendered at the normal",
                            "  Minecraft position. This setting moves the render",
                            "  position of body backwards by the desired distance,",
                            "  in cm."
                    };
                case EYE_HEIGHT:
                    return new String[] {
                            "Your real-world Eye Height when standing (in meters)",
                            "  Setting this value isn't required, but you should",
                            "  strive to get it as close as possible for an accurate",
                            "  experience"
                    };
                case EYE_PROTRUSION:
                    return new String[] {
                            "Distance from \"head-center\" to your eyes (in meters)",
                            "  (with pos track affects origin position only)",
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
                            "  (with pos track affects origin position only)",
                            " (\"Y\" distance below)     ____  ",
                            "                              /      \\ ",
                            "                              |   Y  o ",
                            "                              |   Y  _\\",
                            "                               \\ Y /",
                            "                                 |Y|"
                    };
            case CALIBRATION_STRATEGY:
                return new String[] {
                        "Sets whether device calibration is performed when",
                        "Minecraft is started.",
                        "  At Startup: Calibration routines for all",
                        "     utilised devices are run at startup.",
                        "  Skip: No calibration will be performed. The user",
                        "     will have to manually trigger calibration",
                        "     at some point for correct device operation."
                };
                default:
                    return null;
            }
        else
            switch(buttonId)
            {
//                case 201:
//                    return new String[] {
//                            "Open this configuration screen to adjust the Head",
//                            "  Tracker orientation (direction) settings. ",
//                            "  Ex: Head Tracking Selection (Hydra/Oculus), Prediction"
//                    };
                default:
                    return null;
            }
    }

    @Override
    public void event(int id, VRSettings.VrOptions enumm)
    {
        if (enumm == VRSettings.VrOptions.IPD)
        {
           // mc.hmdInfo.setIPD(this.mc.vrSettings.getIPD());   // TODO: IPD
            Minecraft.getMinecraft().reinitFramebuffers = true;
        }
        else if (enumm == VRSettings.VrOptions.OCULUS_PROFILE)
        {
            this.reinit = true;
            Minecraft.getMinecraft().reinitFramebuffers = true;
        }
    }
}

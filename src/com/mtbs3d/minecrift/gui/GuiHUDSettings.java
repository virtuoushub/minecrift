package com.mtbs3d.minecrift.gui;

import com.mtbs3d.minecrift.settings.VRSettings;

import net.minecraft.src.*;

public class GuiHUDSettings extends BaseGuiSettings
{
    static EnumOptions[] hudOptions = new EnumOptions[] {
            EnumOptions.HUD_HIDE,
            EnumOptions.HUD_LOCK_TO,
            EnumOptions.HUD_SCALE,
            EnumOptions.HUD_DISTANCE,
            EnumOptions.HUD_PITCH,
            EnumOptions.HUD_YAW,
            EnumOptions.HUD_OPACITY,
            EnumOptions.HUD_OCCLUSION,
            EnumOptions.CROSSHAIR_SCALE,
            EnumOptions.CROSSHAIR_ALWAYS_SHOW,
            EnumOptions.CROSSHAIR_ROLL,
            EnumOptions.BLOCK_OUTLINE_ALWAYS_SHOW,
    };

    public GuiHUDSettings(GuiScreen guiScreen, VRSettings guivrSettings) {
        super( guiScreen, guivrSettings );
        screenTitle = "HUD / Overlay Settings";
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        StringTranslate stringTranslate = StringTranslate.getInstance();
        this.buttonList.clear();
//        this.buttonList.add(new GuiSmallButtonEx(EnumOptions.USE_VR.returnEnumOrdinal(), this.width / 2 - 78, this.height / 6 - 14, EnumOptions.USE_VR, this.guivrSettings.getKeyBinding(EnumOptions.USE_VR)));
        this.buttonList.add(new GuiButtonEx(201, this.width / 2 - 100, this.height / 6 + 148, "Reset To Defaults"));
        this.buttonList.add(new GuiButtonEx(200, this.width / 2 - 100, this.height / 6 + 168, stringTranslate.translateKey("gui.done")));
        EnumOptions[] buttons = hudOptions;

        for (int var12 = 2; var12 < buttons.length + 2; ++var12)
        {
            EnumOptions var8 = buttons[var12 - 2];
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10;

            if (var8 == EnumOptions.DUMMY)
                continue;

            if (var8.getEnumFloat())
            {
                float minValue = 0.0f;
                float maxValue = 1.0f;
                float increment = 0.01f;

                if (var8 == EnumOptions.HUD_SCALE)
                {
                    minValue = 0.35f;
                    maxValue = 2.5f;
                    increment = 0.01f;
                }
                else if (var8 == EnumOptions.HUD_DISTANCE)
                {
                    minValue = 0.25f;
                    maxValue = 5.0f;
                    increment = 0.01f;
                }
                else if (var8 == EnumOptions.CROSSHAIR_SCALE)
                {
                    minValue = 0.25f;
                    maxValue = 2.5f;
                    increment = 0.01f;
                }
                else if (var8 == EnumOptions.HUD_PITCH)
                {
                    minValue = -45f;
                    maxValue = 45f;
                    increment = 1f;
                }
                else if (var8 == EnumOptions.HUD_YAW)
                {
                    minValue = -100f;
                    maxValue = 100f;
                    increment = 1f;
                }
                else if (var8 == EnumOptions.HUD_OPACITY)
                {
                    minValue = 0.15f;
                    maxValue = 1.0f;
                    increment = 0.05f;
                }

                this.buttonList.add(new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guivrSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guivrSettings.getOptionFloatValue(var8)));
            }
            else
            {
                this.buttonList.add(new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, this.guivrSettings.getKeyBinding(var8)));
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
                this.guivrSettings.setOptionValue(((GuiSmallButtonEx)par1GuiButton).returnEnumOptions(), 1);
                par1GuiButton.displayString = this.guivrSettings.getKeyBinding(EnumOptions.getEnumOptions(par1GuiButton.id));
            }
            else if (par1GuiButton.id == 200)
            {
                this.mc.vrSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }
            else if (par1GuiButton.id == 201)
            {
                this.guivrSettings.hudDistance = 1.0f;
                this.guivrSettings.hudScale = 1.0f;
                this.guivrSettings.hudPitchOffset = 0f;
                this.guivrSettings.hudYawOffset = 0f;
                this.mc.gameSettings.hideGUI = false;
                this.guivrSettings.hudOpacity = 1.0f;
                this.guivrSettings.hudLockToHead = false;
                this.guivrSettings.hudOcclusion = false;
                this.guivrSettings.crosshairScale = 1.0f;
                this.guivrSettings.alwaysRenderBlockOutline = false;
                this.guivrSettings.alwaysRenderInGameCrosshair = false;
                this.guivrSettings.crosshairRollsWithHead = true;
                this.mc.vrSettings.saveOptions();
                this.reinit = true;
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
                case HUD_OPACITY:
                    return new String[] {
                            "How transapernet to draw the in-game HUD and UI",
                    };
                case HUD_HIDE:
                    return new String[] {
                            "Whether to hide the in-game HUD or not.",
                            "  YES: The HUD is hidden.",
                            "  NO:  The HUD is visible."
                    };
                case HUD_SCALE:
                return new String[] {
                        "Relative size HUD takes up in field-of-view",
                        "  The units are just relative, not in degrees",
                        "  or a fraction of FOV or anything"
                };
                case HUD_PITCH:
                    return new String[] {
                            "The vertical offset of the HUD, in degrees.",
                            "  Negative values are down, positive up."
                    };
                case HUD_YAW:
                    return new String[] {
                            "The horizontal offset of the HUD, in degrees.",
                            "  Negative values are to the left, positive to",
                            "  the right."
                    };
                case HUD_DISTANCE:
                    return new String[] {
                            "Distance the floating HUD is drawn in front of your body",
                            "  The relative size of the HUD is unchanged by this",
                            "  Distance is in meters (though isn't obstructed by blocks)"
                    };
                case HUD_OCCLUSION:
                    return new String[] {
                            "Specifies whether the HUD is occluded by closer objects.",
                            "  ON:  The HUD will be hidden by closer objects. May",
                            "       be hidden completely in confined environments!",
                            "  OFF: The HUD is always visible. Stereo depth issues",
                            "       may be noticable."
                    };
                case HUD_LOCK_TO:
                    return new String[] {
                            "Specifies to which orientation the HUD is locked to.",
                            "  BODY:  The HUD when visible will appear locked",
                            "         relative to your body orientation. This allows",
							"         free look around the HUD.",
                            "  HEAD:  The HUD appears locked to your head",
                            "         orientation. The HUD may have to be scaled to",
							"         fit within your visible field of view. Edges",
							"         may be blurred!"
                    };
                case CROSSHAIR_ALWAYS_SHOW:
                    return new String[] {
                            "Set the in-game crosshair display mode",
                            "  Always: The crosshair is always shown even if the",
                            "          HUD is disabled",
                            "  With HUD: The crosshair is only shown when the HUD",
                            "            is enabled"
                    };
                case CROSSHAIR_SCALE:
                    return new String[] {
                            "Sets the size of the in-game crosshair"
                    };
                case BLOCK_OUTLINE_ALWAYS_SHOW:
                    return new String[] {
                            "Sets the in-game block outline display mode.",
                            "  Always: The block outline is always shown even if",
                            "          the HUD is disabled",
                            "  With HUD: The block outline is only shown when the",
                            "           HUD is enabled"
                    };
                case CROSSHAIR_ROLL:
                    return new String[] {
                            "Sets the crosshair roll behaviour.",
                            "  With Head: The crosshair rolls with your head.",
                            "  With HUD:  The crosshair appears to roll, keeping",
                            "             the same orientation as the HUD."
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
}

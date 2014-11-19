package com.mtbs3d.minecrift.gui;

import com.mtbs3d.minecrift.settings.VRSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.src.*;
import net.minecraft.util.StringTranslate;

public class GuiHUDSettings extends BaseGuiSettings
{
    static VRSettings.VrOptions[] hudOptions = new VRSettings.VrOptions[] {
//            VRSettings.VrOptions.HUD_HIDE,
            VRSettings.VrOptions.HUD_LOCK_TO,
            VRSettings.VrOptions.HUD_OCCLUSION,
            VRSettings.VrOptions.HUD_SCALE,
            VRSettings.VrOptions.HUD_DISTANCE,
            VRSettings.VrOptions.HUD_PITCH,
            VRSettings.VrOptions.HUD_YAW,
            VRSettings.VrOptions.HUD_OPACITY,
            VRSettings.VrOptions.RENDER_MENU_BACKGROUND,
            VRSettings.VrOptions.OTHER_HUD_SETTINGS,
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
        this.buttonList.clear();
        this.buttonList.add(new GuiSmallButtonEx(VRSettings.VrOptions.HUD_HIDE.returnEnumOrdinal(), this.width / 2 - 78, this.height / 6 - 14, VRSettings.VrOptions.HUD_HIDE, this.guivrSettings.getKeyBinding(VRSettings.VrOptions.HUD_HIDE)));
        this.buttonList.add(new GuiButtonEx(201, this.width / 2 - 100, this.height / 6 + 148, "Reset To Defaults"));
        this.buttonList.add(new GuiButtonEx(200, this.width / 2 - 100, this.height / 6 + 168, "Done"));
        VRSettings.VrOptions[] buttons = hudOptions;

        for (int var12 = 2; var12 < buttons.length + 2; ++var12)
        {
            VRSettings.VrOptions var8 = buttons[var12 - 2];
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10;

            if (var8 == VRSettings.VrOptions.DUMMY)
                continue;

            if (var8.getEnumFloat())
            {
                float minValue = 0.0f;
                float maxValue = 1.0f;
                float increment = 0.01f;

                if (var8 == VRSettings.VrOptions.HUD_SCALE)
                {
                    minValue = 0.35f;
                    maxValue = 2.5f;
                    increment = 0.01f;
                }
                else if (var8 == VRSettings.VrOptions.HUD_DISTANCE)
                {
                    minValue = 0.25f;
                    maxValue = 5.0f;
                    increment = 0.01f;
                }
                else if (var8 == VRSettings.VrOptions.HUD_PITCH)
                {
                    minValue = -45f;
                    maxValue = 45f;
                    increment = 1f;
                }
                else if (var8 == VRSettings.VrOptions.HUD_YAW)
                {
                    minValue = -100f;
                    maxValue = 100f;
                    increment = 1f;
                }
                else if (var8 == VRSettings.VrOptions.HUD_OPACITY)
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
            if (par1GuiButton.id == VRSettings.VrOptions.OTHER_HUD_SETTINGS.returnEnumOrdinal())
            {
                Minecraft.getMinecraft().vrSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiOtherHUDSettings(this, this.guivrSettings));
            }
            else if (par1GuiButton.id < 200 && par1GuiButton instanceof GuiSmallButtonEx)
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
                this.guivrSettings.hudDistance = 1.25f;
                this.guivrSettings.hudScale = 1.25f;
                this.guivrSettings.hudPitchOffset = 0f;
                this.guivrSettings.hudYawOffset = 0f;
                this.mc.gameSettings.hideGUI = false;
                this.guivrSettings.hudOpacity = 1.0f;
                this.guivrSettings.menuBackground = false;
                this.guivrSettings.hudLockToHead = false;
                this.guivrSettings.hudOcclusion = false;

                Minecraft.getMinecraft().vrSettings.saveOptions();
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
                case HUD_OPACITY:
                    return new String[] {
                            "How transparent to draw the in-game HUD and UI",
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
                case RENDER_MENU_BACKGROUND:
                    return new String[] {
                            "Specifies whether the in game GUI menus have a ",
                            "semi-transparent background or not.",
                            "  ON:  Semi-transparent background on in-game menus.",
                            "  OFF: No background on in-game menus."
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
                case OTHER_HUD_SETTINGS:
                    return new String[] {
                            "Configure Crosshair and overlay settings."
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

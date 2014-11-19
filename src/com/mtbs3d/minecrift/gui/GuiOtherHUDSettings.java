package com.mtbs3d.minecrift.gui;

import com.mtbs3d.minecrift.settings.VRSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiOtherHUDSettings extends BaseGuiSettings
{
    static VRSettings.VrOptions[] hudOptions = new VRSettings.VrOptions[] {
            VRSettings.VrOptions.CROSSHAIR_SCALE,
            VRSettings.VrOptions.RENDER_CROSSHAIR_MODE,
            VRSettings.VrOptions.CROSSHAIR_ROLL,
            VRSettings.VrOptions.RENDER_BLOCK_OUTLINE_MODE,
            VRSettings.VrOptions.MENU_CROSSHAIR_SCALE,
            VRSettings.VrOptions.CROSSHAIR_OCCLUSION,
            VRSettings.VrOptions.MAX_CROSSHAIR_DISTANCE_AT_BLOCKREACH,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.CHAT_OFFSET_X,
            VRSettings.VrOptions.CHAT_OFFSET_Y,
    };

    public GuiOtherHUDSettings(GuiScreen guiScreen, VRSettings guivrSettings) {
        super( guiScreen, guivrSettings );
        screenTitle = "HUD Overlay / Crosshair Settings";
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.buttonList.clear();
        this.buttonList.add(new GuiButtonEx(ID_GENERIC_DEFAULTS, this.width / 2 - 100, this.height / 6 + 148, "Reset To Defaults"));
        this.buttonList.add(new GuiButtonEx(ID_GENERIC_DONE, this.width / 2 - 100, this.height / 6 + 168, "Done"));
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

                if (var8 == VRSettings.VrOptions.CROSSHAIR_SCALE)
                {
                    minValue = 0.25f;
                    maxValue = 2.5f;
                    increment = 0.01f;
                }
                else if (var8 == VRSettings.VrOptions.MENU_CROSSHAIR_SCALE)
                {
                    minValue = 0.25f;
                    maxValue = 2.5f;
                    increment = 0.01f;
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
            if (par1GuiButton.id == ID_GENERIC_DONE)
            {
                Minecraft.getMinecraft().vrSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }
            else if (par1GuiButton.id == ID_GENERIC_DEFAULTS)
            {
                this.guivrSettings.crosshairScale = 1.0f;
                this.guivrSettings.renderBlockOutlineMode = VRSettings.RENDER_BLOCK_OUTLINE_MODE_HUD;
                this.guivrSettings.renderInGameCrosshairMode = VRSettings.RENDER_CROSSHAIR_MODE_HUD;
                this.guivrSettings.crosshairRollsWithHead = true;
                this.guivrSettings.menuCrosshairScale = 1f;
                this.guivrSettings.useCrosshairOcclusion = false;
                this.guivrSettings.maxCrosshairDistanceAtBlockReach = false;

                Minecraft.getMinecraft().vrSettings.saveOptions();
                this.reinit = true;
            }
            else if (par1GuiButton instanceof GuiSmallButtonEx)
            {
                VRSettings.VrOptions num = VRSettings.VrOptions.getEnumOptions(par1GuiButton.id);
                this.guivrSettings.setOptionValue(((GuiSmallButtonEx)par1GuiButton).returnVrEnumOptions(), 1);
                par1GuiButton.displayString = this.guivrSettings.getKeyBinding(VRSettings.VrOptions.getEnumOptions(par1GuiButton.id));
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
            case RENDER_CROSSHAIR_MODE:
                return new String[] {
                        "Set the in-game crosshair display mode",
                        "  Always:   The crosshair is always shown even if the",
                        "            HUD is disabled",
                        "  With HUD: The crosshair is only shown when the HUD",
                        "            is enabled",
                        "  Never:    The crosshair is never shown"
                };
            case CROSSHAIR_SCALE:
                return new String[] {
                        "Sets the size of the in-game crosshair"
                };
            case MENU_CROSSHAIR_SCALE:
                return new String[] {
                        "Sets the size of the menu crosshair"
                };
            case RENDER_BLOCK_OUTLINE_MODE:
                return new String[] {
                        "Sets the in-game block outline display mode.",
                        "  Always:   The block outline is always shown even if",
                        "            the HUD is disabled",
                        "  With HUD: The block outline is only shown when the",
                        "            HUD is enabled",
                        "  Never:    The block outline is never shown"
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

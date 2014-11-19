package com.mtbs3d.minecrift.gui;

import com.mtbs3d.minecrift.settings.VRSettings;

import de.fruitfly.ovr.enums.EyeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiOtherRenderOpticsSettings extends BaseGuiSettings implements GuiEventEx
{
    static VRSettings.VrOptions[] oculusOptionsUseSingleIpd = new VRSettings.VrOptions[]
    {
            VRSettings.VrOptions.USE_PROFILE_IPD,
            VRSettings.VrOptions.CONFIG_IPD_MODE,
            VRSettings.VrOptions.TOTAL_IPD,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.MAX_FOV,
            VRSettings.VrOptions.FOV_CHANGE,
    };

    static VRSettings.VrOptions[] oculusOptionsUseTwinIpd = new VRSettings.VrOptions[]
    {
            VRSettings.VrOptions.USE_PROFILE_IPD,
            VRSettings.VrOptions.CONFIG_IPD_MODE,
            VRSettings.VrOptions.LEFT_HALF_IPD,
            VRSettings.VrOptions.RIGHT_HALF_IPD,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.MAX_FOV,
            VRSettings.VrOptions.FOV_CHANGE,
    };

    public GuiOtherRenderOpticsSettings(GuiScreen guiScreen, VRSettings guivrSettings) {
        super( guiScreen, guivrSettings );
        screenTitle = "IPD / FOV Settings";
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.buttonList.clear();
        this.buttonList.add(new GuiButtonEx(ID_GENERIC_DEFAULTS, this.width / 2 - 100, this.height / 6 + 148, "Reset To Defaults"));
        this.buttonList.add(new GuiButtonEx(ID_GENERIC_DONE, this.width / 2 - 100, this.height / 6 + 168, "Done"));
        VRSettings.VrOptions[] buttons = null;
        if (Minecraft.getMinecraft().vrSettings.useHalfIpds == false)
            buttons = oculusOptionsUseSingleIpd;
        else
            buttons = oculusOptionsUseTwinIpd;

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

                if (var8 == VRSettings.VrOptions.IPD_SCALE)
                {
                    minValue = -5f;
                    maxValue = 5f;
                    increment = 0.01f;
                }
                else if (var8 == VRSettings.VrOptions.EYE_RELIEF)
                {
                    minValue = -0.25f;
                    maxValue =  0.25f;
                    increment = 0.005f;
                }
                else if (var8 == VRSettings.VrOptions.FOV_CHANGE)
                {
                    minValue  = -10f;
                    maxValue  = 10f;
                    increment = 0.1f;
                }
                else if (var8 == VRSettings.VrOptions.TOTAL_IPD)
                {
                    minValue = 0.055f;
                    maxValue = 0.075f;
                    increment = 0.0001f;
                }
                else if (var8 == VRSettings.VrOptions.LEFT_HALF_IPD)
                {
                    minValue = -0.0375f;
                    maxValue = -0.0225f;
                    increment = 0.0001f;
                }
                else if (var8 == VRSettings.VrOptions.RIGHT_HALF_IPD)
                {
                    minValue = 0.0225f;
                    maxValue = 0.0375f;
                    increment = 0.0001f;
                }

                GuiSliderEx slider = new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guivrSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guivrSettings.getOptionFloatValue(var8));
                slider.setEventHandler(this);
                slider.enabled = getEnabledState(var8);
                this.buttonList.add(slider);
            }
            else
            {
                GuiSmallButtonEx smallButton = new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, this.guivrSettings.getKeyBinding(var8));
                smallButton.setEventHandler(this);
                smallButton.enabled = getEnabledState(var8);
                this.buttonList.add(smallButton);
            }
        }
    }

    private boolean getEnabledState(VRSettings.VrOptions var8)
    {
        String s = var8.getEnumString();

        if (this.guivrSettings.useOculusProfileIpd)
        {
            if (var8 == VRSettings.VrOptions.TOTAL_IPD ||
                var8 == VRSettings.VrOptions.LEFT_HALF_IPD ||
                var8 == VRSettings.VrOptions.RIGHT_HALF_IPD)
            {
                return false;
            }
        }

        return true;
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
                this.guivrSettings.useOculusProfileIpd = true;
                this.guivrSettings.setMinecraftIpd(this.guivrSettings.getOculusProfileHalfIPD(EyeType.ovrEye_Left), this.guivrSettings.getOculusProfileHalfIPD(EyeType.ovrEye_Right));
                this.guivrSettings.fovChange = 0f;
                this.guivrSettings.useMaxFov = false;

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
    public void event(int id, VRSettings.VrOptions enumm)
    {
        if (enumm == VRSettings.VrOptions.FOV_CHANGE ||
            enumm == VRSettings.VrOptions.MAX_FOV)
        {
            this.mc.reinitFramebuffers = true;
            this.reinit = true;
        }

        if (enumm == VRSettings.VrOptions.CONFIG_IPD_MODE ||
            enumm == VRSettings.VrOptions.USE_PROFILE_IPD)
        {
            this.reinit = true;
        }
    }

    @Override
    protected String[] getTooltipLines(String displayString, int buttonId)
    {
        VRSettings.VrOptions e = VRSettings.VrOptions.getEnumOptions(buttonId);
        if( e != null )
            switch(e)
            {
                // TODO: Tooltips
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

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
import com.mtbs3d.minecrift.api.ICenterEyePositionProvider;
import net.minecraft.src.*;

public class GuiMoveAimSettings extends BaseGuiSettings
{
    /** An array of all of EnumOption's movement options. */
    static EnumOptions[] moveAimOptions = new EnumOptions[] {
        EnumOptions.DECOUPLEAIMLOOK,
        EnumOptions.DECOUPLEMOVELOOK,
        EnumOptions.MOVEMENT_MULTIPLIER,
        EnumOptions.JOYSTICK_SENSITIVITY,
        EnumOptions.PITCH_AFFECTS_CAMERA,
        EnumOptions.KEYHOLE_WIDTH,
    };
	private PluginModeChangeButton pluginModeChangeutton;

    public GuiMoveAimSettings(GuiScreen par1GuiScreen,
                            GameSettings par2GameSettings)
    {
    	super( par1GuiScreen, par2GameSettings );
        screenTitle = "Move/Look/Aim Controls Configuration";
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        StringTranslate stringTranslate = StringTranslate.getInstance();
        this.buttonList.clear();
        this.buttonList.add(new GuiButtonEx(200, this.width / 2 - 100, this.height / 6 + 168, stringTranslate.translateKey("gui.done")));
        
        pluginModeChangeutton = new PluginModeChangeButton(201, this.width / 2 - 78, this.height / 6 - 14, (List<IBasePlugin>)(List<?>)BasePlugin.controllerPlugins, this.guiGameSettings.controllerPluginID );
        this.buttonList.add(pluginModeChangeutton);

        EnumOptions[] var10 = moveAimOptions;
        int var11 = var10.length;

        for (int var12 = 2; var12 < var11 + 2; ++var12)
        {
            EnumOptions var8 = var10[var12 - 2];
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10;

            if (var8.getEnumFloat())
            {
                float minValue = 0.0f;
                float maxValue = 1.0f;
                float increment = 0.01f;

                if (var8 == EnumOptions.MOVEMENT_MULTIPLIER)
                {
                    minValue = 0.15f;
                    maxValue = 1.0f;
                    increment = 0.01f;
                }
                else if( var8 == EnumOptions.JOYSTICK_SENSITIVITY )
                {
                	minValue = 0.5f;
                	maxValue = 10.0f;
                	increment= 0.1f;
                }
                else if( var8 == EnumOptions.KEYHOLE_WIDTH)
                {
                	minValue = 0.0f;
                	maxValue = 90f;
                	increment= 1.0f;
                }

                GuiSliderEx slider = new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guiGameSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guiGameSettings.getOptionFloatValue(var8));
                this.buttonList.add(slider);
            }
            else
            {
                String keyText = this.guiGameSettings.getKeyBinding(var8);
                GuiSmallButtonEx smallButton = new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, keyText);
                this.buttonList.add(smallButton);
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
            }
            else if (par1GuiButton.id == 200)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }
            else if (par1GuiButton.id == 201) // Mode Change
            {
            	this.mc.gameSettings.controllerPluginID = pluginModeChangeutton.getSelectedID();
                this.mc.gameSettings.saveOptions();
            	this.mc.lookaimController = BasePlugin.configureController(this.mc.gameSettings.controllerPluginID);
            }
        }
    }
}

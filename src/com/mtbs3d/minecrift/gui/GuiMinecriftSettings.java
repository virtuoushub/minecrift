/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.gui;

import de.fruitfly.ovr.EyeRenderParams;
import net.minecraft.src.EnumOptions;
import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.StringTranslate;

public class GuiMinecriftSettings extends BaseGuiSettings
{
	static EnumOptions[] minecriftOptions = new EnumOptions[] {
//            EnumOptions.EYE_HEIGHT,
//            EnumOptions.HUD_OPACITY,
//            EnumOptions.HUD_SCALE,
//            EnumOptions.HUD_DISTANCE,
//            EnumOptions.RENDER_OWN_HEADWEAR,
//            EnumOptions.RENDER_PLAYER_OFFSET,
        };

    /** An array of all of EnumOption's video options. */

    public GuiMinecriftSettings( GuiScreen par1GuiScreen,
                                GameSettings par2GameSettings)
    {
    	super( par1GuiScreen, par2GameSettings );
    	screenTitle = "VR Settings";
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        StringTranslate stringTranslate = StringTranslate.getInstance();
        this.buttonList.clear();
        this.buttonList.add(new GuiSmallButtonEx(EnumOptions.USE_VR.returnEnumOrdinal(), this.width / 2 - 78, this.height / 6 - 14, EnumOptions.USE_VR, this.guiGameSettings.getKeyBinding(EnumOptions.USE_VR)));
        this.buttonList.add(new GuiButtonEx(200, this.width / 2 - 100, this.height / 6 + 168, stringTranslate.translateKey("gui.done")));
        EnumOptions[] buttons = minecriftOptions;

        for (int var12 = 2; var12 < buttons.length + 2; ++var12)
        {
            EnumOptions var8 = buttons[var12 - 2];
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10;

            if (var8.getEnumFloat())
            {
                float minValue = 0.0f;
                float maxValue = 1.0f;
                float increment = 0.01f;

                this.buttonList.add(new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guiGameSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guiGameSettings.getOptionFloatValue(var8)));
            }
            else
            {
                this.buttonList.add(new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, this.guiGameSettings.getKeyBinding(var8)));
            }
        }

        String buttonText [] =
        {
            "Player Preferences...",
            "HUD / Overlay Settings...",
            "", "",
            "Head Orientation Tracking...",
            "Optics/Rendering...",
            "Head Position Tracking...",
            "Move/Aim Control...",
        };

        for( int i = 0; i < buttonText.length; ++i )
        {
        	int var12 = buttons.length + 2 + i;
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = (this.height / 6 + 21 * (var12 / 2) - 10) + 20;

            if (!buttonText[i].isEmpty())
            {
                GuiSmallButtonEx btn = new GuiSmallButtonEx(201+i, width, height, buttonText[i] );
                //btn.enabled = this.guiGameSettings.useVRRenderer; //TODO: could be good, maybe not yet
                this.buttonList.add(btn);
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
                this.guiGameSettings.setOptionValue(((GuiSmallButtonEx)par1GuiButton).returnEnumOptions(), 1);
                par1GuiButton.displayString = this.guiGameSettings.getKeyBinding(EnumOptions.getEnumOptions(par1GuiButton.id));

                if (num == EnumOptions.USE_VR)
                {
                    if (vrRenderer != null)
                        vrRenderer._FBOInitialised = false;
                }
            }
            else if (par1GuiButton.id == 201)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiPlayerPreferenceSettings(this, this.guiGameSettings));
            }
            else if (par1GuiButton.id == 202)
            {
                if( mc.headTracker != null )
                {
                    this.mc.gameSettings.saveOptions();
                    this.mc.displayGuiScreen(new GuiHUDSettings(this, this.guiGameSettings));
                }
            }
            else if (par1GuiButton.id == 205)
            {
            	if( mc.headTracker != null )
            	{
	                this.mc.gameSettings.saveOptions();
	                this.mc.displayGuiScreen(new GuiHeadOrientationSettings(this, this.guiGameSettings));
            	}
            } 
            else if (par1GuiButton.id == 206)
            {
            	if( mc.headTracker != null && mc.hmdInfo != null && mc.positionTracker != null )
            	{
	                this.mc.gameSettings.saveOptions();
	                this.mc.displayGuiScreen(new GuiRenderOpticsSettings(this, this.guiGameSettings));
            	}
            } 
            else if (par1GuiButton.id == 207)
            {
            	if( mc.positionTracker != null )
            	{
	                this.mc.gameSettings.saveOptions();
	                this.mc.displayGuiScreen(new GuiHeadPositionSettings(this, this.guiGameSettings));
            	}
            } 
            else if (par1GuiButton.id == 208)
            {
            	if( mc.lookaimController != null )
            	{
	                this.mc.gameSettings.saveOptions();
	                this.mc.displayGuiScreen(new GuiMoveAimSettings(this, this.guiGameSettings));
            	}
            } 
            else if (par1GuiButton.id == 200)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
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
    	case USE_VR:
    		return new String[] {
				"Whether to enable all the fun new Virtual Reality features",
				"  ON: Yay Fun!",
				"  OFF: Sad vanilla panda: gameplay unchanged"
    		};
    	default:
    		return null;
    	}
    	else
    	switch(buttonId)
    	{
            case 201:
                return new String[] {
                        "Open this configuration screen to adjust the Player",
                        "  avatar preferences, select Oculus profiles etc.",
                        "  Ex: IPD, Player (Eye) Height"
                };
            case 202:
                return new String[] {
                        "Open this configuration screen to adjust the Head",
                        "Up Display (HUD) overlay properties.",
                        "  Ex: HUD size, HUD distance, Crosshair options"
                };
	    	case 205:
	    		return new String[] {
	    			"Open this configuration screen to adjust the Head",
	    			"  Tracker orientation (direction) settings. ",
	    			"  Ex: Head Tracking Selection (Hydra/Oculus), Prediction"
	    		};
	    	case 206:
	    		return new String[] {
	    			"Open this configuration screen to adjust the Head ",
	    			"  Mounted Display optics or other rendering features.",
	    			"  Ex: FOV, Distortion, FSAA, Chromatic Abberation"
	    		};
	    	case 207:
	    		return new String[] {
	    			"Open this configuration screen to adjust the Head",
	    			"  Tracker position settings. ",
	    			"  Ex: Head Position Selection (Hydra/None), " ,
	    			"       Hydra head placement (left, right, top etc)"
	    		};
	    	case 208:
	    		return new String[] {
	    			"Open this configuration screen to adjust how the ",
	    			"  character is controlled. ",
	    			"  Ex: Look/move/aim decouple, joystick sensitivty, " ,
	    			"     Keyhole width, Mouse-pitch-affects camera" ,
	    		};
    		default:
    			return null;
    	}
    }

}

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
            EnumOptions.PLAYER_HEIGHT,
            EnumOptions.EYE_PROTRUSION,
            EnumOptions.NECK_LENGTH,
            EnumOptions.HUD_OPACITY,
            EnumOptions.HUD_SCALE,
            EnumOptions.HUD_DISTANCE,
            EnumOptions.RENDER_OWN_HEADWEAR,
            EnumOptions.RENDER_PLAYER_OFFSET,
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

                if (var8 == EnumOptions.PLAYER_HEIGHT)
                {
                    minValue = 1.62f;
                    maxValue = 1.85f;
                    increment = 0.01f;
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
                if (var8 == EnumOptions.HUD_SCALE)
                {
                    minValue = 0.5f;
                    maxValue = 1.5f;
                    increment = 0.01f;
                }
                if (var8 == EnumOptions.HUD_DISTANCE)
                {
                    minValue = 0.5f;
                    maxValue = 3.0f;
                    increment = 0.02f;
                }
                if (var8 == EnumOptions.RENDER_PLAYER_OFFSET)
                {
                    minValue = 0.0f;
                    maxValue = 0.25f;
                    increment = 0.01f;
                }

                this.buttonList.add(new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guiGameSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guiGameSettings.getOptionFloatValue(var8)));
            }
            else
            {
                this.buttonList.add(new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, this.guiGameSettings.getKeyBinding(var8)));
            }
        }
        for( int i = 0; i < 4; ++i )
        {
        	int var12 = buttons.length + 2 + i;
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10;
            String buttonText [] =
        	{
        		"Head Orientation Tracking...",
        		"Optics/Rendering...",
        		"Head Position Tracking...",
        		"Move/Aim Control...",
        	};
            
            GuiSmallButtonEx btn = new GuiSmallButtonEx(201+i, width, height, buttonText[i] ); 
            //btn.enabled = this.guiGameSettings.useVRRenderer; //TODO: could be good, maybe not yet
        	this.buttonList.add(btn);
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
            	if( mc.headTracker != null )
            	{
	                this.mc.gameSettings.saveOptions();
	                this.mc.displayGuiScreen(new GuiHeadOrientationSettings(this, this.guiGameSettings));
            	}
            } 
            else if (par1GuiButton.id == 202)
            {
            	if( mc.headTracker != null && mc.hmdInfo != null && mc.positionTracker != null )
            	{
	                this.mc.gameSettings.saveOptions();
	                this.mc.displayGuiScreen(new GuiRenderOpticsSettings(this, this.guiGameSettings));
            	}
            } 
            else if (par1GuiButton.id == 203)
            {
            	if( mc.positionTracker != null )
            	{
	                this.mc.gameSettings.saveOptions();
	                this.mc.displayGuiScreen(new GuiHeadPositionSettings(this, this.guiGameSettings));
            	}
            } 
            else if (par1GuiButton.id == 204)
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
    	case PLAYER_HEIGHT:
    		return new String[] {
				"Your real-world Eye Height when standing (in meters)",
				"  Setting this value isn't required, but you should",
				"  strive to get it as close as possible for an accurate",
				"  experience"
    		};
    	case EYE_PROTRUSION:
    		return new String[] {
				"Distance from \"head-center\" to your eyes (in meters)",
				"  Not required, but get it close for the best experience",
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
				"  Not required, but get it close for the best experience",
				" (\"Y\" distance below)     ____  ",
				"                              /      \\ ",
				"                              |   Y  o ",
				"                              |   Y  _\\",
				"                               \\ Y /",
				"                                 |Y|"
    		};
    	case HUD_OPACITY:
    		return new String[] {
				"Whether the HUD and UI are slightly transparent",
				"  ON: HUD and UI are transparent",
				"  OFF: HUD and UI are opaque"
    		};
    	case HUD_SCALE:
    		return new String[] {
				"Relative size HUD takes up in field-of-view",
				"  The units are just relative, not in degrees",
				"  or a fraction of FOV or anything"
    		};
    	case HUD_DISTANCE:
    		return new String[] {
				"Distance the floating HUD is drawn in front of your body",
				"  The relative size of the HUD is unchanged by this",
				"  Distance is in meters (though isn't obstructed by blocks)"
    		};
        case RENDER_OWN_HEADWEAR:
            return new String[] {
                    "Whether to render the player's own head-ware or not",
                    "  ON: The head-ware is rendered. May obscure your view!",
                    "  OFF: Not rendered."
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
	    			"Open this configuration screen to adjust the Head",
	    			"  Tracker orientation (direction) settings. ",
	    			"  Ex: Head Tracking Selection (Hydra/Oculus), Prediction"
	    		};
	    	case 202:
	    		return new String[] {
	    			"Open this configuration screen to adjust the Head ",
	    			"  Mounted Display optics or other rendering features.",
	    			"  Ex: IPD, FOV, Distortion, FSAA, Chromatic Abberation"
	    		};
	    	case 203:
	    		return new String[] {
	    			"Open this configuration screen to adjust the Head",
	    			"  Tracker position settings. ",
	    			"  Ex: Head Position Selection (Hydra/None), " ,
	    			"       Hydra head placement (left, right, top etc)"
	    		};
	    	case 204:
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

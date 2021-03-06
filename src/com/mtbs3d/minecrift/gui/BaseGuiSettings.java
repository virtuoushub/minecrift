/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.gui;

import com.mtbs3d.minecrift.VRRenderer;
import com.mtbs3d.minecrift.settings.VRSettings;

import net.minecraft.src.Minecraft;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

public class BaseGuiSettings extends GuiScreen
{
	protected GuiScreen parentGuiScreen;

    /** The title string that is displayed in the top-center of the screen. */
    protected String screenTitle = "";

    /** GUI game settings */
    protected VRSettings guivrSettings;

    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private long mouseStillTimeMs = 0L;
    public static final long TOOLTIP_DELAY_MS = 750;

    protected VRRenderer vrRenderer;

    protected boolean reinit = false;

    /**
     * True if the system is 64-bit (using a simple indexOf test on a system property)
     */
    private boolean is64bit = false;

    /** An array of all of EnumOption's video options. */

    public BaseGuiSettings( GuiScreen par1GuiScreen,
                                VRSettings par2vrSettings)
    {
		this.parentGuiScreen = par1GuiScreen;
        this.guivrSettings = par2vrSettings;
        this.vrRenderer = Minecraft.getMinecraft().vrRenderer;
    }

    public void drawScreen(int par1, int par2, float par3) {
    	this.drawScreen( par1, par2, par3, true );
    }
    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3, boolean drawBackground)
    {
        if (this.reinit)
        {
            initGui();
            this.reinit = false;
        }

        if( drawBackground)
        	this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 15, 16777215);
        super.drawScreen(par1, par2, par3);

        if (Math.abs(par1 - this.lastMouseX) <= 5 && Math.abs(par2 - this.lastMouseY) <= 5)
        {
            long delayMs = TOOLTIP_DELAY_MS;

            if (System.currentTimeMillis() >= this.mouseStillTimeMs + delayMs)
            {
                int var5 = this.width / 2 - 150;
                int var6 = this.height / 6 - 5;

                if (par2 <= var6 + 98)
                {
                    var6 += 105;
                }

                int var7 = var5 + 150 + 150;
                int var8 = var6 + 84 + 10;
                GuiButton var9 = this.getSelectedButton(par1, par2);

                if (var9 != null)
                {
                    String var10 = this.getButtonName(var9.displayString);
                    String[] var11 = this.getTooltipLines(var10, var9.id);

                    if (var11 == null)
                    {
                        return;
                    }

                    this.drawGradientRect(var5, var6, var7, var8, -536870912, -536870912);

                    for (int var12 = 0; var12 < var11.length; ++var12)
                    {
                        String var13 = var11[var12];
                        this.fontRenderer.drawStringWithShadow(var13, var5 + 5, var6 + 5 + var12 * 11, 14540253);
                    }
                }
            }
        }
        else
        {
            this.lastMouseX = par1;
            this.lastMouseY = par2;
            this.mouseStillTimeMs = System.currentTimeMillis();
        }
    }

    protected String[] getTooltipLines(String displayString, int buttonId )
    {
        return null;
    }

    protected String getButtonName(String var1)
    {
        int var2 = var1.indexOf(58);
        return var2 < 0 ? var1 : var1.substring(0, var2);
    }

    protected GuiButton getSelectedButton(int var1, int var2)
    {
        for (int var3 = 0; var3 < this.buttonList.size(); ++var3)
        {
            GuiButtonEx var4 = (GuiButtonEx)this.buttonList.get(var3);
            boolean var5 = var1 >= var4.xPosition && var2 >= var4.yPosition && var1 < var4.xPosition + var4.getWidth() && var2 < var4.yPosition + var4.getHeight();

            if (var5)
            {
                return var4;
            }
        }

        return null;
    }
}

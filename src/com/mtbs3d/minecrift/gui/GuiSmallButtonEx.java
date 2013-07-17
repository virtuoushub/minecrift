/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.gui;

import net.minecraft.src.Minecraft;
import net.minecraft.src.EnumOptions;

/**
 * Created with IntelliJ IDEA.
 * User: Engineer
 * Date: 6/5/13
 * Time: 7:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class GuiSmallButtonEx extends GuiButtonEx
{
    private final EnumOptions enumOptions;
    GuiEventEx _eventHandler = null;

    public GuiSmallButtonEx(int par1, int par2, int par3, String par4Str)
    {
        this(par1, par2, par3, (EnumOptions)null, par4Str);
    }

    public GuiSmallButtonEx(int par1, int par2, int par3, int par4, int par5, String par6Str)
    {
        super(par1, par2, par3, par4, par5, par6Str);
        this.enumOptions = null;
    }

    public GuiSmallButtonEx(int par1, int par2, int par3, EnumOptions par4EnumOptions, String par5Str)
    {
        super(par1, par2, par3, 150, 20, par5Str);
        this.enumOptions = par4EnumOptions;
    }

    public EnumOptions returnEnumOptions()
    {
        return this.enumOptions;
    }

    void setEventHandler(GuiEventEx eventHandler)
    {
        _eventHandler = eventHandler;
    }

    public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3)
    {
        boolean result = super.mousePressed(par1Minecraft, par2, par3);

        if (_eventHandler != null && result)
            _eventHandler.event(GuiEventEx.ID_VALUE_CHANGED, enumOptions);

        return result;
    }
}

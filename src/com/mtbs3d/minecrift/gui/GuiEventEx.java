package com.mtbs3d.minecrift.gui;

import net.minecraft.src.EnumOptions;

/**
 * Created with IntelliJ IDEA.
 * User: Pete
 * Date: 6/12/13
 * Time: 11:01 PM
 * To change this template use File | Settings | File Templates.
 */
public interface GuiEventEx
{
    public static int ID_VALUE_CHANGED = 0;

    public void event(int id, EnumOptions enumm);
}

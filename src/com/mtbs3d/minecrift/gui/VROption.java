package com.mtbs3d.minecrift.gui;

import net.minecraft.src.Minecraft;
import net.minecraft.src.EnumOptions;

/**
 * Created with IntelliJ IDEA.
 * User: Engineer
 * Date: 7/7/13
 * Time: 2:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class VROption
{
    public static enum Position
    {
        POS_LEFT,
        POS_CENTER,
        POS_RIGHT,
    };

    public static final boolean ENABLED = true;
    public static final boolean DISABLED = false;

    EnumOptions _e;
    Position _pos;
    float _row;
    boolean _enabled;
    String _title = "";
    int _ordinal;

    boolean _defaultb;

    float _defaultf;
    float _maxf;
    float _minf;
    float _incrementf;

    int _defaulti;
    int _maxi;
    int _mini;
    int _incrementi;

    VROption(EnumOptions e, Position pos, float row, boolean enabled, String title)
    {
        _e = e;
        _pos = pos;
        _row = row;
        if (title != null)
            _title = title;
        _enabled = enabled;
    }

    VROption(int ordinal, Position pos, float row, boolean enabled, String title)
    {
        _ordinal = ordinal;
        _pos = pos;
        _row = row;
        _title = title;
        _enabled = enabled;
    }

    public int getWidth(int screenWidth)
    {
        if (_pos == Position.POS_LEFT)
            return screenWidth / 2 - 155 + 0 * 160;
        else if (_pos == Position.POS_RIGHT)
            return screenWidth / 2 - 155 + 1 * 160;
        else
            return screenWidth / 2 - 155 + 1 * 160 / 2;
    }

    public int getHeight(int screenHeight)
    {
        return (int)Math.ceil(screenHeight / 6 + 21 * _row - 10);
    }

    public String getButtonText()
    {
        if (_title.isEmpty())
        {
            if (_e != null)
                return Minecraft.getMinecraft().vrSettings.getKeyBinding(_e);
        }

        return _title;
    }

    public int getOrdinal()
    {
        if (_e == null)
            return _ordinal;
        else
            return _e.returnEnumOrdinal();
    }
}

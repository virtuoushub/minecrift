package com.mtbs3d.minecrift.gui;

import com.mtbs3d.minecrift.VRRenderer;
import com.mtbs3d.minecrift.gui.*;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EnumOptions;
import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.StringTranslate;

public class GuiMinecriftSettings extends GuiScreen
{
	static EnumOptions[] minecriftOptions = new EnumOptions[] {
            EnumOptions.PLAYER_HEIGHT,
            EnumOptions.EYE_PROTRUSION,
            EnumOptions.NECK_LENGTH,
            EnumOptions.MOVEMENT_MULTIPLIER,
            EnumOptions.HUD_SCALE,
            EnumOptions.HUD_DISTANCE,
            EnumOptions.HUD_OPACITY,
            EnumOptions.RENDER_OWN_HEADWEAR,
            EnumOptions.ALLOW_MOUSE_PITCH_INPUT,
            EnumOptions.DECOUPLEMOVELOOK,
            //EnumOptions.POSITIONAL_TRACK_METHOD,
        };

	private GuiScreen parentGuiScreen;

    /** The title string that is displayed in the top-center of the screen. */
    protected String screenTitle = "VR Settings";

    /** GUI game settings */
    private GameSettings guiGameSettings;

    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private long mouseStillTime = 0L;

    private VRRenderer vrRenderer;

    /**
     * True if the system is 64-bit (using a simple indexOf test on a system property)
     */
    private boolean is64bit = false;

    /** An array of all of EnumOption's video options. */

    public GuiMinecriftSettings( GuiScreen par1GuiScreen,
                                GameSettings par2GameSettings)
    {
		this.parentGuiScreen = par1GuiScreen;
        this.guiGameSettings = par2GameSettings;
        this.vrRenderer = Minecraft.getMinecraft().vrRenderer;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        StringTranslate stringTranslate = StringTranslate.getInstance();
       // this.screenTitle = var1.translateKey("options.videoTitle");
        this.buttonList.clear();
        this.buttonList.add(new GuiButtonEx(200, this.width / 2 - 100, this.height / 6 + 168, stringTranslate.translateKey("gui.done")));
        this.buttonList.add(new GuiSmallButtonEx(EnumOptions.USE_VR.returnEnumOrdinal(), this.width / 2 - 78, this.height / 6 - 14, EnumOptions.USE_VR, this.guiGameSettings.getKeyBinding(EnumOptions.USE_VR)));
        this.buttonList.add(new GuiButtonEx(201, this.width / 2 - 100, this.height / 6 + 128, "Oculus Settings..."));
        this.buttonList.add(new GuiButtonEx(202, this.width / 2 - 100, this.height / 6 + 148, "Positional Tracking..."));
        this.is64bit = false;
        String[] archStrings = new String[] {"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};
        String[] var3 = archStrings;
        int var4 = archStrings.length;

        for (int var5 = 0; var5 < var4; ++var5)
        {
            String var6 = var3[var5];
            String var7 = System.getProperty(var6);

            if (var7 != null && var7.contains("64"))
            {
                this.is64bit = true;
                break;
            }
        }

        int var9 = 0;
        var4 = this.is64bit ? 0 : -15;
        EnumOptions[] var10 = minecriftOptions;
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

                if (var8 == EnumOptions.PLAYER_HEIGHT)
                {
                    minValue = 1.62f;
                    maxValue = 1.85f;
                    increment = 0.01f;
                }
                if (var8 == EnumOptions.EYE_PROTRUSION)
                {
                    minValue = 0.12f;
                    maxValue = 0.25f;
                    increment = 0.001f;
                }
                if (var8 == EnumOptions.NECK_LENGTH)
                {
                    minValue = 0.15f;
                    maxValue = 0.25f;
                    increment = 0.001f;
                }
                if (var8 == EnumOptions.MOVEMENT_MULTIPLIER)
                {
                    minValue = 0.15f;
                    maxValue = 1.0f;
                    increment = 0.01f;
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

                this.buttonList.add(new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guiGameSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guiGameSettings.getOptionFloatValue(var8)));
            }
            else
            {
                this.buttonList.add(new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, this.guiGameSettings.getKeyBinding(var8)));
            }

            ++var9;
        }
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        if (par1GuiButton.enabled)
        {
            int var2 = this.guiGameSettings.guiScale;

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

            if (par1GuiButton.id == 201)
            {
            	if( mc.headTracker != null && mc.hmdInfo != null && mc.positionTracker != null )
            	{
	                this.mc.gameSettings.saveOptions();
	                this.mc.displayGuiScreen(new GuiMinecriftDisplaySettings(this, this.guiGameSettings));
            	}
            }

            if (par1GuiButton.id == 202)
            {
            	if( mc.positionTracker != null )
            	{
	                this.mc.gameSettings.saveOptions();
	                this.mc.displayGuiScreen(new GuiHydraSettings(this, this.guiGameSettings));
            	}
            }

            if (par1GuiButton.id == 200)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 20, 16777215);
        super.drawScreen(par1, par2, par3);

        if (Math.abs(par1 - this.lastMouseX) <= 5 && Math.abs(par2 - this.lastMouseY) <= 5)
        {
            short var4 = 700;

            if (System.currentTimeMillis() >= this.mouseStillTime + (long)var4)
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
                    String[] var11 = this.getTooltipLines(var10);

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
            this.mouseStillTime = System.currentTimeMillis();
        }
    }

    private String[] getTooltipLines(String var1)
    {
        return var1.equals("Chrom. Ab. Correction") ? new String[] {"Chromatic aberration correction", "  OFF - no correction", "  ON - correction applied"} : null;
    }

    private String getButtonName(String var1)
    {
        int var2 = var1.indexOf(58);
        return var2 < 0 ? var1 : var1.substring(0, var2);
    }

    private GuiButton getSelectedButton(int var1, int var2)
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

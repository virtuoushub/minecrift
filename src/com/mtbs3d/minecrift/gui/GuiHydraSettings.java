package com.mtbs3d.minecrift.gui;

import com.mtbs3d.minecrift.VRRenderer;
import net.minecraft.src.*;

/**
 * Created with IntelliJ IDEA.
 * User: Engineer
 * Date: 6/11/13
 * Time: 4:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class GuiHydraSettings extends GuiScreen
{
    static EnumOptions[] hydraOptions = new EnumOptions[] {
    };

    private GuiScreen parentGuiScreen;

    /** The title string that is displayed in the top-center of the screen. */
    protected String screenTitle = "Hydra Settings";

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

    public GuiHydraSettings(GuiScreen par1GuiScreen,
                            GameSettings par2GameSettings)
    {
        this.parentGuiScreen = par1GuiScreen;
        this.guiGameSettings = par2GameSettings;
        this.vrRenderer = this.guiGameSettings.mc.vrRenderer;
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
        //this.buttonList.add(new GuiButtonEx(201, this.width / 2 - 100, this.height / 6 + 128, "Reset"));
        this.buttonList.add(new GuiButtonEx(202, this.width / 2 - 100, this.height / 6 + 108, "Reset Origin Point"));
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
        EnumOptions[] var10 = hydraOptions;
        int var11 = var10.length;

        for (int var12 = 0; var12 < var11; ++var12)
        {
            EnumOptions var8 = var10[var12];
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10;

            if (var8.getEnumFloat())
            {
                float minValue = 0.0f;
                float maxValue = 1.0f;
                int precision = 2;

//                if (var8 == EnumOptions.IPD)
//                {
//                    minValue = 0.055f;
//                    maxValue = 0.075f;
//                }


                this.buttonList.add(new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guiGameSettings.getKeyBinding(var8), minValue, maxValue, precision, this.guiGameSettings.getOptionFloatValue(var8)));
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
            else if (par1GuiButton.id == 201) // Reset all
            {

            }
            else if (par1GuiButton.id == 202) // Reset origin
            {
                this.guiGameSettings.resetPositionalTrackPos = true;
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
        return var1.equals("Chrom. Ab. Correction") ? new String[] {"Chromatic aberration correction", "  OFF - no correction", "  ON - correction applied"} :
                (var1.equals("Chrom. Ab. Correction") ? new String[] {"Chromatic aberration correction", "  OFF - no correction", "  ON - correction applied"} :
                        null);
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

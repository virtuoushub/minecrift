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
public class GuiHydraSettings extends GuiScreen implements GuiEventEx
{
    static EnumOptions[] hydraOptions = new EnumOptions[] {
        EnumOptions.POS_TRACK_HYDRALOC,
        EnumOptions.POS_TRACK_HYDRA_DISTANCE_SCALE,
        EnumOptions.POS_TRACK_HYDRA_USE_CONTROLLER_ONE,
        EnumOptions.POS_TRACK_HYDRA_OFFSET_X,
        EnumOptions.DUMMY,
        EnumOptions.POS_TRACK_HYDRA_OFFSET_Y,
        EnumOptions.DUMMY,
        EnumOptions.POS_TRACK_HYDRA_OFFSET_Z,
    };

    private GuiScreen parentGuiScreen;

    /** The title string that is displayed in the top-center of the screen. */
    protected String screenTitle = "Positional Tracking Configuration";

    /** GUI game settings */
    private GameSettings guiGameSettings;

    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private long mouseStillTime = 0L;

    protected boolean reinit = false;
    protected boolean reinitOffsetDefaults = false;

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
        if (this.reinitOffsetDefaults)
        {
            setHydraLocOffsetDefaults();
            this.reinitOffsetDefaults = false;
        }

        StringTranslate stringTranslate = StringTranslate.getInstance();
        // this.screenTitle = var1.translateKey("options.videoTitle");
        this.buttonList.clear();
        this.buttonList.add(new GuiButtonEx(200, this.width / 2 - 100, this.height / 6 + 168, stringTranslate.translateKey("gui.done")));

        GuiSmallButtonEx posMethodButton = new GuiSmallButtonEx(EnumOptions.POS_TRACK_METHOD.returnEnumOrdinal(), this.width / 2 - 78, this.height / 6 - 14, EnumOptions.POS_TRACK_METHOD, this.guiGameSettings.getKeyBinding(EnumOptions.POS_TRACK_METHOD));
        posMethodButton.setEventHandler(this);
        this.buttonList.add(posMethodButton);

        //this.buttonList.add(new GuiButtonEx(201, this.width / 2 - 100, this.height / 6 + 128, "Reset"));
        GuiButtonEx resetPosButton = new GuiButtonEx(202, this.width / 2 - 100, this.height / 6 + 108, "Reset Origin Point");
        resetPosButton.enabled = this.guiGameSettings.posTrackMethod == GameSettings.POS_TRACK_HYDRA ? true : false;
        this.buttonList.add(resetPosButton);

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

        for (int var12 = 2; var12 < var11 + 2; ++var12)
        {
            EnumOptions var8 = var10[var12 - 2];
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10;

            if (var8 != EnumOptions.DUMMY)
            {
                if (var8.getEnumFloat())
                {
                    float minValue = 0.0f;
                    float maxValue = 1.0f;
                    float increment = 0.01f;

                    if (var8 == EnumOptions.POS_TRACK_HYDRA_OFFSET_X || var8 == EnumOptions.POS_TRACK_HYDRA_OFFSET_Y)
                    {
                        minValue = -0.30f;
                        maxValue = 0.30f;
                        increment = 0.001f;
                    }
                    else if (var8 == EnumOptions.POS_TRACK_HYDRA_OFFSET_Z)
                    {
                        minValue = -0.30f;
                        maxValue = 0.30f;
                        increment = 0.001f;
                    }
                    else if (var8 == EnumOptions.POS_TRACK_HYDRA_DISTANCE_SCALE)
                    {
                        minValue = 0.8f;
                        maxValue = 1.2f;
                        increment = 0.001f;
                    }

                    GuiSliderEx slider = new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guiGameSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guiGameSettings.getOptionFloatValue(var8));
                    slider.setEventHandler(this);
                    slider.enabled = getEnabledState(var8);
                    this.buttonList.add(slider);
                }
                else
                {
                    GuiSmallButtonEx smallButton = new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, this.guiGameSettings.getKeyBinding(var8));
                    smallButton.setEventHandler(this);
                    smallButton.enabled = getEnabledState(var8);
                    this.buttonList.add(smallButton);
                }
            }

            ++var9;
        }
    }

    private boolean getEnabledState(EnumOptions var8)
    {
        boolean enabled = false;
        String s = var8.getEnumString();

        if (this.guiGameSettings.posTrackMethod != GameSettings.POS_TRACK_HYDRA)
        {
            return false;
        }

        if (var8 == EnumOptions.POS_TRACK_HYDRALOC || var8 == EnumOptions.POS_TRACK_HYDRA_DISTANCE_SCALE)
            return true;

        if (this.guiGameSettings.posTrackHydraLoc == GameSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT)
            return false;

        return true;
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
                this.guiGameSettings.posTrackResetPosition = true;
            }
        }
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

    @Override
    public void event(int id, EnumOptions enumm)
    {
        if (enumm == EnumOptions.POS_TRACK_HYDRALOC)
        {
            this.reinitOffsetDefaults = true;
        }

        if (enumm == EnumOptions.POS_TRACK_METHOD ||
            enumm == EnumOptions.POS_TRACK_HYDRALOC)
        {
            this.reinit = true;
        }
    }

    private void setHydraLocOffsetDefaults()
    {
        switch (this.guiGameSettings.posTrackHydraLoc)
        {
            case GameSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT:
                this.guiGameSettings.posTrackHydraOffsetX = 0.108f;
                this.guiGameSettings.posTrackHydraOffsetY = 0.0f;
                this.guiGameSettings.posTrackHydraOffsetZ = 0.0f;
                break;
            case GameSettings.POS_TRACK_HYDRA_LOC_HMD_TOP:
                this.guiGameSettings.posTrackHydraOffsetX = 0.0f;
                this.guiGameSettings.posTrackHydraOffsetY = -0.085f;
                this.guiGameSettings.posTrackHydraOffsetZ = 0.0f;
                break;
            case GameSettings.POS_TRACK_HYDRA_LOC_HMD_RIGHT:
                this.guiGameSettings.posTrackHydraOffsetX = -0.108f;
                this.guiGameSettings.posTrackHydraOffsetY = 0.0f;
                this.guiGameSettings.posTrackHydraOffsetZ = 0.0f;
                break;
            default:
                this.guiGameSettings.posTrackHydraOffsetX = 0.0f;
                this.guiGameSettings.posTrackHydraOffsetY = 0.0f;
                this.guiGameSettings.posTrackHydraOffsetZ = 0.0f;
                break;
        }
    }
}

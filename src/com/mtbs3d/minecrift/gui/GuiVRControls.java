package com.mtbs3d.minecrift.gui;

import org.lwjgl.input.Keyboard;

import net.minecraft.src.EnumChatFormatting;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.StringTranslate;

import com.mtbs3d.minecrift.control.ControlBinding;
import com.mtbs3d.minecrift.control.ControlBinding.ControlBindCallback;
import com.mtbs3d.minecrift.settings.VRSettings;

public class GuiVRControls extends BaseGuiSettings implements ControlBindCallback {

	static String getBindString( ControlBinding binding ) {
		return (binding.isValid() ? "" : (""+ EnumChatFormatting.RED )) +StringTranslate.getInstance().translateKey(binding.getDescription())+": "+binding.boundTo();
	}
	class ControlBindButton extends GuiButtonEx {
		public ControlBinding binding;
		public ControlBindButton(int id, int xPos, int yPos, ControlBinding binding) {
			this(id,xPos,yPos,150,20,binding);
		}
		public ControlBindButton(int id, int xPos, int yPos, int width, int height, ControlBinding binding) {
			super(id, xPos, yPos, width, height, getBindString(binding));
			this.binding = binding;
		}
		
		void startBinding() {
			super.displayString = "" + EnumChatFormatting.WHITE + "> " + EnumChatFormatting.YELLOW + "??? " + EnumChatFormatting.WHITE + "<";
		}
		
		void doneBinding() {
			super.displayString = getBindString(binding);
		}
	};
	
	private ControlBindButton nextBind = null;
	public GuiVRControls(GuiScreen par1GuiScreen, VRSettings par2vrSettings) {
		super(par1GuiScreen, par2vrSettings);
        screenTitle = "Control Remapping";
	}

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char par1, int par2)
    {
        if ( nextBind != null && par2 == Keyboard.KEY_ESCAPE ) {
        	doneBinding();
        } else {
            super.keyTyped(par1, par2);
        }
    }

	@Override
	public void doneBinding() {
		if( nextBind != null ) {
			nextBind.doneBinding();
			nextBind = null;
		}
	}

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui() {
        StringTranslate stringTranslate = StringTranslate.getInstance();
        this.buttonList.clear();
        //this.buttonList.add(new GuiButtonEx(202, this.width / 2 - 100, this.height / 6 + 148, "Reset To Defaults"));
        this.buttonList.add(new GuiButtonEx(200, this.width / 2 - 100, this.height / 6 + 168, stringTranslate.translateKey("gui.done")));
        
        nextBind = null;

        for (int var12 = 0; var12 < ControlBinding.bindings.size(); ++var12) {
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * ( var12 / 2) - 10;

            this.buttonList.add( new ControlBindButton(var12, width, height, ControlBinding.bindings.get(var12)));
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3) {
        if (reinit) {
            initGui();
            reinit = false;
        }
        super.drawScreen(par1,par2,par3);
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton) {
    	if( par1GuiButton.id < 200 ) {
    		nextBind = (ControlBindButton)par1GuiButton;
    		nextBind.binding.setDoneBindingCallback(this);
    		nextBind.startBinding();
    		this.mc.lookaimController.mapBinding(nextBind.binding);
    	} else if (par1GuiButton.id == 200) {
            this.guivrSettings.saveOptions();
            this.mc.displayGuiScreen(this.parentGuiScreen);
        }
    }
}

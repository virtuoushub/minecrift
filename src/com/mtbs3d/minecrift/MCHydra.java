/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import com.mtbs3d.minecrift.api.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.sixense.ControllerData;
import com.sixense.EnumButton;
import com.sixense.Sixense;
import com.sixense.utils.ControllerManager;
import com.sixense.utils.enums.EnumSetupStep;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.GameSettings;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Vec3;

/**
 * @author Mark Browning
 *
 */
public class MCHydra extends BasePlugin implements ICenterEyePositionProvider, IOrientationProvider, IBodyAimController, IEventListener {

    ControllerManager cm;
    public static ControllerData[] newData = {new ControllerData(), new ControllerData(), new ControllerData(), new ControllerData()};

    Vec3 headPos; //in meters, in world coordinates
    Vec3 origin = Vec3.createVectorHelper(0, 0, 0);  //in meters, relative to hydra base station

    float baseStationYawOffset = 0.0f;

    boolean hydraRunning = false;
	private boolean hydraInitialized = false;
	private String initStatus = "";
	private String calibrationStep = "";

	private static boolean libraryLoaded = false;
	
    private boolean resetOrigin = true;
    private boolean resetOriginRotation = true;

	//latest data
	
	private float cont1PosX;
	private float cont1PosY;
	private float cont1PosZ;
	private float cont2PosX;
	private float cont2PosY;
	private float cont2PosZ;

	private float cont1Yaw;
	private float cont1Pitch;
	private float cont1Roll;
	private float cont2Yaw;
	private float cont2Pitch;
	private float cont2Roll; 
	
	//For IOrientationProvider implementation; allows setting origin to set yaw offset as well
	private float yawOffset = 0;


    private final float SCALE = 0.001f; //mm -> m
    private float userScale = SCALE;

    // Previously +X 1.0f, -Y -1.0f, +Z 1.0f
    private final float XDIRECTION = 1.0f;    // +X
    private final float YDIRECTION = 1.0f;    // +Y
    private final float ZDIRECTION = 1.0f;    // +Z
    
    //body/Aim implementation
    private float bodyYaw; //floating; not fixed to base station; 
    private float aimYaw; //relative to bodyYaw, so that IRL "forward"
    //(that is, baseStationYawOffset) is always the direction of movement

    //aimPitch is always cont2Pitch

    //Input/controller
	int lastcont1Buttons = 0;
	int lastcont2Buttons = 0;
	
	//For mouse menu emulation
	private int hydraMouseX = 0;
	private int hydraMouseY = 0;
	private boolean leftMouseClicked = false;
	private Field keyDownField; //Whee, reflection
	private Field buttonDownField; //Whee, reflection
	private boolean mouseUseJoystick = true;

	public MCHydra()
	{
		super();
        pluginID = "hydra";
        pluginName = "Razer Hydra";

	}
	@Override
	public String getInitializationStatus() {
		return initStatus;
	}

	@Override
	public String getVersion() {
		return "0.28";
	}

	@Override
	public boolean init(File nativeDir) {
		hydraInitialized = libraryLoaded;

		if( !libraryLoaded )
		{
			try {
				libraryLoaded = Sixense.LoadLibrary(nativeDir);
			}
			catch( UnsatisfiedLinkError e )
			{
				initStatus = e.getMessage();
			}
		}

		if( libraryLoaded )
		{
			init();
		}
		return hydraInitialized;
	}

	@Override
	public boolean init() {
		Sixense.init();
		cm = ControllerManager.getInstance();
		cm.setGameType(com.sixense.utils.enums.EnumGameType.ONE_PLAYER_TWO_CONTROLLER);
		Sixense.setActiveBase(0);
		Sixense.setFilterEnabled(Minecraft.getMinecraft().gameSettings.hydraUseFilter);
		
        try {
			keyDownField = Keyboard.class.getDeclaredField("keyDownBuffer");
			keyDownField.setAccessible(true);
			buttonDownField = Mouse.class.getDeclaredField("buttons");
			buttonDownField.setAccessible(true);
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		}

		hydraInitialized = true;
		return isInitialized(); //no re-init method
	}

	@Override
	public boolean isInitialized() {
		return hydraInitialized;
	}
	
	@Override
	public void poll() {
        // Poll hydras, get position information in metres
        Sixense.getAllNewestData(newData);
        Minecraft mc = Minecraft.getMinecraft();

        ControllerData cont1, cont2;
        if (mc.gameSettings.posTrackHydraUseController1)
        {
        	cont1 = newData[0];
        	cont2 = newData[1];
        }
        else
        {
        	cont1 = newData[1];
        	cont2 = newData[0];
        }

        cm.update(newData);
        EnumSetupStep step = cm.getCurrentStep();
        switch ( step )
        {
            case P1C2_IDLE:
                hydraRunning = true;
                calibrationStep = "";
                break;
            default:
                hydraRunning = false;
                if (step != null)
                {
                    System.out.println("HYDRA: " + step.toString());
                    calibrationStep = cm.getStepString();
                }
                break;
        }
        
        if(hydraRunning)
        {
			cont1Yaw   = cont1.yaw;
			cont1Pitch = cont1.pitch;
			cont1Roll  = cont1.roll;
	
			cont2Yaw   = cont2.yaw;
			cont2Pitch = cont2.pitch;
			cont2Roll  = cont2.roll;
	
	        userScale = SCALE * mc.gameSettings.posTrackHydraDistanceScale;
	
	        cont1PosX = userScale * cont1.pos[0] * XDIRECTION;
	        cont1PosY = userScale * cont1.pos[1] * YDIRECTION;
	        cont1PosZ = userScale * cont1.pos[2] * ZDIRECTION;
	
	        cont2PosX = userScale * cont2.pos[0] * XDIRECTION;
	        cont2PosY = userScale * cont2.pos[1] * YDIRECTION;
	        cont2PosZ = userScale * cont2.pos[2] * ZDIRECTION;
        }
        else
        {
			cont1Yaw   = 0;
			cont1Pitch = 0;
			cont1Roll  = 0;
	
			cont2Yaw   = 0;
			cont2Pitch = 0;
			cont2Roll  = 0;
			
	        cont1PosX = 0;
	        cont1PosY = 0;
	        cont1PosZ = 0;
	
	        cont2PosX = 0;
	        cont2PosY = 0;
	        cont2PosZ = 0;
        }

	        
        if( mc.lookaimController != this) return;
        if( mc.theWorld != null )
        {
	        EntityClientPlayerMP thePlayer = mc.thePlayer;
	        
	        //buttons?
	        //TODO: move this somewhere sane and make configurable
	        GameSettings settings = mc.gameSettings;
	
	        if((cont2.buttons & EnumButton.START.mask())>0 &&
	        	(lastcont2Buttons & EnumButton.START.mask()) == 0)
	        {
	        	if(mc.currentScreen != null)
		        	thePlayer.closeScreen();
	        	else
		        	settings.keyBindInventory.pressTime=1;
	        }

	        if((cont1.buttons & EnumButton.START.mask())>0 &&
	        	(lastcont1Buttons & EnumButton.START.mask()) == 0)
	        {
	        	if(mc.currentScreen != null)
		        	thePlayer.closeScreen();
	        	else
		        	mc.displayInGameMenu();
	        }
	       
	        //In game!
	        if( mc.currentScreen == null )
	        {
	        	hydraMouseX = 0;
	        	hydraMouseY = 0;

		        //aim/body adjustments
	        	float headYaw = 0;
	        	if( mc.gameSettings.keyholeHeadRelative )
		        	headYaw = mc.headTracker.getHeadYawDegrees();
	        	
	        	//Adjust keyhole width on controller pitch; otherwise its a very narrow window at the top and bottom
	        	float keyholeYaw = mc.gameSettings.aimKeyholeWidthDegrees/2/MathHelper.cos(cont2Pitch*PIOVER180);
	        	
	        	float bodyYawT = cont2Yaw - baseStationYawOffset; //

	            if( bodyYawT > headYaw + keyholeYaw  ) 
	            {
	            	bodyYawT = Math.min(10,bodyYawT - headYaw - keyholeYaw) * 0.1f*mc.gameSettings.joystickSensitivity; //TODO: add new sensitivity
	            	//Controller pointing too far right, move body to the right
	                aimYaw = headYaw + keyholeYaw;
	            }
	            else if( bodyYawT < headYaw -keyholeYaw )
	            {
	            	//Controller pointing too far left, move body to the left
	            	bodyYawT = Math.max(-10,bodyYawT -headYaw + keyholeYaw) * 0.1f*mc.gameSettings.joystickSensitivity;
	                aimYaw = headYaw - keyholeYaw;
	            }
	            else
	            {
	            	aimYaw = bodyYawT;
	            	bodyYawT = 0;
	            }
            	bodyYaw += bodyYawT;
                bodyYaw %= 360;
		        
		        if( thePlayer != null )
		        {
			        if( Math.abs(cont2.joystick_y)>0.01)
				        thePlayer.movementInput.baseMoveForward = cont2.joystick_y;
			        else
				        thePlayer.movementInput.baseMoveForward = 0.0f;
			        
			        if( Math.abs(cont2.joystick_x)>0.01)
				        thePlayer.movementInput.baseMoveStrafe = -cont2.joystick_x;
			        else
				        thePlayer.movementInput.baseMoveStrafe = 0.0f;
		        }
		
		        //Do buttons
		        if( cont2.trigger > 0.05 )
		        {
		        	if( !leftMouseClicked )
		        		mc.clickMouse(0);
		        	mc.gameSettings.keyBindAttack.pressed = true;
		        	leftMouseClicked = true;
		        }
		        else
		        {
		        	leftMouseClicked = false;
		        	mc.gameSettings.keyBindAttack.pressed = false;
		        }
		        	
		        if((cont2.buttons & EnumButton.BUMPER.mask()) >0 && 
		        	 (lastcont2Buttons & EnumButton.BUMPER.mask()) == 0)
		        {
		        	mc.clickMouse(1);
		        }

	        	mc.gameSettings.keyBindUseItem.pressed = (cont2.buttons & EnumButton.BUMPER.mask()) >0 ;
		        
		        if((cont2.buttons & EnumButton.JOYSTICK.mask())>0 &&
		        	 (lastcont2Buttons & EnumButton.JOYSTICK.mask()) == 0)
		        {
		        	settings.keyBindSneak.pressed = true;
		        }
		        if(( lastcont2Buttons   & EnumButton.JOYSTICK.mask())>0 &&
		        	(cont2.buttons & EnumButton.JOYSTICK.mask()) == 0)
		        {
		        	settings.keyBindSneak.pressed = false;
		        }
	
		        if((cont2.buttons & EnumButton.BUTTON_1.mask())>0 &&
		        	(lastcont2Buttons & EnumButton.BUTTON_1.mask()) == 0)
		        {
		        	settings.keyBindDrop.pressTime++;
		        }
	
		        if((cont2.buttons & EnumButton.BUTTON_2.mask())>0 &&
		        	(lastcont2Buttons & EnumButton.BUTTON_2.mask()) == 0)
		        {
		        	settings.keyBindJump.pressed = true;
		        }
		        if((lastcont2Buttons & EnumButton.BUTTON_2.mask())>0 &&
		        	(cont2.buttons & EnumButton.BUTTON_2.mask()) == 0)
		        {
		        	settings.keyBindJump.pressed = false;
		        }
		        
		        if((cont2.buttons & EnumButton.BUTTON_3.mask())>0 &&
		        	(lastcont2Buttons & EnumButton.BUTTON_3.mask()) == 0)
		        {
		        	thePlayer.inventory.changeCurrentItem(1);
		        }
		
		        if((cont2.buttons & EnumButton.BUTTON_4.mask())>0 &&
		        	(lastcont2Buttons & EnumButton.BUTTON_4.mask()) == 0)
		        {
		        	thePlayer.inventory.changeCurrentItem(-1);
		        }
	        }
        }
        
        //GUI controls
        if( mc.currentScreen != null )
        {
        	mouseUseJoystick = false;
        	if( mouseUseJoystick )
        	{
	        	hydraMouseX += 2*mc.gameSettings.joystickSensitivity*cont2.joystick_x;
	        	hydraMouseY += 2*mc.gameSettings.joystickSensitivity*cont2.joystick_y;
        	}
        	else
        	{
        		hydraMouseX = (int)(mc.displayWidth * (  0.5*cont2Yaw/75f   )); 
        		hydraMouseY = (int)(mc.displayWidth * ( -0.5*cont2Pitch/60f ));
        	}
            float scaleX = mc.displayFBWidth/(float)mc.displayWidth;
            float scaleY = mc.displayFBHeight/(float)mc.displayHeight;
            mc.currentScreen.mouseOffsetX = (int)(hydraMouseX*scaleX);
            mc.currentScreen.mouseOffsetY = (int)(hydraMouseY*scaleY);
	        int mouseX = Mouse.getX() + hydraMouseX;
	        int	mouseY = Mouse.getY() + hydraMouseY;
            Mouse.setCursorPosition(mouseX, mouseY);
            
            mouseX = (int)(mouseX*scaleX);
            mouseY = (int)(mouseY*scaleY);

            //hack, hack hack... Joystick button is right shift for use in GUI
            //Set the internal keyboard state that right shift is depressed if this button is
	        if( keyDownField != null )
	        {
	        	try {
	        		byte b = (byte)((cont2.buttons & EnumButton.JOYSTICK.mask())>0?1:0);
					((ByteBuffer)keyDownField.get(null)).put(Keyboard.KEY_RSHIFT,b);
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
	        }

	        if( cont2.trigger > 0.05 )
	        {
	        	//click left mouse button
	        	if(!leftMouseClicked)
	        		mc.currentScreen.mouseDown(mouseX,mouseY,0);
	        	else
	        		//Already down
	        		mc.currentScreen.mouseDrag(mouseX, mouseY);//Signals mouse move
	        	leftMouseClicked = true;
		        if( buttonDownField != null )
		        {
		        	try {
						((ByteBuffer)buttonDownField.get(null)).put(0,(byte) 1);
					} catch (IllegalArgumentException e) {
					} catch (IllegalAccessException e) {
					}
		        }
	        }
	        else if( leftMouseClicked )
	        {
        		mc.currentScreen.mouseUp(mouseX,mouseY,0);
	        	leftMouseClicked = false;
	        }

	        if((cont2.buttons & EnumButton.BUMPER.mask())>0 )
	        {
	        	if ((lastcont2Buttons & EnumButton.BUMPER.mask()) == 0)
	        		mc.currentScreen.mouseDown(mouseX,mouseY,1);
	        	else
	        		mc.currentScreen.mouseDrag(mouseX, mouseY);
		        if( buttonDownField != null )
		        {
		        	try {
						((ByteBuffer)buttonDownField.get(null)).put(1,(byte) 1);
					} catch (IllegalArgumentException e) {
					} catch (IllegalAccessException e) {
					}
		        }
	        }
	        else if( ( lastcont2Buttons & EnumButton.BUMPER.mask())>0 )
	        {
        		mc.currentScreen.mouseUp(mouseX,mouseY,1);
	        }
	        
        }

        lastcont1Buttons = cont1.buttons;
        lastcont2Buttons = cont2.buttons;
	}

	@Override
	public void destroy() {
		Sixense.exit();
        hydraRunning = false;
        hydraInitialized = false;
        initStatus = "Not initialised";
        cm = null;
	}

	@Override
	public void update(float yawHeadDegrees, float pitchHeadDegrees, float rollHeadDegrees,
                       float worldYawOffsetDegrees, float worldPitchOffsetDegrees, float worldRollOffsetDegrees)
    {
        if (!hydraInitialized || !hydraRunning)
        {
        	headPos = Vec3.createVectorHelper(0, 0, 0);
            return;
        }

        GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
        
        if( resetOriginRotation )
        {
        	//TODO: this might be backwards: with only a razer to test the yawHeadDegrees, its always the same!
        	if( Minecraft.getMinecraft().headTracker != this ) //if the positional tracker is the hydra, they are always aligned
        	{
        		baseStationYawOffset = cont1Yaw - yawHeadDegrees; //assume hydra oriented straight with head orientation
	        	if( gameSettings.posTrackHydraLoc == GameSettings.POS_TRACK_HYDRA_LOC_BACK_OF_HEAD)
	        		baseStationYawOffset -= 90;//assume hydra oriented at 90degrees to head orientation
        	}
        	resetOriginRotation = false;
        }
        

        // Using a single controller. Select controller. poll() has already switch left and right depending on configuration 
        //cont1 is for head tracking, if head tracking is enabled (TODO: make head tracking optional?)
        float rawX = cont1PosX;
        float rawY = cont1PosY;
        float rawZ = cont1PosZ;

        if (gameSettings.posTrackHydraLoc == GameSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT)
        {
        	//Otherwise, use average of controllers
            rawX = (cont1PosX + cont2PosX) / 2.0f;
            rawY = (cont1PosY + cont2PosY) / 2.0f;
            rawZ = (cont1PosZ + cont2PosZ) / 2.0f;


            if (gameSettings.posTrackHydraDebugCentreEyePos)
            {
                // Get controller offset
                float rotatedC1OffsetX = cont1PosX - rawX;
                float rotatedC1OffsetY = cont1PosX - rawY;
                float rotatedC1OffsetZ = cont1PosZ - rawZ;
                float rotatedC2OffsetX = cont2PosX - rawX;
                float rotatedC2OffsetY = cont2PosX - rawY;
                float rotatedC2OffsetZ = cont2PosZ - rawZ;

                debugOffsets("Left", yawHeadDegrees, pitchHeadDegrees, rollHeadDegrees, rotatedC1OffsetX, rotatedC1OffsetY, rotatedC1OffsetZ);
                debugOffsets("Right", yawHeadDegrees, pitchHeadDegrees, rollHeadDegrees, rotatedC2OffsetX, rotatedC2OffsetY, rotatedC2OffsetZ);
            }
        }

        // Correct for distance from base y axis skew
        // TODO: figure out why the heck this is required for Stella's Hydras...
        // TODO: rename this gameSetting to "hydraXRotationSkew" or other sensible name
        rawY += rawZ * MathHelper.sin(gameSettings.posTrackHydraYAxisDistanceSkewAngleDeg*PIOVER180 );
        
        //Raw is the absolute coordinate in hydra reference frame of the sample (possible average of two controllers)
        Vec3 raw = Vec3.createVectorHelper(rawX, rawY, rawZ);
        
        //Rel is the relative coordinate in hydra reference frame
        Vec3 rel = origin.subtract(raw); 
        
        //Account for hydra base station / head tracker orientation not aligned
        //After this, rel is in body coordinates (relative to head tracker reference frame, not accounting for mouse-induced world yaw offset)
        rel.rotateAroundY(baseStationYawOffset*PIOVER180);

        //Now, compute the offset from the hydra controller to the camera location. Straight from the settings (although negated -
        //gameSettings stores eye center -> hydra values for user readability. We need hydra -> eye center values here)
        float hydraXOffset = -gameSettings.getPosTrackHydraOffsetX();
        float hydraYOffset = -gameSettings.getPosTrackHydraOffsetY();
        float hydraZOffset = -gameSettings.getPosTrackHydraOffsetZ();

        if (gameSettings.posTrackHydraDebugCentreEyePos)
        {
            debugCentreEyePosition(userScale, yawHeadDegrees, pitchHeadDegrees, rollHeadDegrees, hydraXOffset, hydraYOffset, hydraZOffset,
                    cont1PosX, cont1PosY, cont1PosZ, cont2PosX, cont2PosY, cont2PosZ);
        }

        // The configured offset is for a 0,0,0 rotation head. Apply current head orientation to get final offset
        Vec3 correctionToCentreEyePosition = Vec3.createVectorHelper(hydraXOffset, hydraYOffset, hydraZOffset);

        correctionToCentreEyePosition.rotateAroundZ(rollHeadDegrees*PIOVER180);
        correctionToCentreEyePosition.rotateAroundX(pitchHeadDegrees*PIOVER180);
        correctionToCentreEyePosition.rotateAroundY(-yawHeadDegrees*PIOVER180);

        //Add the hydra position (in head tracker reference frame) to the camera offset 
        //to get the camera position in head tracker reference frame
        headPos = vecAdd(rel,correctionToCentreEyePosition);

        if (resetOrigin)
        {
        	//We compute the "ideal" neck model position, in head tracker reference frame
        	Vec3 neckModelToCentreEyePosition = Vec3.createVectorHelper(0, gameSettings.neckBaseToEyeHeight, -gameSettings.eyeProtrusion);
	        neckModelToCentreEyePosition.rotateAroundZ(rollHeadDegrees*PIOVER180);
	        neckModelToCentreEyePosition.rotateAroundX(pitchHeadDegrees*PIOVER180);
	        neckModelToCentreEyePosition.rotateAroundY(-yawHeadDegrees*PIOVER180);

	        //The actual hydra position on the head is offset from the eye center by this amount
	        Vec3 originOffset = correctionToCentreEyePosition.subtract(neckModelToCentreEyePosition);
        
	        //Counteract the base station yaw to get back to absolute razer coordinates
	        originOffset.rotateAroundY(-baseStationYawOffset*PIOVER180);

            // save raw - originOffset as origin. That way, when origin is subtracted in the future, 
	        // positions very close to the current location+orientation will have the eye in the correct spot
	        origin = originOffset.subtract(raw);
            
            resetOrigin = false;
        }

        // Rotate the centre eye position around any world yaw offset (mouse/controller induced rotation)
        headPos.rotateAroundY(-worldYawOffsetDegrees*PIOVER180);
	}

    private void debugOffsets(String name, float yawHeadDegrees, float pitchHeadDegrees, float rollHeadDegrees, float rotOffsetX, float rotOffsetY, float rotOffsetZ)
    {
        // Correct for Rift orientation
        Vec3 correctedOffsets = Vec3.createVectorHelper(rotOffsetX, rotOffsetY, rotOffsetZ);

        correctedOffsets.rotateAroundX(-pitchHeadDegrees*PIOVER180);
        correctedOffsets.rotateAroundY(yawHeadDegrees*PIOVER180);
        correctedOffsets.rotateAroundZ(-rollHeadDegrees*PIOVER180);

        float offsetX = (float)correctedOffsets.xCoord;
        float offsetY = (float)correctedOffsets.yCoord;
        float offsetZ = (float)correctedOffsets.zCoord;

        System.out.println(String.format("Positional Track: " + name + " Offset:   (l/r)x=%.3fcm, (up/down)y=%.3fcm, (in/out)z=%.3fcm", new Object[] {Float.valueOf(offsetX * 100.0f), Float.valueOf(offsetY * 100.0f), Float.valueOf(offsetZ * 100.0f)}));
    }

    @Override
	public Vec3 getCenterEyePosition() {
		return headPos;
	}

	@Override
	public void resetOrigin() {
		resetOrigin = true;
	}

	@Override
	public void resetOriginRotation() {
		resetOriginRotation = true;

        Minecraft mc = Minecraft.getMinecraft();
        if( resetOriginRotation && mc.headTracker == this )
        {
        	float prevTotalYaw = mc.lookaimController.getBodyYawDegrees() + getHeadYawDegrees();
        	yawOffset = cont1Yaw;
        	if( mc.thePlayer == null )
        		//Reset bodyYaw for main menu
        		mc.lookaimController.setBodyYawDegrees(0);
        	else
        		mc.lookaimController.setBodyYawDegrees(prevTotalYaw);
        }

	}

	@Override
	public void setPrediction(float delta, boolean enable) {
        Sixense.setFilterEnabled(enable);
	}

	@Override
	public float getHeadYawDegrees() {
		return cont1Yaw - yawOffset;
	}

	@Override
	public float getHeadPitchDegrees() {
		return cont1Pitch;
	}

	@Override
	public float getHeadRollDegrees() {
		return cont1Roll;
	}

    protected void debugCentreEyePosition(float scale, float yaw, float pitch, float roll, float hydraXOffset, float hydraYOffset, float hydraZOffset,
                                          float cont1PosX, float cont1PosY, float cont1PosZ, float cont2PosX, float cont2PosY, float cont2PosZ)
    {
        float tempHmdPosX = 0.0f;
        float tempHmdPosY = 0.0f;
        float tempHmdPosZ = 0.0f;

        System.out.println(String.format("Positional Track: Orientation         : Yaw=%.3f, Pitch=%.3f, Roll=%.3f", new Object[] {Float.valueOf(yaw), Float.valueOf(pitch), Float.valueOf(roll)}));

        // The centre eye position returned by all off these methods should agree with each other...

        //GameSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT_AND_RIGHT:
        {
            // 2 hydras are used for positional tracking, strapped to the left and right sides of the HMD respectively
            // Get average centre (eye) position.

            tempHmdPosX = ((cont1PosX + cont2PosX) / 2.0f);
            tempHmdPosY = ((cont1PosY + cont2PosY) / 2.0f);
            tempHmdPosZ = ((cont1PosZ + cont2PosZ) / 2.0f);

            System.out.println(String.format("Positional Track: Centre Eye pos (L&R): (l/r)x=%.3fcm, (up/down)y=%.3fcm, (in/out)z=%.3fcm", new Object[] {Float.valueOf(tempHmdPosX * 100.0f), Float.valueOf(tempHmdPosY * 100.0f), Float.valueOf(tempHmdPosZ * 100.0f)}));
        }

        //GameSettings.POS_TRACK_HYDRA_LOC_HMD_LEFT:

        // 1 hydra is used for positional tracking, strapped to the left side of the HMD
        // Get centre (eye) position.

        tempHmdPosX = cont1PosX;
        tempHmdPosY = cont1PosY;
        tempHmdPosZ = cont1PosZ;

        Vec3 correctionToCentreEyePosition = Vec3.createVectorHelper(hydraXOffset, hydraYOffset, hydraZOffset);

        correctionToCentreEyePosition.rotateAroundX((float)Math.toRadians(pitch));
        correctionToCentreEyePosition.rotateAroundY((float)Math.toRadians(-yaw));
        correctionToCentreEyePosition.rotateAroundZ((float)Math.toRadians(roll));

        tempHmdPosX = tempHmdPosX + (float)correctionToCentreEyePosition.xCoord;
        tempHmdPosY = tempHmdPosY + (float)correctionToCentreEyePosition.yCoord;
        tempHmdPosZ = tempHmdPosZ + (float)correctionToCentreEyePosition.zCoord;

        System.out.println(String.format("Positional Track: Centre Eye pos (L):   (l/r)x=%.3fcm, (up/down)y=%.3fcm, (in/out)z=%.3fcm", new Object[] {Float.valueOf(tempHmdPosX * 100.0f), Float.valueOf(tempHmdPosY * 100.0f), Float.valueOf(tempHmdPosZ * 100.0f)}));

        //GameSettings.POS_TRACK_HYDRA_LOC_HMD_RIGHT:

        // 1 hydra is used for positional tracking, strapped to the right side of the HMD
        // Get centre (eye) position.

        tempHmdPosX = cont2PosX;
        tempHmdPosY = cont2PosY;
        tempHmdPosZ = cont2PosZ;

        correctionToCentreEyePosition = Vec3.createVectorHelper(hydraXOffset, hydraYOffset, hydraZOffset);

        correctionToCentreEyePosition.rotateAroundX((float)Math.toRadians(pitch));
        correctionToCentreEyePosition.rotateAroundY((float)Math.toRadians(-yaw));
        correctionToCentreEyePosition.rotateAroundZ((float)Math.toRadians(roll));

        tempHmdPosX = tempHmdPosX + (float)correctionToCentreEyePosition.xCoord;
        tempHmdPosY = tempHmdPosY + (float)correctionToCentreEyePosition.yCoord;
        tempHmdPosZ = tempHmdPosZ + (float)correctionToCentreEyePosition.zCoord;

        System.out.println(String.format("Positional Track: Centre Eye pos (R):   (l/r)x=%.3fcm, (up/down)y=%.3fcm, (in/out)z=%.3fcm", new Object[] {Float.valueOf(tempHmdPosX * 100.0f), Float.valueOf(tempHmdPosY * 100.0f), Float.valueOf(tempHmdPosZ * 100.0f)}));

        //GameSettings.POS_TRACK_HYDRA_LOC_HMD_TOP:

        // 1 hydra is used for positional tracking, strapped to the top of the HMD
        // Get centre (eye) position.

        tempHmdPosX = cont2PosX;
        tempHmdPosY = cont2PosY;
        tempHmdPosZ = cont2PosZ;

        correctionToCentreEyePosition = Vec3.createVectorHelper(hydraXOffset, hydraYOffset, hydraZOffset);

        correctionToCentreEyePosition.rotateAroundX((float)Math.toRadians(pitch));
        correctionToCentreEyePosition.rotateAroundY((float)Math.toRadians(-yaw));
        correctionToCentreEyePosition.rotateAroundZ((float)Math.toRadians(roll));

        tempHmdPosX = tempHmdPosX + (float)correctionToCentreEyePosition.xCoord;
        tempHmdPosY = tempHmdPosY + (float)correctionToCentreEyePosition.yCoord;
        tempHmdPosZ = tempHmdPosZ + (float)correctionToCentreEyePosition.zCoord;

        System.out.println(String.format("Positional Track: Centre Eye pos (Top): (l/r)x=%.3fcm, (up/down)y=%.3fcm, (in/out)z=%.3fcm", new Object[] {Float.valueOf(tempHmdPosX * 100.0f), Float.valueOf(tempHmdPosY * 100.0f), Float.valueOf(tempHmdPosZ * 100.0f)}));
    }

	@Override
	public void beginAutomaticCalibration() { 
		resetOriginRotation(); //Reset the head tracker/hydra orientation reference frame
	}

	@Override
	public void updateAutomaticCalibration() {/*no-op*/ }

	@Override
	public float getBodyYawDegrees() {
		return bodyYaw;
	}

	@Override
	public void setBodyYawDegrees(float yawOffset) {
		bodyYaw = yawOffset;
	}

	@Override
	public float getBodyPitchDegrees() {
		return 0; //Always return 0 for body pitch
	}

	@Override
	public float getAimYaw() {
		return aimYaw + bodyYaw;
	}
	@Override
	public float getAimPitch() {
		return cont2Pitch;
	}

	@Override
	public boolean isCalibrated() { 
		return hydraRunning;
	}

	@Override
	public String getCalibrationStep() {
		return calibrationStep;
	}
	
	Vec3 vecAdd(Vec3 a, Vec3 b)
	{
		return a.addVector(b.xCoord, b.yCoord, b.zCoord);
	}

    @Override
    public void eventNotification(int eventId)
    {
        if (eventId == IBasePlugin.EVENT_CALIBRATION_SET_ORIGIN)
        {
            resetOrigin();
            resetOriginRotation();
        }
    }
}

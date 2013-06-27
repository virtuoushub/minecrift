/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 * 
 * Contains code from Minecraft, copyright Mojang AB
 */
package com.mtbs3d.minecrift;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import de.fruitfly.ovr.EyeRenderParams;
import de.fruitfly.ovr.HMDInfo;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;

import com.mtbs3d.minecrift.api.BasePlugin;
import com.mtbs3d.minecrift.api.IOrientationProvider;

import net.minecraft.src.*;
import paulscode.sound.SoundSystem;

import static java.lang.Math.ceil;

public class VRRenderer extends EntityRenderer
{
    // FBO stuff

    //Status & initialization
    int _previousDisplayWidth = 0;
    int _previousDisplayHeight = 0;
    public boolean _FBOInitialised = false;
    
    boolean guiYawOrientationResetRequested = true;

    // Shader Programs
    int _shaderProgramId = -1;
    int _Lanczos_shaderProgramId = -1;

    FBOParams guiFBO; //This is where the GUI is rendered; it is rendered into main world as an object
    FBOParams preDistortionFBO; //This is where the world is rendered  
    FBOParams postDistortionFBO; 
    FBOParams postSuperSampleFBO;

    // Sound system
    Field _soundManagerSndSystemField = null;

    /*
     * MC:    the minecraft world rendering code, below
     * GUI:   the guiFBO, with GUI rendered into it
	 * OUT:   graphics card output FB
	 * preD:  preDistoritonFBO
	 * postD: postDistortionFBO
	 * pSS  : postSuperSampleFBO
	 * 
     * No distortion, no supersample, output of world render is true video output FB
     * 
     *                  +----+
     *     MC -render-> |OUT |
     *           ^      +----+
     *           |
     *         +----+
     *    GUI->|GUI |
     *         +----+
     * 
     * Distortion, no supersample
     * 
     *                  +----+            +----+
     *     MC -render-> |preD| -distort-> |OUT |
     *           ^      +----+            +----+
     *           |
     *         +----+
     *    GUI->|GUI |
     *         +----+
     *     
     * Distortion, supersample
     * 
     *                  +--------+            +--------+                 +----+
	 *                  |        |            |        |                 |    |                 +----+
     *     MC -render-> |  preD  | -distort-> | postD  | -supersample1-> |pSS | -supersample2-> |OUT |
     *           ^      +--------+            +--------+                 +----+                 +----+
     *           |
     *         +----+
     *    GUI->|GUI |
     *         +----+
	 *
     * No distortion, supersample
     * 
     *                  +--------+                 +----+ 
	 *                  |        |                 |    |                 +----+
     *     MC -render-> | postD  | -supersample1-> |pSS | -supersample2-> |OUT |
     *           ^      +--------+                 +----+                 +----+
     *           |
     *         +----+
     *    GUI->|GUI |
     *         +----+
     * 
     */
    
    

    // VBO stuff, for superSampling
    // Quad variables
    private int vaoId = 0;
    private int vboId = 0;
    private int vboiId = 0;
    private int indicesCount = 0;

    GuiAchievement guiAchievement;
    EyeRenderParams eyeRenderParams;

	double renderOriginX;
	double renderOriginY;
	double renderOriginZ;

    float headYaw = 0.0F; //relative to head tracker reference frame, absolute
    float headPitch = 0.0F;
	float guiHeadYaw = 0.0f; //Not including mouse

	float camRelX;
	float camRelY;
	float camRelZ;

	float crossX;
	float crossY;
	float crossZ;
	
	float lookX; //In world coordinates
	float lookY;
	float lookZ;
	
	float aimX; //In world coordinates
	float aimY;
	float aimZ;
	
	float aimYaw;
	float aimPitch;
    
    boolean superSampleSupported;
	private boolean guiShowingLastFrame = false; //Used for detecting when UI is shown, fixing the guiYaw 

	// Calibration
	private CalibrationHelper calibrationHelper;

    public VRRenderer(Minecraft par1Minecraft, GuiAchievement guiAchiv )
    {
    	super( par1Minecraft );
    	this.guiAchievement = guiAchiv;
    	
    	try
    	{
    		GL30.glBindVertexArray(0);
    		superSampleSupported = true;
    		
    	}
    	catch( IllegalStateException e )
    	{
    		superSampleSupported = false;
    	}
    	
    	calibrationHelper = new CalibrationHelper(par1Minecraft);
    }


    /**
     * sets up projection, view effects, camera position/rotation
     */
    private void setupCameraTransform(float renderPartialTicks, int renderSceneNumber)
    {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        if (renderSceneNumber == 0)
        {
            // Left eye
            FloatBuffer leftProj = eyeRenderParams.gl_getLeftProjectionMatrix();
            GL11.glLoadMatrix(leftProj);
            mc.checkGLError("Set left projection");
        }
        else
        {
            // Right eye
            FloatBuffer rightProj = eyeRenderParams.gl_getRightProjectionMatrix();
            GL11.glLoadMatrix(rightProj);
            mc.checkGLError("Set right projection");
        }
        float var5;

        if (this.mc.playerController != null && this.mc.playerController.enableEverythingIsScrewedUpMode())
        {
            var5 = 0.6666667F;
            GL11.glScalef(1.0F, var5, 1.0F);
        }

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        //First, IPD transformation
        if (renderSceneNumber == 0)
        {
            // Left eye
            FloatBuffer leftEyeTransform = eyeRenderParams.gl_getLeftViewportTransform();
            GL11.glMultMatrix(leftEyeTransform);
        }
        else
        {
            // Right eye
            FloatBuffer rightEyeTransform = eyeRenderParams.gl_getRightViewportTransform();
            GL11.glMultMatrix(rightEyeTransform);
        }

        // Camera height offset
        float cameraYOffset = 1.62f - (this.mc.gameSettings.playerHeight - this.mc.gameSettings.neckBaseToEyeHeight);

        EntityLiving entity = this.mc.renderViewEntity;
        if( entity != null )
        {
        	//Do in-game camera adjustments if renderViewEntity exists
	        //A few game effects
	        this.hurtCameraEffect(renderPartialTicks);
	
	        if (this.mc.gameSettings.viewBobbing)
	        {
	            this.setupViewBobbing(renderPartialTicks);
	        }
	        
	        //For doing camera collision detection
	        double camX = renderOriginX + camRelX;
	        double camY = renderOriginY + camRelY - cameraYOffset;
	        double camZ = renderOriginZ + camRelZ;
	      
	        var5 = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * renderPartialTicks;
	
	        if (var5 > 0.0F)
	        {
	            byte var6 = 20;
	
	            if (this.mc.thePlayer.isPotionActive(Potion.confusion))
	            {
	                var6 = 7;
	            }
	
	            float var7 = 5.0F / (var5 * var5 + 5.0F) - var5 * 0.04F;
	            var7 *= var7;
	            GL11.glRotatef(((float)this.rendererUpdateCount + renderPartialTicks) * (float)var6, 0.0F, 1.0F, 1.0F);
	            GL11.glScalef(1.0F / var7, 1.0F, 1.0F);
	            GL11.glRotatef(-((float)this.rendererUpdateCount + renderPartialTicks) * (float)var6, 0.0F, 1.0F, 1.0F);
	        }
	
	        if (this.mc.gameSettings.thirdPersonView > 0)
	        {
	            float thirdPersonCameraDist = this.thirdPersonDistanceTemp + (this.thirdPersonDistance - this.thirdPersonDistanceTemp) * renderPartialTicks;
	            float thirdPersonYaw;
	            float thirdPersonPitch;
	
	            if (this.mc.gameSettings.debugCamEnable)
	            {
	                thirdPersonYaw = this.prevDebugCamYaw + (this.debugCamYaw - this.prevDebugCamYaw) * renderPartialTicks;
	                thirdPersonPitch = this.prevDebugCamPitch + (this.debugCamPitch - this.prevDebugCamPitch) * renderPartialTicks;
	                GL11.glTranslatef(0.0F, 0.0F, (float)(-thirdPersonCameraDist));
	                GL11.glRotatef(thirdPersonYaw, 1.0F, 0.0F, 0.0F);
	                GL11.glRotatef(thirdPersonPitch, 0.0F, 1.0F, 0.0F);
	            }
	            else
	            {
	                thirdPersonYaw = cameraYaw;
	                thirdPersonPitch = cameraPitch;
	
	                if (this.mc.gameSettings.thirdPersonView == 2)
	                {
	                    thirdPersonPitch += 180.0F;
	                }
	
	                float PIOVER180 = (float)(Math.PI/180);
	                float camXOffset = -MathHelper.sin(thirdPersonYaw    * PIOVER180) * MathHelper.cos(thirdPersonPitch * PIOVER180 ) * thirdPersonCameraDist;
	                float camZOffset =  MathHelper.cos(thirdPersonYaw    * PIOVER180) * MathHelper.cos(thirdPersonPitch * PIOVER180 ) * thirdPersonCameraDist;
	                float camYOffset = -MathHelper.sin(thirdPersonPitch  * PIOVER180) * thirdPersonCameraDist;
	                
	                //This loop offsets at [-.1, -.1, -.1], [.1,-.1,-.1], [.1,.1,-.1] etc... for all 8 directions
	                for (int var20 = 0; var20 < 8; ++var20)
	                {
	                    float var21 = (float)((var20 & 1) * 2 - 1);
	                    float var22 = (float)((var20 >> 1 & 1) * 2 - 1);
	                    float var23 = (float)((var20 >> 2 & 1) * 2 - 1);
	                    var21 *= 0.1F;
	                    var22 *= 0.1F;
	                    var23 *= 0.1F;
	                    MovingObjectPosition var24 = this.mc.theWorld.rayTraceBlocks(
	                    		this.mc.theWorld.getWorldVec3Pool().getVecFromPool(camX + var21, camY + var22, camZ + var23), 
	                    		this.mc.theWorld.getWorldVec3Pool().getVecFromPool(camX - camXOffset + var21 + var23, camY - camYOffset + var22, camZ - camZOffset + var23));
	
	                    if (var24 != null)
	                    {
	                        double var25 = var24.hitVec.distanceTo(this.mc.theWorld.getWorldVec3Pool().getVecFromPool(camX, camY, camZ));
	
	                        if (var25 < thirdPersonCameraDist)
	                        {
	                            thirdPersonCameraDist = (float)var25;
	                        }
	                    }
	                }
	
	                if (this.mc.gameSettings.thirdPersonView == 2)
	                {
	                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
	                }
	
	                GL11.glRotatef(cameraPitch - thirdPersonPitch, 1.0F, 0.0F, 0.0F);
	                GL11.glRotatef(cameraYaw - thirdPersonYaw, 0.0F, 1.0F, 0.0F);
	                GL11.glTranslatef(0.0F, 0.0F, (float)(-thirdPersonCameraDist));
	                GL11.glRotatef(thirdPersonYaw - cameraYaw, 0.0F, 1.0F, 0.0F);
	                GL11.glRotatef(thirdPersonPitch - cameraPitch, 1.0F, 0.0F, 0.0F);
	            }
	        }
        }


        if (!this.mc.gameSettings.debugCamEnable)
        {
        	//TODO: get rotation matrix instead of pitch/yaw/roll
            GL11.glRotatef(this.cameraRoll, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(this.cameraPitch, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(this.cameraYaw + 180.0F, 0.0F, 1.0F, 0.0F);
        }

        GL11.glTranslated(-camRelX, cameraYOffset-camRelY, -camRelZ);

        if (this.debugViewDirection > 0)
        {
            int var8 = this.debugViewDirection - 1;

            if (var8 == 1)
            {
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            }

            if (var8 == 2)
            {
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            }

            if (var8 == 3)
            {
                GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            }

            if (var8 == 4)
            {
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (var8 == 5)
            {
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
            }
        }
    }

    /**
    * Sets the listener of sounds
    */
    public void setSoundListenerOrientation()
    {
        SoundSystem sndSystem = null;

        // Use reflection to get the sndManager
        if (_soundManagerSndSystemField == null)
        {
	        try
	        {
	        	_soundManagerSndSystemField = SoundManager.class.getDeclaredField("sndSystem");
	        }
	        catch (NoSuchFieldException e) {
		        try
		        {
		        	_soundManagerSndSystemField = SoundManager.class.getDeclaredField("a"); //obfuscated name
		        }
		        catch (NoSuchFieldException e1) { 
		        	sndSystem = SoundManager.sndSystem;
		        };
	        }
	       	if (_soundManagerSndSystemField != null)
	       		_soundManagerSndSystemField.setAccessible(true);
        }
        
        
        if (_soundManagerSndSystemField != null && this.mc.sndManager != null)
        {
			try 
        	{
				sndSystem = (SoundSystem)_soundManagerSndSystemField.get(null);
			} 
        	catch (IllegalArgumentException e) { } 
        	catch (IllegalAccessException e) { };
        }

        if ( sndSystem != null && this.mc.gameSettings.soundVolume != 0.0F)
        {
            sndSystem.setListenerPosition((float)renderOriginX, (float)renderOriginY, (float)renderOriginZ);
	        float PIOVER180 = (float)(Math.PI/180);

	        Vec3 up = Vec3.createVectorHelper(0, 1, 0);
	        up.rotateAroundZ(-cameraRoll * PIOVER180);
	        up.rotateAroundX(-cameraPitch* PIOVER180);
	        up.rotateAroundY(-cameraYaw  * PIOVER180);

            sndSystem.setListenerOrientation(lookX, lookY, lookZ, 
            								(float)up.xCoord, (float)up.yCoord, (float)up.zCoord);
        }
    }
    
    protected void updateCamera( float renderPartialTicks, boolean displayActive )
    {
        float PIOVER180 = (float)(Math.PI/180);
        EntityLiving entity = this.mc.renderViewEntity;
        
        //runs a step of calibration
        if(calibrationHelper != null &&  calibrationHelper.allPluginsCalibrated())
        {
    		calibrationHelper = null;
        }

        if (this.mc.gameSettings.posTrackResetPosition)
        {
            mc.positionTracker.resetOrigin();
            mc.headTracker.resetOrigin();
            this.mc.gameSettings.posTrackResetPosition = false;
        }

        BasePlugin.pollAll();
        
        float lookYawOffset   = mc.lookaimController.getLookYawOffset();
        float lookPitchOffset = mc.lookaimController.getLookPitchOffset(); 
        
        aimYaw    = mc.lookaimController.getAimYaw();
        aimPitch  = mc.lookaimController.getAimPitch();
        
        if (mc.headTracker.isInitialized() && this.mc.gameSettings.useHeadTracking)
        {
            this.mc.mcProfiler.startSection("oculus");
                                                         // Roll multiplier is a one-way trip to barf-ville!
            cameraRoll = mc.headTracker.getRollDegrees_LH()  * this.mc.gameSettings.headTrackSensitivity;
            headPitch  = mc.headTracker.getPitchDegrees_LH() * this.mc.gameSettings.headTrackSensitivity;
            headYaw    = mc.headTracker.getYawDegrees_LH()   * this.mc.gameSettings.headTrackSensitivity;

            cameraPitch = (lookPitchOffset + headPitch )%180;
            cameraYaw   = (lookYawOffset   + headYaw ) % 360;
            
            // Correct for gimbal lock prevention
            if (cameraPitch > IOrientationProvider.MAXPITCH)
                cameraPitch = IOrientationProvider.MAXPITCH;
            else if (cameraPitch < -IOrientationProvider.MAXPITCH)
                cameraPitch = -IOrientationProvider.MAXPITCH;

            if (cameraRoll > IOrientationProvider.MAXROLL)
                cameraRoll = IOrientationProvider.MAXROLL;
            else if (cameraRoll < -IOrientationProvider.MAXROLL)
                cameraRoll = -IOrientationProvider.MAXROLL;

            this.mc.mcProfiler.endSection();
        }
        else
        {
        	cameraRoll = 0;
        	cameraPitch = lookPitchOffset;
        	cameraYaw = lookYawOffset;
        }
        
        if( entity != null )
        {
        	//set movement direction
        	if( this.mc.gameSettings.lookMoveDecoupled )
	        	entity.rotationYaw = lookYawOffset;
        	else
        		entity.rotationYaw = cameraYaw;
        	entity.rotationYawHead = cameraYaw;
        	entity.rotationPitch = cameraPitch;
        	
        }


        //TODO: not sure if headPitch or cameraPitch is better here... they really should be the same; silly
        //people with their "pitch affects camera" settings.
        //At any rate, using cameraPitch makes the UI look less silly
        mc.positionTracker.update(headYaw, cameraPitch, cameraRoll, lookYawOffset, 0.0f, 0.0f);

        //Do head/neck model in non-GL math so we can use camera location(between eyes)
        Vec3 cameraOffset = mc.positionTracker.getCenterEyePosition();
        cameraOffset.rotateAroundY((float)Math.PI);

        //The worldOrigin is at player "eye height" (1.62) above foot position
        camRelX = (float)cameraOffset.xCoord; camRelY = (float)cameraOffset.yCoord; camRelZ = (float)cameraOffset.zCoord;

        Vec3 look = Vec3.createVectorHelper(0, 0, 1);
        look.rotateAroundX(-cameraPitch* PIOVER180);
        look.rotateAroundY(-cameraYaw  * PIOVER180);
        lookX = (float)look.xCoord; lookY = (float)look.yCoord; lookZ = (float)look.zCoord;
        
        Vec3 aim = Vec3.createVectorHelper(0, 0, 1);
        aim.rotateAroundX(-aimPitch * PIOVER180);
    	aim.rotateAroundY(-aimYaw   * PIOVER180);
        aimX = (float)aim.xCoord; aimY = (float)aim.yCoord; aimZ = (float)aim.zCoord;
        
        if(guiYawOrientationResetRequested)
        {
        	//Hit once at startup and if reset requested (usually during calibration when an origin
            //has been set)
        	guiHeadYaw = cameraYaw;
            guiYawOrientationResetRequested = false;
        }
    } 

    protected void renderGUIandWorld(float renderPartialTicks)
    {
        this.farPlaneDistance = (float)this.mc.gameSettings.ofRenderDistanceFine;

        if (Config.isFogFancy())
        {
            this.farPlaneDistance *= 0.95F;
        }

        if (Config.isFogFast())
        {
            this.farPlaneDistance *= 0.83F;
        }

        //Setup eye render params
        if ( superSampleSupported && this.mc.gameSettings.useSupersample)
        {
            eyeRenderParams = mc.hmdInfo.getEyeRenderParams(0,
                    0,
                    (int)ceil(this.mc.displayFBWidth  * this.mc.gameSettings.superSampleScaleFactor),
                    (int)ceil(this.mc.displayFBHeight * this.mc.gameSettings.superSampleScaleFactor),
                    0.05F,
                    this.farPlaneDistance * 2.0F,
                    this.mc.gameSettings.fovScaleFactor,
                    getDistortionFitX(),
                    getDistortionFitY());
        }
        else
        {
            eyeRenderParams = mc.hmdInfo.getEyeRenderParams(0,
                    0,
                    this.mc.displayFBWidth,
                    this.mc.displayFBHeight,
                    0.05F,
                    this.farPlaneDistance * 2.0F,
                    this.mc.gameSettings.fovScaleFactor,
                    getDistortionFitX(),
                    getDistortionFitY());
        }

        //Ensure FBO are in place and initialized
        setupFBOs();

        boolean guiShowingThisFrame = false;
        int mouseX = 0;
        int mouseY = 0;
        if ( (this.mc.theWorld != null && !this.mc.gameSettings.hideGUI && this.mc.thePlayer.getSleepTimer() == 0) || this.mc.currentScreen != null )
        {

	    	//Render all UI elements into guiFBO
	        ScaledResolution var15 = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
	        int var16 = var15.getScaledWidth();
	        int var17 = var15.getScaledHeight();
	        mouseX = Mouse.getX() * var16 / this.mc.displayWidth;
	        mouseY = var17 - Mouse.getY() * var17 / this.mc.displayHeight - 1;
	
	        guiFBO.bindRenderTarget();
	
	        GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
	    	GL11.glClearColor(0, 0, 0, 0);
	    	GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );
	        GL11.glMatrixMode(GL11.GL_PROJECTION);
	        GL11.glLoadIdentity();
	        GL11.glOrtho(0.0D, var15.getScaledWidth_double(), var15.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
	        GL11.glMatrixMode(GL11.GL_MODELVIEW);
	        GL11.glLoadIdentity();
	        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
	        guiShowingThisFrame = true;
        }


        if (this.mc.theWorld != null && !this.mc.gameSettings.hideGUI)
        {
			//Disable any forge gui crosshairs and helmet overlay (pumkinblur)
			if( Reflector.ForgeGuiIngame_renderCrosshairs.exists())
			{
				Reflector.ForgeGuiIngame_renderCrosshairs.setValue(false);
				Reflector.ForgeGuiIngame_renderHelmet.setValue(false);
			}
			//Draw in game GUI
            this.mc.ingameGUI.renderGameOverlay(renderPartialTicks, this.mc.currentScreen != null, mouseX, mouseY);
            guiAchievement.updateAchievementWindow();
        }

        if( this.mc.currentScreen != null )
        {
	        try
	        {
	            this.mc.currentScreen.drawScreen(mouseX, mouseY, renderPartialTicks);
	        }
	        catch (Throwable var13)
	        {
	            CrashReport var11 = CrashReport.makeCrashReport(var13, "Rendering screen");
	            throw new ReportedException(var11);
	        }
	
	        if (this.mc.currentScreen.guiParticles != null)
	        {
	          this.mc.currentScreen.guiParticles.draw(renderPartialTicks);
	        }
	        GL11.glDisable(GL11.GL_LIGHTING); //inventory messes up fog color sometimes... This fixes
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	        drawMouseQuad( mouseX, mouseY );
        }
      
        //Setup render target
        if (mc.gameSettings.useDistortion)
        {
            preDistortionFBO.bindRenderTarget();
        }
        else if ( superSampleSupported && this.mc.gameSettings.useSupersample)
        {
            postDistortionFBO.bindRenderTarget();
            eyeRenderParams._renderScale = 1.0f;
        }
        else
        {
            unbindFBORenderTarget();
            eyeRenderParams._renderScale = 1.0f;
        }

        
        GL11.glClearColor(0, 0, 0, 1);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        if (this.mc.theWorld != null)
        {
        	//If we're in-game, render in-game stuff
            this.mc.mcProfiler.startSection("level");

	        if (this.mc.renderViewEntity == null)
	        {
	            this.mc.renderViewEntity = this.mc.thePlayer;
	        }
	
	        EntityLiving renderViewEntity = this.mc.renderViewEntity;
	        this.mc.mcProfiler.endStartSection("center");

	        //Used by fog comparison, 3rd person camera/block collision detection
	        renderOriginX = renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * (double)renderPartialTicks;
	        renderOriginY = renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * (double)renderPartialTicks;
	        renderOriginZ = renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * (double)renderPartialTicks;

	        if( this.mc.currentScreen == null )
	        {
		        this.mc.mcProfiler.endStartSection("pick");
		        getPointedBlock(renderPartialTicks);
	        }

	        // Update sound engine
	        setSoundListenerOrientation();

        }

        //Update gui Yaw
        if( guiShowingThisFrame && !guiShowingLastFrame )
        {
        	guiHeadYaw = this.cameraYaw - this.mc.lookaimController.getLookYawOffset();
        } 
        guiShowingLastFrame = guiShowingThisFrame;
        

        //Now, actually render world
        for (int renderSceneNumber = 0; renderSceneNumber < 2; ++renderSceneNumber)
        {
        	setupEyeViewport( renderSceneNumber );

	        this.mc.mcProfiler.endStartSection("camera");
	        //transform camera with pitch,yaw,roll + neck model + game effects 
	        setupCameraTransform(renderPartialTicks, renderSceneNumber);

        	if( this.mc.theWorld != null )
        	{
		        GL11.glMatrixMode(GL11.GL_MODELVIEW );
        		GL11.glPushMatrix();

        		this.renderWorld(renderPartialTicks, 0L, renderSceneNumber );

		        GL11.glMatrixMode(GL11.GL_MODELVIEW );
        		GL11.glPopMatrix();
        	}
        	else
        	{
	            GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);            // Clear Screen And Depth Buffer on the framebuffer to black
	            GL11.glDisable(GL11.GL_BLEND);
        	}
        	
        	if( guiShowingThisFrame )
        	{

        		GL11.glPushMatrix();
		        GL11.glEnable(GL11.GL_TEXTURE_2D);
		        guiFBO.bindTexture();

                float guiYaw;
                if( this.mc.theWorld != null && this.mc.gameSettings.lookMoveDecoupled)
                    guiYaw = this.mc.lookaimController.getLookYawOffset();
                else
                    guiYaw = guiHeadYaw + this.mc.lookaimController.getLookYawOffset();
                GL11.glRotatef(-guiYaw, 0f, 1f, 0f);

				if( this.mc.gameSettings.pitchInputAffectsCamera)
		        	GL11.glRotatef( this.mc.lookaimController.getLookPitchOffset(), 1f, 0f, 0f);
				GL11.glTranslatef (0.0f, 0.0f, this.mc.gameSettings.hudDistance);
				GL11.glRotatef( 180f, 0f, 1f, 0f);//Not sure why this is necessary... normals/backface culling maybe?
				if( this.mc.gameSettings.useHudOpacity )
				{
			        GL11.glEnable(GL11.GL_BLEND);
			        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				}
				else
				{
			        GL11.glDisable(GL11.GL_BLEND);
					
				}
		        GL11.glDisable(GL11.GL_DEPTH_TEST);
				drawQuad2(this.mc.displayWidth,this.mc.displayHeight,this.mc.gameSettings.hudScale*this.mc.gameSettings.hudDistance);
		        GL11.glDisable(GL11.GL_BLEND);
		        GL11.glEnable(GL11.GL_DEPTH_TEST);
		        GL11.glPopMatrix();
		
		        unbindTexture();
		        this.mc.renderEngine.resetBoundTexture();
	        	mc.checkGLError("GUI");
        	}

	    	if( calibrationHelper != null )
	    	{
		        GL11.glDisable(GL11.GL_DEPTH_TEST);
	            GL11.glPushMatrix();
	            GL11.glTranslatef(lookX*mc.gameSettings.hudDistance,lookY*mc.gameSettings.hudDistance,lookZ*mc.gameSettings.hudDistance);
	            GL11.glRotatef(-this.cameraYaw, 0.0F, 1.0F, 0.0F);
	            GL11.glRotatef(this.cameraPitch, 1.0F, 0.0F, 0.0F);
	            GL11.glRotatef(180+this.cameraRoll, 0.0F, 0.0F, 1.0F);
	            GL11.glScaled(0.02, 0.02, 0.02);
	            String calibrating = "Calibrating "+calibrationHelper.currentPlugin.getName()+"...";
	        	mc.fontRenderer.drawStringWithShadow(calibrating, -mc.fontRenderer.getStringWidth(calibrating)/2, -8, /*white*/16777215);
	        	String calibrationStep = calibrationHelper.calibrationStep;
	        	mc.fontRenderer.drawStringWithShadow(calibrationStep, -mc.fontRenderer.getStringWidth(calibrationStep)/2, 8, /*white*/16777215);
		        GL11.glPopMatrix();
		        GL11.glEnable(GL11.GL_DEPTH_TEST);
	    	}
	    }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        doDistortionAndSuperSample();
    }
    
    private void setupFBOs()
    {
        if (this.mc.displayFBWidth != _previousDisplayWidth || this.mc.displayFBHeight != _previousDisplayHeight || !_FBOInitialised)
        {
            _FBOInitialised = false;

            _previousDisplayWidth = this.mc.displayFBWidth;
            _previousDisplayHeight = this.mc.displayFBHeight;

            if( preDistortionFBO != null )
	            preDistortionFBO.delete();
            preDistortionFBO = null;

            if( guiFBO != null )
	            guiFBO.delete();
            guiFBO = null;

            if( superSampleSupported )
            {
            	if( postDistortionFBO != null )
	            	postDistortionFBO.delete();
            	postDistortionFBO = null;

            	if( postSuperSampleFBO != null )
	            	postSuperSampleFBO.delete();
            	postSuperSampleFBO = null;
	
	            destroyVBO();
            }
        }

        if (!_FBOInitialised)
        {
            System.out.println("Width: " + this.mc.displayFBWidth + ", Height: " + this.mc.displayFBHeight);
            if ( superSampleSupported && this.mc.gameSettings.useSupersample)
            {
                preDistortionFBO = new FBOParams(false, (int)ceil(this.mc.displayFBWidth * eyeRenderParams._renderScale * this.mc.gameSettings.superSampleScaleFactor), (int)ceil(this.mc.displayFBHeight * eyeRenderParams._renderScale * this.mc.gameSettings.superSampleScaleFactor));
            }
            else
            {
                preDistortionFBO = new FBOParams(false, (int)ceil(this.mc.displayFBWidth * eyeRenderParams._renderScale), (int)ceil(this.mc.displayFBHeight * eyeRenderParams._renderScale));
            }
            mc.checkGLError("FBO create");

            if (this.mc.gameSettings.useChromaticAbCorrection)
            {
                _shaderProgramId = initOculusShaders(OCULUS_BASIC_VERTEX_SHADER, OCULUS_DISTORTION_FRAGMENT_SHADER_WITH_CHROMATIC_ABERRATION_CORRECTION, false);
            }
            else
            {
                _shaderProgramId = initOculusShaders(OCULUS_BASIC_VERTEX_SHADER, OCULUS_DISTORTION_FRAGMENT_SHADER_NO_CHROMATIC_ABERRATION_CORRECTION, false);
            }
            mc.checkGLError("FBO init shader");

            // GUI FBO
            guiFBO = new FBOParams(false, this.mc.displayWidth, this.mc.displayHeight);
            
            if( superSampleSupported )
            {
	
	            if (this.mc.gameSettings.useSupersample)
	            {
		            // Lanczos downsample FBOs
		            postDistortionFBO = new FBOParams(false, (int)ceil(this.mc.displayFBWidth * this.mc.gameSettings.superSampleScaleFactor), (int)ceil(this.mc.displayFBHeight * this.mc.gameSettings.superSampleScaleFactor));
		            postSuperSampleFBO = new FBOParams(false, (int)ceil(this.mc.displayFBWidth), (int)ceil(this.mc.displayFBHeight * this.mc.gameSettings.superSampleScaleFactor));
		
		            mc.checkGLError("Lanczos FBO create");

	                _Lanczos_shaderProgramId = initOculusShaders(LANCZOS_SAMPLER_VERTEX_SHADER, LANCZOS_SAMPLER_FRAGMENT_SHADER, true);
	                mc.checkGLError("@1");
	
	
	                GL20.glValidateProgram(_Lanczos_shaderProgramId);
	
	                mc.checkGLError("FBO init Lanczos shader");
	
	                setupVBO();
	            }
	            else
	            {
	                _Lanczos_shaderProgramId = -1;
	            }
            }

            _FBOInitialised = true;
        }


    }
    
    private void setupEyeViewport( int renderSceneNumber )
    {
        this.mc.mcProfiler.endStartSection("clear");

        if (renderSceneNumber == 0)
        {
            // Left eye
            GL11.glViewport((int)ceil(eyeRenderParams._leftViewPortX * eyeRenderParams._renderScale),
                    (int)ceil(eyeRenderParams._leftViewPortY * eyeRenderParams._renderScale),
                    (int)ceil(eyeRenderParams._leftViewPortW * eyeRenderParams._renderScale),
                    (int)ceil(eyeRenderParams._leftViewPortH * eyeRenderParams._renderScale));

            GL11.glScissor((int)ceil(eyeRenderParams._leftViewPortX * eyeRenderParams._renderScale),
                    (int)ceil(eyeRenderParams._leftViewPortY * eyeRenderParams._renderScale),
                    (int)ceil(eyeRenderParams._leftViewPortW * eyeRenderParams._renderScale),
                    (int)ceil(eyeRenderParams._leftViewPortH * eyeRenderParams._renderScale));
        }
        else
        {
            // Right eye
            GL11.glViewport((int)ceil(eyeRenderParams._rightViewPortX * eyeRenderParams._renderScale),
                            (int)ceil(eyeRenderParams._rightViewPortY * eyeRenderParams._renderScale),
                            (int)ceil(eyeRenderParams._rightViewPortW * eyeRenderParams._renderScale),
                            (int)ceil(eyeRenderParams._rightViewPortH * eyeRenderParams._renderScale));

            GL11.glScissor((int)ceil(eyeRenderParams._rightViewPortX * eyeRenderParams._renderScale),
                           (int)ceil(eyeRenderParams._rightViewPortY * eyeRenderParams._renderScale),
                           (int)ceil(eyeRenderParams._rightViewPortW * eyeRenderParams._renderScale),
                           (int)ceil(eyeRenderParams._rightViewPortH * eyeRenderParams._renderScale));
        }

        mc.checkGLError("FBO viewport / scissor setup");
    }
    
    private void doDistortionAndSuperSample()
    {
    	int FBWidth = this.mc.displayFBWidth;
    	int FBHeight = this.mc.displayFBHeight;
        if ( superSampleSupported && this.mc.gameSettings.useSupersample)
        {
        	FBWidth  = (int)ceil(this.mc.displayFBWidth  * this.mc.gameSettings.superSampleScaleFactor);
        	FBHeight = (int)ceil(this.mc.displayFBHeight * this.mc.gameSettings.superSampleScaleFactor);
        	
        }
    	
        if (mc.gameSettings.useDistortion)
        {
            mc.checkGLError("Before distortion");

            preDistortionFBO.bindTexture();

            if ( superSampleSupported && this.mc.gameSettings.useSupersample)
            {
            	//chain into the superSample FBO
                postDistortionFBO.bindRenderTarget();
            }
            else
            {
	            unbindFBORenderTarget();
            }

            GL11.glClearColor (1.0f, 1.0f, 1.0f, 0.5f);
            GL11.glClearDepth(1.0D);
            GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);            // Clear Screen And Depth Buffer on the framebuffer to black

            // Render onto the entire screen framebuffer
            GL11.glViewport(0, 0, FBWidth, FBHeight);
            mc.checkGLError("3");

            // Set the distortion shader as in use
            ARBShaderObjects.glUseProgramObjectARB(_shaderProgramId);

            HMDInfo hmdInfo = mc.hmdInfo.getHMDInfo();

            float lw = eyeRenderParams._leftViewPortW  / (float)FBWidth;
            float lh = eyeRenderParams._leftViewPortH  / (float)FBHeight;
            float lx = eyeRenderParams._leftViewPortX  / (float)FBWidth;
            float ly = eyeRenderParams._leftViewPortY  / (float)FBHeight;
            float rw = eyeRenderParams._rightViewPortW / (float)FBWidth;
            float rh = eyeRenderParams._rightViewPortH / (float)FBHeight;
            float rx = eyeRenderParams._rightViewPortX / (float)FBWidth;
            float ry = eyeRenderParams._rightViewPortY / (float)FBHeight;

            float aspect = (float)eyeRenderParams._leftViewPortW / (float)eyeRenderParams._leftViewPortH;

            float leftLensCenterX = lx + (lw + eyeRenderParams._XCenterOffset * 0.5f) * 0.5f;
            float leftLensCenterY = ly + lh * 0.5f;
            float rightLensCenterX = rx + (rw + -eyeRenderParams._XCenterOffset * 0.5f) * 0.5f;
            float rightLensCenterY = ry + rh * 0.5f;

            float leftScreenCenterX = lx + lw * 0.5f;
            float leftScreenCenterY = ly + lh * 0.5f;
            float rightScreenCenterX = rx + rw * 0.5f;
            float rightScreenCenterY = ry + rh * 0.5f;

            float scaleFactor = 1.0f / eyeRenderParams._renderScale;
            float scaleX = (lw / 2) * scaleFactor;
            float scaleY = (lh / 2) * scaleFactor * aspect;
            float scaleInX = 2 / lw;
            float scaleInY = (2 / lh) / aspect;

            // Set up the fragment shader uniforms
            ARBShaderObjects.glUniform1iARB(ARBShaderObjects.glGetUniformLocationARB(_shaderProgramId, "bgl_RenderTexture"), 0);
            ARBShaderObjects.glUniform1iARB(ARBShaderObjects.glGetUniformLocationARB(_shaderProgramId, "half_screenWidth"), FBWidth/2 );
            ARBShaderObjects.glUniform2fARB(ARBShaderObjects.glGetUniformLocationARB(_shaderProgramId, "LeftLensCenter"), leftLensCenterX, leftLensCenterY);
            ARBShaderObjects.glUniform2fARB(ARBShaderObjects.glGetUniformLocationARB(_shaderProgramId, "RightLensCenter"), rightLensCenterX, rightLensCenterY);
            ARBShaderObjects.glUniform2fARB(ARBShaderObjects.glGetUniformLocationARB(_shaderProgramId, "LeftScreenCenter"), leftScreenCenterX, leftScreenCenterY);
            ARBShaderObjects.glUniform2fARB(ARBShaderObjects.glGetUniformLocationARB(_shaderProgramId, "RightScreenCenter"), rightScreenCenterX, rightScreenCenterY);
            ARBShaderObjects.glUniform2fARB(ARBShaderObjects.glGetUniformLocationARB(_shaderProgramId, "Scale"), scaleX, scaleY);
            ARBShaderObjects.glUniform2fARB(ARBShaderObjects.glGetUniformLocationARB(_shaderProgramId, "ScaleIn"), scaleInX, scaleInY);
            ARBShaderObjects.glUniform4fARB(ARBShaderObjects.glGetUniformLocationARB(_shaderProgramId, "HmdWarpParam"), hmdInfo.DistortionK[0], hmdInfo.DistortionK[1], hmdInfo.DistortionK[2], hmdInfo.DistortionK[3]);
            ARBShaderObjects.glUniform4fARB(ARBShaderObjects.glGetUniformLocationARB(_shaderProgramId, "ChromAbParam"), hmdInfo.ChromaticAb[0], hmdInfo.ChromaticAb[1], hmdInfo.ChromaticAb[2], hmdInfo.ChromaticAb[3]);

            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);

            GL11.glTranslatef (0.0f, 0.0f, -0.7f);                               // Translate 6 Units Into The Screen and then rotate
            GL11.glColor3f(1, 1, 1);                                               // set the color to white

            drawQuad();                                                      // draw the box

            // Stop shader use
            ARBShaderObjects.glUseProgramObjectARB(0);

            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
            GL11.glPopAttrib();

            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);

            mc.checkGLError("After distortion");
        }

        if (superSampleSupported && this.mc.gameSettings.useSupersample)
        {
            // Now switch to 1st pass target framebuffer
        	postSuperSampleFBO.bindRenderTarget();

            // Bind the texture
        	postDistortionFBO.bindTexture();


            GL11.glClearColor (0.0f, 0.0f, 1.0f, 0.5f);
            GL11.glClearDepth(1.0D);
            GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);            // Clear Screen And Depth Buffer on the framebuffer to black

            // Render onto the entire screen framebuffer
            GL11.glViewport(0, 0, this.mc.displayFBWidth, FBHeight);
            mc.checkGLError("3");


            // Set the downsampling shader as in use
            ARBShaderObjects.glUseProgramObjectARB(_Lanczos_shaderProgramId);
            this.mc.checkGLError("UseLanczos");

            // Set up the fragment shader uniforms
            this.mc.checkGLError("lanzosLoc");
            ARBShaderObjects.glUniform1fARB(ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "texelWidthOffset"), 1.0f / (3.0f * (float)this.mc.displayFBWidth));
            this.mc.checkGLError("lanzosUni1");
            ARBShaderObjects.glUniform1fARB(ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "texelHeightOffset"), 0.0f);
            this.mc.checkGLError("lanzosUni2");
            ARBShaderObjects.glUniform1iARB(ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "inputImageTexture"), 0);

            // Pass 1

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            // Bind to the VAO that has all the information about the vertices
            GL30.glBindVertexArray(vaoId);
            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glEnableVertexAttribArray(2);

            // Bind to the index VBO that has all the information about the order of the vertices
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);

            // Draw the vertices
            GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_BYTE, 0);

            // Put everything back to default (deselect)
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
            GL20.glDisableVertexAttribArray(0);
            GL20.glDisableVertexAttribArray(1);
            GL20.glDisableVertexAttribArray(2);
            GL30.glBindVertexArray(0);

            // Pass 2
            // Now switch to 2nd pass screen framebuffer
            unbindFBORenderTarget();
            postSuperSampleFBO.bindTexture();

            GL11.glViewport(0, 0, this.mc.displayFBWidth, this.mc.displayFBHeight);
            GL11.glClearColor (0.0f, 0.0f, 1.0f, 0.5f);
            GL11.glClearDepth(1.0D);
            GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // Bind the texture
            GL13.glActiveTexture(GL13.GL_TEXTURE0);

            // Set up the fragment shader uniforms for pass 2
            this.mc.checkGLError("lanzosLoc");
            ARBShaderObjects.glUniform1fARB(ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "texelWidthOffset"), 0.0f);
            this.mc.checkGLError("lanzosUni1");
            ARBShaderObjects.glUniform1fARB(ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "texelHeightOffset"), 1.0f / (3.0f * (float)this.mc.displayFBHeight));
            this.mc.checkGLError("lanzosUni2");
            ARBShaderObjects.glUniform1iARB(ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "inputImageTexture"), 0);

            // Bind to the VAO that has all the information about the vertices
            GL30.glBindVertexArray(vaoId);
            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glEnableVertexAttribArray(2);

            // Bind to the index VBO that has all the information about the order of the vertices
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);

            // Draw the vertices
            GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_BYTE, 0);

            // Put everything back to default (deselect)
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
            GL20.glDisableVertexAttribArray(0);
            GL20.glDisableVertexAttribArray(1);
            GL20.glDisableVertexAttribArray(2);
            GL30.glBindVertexArray(0);

            // Stop shader use
            ARBShaderObjects.glUseProgramObjectARB(0);
            this.mc.checkGLError("loopCycle");
        }
    }
    
    public void renderWorld(float renderPartialTicks, long nextFrameTime, int renderSceneNumber )
    {
        RenderGlobal renderGlobal = this.mc.renderGlobal;
        EffectRenderer effectRenderer = this.mc.effectRenderer;
        EntityLiving renderViewEntity = this.mc.renderViewEntity;

        //TODO: fog color isn't quite right yet when eyes split water/air
        this.updateFogColor(renderPartialTicks);
        GL11.glClearColor (fogColorRed, fogColorGreen, fogColorBlue, 0.5f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        mc.checkGLError("FBO init");

        this.mc.mcProfiler.startSection("lightTex");
        if (this.lightmapUpdateNeeded)
        {
            this.updateLightmap(renderPartialTicks);
        }
        
        ActiveRenderInfo.updateRenderInfo(this.mc.thePlayer, this.mc.gameSettings.thirdPersonView == 2);
        this.mc.mcProfiler.endStartSection("frustrum");
        ClippingHelperImpl.getInstance(); // setup clip, using current modelview / projection matrices

        if (!Config.isSkyEnabled() && !Config.isSunMoonEnabled() && !Config.isStarsEnabled())
        {
            GL11.glDisable(GL11.GL_BLEND);
        }
        else
        {
            this.setupFog(-1, renderPartialTicks);
            this.mc.mcProfiler.endStartSection("sky");
            renderGlobal.renderSky(renderPartialTicks);
        }

        GL11.glEnable(GL11.GL_FOG);
        this.setupFog(1, renderPartialTicks);

        if (this.mc.gameSettings.ambientOcclusion != 0)
        {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        }

        this.mc.mcProfiler.endStartSection("culling");
        Frustrum frustrum = new Frustrum();
        frustrum.setPosition(renderOriginX , renderOriginY, renderOriginZ);


        this.mc.renderGlobal.clipRenderersByFrustum(frustrum, renderPartialTicks);

        if (renderSceneNumber == 0 )
        {
            this.mc.mcProfiler.endStartSection("updatechunks");

            while (!this.mc.renderGlobal.updateRenderers(renderViewEntity, false) && nextFrameTime != 0L)
            {
                long var15 = nextFrameTime - System.nanoTime();

                if (var15 < 0L || var15 > 1000000000L)
                {
                    break;
                }
            }
        }

        if (renderViewEntity.posY < 128.0D)
        {
            this.renderCloudsCheck(renderGlobal, renderPartialTicks);
        }

        this.mc.mcProfiler.endStartSection("prepareterrain");
        this.setupFog(0, renderPartialTicks);
        GL11.glEnable(GL11.GL_FOG);
        this.mc.renderEngine.bindTexture("/terrain.png");
        RenderHelper.disableStandardItemLighting();
        this.mc.mcProfiler.endStartSection("terrain");
        renderGlobal.sortAndRender(renderViewEntity, 0, (double) renderPartialTicks);
        GL11.glShadeModel(GL11.GL_FLAT);
        boolean var16 = Reflector.ForgeHooksClient.exists();
        EntityPlayer var18;

        if (this.debugViewDirection == 0)
        {
            RenderHelper.enableStandardItemLighting();
            this.mc.mcProfiler.endStartSection("entities");

            if (var16)
            {
                Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, new Object[] {Integer.valueOf(0)});
            }

            //TODO: multiple render passes for entities?
            renderGlobal.renderEntities(renderViewEntity.getPosition(renderPartialTicks), frustrum, renderPartialTicks);

            if (var16)
            {
                Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, new Object[] {Integer.valueOf(-1)});
            }

            this.enableLightmap((double) renderPartialTicks);
            this.mc.mcProfiler.endStartSection("litParticles");
            effectRenderer.renderLitParticles(renderViewEntity, renderPartialTicks);
            RenderHelper.disableStandardItemLighting();
            this.setupFog(0, renderPartialTicks);
            this.mc.mcProfiler.endStartSection("particles");
            effectRenderer.renderParticles(renderViewEntity, renderPartialTicks);
            this.disableLightmap((double) renderPartialTicks);
                                                                                                                                                    // TODO: Always render outline?
            if (this.mc.objectMouseOver != null && renderViewEntity.isInsideOfMaterial(Material.water) && renderViewEntity instanceof EntityPlayer && !this.mc.gameSettings.hideGUI)
            {
                var18 = (EntityPlayer)renderViewEntity;
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                this.mc.mcProfiler.endStartSection("outline");

                if (!var16 || !Reflector.callBoolean(Reflector.ForgeHooksClient_onDrawBlockHighlight, new Object[] {renderGlobal, var18, this.mc.objectMouseOver, Integer.valueOf(0), var18.inventory.getCurrentItem(), Float.valueOf(renderPartialTicks)}))
                {
                    renderGlobal.drawBlockBreaking(var18, this.mc.objectMouseOver, 0, var18.inventory.getCurrentItem(), renderPartialTicks);
                    if (!this.mc.gameSettings.hideGUI)
                    {
                        renderGlobal.drawSelectionBox(var18, this.mc.objectMouseOver, 0, var18.inventory.getCurrentItem(), renderPartialTicks);
                    }
                }
                GL11.glEnable(GL11.GL_ALPHA_TEST);
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(true);
        this.setupFog(0, renderPartialTicks);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);

        this.mc.renderEngine.bindTexture("/terrain.png");
        WrUpdates.resumeBackgroundUpdates();

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (Config.isWaterFancy())
        {
            this.mc.mcProfiler.endStartSection("water");

            if (this.mc.gameSettings.ambientOcclusion != 0)
            {
                GL11.glShadeModel(GL11.GL_SMOOTH);
            }

            int var17 = renderGlobal.renderAllSortedRenderers(1, (double)renderPartialTicks);

            if (var17 > 0)
            {
                renderGlobal.renderAllSortedRenderers(1, (double)renderPartialTicks);
            }

            GL11.glShadeModel(GL11.GL_FLAT);
        }
        else
        {
            this.mc.mcProfiler.endStartSection("water");
            renderGlobal.renderAllSortedRenderers( 1, (double) renderPartialTicks);
        }

        WrUpdates.pauseBackgroundUpdates();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
                                                                                    // TODO: Always render selection boxes?
        if (this.cameraZoom == 1.0D && renderViewEntity instanceof EntityPlayer && this.mc.objectMouseOver != null && !renderViewEntity.isInsideOfMaterial(Material.water))
        {
            var18 = (EntityPlayer)renderViewEntity;
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            this.mc.mcProfiler.endStartSection("outline");

            if (!var16 || !Reflector.callBoolean(Reflector.ForgeHooksClient_onDrawBlockHighlight, new Object[] {renderGlobal, var18, this.mc.objectMouseOver, Integer.valueOf(0), var18.inventory.getCurrentItem(), Float.valueOf(renderPartialTicks)}))
            {
                renderGlobal.drawBlockBreaking(var18, this.mc.objectMouseOver, 0, var18.inventory.getCurrentItem(), renderPartialTicks );

                renderGlobal.drawSelectionBox(var18, this.mc.objectMouseOver, 0, var18.inventory.getCurrentItem(), renderPartialTicks );
            }
            GL11.glEnable(GL11.GL_ALPHA_TEST);
        }

        this.mc.mcProfiler.endStartSection("destroyProgress");
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        renderGlobal.drawBlockDamageTexture(Tessellator.instance, renderViewEntity, renderPartialTicks);
        GL11.glDisable(GL11.GL_BLEND);
        this.mc.mcProfiler.endStartSection("weather");
        this.renderRainSnow(renderPartialTicks);


        GL11.glDisable(GL11.GL_FOG);

        if (renderViewEntity.posY >= 128.0D)
        {
            this.renderCloudsCheck(renderGlobal, renderPartialTicks);
        }

        if (var16)
        {
	        mc.checkGLError("PreFRenderLast");
            this.mc.mcProfiler.endStartSection("FRenderLast");
            Reflector.callVoid(Reflector.ForgeHooksClient_dispatchRenderLast, new Object[] {renderGlobal, Float.valueOf(renderPartialTicks)});
	        mc.checkGLError("PostFRenderLast");
        }

	    GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f); //white crosshair, with blending
    	//Draw crosshair
    	if( this.mc.gameSettings.thirdPersonView == 0 )
    	{
    		this.mc.mcProfiler.endStartSection("crosshair");
            float crossDepth = (float)Math.sqrt((crossX*crossX + crossY*crossY + crossZ*crossZ));
            float scale = 0.025f*crossDepth;

            GL11.glPushMatrix();
        	GL11.glTranslatef(crossX, crossY, crossZ);
            GL11.glRotatef(-this.aimYaw, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(this.aimPitch, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(this.cameraRoll, 0.0F, 0.0F, 1.0F);
            GL11.glScalef(-scale, -scale, scale);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);

	        this.mc.renderEngine.bindTexture("/gui/icons.png");

	        float var7 = 0.00390625F;
	        float var8 = 0.00390625F;
	        Tessellator.instance.startDrawingQuads();
	        Tessellator.instance.addVertexWithUV(- 1, + 1, 0,  0     , 16* var8);
	        Tessellator.instance.addVertexWithUV(+ 1, + 1, 0, 16*var7, 16* var8);
	        Tessellator.instance.addVertexWithUV(+ 1, - 1, 0, 16*var7, 0       );
	        Tessellator.instance.addVertexWithUV(- 1, - 1, 0, 0      , 0       );
	        Tessellator.instance.draw();
	        GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
	        GL11.glPopMatrix();
	        mc.checkGLError("crosshair");
        }
        
        this.mc.mcProfiler.endSection();
    }

    private void destroyVBO()
    {
        // Select the VAO
        GL30.glBindVertexArray(vaoId);

        // Disable the VBO index from the VAO attributes list
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);

        // Delete the vertex VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboId);
        vboId = 0;

        // Delete the index VBO
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboiId);
        vboiId = 0;

        // Delete the VAO
        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoId);
        vaoId = 0;

        this.mc.checkGLError("destroyVBO");
    }

    private void setupVBO()
    {
        // We'll define our quad using 4 vertices of the custom 'TexturedVertex' class
        TexturedVertex v0 = new TexturedVertex();
        v0.setXYZ(-1.0f, 1.0f, 0); v0.setRGB(1, 0, 0); v0.setST(0, 1);
        TexturedVertex v1 = new TexturedVertex();
        v1.setXYZ(-1.0f, -1.0f, 0); v1.setRGB(0, 1, 0); v1.setST(0, 0);
        TexturedVertex v2 = new TexturedVertex();
        v2.setXYZ(1.0f, -1.0f, 0); v2.setRGB(0, 0, 1); v2.setST(1, 0);
        TexturedVertex v3 = new TexturedVertex();
        v3.setXYZ(1.0f, 1.0f, 0); v3.setRGB(1, 1, 1); v3.setST(1, 1);

        TexturedVertex[] vertices = new TexturedVertex[] {v0, v1, v2, v3};
        // Put each 'Vertex' in one FloatBuffer
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length *
                TexturedVertex.elementCount);
        for (int i = 0; i < vertices.length; i++) {
            // Add position, color and texture floats to the buffer
            verticesBuffer.put(vertices[i].getElements());
        }
        verticesBuffer.flip();
        // OpenGL expects to draw vertices in counter clockwise order by default
        byte[] indices = {
                0, 1, 2,
                2, 3, 0
        };
        indicesCount = indices.length;
        ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indicesCount);
        indicesBuffer.put(indices);
        indicesBuffer.flip();

        // Create a new Vertex Array Object in memory and select it (bind)
        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // Create a new Vertex Buffer Object in memory and select it (bind)
        vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);

        // Put the position coordinates in attribute list 0
        GL20.glVertexAttribPointer(0, TexturedVertex.positionElementCount, GL11.GL_FLOAT,
                false, TexturedVertex.stride, TexturedVertex.positionByteOffset);
        // Put the color components in attribute list 1
        GL20.glVertexAttribPointer(1, TexturedVertex.colorElementCount, GL11.GL_FLOAT,
                false, TexturedVertex.stride, TexturedVertex.colorByteOffset);
        // Put the texture coordinates in attribute list 2
        GL20.glVertexAttribPointer(2, TexturedVertex.textureElementCount, GL11.GL_FLOAT,
                false, TexturedVertex.stride, TexturedVertex.textureByteOffset);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        // Deselect (bind to 0) the VAO
        GL30.glBindVertexArray(0);

        // Create a new VBO for the indices and select it (bind) - INDICES
        vboiId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        this.mc.checkGLError("setupQuad&VBO");
    }
    
    private void unbindFBORenderTarget()
    {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0 );
    }
    
    private void unbindTexture()
    {
    	GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public void drawQuad()
    {
    	// this func just draws a perfectly normal box with some texture coordinates
        GL11.glBegin(GL11.GL_QUADS);
        
        // Front Face
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-1.0f, -1.0f,  0.0f);  // Bottom Left Of The Texture and Quad
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( 1.0f, -1.0f,  0.0f);  // Bottom Right Of The Texture and Quad
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( 1.0f,  1.0f,  0.0f);  // Top Right Of The Texture and Quad
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-1.0f,  1.0f,  0.0f);  // Top Left Of The Texture and Quad

        GL11.glEnd();
    }

    public void drawQuad2(float displayWidth, float displayHeight, float scale)
    {
        float aspect = displayHeight / displayWidth;

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-1.0f * scale, -1.0f * aspect * scale, 0.0f);  // Bottom Left  Of The Texture and Quad
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( 1.0f * scale, -1.0f * aspect * scale, 0.0f);  // Bottom Right Of The Texture and Quad
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( 1.0f * scale,  1.0f * aspect * scale, 0.0f);  // Top    Right Of The Texture and Quad
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-1.0f * scale,  1.0f * aspect * scale, 0.0f);  // Top    Left  Of The Texture and Quad

        GL11.glEnd();
    }

    public int initOculusShaders(String vertexShaderGLSL, String fragmentShaderGLSL, boolean doAttribs)
    {
        int vertShader = 0, pixelShader = 0;
        int program = 0;

        try {
            vertShader = createShader(vertexShaderGLSL, ARBVertexShader.GL_VERTEX_SHADER_ARB);
            pixelShader = createShader(fragmentShaderGLSL, ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
        }
        catch(Exception exc) {
            exc.printStackTrace();
            return 0;
        }
        finally {
            if(vertShader == 0 || pixelShader == 0)
                return 0;
        }

        program = ARBShaderObjects.glCreateProgramObjectARB();
        if(program == 0)
            return 0;

        /*
        * if the fragment shaders setup sucessfully,
        * attach them to the shader program, link the shader program
        * into the GL context and validate
        */
        ARBShaderObjects.glAttachObjectARB(program, vertShader);
        ARBShaderObjects.glAttachObjectARB(program, pixelShader);

        if (doAttribs)
        {
            // Position information will be attribute 0
            GL20.glBindAttribLocation(program, 0, "in_Position");
            mc.checkGLError("@2");
            // Color information will be attribute 1
            GL20.glBindAttribLocation(program, 1, "in_Color");
            mc.checkGLError("@2a");
            // Texture information will be attribute 2
            GL20.glBindAttribLocation(program, 2, "in_TextureCoord");
            mc.checkGLError("@3");
        }

        ARBShaderObjects.glLinkProgramARB(program);
        mc.checkGLError("Link");

        if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
            System.out.println(getLogInfo(program));
            return 0;
        }

        ARBShaderObjects.glValidateProgramARB(program);
        if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
            System.out.println(getLogInfo(program));
            return 0;
        }

        return program;
    }

    public final String OCULUS_BASIC_VERTEX_SHADER =

        "#version 110\n" +
        "\n" +
        "void main() {\n" +
        "    gl_Position = ftransform(); //Transform the vertex position\n" +
        "    gl_TexCoord[0] = gl_MultiTexCoord0; // Use Texture unit 0\n" +
        "    //glTexCoord is an openGL defined varying array of vec4. Different elements in the array can be used for multi-texturing with\n" +
        "    //different textures, each requiring their own coordinates.\n" +
        "    //gl_MultiTexCoord0 is an openGl defined attribute vec4 containing the texture coordinates for unit 0 (I'll explain units soon) that\n" +
        "    //you give with calls to glTexCoord2f, glTexCoordPointer etc. gl_MultiTexCoord1 contains unit 1, gl_MultiTexCoord2  unit 2 etc.\n" +
        "}\n";

    public final String OCULUS_DISTORTION_FRAGMENT_SHADER_NO_CHROMATIC_ABERRATION_CORRECTION =

        "#version 120\n" +
        "\n" +
        "uniform sampler2D bgl_RenderTexture;\n" +
        "uniform int half_screenWidth;\n" +
        "uniform vec2 LeftLensCenter;\n" +
        "uniform vec2 RightLensCenter;\n" +
        "uniform vec2 LeftScreenCenter;\n" +
        "uniform vec2 RightScreenCenter;\n" +
        "uniform vec2 Scale;\n" +
        "uniform vec2 ScaleIn;\n" +
        "uniform vec4 HmdWarpParam;\n" +
        "uniform vec4 ChromAbParam;\n" +
        "\n" +
        "// Scales input texture coordinates for distortion.\n" +
        "vec2 HmdWarp(vec2 in01, vec2 LensCenter)\n" +
        "{\n" +
        "    vec2 theta = (in01 - LensCenter) * ScaleIn; // Scales to [-1, 1]\n" +
        "    float rSq = theta.x * theta.x + theta.y * theta.y;\n" +
        "    vec2 rvector = theta * (HmdWarpParam.x + HmdWarpParam.y * rSq +\n" +
        "            HmdWarpParam.z * rSq * rSq +\n" +
        "            HmdWarpParam.w * rSq * rSq * rSq);\n" +
        "    return LensCenter + Scale * rvector;\n" +
        "}\n" +
        "\n" +
        "void main()\n" +
        "{\n" +
        "    // The following two variables need to be set per eye\n" +
        "    vec2 LensCenter = gl_FragCoord.x < half_screenWidth ? LeftLensCenter : RightLensCenter;\n" +
        "    vec2 ScreenCenter = gl_FragCoord.x < half_screenWidth ? LeftScreenCenter : RightScreenCenter;\n" +
        "\n" +
        "    vec2 oTexCoord = gl_TexCoord[0].xy;\n" +
        "    //vec2 oTexCoord = (gl_FragCoord.xy + vec2(0.5, 0.5)) / vec2(screenWidth, screenHeight);\n" +
        "\n" +
        "    vec2 tc = HmdWarp(oTexCoord, LensCenter);\n" +
        "    if (any(bvec2(clamp(tc,ScreenCenter-vec2(0.25,0.5), ScreenCenter+vec2(0.25,0.5)) - tc)))\n" +
        "    {\n" +
        "        gl_FragColor = vec4(vec3(0.0), 1.0);\n" +
        "        return;\n" +
        "    }\n" +
        "\n" +
        "    //tc.x = gl_FragCoord.x < half_screenWidth ? (2.0 * tc.x) : (2.0 * (tc.x - 0.5));\n" +
        "    //gl_FragColor = texture2D(bgl_RenderTexture, tc).aaaa * texture2D(bgl_RenderTexture, tc);\n" +
        "    gl_FragColor = texture2D(bgl_RenderTexture, tc);\n" +
        "}\n";

    public final String OCULUS_DISTORTION_FRAGMENT_SHADER_WITH_CHROMATIC_ABERRATION_CORRECTION =

        "#version 120\n" +
        "\n" +
        "uniform sampler2D bgl_RenderTexture;\n" +
        "uniform int half_screenWidth;\n" +
        "uniform vec2 LeftLensCenter;\n" +
        "uniform vec2 RightLensCenter;\n" +
        "uniform vec2 LeftScreenCenter;\n" +
        "uniform vec2 RightScreenCenter;\n" +
        "uniform vec2 Scale;\n" +
        "uniform vec2 ScaleIn;\n" +
        "uniform vec4 HmdWarpParam;\n" +
        "uniform vec4 ChromAbParam;\n" +
        "\n" +
        "void main()\n" +
        "{\n" +
        "    vec2 LensCenter = gl_FragCoord.x < half_screenWidth ? LeftLensCenter : RightLensCenter;\n" +
        "    vec2 ScreenCenter = gl_FragCoord.x < half_screenWidth ? LeftScreenCenter : RightScreenCenter;\n" +
        "\n" +
        "    vec2 theta = (gl_TexCoord[0].xy - LensCenter) * ScaleIn;\n" +
        "    float rSq = theta.x * theta.x + theta.y * theta.y;\n" +
        "    vec2 theta1 = theta * (HmdWarpParam.x + HmdWarpParam.y * rSq + HmdWarpParam.z * rSq * rSq + HmdWarpParam.w * rSq * rSq * rSq);\n" +
        "\n" +
        "    vec2 thetaBlue = theta1 * (ChromAbParam.w * rSq + ChromAbParam.z);\n" +
        "    vec2 tcBlue = thetaBlue * Scale + LensCenter;\n" +
        "\n" +
        "    if (any(bvec2(clamp(tcBlue, ScreenCenter-vec2(0.25,0.5), ScreenCenter+vec2(0.25,0.5)) - tcBlue))) {\n" +
        "        gl_FragColor = vec4(vec3(0.0), 1.0);\n" +
        "        return;\n" +
        "    }\n" +
        "    float blue = texture2D(bgl_RenderTexture, tcBlue).b;\n" +
        "\n" +
        "    vec2 tcGreen = theta1 * Scale + LensCenter;\n" +
        "    float green = texture2D(bgl_RenderTexture, tcGreen).g;\n" +
        "\n" +
        "    vec2 thetaRed = theta1 * (ChromAbParam.y * rSq + ChromAbParam.x);\n" +
        "    vec2 tcRed = thetaRed * Scale + LensCenter;\n" +
        "    float red = texture2D(bgl_RenderTexture, tcRed).r;\n" +
        "\n" +
        "    gl_FragColor = vec4(red, green, blue, 1.0);\n" +
        "}\n";

    public final String OCULUS_BASIC_FRAGMENT_SHADER =

        "#version 120\n" +
        "\n" +
        "uniform sampler2D bgl_RenderTexture;\n" +
        "\n" +
        "void main() {\n" +
        "    vec4 color = texture2D(bgl_RenderTexture, gl_TexCoord[0].st);\n" +
        "    gl_FragColor = color;\n" +
        "}\n";

    public final String LANCZOS_SAMPLER_VERTEX_SHADER =
        "#version 120\n" +
        "\n" +
        " attribute vec4 in_Position;//position;\n" +
        " attribute vec4 in_Color;//position;\n" +
        " attribute vec2 in_TextureCoord;//inputTextureCoordinate;\n" +
        "\n" +
        " uniform float texelWidthOffset;\n" +
        " uniform float texelHeightOffset;\n" +
        "\n" +
        " varying vec2 centerTextureCoordinate;\n" +
        " varying vec2 oneStepLeftTextureCoordinate;\n" +
        " varying vec2 twoStepsLeftTextureCoordinate;\n" +
        " varying vec2 threeStepsLeftTextureCoordinate;\n" +
        " varying vec2 fourStepsLeftTextureCoordinate;\n" +
        " varying vec2 oneStepRightTextureCoordinate;\n" +
        " varying vec2 twoStepsRightTextureCoordinate;\n" +
        " varying vec2 threeStepsRightTextureCoordinate;\n" +
        " varying vec2 fourStepsRightTextureCoordinate;\n" +
        "\n" +
        " void main()\n" +
        " {\n" +
        "     gl_Position = in_Position;//position;\n" +
        "\n" +
        "     vec2 firstOffset = vec2(texelWidthOffset, texelHeightOffset);\n" +
        "     vec2 secondOffset = vec2(2.0 * texelWidthOffset, 2.0 * texelHeightOffset);\n" +
        "     vec2 thirdOffset = vec2(3.0 * texelWidthOffset, 3.0 * texelHeightOffset);\n" +
        "     vec2 fourthOffset = vec2(4.0 * texelWidthOffset, 4.0 * texelHeightOffset);\n" +
        "\n" +
        "     centerTextureCoordinate = in_TextureCoord;//inputTextureCoordinate;\n" +
        "     oneStepLeftTextureCoordinate = in_TextureCoord - firstOffset;\n" +
        "     twoStepsLeftTextureCoordinate = in_TextureCoord - secondOffset;\n" +
        "     threeStepsLeftTextureCoordinate = in_TextureCoord - thirdOffset;\n" +
        "     fourStepsLeftTextureCoordinate = in_TextureCoord - fourthOffset;\n" +
        "     oneStepRightTextureCoordinate = in_TextureCoord + firstOffset;\n" +
        "     twoStepsRightTextureCoordinate = in_TextureCoord + secondOffset;\n" +
        "     threeStepsRightTextureCoordinate = in_TextureCoord + thirdOffset;\n" +
        "     fourStepsRightTextureCoordinate = in_TextureCoord + fourthOffset;\n" +
        " }\n";

    public final String LANCZOS_SAMPLER_FRAGMENT_SHADER =

        "#version 120\n" +
        "\n" +
        " uniform sampler2D inputImageTexture;\n" +
        "\n" +
        " varying vec2 centerTextureCoordinate;\n" +
        " varying vec2 oneStepLeftTextureCoordinate;\n" +
        " varying vec2 twoStepsLeftTextureCoordinate;\n" +
        " varying vec2 threeStepsLeftTextureCoordinate;\n" +
        " varying vec2 fourStepsLeftTextureCoordinate;\n" +
        " varying vec2 oneStepRightTextureCoordinate;\n" +
        " varying vec2 twoStepsRightTextureCoordinate;\n" +
        " varying vec2 threeStepsRightTextureCoordinate;\n" +
        " varying vec2 fourStepsRightTextureCoordinate;\n" +
        "\n" +
        " // sinc(x) * sinc(x/a) = (a * sin(pi * x) * sin(pi * x / a)) / (pi^2 * x^2)\n" +
        " // Assuming a Lanczos constant of 2.0, and scaling values to max out at x = +/- 1.5\n" +
        "\n" +
        " void main()\n" +
        " {\n" +
        "     vec4 fragmentColor = texture2D(inputImageTexture, centerTextureCoordinate) * 0.38026;\n" +
        "\n" +
        "     fragmentColor += texture2D(inputImageTexture, oneStepLeftTextureCoordinate) * 0.27667;\n" +
        "     fragmentColor += texture2D(inputImageTexture, oneStepRightTextureCoordinate) * 0.27667;\n" +
        "\n" +
        "     fragmentColor += texture2D(inputImageTexture, twoStepsLeftTextureCoordinate) * 0.08074;\n" +
        "     fragmentColor += texture2D(inputImageTexture, twoStepsRightTextureCoordinate) * 0.08074;\n" +
        "\n" +
        "     fragmentColor += texture2D(inputImageTexture, threeStepsLeftTextureCoordinate) * -0.02612;\n" +
        "     fragmentColor += texture2D(inputImageTexture, threeStepsRightTextureCoordinate) * -0.02612;\n" +
        "\n" +
        "     fragmentColor += texture2D(inputImageTexture, fourStepsLeftTextureCoordinate) * -0.02143;\n" +
        "     fragmentColor += texture2D(inputImageTexture, fourStepsRightTextureCoordinate) * -0.02143;\n" +
        "\n" +
        "     gl_FragColor = fragmentColor;\n" +
        " }\n";

    public final String LANCZOS_SAMPLER_FRAGMENT_SHADER2 =
        "#version 120\n" +
        "\n" +
        "uniform sampler2D rubyTexture;\n" +
        "uniform vec2 rubyTextureSize;\n" +
        "\n" +
        "const float PI = 3.1415926535897932384626433832795;\n" +
        "\n" +
        "vec3 weight3(float x)\n" +
        "{\n" +
        "    const float radius = 3.0;\n" +
        "    vec3 sample = FIX(PI * vec3(\n" +
        "        x * 2.0 + 0.0 * 2.0 - 3.0,\n" +
        "        x * 2.0 + 1.0 * 2.0 - 3.0,\n" +
        "        x * 2.0 + 2.0 * 2.0 - 3.0));\n" +
        "\n" +
        "    // Lanczos3\n" +
        "    vec3 ret = 2.0 * sin(sample) * sin(sample / radius) / pow(sample, 2.0);\n" +
        "\n" +
        "    // Normalize\n" +
        "    return ret;\n" +
        "}\n" +
        "\n" +
        "vec3 pixel(float xpos, float ypos)\n" +
        "{\n" +
        "    return texture2D(rubyTexture, vec2(xpos, ypos)).rgb;\n" +
        "}\n" +
        "\n" +
        "vec3 line(float ypos, vec3 xpos1, vec3 xpos2, vec3 linetaps1, vec3 linetaps2)\n" +
        "{\n" +
        "    return\n" +
        "        pixel(xpos1.r, ypos) * linetaps1.r +\n" +
        "        pixel(xpos1.g, ypos) * linetaps2.r +\n" +
        "        pixel(xpos1.b, ypos) * linetaps1.g +\n" +
        "        pixel(xpos2.r, ypos) * linetaps2.g +\n" +
        "        pixel(xpos2.g, ypos) * linetaps1.b +\n" +
        "        pixel(xpos2.b, ypos) * linetaps2.b;\n" +
        "}\n" +
        "\n" +
        "void main()\n" +
        "{\n" +
        "    vec2 stepxy = 1.0 / rubyTextureSize.xy;\n" +
        "    vec2 pos = gl_TexCoord[0].xy + stepxy * 0.5;\n" +
        "    vec2 f = fract(pos / stepxy);\n" +
        "\n" +
        "    vec3 linetaps1   = weight3((1.0 - f.x) / 2.0);\n" +
        "    vec3 linetaps2   = weight3((1.0 - f.x) / 2.0 + 0.5);\n" +
        "    vec3 columntaps1 = weight3((1.0 - f.y) / 2.0);\n" +
        "    vec3 columntaps2 = weight3((1.0 - f.y) / 2.0 + 0.5);\n" +
        "\n" +
        "    // make sure all taps added together is exactly 1.0, otherwise some\n" +
        "    // (very small) distortion can occur\n" +
        "    float suml = dot(linetaps1, 1.0) + dot(linetaps2, 1.0);\n" +
        "    float sumc = dot(columntaps1, 1.0) + dot(columntaps2, 1.0);\n" +
        "    linetaps1 /= suml;\n" +
        "    linetaps2 /= suml;\n" +
        "    columntaps1 /= sumc;\n" +
        "    columntaps2 /= sumc;\n" +
        "\n" +
        "    vec2 xystart = (-2.5 - f) * stepxy + pos;\n" +
        "    vec3 xpos1 = vec3(xystart.x, xystart.x + stepxy.x, xystart.x + stepxy.x * 2.0);\n" +
        "    vec3 xpos2 = vec3(xystart.x + stepxy.x * 3.0, xystart.x + stepxy.x * 4.0, xystart.x + stepxy.x * 5.0);\n" +
        "\n" +
        "    gl_FragColor.rgb =\n" +
        "        line(xystart.y                 , xpos1, xpos2, linetaps1, linetaps2) * columntaps1.r +\n" +
        "        line(xystart.y + stepxy.y      , xpos1, xpos2, linetaps1, linetaps2) * columntaps2.r +\n" +
        "        line(xystart.y + stepxy.y * 2.0, xpos1, xpos2, linetaps1, linetaps2) * columntaps1.g +\n" +
        "        line(xystart.y + stepxy.y * 3.0, xpos1, xpos2, linetaps1, linetaps2) * columntaps2.g +\n" +
        "        line(xystart.y + stepxy.y * 4.0, xpos1, xpos2, linetaps1, linetaps2) * columntaps1.b +\n" +
        "        line(xystart.y + stepxy.y * 5.0, xpos1, xpos2, linetaps1, linetaps2) * columntaps2.b;\n" +
        "\n" +
        "    gl_FragColor.a = 1.0;\n" +
        "}\n";
    private int createShader(String shaderGLSL, int shaderType) throws Exception
    {
        int shader = 0;
        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);
            if(shader == 0)
                return 0;

            ARBShaderObjects.glShaderSourceARB(shader, shaderGLSL);
            ARBShaderObjects.glCompileShaderARB(shader);

            if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
                throw new RuntimeException("Error creating shader: " + getLogInfo(shader));

            return shader;
        }
        catch(Exception exc) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            throw exc;
        }
    }

    private static String getLogInfo(int obj) {
        return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }
  

    private float getDistortionFitY()
    {
        float fit = 0.0f;

        switch (this.mc.gameSettings.distortionFitPoint)
        {
            case 0:
                fit = 1.0f;
                break;
            case 1:
                fit = 0.8f;
                break;
            case 2:
                fit = 0.6f;
                break;
            case 3:
                fit = 0.4f;
                break;
            case 4:
                fit = 0.2f;
                break;
            case 5:
            default:
                fit = 0.0f;
                break;
            case 6:
                fit = 0.0f;
                break;
            case 7:
                fit = 0.0f;
                break;
            case 8:
                fit = 0.0f;
                break;
            case 9:
                fit = 0.0f;
                break;
            case 10:
                fit = 0.0f;
                break;
            case 11:
                fit = 0.0f;
                break;
            case 12:
                fit = 0.0f;
                break;
            case 13:
                fit = 0.0f;
                break;
            case 14:
                fit = 0.0f;
                break;
        }

        return fit;
    }

    private float getDistortionFitX()
    {
        float fit = -1.0f;

        switch (this.mc.gameSettings.distortionFitPoint)
        {
            case 0:
                fit = -1.0f;
                break;
            case 1:
                fit = -1.0f;
                break;
            case 2:
                fit = -1.0f;
                break;
            case 3:
                fit = -1.0f;
                break;
            case 4:
                fit = -1.0f;
                break;
            case 5:
            default:
                fit = -1.0f;
                break;
            case 6:
                fit = -0.9f;
                break;
            case 7:
                fit = -0.8f;
                break;
            case 8:
                fit = -0.7f;
                break;
            case 9:
                fit = -0.6f;
                break;
            case 10:
                fit = -0.5f;
                break;
            case 11:
                fit = -0.4f;
                break;
            case 12:
                fit = -0.3f;
                break;
            case 13:
                fit = -0.2f;
                break;
            case 14:
                fit = -0.1f;
                break;
        }

        return fit;
    }
    
    private void drawMouseQuad( int mouseX, int mouseY )
    {
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColor3f(1, 1, 1);
        this.mc.mcProfiler.endStartSection("mouse pointer");
        this.mc.renderEngine.bindTexture("/gui/icons.png");
        this.mc.ingameGUI.drawTexturedModalRect(mouseX - 7, mouseY - 7, 0, 0, 16, 16);
        
        GL11.glEnable(GL11.GL_BLEND);
    }
    
    private void getPointedBlock(float renderPartialTicks)
    {
        if (this.mc.renderViewEntity != null && this.mc.theWorld != null)
        {
            this.mc.pointedEntityLiving = null;
            double blockReachDistance = (double)this.mc.playerController.getBlockReachDistance();
            double entityReachDistance = blockReachDistance;
            Vec3 pos = Vec3.createVectorHelper(renderOriginX + camRelX, renderOriginY + camRelY, renderOriginZ + camRelZ); 
            Vec3 aim = Vec3.createVectorHelper(aimX, aimY, aimZ);
            Vec3 endPos = pos.addVector(aim.xCoord*blockReachDistance,aim.yCoord*blockReachDistance ,aim.zCoord*blockReachDistance );

            this.mc.objectMouseOver = this.mc.theWorld.rayTraceBlocks(pos, endPos);


            double crossDistance = 0;
            if (this.mc.objectMouseOver != null)
            {
                entityReachDistance = this.mc.objectMouseOver.hitVec.distanceTo(pos);
                crossDistance = entityReachDistance;
            }
            else
            {
	            endPos = pos.addVector(aim.xCoord*128,aim.yCoord*128,aim.zCoord*128);
	            MovingObjectPosition crossPos = this.mc.theWorld.rayTraceBlocks(pos, endPos);
	            if( crossPos != null )
	            {
	            	crossDistance = crossPos.hitVec.distanceTo(pos);
	            }
            }

            if (this.mc.playerController.extendedReach())
            {
                blockReachDistance = 6.0D;
                entityReachDistance = 6.0D;
            }
            else
            {
                if (blockReachDistance > 3.0D)
                {
                    entityReachDistance = 3.0D;
                }

                blockReachDistance = entityReachDistance;
            }

            crossX = (float)(aim.xCoord * crossDistance + pos.xCoord - renderOriginX);
            crossY = (float)(aim.yCoord * crossDistance + pos.yCoord - renderOriginY);
            crossZ = (float)(aim.zCoord * crossDistance + pos.zCoord - renderOriginZ);

            Vec3 otherpos = mc.renderViewEntity.getPosition(renderPartialTicks);
            getPointedEntity(otherpos, aim, blockReachDistance, entityReachDistance);
        }
    }
    

    public void getMouseOver(float par1)
    {
    	//No-op for performance reasons (MouseOver set in render loop)
    }
    
    public void startCalibration()
    {
    	calibrationHelper = new CalibrationHelper(mc);
    }

    public void resetGuiYawOrientation()
    {
        guiYawOrientationResetRequested = true;
    }
}

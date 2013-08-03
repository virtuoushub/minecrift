/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;

import net.minecraft.src.Minecraft;
import net.minecraft.src.OpenGlHelper;

import org.lwjgl.opengl.*;

public class FBOParams
{
    protected static boolean useEXT = false;

	public FBOParams(String fboName, boolean useHighPrecisionBuffer, int fboWidth, int fboHeight )
	{
        int nBufferFormat = GL11.GL_RGBA8;

        Minecraft mc = Minecraft.getMinecraft();
        if (useHighPrecisionBuffer)
        {
            nBufferFormat = GL11.GL_RGBA16;
        }

        // The framebuffer, which regroups 0, 1, or more textures, and 0 or 1 depth buffer.
        try {
            _frameBufferId = GL30.glGenFramebuffers();
        }
        catch (IllegalStateException ex)
        {
            System.out.println("[Minecrift] FBO creation: GL30.glGenFramebuffers not supported. Attempting to use EXTFramebufferObject.glGenFramebuffersEXT");
            useEXT = true;
            try {
                _frameBufferId = EXTFramebufferObject.glGenFramebuffersEXT();
            }
            catch (IllegalStateException ex1)
            {
                System.out.println("[Minecrift] FBO creation: EXTFramebufferObject.glGenFramebuffersEXT not supported, FBO creation failed.");
                throw ex1;
            }
        }

        if (!useEXT)
        {
            _colorTextureId = GL11.glGenTextures();
            _depthRenderBufferId = GL30.glGenRenderbuffers();

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, _frameBufferId);
            mc.checkGLError("FBO bind framebuffer");

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, _colorTextureId);
            mc.checkGLError("FBO bind texture");

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, nBufferFormat, fboWidth, fboHeight, 0, GL11.GL_RGBA, GL11.GL_INT, (java.nio.ByteBuffer) null);
            System.out.println("[Minecrift] FBO '" + fboName + "': w: " + fboWidth + ", h: " + fboHeight);

            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, _colorTextureId, 0);

            mc.checkGLError("FBO bind texture framebuffer");

            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, _depthRenderBufferId);                // bind the depth renderbuffer
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, fboWidth, fboHeight); // get the data space for it
            GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, _depthRenderBufferId);
            mc.checkGLError("FBO bind depth framebuffer");
        }
        else
        {
            _colorTextureId = GL11.glGenTextures();
            _depthRenderBufferId = EXTFramebufferObject.glGenRenderbuffersEXT();

            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, _frameBufferId);
            mc.checkGLError("FBO bind framebuffer");

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, _colorTextureId);
            mc.checkGLError("FBO bind texture");

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, nBufferFormat, fboWidth, fboHeight, 0, GL11.GL_RGBA, GL11.GL_INT, (java.nio.ByteBuffer) null);
            System.out.println("[Minecrift] FBO '" + fboName + "': w: " + fboWidth + ", h: " + fboHeight);

            EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, _colorTextureId, 0);

            mc.checkGLError("FBO bind texture framebuffer");

            EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, _depthRenderBufferId);                // bind the depth renderbuffer
            EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT24, fboWidth, fboHeight); // get the data space for it
            EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, _depthRenderBufferId);
            mc.checkGLError("FBO bind depth framebuffer");
        }
	}
	
	public void bindRenderTarget()
	{
        if (!useEXT)
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, _frameBufferId );
        else
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, _frameBufferId );
	}
	
	public void bindTexture()
	{
        OpenGlHelper.setActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, _colorTextureId);
    }
	
	public void delete()
	{
        if (_depthRenderBufferId != -1)
        {
            if (!useEXT)
                GL30.glDeleteRenderbuffers(_depthRenderBufferId);
            else
                EXTFramebufferObject.glDeleteRenderbuffersEXT(_depthRenderBufferId);

            _depthRenderBufferId = -1;
        }

        if (_colorTextureId != -1)
        {
            GL11.glDeleteTextures(_colorTextureId);
            _colorTextureId = -1;
        }

        if (_frameBufferId != -1)
        {
            if (!useEXT)
                GL30.glDeleteFramebuffers(_frameBufferId);
            else
                EXTFramebufferObject.glDeleteFramebuffersEXT(_frameBufferId);

            _frameBufferId = -1;
        }
	}
	
    int _frameBufferId = -1;
    int _colorTextureId = -1;
    int _depthRenderBufferId = -1;
}

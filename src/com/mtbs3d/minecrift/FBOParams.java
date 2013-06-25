/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift;

import net.minecraft.client.Minecraft;
import net.minecraft.src.OpenGlHelper;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

public class FBOParams
{
	public FBOParams(boolean useHighPrecisionBuffer, int fboWidth, int fboHeight )
	{
        int nBufferFormat = GL11.GL_RGBA8;

        Minecraft mc = Minecraft.getMinecraft();
        if (useHighPrecisionBuffer)
        {
            nBufferFormat = GL11.GL_RGBA16;
        }

        // The framebuffer, which regroups 0, 1, or more textures, and 0 or 1 depth buffer.
        _frameBufferId = GL30.glGenFramebuffers();
        _colorTextureId = GL11.glGenTextures();
        _depthRenderBufferId = GL30.glGenRenderbuffers();

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, _frameBufferId);
        mc.checkGLError("FBO bind framebuffer");

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, _colorTextureId);
        mc.checkGLError("FBO bind texture");

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        if (mc.gameSettings.useMipMaps)
        {
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        }
        else
        {
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, nBufferFormat, fboWidth, fboHeight, 0, GL11.GL_RGBA, GL11.GL_INT, (java.nio.ByteBuffer) null);
        System.out.println("FBO width: " + fboWidth + ", FBO height: " + fboHeight);
        if (mc.gameSettings.useMipMaps)
        {
            // Mipmap gen
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        }

        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, _colorTextureId, 0);

        mc.checkGLError("FBO bind texture framebuffer");

        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, _depthRenderBufferId);                // bind the depth renderbuffer
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, fboWidth, fboHeight); // get the data space for it
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, _depthRenderBufferId);
        mc.checkGLError("FBO bind depth framebuffer");
	}
	
	public void bindRenderTarget()
	{
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, _frameBufferId );
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
            GL30.glDeleteRenderbuffers(_depthRenderBufferId);
            _depthRenderBufferId = -1;
        }

        if (_colorTextureId != -1)
        {
            GL11.glDeleteTextures(_colorTextureId);
            _colorTextureId = -1;
        }

        if (_frameBufferId != -1)
        {
            GL30.glDeleteFramebuffers(_frameBufferId);
            _frameBufferId = -1;
        }
	}
	
    int _frameBufferId = -1;
    int _colorTextureId = -1;
    int _depthRenderBufferId = -1;
}

/**
 * Copyright 2014 StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.api;


import de.fruitfly.ovr.EyeRenderParams;
import de.fruitfly.ovr.enums.EyeType;
import de.fruitfly.ovr.structs.*;

/**
 * Implement this to provide Minecrift with stereo rendering services
 *
 * @author StellaArtois
 *
 */
public interface IStereoProvider extends IBasePlugin
{
    public FovTextureInfo getFovTextureSize(FovPort LeftFov,
                                            FovPort RightFov,
                                            float renderScaleFactor);

    public EyeRenderParams configureRendering(Sizei InTextureSize,
                                              Sizei OutTextureSize,
                                              GLConfig glConfig,
                                              FovPort LeftFov,
                                              FovPort RightFov);

    public EyeRenderParams configureRenderingDualTexture(Sizei InTexture1Size,
                                                         Sizei InTexture2Size,
                                                         Sizei OutDisplaySize,
                                                         GLConfig glConfig,
                                                         FovPort LeftFov,
                                                         FovPort RightFov);

    public void resetRenderConfig();

    public EyeType eyeRenderOrder(int index);

    public boolean usesDistortion();

    public boolean isStereo();

    public boolean isGuiOrtho();

    public FrameTiming getFrameTiming();

    public Posef getEyePose(EyeType eye);

    public Matrix4f getMatrix4fProjection(FovPort fov,
                                          float nearClip,
                                          float farClip);
}
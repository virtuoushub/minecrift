package com.mtbs3d.minecrift.render;

import com.mtbs3d.minecrift.FBOParams;
import de.fruitfly.ovr.EyeRenderParams;
import de.fruitfly.ovr.HMDInfo;
import net.minecraft.src.OpenGlHelper;
import org.lwjgl.opengl.*;

import static java.lang.Math.ceil;

/**
 * Created with IntelliJ IDEA.
 * User: StellaArtois
 * Date: 8/6/13
 * Time: 9:33 PM
 */
public class DistortionParams
{
    public int half_screenWidth;

    public float leftLensCenterX;
    public float leftLensCenterY;
    public float rightLensCenterX;
    public float rightLensCenterY;

    public float leftScreenCenterX;
    public float leftScreenCenterY;
    public float rightScreenCenterX;
    public float rightScreenCenterY;

    public float scaleX;
    public float scaleY;
    public float scaleInX;
    public float scaleInY;

    public float[] DistortionK;
    public float[] ChromaticAb;

    FBOParams distortionMapFBO;

    public DistortionParams(HMDInfo hmdInfo,
                            EyeRenderParams eyeRenderParams,
                            int FBWidth,
                            int FBHeight,
                            boolean useChromaticAbCorrection,
                            boolean superSampleSupported,
                            boolean useSuperSample,
                            float superSampleScaleFactor)
    {
        DistortionK = new float[4];
        ChromaticAb = new float[4];

        DistortionK[0] = hmdInfo.DistortionK[0]; DistortionK[1] = hmdInfo.DistortionK[1]; DistortionK[2] = hmdInfo.DistortionK[2]; DistortionK[3] = hmdInfo.DistortionK[3];
        ChromaticAb[0] = hmdInfo.ChromaticAb[0]; ChromaticAb[1] = hmdInfo.ChromaticAb[1]; ChromaticAb[2] = hmdInfo.ChromaticAb[2]; ChromaticAb[3] = hmdInfo.ChromaticAb[3];

        if ( !superSampleSupported || !useSuperSample)
            superSampleScaleFactor = 1f;

        FBWidth  = (int)ceil(FBWidth  * superSampleScaleFactor);
        FBHeight = (int)ceil(FBHeight * superSampleScaleFactor);

        // Setup distortion parameters
        float lw = eyeRenderParams._leftViewPortW  / (float)FBWidth;
        float lh = eyeRenderParams._leftViewPortH  / (float)FBHeight;
        float lx = eyeRenderParams._leftViewPortX  / (float)FBWidth;
        float ly = eyeRenderParams._leftViewPortY  / (float)FBHeight;
        float rw = eyeRenderParams._rightViewPortW / (float)FBWidth;
        float rh = eyeRenderParams._rightViewPortH / (float)FBHeight;
        float rx = eyeRenderParams._rightViewPortX / (float)FBWidth;
        float ry = eyeRenderParams._rightViewPortY / (float)FBHeight;

        half_screenWidth = FBWidth/2;

        float aspect = (float)eyeRenderParams._leftViewPortW / (float)eyeRenderParams._leftViewPortH;

        leftLensCenterX = lx + (lw + eyeRenderParams._XCenterOffset * 0.5f) * 0.5f;
        leftLensCenterY = ly + lh * 0.5f;
        rightLensCenterX = rx + (rw + -eyeRenderParams._XCenterOffset * 0.5f) * 0.5f;
        rightLensCenterY = ry + rh * 0.5f;

        leftScreenCenterX = lx + lw * 0.5f;
        leftScreenCenterY = ly + lh * 0.5f;
        rightScreenCenterX = rx + rw * 0.5f;
        rightScreenCenterY = ry + rh * 0.5f;

        float scaleFactor = 1.0f / eyeRenderParams._renderScale;
        scaleX = (lw / 2) * scaleFactor;
        scaleY = (lh / 2) * scaleFactor * aspect;
        scaleInX = 2 / lw;
        scaleInY = (2 / lh) / aspect;

        // Pre-calculate distortion texture
        int distortionMapShaderId;

        // Init shaders
        if (useChromaticAbCorrection)
        {
            distortionMapShaderId = ShaderHelper.initShaders(BASIC_VERTEX_SHADER, OCULUS_DISTORTION_MAP_FRAGMENT_SHADER_WITH_CHROMATIC_ABERRATION_CORRECTION, false);
        }
        else
        {
            distortionMapShaderId = ShaderHelper.initShaders(BASIC_VERTEX_SHADER, OCULUS_DISTORTION_MAP_FRAGMENT_SHADER_NO_CHROMATIC_ABERRATION_CORRECTION, false);
        }

        // Create FBO
        distortionMapFBO = new FBOParams("distortionMap", true, (int)ceil(FBWidth), (int)ceil(FBHeight));

        // Bind as active
        distortionMapFBO.bindRenderTarget();

        // Clear down
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL11.glClearDepth(1.0D);
        GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // Render onto the entire screen framebuffer
        GL11.glViewport(0, 0, FBWidth, FBHeight);

//        ARBColorBufferFloat.glClampColorARB(ARBColorBufferFloat.GL_CLAMP_VERTEX_COLOR_ARB, GL11.GL_FALSE);
//        ARBColorBufferFloat.glClampColorARB(ARBColorBufferFloat.GL_CLAMP_READ_COLOR_ARB, GL11.GL_FALSE);
//        ARBColorBufferFloat.glClampColorARB(ARBColorBufferFloat.GL_CLAMP_FRAGMENT_COLOR_ARB, GL11.GL_FALSE);

        // Set the distortion map creation shader as in use
        ARBShaderObjects.glUseProgramObjectARB(distortionMapShaderId);

        // Set up the fragment shader uniforms
        ARBShaderObjects.glUniform1iARB(ARBShaderObjects.glGetUniformLocationARB(distortionMapShaderId, "half_screenWidth"), FBWidth/2 );
        ARBShaderObjects.glUniform2fARB(ARBShaderObjects.glGetUniformLocationARB(distortionMapShaderId, "LeftLensCenter"), leftLensCenterX, leftLensCenterY);
        ARBShaderObjects.glUniform2fARB(ARBShaderObjects.glGetUniformLocationARB(distortionMapShaderId, "RightLensCenter"), rightLensCenterX, rightLensCenterY);
        ARBShaderObjects.glUniform2fARB(ARBShaderObjects.glGetUniformLocationARB(distortionMapShaderId, "LeftScreenCenter"), leftScreenCenterX, leftScreenCenterY);
        ARBShaderObjects.glUniform2fARB(ARBShaderObjects.glGetUniformLocationARB(distortionMapShaderId, "RightScreenCenter"), rightScreenCenterX, rightScreenCenterY);
        ARBShaderObjects.glUniform2fARB(ARBShaderObjects.glGetUniformLocationARB(distortionMapShaderId, "Scale"), scaleX, scaleY);
        ARBShaderObjects.glUniform2fARB(ARBShaderObjects.glGetUniformLocationARB(distortionMapShaderId, "ScaleIn"), scaleInX, scaleInY);
        ARBShaderObjects.glUniform4fARB(ARBShaderObjects.glGetUniformLocationARB(distortionMapShaderId, "HmdWarpParam"), hmdInfo.DistortionK[0], hmdInfo.DistortionK[1], hmdInfo.DistortionK[2], hmdInfo.DistortionK[3]);
        ARBShaderObjects.glUniform4fARB(ARBShaderObjects.glGetUniformLocationARB(distortionMapShaderId, "ChromAbParam"), hmdInfo.ChromaticAb[0], hmdInfo.ChromaticAb[1], hmdInfo.ChromaticAb[2], hmdInfo.ChromaticAb[3]);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glTranslatef (0.0f, 0.0f, -0.7f);
        GL11.glColor3f(1, 1, 1);

        drawQuad();

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glPopAttrib();

        // Stop shader use
        ARBShaderObjects.glUseProgramObjectARB(0);

//        ARBColorBufferFloat.glClampColorARB(ARBColorBufferFloat.GL_CLAMP_VERTEX_COLOR_ARB, GL11.GL_TRUE);
//        ARBColorBufferFloat.glClampColorARB(ARBColorBufferFloat.GL_CLAMP_READ_COLOR_ARB, GL11.GL_TRUE);
//        ARBColorBufferFloat.glClampColorARB(ARBColorBufferFloat.GL_CLAMP_FRAGMENT_COLOR_ARB, GL11.GL_TRUE);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0 );

        ShaderHelper.checkGLError("After distortionMap");
    }

    public void bindTexture()
    {
        distortionMapFBO.bindTexture();
    }

    public void bindTexture_Unit1()
    {
        distortionMapFBO.bindTexture_Unit1();
    }

    public int getColorTextureId()
    {
        return distortionMapFBO.getColorTextureId();
    }

    public void delete()
    {
        distortionMapFBO.delete();
    }

    private void drawQuad()
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

    private final String BASIC_VERTEX_SHADER =

            "#version 110\n" +
                    "\n" +
                    "varying vec4 textCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = ftransform();\n" +
                    "    textCoord = gl_MultiTexCoord0;\n" +
                    "}\n";

    public final String OCULUS_DISTORTION_MAP_FRAGMENT_SHADER_NO_CHROMATIC_ABERRATION_CORRECTION =

            "#version 120\n" +
                    "\n" +
                    "//uniform sampler2D bgl_RenderTexture;\n" +
                    "uniform int half_screenWidth;\n" +
                    "uniform vec2 LeftLensCenter;\n" +
                    "uniform vec2 RightLensCenter;\n" +
                    "uniform vec2 LeftScreenCenter;\n" +
                    "uniform vec2 RightScreenCenter;\n" +
                    "uniform vec2 Scale;\n" +
                    "uniform vec2 ScaleIn;\n" +
                    "uniform vec4 HmdWarpParam;\n" +
                    "uniform vec4 ChromAbParam;\n" +
                    "varying vec4 textCoord;\n" +
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
                    "    vec2 oTexCoord = textCoord.xy;\n" +
                    "    vec2 tc = HmdWarp(oTexCoord, LensCenter);\n" +
                    "    if (any(bvec2(clamp(tc,ScreenCenter-vec2(0.25,0.5), ScreenCenter+vec2(0.25,0.5)) - tc)))\n" +
                    "    {\n" +
                    "        gl_FragColor = vec4(-1.0, -1.0, 0.0, 1.0);\n" +
                    "        return;\n" +
                    "    }\n" +
                    "\n" +
                    "    gl_FragColor = vec4(tc.x, tc.y, 0.0, 1.0);\n" +
                    "}\n";

    // TODO: It would be possible to remove all calculations in the in-game ab chrom shader if we cached
    // all color component lookup vectors to a 2D texture array. However I'm not so sure the increased
    // number of texture lookups in game would out-weight the extra math currently...

    public final String OCULUS_DISTORTION_MAP_FRAGMENT_SHADER_WITH_CHROMATIC_ABERRATION_CORRECTION =

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
                    "varying vec4 textCoord;\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "    vec2 LensCenter = gl_FragCoord.x < half_screenWidth ? LeftLensCenter : RightLensCenter;\n" +
                    "    vec2 ScreenCenter = gl_FragCoord.x < half_screenWidth ? LeftScreenCenter : RightScreenCenter;\n" +
                    "\n" +
                    "    vec2 theta = (textCoord.xy - LensCenter) * ScaleIn;\n" +
                    "    float rSq = theta.x * theta.x + theta.y * theta.y;\n" +
                    "    vec2 theta1 = theta * (HmdWarpParam.x + HmdWarpParam.y * rSq + HmdWarpParam.z * rSq * rSq + HmdWarpParam.w * rSq * rSq * rSq);\n" +
                    "\n" +
                    "    gl_FragColor = vec4(theta1.x, theta1.y, 0.0, 1.0); // Cache theta1 in our distortion map...\n" +
                    "\n" +
                    "    vec2 thetaBlue = theta1 * (ChromAbParam.w * rSq + ChromAbParam.z);\n" +
                    "    vec2 tcBlue = thetaBlue * Scale + LensCenter;\n" +
                    "\n" +
                    "    // ...but keep processing until we find out if we can discard the result as out-of-view...\n" +
                    "    if (any(bvec2(clamp(tcBlue, ScreenCenter-vec2(0.25,0.5), ScreenCenter+vec2(0.25,0.5)) - tcBlue))) {\n" +
                    "        gl_FragColor = vec4(-1.0, -1.0, 0.0, 1.0);\n" +
                    "    }\n" +
                    "}\n";
}

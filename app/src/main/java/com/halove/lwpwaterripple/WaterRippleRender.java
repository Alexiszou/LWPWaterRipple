package com.halove.lwpwaterripple;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by alexis on 09/01/17.
 */

public class WaterRippleRender extends BaseRender{
    private static final String TAG = "WaterRippleRender";

    private Bitmap mBitmap;
    public WaterRippleRender(Context context){
        super(context);
        init();
    }

    private void init(){
        mBitmap = Utilities.drawable2Bitmap(mWallpaperManager.getDrawable());

    }

    public void onDrawFrame(GL10 gl) {
        // Your rendering code goes here

    }

    @Override
    public void onSurfaceCreated(GL10 gl10, javax.microedition.khronos.egl.EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);
        /*GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        //active the texture unit 0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        loadVertex();
        initShader();
        loadTexture();*/


    }



    /*private void loadVertex() {
    }

    private void initShader(){

    }*/

    private void loadTexture(Bitmap bmp){

        //GLES20.glGenTextures();
    }
    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        super.onSurfaceChanged(gl10, i, i1);
    }

    @Override
    public void release() {
        super.release();
    }
}

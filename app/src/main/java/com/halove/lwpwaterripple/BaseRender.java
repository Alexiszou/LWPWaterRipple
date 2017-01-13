package com.halove.lwpwaterripple;

import android.app.WallpaperManager;
import android.content.Context;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by alexis on 10/01/17.
 */

public class BaseRender implements GLSurfaceView.Renderer {

    public WallpaperManager mWallpaperManager;
    public Context mContext;

    public BaseRender(Context context){
        //mWallpaperManager = (WallpaperManager)mContext.getSystemService(Context.WALLPAPER_SERVICE);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {

    }

    @Override
    public void onDrawFrame(GL10 gl10) {

    }

    /**
     * Called when the engine is destroyed. Do any necessary clean up because
     * at this point your renderer instance is now done for.
     */
    public void release() {

    }
}

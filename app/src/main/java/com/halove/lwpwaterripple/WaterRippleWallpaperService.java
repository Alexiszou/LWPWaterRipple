package com.halove.lwpwaterripple;

import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;

/**
 * Created by alexis on 09/01/17.
 */

public class WaterRippleWallpaperService extends OpenGLES2WallpaperService {

    @Override
    GLSurfaceView.Renderer getNewRenderer() {
        TextureRender renderer = new TextureRender(this);
        renderer.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.samsungpond));
        return renderer;
    }
}

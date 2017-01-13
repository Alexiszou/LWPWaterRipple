package com.halove.lwpwaterripple;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.service.wallpaper.WallpaperService;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import java.io.FileDescriptor;
import java.io.PrintWriter;

/**
 * Created by alexis on 06/01/17.
 * 动态壁纸的服务程序
 */

public class GLWallpaperService extends WallpaperService {

    public GLWallpaperService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public Engine onCreateEngine() {
        return new GLEngine();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void dump(FileDescriptor fd, PrintWriter out, String[] args) {
        super.dump(fd, out, args);
    }


    /**
     * 动态壁纸引擎
     *
     */
    public class GLEngine extends Engine{
        private static final String TAG = "MyEngine";
        private WallpaperGLSurfaceView mGLSurfaceView;
        private boolean rendererHasBeenSet;
        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (rendererHasBeenSet) {
                if (visible) {
                    mGLSurfaceView.onResume();
                } else {
                    mGLSurfaceView.onPause();
                }
            }
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            mGLSurfaceView = new WallpaperGLSurfaceView(GLWallpaperService.this);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mGLSurfaceView.onDestroy();
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
        }

        protected void setRenderer(GLSurfaceView.Renderer renderer) {
            mGLSurfaceView.setRenderer(renderer);
            rendererHasBeenSet = true;
        }

        protected void setEGLContextClientVersion(int version) {
            mGLSurfaceView.setEGLContextClientVersion(version);
        }

        protected void setPreserveEGLContextOnPause(boolean preserve) {
            mGLSurfaceView.setPreserveEGLContextOnPause(preserve);
        }

        class WallpaperGLSurfaceView extends GLSurfaceView{

            public WallpaperGLSurfaceView(Context context) {
                super(context);
            }

            public WallpaperGLSurfaceView(Context context, AttributeSet attrs) {
                super(context, attrs);
            }


            @Override
            public SurfaceHolder getHolder() {
                return getSurfaceHolder();
            }

            public void onDestroy(){
                super.onDetachedFromWindow();
            }

        }
    }
}

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.halove.waterripple;

import android.app.WallpaperManager;
import android.content.Context;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.renderscript.RenderScriptGL;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public abstract class RenderScriptWallpaper<T extends RenderScriptScene> extends WallpaperService {
    private static final String TAG = "RenderScriptWallpaper";
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public Engine onCreateEngine() {
        return new RenderScriptEngine();
    }


    protected abstract T createScene(int width, int height);
    protected abstract Context getContext();

    private class RenderScriptEngine extends Engine {
        private RenderScriptGL mRs;
        private T mRenderer;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(false);
            surfaceHolder.setSizeFromLayout();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d(TAG,"RenderScriptWallpaper onDestroy!!!!");
            destroyRenderer();
        }


        private void destroyRenderer() {
            if (mRenderer != null) {
                mRenderer.stop();
                mRenderer = null;
            }
            if (mRs != null) {
                mRs.setSurface(null, 0, 0);
                mRs.destroy();
                mRs = null;
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            Log.d(TAG,"RenderScriptWallpaper onVisibilityChanged!!!!"+visible);
            if (mRenderer != null) {
                if (visible) {
                    mRenderer.start();
                } else {
                    mRenderer.stop();
                }
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            if (mRs != null) {
                mRs.setSurface(holder, width, height);
            }
            if (mRenderer == null) {
                mRenderer = createScene(width, height);
                mRenderer.init(getContext(),mRs, getResources(), isPreview());
                mRenderer.start();
            } else {
                mRenderer.resize(width, height);
            }
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xStep, float yStep, int xPixels, int yPixels) {
            if (mRenderer != null) mRenderer.setOffset(xOffset, yOffset, xPixels, yPixels);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);

            RenderScriptGL.SurfaceConfig sc = new RenderScriptGL.SurfaceConfig();
            mRs = new RenderScriptGL(RenderScriptWallpaper.this, sc);
            mRs.setPriority(RenderScript.Priority.LOW);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            destroyRenderer();
            Log.d(TAG,"RenderScriptWallpaper onSurfaceDestroyed!!!!");
        }

        @Override
        public Bundle onCommand(String action, int x, int y, int z,
                Bundle extras, boolean resultRequested) {
            if (mRenderer != null) {
                return mRenderer.onCommand(action, x, y, z, extras, resultRequested);
            } else {
                return null;
            }
        }


        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    mRenderer.addDrop(event.getX(), event.getY());
                    break;
            }

        }
    }
}

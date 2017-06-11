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


package com.harlan.waterscreen;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.renderscript.RSSurfaceView;
import android.renderscript.RenderScript;
import android.renderscript.RenderScriptGL;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

class FallView extends RSSurfaceView {
    private FallRS mRender;
    /*******add by Harlan***********************/
    private RenderScriptGL mRs;
    
	public static final int PLAY_DROP_MUSIC = 0x1;
	public static final int PLAY_WATER_MUSIC = 0x2;
	private boolean isPlay;
	private int sConut;
	private SoundPool mSound;
	private HashMap<Integer, Integer> soundMap;
    /*******************************************/

    public FallView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
//        RenderScriptGL.SurfaceConfig sc = new RenderScriptGL.SurfaceConfig();
//        RenderScriptGL RS = createRenderScriptGL(sc);
//        mRender = new FallRS(w, h);
//        mRender.init(RS, getResources(), false);
//        mRender.start();
        /*******add by Harlan***********************/
        Log.e("zhz","w:"+w+",h:"+h);
        if (mRs != null) {
            mRs.setSurface(holder, w, h);
        }
        if (mRender == null) {
            mRender = new FallRS(w, h);
            mRender.init(mRs, getResources(), false);
            mRender.start();
        } else {
            mRender.resize(w, h);
        }
        /*****************************************/
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	mSound.play(soundMap.get(1), 5, 5, 0, 0, 1);
            	isPlay = true;
            	sConut = 0;
            	mRender.addDrop(event.getX(), event.getY());
            	break;
            case MotionEvent.ACTION_MOVE:
            	sConut ++;
                if (isPlay){
                //mSound.play(soundMap.get(2), 5, 5, 0, 0, 1);
                isPlay = false;
                }
                //if (sConut <10){
                mRender.addDrop(event.getX(), event.getY());
                //}
                MotionEvent.obtain(event).setAction(MotionEvent.ACTION_UP);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // Ignore
                }
                break;
        }
        return true;
    }
    
    /*******add by Harlan***********************/
   @Override
public void surfaceCreated(SurfaceHolder holder)
{
    super.surfaceCreated(holder);
    //初始化RenderScriptRS
    RenderScriptGL.SurfaceConfig sc = new RenderScriptGL.SurfaceConfig();
		if (mRs == null) {
			mRs = createRenderScriptGL(sc);
		}
    mRs.setPriority(RenderScript.Priority.LOW);
    
    mSound = new SoundPool(2, AudioManager.STREAM_RING, 5);
    soundMap = new HashMap<Integer, Integer>();
    soundMap.put(1, mSound.load(getContext(), R.raw.drop, 5));
    soundMap.put(2, mSound.load(getContext(), R.raw.water, 5)); 
}
    
    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        destroyRenderer();
    }
    
    private void destroyRenderer() {
        if (mRender != null) {
            mRender.stop();
            mRender = null;
        }
        if (mRs != null) {
            mRs.setSurface(null, 0, 0);
            mRs.destroy();
            mRs = null;
        }
    }
    
}
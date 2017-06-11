package com.halove.waterripple;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.wallpaper.WallpaperService;

/**
 * Created by alexis on 11/01/17.
 */

public class WaterRippleWallpaperService extends RenderScriptWallpaper<FallRS> {

    @Override
    public void onCreate() {
        super.onCreate();
        //startScreenBroadcastReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //destroyScreenReceiver();
    }

    @Override
    protected FallRS createScene(int width, int height) {
        return new FallRS(width,height);
    }

    @Override
    protected Context getContext() {
        return WaterRippleWallpaperService.this;
    }

    private ScreenBroadcastReceiver mScreenReceiver;
    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;


        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                // 开屏
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                // 锁屏
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                // 解锁
            }
        }
    }
    private void startScreenBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mScreenReceiver, filter);
    }

    private void destroyScreenReceiver(){
        unregisterReceiver(mScreenReceiver);
    }
}

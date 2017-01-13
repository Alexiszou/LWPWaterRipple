package com.halove.waterripple;

import android.app.WallpaperManager;
import android.content.Context;
import android.service.wallpaper.WallpaperService;

/**
 * Created by alexis on 11/01/17.
 */

public class WaterRippleWallpaperService extends RenderScriptWallpaper<FallRS> {

    @Override
    protected FallRS createScene(int width, int height) {
        return new FallRS(width,height);
    }

    @Override
    protected Context getContext() {
        return WaterRippleWallpaperService.this;
    }
}

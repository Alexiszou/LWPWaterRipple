package halove.app1;

import android.service.wallpaper.WallpaperService;

/**
 * Created by alexis on 11/01/17.
 */

public class WaterRippleWallpaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new WaterRippleEngine();
    }


    class WaterRippleEngine extends Engine{

    }
}

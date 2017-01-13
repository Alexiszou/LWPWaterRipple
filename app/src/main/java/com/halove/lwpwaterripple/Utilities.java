package com.halove.lwpwaterripple;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by alexis on 10/01/17.
 */

public class Utilities {

    public static Bitmap drawable2Bitmap(Drawable drawable){
        Bitmap bitmap = null;
        bitmap = ((BitmapDrawable)drawable).getBitmap();
        if(bitmap == null){
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 :
                    Bitmap.Config.RGB_565;
            bitmap = Bitmap.createBitmap(w,h,config);
            //注意，下面三行代码要用到，否则在View或者surfaceview里的canvas.drawBitmap会看不到图
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0,0,w,h);
            drawable.draw(canvas);
        }
        return bitmap;

    }
}

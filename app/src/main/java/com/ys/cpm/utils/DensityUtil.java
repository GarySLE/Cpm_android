package com.ys.cpm.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class DensityUtil { //todo kotlin转换

	public static int getScreenWidth(Context context) {
		return getScreenWidth(context.getResources().getDisplayMetrics());
	}

	public static int getScreenWidth(DisplayMetrics dm) {
		return dm.widthPixels;
	}

	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

    public static int dip2px(Context context, float dpValue) {
        return dip2px(context.getResources().getDisplayMetrics(), dpValue);
    }

    public static int dip2px(DisplayMetrics dm, float dpValue) {
        final float scale = dm.density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int px2sp(Context context, float v) {
	    final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
	    return (int) (v / fontScale + 0.5f);
    }

    public static int sp2px(Context context, float v) {
	    final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
	    return (int) (v * fontScale + 0.5f);
    }

}

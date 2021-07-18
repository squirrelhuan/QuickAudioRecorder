package cn.demomaster.quickaudiorecorderlib.util;

import android.app.Activity;
import android.content.Context;

/**
 * @author squirrel桓
 * @date 2018/11/21.
 * description：
 */
public class DisplayUtil {

    /* dp转换成px
     */
    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机分辨率从DP转成PX
     *
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 根据手机的分辨率PX(像素)转成DP
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static float getDimension(Context context, String value) {
        float r = 0f;
        if (value != null) {
            value = value.trim();
            if (value.endsWith("dp")) {

            }
            if (value.endsWith("dip")) {
                String a = value.replace("dip", "");
                r = dip2px(context, Float.valueOf(a));
            }
            if (value.endsWith("sp")) {
                String a = value.replace("sp", "");
                r = sp2px(context, Float.valueOf(a));
            }
            if (value.endsWith("pt")) {

            }
            if (value.endsWith("px")) {

            }
            if (value.endsWith("mm")) {

            }
            if (value.endsWith("in")) {

            }
        }
        return r;
    }

    /**
     * 获取屏幕高度(px)
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }
    /**
     * 获取屏幕高度(px)
     */
    public static int getScreenHeight(Activity context) {
        int height = context.getWindow().getDecorView().getMeasuredHeight();
        return Math.max(height,context.getResources().getDisplayMetrics().heightPixels);
    }
    /**
     * 获取屏幕宽度(px)
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

}

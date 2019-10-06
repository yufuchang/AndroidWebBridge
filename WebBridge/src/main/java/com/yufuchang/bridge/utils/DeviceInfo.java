package com.yufuchang.bridge.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.webkit.WebSettings;

public class DeviceInfo {
    private static DeviceInfo systemInfo;
    // 下拉刷新界面高度
    public int defaultBounceHeight;
    // 默认字体大小
    public int defaultFontSize;
    // WebView根据设配分辨率调整大小
    public WebSettings.ZoomDensity defaultzoom;
    // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
    public float density;
    // 屏幕密度（每寸像素：120/160/240/320）
    public int densityDpi;
    // 获取屏幕信息的类
    public DisplayMetrics displayMetrics;
    // 屏幕高（像素，如：800px）
    public int heightPixels;
    // 屏幕宽（像素，如：480px）
    public int widthPixels;
    public float scaledDensity;
    public float xdpi;
    public float ydpi;

    public static DeviceInfo getInstance() {
        if (systemInfo == null)
            systemInfo = new DeviceInfo();
        return systemInfo;
    }

    public void init(Context context, DisplayMetrics displayMetrics) {
        this.displayMetrics = displayMetrics;
        this.heightPixels = displayMetrics.heightPixels;
        this.widthPixels = displayMetrics.widthPixels;
        this.xdpi = displayMetrics.xdpi;
        this.ydpi = displayMetrics.ydpi;
        this.density = displayMetrics.density;
        this.densityDpi = displayMetrics.densityDpi;
        this.scaledDensity = displayMetrics.scaledDensity;
        // 根据屏幕每英寸的像素数，设置字体大小，下拉刷新高度
        switch (this.densityDpi) {

            case DisplayMetrics.DENSITY_LOW:
                this.defaultFontSize = 14;
                this.defaultzoom = WebSettings.ZoomDensity.CLOSE;
                this.defaultBounceHeight = 60;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                this.defaultFontSize = 16;
                this.defaultzoom = WebSettings.ZoomDensity.MEDIUM;
                this.defaultBounceHeight = 70;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                this.defaultFontSize = 24;
                this.defaultzoom = WebSettings.ZoomDensity.FAR;
                this.defaultBounceHeight = 80;
                break;
            case DisplayMetrics.DENSITY_TV:
                this.defaultFontSize = 32;
                this.defaultzoom = WebSettings.ZoomDensity.FAR;
                this.defaultBounceHeight = 90;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                this.defaultFontSize = 32;
                this.defaultzoom = WebSettings.ZoomDensity.FAR;
                this.defaultBounceHeight = 100;
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                this.defaultFontSize = 48;
                this.defaultzoom = WebSettings.ZoomDensity.FAR;
                this.defaultBounceHeight = 125;
                break;
            default:
                this.defaultFontSize = 32;
                this.defaultzoom = WebSettings.ZoomDensity.FAR;
                this.defaultBounceHeight = 100;
                break;
        }
    }
}

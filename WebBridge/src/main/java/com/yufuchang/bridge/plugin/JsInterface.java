package com.yufuchang.bridge.plugin;

import android.os.Handler;
import android.webkit.JavascriptInterface;

import com.yufuchang.bridge.utils.LogUtils;

public class JsInterface {

    private Handler handler = new Handler();

    private PluginManager pluginManager;

    public JsInterface(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    /**
     * js调用invoke方法，有参数，无返回值
     *
     * @param serviceName 服务名
     * @param action      操作名
     * @param params      操作参数
     */
    @JavascriptInterface
    public void invoke(final String serviceName, final String action,
                       final String params) {
        LogUtils.YfcDebug("serviceName:"+serviceName);
        LogUtils.YfcDebug("action:"+action);
        LogUtils.YfcDebug("params:"+params);
        handler.post(new Runnable() {
            @Override
            public void run() {
                pluginManager.execute(serviceName, action, params);
            }
        });
    }

    /**
     * js调用invoke方法，无参数，无返回值
     *
     * @param serviceName 服务名
     * @param action      操作名
     * @param
     */
    @JavascriptInterface
    public void invoke(final String serviceName, final String action) {
        LogUtils.YfcDebug("serviceName:"+serviceName);
        LogUtils.YfcDebug("action:"+action);
        handler.post(new Runnable() {
            public void run() {
                pluginManager.execute(serviceName, action, null);
            }
        });
    }

    /**
     * js调用invoke方法，有参数，有返回值
     *
     * @param serviceName 服务名
     * @param action      操作名
     * @param params      操作参数
     */
    @JavascriptInterface
    public String invokeAndReturn(String serviceName,
                                  String action, String params) {
        return pluginManager.executeAndReturn(serviceName, action, params);
    }

    /**
     * js调用invoke方法，无参数，有返回值
     *
     * @param serviceName 服务名
     * @param action      操作名
     * @param
     */
    @JavascriptInterface
    public String invokeAndReturn(final String serviceName, final String action) {
        return pluginManager.executeAndReturn(serviceName, action, null);
    }

}
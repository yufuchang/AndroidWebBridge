package com.yufuchang.bridge.plugin;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.yufuchang.bridge.interfaces.AndroidWebBridgeCallBackInterface;
import com.yufuchang.bridge.plugin.app.AppService;
import com.yufuchang.bridge.utils.LogUtils;
import com.yufuchang.bridge.utils.StringUtils;
import com.yufuchang.bridge.webview.AndroidWebBridgeWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


/**
 * 后台接口管理类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class PluginManager {

    private static final String TAG = "PLUGIN_MGR";
    private Context context;
    private AndroidWebBridgeWebView webView;
    private PluginInterface plugin;

    // 缓存功能类实例
    private HashMap<String, PluginInterface> entries = new HashMap<String, PluginInterface>();

    private AndroidWebBridgeCallBackInterface androidWebBridgeCallBackInterface;

    public PluginManager(Context ctx, AndroidWebBridgeWebView ImpWebView) {
        context = ctx;
        webView = ImpWebView;
    }

    /**
     * 执行对插件的操作，无返回值
     *
     * @param serviceName 服务名
     * @param action      操作名
     * @param params      操作参数
     */
    public void execute(final String serviceName, final String action,
                        final String params) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Handler mainThread = new Handler(Looper.getMainLooper());
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    executeOnMainThread(serviceName, action, params);
                }
            });
        } else {
            executeOnMainThread(serviceName, action, params);
        }
    }

    private void executeOnMainThread(String serviceName, final String action,
                                     final String params) {
        serviceName = getReallyServiceName(serviceName);
        if (serviceName != null) {
            LogUtils.YfcDebug("serviceName=" + serviceName);
            LogUtils.YfcDebug("action=" + action);
            PluginInterface plugin = getPlugin(serviceName);
            // 将传递过来的参数转换为JSON
            JSONObject jo = null;
            if (StringUtils.strIsNotNull(params)) {
                try {
                    jo = new JSONObject(params);
                } catch (JSONException e) {
                    LogUtils.YfcDebug("e==" + e.toString());
//                    iLog.e(TAG, "组装Json对象出现异常!");
                }
            }
            // 执行接口的execute方法
            if (plugin != null) {
                LogUtils.YfcDebug("11111111111111111");
                plugin.execute(action, jo);
            } else {
                if (androidWebBridgeCallBackInterface != null) {
                    androidWebBridgeCallBackInterface.onShowAndroidWebBridgeDialog();
                }
            }
        } else {
            if (androidWebBridgeCallBackInterface != null) {
                androidWebBridgeCallBackInterface.onShowAndroidWebBridgeDialog();
            }
        }
    }

    /**
     * 执行对插件的操作，有返回值
     *
     * @param serviceName 服务名
     * @param action      操作名
     * @param params      操作参数
     */
    public String executeAndReturn(final String serviceName,
                                   final String action, final String params) {
        String res = "";
        if (Looper.myLooper() != Looper.getMainLooper() && !serviceName.endsWith("EMMService") && !serviceName.endsWith("DeviceService")) {
            Handler mainThread = new Handler(Looper.getMainLooper());
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    executeAndReturnOnMainThead(serviceName, action, params);
                }
            });
        } else {
            res = executeAndReturnOnMainThead(serviceName, action, params);
        }
        return res;
    }


    private String executeAndReturnOnMainThead(final String serviceName,
                                               final String action, final String params) {
        String reallyServiceName = getReallyServiceName(serviceName);
        String res = "";
        if (reallyServiceName != null) {
            LogUtils.YfcDebug("serviceName=" + reallyServiceName);
            LogUtils.YfcDebug("action=" + action);
            PluginInterface plugin = getPlugin(reallyServiceName);
            // 将传递过来的参数转换为JSON
            JSONObject jo = null;
            if (StringUtils.strIsNotNull(params)) {
                try {
                    jo = new JSONObject(params);
                } catch (JSONException e) {
                    LogUtils.YfcDebug("组装Json对象出现异常!");
                }
            }
            // 执行接口的execute方法
            if (plugin != null) {
                try {
                    res = plugin.executeAndReturn(action, jo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (androidWebBridgeCallBackInterface != null) {
                    androidWebBridgeCallBackInterface.onShowAndroidWebBridgeDialog();
                }
            }
        } else {
            if (androidWebBridgeCallBackInterface != null) {
                androidWebBridgeCallBackInterface.onShowAndroidWebBridgeDialog();
            }
        }
        return res;
    }

    private String getReallyServiceName(String serviceName) {
        if (serviceName.endsWith("AppService")) {
            serviceName = AppService.class.getCanonicalName();
        }
        return serviceName;
    }

    public AndroidWebBridgeCallBackInterface getAndroidWebBridgeCallBackInterface() {
        return androidWebBridgeCallBackInterface;
    }

    public void setAndroidWebBridgeCallBackInterface(AndroidWebBridgeCallBackInterface androidWebBridgeCallBackInterface) {
        this.androidWebBridgeCallBackInterface = androidWebBridgeCallBackInterface;
    }

    /**
     * 获取插件
     *
     * @param service
     * @return PluginInterface
     */
    public PluginInterface getPlugin(String service) {
        service = service.trim();
        PluginInterface plugin = null;
        Log.d("jason", "serviceName=" + service);
        if (!entries.containsKey(service)) {
            plugin = createPlugin(service);
            if (plugin != null) {
                entries.put(service, plugin);
            }
        } else {
            plugin = entries.get(service);
        }
        return plugin;
    }

    /**
     * 创建IPlugin对象 如果对象已经被创建则返回接口
     *
     * @return The plugin object
     */
    private PluginInterface createPlugin(String clssName) {
        try {
            @SuppressWarnings("rawtypes")
            Class c = getClassByName(clssName);
            plugin = (PluginInterface) c.newInstance();
            plugin.init(context, webView, androidWebBridgeCallBackInterface);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plugin;
    }

    /**
     * 获取到功能类
     *
     * @param clazz
     * @return
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("rawtypes")
    private Class getClassByName(String clazz)
            throws ClassNotFoundException {
        Class c = null;
        if (clazz != null) {
            c = Class.forName(clazz);
        }
        return c;
    }

    /**
     * activity关闭之前调用方法关闭相应的空间
     */
    public void onDestroy() {
        if (entries != null) {
            for (PluginInterface plugin : entries.values()) {
                if (plugin != null) {
                    plugin.onDestroy();
                }
            }
            entries.clear();
        }
    }

    /**
     * activity onResume事件
     */
    public void onResume() {
        if (entries != null) {
            for (PluginInterface plugin : entries.values()) {
                if (plugin != null) {
                    plugin.onActivityResume();
                }
            }
        }
    }

    /**
     * activity onPause事件
     */
    public void onPause() {
        if (entries != null) {
            for (PluginInterface plugin : entries.values()) {
                if (plugin != null) {
                    plugin.onActivityPause();
                }
            }
        }
    }

    /**
     * activity onResume事件
     */
    public void onStar() {
        if (entries != null) {
            for (PluginInterface plugin : entries.values()) {
                if (plugin != null) {
                    plugin.onActivityStart();
                }
            }
        }
    }

    public void onNewIntent(Intent intent) {
        if (entries != null) {
            for (PluginInterface plugin : entries.values()) {
                if (plugin != null) {
                    plugin.onActivityNewIntent(intent);
                }
            }
        }
    }

}

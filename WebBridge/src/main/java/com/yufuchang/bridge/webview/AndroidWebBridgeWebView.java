package com.yufuchang.bridge.webview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yufuchang.bridge.interfaces.AndroidWebBridgeCallBackInterface;
import com.yufuchang.bridge.plugin.JsInterface;
import com.yufuchang.bridge.plugin.PluginManager;
import com.yufuchang.bridge.utils.LogUtils;
import com.yufuchang.bridge.utils.ParseHtmlElementsUtils;

import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Method;

@SuppressLint("NewApi")
public class AndroidWebBridgeWebView extends WebView {
    public static final String USERAGENT = "Mozilla/5.0 (Linux; U; Android "
            + Build.VERSION.RELEASE + "; en-us; " + Build.MODEL
            + " Build/FRF91) AppleWebKit/533.1 "
            + "(KHTML, like Gecko) Version/4.0 Chrome/51.0.2704.81 Mobile Safari/533.1 ";
    private static final int SET_TITLE = 1;
    private static final int DIMISS_LOADING = 2;
    // js接口的名称
    private final String method = "bridge";
    // 判断当前webview是否被销毁
    public boolean destroyed = false;
    // 当前webview名称
    private String viewname;
    // 上下文
    private Context context;
    // webview属性类
    private WebSettings settings = this.getSettings();
    // 相关的url
    private String relativeUrl;
    private ProgressBar progressbar;
    private AndroidWebBridgeWebChromeClient androidWebBridgeWebChromeClient;
    private AndroidWebBridgeWebViewClient androidWebBridgeWebViewClient;
    private TextView titleText;
    private Handler handler;
    private FrameLayout frameLayout;
    private PluginManager pluginMgr;

    private AndroidWebBridgeCallBackInterface androidWebBridgeCallBackInterface;
    private int mLastMotionX;
    private int mLastMotionY;

    public AndroidWebBridgeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void setProperty(TextView titleText, FrameLayout layout, AndroidWebBridgeCallBackInterface androidWebBridgeCallBackInterface) {
        this.titleText = titleText;
        this.frameLayout = layout;
        this.androidWebBridgeCallBackInterface = androidWebBridgeCallBackInterface;
        this.setWebView();
        this.setWebSetting();
        handMessage();
        init();
    }

    private void handMessage() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SET_TITLE:
                        if (titleText != null) {
                            String title = (String) msg.obj;
                            titleText.setText(title);
                        }
                        break;
                    case DIMISS_LOADING:
                        if (androidWebBridgeCallBackInterface != null) {
                            androidWebBridgeCallBackInterface.onDissmissAndroidWebBridgeLoadingDialog();
                        }
                        break;
                    default:
                        break;
                }
            }
        };

    }

    //修改处
    public AndroidWebBridgeWebChromeClient getWebChromeClient() {
        return androidWebBridgeWebChromeClient;
    }

    //修改处
    public AndroidWebBridgeWebViewClient getAndroidWebBridgeWebViewClient() {
        return androidWebBridgeWebViewClient;
    }

    public void init() {
        initPlugin();
        this.addJavascriptInterface(new JsInterface(pluginMgr), method);
        //显示webview网页标题
        this.addJavascriptInterface(new GetContent(), "getContent");
        setDownloadListener(new MyWebViewDownLoadListener());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
        }

    }

    // 重置当前接口的webview
    private void initPlugin() {
        pluginMgr = new PluginManager(context, this);
        pluginMgr.setAndroidWebBridgeCallBackInterface(androidWebBridgeCallBackInterface);
    }

    public void onActivityStart() {
        if (pluginMgr != null) {
            pluginMgr.onStar();
        }
    }

    public void onActivityDestroy() {
        if (pluginMgr != null) {
            pluginMgr.onDestroy();
        }
    }

    public void onActivityResume() {
        if (pluginMgr != null) {
            pluginMgr.onResume();
        }
    }

    public void onActivityPause() {
        if (pluginMgr != null) {
            pluginMgr.onPause();
        }
    }

    public void onActivityNewIntent(Intent intent) {
        if (pluginMgr != null) {
            pluginMgr.onNewIntent(intent);
        }
    }

    public AndroidWebBridgeCallBackInterface getAndroidWebBridgeCallBackInterface() {
        return androidWebBridgeCallBackInterface;
    }

    /**
     * 设置webview
     */
    private void setWebView() {
        // 为0就是不给滚动条留空间，滚动条覆盖在网页上
        this.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        //this.setInitialScale(100);
        this.setInitialScale(0);
        setLayoutAnimation(null);
        setAnimation(null);
        setNetworkAvailable(true);

        this.setBackgroundColor(Color.WHITE);
        androidWebBridgeWebViewClient = new AndroidWebBridgeWebViewClient(androidWebBridgeCallBackInterface);
        this.setWebViewClient(androidWebBridgeWebViewClient);
        // 使WebView支持弹出框
        androidWebBridgeWebChromeClient = new AndroidWebBridgeWebChromeClient(context, this);
        this.setWebChromeClient(androidWebBridgeWebChromeClient);
        // 兼容2.3版本
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mLastMotionX = x;
                        mLastMotionY = y;
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mLastMotionX - 80 > event.getX()) {
                            loadUrl("javascript:if(window.showRight){window.showRight();}");
                        } else if (mLastMotionX < event.getX() - 80) {
                            loadUrl("javascript:if(window.showLeft){window.showLeft();}");
                        }
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void loadUrl(String url) {
        super.loadUrl(url);
        Runtime.getRuntime().gc();
    }

    /*
     * 测量webview高度和宽度
     */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        invalidate();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    // 设置websettings属性
    public void setWebSetting() {
        //基础设置，地理位置，缓存，userAgent等
        setBaseConfig();
        // 支持js相关方法
        setJSConfig();
        // 页面效果设置
        setPageStyle();
        setFontSize();
        // 本地安全设置
        setSecury();
        // API为16的方法访问
        Level16Apis.invoke(settings);
        // 允许ajax执行web请求
        Level4Apis.invoke(settings);
        // 支持html5数据库和使用缓存的功能
        Html5Apis htmlApi = new Html5Apis();
        htmlApi.invoke(settings);
    }

    /**
     * 基础设置
     */
    private void setBaseConfig() {
        // 代理字符串，如果字符串为空或者null系统默认字符串将被利用
//        String userAgent = USERAGENT + "/emmcloud/" + getVersion(context) + " ";
        String userAgent = USERAGENT + "/webbridge/" + getVersion(context) + " ";
//        String languageJson = PreferencesUtils.getString(
//                BaseApplication.getInstance(), BaseApplication.getInstance().getTanent() + "appLanguageObj");
//        if (languageJson != null) {
//            Language language = new Language(languageJson);
//            userAgent = userAgent + "lang/" + language.getIana();
//        }
        settings.setUserAgentString(userAgent);
        settings.enableSmoothTransition();
        settings.setGeolocationEnabled(true);
        String dir = context.getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setGeolocationDatabasePath(dir);
    }

    /* 支持js相关方法 */
    private void setJSConfig() {
        // 设置WebView的属性，此时可以去执行JavaScript脚本
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        //解决在安卓5.0以上跨域链接无法访问的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    /* 页面效果设置 */
    private void setPageStyle() {
        //设置自适应屏幕
        settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        // 支持自动加载图片
        settings.setLoadsImagesAutomatically(true);
        settings.setAllowFileAccess(true);
        // 支持多窗口
        settings.supportMultipleWindows();
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 设置webview推荐使用的窗口
        settings.setUseWideViewPort(true);
        // 页面适应手机屏幕的分辨率
        settings.setLoadWithOverviewMode(true);
        settings.setDefaultTextEncodingName("utf-8");//设置自适应屏幕
    }

    // 本地安全设置
    private void setSecury() {
        // 是否保存表单数据
        settings.setSaveFormData(false);
        // 是否保存密码
        settings.setSavePassword(false);
    }

//    // 点击back键回退，如果你处理了该事件，则返回true。如果你想允许事件要处理的下一个接收器，返回false
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        LogUtils.jasonDebug("onKeyUp------");
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            // 浏览记录含有历史记录，可以回退
//            if (this.canGoBack()) {
//                this.goBack();
//            }
//            return true;
//        }
//        return super.onKeyUp(keyCode, event);
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        LogUtils.jasonDebug("onKeyDown------");
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            return !(startOfHistory());
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    // 判断当前页面是否是首页
//    private boolean startOfHistory() {
//        WebBackForwardList currentList = this.copyBackForwardList();
//        WebHistoryItem item = currentList.getItemAtIndex(0);
//        // 如果item为空，表示webview尚未加载url
//        if (item != null) {
//            String url = item.getUrl();
//            String currentUrl = this.getUrl();
//            return currentUrl.equals(url);
//        }
//        return true;
//    }


    /**
     * 获取版本号
     **/
    public static String getVersion(Context context) {
        String versionCode = null;
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            // 截取正常版本号的方法
            versionCode = info.versionName;
            // versionCode = getNormalVersionCode(info.versionName);
        } catch (Exception e) {
            LogUtils.YfcDebug(e.toString());
        }
        return versionCode;
    }

    public void destroy() {
        this.destroyed = true;
        if (handler != null) {
            handler = null;
        }
        if (pluginMgr != null) {
            pluginMgr = null;
        }
        super.destroy();
    }

    public PluginManager getPluginMgr() {
        return pluginMgr;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // 设置字体大小
    private void setFontSize() {
        //修改处  此处默认字体大小为0
//		settings.setDefaultFontSize(DeviceInfo.getInstance().defaultFontSize);
//		settings.setDefaultFixedFontSize(DeviceInfo.getInstance().defaultFontSize);
        settings.setDefaultTextEncodingName("utf-8");
    }

    public String getRelativeUrl() {
        return relativeUrl;
    }

    public void setRelativeUrl(String relativeUrl) {
        this.relativeUrl = relativeUrl;
    }

    public String getViewname() {
        return viewname;
    }

    public void setViewname(String viewname) {
        this.viewname = viewname;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
//		LayoutParams lp = (LayoutParams) progressbar.getLayoutParams();
//		lp.x = l;
//		lp.y = t;
//		progressbar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }

    /**
     * API为16的方法访问
     */
    @TargetApi(16)
    private static class Level16Apis {
        static void invoke(WebSettings settings) {
            try {
                Method method = WebSettings.class.getMethod(
                        "setAllowFileAccessFromFileURLs",
                        boolean.class);
                method.invoke(settings, true);
            } catch (Exception e) {
                LogUtils.YfcDebug("设备api不支持：" + e.getMessage());
            }
        }
    }

    /**
     * API为16或以上，允许ajax执行web请求
     */
    @TargetApi(16)
    private static class Level4Apis {
        static void invoke(WebSettings settings) {
            try {
                Method method = WebSettings.class.getMethod(
                        "setAllowUniversalAccessFromFileURLs",
                        boolean.class);
                method.invoke(settings, true);
            } catch (Exception e) {
                LogUtils.YfcDebug( e.getMessage() + "");
            }
        }
    }

    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
            try {
                JSONObject object = new JSONObject();
                object.put("url", url);
                pluginMgr.execute("FileTransferService", "download", object.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public class GetContent {
        @JavascriptInterface
        public void onGetHtmlContent(String html) {
            Elements elements = ParseHtmlElementsUtils.getDataFromHtml(html, "meta");
            boolean isWebControlLoading = false;
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
            }
            if (!isWebControlLoading) {
                handler.sendEmptyMessage(DIMISS_LOADING);
            }
        }

        @JavascriptInterface
        public void onHashChangeEvent() {
        }
    }

    /**
     * 支持html5数据库和使用缓存的功能
     */
    private class Html5Apis {
        void invoke(WebSettings settings) {
            try {
                // 数据库路径
                String databasePath = context.getDir("database", 0).getPath();
                // 使用localStorage则必须打开
                settings.setDomStorageEnabled(true);
                // 是否允许数据库存储的api
                settings.setDatabaseEnabled(true);
                // 设置数据库路径
                settings.setDatabasePath(databasePath);

                // webview加载 服务端的网页，为了减少访问压力，用html5缓存技术
                settings.setAppCacheEnabled(true);
                // 设置加载cache的方式，
                settings.setCacheMode(WebSettings.LOAD_DEFAULT);
                // 设置缓存路径
                settings.setAppCachePath(databasePath);
                // 设置缓冲大小,此处设置为缓存最大为8m
                settings.setAppCacheMaxSize(1024 * 1024 * 8);

            } catch (Exception e) {
                LogUtils.YfcDebug( e.getMessage() + "");
            }
        }
    }


}

package com.yufuchang.bridge.webview;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import com.yufuchang.bridge.interfaces.AndroidWebBridgeCallBackInterface;
import com.yufuchang.bridge.utils.LogUtils;
import com.yufuchang.bridge.utils.PreferencesUtils;

import java.util.HashMap;
import java.util.Map;


public class AndroidWebBridgeWebViewClient extends WebViewClient {
    private AndroidWebBridgeWebView myWebView;
    private Handler mHandler = null;
    private Runnable runnable = null;
    private AndroidWebBridgeCallBackInterface androidWebBridgeCallBackInterface;
    private String url = "";
    private boolean isLogin = false;

    public AndroidWebBridgeWebViewClient(AndroidWebBridgeCallBackInterface androidWebBridgeCallBackInterface) {
        this.androidWebBridgeCallBackInterface = androidWebBridgeCallBackInterface;
        handMessage();
        initRunnable();
    }

    private void handMessage() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        myWebView.reload();
                        break;
                    default:
                        break;

                }
            }
        };
    }

    private void initRunnable() {
        runnable = new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(1);
            }
        };
    }

    /*
     * 开始加载网页的操作
     */
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        this.url = url;
        LogUtils.YfcDebug("开始加载："+url);
        myWebView = (AndroidWebBridgeWebView) view;
        if (runnable != null && url.startsWith("http://baoxiao.inspur.com")) {
            mHandler.postDelayed(runnable, 2000);
        }
    }

    /*
     * 网页加载成功
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        if (runnable != null) {
            mHandler.removeCallbacks(runnable);
            runnable = null;
        }
        if (androidWebBridgeCallBackInterface != null) {
            androidWebBridgeCallBackInterface.onInitWebViewGoBackOrClose();
        }
        AndroidWebBridgeWebView webview = (AndroidWebBridgeWebView) view;
        if (webview.destroyed) {
            return;
        }
        webview.setVisibility(View.VISIBLE);
        //为了获取网页的html内容
        String script = "javascript:window.getContent.onGetHtmlContent("
                + "document.getElementsByTagName('html')[0].innerHTML" + ");";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                }
            });
        } else {
            view.loadUrl(script);
        }
        String c = CookieManager.getInstance().getCookie(url);
        PreferencesUtils.putString(view.getContext(), "web_cookie", c);
        CookieSyncManager.getInstance().sync();
    }

    /*
     * 网页加载失败，取消加载，并清理当前的view
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {
        LogUtils.YfcDebug("加载失败："+failingUrl);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return;
        }
        if (runnable != null) {
            mHandler.removeCallbacks(runnable);
            runnable = null;
        }
        if (androidWebBridgeCallBackInterface != null) {
            androidWebBridgeCallBackInterface.onDissmissAndroidWebBridgeLoadingDialog();
            androidWebBridgeCallBackInterface.showLoadFailLayout(failingUrl, description);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        LogUtils.YfcDebug("加载失败："+error.getErrorCode());
        LogUtils.YfcDebug("加载失败："+error.getDescription());
        // 在这里加上个判断,防止资源文件等错误导致显示错误页
        if (request.isForMainFrame() && request.getUrl().toString().equals(url)) {
            if (runnable != null) {
                mHandler.removeCallbacks(runnable);
                runnable = null;
            }
            if (androidWebBridgeCallBackInterface != null) {
                androidWebBridgeCallBackInterface.onDissmissAndroidWebBridgeLoadingDialog();
                androidWebBridgeCallBackInterface.showLoadFailLayout(url, error.getDescription().toString());
            }
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
    }


    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, final WebResourceRequest request) {
        if (runnable != null) {
            mHandler.removeCallbacks(runnable);
            runnable = null;
        }
        if (!filterUrl(request.getUrl().toString(), view)) {
            LogUtils.YfcDebug("111111111111");
            WebResourceRequest newRequest = new WebResourceRequest() {
                @Override
                public Uri getUrl() {
                    return request.getUrl();
                }

                @Override
                public boolean isForMainFrame() {
                    return request.isForMainFrame();
                }

                @Override
                public boolean isRedirect() {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        return request.isRedirect();
                    }
                    return false;
                }

                @Override
                public boolean hasGesture() {
                    return request.hasGesture();
                }

                @Override
                public String getMethod() {
                    return request.getMethod();
                }

                @Override
                public Map<String, String> getRequestHeaders() {
                    return getWebViewHeaders(request.getUrl().toString());
                }
            };
            return super.shouldOverrideUrlLoading(view, newRequest);
        }
        return true;


    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return false;
        }
        if (runnable != null) {
            mHandler.removeCallbacks(runnable);
            runnable = null;
        }
        if (!filterUrl(url, view)) {
            view.loadUrl(url, getWebViewHeaders(url));
        }
        return true;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        super.doUpdateVisitedHistory(view, url, isReload);
        if (isLogin) {
            view.clearHistory();
            isLogin = false;
        }
    }

    /**
     * 过滤url
     *
     * @return 是否被过滤掉
     */
    private boolean filterUrl(String url, WebView webView) {
        if (!url.startsWith("http") && !url.startsWith("ftp")) {
            try {
                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                intent.setComponent(null);
                webView.getContext().startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * 获取Header
     *
     * @return
     */
    private Map<String, String> getWebViewHeaders(String url) {
        return myWebView == null ? new HashMap<String, String>() : ((androidWebBridgeCallBackInterface != null) ? androidWebBridgeCallBackInterface.onGetWebViewHeaders(url) : new HashMap<String, String>());
    }
}

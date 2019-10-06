package com.yufuchang.bridge.webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.yufuchang.bridge.interfaces.AndroidWebBridgeCallBackInterface;
import com.yufuchang.bridge.utils.Res;
import com.yufuchang.permission.PermissionRequestCallback;
import com.yufuchang.permission.PermissionRequestManagerUtils;
import com.yufuchang.permission.Permissions;

import java.util.List;


public class AndroidWebBridgeWebChromeClient extends WebChromeClient {

    // File Chooser
    public static final int FILE_CHOOSER_RESULT_CODE = 5173;
    private Context context;
    private ValueCallback<Uri> mUploadMessage;// 回调图片选择，4.4以下
    private ValueCallback<Uri[]> mUploadCallbackAboveL;// 回调图片选择，5.0以上
    private AndroidWebBridgeWebView mWebView;
    private View customView;
    private FrameLayout fullscreenContainer;
    private CustomViewCallback customViewCallback;
//    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    public AndroidWebBridgeWebChromeClient(Context context, AndroidWebBridgeWebView webView) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.mWebView = webView;
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(context, Permissions.LOCATION, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                callback.invoke(origin, true, false);
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                callback.invoke(origin, false, false);
            }
        });
    }


    @Override
    public void onCloseWindow(WebView window) {
        super.onCloseWindow(window);
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean dialog,
                                  boolean userGesture, Message resultMsg) {
        return super.onCreateWindow(view, dialog, userGesture, resultMsg);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        showCustomView(view, callback);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//播放时横屏幕，如果需要改变横竖屏，只需该参数就行了
    }

    @Override
    public void onHideCustomView() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        hideCustomView();
    }

    /**
     * 视频播放全屏
     *
     * @param view
     * @param callback
     */
    private void showCustomView(View view, CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }
        getActivity().getWindow().getDecorView();
        FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
        fullscreenContainer = new FullscreenHolder(getActivity());
        FrameLayout.LayoutParams containerLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        decor.addView(fullscreenContainer, containerLayoutParams);
        FrameLayout.LayoutParams viewLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fullscreenContainer.addView(view, viewLayoutParams);
        customView = view;
        setStatusBarVisibility(false);
        customViewCallback = callback;
        mWebView.setVisibility(View.GONE);
    }

    /**
     * 退出视频播放全屏
     *
     * @return 是否消费掉返回键
     */
    public boolean hideCustomView() {
        if (customView == null) {
            return false;
        }

        setStatusBarVisibility(true);
        FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;
        customViewCallback.onCustomViewHidden();
        mWebView.setVisibility(View.VISIBLE);
        return true;
    }

    private void setStatusBarVisibility(boolean visible) {
        int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getActivity().getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 转化为Activity
     *
     * @return
     */
    private Activity getActivity() {
        if (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }

        if (context instanceof Activity) {
            return (Activity) context;
        }
        return null;
    }

    /**
     * 覆盖默认的window.alert展示界面
     */
    public boolean onJsAlert(WebView view, String url, String message,
                             final JsResult result) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                view.getContext(), AlertDialog.THEME_HOLO_LIGHT);

        builder.setTitle(Res.getStringID("msg_title")).setMessage(message);

        builder.setPositiveButton(Res.getStringID("file_ok"),
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }

                });
        // 禁止取消按钮
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        if (context != null) {
            dialog.show();
        }
        return true;
    }

    public boolean onJsBeforeUnload(WebView view, String url, String message,
                                    JsResult result) {
        return super.onJsBeforeUnload(view, url, message, result);
    }

    /**
     * 覆盖默认的window.confirm
     */
    public boolean onJsConfirm(WebView view, String url, String message,
                               final JsResult result) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                view.getContext(), AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle(Res.getStringID("msg_makesure"))
                .setMessage(message)
                .setPositiveButton(Res.getStringID("file_ok"),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                result.confirm();
                            }
                        })
                .setNeutralButton(Res.getStringID("file_cancel"),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                result.cancel();
                            }
                        });
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                result.cancel();
            }
        });

        // 监听对话框的点击事件
        builder.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
//                iLog.v("onJsConfirm", "keyCode==" + keyCode + "event=" + event);
                return true;
            }
        });
        // 禁止响应按back键的事件
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    /**
     * 覆盖默认的window.prompt
     */
    public boolean onJsPrompt(WebView view, String url, String message,
                              String defaultValue, final JsPromptResult result) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                view.getContext(), AlertDialog.THEME_HOLO_LIGHT);

        builder.setTitle(Res.getStringID("edit")).setMessage(message);

        final EditText et = new EditText(view.getContext());
        et.setSingleLine();
        et.setText(defaultValue);
        builder.setView(et)
                .setPositiveButton(Res.getStringID("file_ok"),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                result.confirm(et.getText().toString());
                            }

                        })
                .setNeutralButton(Res.getStringID("file_cancel"),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                result.cancel();
                            }
                        });

        // 监听对话框的点击事件
        builder.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
//                iLog.v("onJsPrompt", "keyCode==" + keyCode + "event=" + event);
                return true;
            }
        });

        // 禁止响应按back键的事件
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    /****** 修改处，web页面可以直接获取文件 *******/
    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        this.openFileChooser(uploadMsg, "*/*");
    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        this.openFileChooser(uploadMsg, acceptType, null);
    }

    // For Android 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                String acceptType, String capture) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        if (TextUtils.isEmpty(capture)) {
            acceptType = "*/*";
        }
        i.setType(acceptType);
        if (mWebView.getAndroidWebBridgeCallBackInterface() != null) {
            mWebView.getAndroidWebBridgeCallBackInterface().onStartActivityForResult(Intent.createChooser(i, "File Browser"), FILE_CHOOSER_RESULT_CODE);
        }
    }

    // For Android 5.0+
    @SuppressLint("NewApi")
    public boolean onShowFileChooser(WebView webView,
                                     ValueCallback<Uri[]> filePathCallback,
                                     FileChooserParams fileChooserParams) {
        mUploadCallbackAboveL = filePathCallback;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        String type = "*/*";
        if (fileChooserParams != null
                && fileChooserParams.getAcceptTypes() != null
                && fileChooserParams.getAcceptTypes().length > 0) {
            if (!TextUtils.isEmpty(fileChooserParams.getAcceptTypes()[0])) {
                type = fileChooserParams.getAcceptTypes()[0];
            }
        }
        i.setType(type);

        if (mWebView.getAndroidWebBridgeCallBackInterface() != null) {
            mWebView.getAndroidWebBridgeCallBackInterface().onStartActivityForResult(Intent.createChooser(i, "File Browser"), FILE_CHOOSER_RESULT_CODE);
        }
        return true;
    }

    public ValueCallback<Uri> getValueCallback() {
        return mUploadMessage;
    }

    public ValueCallback<Uri[]> getValueCallbackAboveL() {
        return mUploadCallbackAboveL;
    }

    /*
     * 根据网页加载速度更改进度条显示进度
     */
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        AndroidWebBridgeCallBackInterface androidWebBridgeCallBackInterface = mWebView.getAndroidWebBridgeCallBackInterface();
        if (androidWebBridgeCallBackInterface != null) {
            androidWebBridgeCallBackInterface.onProgressChanged(newProgress);
        }
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        super.onReceivedIcon(view, icon);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        if (null != title && !getRemoveHttpUrl(title).equals(getRemoveHttpUrl(view.getUrl())) && !getRemoveHttpUrl(title).equals(getRemoveHttpUrl(view.getOriginalUrl()))) {
            AndroidWebBridgeCallBackInterface androidWebBridgeCallBackInterface = mWebView.getAndroidWebBridgeCallBackInterface();
            if (androidWebBridgeCallBackInterface != null) {
                androidWebBridgeCallBackInterface.onSetTitle(title);
            }
        }
    }

    @Override
    public void onRequestFocus(WebView view) {
        super.onRequestFocus(view);
    }

    public String getRemoveHttpUrl(String url) {
        return url.replace("http://", "").replace("https://", "").trim();
    }

    /**
     * 全屏容器界面
     */
    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }


}

package com.yufuchang.bridge.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.yufuchang.bridge.R;
import com.yufuchang.bridge.interfaces.AndroidWebBridgeCallBackInterface;
import com.yufuchang.bridge.interfaces.OnKeyDownListener;
import com.yufuchang.bridge.plugin.PluginManager;
import com.yufuchang.bridge.utils.Constant;
import com.yufuchang.bridge.utils.DensityUtil;
import com.yufuchang.bridge.utils.LogUtils;
import com.yufuchang.bridge.utils.Res;
import com.yufuchang.bridge.utils.StringUtils;
import com.yufuchang.bridge.webview.AndroidWebBridgeWebView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yufuchang on 2018/7/9.
 */

public class AndroidWebBridgeFragment extends AndroidWebBridgeBaseFragment {
    // 浏览文件resultCode
    public static final int CAMERA_SERVICE_CAMERA_REQUEST = 1;
    public static final int CAMERA_SERVICE_GALLERY_REQUEST = 2;
    public static final int PHOTO_SERVICE_CAMERA_REQUEST = 3;
    public static final int PHOTO_SERVICE_GALLERY_REQUEST = 4;
    public static final int SELECT_STAFF_SERVICE_REQUEST = 5;
    public static final int FILE_SERVICE_REQUEST = 6;
    public static final int DO_NOTHING_REQUEST = 7;
    public static final int BARCODE_SERVER__SCAN_REQUEST = 8;
    public static final int SELECT_FILE_SERVICE_REQUEST = 9;
    public static final int REQUEST_CODE_RECORD_VIDEO = 10;
    public static final int FILE_CHOOSER_RESULT_CODE = 5173;
    private static final String JAVASCRIPT_PREFIX = "javascript:";
    private static String EXTRA_OUTSIDE_URL = "extra_outside_url";
    private static String EXTRA_OUTSIDE_URL_REQUEST_RESULT = "extra_outside_url_request_result";
    private AndroidWebBridgeWebView webView;
    private Map<String, String> webViewHeaders;
    private LinearLayout loadFailLayout;
    private Button normalBtn, middleBtn, bigBtn, biggestBtn;
    private String appId = "";
    private FrameLayout frameLayout;
    private RelativeLayout loadingLayout;
    private TextView loadingText;
    private String helpUrl = "";
    private HashMap<String, String> urlTilteMap = new HashMap<>();
    private View rootView;

    private String appName = "";
    private String version;
    private AndroidWebBridgeFragmentClickListener listener;
    private RelativeLayout headerLayout;
    private AndroidWebBridgeCallBackInterface androidWebBridgeCallBackInterface;
    private OnKeyDownListener onKeyDownListener;
    private boolean isStaticWebTitle = false;
    //错误url和错误信息
    private String errorUrl = "";
    private String errorDescription = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        rootView = LayoutInflater.from(getActivity()).inflate(Res.getLayoutID("fragment_android_web_bridge"), null);
        initViews();
        version = getArguments().getString(Constant.WEB_FRAGMENT_VERSION, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView == null) {
            rootView = inflater.inflate(Res.getLayoutID("fragment_android_web_bridge"), container,
                    false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        if (!version.equals(getArguments().getString(Constant.WEB_FRAGMENT_VERSION, ""))) {
            initFragmentViews();
        }
        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
        webView.onActivityStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onActivityResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onActivityPause();
    }

    protected void onNewIntent(Intent intent) {
        webView.onActivityNewIntent(intent);
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        //防止以后扩展其他Activity时，忘记设置相关参数造成崩溃
        if (getArguments() == null) {
            setArguments(new Bundle());
        }
        appName = getArguments().getString(Constant.WEB_FRAGMENT_APP_NAME);
        isStaticWebTitle = getArguments().getBoolean(Constant.Web_STATIC_TITLE, false);
        headerLayout = rootView.findViewById(Res.getWidgetID("rl_header"));
        loadingLayout = rootView.findViewById(Res.getWidgetID("rl_loading"));
        loadingText = rootView.findViewById(Res.getWidgetID("tv_loading"));
        frameLayout = rootView.findViewById(Res.getWidgetID("videoContainer"));
        loadFailLayout = rootView.findViewById(Res.getWidgetID("load_error_layout"));
        webView = rootView.findViewById(Res.getWidgetID("webview"));
        headerText = rootView.findViewById(Res.getWidgetID("header_text"));
        functionLayout = rootView.findViewById(Res.getWidgetID("function_layout"));
        webFunctionLayout = rootView.findViewById(Res.getWidgetID("ll_web_function"));
        if (isStaticWebTitle) {
            rootView.findViewById(R.id.ibt_back).setVisibility(View.GONE);
            rootView.findViewById(R.id.imp_close_btn).setVisibility(View.GONE);
        }
        showLoadingDlg("");
        if (!StringUtils.isBlank(getArguments().getString("help_url"))) {
            String helpUrl = getArguments().getString("help_url");
            if (!StringUtils.isBlank(helpUrl)) {
                this.helpUrl = helpUrl;
            }
        }
        if (!StringUtils.isBlank(getArguments().getString("appId"))) {
            appId = getArguments().getString("appId");
        }
        initFragmentViews();
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(isStaticWebTitle ?
                (RelativeLayout.ALIGN_PARENT_LEFT | RelativeLayout.CENTER_VERTICAL) : RelativeLayout.CENTER_IN_PARENT);
        headerText.setLayoutParams(layoutParams);
        if (isStaticWebTitle) {
            headerText.setPadding(DensityUtil.dip2px(getActivity(), 15), 0, 0, 0);
        } else {
            headerText.setTextSize(17);
        }
        headerText.setText(StringUtils.isBlank(appName) ? "" : appName);
    }


    /**
     * 初始化Fragment的WebView
     */
    private void initFragmentViews() {
        String url = getArguments().getString(Constant.APP_WEB_URI);
        LogUtils.YfcDebug("uri:"+url);
//        setWebViewFunctionVisiable();
        initHeaderOptionMenu();
        initListeners();
        initWebViewHeaderLayout();
        setWebViewHeader(url);
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });
        webView.loadUrl(url, webViewHeaders);
    }

    /**
     * 初始化监听器
     */
    private void initListeners() {
        listener = new AndroidWebBridgeFragmentClickListener();
        rootView.findViewById(R.id.imp_change_font_size_btn).setOnClickListener(listener);
        rootView.findViewById(R.id.ibt_back).setOnClickListener(listener);
        rootView.findViewById(R.id.imp_close_btn).setOnClickListener(listener);
        rootView.findViewById(R.id.tv_look_web_error_detail).setOnClickListener(listener);
        rootView.findViewById(R.id.tv_reload_web).setOnClickListener(listener);
    }

    /**
     * 执行JS脚本
     *
     * @param script
     */
    @Override
    protected void runJavaScript(String script) {
        webView.loadUrl(script);
    }

    /**
     * 在WebClient获取header
     * 为了防止第一层不符合规则，第二层符合添加token规则时不再检查url的问题，需要回传url重新检查增加每次检查是否需要加token
     *
     * @return
     */
    public Map<String, String> getWebViewHeaders(String url) {
//        addAuthorizationToken(url);
        return webViewHeaders;
    }

//    /**
//     * 设置Webview自定义功能是否显示
//     */
//    private void setWebViewFunctionVisiable() {
//        int isZoomable = getArguments().getInt("is_zoomable", 0);
//        if (isZoomable == 1 || !StringUtils.isBlank(helpUrl)) {
//            rootView.findViewById(R.id.imp_change_font_size_btn).setVisibility(View.VISIBLE);
//        }
//        if (isZoomable == 1) {
//            int textSize = PreferencesByUsersUtils.getInt(getActivity(), "app_crm_font_size_" + appId, MyAppWebConfig.NORMAL);
//            webView.getSettings().setTextZoom(textSize);
//        }
//    }

    /**
     * 初始化webview haader layout
     */
    private void initWebViewHeaderLayout() {
        androidWebBridgeCallBackInterface = getAndroidWebBridgeCallBackInterface();
        if (getArguments().getBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, true)) {
            String title = getArguments().getString(Constant.WEB_FRAGMENT_APP_NAME);
            headerText.setOnClickListener(new AndroidWebBridgeFragmentClickListener());
            webView.setProperty(headerText, frameLayout, androidWebBridgeCallBackInterface);
            initWebViewGoBackOrClose();
            headerLayout.setVisibility(View.VISIBLE);
            headerText.setText(title);
        } else {
            webView.setProperty(null, frameLayout, androidWebBridgeCallBackInterface);
        }
    }


    /**
     * 与主Fragment通信的接口
     *
     * @return
     */
    private AndroidWebBridgeCallBackInterface getAndroidWebBridgeCallBackInterface() {
        return new AndroidWebBridgeCallBackInterface() {
            @Override
            public void onDissmissAndroidWebBridgeLoadingDialog() {
                dimissLoadingDlg();
            }

            @Override
            public void onShowAndroidWebBridgeDialog() {
                showAndroidWebBridgeDialog();
            }

            @Override
            public Map<String, String> onGetWebViewHeaders(String url) {
                return getWebViewHeaders(url);
            }

            @Override
            public void onInitWebViewGoBackOrClose() {
                initWebViewGoBackOrClose();
            }

            @Override
            public void onSetTitle(String title) {
                if (!isStaticWebTitle) {
                    setTitle(title);
                }
            }

            @Override
            public void onFinishActivity() {
                finishActivity();
            }

            @Override
            public void onLoadingDlgShow(String content) {
                showLoadingDlg(content);
            }

            @Override
            public void onStartActivityForResult(Intent intent, int requestCode) {
                startActivityForResult(intent, requestCode);
            }

            @Override
            public void onStartActivityForResult(String routerPath, Bundle bundle, int requestCode) {
                //ARouter不支持fragment.startActivityForResult().
//                Postcard postcard = ARouter.getInstance().build(routerPath).with(bundle);
//                LogisticsCenter.completion(postcard);
//                Intent intent = new Intent(getActivity(), postcard.getDestination());
//                intent.putExtras(postcard.getExtras());
//                startActivityForResult(intent, requestCode);


//                ARouter.getInstance().build(routerPath).with(bundle).navigation(AndroidWebBridgeFragment.this.getActivity(),requestCode);
            }

            @Override
            public void onProgressChanged(int newProgress) {
            }


            @Override
            public void setOnKeyDownListener(OnKeyDownListener onKeyDownListener) {
                AndroidWebBridgeFragment.this.onKeyDownListener = onKeyDownListener;
            }

//            @Override
//            public boolean isWebFromIndex() {
//                return isStaticWebTitle;
//            }

            @Override
            public void showLoadFailLayout(String url, String description) {
                errorUrl = url;
                errorDescription = description;
                if (loadFailLayout != null) {
                    loadFailLayout.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    /**
     * 初始化原生WebView的返回和关闭
     * （不是GS应用，GS应用有重定向，不容易实现返回）
     */
    public void initWebViewGoBackOrClose() {
        if (webView != null) {
            if (getActivity().getClass().getName().equals(AndroidWebBridgeActivity.class.getName())) {
                (rootView.findViewById(Res.getWidgetID("imp_close_btn"))).
                        setVisibility(webView.canGoBack() && !webView.getAndroidWebBridgeWebViewClient().isLogin() ? View.VISIBLE : View.GONE);
            }
            setHeaderTextWidth();
        }
    }

    public void setTitle(String title) {
        if (!StringUtils.isBlank(title)) {
            urlTilteMap.put(webView.getUrl(), title);
            headerText.setText(title);
        }
    }

    /**
     * 解决有的机型Webview goback时候不会获取title的问题
     */
    private void setGoBackTitle() {
        String title = urlTilteMap.get(webView.getUrl());
        if (!StringUtils.isBlank(title)) {
            headerText.setText(title);
        }
    }

    /**
     * 返回
     */
    public boolean onBackKeyDown() {
        if (AndroidWebBridgeFragment.this.onKeyDownListener != null) {
            AndroidWebBridgeFragment.this.onKeyDownListener.onBackKeyDown();
        } else {
            if (!webView.getWebChromeClient().hideCustomView()) {
                if (webView.canGoBack()) {
                    webView.goBack();// 返回上一页面
                    setGoBackTitle();
                } else {
                    finishActivity();
                }
            }

        }
        return true;
    }

    public void finishActivity() {
        webView.onActivityDestroy();
        getActivity().finish();// 退出程序
    }

    /**
     * 设置WebView的Header参数
     */
    private void setWebViewHeader(String url) {
        webViewHeaders = new HashMap<>();
//        addAuthorizationToken(url);
//        if (BaseApplication.getInstance().getCurrentEnterprise() != null) {
//            webViewHeaders.put("X-ECC-Current-Enterprise", BaseApplication.getInstance().getCurrentEnterprise().getId());
//        }
//        webViewHeaders.put("Accept-Language", LanguageManager.getInstance().getCurrentAppLanguage());
    }

//    /**
//     * 根据规则添加token
//     * 当URL主域名是Constant.INSPUR_HOST_URL
//     * 或者Constant.INSPURONLINE_HOST_URL结尾时添加token
//     */
//    private void addAuthorizationToken(String url) {
//        try {
//            URL urlHost = new URL(url);
//            String token = BaseApplication.getInstance().getToken();
//            if (token != null && (urlHost.getHost().endsWith(Constant.INSPUR_HOST_URL)) || urlHost.getHost().endsWith(Constant.INSPURONLINE_HOST_URL) || urlHost.getPath().endsWith("/app/mdm/v3.0/loadForRegister")) {
//                webViewHeaders.put("Authorization", token);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    /**
//     * 打开修改字体的dialog
//     */
//    private void showChangeFontSizeDialog() {
//        View view = getActivity().getLayoutInflater().inflate(R.layout.app_imp_crm_font_dialog, null);
//        Dialog dialog = new Dialog(getActivity(), R.style.transparentFrameWindowStyle);
//        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT));
//        if (!StringUtils.isBlank(helpUrl)) {
//            initHelpUrlViews(dialog, view);
//        }
//        initFontSizeDialogViews(view);
//        Window window = dialog.getWindow();
//        // 设置显示动画
//        window.setWindowAnimations(R.style.main_menu_animstyle);
//        WindowManager.LayoutParams wl = window.getAttributes();
//        wl.x = 0;
//        wl.y = getActivity().getWindowManager().getDefaultDisplay().getHeight();
//        // 以下这两句是为了保证按钮可以水平满屏
//        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
//        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//        // 设置Dialog的透明度
//        wl.dimAmount = 0.31f;
//        dialog.getWindow().setAttributes(wl);
//        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        // 设置显示位置
//        dialog.onWindowAttributesChanged(wl);
//        // 设置点击外围解散
//        dialog.setCanceledOnTouchOutside(true);
//
//        view.findViewById(R.id.app_imp_crm_font_normal_btn).setOnClickListener(listener);
//        view.findViewById(R.id.app_imp_crm_font_middle_btn).setOnClickListener(listener);
//        view.findViewById(R.id.app_imp_crm_font_big_btn).setOnClickListener(listener);
//        view.findViewById(R.id.app_imp_crm_font_biggest_btn).setOnClickListener(listener);
//
//        if (getArguments().getInt("is_zoomable", 0) == 1) {
//            setWebViewButtonTextColor(0);
//        }
//        dialog.show();
//    }

//    /**
//     * 初始化帮助view
//     */
//    private void initHelpUrlViews(final Dialog dialog, View view) {
//        view.findViewById(R.id.app_imp_crm_help_layout).setVisibility(View.VISIBLE);
//        view.findViewById(R.id.app_news_share_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(getActivity(), AndroidWebBridgeActivity.class);
//                intent.putExtra(Constant.APP_WEB_URI, helpUrl);
//                intent.putExtra(Constant.WEB_FRAGMENT_APP_NAME, "");
//                startActivity(intent);
//                dialog.dismiss();
//            }
//        });
//    }

//    /**
//     * 初始化Dialog的Views
//     *
//     * @param view
//     */
//    private void initFontSizeDialogViews(View view) {
//        if (getArguments().getInt("is_zoomable", 0) == 1) {
//            view.findViewById(R.id.app_imp_crm_font_text).setVisibility(View.VISIBLE);
//            view.findViewById(R.id.app_imp_crm_font_layout).setVisibility(View.VISIBLE);
//            normalBtn = (Button) view.findViewById(R.id.app_imp_crm_font_normal_btn);
//            normalBtn.setText(getString(R.string.news_font_normal));
//            middleBtn = (Button) view.findViewById(R.id.app_imp_crm_font_middle_btn);
//            middleBtn.setText(getString(R.string.news_font_middle));
//            bigBtn = (Button) view.findViewById(R.id.app_imp_crm_font_big_btn);
//            bigBtn.setText(getString(R.string.news_font_big_text));
//            biggestBtn = (Button) view.findViewById(R.id.app_imp_crm_font_biggest_btn);
//            biggestBtn.setText(getString(R.string.news_font_biggest_text));
//        }
//
//    }

//    /**
//     * 改变WebView字体大小
//     *
//     * @param textZoom
//     */
//    private void setNewsFontSize(int textZoom) {
//        WebSettings webSettings = webView.getSettings();
//        PreferencesByUsersUtils.putInt(getActivity(), "app_crm_font_size_" + appId, textZoom);
//        webSettings.setTextZoom(textZoom);
//        setWebViewButtonTextColor(textZoom);
//    }

//    /**
//     * 初始化WebView的字体大小
//     */
//    private void setWebViewButtonTextColor(int textZoom) {
//        int textSize = PreferencesByUsersUtils.getInt(getActivity(), "app_crm_font_size_" + appId, MyAppWebConfig.NORMAL);
//        if (textZoom != 0) {
//            textSize = textZoom;
//        }
//        int lightModeFontColor = ContextCompat.getColor(getActivity(), R.color.app_dialog_day_font_color);
//        int blackFontColor = ContextCompat.getColor(getActivity(), R.color.black);
//        normalBtn.setTextColor((textSize == MyAppWebConfig.NORMAL) ? lightModeFontColor : blackFontColor);
//        middleBtn.setTextColor((textSize == MyAppWebConfig.CRM_BIG) ? lightModeFontColor : blackFontColor);
//        bigBtn.setTextColor((textSize == MyAppWebConfig.CRM_BIGGER) ? lightModeFontColor : blackFontColor);
//        biggestBtn.setTextColor((textSize == MyAppWebConfig.CRM_BIGGEST) ? lightModeFontColor : blackFontColor);
//    }

    /**
     * 弹出提示框
     */
    public void showAndroidWebBridgeDialog() {
        Toast.makeText(getActivity(), "调用错误",Toast.LENGTH_SHORT);
    }

    @Override
    public void onDestroy() {
        if (webView != null) {
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        androidWebBridgeCallBackInterface = null;
        //清除掉图片缓存
//        DataCleanManager.cleanCustomCache(MyAppConfig.LOCAL_IMG_CREATE_PATH);
        super.onDestroy();
    }

    public void showLoadingDlg(String content) {
        if (StringUtils.isBlank(content)) {
            loadingText.setVisibility(View.GONE);
        } else {
            loadingText.setText(content);
            loadingText.setVisibility(View.VISIBLE);
        }
        loadingLayout.setVisibility(View.VISIBLE);
    }

    public void dimissLoadingDlg() {
        loadingLayout.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            Uri uri = data == null || resultCode != Activity.RESULT_OK ? null
                    : data.getData();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                ValueCallback<Uri[]> mUploadCallbackAboveL = webView
                        .getWebChromeClient().getValueCallbackAboveL();
                if (null == mUploadCallbackAboveL) {
                    return;
                }
                if (uri == null) {
                    mUploadCallbackAboveL.onReceiveValue(null);
                } else {
                    Uri[] uris = new Uri[]{uri};
                    mUploadCallbackAboveL.onReceiveValue(uris);
                }
                mUploadCallbackAboveL = null;
            } else {
                ValueCallback<Uri> mUploadMessage = webView
                        .getWebChromeClient().getValueCallback();
                if (null == mUploadMessage) {
                    return;
                }
                mUploadMessage.onReceiveValue(uri);
                mUploadMessage = null;
            }
        } else {
            PluginManager pluginMgr = webView.getPluginMgr();

//            if (pluginMgr != null) {
//                String serviceName = "";
//                switch (requestCode) {
//                    case CAMERA_SERVICE_CAMERA_REQUEST:
//                    case CAMERA_SERVICE_GALLERY_REQUEST:
//                        serviceName = CameraService.class.getCanonicalName().trim();
//                        break;
//                    case PHOTO_SERVICE_CAMERA_REQUEST:
//                    case PHOTO_SERVICE_GALLERY_REQUEST:
//                        serviceName = PhotoService.class.getCanonicalName().trim();
//                        break;
//                    case SELECT_STAFF_SERVICE_REQUEST:
//                        serviceName = SelectStaffService.class.getCanonicalName().trim();
//                        break;
//                    case BARCODE_SERVER__SCAN_REQUEST:
//                        serviceName = BarCodeService.class.getCanonicalName().trim();
//                        break;
//                    case SELECT_FILE_SERVICE_REQUEST:
//                        serviceName = FileTransferService.class.getCanonicalName().trim();
//                        break;
//                    case REQUEST_CODE_RECORD_VIDEO:
//                        serviceName = VideoService.class.getCanonicalName();
//                        break;
//                    default:
//                        break;
//                }
//                if (!StringUtils.isBlank(serviceName)) {
//                    PluginInterface plugin = pluginMgr.getPlugin(serviceName);
//                    if (plugin != null) {
//                        plugin.onActivityResult(requestCode, resultCode, data);
//                    }
//
//                }
//
//            }
        }


    }

    class AndroidWebBridgeFragmentClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int i = v.getId();
           if (i == R.id.ibt_back) {
                if (webView.canGoBack()) {
                    webView.goBack();// 返回上一页面
                    setGoBackTitle();
                } else {
                    finishActivity();
                }

            } else if (i == R.id.imp_close_btn) {
                finishActivity();

            }  else if (i == R.id.tv_reload_web) {
                showLoadingDlg("加载失败");
                webView.reload();
                webView.setVisibility(View.INVISIBLE);
                loadFailLayout.setVisibility(View.GONE);

            } else {
            }
        }
    }

}

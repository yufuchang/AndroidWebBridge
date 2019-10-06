package com.yufuchang.bridge.interfaces;

import android.content.Intent;
import android.os.Bundle;



import java.util.List;
import java.util.Map;

/**
 * Created by yufuchang on 2019/7/11.
 */

public interface AndroidWebBridgeCallBackInterface {
    void onDissmissAndroidWebBridgeLoadingDialog();

    void onShowAndroidWebBridgeDialog();

    Map<String, String> onGetWebViewHeaders(String url);

    void onInitWebViewGoBackOrClose();

    void onSetTitle(String title);

    void onFinishActivity();

    void onLoadingDlgShow(String content);

    void onStartActivityForResult(Intent intent, int requestCode);

    void onStartActivityForResult(String routerPath, Bundle bundle, int requestCode);


    void onProgressChanged(int newProgress);


    void setOnKeyDownListener(OnKeyDownListener onKeyDownListener);


    void showLoadFailLayout(String url, String description);
}

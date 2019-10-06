package com.yufuchang.bridge.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by: yufuchang
 * Date: 2019/10/6
 */
public class ShareUtils {

    /**
     * 处理带分享功能的Action
     */
    private void handleShareIntent(Context context, Intent intent) {
        String action = intent.getAction();
        List<String> uriList = new ArrayList<>();
        //预留调试代码，后续需要根据Intent字段做调整可解开这里调试，有一个现象需要注意，EXTRA_STREAM字段需要用get方法
        //取得而不能用日志取得
//        LogUtils.YfcDebug("Intent："+JSONUtils.toJSONString(getIntent()));
//        Bundle bundle = getIntent().getExtras();
//        for (int i = 0; i < bundle.keySet().size(); i++) {
//            LogUtils.YfcDebug("key："+bundle.keySet().toArray()[i] + "content;"+bundle.get((String) bundle.keySet().toArray()[i]));
//        }
        if (Intent.ACTION_SEND.equals(action)) {
            Uri uri = FileUtils.getShareFileUri(intent);
            //如果是text/plain类型则先拦截这种type再进行进一步处理，否则当做文件分享处理
            if (intent.getType().equals("text/plain")) {
                if (uri != null) {
                    uriList.add(GetPathFromUri4kitkat.getPathByUri(context, uri));
                } else if (isLinkShare(intent)) {
//                    handleLinkShare(getShareLinkContent());
                    return;
                    //增加复制文本分享功能
                } else if (isTextShare(intent)) {
//                    shareTextMessage(getIntent().getExtras().getString(Intent.EXTRA_TEXT));
                    return;
                } else {
//                    ToastUtils.show(AppSchemeHandleActivity.this, getString(R.string.share_not_support));
//                    finish();
                }
            } else {
                if (uri != null) {
                    uriList.add(GetPathFromUri4kitkat.getPathByUri(context, uri));
                }
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            List<Uri> fileUriList = FileUtils.getShareFileUriList(intent);
            for (int i = 0; i < fileUriList.size(); i++) {
                uriList.add(GetPathFromUri4kitkat.getPathByUri(context, fileUriList.get(i)));
            }
        }
//        if (uriList.size() > 0) {
//            startVolumeShareActivity(uriList);
//        } else {
//            ToastUtils.show(AppSchemeHandleActivity.this, getString(R.string.share_not_support));
//            finish();
//        }
    }

    /**
     * 获取title和url
     * 此处验证过在不同手机不同系统，不同浏览器上Intent的extras有不同，字段名称可能不一样，个数可能不一样
     * 唯一能确定的是必定含有Intent.EXTRA_TEXT 在华为Mate20 系统9.0 华为原生浏览器上就只有此字段
     *
     * @return
     */
    private HashMap<String, String> getShareLinkContent(Intent intent) {
//        Intent intent = getIntent();
        String urlStr = "";
        String titleStr = "";
        String digest = "";
        HashMap<String, String> shareLinkMap = new HashMap<>();
        if (intent != null) {
            String text = intent.getExtras().getString(Intent.EXTRA_TEXT);
            String subject = intent.getExtras().getString(Intent.EXTRA_SUBJECT);
            String url = getContentUrl(intent);
            if (text != null && subject != null) {
                urlStr = !StringUtils.isBlank(url) ? url : getShareUrl(text);
                titleStr = subject;
                digest = text.replace(getShareUrl(text), "");
            } else if (text != null && subject == null) {
                urlStr = !StringUtils.isBlank(url) ? url : getShareUrl(text);
                titleStr = text.replace(urlStr, "");
            } else if (text == null && subject == null) {
                return shareLinkMap;
            }
            shareLinkMap.put("title", titleStr);
            shareLinkMap.put("url", urlStr);
            shareLinkMap.put("digest", digest);
        }
        return shareLinkMap;
    }

    /**
     * 如果包含url，则取出这个url
     *
     * @param intent
     * @return
     */
    private String getContentUrl(Intent intent) {
        Bundle bundle = intent.getExtras();
        String urlKey = "";
        for (String key : bundle.keySet()) {
            if (key.equals("url") || key.endsWith("url")) {
                urlKey = key;
                break;
            }
        }
        return bundle.getString(urlKey);
    }

    /**
     * 获取url
     *
     * @param text
     */
    private String getShareUrl(String text) {
        int begin = text.indexOf("http");
        int last = text.length();
        text = text.substring(begin, last);
//        Pattern p = Pattern.compile("((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?", Pattern.CASE_INSENSITIVE);
        Pattern p = Pattern.compile(Constant.PATTERN_URL, Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(text);
        matcher.find();
        return matcher.group();
    }

    /**
     * 是一个复制文本分享
     *
     * @return
     */
    private boolean isTextShare(Intent intent) {
        Bundle bundle = intent.getExtras();
        String extraText = bundle.getString(Intent.EXTRA_TEXT);
        return bundle != null && !StringUtils.isBlank(extraText);
    }

    /**
     * 是一个链接分享
     *
     * @return
     */
    private boolean isLinkShare(Intent intent) {
        Bundle bundle = intent.getExtras();
        String extraText = bundle.getString(Intent.EXTRA_TEXT);
        return bundle != null && !StringUtils.isBlank(extraText) &&
                StringUtils.matchResult(Pattern.compile(Constant.PATTERN_URL,
                        Pattern.CASE_INSENSITIVE), extraText, false).size() > 0;
    }
}

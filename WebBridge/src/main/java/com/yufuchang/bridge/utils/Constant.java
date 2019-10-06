package com.yufuchang.bridge.utils;

public class Constant {
    public static final String WEB_FRAGMENT_VERSION = "version";
    public static final String WEB_FRAGMENT_APP_NAME = "appName";
    public static final String WEB_FRAGMENT_SHOW_HEADER = "show_webview_header";
    public static final String Web_STATIC_TITLE = "web_from_index";
    public static final String APP_WEB_URI = "uri";

    public static final String PATTERN_URL = "(((https?)://[a-zA-Z0-9\\_\\-]+(\\.[a-zA-Z0-9\\_\\-]+)*(\\:\\d{2,4})?(/?[a-zA-Z0-9\\-\\_\\.\\?\\=\\&\\%\\#]+)*/?)" +
            "|([a-zA-Z0-9\\-\\_]+\\.)+([a-zA-Z\\-\\_]+)(\\:\\d{2,4})?(/?[a-zA-Z0-9\\-\\_\\.\\?\\=\\&\\%\\#]+)*/?|\\d+(\\.\\d+){3}(\\:\\d{2,4})?)";
}

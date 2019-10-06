package com.yufuchang.bridge.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Created by yufuchang on 2019/6/15.
 */
public class ParseHtmlElementsUtils {

    /**
     * 解析html内容获取指定标签
     *
     * @param html
     * @param metaLable
     * @return
     */
    public static Elements getDataFromHtml(String html, String metaLable) {
        Document doc = Jsoup.parse(html);
        Elements metas = doc.select(metaLable);
        return metas;
    }
}

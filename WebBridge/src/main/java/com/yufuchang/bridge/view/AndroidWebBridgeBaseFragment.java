package com.yufuchang.bridge.view;

import android.support.v4.app.Fragment;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.yufuchang.bridge.utils.ResolutionUtils;

/**
 * Created by chenmch on 2018/11/27.
 */
public class AndroidWebBridgeBaseFragment extends Fragment {
    protected static final String JAVASCRIPT_PREFIX = "javascript:";
    protected RelativeLayout functionLayout;
    protected LinearLayout webFunctionLayout;
    protected TextView headerText;
    private int functionLayoutWidth = -1;
    private int webFunctionLayoutWidth = -1;

    protected void initHeaderOptionMenu() {

            setHeaderTextWidth();
    }


    /**
     * 动态监控布局变化
     */
    protected void setHeaderTextWidth() {
        webFunctionLayout.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    functionLayoutWidth = functionLayout.getWidth();
                    webFunctionLayoutWidth = webFunctionLayout.getWidth();
                    headerText.setMaxWidth(ResolutionUtils.getWidth(getActivity()) - getMaxWidth() * 2);
                }
            }
        });
    }

    /**
     * 取两个宽度的最大值
     *
     * @return
     */
    private int getMaxWidth() {
        if (functionLayoutWidth > webFunctionLayoutWidth) {
            return functionLayoutWidth;
        }
        return webFunctionLayoutWidth;
    }



    /**
     * 执行JS脚本
     *
     * @param script
     */
    protected void runJavaScript(String script) {

    }


}

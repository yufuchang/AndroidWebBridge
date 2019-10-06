package com.yufuchang.bridge.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;

import com.yufuchang.bridge.R;


/**
 * 悬浮窗管理类封装
 * Created by yufuchang on 2018/8/29.
 */

public class SuspensionWindowManagerUtils {

    private static SuspensionWindowManagerUtils suspensionWindowManagerUtils;
    private View windowView = null;//整个悬浮窗view
    private WindowManager windowManager = null;//WindowManager管理类
    private Context windowContext = null;//传入的上下文
    private boolean isShowing = false;//是否还在显示的标志
    private WindowManager.LayoutParams params;//window相关参数
    private int screenWidthSize = 0;//上一个window的宽度
    private long passedTime = 0;//显示在页面上的秒数
    private Chronometer chronometer;
    private long beginTime = 0;//touch开始时间
    private boolean isTouchEvent = false;//判定touch事件的标志

    /**
     * 获取悬浮窗实例
     *
     * @return
     */
    public static SuspensionWindowManagerUtils getInstance() {
        if (suspensionWindowManagerUtils == null) {
            synchronized (SuspensionWindowManagerUtils.class) {
                if (suspensionWindowManagerUtils == null) {
                    suspensionWindowManagerUtils = new SuspensionWindowManagerUtils();
                }
            }
        }
        return suspensionWindowManagerUtils;
    }

    /**
     * 显示悬浮窗
     *
     * @param context
     * @param screenWidthSize
     * @param time
     */
    public void showCommunicationSmallWindow(Context context, int screenWidthSize, long time) {
        this.screenWidthSize = screenWidthSize;
        this.passedTime = time;
        this.windowContext = context;
        if (isShowing) {
            return;
        }
        isShowing = true;
        initSuspensionWindowView();
        initParamsAndListeners();
        windowManager.addView(windowView, params);
    }

    /**
     * 隐藏悬浮窗
     */
    public void hideCommunicationSmallWindow() {
        if (isShowing && null != windowView) {
            windowManager.removeView(windowView);
            isShowing = false;
        }
    }

    /**
     * 悬浮窗是否还在显示
     *
     * @return
     */
    public boolean isShowing() {
        return isShowing;
    }


    /**
     * 组装悬浮窗View
     *
     * @return
     */
    private void initSuspensionWindowView() {
        windowView = LayoutInflater.from(windowContext).inflate(R.layout.service_voice_communication,
                null);
        chronometer = (Chronometer) windowView.findViewById(R.id.chronometer_voice_communication_time);
        chronometer.setBase(SystemClock.elapsedRealtime() - passedTime * 1000);
        chronometer.start();
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTouchEvent) {
                    goBackVoiceCommunicationActivity();
                    hideCommunicationSmallWindow();
                    isTouchEvent = false;
                }
            }
        };
        //点击事件的监听
        ImageButton imgBtnPhone = (ImageButton) windowView.findViewById(R.id.img_btn_voice_window);
        imgBtnPhone.setOnClickListener(clickListener);
        windowView.setOnClickListener(clickListener);
        //更新窗口位置的监听
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isTouchEvent = false;
                        beginTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        params.x = (int) event.getRawX() - DensityUtil.dip2px(windowContext, 32);
                        params.y = (int) event.getRawY() - DensityUtil.dip2px(windowContext, 42) - getStatusBarHeight();
                        windowManager.updateViewLayout(windowView, params);
                        break;
                    case MotionEvent.ACTION_UP:
                        isTouchEvent = (System.currentTimeMillis() - beginTime) > 100;
                        break;
                }
                return false;
            }
        };
        imgBtnPhone.setOnTouchListener(touchListener);
        windowView.setOnTouchListener(touchListener);
    }

    /**
     * 设置参数
     *
     * @return
     */
    private void initParamsAndListeners() {
        // 获取应用的Context
        // 获取WindowManager
        windowManager = (WindowManager) windowContext.getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        // 悬浮窗类型
        // WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        // 设置flag
        // 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
        // 设置WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM可以拦截back事件
        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.flags = flags;
        // 不设置这个弹出框的透明遮罩显示为黑色
        params.format = PixelFormat.TRANSLUCENT;
        //设置悬浮窗口长宽数据.
        params.width = DensityUtil.dip2px(windowContext, 64);
        params.height = DensityUtil.dip2px(windowContext, 84);
        //设置悬浮窗位置
        params.x = screenWidthSize - DensityUtil.dip2px(windowContext, 74);
        params.y = DensityUtil.dip2px(windowContext, 4);
        //设置悬浮窗位置和滑动参数
        params.gravity = Gravity.LEFT | Gravity.TOP;

        //窗口类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
    }

    /**
     * 获取statusBar高度
     *
     * @return
     */
    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        int resourceId = windowContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = windowContext.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * 回到语音通话界面
     */
    private void goBackVoiceCommunicationActivity() {
//        Intent intent = new Intent();
//        intent.setClass(windowContext, ChannelVoiceCommunicationActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_COMMUNICATION_STATE, ChannelVoiceCommunicationActivity.COME_BACK_FROM_SERVICE);
//        intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_TIME, Long.parseLong(TimeUtils.getChronometerSeconds(chronometer.getText().toString())));
//        windowContext.startActivity(intent);
    }
}


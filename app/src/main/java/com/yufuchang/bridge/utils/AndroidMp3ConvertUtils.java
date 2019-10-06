package com.yufuchang.bridge.utils;

import android.content.Context;
import android.os.Handler;


import java.io.File;

import jaygoo.library.converter.Mp3Converter;

/**
 * 用于录音格式从raw向mp3转化
 * Created by yufuchang on 2018/10/9.
 */

public class AndroidMp3ConvertUtils {
    private static final int SIMPLE_RATE = 8000;//采样率
    private static final int CHANNEL = 1;//声道数
    private static final int MODE = 0;//模式，默认0
    private static final int OUT_BIT_RATE = 32;//输入比特率
    private static final int QUALITY = 5;//音频质量0~9,0质量最好体积最大，9质量最差体积最小
    Handler handler = new Handler();
    private Context context;
    private String rawPath = "", mp3Path = "";
    private long rawFileSize = 0;
    private AndroidMp3ConvertCallback callback;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long bytes = Mp3Converter.getConvertBytes();
            float progress = (100f * bytes / rawFileSize);
            if (bytes == -1) {
                progress = 100;
            }
            //解决部分手机（如mi6）在有些情况下回调有问题导致dialog不消失的问题
            if (progress == 100) {
                callback.onSuccess(mp3Path);
            }
            if (handler != null && progress != 100) {
                handler.postDelayed(this, 20);
            }
        }
    };

    private AndroidMp3ConvertUtils(Context context) {
        this.context = context;
        Mp3Converter.init(SIMPLE_RATE, CHANNEL, MODE, SIMPLE_RATE, OUT_BIT_RATE, QUALITY);
    }

    /**
     * 仿照AndroidAudioConverter转化类的构建方式
     *
     * @param context
     * @return
     */
    public static AndroidMp3ConvertUtils with(Context context) {
        return new AndroidMp3ConvertUtils(context);
    }

    /**
     * 设置回调函数
     *
     * @param callBack
     * @return
     */
    public AndroidMp3ConvertUtils setCallBack(AndroidMp3ConvertCallback callBack) {
        this.callback = callBack;
        return this;
    }

    /**
     * 设置raw和mp3文件路径
     * rawFilePath为要转换的raw文件
     * mp3FilePath为转换完成mp3文件存放位置
     *
     * @param rawFilePath
     * @param mp3FilePath
     * @return
     */
    public AndroidMp3ConvertUtils setRawPathAndMp3Path(String rawFilePath, String mp3FilePath) {
        this.rawPath = rawFilePath;
        this.mp3Path = mp3FilePath;
        return this;
    }

    /**
     * raw到mp3转码
     */
    public void startConvert() {
        //检查回调，raw和mp3路径是否为空
        if (callback == null || StringUtils.isBlank(rawPath) || StringUtils.isBlank(mp3Path)) {
            Exception e = new Exception("callback or filepath null exception");
            callback.onFailure(e);
            return;
        }
        //检查raw，mp3路径是否正确
        if (!checkRawAndMp3PathCorrect()) {
            Exception e = new Exception("raw or mp3 filePath not correct exception");
            callback.onFailure(e);
            return;
        }
        keepMp3FilePathExist();
        try {
            rawFileSize = new File(rawPath).length();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Mp3Converter.convertMp3(rawPath, mp3Path);
                }
            }).start();
            handler.postDelayed(runnable, 20);
        } catch (Exception e) {
            callback.onFailure(e);
            e.printStackTrace();
        }
    }

    /**
     * 检查mp3存放文件夹是否存在，不存在需要创建
     */
    private void keepMp3FilePathExist() {
        String mp3FileSavePath = FileUtils.getFolderName(mp3Path);
        File dir = new File(mp3FileSavePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 检查传入raw，mp3文件名称是否正确
     */
    private boolean checkRawAndMp3PathCorrect() {
        if (!rawPath.endsWith(".raw") || !mp3Path.endsWith(".mp3")) {
            return false;
        }
        return true;
    }

    public interface AndroidMp3ConvertCallback {
        void onSuccess(String mp3FilePath);

        void onFailure(Exception e);
    }
}

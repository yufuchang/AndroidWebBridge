package com.yufuchang.bridge.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class GetPathFromUri4kitkat {

    public static String getPathByUri(final Context context, final Uri uri) {
        boolean isAboveKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        String filePath = null;
        if (isAboveKitKat) {
            // 获取4.4及以上版本的文件路径
            filePath = GetPathFromUri4kitkat.getPath(context, uri);
        } else {
            // 低版本兼容方法
            filePath = GetPathFromUri4kitkat.getRealPathFromURI(context, uri);
        }
        return (filePath == null ? "" : filePath);
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    private static String getPath(final Context context, final Uri uri) {
        String filePath = "";
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    filePath = Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                if (!StringUtils.isNumeric(id)) {
                    filePath = getFilePath(id);
                } else {
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(id));
                    filePath = getDataColumn(context, contentUri, null, null);
                }
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                filePath = getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
            //特殊机型
            else if (isSamsungDocument(uri)) {
                filePath = getSamsungDataColumn(context, uri, null, null);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri)) {
                filePath = uri.getLastPathSegment();
            } else if (isHuaweiUri(uri)) {
                filePath = getHuaweiRealPath(uri);
            }
            filePath = getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        if (!FileUtils.isFileExist(filePath)) {
            filePath = getFilePathFromContentUri(uri, context);
        }
        return filePath;
    }

    /**
     * 检查文件是否存在，文件存在则返回路径不存在返回空
     */
    private static String getFilePath(String filePath) {
        String filePathExceptRaw = "";
        if (filePath.startsWith("raw:")) {
            filePathExceptRaw = filePath.split(":")[1];
        }
        if (FileUtils.isFileExist(filePathExceptRaw)) {
            return filePathExceptRaw;
        }
        return "";
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = MediaStore.Images.Media.DATA;
        final String[] projection = {column};
        if (!TextUtils.isEmpty(uri.getAuthority())) {
            try {
                cursor = context.getContentResolver().query(uri, projection,
                        selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int column_index = cursor.getColumnIndexOrThrow(column);
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            return uri.getPath();
        }

        return null;
    }

    private static String getSamsungDataColumn(Context context, Uri uri,
                                              String selection, String[] selectionArgs) {
        Cursor cursor = null;
//        final String column = "_data";
//        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, null, selection,
                    selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 获取华为Uri的真实路径
     *
     * @param uri
     * @return
     */
    private static String getHuaweiRealPath(Uri uri) {
        return "/storage/" + uri.toString().split("storage/")[1];
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isSamsungDocument(Uri uri) {
        return "com.neusoft.td.android.wo116114.browser.documents".equals(uri
                .getAuthority());
    }

    private static boolean isHuaweiUri(Uri uri) {
        return "com.huawei.hidisk.fileprovider".equals(uri.getAuthority());
    }

    //获取低版本文件路径的兼容方法
    private static String getRealPathFromURI(Context context, Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj,
                null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    /**
     * 在通过前面的方式都获取不到文件路径的情况下通过复制一个临时文件来完成分享
     *
     * @param contentUri
     * @param context
     * @return
     */
    private static String getFilePathFromContentUri(Uri contentUri, Context context) {
        if (contentUri == null) {
            return null;
        }
        String filePath = null;
        String fileName;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(contentUri, filePathColumn, null,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            fileName = cursor.getString(cursor.getColumnIndex(filePathColumn[1]));
            cursor.close();
            filePath = getFilePathFromInputStreamUri(context, contentUri, fileName);
        }
        return filePath;
    }


    /**
     * 用流拷贝文件一份到自己APP目录下
     *
     * @param context
     * @param uri
     * @param fileName
     * @return
     */
    private static String getFilePathFromInputStreamUri(Context context, Uri uri, String fileName) {
        InputStream inputStream = null;
        String filePath = null;
        if (uri.getAuthority() != null) {
            try {
                inputStream = context.getContentResolver().openInputStream(uri);
                File file = createTemporalFileFrom(context, inputStream, fileName);
                filePath = file.getPath();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return filePath;
    }

    private static final String tempFilePath =  Environment
            .getExternalStorageDirectory() + "/cache/share_file/";
    /**
     * 在应用内创建一个临时文件用来存放需要分享的临时文件
     *
     * @param context
     * @param inputStream
     * @param fileName
     * @return
     */
    private static File createTemporalFileFrom(Context context, InputStream inputStream, String fileName) {
        File targetFile = null;
        try {
            if (inputStream != null) {
                int read;
                byte[] buffer = new byte[8 * 1024];
                File dir = new File(tempFilePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                //自己定义拷贝文件路径
                targetFile = new File(tempFilePath, fileName);
                if (targetFile.exists()) {
                    targetFile.delete();
                }
                OutputStream outputStream = new FileOutputStream(targetFile);
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetFile;
    }

}

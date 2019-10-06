package com.yufuchang.bridge.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * File Utils
 * <ul>
 * Read or write file
 * <li>{@link #writeFile(String, String, boolean)} write file from String</li>
 * <li>{@link #writeFile(String, String)} write file from String</li>
 * <li>{@link #writeFile(String, List, boolean)} write file from String List</li>
 * <li>{@link #writeFile(String, List)} write file from String List</li>
 * <li>{@link #writeFile(String, InputStream)} write file</li>
 * <li>{@link #writeFile(String, InputStream, boolean)} write file</li>
 * <li>{@link #writeFile(File, InputStream)} write file</li>
 * <li>{@link #writeFile(File, InputStream, boolean)} write file</li>
 * </ul>
 * <ul>
 * Operate file
 * <li>{@link #moveFile(File, File)} or {@link #moveFile(String, String)}</li>
 * <li>{@link #copyFile(String, String)}</li>
 * <li>{@link #getFileExtension(String)}</li>
 * <li>{@link #getFileName(String)}</li>
 * <li>{@link #getFileNameWithoutExtension(String)}</li>
 * <li>{@link #getFileSize(String)}</li>
 * <li>{@link #deleteFile(String)}</li>
 * <li>{@link #isFileExist(String)}</li>
 * <li>{@link #isFolderExist(String)}</li>
 * <li>{@link #makeFolders(String)}</li>
 * <li>{@link #makeDirs(String)}</li>
 * </ul>
 *
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2012-5-12
 */
public class FileUtils {

    public final static String FILE_EXTENSION_SEPARATOR = ".";
    public final static String CLOUD_DOCUMENT = "document";
    public final static String CLOUD_PICTURE = "picture";
    public final static String CLOUD_AUDIO = "audio";
    public final static String CLOUD_VIDEO = "video";
    public final static String CLOUD_UNKNOWN_FILE_TYPE = "unknown_file_type";

    private FileUtils() {
        throw new AssertionError();
    }

    /**
     * read file
     *
     * @param filePath
     * @param charsetName The name of a supported {@link java.nio.charset.Charset
     *                    </code>charset<code>}
     * @return if file not exist, return null, else return content of file
     * @throws RuntimeException if an error occurs while operator BufferedReader
     */
    public static StringBuilder readFile(String filePath, String charsetName) {
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder("");
        if (file == null || !file.isFile()) {
            return new StringBuilder();
        }

        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(
                    file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().equals("")) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            reader.close();
            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * write file
     *
     * @param filePath
     * @param content
     * @param append   is append, if true, write to the end of file, else clear
     *                 content of file and write into it
     * @return return false if content is empty, true otherwise
     * @throws RuntimeException if an error occurs while operator FileWriter
     */
    public static boolean writeFile(String filePath, String content,
                                    boolean append) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }

        FileWriter fileWriter = null;
        try {
            makeDirs(filePath);
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);
            fileWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * write file
     *
     * @param filePath
     * @param contentList
     * @param append      is append, if true, write to the end of file, else clear
     *                    content of file and write into it
     * @return return false if contentList is empty, true otherwise
     * @throws RuntimeException if an error occurs while operator FileWriter
     */
    public static boolean writeFile(String filePath, List<String> contentList,
                                    boolean append) {
        if (contentList == null || contentList.size() == 0) {
            return false;
        }

        FileWriter fileWriter = null;
        try {
            makeDirs(filePath);
            fileWriter = new FileWriter(filePath, append);
            int i = 0;
            for (String line : contentList) {
                if (i++ > 0) {
                    fileWriter.write("\r\n");
                }
                fileWriter.write(line);
            }
            fileWriter.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * write file, the string will be written to the begin of the file
     *
     * @param filePath
     * @param content
     * @return
     */
    public static boolean writeFile(String filePath, String content) {
        return writeFile(filePath, content, false);
    }

    /**
     * write file, the string list will be written to the begin of the file
     *
     * @param filePath
     * @param contentList
     * @return
     */
    public static boolean writeFile(String filePath, List<String> contentList) {
        return writeFile(filePath, contentList, false);
    }

    /**
     * write file, the bytes will be written to the begin of the file
     *
     * @param filePath
     * @param stream
     * @return
     * @see {@link #writeFile(String, InputStream, boolean)}
     */
    public static boolean writeFile(String filePath, InputStream stream) {
        return writeFile(filePath, stream, false);
    }

    /**
     * write file
     *
     * @param filePath the file to be opened for writing.
     * @param stream   the input stream
     * @param append   if <code>true</code>, then bytes will be written to the end of
     *                 the file rather than the beginning
     * @return return true
     * @throws RuntimeException if an error occurs while operator FileOutputStream
     */
    public static boolean writeFile(String filePath, InputStream stream,
                                    boolean append) {
        return writeFile(filePath != null ? new File(filePath) : null, stream,
                append);
    }

    /**
     * write file, the bytes will be written to the begin of the file
     *
     * @param file
     * @param stream
     * @return
     * @see {@link #writeFile(File, InputStream, boolean)}
     */
    public static boolean writeFile(File file, InputStream stream) {
        return writeFile(file, stream, false);
    }

    /**
     * write file
     *
     * @param file   the file to be opened for writing.
     * @param stream the input stream
     * @param append if <code>true</code>, then bytes will be written to the end of
     *               the file rather than the beginning
     * @return return true
     * @throws RuntimeException if an error occurs while operator FileOutputStream
     */
    public static boolean writeFile(File file, InputStream stream,
                                    boolean append) {
        OutputStream o = null;
        try {
            makeDirs(file.getAbsolutePath());
            o = new FileOutputStream(file, append);
            byte data[] = new byte[1024];
            int length = -1;
            while ((length = stream.read(data)) != -1) {
                o.write(data, 0, length);
            }
            o.flush();
            return true;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFoundException occurred. ", e);
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (o != null) {
                try {
                    o.close();
                    stream.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * move file
     *
     * @param sourceFilePath
     * @param destFilePath
     */
    public static void moveFile(String sourceFilePath, String destFilePath) {
        if (TextUtils.isEmpty(sourceFilePath)
                || TextUtils.isEmpty(destFilePath)) {
            throw new RuntimeException(
                    "Both sourceFilePath and destFilePath cannot be null.");
        }
        moveFile(new File(sourceFilePath), new File(destFilePath));
    }

    /**
     * move file
     *
     * @param srcFile
     * @param destFile
     */
    public static void moveFile(File srcFile, File destFile) {
        boolean rename = srcFile.renameTo(destFile);
        if (!rename) {
            copyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
            deleteFile(srcFile.getAbsolutePath());
        }
    }

    /**
     * copy file
     *
     * @param sourceFilePath
     * @param destFilePath
     * @return
     * @throws RuntimeException if an error occurs while operator FileOutputStream
     */
    public static boolean copyFile(String sourceFilePath, String destFilePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(sourceFilePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFoundException occurred. ", e);
        }
        return writeFile(destFilePath, inputStream);
    }

    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：/sdcard/aa
     * @param newPath String 复制后路径 如：/sdcard/bb/aa
     * @return boolean
     */
    public static boolean copyFolder(String oldPath, String newPath) {
        boolean copySuccess = false;
        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a = new File(oldPath);
            String[] files = a.list();
            File temp = null;
            if (files == null) {
                return false;
            }
            for (int i = 0; i < files.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + files[i]);
                } else {
                    temp = new File(oldPath + File.separator + files[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {//如果是子文件夹
                    copyFolder(oldPath + "/" + files[i], newPath + "/" + files[i]);
                }
            }
            copySuccess = true;
        } catch (Exception e) {
            copySuccess = false;
            e.printStackTrace();
        }
        return copySuccess;
    }

    /**
     * read file to string list, a element of list is a line
     *
     * @param filePath
     * @param charsetName The name of a supported {@link java.nio.charset.Charset
     *                    </code>charset<code>}
     * @return if file not exist, return null, else return content of file
     * @throws RuntimeException if an error occurs while operator BufferedReader
     */
    public static List<String> readFileToList(String filePath,
                                              String charsetName) {
        File file = new File(filePath);
        List<String> fileContent = new ArrayList<String>();
        if (file == null || !file.isFile()) {
            return null;
        }

        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(
                    file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                fileContent.add(line);
            }
            reader.close();
            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * get file name from path, not include suffix
     * <p>
     * <pre>
     *      getFileNameWithoutExtension(null)               =   null
     *      getFileNameWithoutExtension("")                 =   ""
     *      getFileNameWithoutExtension("   ")              =   "   "
     *      getFileNameWithoutExtension("abc")              =   "abc"
     *      getFileNameWithoutExtension("a.mp3")            =   "a"
     *      getFileNameWithoutExtension("a.b.rmvb")         =   "a.b"
     *      getFileNameWithoutExtension("c:\\")              =   ""
     *      getFileNameWithoutExtension("c:\\a")             =   "a"
     *      getFileNameWithoutExtension("c:\\a.b")           =   "a"
     *      getFileNameWithoutExtension("c:a.txt\\a")        =   "a"
     *      getFileNameWithoutExtension("/home/admin")      =   "admin"
     *      getFileNameWithoutExtension("/home/admin/a.txt/b.mp3")  =   "b"
     * </pre>
     *
     * @param filePath
     * @return file name from path, not include suffix
     * @see
     */
    public static String getFileNameWithoutExtension(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return filePath;
        }

        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (filePosi == -1) {
            return (extenPosi == -1 ? filePath : filePath.substring(0,
                    extenPosi));
        }
        if (extenPosi == -1) {
            return filePath.substring(filePosi + 1);
        }
        return (filePosi < extenPosi ? filePath.substring(filePosi + 1,
                extenPosi) : filePath.substring(filePosi + 1));
    }

    /**
     * get file name from path, include suffix
     * <p>
     * <pre>
     *      getFileName(null)               =   null
     *      getFileName("")                 =   ""
     *      getFileName("   ")              =   "   "
     *      getFileName("a.mp3")            =   "a.mp3"
     *      getFileName("a.b.rmvb")         =   "a.b.rmvb"
     *      getFileName("abc")              =   "abc"
     *      getFileName("c:\\")              =   ""
     *      getFileName("c:\\a")             =   "a"
     *      getFileName("c:\\a.b")           =   "a.b"
     *      getFileName("c:a.txt\\a")        =   "a"
     *      getFileName("/home/admin")      =   "admin"
     *      getFileName("/home/admin/a.txt/b.mp3")  =   "b.mp3"
     * </pre>
     *
     * @param filePath
     * @return file name from path, include suffix
     */
    public static String getFileName(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? filePath : filePath.substring(filePosi + 1);
    }

    /**
     * get folder name from path
     * <p>
     * <pre>
     *      getFolderName(null)               =   null
     *      getFolderName("")                 =   ""
     *      getFolderName("   ")              =   ""
     *      getFolderName("a.mp3")            =   ""
     *      getFolderName("a.b.rmvb")         =   ""
     *      getFolderName("abc")              =   ""
     *      getFolderName("c:\\")              =   "c:"
     *      getFolderName("c:\\a")             =   "c:"
     *      getFolderName("c:\\a.b")           =   "c:"
     *      getFolderName("c:a.txt\\a")        =   "c:a.txt"
     *      getFolderName("c:a\\b\\c\\d.txt")    =   "c:a\\b\\c"
     *      getFolderName("/home/admin")      =   "/home"
     *      getFolderName("/home/admin/a.txt/b.mp3")  =   "/home/admin/a.txt"
     * </pre>
     *
     * @param filePath
     * @return
     */
    public static String getFolderName(String filePath) {

        if (StringUtils.isEmpty(filePath)) {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? "" : filePath.substring(0, filePosi);
    }

    /**
     * get suffix of file from path
     * <p>
     * <pre>
     *      getFileExtension(null)               =   ""
     *      getFileExtension("")                 =   ""
     *      getFileExtension("   ")              =   "   "
     *      getFileExtension("a.mp3")            =   "mp3"
     *      getFileExtension("a.b.rmvb")         =   "rmvb"
     *      getFileExtension("abc")              =   ""
     *      getFileExtension("c:\\")              =   ""
     *      getFileExtension("c:\\a")             =   ""
     *      getFileExtension("c:\\a.b")           =   "b"
     *      getFileExtension("c:a.txt\\a")        =   ""
     *      getFileExtension("/home/admin")      =   ""
     *      getFileExtension("/home/admin/a.txt/b")  =   ""
     *      getFileExtension("/home/admin/a.txt/b.mp3")  =   "mp3"
     * </pre>
     *
     * @param filePath
     * @return
     */
    public static String getFileExtension(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return filePath;
        }

        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (extenPosi == -1) {
            return "";
        }
        return (filePosi >= extenPosi) ? "" : filePath.substring(extenPosi + 1);
    }

    /**
     * Creates the directory named by the trailing filename of this file,
     * including the complete directory path required to create this directory. <br/>
     * <br/>
     * <ul>
     * <strong>Attentions:</strong>
     * <li>makeDirs("C:\\Users\\Trinea") can only create users folder</li>
     * <li>makeFolder("C:\\Users\\Trinea\\") can create Trinea folder</li>
     * </ul>
     *
     * @param filePath
     * @return true if the necessary directories have been created or the target
     * directory already exists, false one of the directories can not be
     * created.
     * <ul>
     * <li>if {@link FileUtils#getFolderName(String)} return null,
     * return false</li>
     * <li>if target directory already exists, return true</li>
     * <li>return </li>
     * </ul>
     */
    public static boolean makeDirs(String filePath) {
        String folderName = getFolderName(filePath);
        if (StringUtils.isEmpty(folderName)) {
            return false;
        }

        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) || folder
                .mkdirs();
    }

    /**
     * @param filePath
     * @return
     * @see #makeDirs(String)
     */
    public static boolean makeFolders(String filePath) {
        return makeDirs(filePath);
    }

    /**
     * Indicates if this file represents a file on the underlying file system.
     *
     * @param filePath
     * @return
     */
    public static boolean isFileExist(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return false;
        }

        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }

    /**
     * Indicates if this file represents a directory on the underlying file
     * system.
     *
     * @param directoryPath
     * @return
     */
    public static boolean isFolderExist(String directoryPath) {
        if (StringUtils.isBlank(directoryPath)) {
            return false;
        }

        File dire = new File(directoryPath);
        return (dire.exists() && dire.isDirectory());
    }

    /**
     * delete file or directory
     * <ul>
     * <li>if path is null or empty, return true</li>
     * <li>if path not exist, return true</li>
     * <li>if path exist, delete recursion. return true</li>
     * <ul>
     *
     * @param path
     * @return
     */
    public static boolean deleteFile(String path) {
        if (StringUtils.isBlank(path)) {
            return true;
        }

        File file = new File(path);
        if (!file.exists()) {
            LogUtils.YfcDebug("文件不存在");
            return true;
        }
        if (file.isFile()) {
            LogUtils.YfcDebug("文件被删除");
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

    /**
     * get file size
     * <ul>
     * <li>if path is null or empty, return -1</li>
     * <li>if path exist and it is a file, return file size, else return -1</li>
     * <ul>
     *
     * @param path
     * @return returns the length of this file in bytes. returns -1 if the file
     * does not exist.
     */
    public static long getFileSize(String path) {
        if (StringUtils.isBlank(path)) {
            return -1;
        }

        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

    /**
     * 获取文件大小
     *
     * @param fileSize
     * @return
     */
    public static String formatFileSize(String fileSize) {
        long size = Long.parseLong(fileSize);
        return formatFileSize(size);
    }

    /**
     * 获取文件大小
     *
     * @param fileSize
     * @return
     */
    public static String formatFileSize(long fileSize) {
        int MBDATA = 1048576;
        int GBDATA = 1073741824;
//        float size = fileSize;
//        DecimalFormat df = new DecimalFormat("#.0");
//        if (size < 1023) {
//            String tempSize = df.format(size);
//            if (tempSize.length() < 3) {
//                tempSize = "0" + tempSize;
//            }
//            return tempSize + "B";
//        } else if (size > 1023 && size < MBDATA) {
//            size = (long) (size / 1024.0);
//            String tempSize = df.format(size);
//            if (tempSize.length() < 3) {
//                tempSize = "0" + tempSize;
//            }
//            return tempSize + "K";
//        } else if (size >= MBDATA) {
//            size = size / MBDATA;
//            String tempSize = df.format(size);
//            if (tempSize.length() < 3) {
//                tempSize = "0" + tempSize;
//            }
//            return tempSize + "M";
//        } else {
//            size = size / GBDATA;
//            String tempSize = df.format(size);
//            if (tempSize.length() < 3) {
//                tempSize = "0" + tempSize;
//            }
//            return tempSize + "G";
//        }


        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        String formatFileSize = "";
        if (fileSize < 1024) {
            formatFileSize = fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            formatFileSize = nf.format((double) fileSize / 1024) + " K";
        } else if (fileSize < 1024 * 1024 * 1024) {
            formatFileSize = nf.format((double) fileSize / MBDATA) + " M";
        } else {
            formatFileSize = nf.format((double) fileSize / GBDATA) + " G";
        }
        return formatFileSize;
    }

    /**
     * 获取文件扩展名(xx.png,返回png)
     *
     * @param filename
     * @return
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    public static String getFilenamePrefix(String filename) {
        try {
            if ((filename != null) && (filename.length() > 0)) {
                return filename.substring(0, filename.lastIndexOf("."));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取文件扩展名(xx.png,返回.png)
     *
     * @param fileName
     * @return
     */
    public static String getExtensionNameWithPoint(String fileName) {
        if ((fileName != null) && (fileName.length() > 0)) {
            int dot = fileName.lastIndexOf('.');
            if ((dot > -1) && (dot < (fileName.length() - 1))) {
                return "." + fileName.substring(dot + 1);
            }
        }
        return "";
    }

    //

    // 得到mime的方法2
    public static String getSuffix(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return "";
        }
        String fileName = file.getName();
        if (fileName.equals("") || fileName.endsWith(".")) {
            return "";
        }
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1).toLowerCase(Locale.US);
        } else {
            return "";
        }
    }

    public static String getSuffix(String fileName) {
        if (fileName.equals("") || fileName.endsWith(".")) {
            return "";
        }
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1).toLowerCase(Locale.US);
        } else {
            return "";
        }
    }

    /**
     * 获取文件打开的schema
     *
     * @param file
     * @return
     */
    public static String getMimeType(File file) {
        String suffix = getSuffix(file);
        if (suffix == null) {
            return "";
        }
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                suffix);
        if (TextUtils.isEmpty(type)) {
            return "";
        }
        if (!StringUtils.isBlank(type)) {
            return type;
        } else {
            return "";
        }
    }

    public static String getMimeType(String fileName) {
        String suffix = getSuffix(fileName);
        if (suffix == null) {
            return "";
        }
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                suffix);
        if (TextUtils.isEmpty(type)) {
            return "";
        }
        if (!StringUtils.isBlank(type)) {
            return type;
        } else {
            return "";
        }
    }

//    /**
//     * 打开文件多的方法
//     *
//     * @param context
//     */
//    public static void openFile(Activity context, File file, boolean isNeedStartForResult) {
//        String mime = FileUtils.getMimeType(file);
//        if (StringUtils.isBlank(mime)) {
//            mime = "text/plain";
//        }
//        openFile(context, file, mime, isNeedStartForResult);
//    }
//
//    /**
//     * 打开文件多的方法
//     *
//     * @param context
//     * @param path
//     */
//    public static void openFile(Context context, String path) {
//        File file = new File(path);
//        String mime = FileUtils.getMimeType(file);
//        if (StringUtils.isBlank(mime)) {
//            mime = "text/plain";
//        }
//        openFile(context, path, mime);
//    }

//    /**
//     * 文件是否可以用云+本身打开
//     *
//     * @param path
//     * @return
//     */
//    public static boolean canFileOpenByApp(String path) {
//        return canFileOpenByApp(new File(path));
//    }

//    /**
//     * 文件是否可以用云+本身打开
//     *
//     * @param file
//     * @return
//     */
//    public static boolean canFileOpenByApp(File file) {
//        Router router = Router.getInstance();
//        if (router.getService(CommunicationService.class) != null) {
//            String suffix = getSuffix(file);
//            if (suffix.toLowerCase().equals("png") || suffix.toLowerCase().equals("jpg") || suffix.toLowerCase().equals("jpeg")) {
//                return true;
//            }
//        }
//        return false;
//    }

//    /**
//     * 打开文件
//     *
//     * @param context
//     * @param mime
//     */
//    public static void openFile(Activity context, File file, String mime, boolean isNeedStartActivityForResult) {
//        if (canFileOpenByApp(file)) {
//            Bundle bundle = new Bundle();
//            bundle.putInt("image_index", 0);
//            ArrayList<String> urlList = new ArrayList<>();
//            urlList.add("file://" + file.getAbsolutePath());
//            bundle.putStringArrayList("image_urls", urlList);
//            ARouter.getInstance().build(Constant.AROUTER_CLASS_COMMUNICATION_IMAGEPAGER).with(bundle);
//            return;
//        }
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        //判断是否是AndroidN以及更高的版本
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            intent.setDataAndType(contentUri, mime);
//        } else {
//            intent.setDataAndType(Uri.fromFile(file), mime);
//        }
//        if (context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
//            if (isNeedStartActivityForResult) {
//                context.startActivityForResult(intent, 5);
//            } else {
//                context.startActivity(intent);
//            }
//        } else {
//            ToastUtils.show(context, context.getString(R.string.chat_file_open_fail_tip));
//        }
//    }
//
//    /**
//     * 打开文件
//     *
//     * @param context
//     * @param path
//     * @param mime
//     */
//    public static void openFile(Context context, String path, String mime) {
//        File file = new File(path);
//        if (canFileOpenByApp(file)) {
//            Bundle bundle = new Bundle();
//            bundle.putInt("image_index", 0);
//            ArrayList<String> urlList = new ArrayList<>();
//            urlList.add("file://" + file.getAbsolutePath());
//            bundle.putStringArrayList("image_urls", urlList);
//            ARouter.getInstance().build(Constant.AROUTER_CLASS_COMMUNICATION_IMAGEPAGER).with(bundle).navigation();
//            return;
//        }
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        //判断是否是AndroidN以及更高的版本
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            intent.setDataAndType(contentUri, mime);
//        } else {
//            intent.setDataAndType(Uri.fromFile(file), mime);
//        }
//        if (context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
//            context.startActivity(intent);
//        } else {
//            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
//            intent.setDataAndType(contentUri, "text/plain");
//            if (context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
//                context.startActivity(intent);
//            } else {
//                ToastUtils.show(context, context.getString(R.string.chat_file_open_fail_tip));
//            }
//        }
//    }


    /**
     * 根据文件名得到类型
     *
     * @param format
     * @return
     */
    public static String getFileTypeFormat(String format) {
        String fileType = "";
        if (format.startsWith("image/")) {
            fileType = CLOUD_PICTURE;
        } else if (format.startsWith("video/")) {
            fileType = CLOUD_VIDEO;
        } else if (format.startsWith("audio/")) {
            fileType = CLOUD_AUDIO;
        } else if (format.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml")) {
            fileType = CLOUD_DOCUMENT;
        } else if (format.startsWith("application/vnd.openxmlformats-officedocument.presentationml")) {
            fileType = CLOUD_DOCUMENT;
        } else if (format.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml")) {
            fileType = CLOUD_DOCUMENT;
        }
        if (StringUtils.isBlank(fileType)) {
            switch (format) {
                case "text/plain":
                case "application/pdf":
                case "application/msword":
                case "application/vnd.ms-excel":
                case "application/vnd.ms-powerpoint":
                    fileType = CLOUD_DOCUMENT;
                    break;
                default:
                    fileType = CLOUD_UNKNOWN_FILE_TYPE;
                    break;
            }
        }

        return fileType;
    }



    /**
     * 传入目录名称，忽略删除的文件名
     * 返回成功删除的文件名列表
     * 只处理文件夹下没有目录的情况
     *
     * @param src
     * @param protectedFileNameList
     * @return
     */
    public static List<String> delFilesExceptNameList(String src, List<String> protectedFileNameList) {
        List<String> delSuccessFileNameList = new ArrayList<>();
        try {
            File[] files = new File(src).listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    String fileName = files[i].getName();
                    if (protectedFileNameList.indexOf(fileName) == -1) {
                        files[i].delete();
                        delSuccessFileNameList.add(fileName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return delSuccessFileNameList;
    }

    /**
     *  
     *  * 判断assets文件夹下的文件是否存在 
     *  * 
     *  * @return false 不存在    true 存在 
     *  
     */
    public static boolean isAssetsFileExist(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        try {
            String[] fileNames = assetManager.list("");
            for (int i = 0; i < fileNames.length; i++) {
                if (fileNames[i].equals(fileName.trim())) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 把InputStream转化为ByteArray
     *
     * @param input
     * @return
     */
    public static byte[] inputStream2ByteArray(InputStream input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return output.toByteArray();
    }

    public static File byteArray2File(byte[] byt) {
        File file = new File("");
        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
            bufferedOutput.write(byt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 获取分享文件的Uri
     *
     * @return
     */
    public static Uri getShareFileUri(Intent intent) {
        Bundle extras = intent.getExtras();
        // 判断Intent是否是“分享”功能(Share Via)
        if (extras.containsKey(Intent.EXTRA_STREAM)) {
            Uri uri = extras.getParcelable(Intent.EXTRA_STREAM);
            return uri;
        }
        return null;
    }

    /**
     * 获取分享多个文件的Uri列表
     *
     * @return
     */
    public static List<Uri> getShareFileUriList(Intent intent) {
        Bundle extras = intent.getExtras();
        // 判断Intent是否是“分享多个”功能(Share Via)
        if (extras.containsKey(Intent.EXTRA_STREAM)) {
            ArrayList<Uri> fileUriList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            return (fileUriList == null) ? new ArrayList<Uri>() : fileUriList;
        }
        return new ArrayList<Uri>();
    }

    /**
     * uri转file
     *
     * @param context
     * @param uri
     * @return
     */
    public static File uri2File(Context context, Uri uri) {
        String filePath;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null,
                null, null);
        if (cursor == null) {
            filePath = uri.getPath();
        } else {
            int actualColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            filePath = cursor.getString(actualColumnIndex);
        }
        cursor.close();
        File file = new File(filePath);
        return file;
    }

    /**
     * 判断传入的List下的所有文件是否存在，有一个不存在即返回false
     *
     * @param filePathList
     * @return
     */
    public static boolean isFileInListExist(List<String> filePathList) {
        if (filePathList == null || filePathList.size() == 0) {
            return false;
        }
        for (int i = 0; i < filePathList.size(); i++) {
            if (!isFileExist(filePathList.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 读取指定目录Assets下音频文件。
     *
     * @return 二进制文件数据
     */
    public static byte[] readAudioFile(Context context, String filename) {
        try {
            InputStream ins = context.getAssets().open(filename);
            byte[] data = new byte[ins.available()];
            ins.read(data);
            ins.close();
            return data;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取filePath下的数据，读出格式为byte[]
     *
     * @param filePath
     * @return
     */
    public static byte[] readAudioFileFromSDcard(String filePath) {
        try {
            File file = new File(
                    filePath);
            InputStream inputStream = new FileInputStream(file);
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            return data;
        } catch (Exception e) {
            LogUtils.YfcDebug("异常：" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定文件夹下所有文件的文件路径
     *
     * @param dirPath
     * @return
     */
    public static ArrayList<String> getAllFilePathByDirPath(String dirPath) {
        File file = new File(dirPath);
        File[] files = file.listFiles();
        ArrayList<String> filePathList = new ArrayList<>();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                filePathList.add(files[i].getAbsolutePath());
            }
        }
        return filePathList;
    }

    /**
     * 获取单个文件的MD5值！
     *
     * @param file
     * @return
     */

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    public static String encodeBase64File(String filePath) throws Exception {
        File file = new File(filePath);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return Base64.encodeToString(buffer, Base64.NO_WRAP);
    }

    /**
     * 获得指定文件的byte数组
     */
    public static byte[] file2Bytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * 遍历目录
     *
     * @param filePath
     * @return
     */
    public static List<File> getSubFileList(String filePath) {
        List<File> fileList = new ArrayList<>();
        File rootFile = new File(filePath);
        if (rootFile.exists() && rootFile.isDirectory()) {
            File[] files = rootFile.listFiles();
            for (File file : files) {
                fileList.add(file);
            }
        }

        return fileList;
    }

    /**
     * 遍历目录
     *
     * @param filePath
     * @return
     */
    public static List<File> getSubFileForderList(String filePath) {
        List<File> fileList = new ArrayList<>();
        File rootFile = new File(filePath);
        if (rootFile.exists() && rootFile.isDirectory()) {
            File[] files = rootFile.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    fileList.add(file);
                }
            }
        }

        return fileList;
    }

    /**
     * 读取指定文件目录下的文件名列表
     *
     * @param dicName                         目录名称
     * @param listFileNamesIncludeChildForder 是否需要读取子目录下的文件名
     * @return
     */
    public static List<String> getFileNamesInFolder(String dicName, boolean listFileNamesIncludeChildForder) {
        ArrayList arrayList = new ArrayList();
        if (!StringUtils.isBlank(dicName)) {
            File file = new File(dicName);
            if (file.isDirectory()) {
                for (File fileInPath : file.listFiles()) {
                    if (fileInPath.isFile()) {
                        arrayList.add(fileInPath.getAbsolutePath());
                    } else if (listFileNamesIncludeChildForder && fileInPath.isDirectory()) {
                        getFileNamesInFolder(fileInPath.getAbsolutePath(), listFileNamesIncludeChildForder);
                    }
                }
            }
        }
        return arrayList;
    }

    /**
     * 读取指定文件目录下的所有目录
     *
     * @param dicName
     * @return
     */
    public static List<String> getFileFolderNamesInFolder(String dicName) {
        ArrayList arrayList = new ArrayList();
        if (!StringUtils.isBlank(dicName)) {
            File file = new File(dicName);
            if (file.isDirectory()) {
                for (File fileInPath : file.listFiles()) {
                    if (fileInPath.isDirectory()) {
                        arrayList.add(fileInPath.getAbsolutePath());
                    }
                }
            }
        }
        return arrayList;
    }

    /**
     * 获取文件名：保证在文件夹内不重名
     *
     * @param dirPath
     * @param fileName
     * @return
     */
    public static String getNoDuplicateFileNameInDir(String dirPath, String fileName) {
        String filePrefix = getFilenamePrefix(fileName);
        String fileSuffix = getExtensionNameWithPoint(fileName);
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dirPath, fileName);
        for (int i = 1; file.exists() && i < Integer.MAX_VALUE; i++) {
            file = new File(dirPath, filePrefix + '(' + i + ')' + fileSuffix);
        }
        return file.getName();
    }

    /**
     * 文件夹改名
     *
     * @param src
     * @param dest
     * @return
     */
    private boolean renameToNewFile(String src, String dest) {
        File srcDir = new File(src);
        boolean isOk = srcDir.renameTo(new File(dest));
        return isOk;
    }
}

package com.gov.culturems.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Environment;
import android.util.Log;

import com.gov.culturems.common.CommonConstant;

public class FileUtil {
    public static File updateDir = null;
    public static File updateFile = null;

    /***
     * 创建文件
     */
    public static void createFile(String name) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            updateDir = new File(Environment.getExternalStorageDirectory() + "/" + CommonConstant.downloadDir);
            updateFile = new File(updateDir + "/" + name + ".apk");

            if (!updateDir.exists()) {
                updateDir.mkdirs();
            }
            if (!updateFile.exists()) {
                try {
                    updateFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * delete file
     * 
     * @param file the file need to delete
     * @return if delete the file success,return true, else return false
     */
    public static boolean deleteFile(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                return deleteDirectory(file.getPath());
            } else {
                return file.delete();
            }
        }
        return false;
    }

    /**
     * delete file or directory by its path
     * 
     * @return if delete the file success,return true, else return false
     */
    public static boolean deleteFile(String path) {
        File file = new File(path);
        return deleteFile(file);
    }

    /**
     * delete a folder and all files in this folder
     * 
     * @return if delete the file success,return true, else return false
     */
    private static boolean deleteDirectory(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            return false;
        }
        boolean flag = true;
        File[] files = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    flag = deleteFile(files[i]);
                    if (!flag) break;
                } else {
                    flag = deleteDirectory(files[i].getPath());
                    if (!flag) break;
                }
            }
        }
        if (!flag) return false;
        if (file.delete()) {
            return true;
        } else {
            return false;
        }
    }

    /* 上传文件到Server的方法 */
    /**
     * 
     * @param actionUrl
     * @return
     * @throws IOException
     */
    public static String post(String actionUrl, String FileName) throws IOException {

        String BOUNDARY = java.util.UUID.randomUUID().toString();
        String PREFIX = "--", LINEND = "rn";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String CHARSET = "UTF-8";


        URL uri = new URL(actionUrl);
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
        conn.setReadTimeout(15 * 1000);
        // 缓存的最长时间
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false); // 不允许使用缓存
        conn.setRequestMethod("POST");
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);


        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());


        // 发送文件数据
        if (FileName != "") {

            StringBuilder sb1 = new StringBuilder();
            sb1.append(PREFIX);
            sb1.append(BOUNDARY);
            sb1.append(LINEND);
            sb1.append("Content-Disposition: form-data; name=\"file1\"; filename=\"" + FileName + "\"" + LINEND);
            sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
            sb1.append(LINEND);
            outStream.write(sb1.toString().getBytes());


            InputStream is = new FileInputStream(FileName);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }

            is.close();
            outStream.write(LINEND.getBytes());

        }

        // 请求结束标志
        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
        outStream.write(end_data);
        outStream.flush();

        // 得到响应码
        int res = conn.getResponseCode();
        InputStream in = null;
        if (res == 200) {
            in = conn.getInputStream();
            int ch;
            StringBuilder sb2 = new StringBuilder();
            while ((ch = in.read()) != -1) {
                sb2.append((char) ch);
            }
        }
        return in == null ? null : in.toString();
    }
    
    public static String getHiKidPath() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "hikid");
        if (!file.exists()) {
            file.mkdirs();
        }
        
        Log.e("key_up", "make result is : 22 " + file.getAbsolutePath());
        return file.getAbsolutePath();
    }
    
    public static String getHiKidAvatar() {
        String filename = "image" + System.currentTimeMillis() + ".png";
        File file = new File(getHiKidPath(), filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        Log.e("key_up", "make result is : 22 " + file.getAbsolutePath());
        return file.getAbsolutePath();
    }
    
    
}

package com.example.ycb.mywuzi.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * Created by King on 2015/12/18.
 *
 * http request helper
 */
public class HttpHelper {

    /**
     * the connection instance
     */
    protected HttpURLConnection httpURLConnection;
    public HttpHelper() {
        super();
    }

    /**
     * 简易下载工具
     */

    public interface onDownloadStatueListener {
        public void onDownloading(int total, int current);
        public void onDownloadFinished(String path);
        public void onDownloadError(String error);
    }

    public void simpleDownload (String url, String localPath, String fileName,
                                onDownloadStatueListener listener) {
        simpleDownload(url, localPath, fileName, false, true, 3, listener);
    }

    public void simpleDownload (String url, String localPath, String fileName, boolean isCoverOld,
                                int reTryTTL, onDownloadStatueListener listener) {
        simpleDownload(url, localPath, fileName, isCoverOld, true, reTryTTL, listener);
    }

    public void simpleDownload (final String url, final String localPath, final String fileName,
                                boolean isCoverOld, boolean isUsingThread, final int reTryTTL,
                                final onDownloadStatueListener listener) {

        if (!isCoverOld) {

            File file = new File(localPath+fileName);
            if (file.exists()) {

                listener.onDownloadFinished(localPath+fileName);
                return;
            }

        }

        if (reTryTTL < 0)
            return;
        if (isUsingThread) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    beginDownLoad(url, localPath, fileName, reTryTTL, listener);
                }
            }).start();

        } else {
            beginDownLoad(url, localPath, fileName, reTryTTL, listener);
        }
    }

    private void beginDownLoad(String url, String localPath, String fileName, int TTL,
                               onDownloadStatueListener listener) {


        try {
            URL url1 = new URL(url);

            URLConnection connection =  url1.openConnection();

//                    connection.setRequestProperty("Range", "bytes=2000000-");

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection instanceof HttpURLConnection) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
//                        httpURLConnection.setRequestMethod("POST");
//
//                        httpURLConnection.setDoInput(true);
//                        httpURLConnection.setDoOutput(true);
//                        httpURLConnection.setRequestProperty("connection", "Keep-Alive");
//                        httpURLConnection.setRequestProperty("Charsert", "UTF-8");
//                        httpURLConnection.setInstanceFollowRedirects(true);
//                        httpURLConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
//                        httpURLConnection.connect();
                httpURLConnection.setInstanceFollowRedirects(true);

            }



            InputStream is = connection.getInputStream();

            File dir = new File(localPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(localPath+fileName+".temp");
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file);

            int total = connection.getContentLength();
            int read;
            int sum = 0;
            byte[] buff = new byte[1024];

            long prevMillis = System.currentTimeMillis();

            while ((read = is.read(buff)) != -1) {
                fos.write(buff, 0, read);
                sum+=read;

                long currentMillis = System.currentTimeMillis();
                if (currentMillis - prevMillis >= 500) {
                    prevMillis = currentMillis;
                    //反馈下载进度
                    if (listener != null) {
                        listener.onDownloading(total, sum);
                    }
                }

            }

            fos.close();

            //重命名文�?
            File dst = new File(localPath + fileName);
            if (dst.exists()) {
                dst.delete();
            }
            if (file.renameTo(dst)) {

                if (listener != null) {
                    listener.onDownloading(total, total);
                }

                if (listener != null) {

                    listener.onDownloadFinished(localPath+fileName);
                }
            }

        } catch (Exception ex) {
            int currentTTL = TTL-1;
            if (currentTTL > 0) {
                beginDownLoad(url, localPath, fileName, currentTTL, listener);
            } else {
                if (listener != null) {
                    listener.onDownloadError(ex.getMessage());
                }
            }


        }

    }

}

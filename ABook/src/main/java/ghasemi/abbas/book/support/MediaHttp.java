/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.support;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import ghasemi.abbas.book.general.AndroidUtilities;
import ghasemi.abbas.book.ApplicationLoader;
import ghasemi.abbas.book.general.FileLogs;
import ghasemi.abbas.book.general.TinyData;

public class MediaHttp {

    private String url;
    private ResponseConnection responseConnection;
    private File outputFile;
    private MediaType mediaType;
    private URLConnection urlConnection;
    private HttpsURLConnection httpsURLConnection;
    private HttpURLConnection httpURLConnection;
    private RequestAsyncTask requestAsyncTask;

    private MediaHttp() {

    }

    public static class Builder {

        private final MediaHttp mediaHttp;

        public Builder() {
            mediaHttp = new MediaHttp();
        }

        public Builder setMediaType(MediaType mediaType) {
            mediaHttp.mediaType = mediaType;
            return this;
        }

        public Builder setUrl(String url) {
            mediaHttp.url = url;
            return this;
        }

        public MediaHttp request(ResponseConnection responseConnection) {
            if (mediaHttp.mediaType == null) {
                mediaHttp.mediaType = MediaType.DOWNLOADS;
            }
            if (mediaHttp.url == null) {
                throw new RuntimeException("url is null");
            }
            mediaHttp.responseConnection = responseConnection;
            mediaHttp.request();
            return mediaHttp;
        }

        public String exists() {
            if (mediaHttp.mediaType == null) {
                mediaHttp.mediaType = MediaType.DOWNLOADS;
            }
            if (mediaHttp.url == null) {
                throw new RuntimeException("url is null");
            }
            if (mediaHttp.isFileCached()) {
                return TinyData.getInstance().getStringMD5(mediaHttp.url, TinyData.KeyType.MUSIC);
            }
            return null;
        }

    }

    private void request() {
        if (isFileCached()) {
            responseConnection.onSuccess(TinyData.getInstance().getStringMD5(url, TinyData.KeyType.MUSIC));
            return;
        }
        requestMedia();
    }

    private void requestMedia() {
        TinyData.getInstance().putStringMD5(url, TinyData.KeyType.MUSIC, "");
        requestAsyncTask = new RequestAsyncTask();
        requestAsyncTask.execute(url);
    }

    @SuppressLint("StaticFieldLeak")
    private class RequestAsyncTask extends AsyncTask<String, Long, String> {
        private boolean success = false;

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL _url = new URL(url);
                urlConnection = getUrlConnection(_url);
                urlConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"));
                urlConnection.setRequestProperty("Accept", "*/*");
                urlConnection.connect();
                String contentType = urlConnection.getContentType();

                if (((HttpURLConnection) urlConnection).getResponseCode() != 200 || checkContentType(contentType)) {
                    return "Not found file";
                }

                long len;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    len = urlConnection.getContentLengthLong();
                } else {
                    len = urlConnection.getContentLength();
                }
                publishProgress(len, 0L);
                String type = getType();
                String format = getFormat();
                File file = ApplicationLoader.context.getExternalFilesDir(type);
                String name;
                do {
                    name = Math.abs(new Random().nextLong()) + "_" + Math.abs(new Random().nextInt()) + format;
                } while (new File(file, name).exists());
                @SuppressLint("UsableSpace") long available = ApplicationLoader.context.getExternalFilesDir(type).getUsableSpace();
                if (AndroidUtilities.isExternalStorageWritable() && available > len) {
                    InputStream inputStream = urlConnection.getInputStream();
                    outputFile = new File(file, name);
                    outputFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    final String path = outputFile.getPath();
                    byte[] buffer = new byte[1024];
                    int len1;
                    while ((len1 = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, len1);
                        publishProgress(len, fos.getChannel().size());
                    }
                    fos.close();
                    inputStream.close();
                    TinyData.getInstance().putStringMD5(url, TinyData.KeyType.MUSIC, path);
                    success = true;
                    return path;
                } else {
                    return "No space left on device";
                }
            } catch (final ProtocolException e) {
                FileLogs.e(e);
                deleteFile();
                return e.getMessage();
            } catch (final MalformedURLException e) {
                FileLogs.e(e);
                deleteFile();
                return e.getMessage();
            } catch (final IOException e) {
                FileLogs.e(e);
                deleteFile();
                return e.getMessage();
            }
        }


        @Override
        protected void onProgressUpdate(Long... values) {
            if (responseConnection != null) {
                responseConnection.onProgress(values[0], values[1]);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (responseConnection != null) {
                if (success) {
                    responseConnection.onSuccess(s);
                } else {
                    responseConnection.onError(s);
                }
                responseConnection = null;
            }
            disconnect();
        }
    }

    private boolean checkContentType(String contentType) {
        if (contentType.contains("application/octet-stream")) return false;
        switch (mediaType) {
            case PICTURES:
                return !contentType.contains("image");
            case MUSIC:
                return !contentType.contains("audio");
            case MOVIES:
                return !contentType.contains("video");
            case DOWNLOADS:
                return !contentType.contains("text/calendar") && !contentType.contains("html");
            default:
                throw new RuntimeException("media type is null");
        }
    }

    private String getFormat() {
        String format = url;
        int index = format.indexOf("?");
        if (index != -1) {
            format = format.substring(0, index);
        }
        format = format.replace("/", "").replace("#", "");
        String[] split = format.split("\\.");
        format = split[split.length - 1];
        switch (mediaType) {
            case PICTURES:
                if ("jpg".equals(format) || "png".equals(format) || "jpeg".equals(format)) {
                    return "." + format;
                }
                return ".jpg";
            case MUSIC:
                if ("mp3".equals(format) || "aac".equals(format) || "ogg".equals(format) || "wav".equals(format)) {
                    return "." + format;
                }
                return ".mp3";
            case MOVIES:
                if ("mp4".equals(format) || "svi".equals(format) || "m4p ".equals(format) || "gif".equals(format) ||
                        "mpeg".equals(format) || "mov".equals(format) || "flv".equals(format) || "mkv".equals(format) ||
                        "mpg".equals(format) || "vob".equals(format) || "wmv".equals(format) || "ogv".equals(format)) {
                    return "." + format;
                }
                return ".mp4";
            case DOWNLOADS:
                if (format.length() < 5) {
                    return "." + format;
                }
                return "";
            default:
                throw new RuntimeException("none media type");
        }
    }

    private String getType() {
        switch (mediaType) {
            case PICTURES:
                return Environment.DIRECTORY_PICTURES;
            case MUSIC:
                return Environment.DIRECTORY_MUSIC;
            case MOVIES:
                return Environment.DIRECTORY_MOVIES;
            case DOWNLOADS:
                return Environment.DIRECTORY_DOWNLOADS;
            default:
                throw new RuntimeException("media type is null");
        }
    }

    private void deleteFile() {
        if (outputFile != null && outputFile.exists()) {
            outputFile.delete();
        }
    }

    private boolean isFileCached() {
        String _url = TinyData.getInstance().getStringMD5(url, TinyData.KeyType.MUSIC);
        return !TextUtils.isEmpty(_url) && new File(_url).exists();
    }

    private URLConnection getUrlConnection(URL _url) throws IOException {
        if (url.startsWith("https://")) {
            httpsURLConnection = (HttpsURLConnection) _url.openConnection();
            httpsURLConnection.setRequestMethod("GET");
            return httpsURLConnection;
        } else {
            httpURLConnection = (HttpURLConnection) _url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            return httpURLConnection;
        }
    }

    public synchronized void disconnect() {
        if (responseConnection != null) {
            responseConnection.onError("Download canceled");
            responseConnection = null;
        }
        if (requestAsyncTask != null) {
            requestAsyncTask.cancel(true);
            requestAsyncTask = null;
        }
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
            httpURLConnection = null;
        }
        if (httpsURLConnection != null) {
            httpsURLConnection.disconnect();
            httpsURLConnection = null;
        }
        urlConnection = null;
    }

    public interface ResponseConnection {
        void onProgress(long length, long received);

        void onSuccess(String path);

        void onError(String error);
    }

    public enum MediaType {
        DOWNLOADS, MUSIC, MOVIES, PICTURES
    }
}
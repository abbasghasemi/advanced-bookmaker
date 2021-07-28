/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.support;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import ghasemi.abbas.book.general.FileLogs;
import ghasemi.abbas.book.general.TinyData;

public class CachedProxy implements Runnable {

    private Selector selector;
    private ServerSocketChannel serverChannel;
    private int port;
    private String path;
    private File cacheDir;
    private final ByteBuffer buffer;
    private final byte[] bytes;

    static private final int DEFAULT_BUFFER_SIZE = 1024 * 64;
    static private final int DEFAULT_CONNECT_TIMEOUT = 3000;
    static private final int DEFAULT_READ_TIMEOUT = 3000;

    private final int CONNECT_TIMEOUT;
    private final int READ_TIMEOUT;

    private CachedProxy() {
        this(DEFAULT_BUFFER_SIZE, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    private CachedProxy(int bufferSize, int connectTimeout, int readTimeout) {
        CONNECT_TIMEOUT = connectTimeout;
        READ_TIMEOUT = readTimeout;

        buffer = ByteBuffer.allocateDirect(bufferSize);
        bytes = new byte[buffer.capacity()];
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(null);
            port = serverChannel.socket().getLocalPort();
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            FileLogs.e(new IOException("Proxy initialization failed."));
        }
    }

    public static Uri getUrl(String path) {
        throw new RuntimeException();
//        CachedProxy cachedProxy = new CachedProxy();
//        cachedProxy.path = path;
//        cachedProxy.cacheDir = ApplicationLoader.context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
//        new Thread(cachedProxy).start();
//        return Uri.parse(cachedProxy.createProxyPath());
    }

    private String createProxyPath() {
        return String.format(Locale.US, "http://127.0.0.1:%d/%s", port, path);
    }


    @Override
    public void run() {
        int _try = 1;
        while (!Thread.interrupted()) {
            try {
                selector.select();
                Set<SelectionKey> selected = selector.selectedKeys();
                if (selected.isEmpty()) {
                    continue;
                }
                for (SelectionKey key : selected) {
                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        process(key);
                    }
                }
                selected.clear();
            } catch (Exception e) {
                FileLogs.e(new IOException("Proxy died."));
                if (_try > 10) {
                    break;
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                _try++;
            }
        }
        try {
            selector.close();
            serverChannel.close();
        } catch (Exception e) {
            FileLogs.e(new IOException("Proxy cleanup failed."));
        }
    }

    private void accept(SelectionKey key) throws IOException {
        SocketChannel channel = serverChannel.accept();
        if (channel != null) {
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        }
    }

    private String buildStringFromRequest(SelectionKey key) throws IOException {
        StringBuilder builder = new StringBuilder();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        buffer.clear();
        while (socketChannel.read(buffer) > 0) {
            buffer.flip();
            byte[] dst = new byte[buffer.limit()];
            buffer.get(dst);
            builder.append(new String(dst));
            buffer.clear();
        }
        return builder.toString();
    }

    private ByteArrayInputStream buildResponseHeadersStream(HttpURLConnection conn) {
        String protocol = conn.getHeaderField(null);
        StringBuilder builder = new StringBuilder();
        builder.append(protocol).append("\r\n");
        for (String key : conn.getHeaderFields().keySet()) {
            if (key != null) {
                builder.append(key).append(": ").append(conn.getHeaderField(key)).append("\r\n");
            }
        }
        builder.append("\r\n");
        return new ByteArrayInputStream(builder.toString().getBytes());
    }

    private void process(SelectionKey key) throws IOException {
        GetRequest request = new GetRequest(buildStringFromRequest(key));
        File output = new File(request.getPath());
        long l = Long.parseLong(TinyData.getInstance().getString(Base64.encodeToString(path.getBytes(), Base64.DEFAULT), "0"));
        if (output.exists() && output.length() != 0 && output.length() >= l) {
            FileLogs.print("cached");
            write((SocketChannel) key.channel(), new FileInputStream(output), null);
        } else {
            HttpURLConnection conn = (HttpURLConnection) request.getUrl().openConnection();
            for (String hkey : request.getHeaders().keySet()) {
                FileLogs.print("header " + hkey + ":" + request.getHeaders().get(hkey));
                conn.setRequestProperty(hkey, request.getHeaders().get(hkey));
            }
            long range = 0;
            if (output.exists()) {
                range = output.length();
                FileLogs.print("bytes=" + range);
                conn.setRequestProperty("Range", "bytes=" + range + "-");
            }
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.connect();

            long len;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                len = conn.getContentLengthLong();
            } else {
                len = conn.getContentLength();
            }
            if (range == 0) {
                FileLogs.print("len " + len);
                TinyData.getInstance().putString(Base64.encodeToString(path.getBytes(), Base64.DEFAULT), String.valueOf(len));
            } else {
                if (len + range != l) {
                    range = 0;
                    FileLogs.print("error ranged");
                } else {
                    write((SocketChannel) key.channel(), new FileInputStream(output), null);
                    FileLogs.print("ranged ");
                }
            }
            FileOutputStream fos = range == 0 ? new FileOutputStream(output) : new FileOutputStream(output, true);
            write((SocketChannel) key.channel(), buildResponseHeadersStream(conn), fos);
            write((SocketChannel) key.channel(), conn.getInputStream(), fos);
            fos.close();
            conn.disconnect();
        }

        key.channel().close();
        key.cancel();
    }

    private void write(SocketChannel channel, InputStream stream, FileOutputStream fos) {
        int n;
        try {
            while (-1 != (n = stream.read(bytes))) {
                if (fos != null) {
                    fos.write(bytes, 0, n);
                }
                buffer.clear();
                buffer.put(bytes, 0, n);
                buffer.flip();

                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
            }
        } catch (Exception e) {
            FileLogs.e(new IOException("Write to channel/cache failed."));
        } finally {
            try {
                stream.close();
            } catch (Exception e) {
                FileLogs.e(new IOException("Could not close the stream."));
            }
        }
    }

    private class GetRequest {
        private final HashMap<String, String> headers;
        private URL url;
        private String requestPath;

        public GetRequest(String request) {
            StringBuilder builder = new StringBuilder();
            headers = new HashMap<>();
            for (String line : request.split("\r\n")) {
                if (line.startsWith("GET")) {
                    try {
                        url = new URL(line.split(" ")[1].substring(1));
                    } catch (MalformedURLException e) {
                        FileLogs.e(new MalformedURLException("data source URL is malformed."));
                        url = null;
                    }
                    builder.append(line);
                } else {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        if (!key.equals("Host")) {
                            builder.append(value);
                        }
                        headers.put(key, value);
                    }
                }
            }
            requestPath = TinyData.getInstance().getStringMD5(path, TinyData.KeyType.MOVIES);
            if (TextUtils.isEmpty(requestPath)) {
                String name;
                do {
                    name = Math.abs(new Random().nextLong()) + "_" + Math.abs(new Random().nextInt()) + getFormat();
                } while (new File(cacheDir, name).exists());
                requestPath = new File(cacheDir, name).getPath();
                TinyData.getInstance().putStringMD5(path, TinyData.KeyType.MOVIES, requestPath);
            }
        }

        private String getFormat() {
            String format = path;
            int index = format.indexOf("?");
            if (index != -1) {
                format = format.substring(0, index);
            }
            format = format.replace("/", "").replace("#", "");
            String[] split = format.split("\\.");
            format = split[split.length - 1];
            if ("mp4".equals(format) || "svi".equals(format) || "m4p ".equals(format) || "gif".equals(format) ||
                    "mpeg".equals(format) || "mov".equals(format) || "flv".equals(format) || "mkv".equals(format) ||
                    "mpg".equals(format) || "vob".equals(format) || "wmv".equals(format) || "ogv".equals(format)) {
                return "." + format;
            }
            return ".mp4";
        }

        private HashMap<String, String> getHeaders() {
            return headers;
        }

        private URL getUrl() {
            return url;
        }

        private String getPath() {
            return requestPath;
        }
    }
}
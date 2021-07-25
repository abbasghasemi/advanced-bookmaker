package ghasemi.abbas.book.support.medialoader.tinyhttpd.interceptor;

import ghasemi.abbas.book.support.medialoader.tinyhttpd.request.Request;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.response.Response;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.response.ResponseException;

import java.io.IOException;

/**
 * 拦截器
 *
 * @author wencanyang
 */
public interface Interceptor {
    void intercept(Chain chain) throws ResponseException, IOException;

    /**
     * 拦截器链
     */
    interface Chain {
        Request request();

        Response response();

        void proceed(Request request, Response response) throws ResponseException, IOException;
    }
}
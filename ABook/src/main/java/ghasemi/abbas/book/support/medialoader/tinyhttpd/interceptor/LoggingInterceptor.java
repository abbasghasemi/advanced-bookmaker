package ghasemi.abbas.book.support.medialoader.tinyhttpd.interceptor;

import ghasemi.abbas.book.support.medialoader.tinyhttpd.request.Request;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.response.Response;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.response.ResponseException;
import ghasemi.abbas.book.support.medialoader.utils.LogUtil;
import ghasemi.abbas.book.support.medialoader.utils.Util;

import java.io.IOException;

/**
 * 日志拦截器
 *
 * @author wencanyang
 */
public class LoggingInterceptor implements Interceptor {

    @Override
    public void intercept(Chain chain) throws ResponseException,IOException {
        long t1 = System.nanoTime();
        Request request = chain.request();
        Response response = chain.response();
                LogUtil.e(String.format("Sending request %s with headers %n%s", Util.decode(request.url()), request.headers()));
        chain.proceed(request,response);
        long t2 = System.nanoTime();
        LogUtil.e(String.format("Received response for %s in %.1fms with headers %n%s", Util.decode(request.url()), (t2 - t1) / 1e6d, response.headers()));
    }
}

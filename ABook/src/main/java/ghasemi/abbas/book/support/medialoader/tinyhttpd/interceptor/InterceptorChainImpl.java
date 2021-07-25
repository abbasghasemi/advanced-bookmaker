package ghasemi.abbas.book.support.medialoader.tinyhttpd.interceptor;

import ghasemi.abbas.book.support.medialoader.tinyhttpd.request.Request;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.response.Response;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.response.ResponseException;

import java.io.IOException;
import java.util.List;

/**
 * 拦截器链实现
 *
 * @author vincanyang
 */
public class InterceptorChainImpl implements Interceptor.Chain {

    private final List<Interceptor> mInterceptors;

    private final int mIndex;

    private final Request mRequest;

    private final Response mResponse;

    public InterceptorChainImpl(List<Interceptor> interceptors, int index, Request request, Response response) {
        mInterceptors = interceptors;
        mIndex = index;
        mRequest = request;
        mResponse = response;
    }

    @Override
    public Request request() {
        return mRequest;
    }

    @Override
    public Response response() {
        return mResponse;
    }

    @Override
    public void proceed(Request request, Response response) throws ResponseException, IOException {
        if (mIndex >= mInterceptors.size()) {
            throw new AssertionError();
        }
        InterceptorChainImpl next = new InterceptorChainImpl(
                mInterceptors, mIndex + 1, request, response);
        Interceptor interceptor = mInterceptors.get(mIndex);
        interceptor.intercept(next);
    }
}

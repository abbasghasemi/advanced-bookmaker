package ghasemi.abbas.book.support.medialoader.manager;

import ghasemi.abbas.book.support.medialoader.tinyhttpd.request.Request;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.response.Response;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.response.ResponseException;

import java.io.IOException;

/**
 * Media业务接口
 *
 * @author vincanyang
 */
public interface MediaManager {

    void responseByRequest(Request request, Response response) throws ResponseException, IOException;

    void pauseDownload(String url);

    void resumeDownload(String url);

    void destroy();
}

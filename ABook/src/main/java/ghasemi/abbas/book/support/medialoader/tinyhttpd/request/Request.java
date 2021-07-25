package ghasemi.abbas.book.support.medialoader.tinyhttpd.request;

import ghasemi.abbas.book.support.medialoader.tinyhttpd.HttpHeaders;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.HttpVersion;

/**
 * 请求接口
 *
 * @author vincanyang
 */
public interface Request {

    HttpMethod method();

    String url();

    HttpVersion protocol();

    HttpHeaders headers();

    String getParam(String name);
}

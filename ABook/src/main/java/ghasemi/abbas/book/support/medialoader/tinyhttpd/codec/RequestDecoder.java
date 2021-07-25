package ghasemi.abbas.book.support.medialoader.tinyhttpd.codec;

import ghasemi.abbas.book.support.medialoader.tinyhttpd.request.Request;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.response.ResponseException;

/**
 * {@link Request}的解码器
 *
 * @author vincanyang
 */
public interface RequestDecoder<T extends Request> {

    T decode(byte[] bytes) throws ResponseException;
}

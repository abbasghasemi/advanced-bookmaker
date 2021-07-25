package ghasemi.abbas.book.support.medialoader.tinyhttpd.codec;

import ghasemi.abbas.book.support.medialoader.tinyhttpd.response.Response;

import java.io.IOException;

/**
 * {@link Response}的编码器
 *
 * @author vincanyang
 */
public interface ResponseEncoder<T extends Response> {

    byte[] encode(T response) throws IOException;
}

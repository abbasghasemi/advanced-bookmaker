package ghasemi.abbas.book.support.medialoader.tinyhttpd.codec;

import android.text.TextUtils;

import ghasemi.abbas.book.support.medialoader.tinyhttpd.HttpConstants;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.HttpHeaders;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.HttpVersion;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.request.HttpMethod;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.request.HttpRequest;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.response.ResponseException;
import ghasemi.abbas.book.support.medialoader.tinyhttpd.response.HttpStatus;
import ghasemi.abbas.book.support.medialoader.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * {@link HttpRequest}的解码器
 *
 * @author vincanyang
 */
public class HttpRequestDecoder implements RequestDecoder<HttpRequest> {

    @Override
    public HttpRequest decode(byte[] bytes) throws ResponseException {
        String rawRequest = new String(bytes);
        try {
            //Http协议第1行是Method URI VERSION
            StringTokenizer st = new StringTokenizer(rawRequest);
            if (!st.hasMoreTokens()) {
                throw new ResponseException(HttpStatus.BAD_REQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/originFile.html");
            }
            HttpMethod method = HttpMethod.get(st.nextToken().toUpperCase());
            if (!st.hasMoreTokens()) {
                throw new ResponseException(HttpStatus.BAD_REQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/originFile.html");
            }
            String url = st.nextToken().substring(1);
            // decode params
            int questionMarkIndex = url.indexOf('?');
            Map<String, List<String>> params = new HashMap<>();
            if (questionMarkIndex >= 0) {
                decodeParms(url.substring(questionMarkIndex + 1), params);
                url = Util.decode(url.substring(0, questionMarkIndex));
            } else {
                url = Util.decode(url);
            }
            HttpVersion httpVersion = HttpVersion.get(st.nextToken());
            //第2行起都是KEY:VALUE格式的header
            HttpHeaders headers = new HttpHeaders();
            String[] lines = rawRequest.split(HttpConstants.CRLF);
            for (int i = 1; i < lines.length; i++) {//igore the first line
                String[] keyVal = lines[i].split(HttpConstants.COLON, 2);
                if (!TextUtils.isEmpty(keyVal[0]) && !TextUtils.isEmpty(keyVal[1])) {
                    headers.put(keyVal[0], keyVal[1]);
                }
            }
            return new HttpRequest.Builder().method(method).url(url).version(httpVersion).headers(headers).params(params).build();
        } catch (Exception ex) {
            throw new ResponseException(HttpStatus.INTERNAL_ERROR, "SERVER INTERNAL ERROR: IOException: " + ex.getMessage(), ex);
        }
    }

    private void decodeParms(String queryString, Map<String, List<String>> params) {
        StringTokenizer st = new StringTokenizer(queryString, "&");
        while (st.hasMoreTokens()) {
            String paramStr = st.nextToken();
            int equalMarkIndex = paramStr.indexOf('=');
            String key;
            String value;
            if (equalMarkIndex >= 0) {
                key = Util.decode(paramStr.substring(0, equalMarkIndex)).trim();
                value = Util.decode(paramStr.substring(equalMarkIndex + 1));
            } else {
                key = Util.decode(paramStr).trim();
                value = "";
            }
            List<String> values = params.get(key);
            if (values == null) {
                values = new ArrayList<>();
                params.put(key, values);
            }
            values.add(value);
        }
    }
}

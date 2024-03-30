package http.request;

import http.enumclass.HttpHeader;
import http.enumclass.HttpMethod;
import http.utils.HttpRequestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import static http.enumclass.HttpHeader.CONTENT_LENGTH;
import static http.utils.IOUtils.readData;

public class HttpRequest {
    private final HttpStartline httpStartline;
    private final HttpHeaders httpHeaders;
    private final String httpBody;

    private HttpRequest(HttpStartline httpStartline, HttpHeaders httpHeaders, String httpBody) {
        this.httpStartline = httpStartline;
        this.httpHeaders = httpHeaders;
        this.httpBody = httpBody;
    }

    public static HttpRequest from(BufferedReader br) throws IOException {
        final String requestStartLine = br.readLine();
        if (requestStartLine == null) {
            throw new IllegalArgumentException("request가 비어있습니다.");
        }

        // Header 분석
        final HttpStartline startLine = HttpStartline.from((requestStartLine));
        final HttpHeaders headers = HttpHeaders.from(br);
        final String body = readBody(br, headers);

        return new HttpRequest(startLine, headers, body);
    }

    private static String readBody(final BufferedReader bufferedReader, final HttpHeaders headers) throws IOException {
        if (!headers.contains(CONTENT_LENGTH)) {
            return "";
        }

        final int contentLength = Integer.parseInt(headers.get(CONTENT_LENGTH));

        return readData(bufferedReader, contentLength);
    }
    public Map<String, String> getQueryParam() {
        return httpStartline.getQueryParameters();
    }

    public Map<String, String> getQueryParamsFromBody() {
        return HttpRequestUtils.parseQueryParameter(httpBody);
    }

    public String getUrl() {
        return httpStartline.getUrl();
    }

    public HttpMethod getMethod() {
        return httpStartline.getMethod();
    }

    public String getHttpHeader(HttpHeader httpHeader) {
        if (httpHeaders.contains(httpHeader)) {
            return httpHeaders.get(httpHeader);
        }
        return null;
    }
}

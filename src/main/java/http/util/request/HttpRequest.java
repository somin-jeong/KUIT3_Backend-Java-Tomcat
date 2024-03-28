package http.util.request;

import http.util.utils.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;

import static http.enumclass.HttpHeader.CONTENT_LENGTH;
import static http.enumclass.HttpHeader.COOKIE;

public class HttpRequest {
    private static final String HEADER_SPLIT_REGEX = ": ";
    private static final String STARTLINE_SPLIT_REGEX = " ";
    private final String httpStartline;
    private final String httpHeaders;
    private final String httpBody;
    private final String httpCookie;

    private HttpRequest(String httpStartline, String httpHeaders, String httpBody, String httpCookie) {
        this.httpStartline = httpStartline;
        this.httpHeaders = httpHeaders;
        this.httpBody = httpBody;
        this.httpCookie = httpCookie;
    }

    public static HttpRequest from(BufferedReader br) throws IOException {
        // Header 분석
        String startLine = br.readLine();

        StringBuilder header = new StringBuilder();
        int requestContentLength = 0;
        String cookie = "";

        while (true) {
            final String line = br.readLine();

            if (line.equals("")) {
                break;
            }

            // header info
            if (line.startsWith(CONTENT_LENGTH.getHeader())) {
                requestContentLength = Integer.parseInt(line.split(HEADER_SPLIT_REGEX)[1]);
            }

            if (line.startsWith(COOKIE.getHeader())) {
                cookie = line.split(HEADER_SPLIT_REGEX)[1];
            }

            header.append(line + " \r\n");
        }

        String body = IOUtils.readData(br, requestContentLength);

        return new HttpRequest(startLine, header.toString(), body, cookie);
    }

    public String getMethod() {
        String[] startLines = httpStartline.split(STARTLINE_SPLIT_REGEX);
        return startLines[0];
    }

    public String getUrl() {
        String[] startLines = httpStartline.split(STARTLINE_SPLIT_REGEX);
        return startLines[1];
    }

    public String getHttpStartline() {
        return httpStartline;
    }

    public String getHttpHeaders() {
        return httpHeaders;
    }

    public String getHttpBody() {
        return httpBody;
    }

    public String getHttpCookie() {
        return httpCookie;
    }
}

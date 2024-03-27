package http.util;

import java.io.BufferedReader;
import java.io.IOException;

import static http.enumclass.HttpHeader.CONTENT_LENGTH;
import static http.enumclass.HttpHeader.COOKIE;

public class HttpRequest {
    private static final String STARTLINE_SPLIT_REGEX = " ";
    private static final String HEADER_SPLIT_REGEX = ": ";
    private final String httpStartline;
    private final String httpHeader;
    private final String httpBody;
    private final String httpCookie;

    private HttpRequest(String httpStartline, String httpHeader, String httpBody, String httpCookie) {
        this.httpStartline = httpStartline;
        this.httpHeader = httpHeader;
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

    public String getHttpStartline() {
        return httpStartline;
    }

    public String getHttpHeader() {
        return httpHeader;
    }

    public String getHttpBody() {
        return httpBody;
    }

    public String getHttpCookie() {
        return httpCookie;
    }
}

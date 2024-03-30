package http.request;

import http.enumclass.HttpHeader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class HttpHeaders {
    private static final String DISCRIMINATOR = ": ";
    private final Map<HttpHeader, String> httpHeaders;
    private HttpHeaders(Map<HttpHeader, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public static HttpHeaders from(final BufferedReader bufferedReader) throws IOException {
        return new HttpHeaders(readAllHeaders(bufferedReader));
    }

    private static Map<HttpHeader, String> readAllHeaders(final BufferedReader bufferedReader) throws IOException {
        final Map<HttpHeader, String> headers = new LinkedHashMap<>();

        while (true) {
            final String line = bufferedReader.readLine();
            if (line.equals("")) {
                break;
            }

            final List<String> header = parseHeader(line);
            final String headerType = header.get(0).trim();
            HttpHeader headerKey = HttpHeader.getHeaderInstance(headerType);
            final String headerValue = header.get(1).trim();
            if (headerKey != null) {
                headers.put(headerKey, headerValue);
            }
        }

        return headers;
    }

    private static List<String> parseHeader(final String line) {
        final List<String> header = Arrays.asList(line.split(DISCRIMINATOR));
        validateHeader(header);
        return header;
    }

    private static void validateHeader(final List<String> header) {
        if (header.size() < 2) {
            throw new IllegalArgumentException("요청 정보가 잘못되었습니다.");
        }
    }

    public boolean contains(final HttpHeader httpHeaderType) {
        return httpHeaders.containsKey(httpHeaderType);
    }

    public String get(final HttpHeader httpHeaderType) {
        return httpHeaders.get(httpHeaderType);
    }

    public Set<HttpHeader> keySet() {
        return httpHeaders.keySet();
    }

    public void put(HttpHeader httpHeaderType, final String httpHeader) {
        httpHeaders.put(httpHeaderType, httpHeader);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        httpHeaders.forEach((key,value) -> sb.append(key.getHeader()).append(": ").append(value).append("\r\n"));

        return sb.append("\r\n").toString();
    }
}

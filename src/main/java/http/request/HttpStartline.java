package http.request;

import http.enumclass.HttpMethod;
import http.utils.HttpRequestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpStartline {
    private static final String STARTLINE_SPLIT_REGEX = " ";
    private static final String PARAM_SPLIT_REGEX = "\\?";
    private static final int START_LINE_MIN_LENGTH = 3;
    private final HttpMethod method;
    private final String url;
    private final Map<String, String> query;
    private final String version;
    private HttpStartline(HttpMethod method, String url, Map<String, String> query, String version) {
        this.method = method;
        this.url = url;
        this.query = query;
        this.version = version;
    }

    public static HttpStartline from(String startLine) {
        return parse(startLine);
    }

    private static HttpStartline parse(String startLine) {
        List<String> startLines = Arrays.asList(startLine.split(STARTLINE_SPLIT_REGEX));
        validateStartLineLength(startLines);

        HttpMethod httpMethod = HttpMethod.getHttpMethod(startLines.get(0));
        String[] httpPath = startLines.get(1).split(PARAM_SPLIT_REGEX);
        String httpUrl = httpPath[0];

        Map<String, String> httpQuery = new HashMap<>();
        if (httpPath.length > 1) {
            httpQuery = HttpRequestUtils.parseQueryParameter(httpPath[1]);
        }
        
        String httpVersion = startLines.get(2);

        return new HttpStartline(httpMethod, httpUrl, httpQuery, httpVersion);
    }

    private static void validateStartLineLength(List<String> startLines) {
        if (startLines.size() < START_LINE_MIN_LENGTH) {
            throw new IllegalArgumentException("요청 정보가 잘못되었습니다.");
        }
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getQueryParameters() {
        return query;
    }
}

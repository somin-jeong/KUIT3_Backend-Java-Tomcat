package http.response;

import http.enumclass.HttpStatus;

public class HttpResponseStartLine {
    private static final String httpVersion = "HTTP/1.1";
    private final HttpStatus httpStatusCode;
    private final HttpStatus httpStatus;

    public HttpResponseStartLine(HttpStatus httpStatusCode, HttpStatus httpStatus) {
        this.httpStatusCode = httpStatusCode;
        this.httpStatus = httpStatus;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public HttpStatus getHttpStatusCode() {
        return httpStatusCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

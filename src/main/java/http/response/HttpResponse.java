package http.response;

import http.enumclass.HttpHeader;
import http.enumclass.HttpStatus;
import http.request.HttpHeaders;
import webserver.RequestHandler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static http.enumclass.HttpContentType.CSS_TYPE;
import static http.enumclass.HttpContentType.HTML_TYPE;
import static http.enumclass.HttpHeader.*;
import static http.enumclass.HttpUrl.HTML;

public class HttpResponse {
    private HttpResponseStartLine httpResponseStartLine;
    private final HttpHeaders httpHeaders;
    private final OutputStream os;
    private byte[] body = new byte[0];

    public HttpResponse(OutputStream outputStream) {
        httpHeaders = new HttpHeaders(new HashMap<>());
        os = outputStream;
    }

    public void forward(String path) throws IOException {
        httpResponseStartLine = new HttpResponseStartLine(HttpStatus.STATUS_CODE200, HttpStatus.OK);
        setHeadersAndBody(path);
        write();
    }

    public void redirect(String path, boolean logined) throws IOException {
        httpResponseStartLine = new HttpResponseStartLine(HttpStatus.STATUS_CODE302, HttpStatus.REDIRECT);
        httpHeaders.put(LOCATION, path);
        if (logined) {
            httpHeaders.put(SET_COOKIE, "logined=true");
        }
        write();
    }

    private void setHeadersAndBody(String path) throws IOException {
        body = Files.readAllBytes(Paths.get(path));
        if (path.endsWith(HTML.getUrl())) {
            httpHeaders.put(CONTENT_TYPE, HTML_TYPE.getType() + ";charset=utf-8");
            httpHeaders.put(HttpHeader.CONTENT_LENGTH, String.valueOf(body.length));
            return;
        }
        httpHeaders.put(CONTENT_TYPE, CSS_TYPE.getType());
    }

    private void write() throws IOException {
        os.write((httpResponseStartLine.getHttpVersion() + " " +
                        httpResponseStartLine.getHttpStatusCode().getCode() + " " +
                        httpResponseStartLine.getHttpStatus().getCode() + " \r\n").getBytes());
        os.write(httpHeaders.toString().getBytes());
        os.write(body);
        os.flush();
        os.close();
    }
}

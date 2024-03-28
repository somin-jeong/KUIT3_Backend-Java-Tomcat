package http.response;

import webserver.RequestHandler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static http.enumclass.HttpContentType.CSS_TYPE;
import static http.enumclass.HttpContentType.HTML_TYPE;
import static http.enumclass.HttpHeader.*;
import static http.enumclass.HttpStatusCode.response200;
import static http.enumclass.HttpStatusCode.response302;
import static http.enumclass.HttpUrl.HTML;

public class HttpResponse {
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    DataOutputStream dos;
    byte[] body = new byte[0];

    public HttpResponse(OutputStream os) {
        dos = new DataOutputStream(os);
    }

    public void forward(String path) {
        try {
            body = Files.readAllBytes(Paths.get(path));
            String type = path.endsWith(HTML.getUrl()) ? HTML_TYPE.getType() : CSS_TYPE.getType();
            response200Header(body.length, type);
            responseBody(body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void redirect(String path, boolean logined) {
        if (logined) {
            response302HeaderWithCookie(path);
        } else {
            response302Header(path);
        }
    }

    private void response302HeaderWithCookie(String path) {
        try {
            dos.writeBytes("HTTP/1.1 " + response302.getCode() + " Redirect \r\n");
            dos.writeBytes(LOCATION.getHeader() + ": " + path + "\r\n");
            dos.writeBytes(SET_COOKIE.getHeader() + ": logined=true" +  "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302Header(String path) {
        try {
            dos.writeBytes("HTTP/1.1 " + response302.getCode() + " Redirect \r\n");
            dos.writeBytes(LOCATION.getHeader() + ": " + path + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response200Header(int lengthOfBodyContent, String type) {
        try {
            dos.writeBytes("HTTP/1.1 " + response200.getCode() + " OK \r\n");
            dos.writeBytes(CONTENT_TYPE.getHeader() + ": " + type + ";charset=utf-8\r\n");
            dos.writeBytes(CONTENT_LENGTH.getHeader() + ": " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}

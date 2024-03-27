package webserver;

import db.MemoryUserRepository;
import db.Repository;
import http.enumclass.HttpHeader;
import http.enumclass.HttpMethod;
import http.enumclass.HttpUrl;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static http.enumclass.HttpContentType.CSS_TYPE;
import static http.enumclass.HttpContentType.HTML_TYPE;
import static http.enumclass.HttpHeader.*;
import static http.enumclass.HttpMethod.GET;
import static http.enumclass.HttpMethod.POST;
import static http.enumclass.HttpStatusCode.response200;
import static http.enumclass.HttpStatusCode.response302;
import static http.enumclass.HttpUrl.*;
import static http.enumclass.UserQueryKey.*;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private static final String ROOT_URL = "./webapp";
    private static final String STARTLINE_SPLIT_REGEX = " ";
    private static final String HEADER_SPLIT_REGEX = ": ";
    private static final String QUERYPARAM_SPLIT_REGEX = "\\?";
    private final Repository repository;

    public RequestHandler(Socket connection) {
        this.connection = connection;
        this.repository = MemoryUserRepository.getInstance();
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            byte[] body = new byte[0];

            // Header 분석
            String startLine = br.readLine();
            String[] startLines = startLine.split(STARTLINE_SPLIT_REGEX);
            String method = startLines[0];
            String url = startLines[1];

            int requestContentLength = 0;
            boolean logined = false;

            while (true) {
                final String line = br.readLine();
                if (line.equals("")) {
                    break;
                }

                // header info
                if (line.startsWith(CONTENT_LENGTH.getHeader())) {
                    requestContentLength = Integer.parseInt(line.split(HEADER_SPLIT_REGEX)[1]);
                }

                if (line.startsWith(COOKIE.getHeader()) && line.split(HEADER_SPLIT_REGEX)[1].equals("logined=true")) {
                    logined = true;
                }
            }

            // 요구사항 1.1 - index.html 반환하기
            if (method.equals(GET.getMethod()) && url.endsWith(HTML.getUrl())) {
                body = Files.readAllBytes(Paths.get(ROOT_URL + url));
            }

            if (method.equals(GET.getMethod()) && url.equals(HOME.getUrl())) {
                body = Files.readAllBytes(Paths.get(ROOT_URL + "/index.html"));
            }

            // 요구사항 1.2 - GET 방식으로 회원가입하기
            if (method.equals(GET.getMethod()) && url.startsWith(SIGNUP.getUrl())) {
                Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter(url.split(QUERYPARAM_SPLIT_REGEX)[1]);
                User newUser = new User(queryParameter.get(USERID.getKey()), queryParameter.get(PASSWORD.getKey()), queryParameter.get(NAME.getKey()), queryParameter.get(EMAIL.getKey()));
                repository.addUser(newUser);

                response302Header(dos, HOME.getUrl());
                return;
            }

            // 요구사항 1.3 - POST 방식으로 회원가입하기
            if (method.equals(POST.getMethod()) && url.startsWith(SIGNUP.getUrl())) {
                Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter(IOUtils.readData(br, requestContentLength));
                User newUser = new User(queryParameter.get(USERID.getKey()), queryParameter.get(PASSWORD.getKey()), queryParameter.get(NAME.getKey()), queryParameter.get(EMAIL.getKey()));
                repository.addUser(newUser);

                response302Header(dos, HOME.getUrl());
                return;
            }

            // 요구사항 1.5 - 로그인하기
            if (method.equals(POST.getMethod()) && url.startsWith(LOGIN.getUrl())) {
                Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter(IOUtils.readData(br, requestContentLength));
                User findUser = repository.findUserById(queryParameter.get(USERID.getKey()));
                if (findUser != null && findUser.getPassword().equals(queryParameter.get(PASSWORD.getKey()))) {
                    response302HeaderWithCookie(dos, HOME.getUrl(), true);
                } else {
                    response302Header(dos, "/user/login_failed.html");
                }
                return;
            }

            // 요구사항 1.6 - 사용자 목록 출력
            if (method.equals(GET.getMethod()) && url.startsWith(USERLIST.getUrl())) {
                if (!logined) {
                    response302Header(dos, "/user/login.html");
                    return;
                }
                body = Files.readAllBytes(Paths.get(ROOT_URL + "/user/list.html"));
            }

            // 요구사항 1.7 - CSS 출력
            if (method.equals(GET.getMethod()) && url.endsWith(CSS.getUrl())) {
                body = Files.readAllBytes(Paths.get(ROOT_URL + url));
                response200Header(dos, body.length, CSS_TYPE.getType());
                responseBody(dos, body);
                return;
            }

            response200Header(dos, body.length, HTML_TYPE.getType());
            responseBody(dos, body);
        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private void response302HeaderWithCookie(DataOutputStream dos, String path, Boolean logined) {
        try {
            dos.writeBytes("HTTP/1.1 " + response302.getCode() + " Redirect \r\n");
            dos.writeBytes(LOCATION.getHeader() + ": " + path + "\r\n");
            dos.writeBytes(SET_COOKIE.getHeader() + ": logined=" + logined +  "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 " + response302.getCode() + " Redirect \r\n");
            dos.writeBytes(LOCATION.getHeader() + ": " + path + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String type) {
        try {
            dos.writeBytes("HTTP/1.1 " + response200.getCode() + " OK \r\n");
            dos.writeBytes(CONTENT_TYPE.getHeader() + ": " + type + ";charset=utf-8\r\n");
            dos.writeBytes(CONTENT_LENGTH.getHeader() + ": " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}
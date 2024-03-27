package webserver;

import db.MemoryUserRepository;
import db.Repository;
import http.enumclass.HttpHeader;
import http.enumclass.HttpMethod;
import http.enumclass.HttpUrl;
import http.util.HttpRequest;
import http.util.HttpRequestUtils;
import http.util.HttpResponse;
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

            HttpRequest httpRequest = HttpRequest.from(br);
            HttpResponse httpResponse = new HttpResponse(dos);

            // 요구사항 1.1 - index.html 반환하기  // 요구사항 1.7 - CSS 출력
            if (httpRequest.getMethod().equals(GET.getMethod()) && httpRequest.getUrl().endsWith(HTML.getUrl()) || httpRequest.getUrl().endsWith(CSS.getUrl())) {
                httpResponse.forward(ROOT_URL + httpRequest.getUrl());
            }

            if (httpRequest.getMethod().equals(GET.getMethod()) && httpRequest.getUrl().equals(HOME.getUrl())) {
                httpResponse.forward(ROOT_URL + "/index.html");
            }

            // 요구사항 1.2 - GET 방식으로 회원가입하기
            if (httpRequest.getMethod().equals(GET.getMethod()) && httpRequest.getUrl().startsWith(SIGNUP.getUrl())) {
                Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter(httpRequest.getUrl().split(QUERYPARAM_SPLIT_REGEX)[1]);
                User newUser = new User(queryParameter.get(USERID.getKey()), queryParameter.get(PASSWORD.getKey()), queryParameter.get(NAME.getKey()), queryParameter.get(EMAIL.getKey()));
                repository.addUser(newUser);

                httpResponse.redirect(HOME.getUrl(), false);
                return;
            }

            // 요구사항 1.3 - POST 방식으로 회원가입하기
            if (httpRequest.getMethod().equals(POST.getMethod()) && httpRequest.getUrl().startsWith(SIGNUP.getUrl())) {
                Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter(httpRequest.getHttpBody());
                User newUser = new User(queryParameter.get(USERID.getKey()), queryParameter.get(PASSWORD.getKey()), queryParameter.get(NAME.getKey()), queryParameter.get(EMAIL.getKey()));
                repository.addUser(newUser);

                httpResponse.redirect(HOME.getUrl(), false);
                return;
            }

            // 요구사항 1.5 - 로그인하기
            if (httpRequest.getMethod().equals(POST.getMethod()) && httpRequest.getUrl().startsWith(LOGIN.getUrl())) {
                Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter(httpRequest.getHttpBody());
                User findUser = repository.findUserById(queryParameter.get(USERID.getKey()));
                if (findUser != null && findUser.getPassword().equals(queryParameter.get(PASSWORD.getKey()))) {
                    httpResponse.redirect(HOME.getUrl(), true);
                } else {
                    httpResponse.redirect("/user/login_failed.html", false);
                }
                return;
            }

            // 요구사항 1.6 - 사용자 목록 출력
            if (httpRequest.getMethod().equals(GET.getMethod()) && httpRequest.getUrl().startsWith(USERLIST.getUrl())) {
                if (!httpRequest.getHttpCookie().equals("logined=true")) {
                    httpResponse.redirect("/user/login.html", false);
                    return;
                }
                httpResponse.forward(ROOT_URL + "/user/list.html");
            }

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

}
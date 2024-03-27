package webserver;

import controller.*;
import http.util.HttpRequest;
import http.util.HttpResponse;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import static http.enumclass.HttpMethod.GET;
import static http.enumclass.HttpMethod.POST;
import static http.enumclass.HttpUrl.*;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    Controller controller = new ForwardController();

    public RequestHandler(Socket connection) {
        this.connection = connection;
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
                controller = new ForwardController();
            }

            if (httpRequest.getMethod().equals(GET.getMethod()) && httpRequest.getUrl().equals(HOME.getUrl())) {
                controller = new HomeController();
            }

            // 요구사항 1.2 - GET 방식으로 회원가입하기
//            if (httpRequest.getMethod().equals(GET.getMethod()) && httpRequest.getUrl().startsWith(SIGNUP.getUrl())) {
//                controller = new SignupController(repository);
//            }

            // 요구사항 1.3 - POST 방식으로 회원가입하기
            if (httpRequest.getMethod().equals(POST.getMethod()) && httpRequest.getUrl().startsWith(SIGNUP.getUrl())) {
                controller = new SignupController();
            }

            // 요구사항 1.5 - 로그인하기
            if (httpRequest.getMethod().equals(POST.getMethod()) && httpRequest.getUrl().startsWith(LOGIN.getUrl())) {
                controller = new LoginController();
            }

            // 요구사항 1.6 - 사용자 목록 출력
            if (httpRequest.getMethod().equals(GET.getMethod()) && httpRequest.getUrl().startsWith(USERLIST.getUrl())) {
                controller = new ListController();
            }

            controller.execute(httpRequest, httpResponse);
        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }
}
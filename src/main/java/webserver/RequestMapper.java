package webserver;

import controller.*;
import http.request.HttpRequest;
import http.response.HttpResponse;

import java.util.HashMap;
import java.util.Map;

import static http.enumclass.HttpUrl.*;

public class RequestMapper {
    private final HttpRequest httpRequest;
    private final HttpResponse httpResponse;
    private static final Map<String, Controller> controllers = new HashMap<>();
    private final Controller controller;

    public RequestMapper(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        if (httpRequest.getUrl().endsWith(HTML.getUrl()) || httpRequest.getUrl().endsWith(CSS.getUrl())) {
            controller = new ForwardController();
        } else {
            controller = controllers.get(httpRequest.getUrl());
        }
    }

    static {
        //url, controller가 key value 형태로 저장
        controllers.put(HOME.getUrl(), new HomeController());
        controllers.put(SIGNUP.getUrl(), new SignupController());
        controllers.put(LOGIN.getUrl(), new LoginController());
        controllers.put(USERLIST.getUrl(), new ListController());
    }

    public void proceed() {
        System.out.println("controller = " + controller);
        controller.execute(httpRequest, httpResponse);
    }
}

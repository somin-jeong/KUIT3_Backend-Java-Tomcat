package webserver;

import controller.*;
import http.util.request.HttpRequest;
import http.util.response.HttpResponse;

import java.util.HashMap;
import java.util.Map;

import static http.enumclass.HttpUrl.*;

public class RequestMapper {
    private final HttpRequest httpRequest;
    private final HttpResponse httpResponse;
    private final Map<String, Controller> controllers = new HashMap<>();

    public RequestMapper(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;

        //url, controller가 key value 형태로 저장
        controllers.put(HOME.getUrl(), new HomeController());
        controllers.put(SIGNUP.getUrl(), new SignupController());
        controllers.put(LOGIN.getUrl(), new LoginController());
        controllers.put(USERLIST.getUrl(), new ListController());
    }
    public void proceed() {
        Controller controller = controllers.get(httpRequest.getUrl());
        if (controller == null) {
            controller = new ForwardController();
        }
        controller.execute(httpRequest, httpResponse);
    }
}

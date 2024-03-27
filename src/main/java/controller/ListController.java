package controller;

import http.util.HttpRequest;
import http.util.HttpResponse;

public class ListController implements Controller {
    private static final String ROOT_URL = "./webapp";
    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (!httpRequest.getHttpCookie().equals("logined=true")) {
            httpResponse.redirect("/user/login.html", false);
            return;
        }
        httpResponse.forward(ROOT_URL + "/user/list.html");
    }
}

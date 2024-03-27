package controller;

import http.util.HttpRequest;
import http.util.HttpResponse;

public class ListController implements Controller {
    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (!httpRequest.getHttpCookie().equals("logined=true")) {
            httpResponse.redirect(LOGIN_URL, false);
            return;
        }
        httpResponse.forward(ROOT_URL + LIST_URL);
    }
}

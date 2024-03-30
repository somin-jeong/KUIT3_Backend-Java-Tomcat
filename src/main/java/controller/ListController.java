package controller;

import http.enumclass.HttpHeader;
import http.request.HttpRequest;
import http.response.HttpResponse;

public class ListController implements Controller {
    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (httpRequest.getHttpHeader(HttpHeader.COOKIE) == null ||
                !httpRequest.getHttpHeader(HttpHeader.COOKIE).equals("logined=true")) {
            httpResponse.redirect(LOGIN_URL, false);
            return;
        }
        httpResponse.forward(ROOT_URL + LIST_URL);
    }

}

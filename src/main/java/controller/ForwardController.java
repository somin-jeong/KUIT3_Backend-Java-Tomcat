package controller;

import http.util.request.HttpRequest;
import http.util.response.HttpResponse;

public class ForwardController implements Controller {
    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) {
        httpResponse.forward(ROOT_URL + httpRequest.getUrl());
    }
}

package controller;

import http.util.HttpRequest;
import http.util.HttpResponse;

public class HomeController implements Controller {
    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) {
        httpResponse.forward(ROOT_URL + HOME_URL);
    }
}

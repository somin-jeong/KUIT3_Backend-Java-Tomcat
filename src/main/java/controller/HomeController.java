package controller;

import http.util.HttpRequest;
import http.util.HttpResponse;

public class HomeController implements Controller {
    private static final String ROOT_URL = "./webapp";
    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) {
        httpResponse.forward(ROOT_URL + "/index.html");
    }
}

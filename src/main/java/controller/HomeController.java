package controller;

import http.request.HttpRequest;
import http.response.HttpResponse;

import java.io.IOException;

public class HomeController implements Controller {
    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        httpResponse.forward(ROOT_URL + HOME_URL);
    }
}

package controller;

import http.util.HttpRequest;
import http.util.HttpResponse;

public interface Controller {
    void execute(HttpRequest httpRequest, HttpResponse httpResponse);
}

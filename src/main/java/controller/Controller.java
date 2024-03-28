package controller;

import db.MemoryUserRepository;
import db.Repository;
import http.util.request.HttpRequest;
import http.util.response.HttpResponse;

public interface Controller {
    Repository repository = MemoryUserRepository.getInstance();
    String ROOT_URL = "./webapp";
    String HOME_URL = "/index.html";
    String LIST_URL = "/user/list.html";
    String LOGIN_URL = "/user/login.html";
    String LOGIN_FAILED_URL = "/user/login_failed.html";
    String QUERYPARAM_SPLIT_REGEX = "\\?";
    void execute(HttpRequest httpRequest, HttpResponse httpResponse);
}

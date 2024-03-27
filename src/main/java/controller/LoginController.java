package controller;

import db.MemoryUserRepository;
import db.Repository;
import http.util.HttpRequest;
import http.util.HttpRequestUtils;
import http.util.HttpResponse;
import model.User;

import java.util.Map;

import static http.enumclass.HttpUrl.HOME;
import static http.enumclass.UserQueryKey.PASSWORD;
import static http.enumclass.UserQueryKey.USERID;

public class LoginController implements Controller {
    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) {
        Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter(httpRequest.getHttpBody());
        User findUser = repository.findUserById(queryParameter.get(USERID.getKey()));
        if (findUser != null && findUser.getPassword().equals(queryParameter.get(PASSWORD.getKey()))) {
            httpResponse.redirect(HOME.getUrl(), true);
        } else {
            httpResponse.redirect(LOGIN_FAILED_URL, false);
        }
    }
}

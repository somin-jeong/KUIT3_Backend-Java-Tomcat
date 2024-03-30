package controller;

import http.request.HttpRequest;
import http.utils.HttpRequestUtils;
import http.response.HttpResponse;
import model.User;

import java.util.Map;

import static http.enumclass.HttpUrl.HOME;
import static model.enumclass.UserQueryKey.PASSWORD;
import static model.enumclass.UserQueryKey.USERID;

public class LoginController implements Controller {
    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) {
        Map<String, String> queryParameter = httpRequest.getQueryParamsFromBody();
        User findUser = repository.findUserById(queryParameter.get(USERID.getKey()));
        if (findUser != null && findUser.getPassword().equals(queryParameter.get(PASSWORD.getKey()))) {
            httpResponse.redirect(HOME.getUrl(), true);
        } else {
            httpResponse.redirect(LOGIN_FAILED_URL, false);
        }
    }
}

package controller;

import db.MemoryUserRepository;
import db.Repository;
import http.util.HttpRequest;
import http.util.HttpRequestUtils;
import http.util.HttpResponse;
import model.User;

import java.util.Map;

import static http.enumclass.HttpUrl.HOME;
import static http.enumclass.UserQueryKey.*;
import static http.enumclass.UserQueryKey.EMAIL;

public class SignupController implements Controller {
    private final Repository repository = MemoryUserRepository.getInstance();

    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) {
        // Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter(httpRequest.getUrl().split(QUERYPARAM_SPLIT_REGEX)[1]);
        Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter(httpRequest.getHttpBody());
        User newUser = new User(queryParameter.get(USERID.getKey()), queryParameter.get(PASSWORD.getKey()), queryParameter.get(NAME.getKey()), queryParameter.get(EMAIL.getKey()));
        repository.addUser(newUser);

        httpResponse.redirect(HOME.getUrl(), false);
    }
}
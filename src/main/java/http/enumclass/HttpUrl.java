package http.enumclass;

public enum HttpUrl {
    HOME("/"), SIGNUP("/user/signup"), LOGIN("/user/login"), USERLIST("/user/userList"), HTML(".html"), CSS(".css");

    private final String url;

    HttpUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}

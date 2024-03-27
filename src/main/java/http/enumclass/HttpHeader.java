package http.enumclass;

public enum HttpHeader {
    CONTENT_LENGTH("Content-Length"), CONTENT_TYPE("Content-Type"), COOKIE("Cookie"), LOCATION("Location"), SET_COOKIE("Set-Cookie");

    private final String header;

    HttpHeader(String header){
        this.header = header;
    }

    public String getHeader(){
        return header;
    }
}

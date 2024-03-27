package http.enumclass;

public enum HttpStatusCode {
    response302("302"), response200("200");

    private final String code;

    HttpStatusCode(String code){
        this.code = code;
    }

    public String getCode(){
        return code;
    }
}

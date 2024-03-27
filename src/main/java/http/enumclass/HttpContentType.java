package http.enumclass;

public enum HttpContentType {
    HTML_TYPE("text/html"), CSS_TYPE("text/css");

    private final String type;

    HttpContentType(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }
}

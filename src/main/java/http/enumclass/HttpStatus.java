package http.enumclass;

public enum HttpStatus {
    OK("OK"), REDIRECT("FOUND"), STATUS_CODE302("302"), STATUS_CODE200("200");

    private final String code;

    HttpStatus(String code){
        this.code = code;
    }

    public String getCode(){
        return code;
    }
}

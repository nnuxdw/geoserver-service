package nnu.ogms.basins.common;

public class GeneralException extends RuntimeException {
    private Integer code;
    private Integer httpCode;
    private String message;
    private Object data;
    private Object[] args;

    public GeneralException(ErrorEnum errorEnum) {
        this.code = errorEnum.getCode();
        this.httpCode = errorEnum.getHttpCode();
        this.message = errorEnum.getMessage();

    }

    public GeneralException(ErrorEnum errorEnum,Object data) {
        this.code = errorEnum.getCode();
        this.httpCode = errorEnum.getHttpCode();
        this.message = errorEnum.getMessage();
        this.data = data;

    }

    public GeneralException(ErrorEnum errorEnum,Object data,Object[] args) {
        this.code = errorEnum.getCode();
        this.httpCode = errorEnum.getHttpCode();
        this.message = errorEnum.getMessage();
        this.data = data;
        this.args = args;
    }

    public GeneralException(ErrorEnum errorEnum,String message) {
        this.code = errorEnum.getCode();
        this.httpCode = errorEnum.getHttpCode();
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(Integer httpCode) {
        this.httpCode = httpCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}

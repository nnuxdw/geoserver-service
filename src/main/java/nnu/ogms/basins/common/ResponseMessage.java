package nnu.ogms.basins.common;

import static nnu.ogms.basins.common.Common.SUCCESS_CODE;
import static nnu.ogms.basins.common.Common.SUCCESS_MSG;

public class ResponseMessage<T> {

    private Integer errCode;

    private Integer code;

    private String errMsg;

    private T data;

    public ResponseMessage() {

    }

    public ResponseMessage(Integer errCode, String errMsg, Integer code, T data) {
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.data = data;
        this.code = code;
    }

    public static ResponseMessage success(Object object, String message){
        ResponseMessage<Object> responseMessage = new ResponseMessage<>();
        responseMessage.setErrCode(SUCCESS_CODE);
        if (message == null){
            responseMessage.setErrMsg(SUCCESS_MSG);
        }else {
            responseMessage.setErrMsg(message);

        }
        responseMessage.setData(object);
        return responseMessage;

    }

    public static ResponseMessage success(Object object){
        return success(object,null);

    }

    public static ResponseMessage success(){
        return success(null,null);
    }

    public Integer getErrCode() {
        return errCode;
    }

    public void setErrCode(Integer errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}

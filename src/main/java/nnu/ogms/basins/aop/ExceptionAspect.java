package nnu.ogms.basins.aop;

import lombok.extern.slf4j.Slf4j;
import nnu.ogms.basins.common.GeneralException;
import nnu.ogms.basins.common.ResponseMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Component
@ControllerAdvice
@Slf4j
public class ExceptionAspect {

    @ResponseBody
    @ExceptionHandler(GeneralException.class)
    public ResponseMessage handleException(GeneralException e){
        log.error("系统异常信息：", e);
        ResponseMessage result = new ResponseMessage();
        result.setErrCode(e.getHttpCode());
        result.setErrMsg(e.getMessage());
        result.setCode(e.getCode());
        return result;
    }
}

package nnu.ogms.basins.common;

import lombok.Data;

public enum ErrorEnum {
    QUERY_SCOPE_ERROR(10001,400,"查询范围错误"),
    LONGITUDE_SCOPE_ERROR(10002,400,"纬度范围错误"),
    LATITUDE_SCOPE_ERROR(10002,400,"经度范围错误"),
    FILE_NOT_EXIST_ERROR(10002,400,"文件不存在"),
    DATASTORE_EXIST_ERROR(10003,400,"数据已发布过，不能重复发布，请修改 dateset name"),
    LAYER_EXIST_ERROR(10004,400,"图层已存在，不能重复发布，请修改 layer name"),
    SHP_FILE_NOT_ZIP_ERROR(10005,400,"shp 源文件必须为 zip 压缩包文件"),
    PUBLISH_LIST_EMPTY_ERROR(10006,400,"前端传入数据不能为空"),
    LAYER_GROUP_EXIST_ERROR(10007,400,"图层组已存在，不能重复发布"),
    LAYER_NOT_EXIST_ERROR(10004,400,"图层不存在"),
    ;

    private Integer code;
    private Integer httpCode;
    private String message;

    ErrorEnum(Integer code, Integer httpCode, String message) {
        this.code = code;
        this.httpCode = httpCode;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
